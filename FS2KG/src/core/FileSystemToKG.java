package core;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Scanner;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.JFileChooser;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

//import b_JenaExamples.prefix;

/**
 * It  scans all files and subfolders of  a given folder
 * and expresses that structure in RDF.
 * It supports a modular configuration approach relying on .kg files and various conventions
 * 
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *
 */
public class FileSystemToKG {
	static String appName = "FS2KG v0.1";
	static int numOfFiles=0;
	static int numOfFolders=0;
	static int numOfClasses=0;
	static int numOfEntities=0;
	static int numOfdotKGfiles=0;
	static int numOfIgnoredFiles=0;
	static int numOfComments=0;
	static int numOfExtractionPairs=0;
	static int numOfExtraTriples =0;
	
	static FileWriter fw;
	
	static  MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
	
	/**
	 * Fixes a URI (adds a missing / after file) 
	 * @param uri
	 * @return
	 */
    public static String fix(URI uri) {
    	String uriS = uri.toString();
    	String toRet ="";
    	if (uriS.substring(0, 6).equals("file:/")) {
    		toRet = "file://" +  uriS.substring(6);
    		//System.out.println(uriS+">>>>"+toRet+"<<<<");
    	} else {
    		toRet=uri.toString();
    	}
    	return toRet;
    }
    	
	/**
	 * Dialog for selecting a folder
	 * mode:  JFileChooser.DIRECTORIES_ONLY, 
	 * @return
	 */
	public static String selectAFolderDialog(int mode) {
		String filePath ="";
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(mode);  // JFileChooser.DIRECTORIES_ONLY
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
		    File selectedFile = fileChooser.getSelectedFile();
		    filePath= selectedFile.getAbsolutePath();
		    System.out.println("Selected file/folder: " +  filePath);
		}
		return filePath;
	}

		private static String headerStr =
				"@prefix : <http://www.ics.forth.gr/example#> .\r\n" + 
				"@prefix ex: <http://www.ics.forth.gr/example> .\r\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\r\n" + 
				"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" + 
				"@prefix tmp: <http://www.semanticweb.org/maria/ontologies/2021/9/running_example#> .\r\n" + 
				"@prefix xml: <http://www.w3.org/XML/1998/namespace> .\r\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\r\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\r\n" + 
				"@prefix example: <http://www.ics.forth.gr/example#> .\r\n" + 
				"@base <http://www.ics.forth.gr/example> .\r\n" + 
				"\r\n" + 
				"<http://www.ics.forth.gr/example> rdf:type owl:Ontology .\n\n "
				+ "example:SemanticNetwork rdf:type owl:Class; rdfs:label  \"SemanticNetwork\". \n\n "
				+ ""
				+ "example:moreAt rdf:type owl:ObjectProperty.  \n\n"
				+ "";
	
	/**
	 * It returns a triple for param file stating that it is NamedIndividual
	 * and instance of the class corresponding to its folder
	 * @param file
	 * @return
	 */
	public static String file2triple(File file) {
		String ts = "<"+fix(file.toURI())+">"    	+ " rdf:type owl:NamedIndividual; " +
				"  rdf:type <" + fix(file.getParentFile().toURI())  +  ">.\n\n"; 
		//System.out.println(ts);
		return ts;
	}
	
	/**
	 * It returns a triple for param folderfile stating that it is a class,
	 * its label and subclassOf its parent folder.
	 * It also supports the kg option "subFoldersClass"
	 * @param file
	 * @param includeParent
	 * @param kg the KGproperties
	 * @return
	 */
	public static String folderFile2triple(File file, boolean includeParent, KGproperties kg) {
		String ts="";
		
		if (includeParent) {  // meaning that this folder has to be related with the parent folder
			if (kg!=null) { // i.e. if .kg exists
				// Property: subFoldersCalls
				if (kg.v_subFoldersClass!=null) { // there is a property in .kg for subFolder's classes
					String typeOftoAdd ="";
					String[] 	subFoldersClassArray = kg.v_subFoldersClass.split(";"); // getting all classes
					for (String classString: subFoldersClassArray) {
						typeOftoAdd += " rdf:type " + classString + "; ";
						
					}
					//System.out.println(">>>>" + typeOftoAdd);
					
					// Policy: Create an additional semantic entity with suffix: folder name +  "_entity"
					//ts = "<"+file.toURI()+"_entity>"    	+
					ts = "example:"+file.getName()+"_entity"   	+           // their id is independent of the location
							typeOftoAdd +								    // rdfs:type triples
							//" rdf:type " +  kg.v_subFoldersClass + ";\n" +
							" rdfs:label  \"" + file.getName()+"_entity" + "\";" +
							" example:moreAt "+ "<"+fix(file.toURI())+">.\n\n"; //connection with the ressolvable URI of the folder
					//For grouping these semantic classes under a class "SemanticNetwork":
					String tsExtra ="";
					for (String classString: subFoldersClassArray) {
						tsExtra += classString + " rdfs:subClassOf example:SemanticNetwork .\n";
						
					}
					
					//tsExtra = kg.v_subFoldersClass+" rdfs:subClassOf example:SemanticNetwork .\n";
					ts = ts + tsExtra;
					numOfEntities++;
					
					try { // for adding a provenance comment inside the ttl file
						fw.write("\n#\n#  from file " + file.getAbsolutePath() +"\n");
					} catch(Exception e) {System.out.println(e);}
				} 
			} 
			
			// default mode: subClassOf of the parent folder class
			ts += "<"+fix(file.toURI())+">"    	+
						" rdf:type owl:Class; \n" +
						" rdfs:label  \"" + file.getName() + "\";" +
						" rdfs:subClassOf <"+ fix((file.getParentFile().toURI()))+">.\n\n"; // sos problem
			numOfClasses++;
		
		} else {  //  Useful only for the first call, on the root folder (i.e. no need to relate it with parent folder) 
			ts = "<"+fix(file.toURI())+">"    	+
					" rdf:type owl:Class; \n" +
					" rdfs:label  \"" + file.getName() + "\".\n\n"; //
		}
		//System.out.println(ts);
		return ts;
	}
   
	/**
	 * This is the recursive function that traverses the filesys and produces the KG
	 * @param folder
	 * @param initialCall
	 * @throws Exception
	 */

    public static void traverseFolderFiles(File folder, boolean initialCall) throws Exception {
    	KGproperties kg=null;
    	
    	if (initialCall) { // for the parent folder
    		fw.write(folderFile2triple(folder, false, kg)); 
    	}
    	
    	// searching for a ".kg" file
    	for (File fileEntry : folder.listFiles()) {
    		if (fileEntry.getName().equals(".kg")) { // if .kg file exists, then I create such an object
    				//System.out.print("\nFound " + fileEntry.getName() + " at " + folder.getName() + ": ");
    				kg = new KGproperties(fileEntry.getAbsolutePath());
    				numOfdotKGfiles++;
    				
    				if (kg.getTraverse()==false) // stop traversal -> ignore this folder
    		    		return ; 
    				
    				// readme => rdfs comment
    				if (kg.v_readme!=null) {
    					//System.out.println("TODO: One comment here");
    					// if the folder contains a readme.txt file
    					for (File f1 : folder.listFiles()) {
    						if (f1.getName().toLowerCase().equals("readme.txt")) {
    							//System.out.println("Bingo (found readme to become comment) at " + folder.getName());
    							fw.write("<"+fix(folder.toURI())+"> rdfs:comment \"" + file2String(f1)+ "\".\n\n");
    							numOfComments++;
    						}
    					}	 
    				}
    				
    			
    				// extra Triples (in the .kg file)
    				String extraTriplesRow = kg.kgProps.getProperty("extraTriples");
    				if (extraTriplesRow!=null) {
    					//System.out.println("extraTripleFound "+ extraTriplesRow);
    					fw.write("\n#\n#  from file " + fileEntry.getAbsolutePath() +"\n");
    					String[] 	extraTriplesArray = extraTriplesRow.split(";");
    					for (String triple: extraTriplesArray) {
    						fw.write(triple + " .\n");
    						numOfExtraTriples++;
    					}
    				}
    				
    				
    				
    		} // if .kg existed
    	}
    	
    	    	
    	// traversal of all file entries
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {  // if it is a subfolder
            	numOfFolders++;
            	fw.write(folderFile2triple(fileEntry,true,kg));
                	
                traverseFolderFiles(fileEntry,false); // recursive calls
            } else {  // plain file
            	numOfFiles++;
            	if (kg!=null) { // if the folder contains a .kg so we should check about .ignore files
            		if (kg.shouldIgnore(fileEntry.getName()))  {// if the file has extension to be ignored
            			numOfIgnoredFiles++;
            			continue;
            		}
            	}
            	fw.write(file2triple(fileEntry));
            	
            	// EXTRACTIONO PART
            	/* Idea:    if fileEntry.extension =.kg then  if exists file with the same basic name
                   	 then we should try to extract data from that file
                */   	 
            	if (KGproperties.getFileExtension(fileEntry.getName()).equals("kg")) {  // if the current file has extension .kg
            		//System.out.println("> I found a KG extraction file");
            		String basicName = KGproperties.getFileNameWithoutExtension(fileEntry.getName());
            		//System.out.println(">> I will search for a file with name " + basicName + ".*");
            		
            		for (File fe : folder.listFiles()) { // scans all files to see if there is a file with the same basic name
            			String feBasicName = KGproperties.getFileNameWithoutExtension(fe.getName());
            			String feExtension = KGproperties.getFileExtension(fe.getName());
            			if (feBasicName.equals(basicName) && (!feExtension.equals("kg"))) { // found such a file
                		//if (fe.getName().equals(basicName + ".kg")) {
                			System.out.println("Will try to extract data from " + fe.getName() +". ");
                			numOfExtractionPairs++;
                			
                			performExtraction(fe,fileEntry);   //extraction	
                		}
            		} // for finding the pair (basic.kg  basic.xxx)			
            		
            	} // if current file has extension kg
            	
            } // plain file (not folder)
        } // for over all file entries
    }
    
    /**
     * Transforms a csv file to triples based on rules in the fileKG file
     * convention CX refers to X-th column
     * convention C0 refers to the filename 
     * @param file the file with the data
     * @param fileKG the file with the KG generation rules
     */
    
    static void performExtraction(File file, File fileKG) {
    	// Part A. Open the file fileKG file and read its properties	
    	KGproperties   	dkg = new KGproperties(fileKG.getAbsolutePath());
    	//dkg.showAll();
    	int K=20; // max num of columns. SOS=to make it constant
    	String[] C = new String[K];  // for reading the C properties of the .kg
    	for (int i=0;i<K;i++) {
    		C[i] = dkg.kgProps.getProperty("C"+i);
    		//System.out.println("C"+i+":"+C[i]); 
    	}
    	String R =  dkg.kgProps.getProperty("R"); // the line of the .kg file with the rules
    	String[] Rules = null; 
    	if (R!=null) {
			Rules = R.split(";"); // putting all read rules in an array
		}
    	
    	boolean isCSVfile = C[0]==null; // Convention (if C[0]<>null then it refers to the filename)
    	//System.out.println("Is a csv file: " + isCSVfile);
		
    	// Part B. Open the data file and read its contents  (ongoing: or just its file name)
    	String[][] file2Ddata; // will store the data as a 2D array   (only file name if not a csv file)
    	if (isCSVfile) {
	    	CSVReader fileR = new CSVReader(file.getAbsolutePath());
			file2Ddata = fileR.readContentsAs2DArrayOfString(";", false); // reads the CSV files as a 2D array
	    } else { //not CSV file  (in that case only the URI of the filename is needed)
	    	String[][] dummy = new String[1][1]; // we need space just for one value (the filename)
	    	dummy[0][0]= "<"+fix(file.toURI())+">";
	    	//System.out.println("C0 extracted="+dummy[0][0]);
	    	file2Ddata = dummy;
	    }
    	
		// print the 2d array by row (for testing)
		/*
		System.out.println("--data file begin -----------------------");
		Arrays.stream(file2Ddata)
	    .map(a -> String.join(" ", a))
	        .forEach(System.out::println);
		System.out.println("--data file end  -----------------------");
		*/
    	
    	// Part C.  Writing the output
		// Part C1:  Entities and their classes
		try {
			fw.write("\n#\n#  from file " + file.getAbsolutePath() +"\n#\n"); // comments for provenance reasons
			if (!isCSVfile) { 
				 String triple = file2Ddata[0][0] + " rdf:type " + C[0]  +".\n";
				 fw.write(triple);
				 //System.out.println("FromNotCSVFile: " +triple);
				 fw.write(C[0] + " rdfs:subClassOf example:SemanticNetwork .\n");   	 
			 }
			if (isCSVfile) {
			 for (int rows=0; rows< file2Ddata.length ; rows++) {
				 for (int cols=0; cols<file2Ddata[rows].length; cols++) {
					 
					
						 if (C[cols+1]!=null) { // exists kgproperty about the class
							
							//ongoing (gia na mhn ksanadilonetai to rdfs:Literal ws class // den douleuei omws h synthiki
							if ( (!C[cols+1].substring(0, 4).equals("rdfs"))
								      &&
							     (!C[cols+1].substring(0, 3).equals("owl"))
							     	&&
							     	(!C[cols+1].substring(0, 3).equals("rdf"))
							   ) // if the class is predefined so no need to define it 
								
							//if ((!file2Ddata[rows][cols].substring(0, 4).equals("rdfs"))
							//      &&
							//      (!file2Ddata[rows][cols].substring(0, 3).equals("owl"))) // if the class is predefined so no need to define it 
							{  
								 fw.write("example:"+file2Ddata[rows][cols] + " rdf:type " + C[cols+1]  +".\n");
								 numOfEntities++;
								 if (rows==0) { // gia na dilwsw tis klaseis subc of SemanticNet mia fora mono kai TO PROVENANCE
									 fw.write(C[cols+1] + " rdfs:subClassOf example:SemanticNetwork .\n");   	 
								 }
							 
						 	}
							// ongoing: PROVENANCE: for recording provenance (for classes only)
							 String provenanceProperty = dkg.kgProps.getProperty("provenance");
							 if (provenanceProperty!=null) {
								 if (provenanceProperty.equals("on")) {
									 String defBy = fix(file.toURI());  // sos
									//String defBy = file.toURI().toString().replace("a","b");
									 
									 fw.write("example:"+file2Ddata[rows][cols] + " rdfs:isDefinedBy <"+defBy+"> .\n");  // entity
									//fw.write(C[cols+1] + " rdfs:isDefinedBy <"+file.toURI()+"> .\n");  // class
								 }
							 } // provenance on
							 
					
						 } // C[cols+1]!=null
					  } // columns
				 } // for rows
			} // is csv file
						
		// Part C2:  Creation of Relationships   (tocheck: to eliminate some redudancies in the output)
		if (R!=null) {	
			for (String rule: Rules ) {   // for each rule
				//System.out.println(">>>"+rule);
				String[] RuleParts=rule.split(","); // reading the 3 parts of each rule
				int left = Integer.parseInt(RuleParts[0].substring(1)); // from C1 we get 1
				int right = Integer.parseInt(RuleParts[2].substring(1)); // from CX we get X
				//System.out.println(">>>" + left + ">>>" + right);
				
				// application of the rules for NOT  CSV files
				if (!isCSVfile) {
					String subj = file2Ddata[0][0];// filename
					String pred = RuleParts[1];
					String obj  = RuleParts[2];
					
					String triple = subj + " " + pred + " " + obj + ".\n";
					//System.out.println("TripleFromNotCSVFile: " + triple);
					fw.write(triple);
				}
				// application of the rules for CSV files
				if (isCSVfile) {  
					//System.out.println("ROWS="+file2Ddata.length +  " LEFT-1:=" + (left-1) + " RIGHT-1:=" + (right-1) );
					for (int rows=0; rows< file2Ddata.length ; rows++) {  // scan each line
						String subj = "example:"+ file2Ddata[rows][left-1];  // the subject is a URI created with nspace "example"
						String pred = RuleParts[1]; // the predicate is specified by the 2nd string of the rule
						
						//boolean noNeedsPrefix =  file2Ddata[rows][right-1].substring(0, 4).equals("rdfs") ||
						//						 file2Ddata[rows][right-1].substring(0, 3).equals("owl"); 
						
						boolean noNeedsPrefix = isPredefinedSchemaURI(file2Ddata[rows][right-1]); // checks if the object is predefined
						
					
						String obj  = (noNeedsPrefix) ? file2Ddata[rows][right-1] : 
							                            "example:" + file2Ddata[rows][right-1];  // creates the URI of the object
						
						String triple = subj + " " + pred + " " + obj + ".\n";
						//System.out.println("Extracted Triple: " + triple);
						fw.write(triple);
					}
				}
				
				//Declaration of owl ObjectProperties if it is required
				/*
				E.g.
				bakery:hasIngredient rdf:type owl:ObjectProperty ;
				   rdfs:domain bakery:BakeryGood ;
				   rdfs:range bakery:Ingredient .
				*/
				
				if ((!RuleParts[1].substring(0, 4).equals("rdfs"))
					&&
					(!RuleParts[1].substring(0, 3).equals("owl"))
					)
				{ // if not an rdfs or owl property			
					String triple = RuleParts[1] + " rdf:type owl:ObjectProperty.  \n";
					fw.write(triple);
				}
			}
		}
			
			
			
		}  catch(Exception e ) {
	    		System.out.println(e);
	    }
		
    }
    
    /**
     * Returns true if p is the name of a URI with namespace rdfs or owl
     * @param p
     * @return
     */
    static boolean isPredefinedSchemaURI(String p) {
    	if (p.length()>=4) 
    		if (p.substring(0, 4).equals("rdfs"))
    			return true;
    	
    	if (p.length()>=3) 
    		if (p.substring(0, 3).equals("owl"))
    			return true;
    	
    	return false;
    }
    
    
    /**
     * Reads a file and returns its contents as a string
     * @param f1 File
     * @return
     */
	private static String file2String(File f1) {
		try {
		//BufferedReader reader = new BufferedReader(new FileReader (file));
		BufferedReader reader = new BufferedReader(new FileReader (f1.getAbsolutePath()));
		
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(" ");
	            //stringBuilder.append(ls);
	        }
	        return stringBuilder.toString();
	       
	    } finally {
	        reader.close();
	    }
	    
		} catch(Exception e) {
			System.out.println(e);
		}
		 
		return "-I CANNOT READ FILE " + f1.getAbsolutePath();
	}

	/**
     * Starts the traveral and the creation of the knowledge graph
     * @param startupfolder the folder where scan should begin
     * @param filenameToWrite  the file to write the produced RDF triples
     */
    public static void traverseAndCreateKG(String startupfolder, String filenameToWrite) {
		
		try {
		//A. Selecting the folder to scan   
		File folder; 
		if (startupfolder==null) {
			folder = new File( selectAFolderDialog(JFileChooser.DIRECTORIES_ONLY));  // file selection dialog
		} else {
			folder = new File(startupfolder);
		}
	    	
    	System.out.println("Start scanning " + folder.getAbsolutePath());
    	
    	Instant start = Instant.now(); // for measuring elapsed time
    	    	
    	//B. Creating the output file with the header
    	fw = new FileWriter(filenameToWrite,false); // overwrite file if it exists
		fw.write(headerStr);
		
    	//C. Starting the traveral
    	traverseFolderFiles(folder,true);
    	
    	//D. Closing the file
    	fw.close();
    	
    	// Elapsed time        
    	Instant finish = Instant.now();
    	Duration timeElapsed  = Duration.between(start, finish);
    	
    	//E. Printing statistics
    	System.out.println("\n\nEnd of scan and KG generation. ");
    	//System.out.printf("# of %s: %d \n", "asdfsdf", 3);
    	String format = "# of %-18s: %d \n";
    	
    	System.out.printf(format, "folders", numOfFolders);
        System.out.printf(format, "files", numOfFiles);
        System.out.printf(format, "kg files", numOfdotKGfiles);
        System.out.printf(format, "ignored files", numOfIgnoredFiles);
        System.out.printf(format, "classes", numOfClasses);
        System.out.printf(format, "dom-spec. entities", numOfEntities);
        System.out.printf(format, "comments", numOfComments);
        System.out.printf(format, "extractions from files", numOfExtractionPairs);
        System.out.printf(format, "extra triples",numOfExtraTriples);
        System.out.println("Elapsed time in secs: " + timeElapsed.getSeconds() );
        
        System.out.printf("OUTPUT WRITTEN AT: " + filenameToWrite + " (size %.3f MB) ", ((float)Files.size(Paths.get(filenameToWrite)))/(1024*1024));
        
		} catch (Exception e) {
			System.out.println(e);
		}
	}
    
    /**
     * Opens a file using the OS-related application (if any)
     * @param file
     */
    static void open(String file) {
    	try {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().open(new File(file));
			    //Desktop.getDesktop().browse(new URI(urlwithParam));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
    }
    
    
    public static void main(String[] args) throws URISyntaxException {   	
    	System.out.println(appName);
        //String startFolder = null;  // for opening the file selection frame
    	String startFolder = "C:\\Users\\tzitzik\\Documents\\YandT\\Yannis\\My_TEACHING\\CS_561\\Software\\FStoKGtestDataFolder\\DemoFolder";
    	String outputFile  =  "datafilesOutput/fileSystemKG.ttl";  
    	traverseAndCreateKG(startFolder, outputFile);
    	//open(outputFile);  // open the ttl with the OS default app
    }
}
