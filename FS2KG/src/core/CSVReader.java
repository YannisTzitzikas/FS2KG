package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Utility class for reading a csv file and placing its contents in a 2D array.
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *
 */

public class CSVReader {
	//private FileReader fr;
	// bfr1;
	private String filepath;
	private FileReader fr ;
	private BufferedReader bfr ;
	
	public CSVReader(String filepath) {
    	 try {
    	 fr = new FileReader(filepath);
    	 bfr = new BufferedReader(fr);
    	 } catch (Exception e) {
    		 System.out.println(e);
    	 }
    	 
     }
		
     public void close() {
    	 try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }
	
     
     /**
 	 * Reads  returns a Arraylist of arrays to Strings
 	 * @param csvFile
 	 * @return
 	 */
 	 public ArrayList<String[]>  readContentsAsArrayListOfArraysToStrings(String separators, boolean printAtConsole) {
 		 ArrayList<String[]> resultAL = new ArrayList<>();
 		 String line = "";
         String cvsSplitBy = separators;
         try {
             while ((line = bfr.readLine()) != null) {
                 String[] values = line.split(cvsSplitBy);
                 //System.out.println("READ:" + line + " " + values);
                 resultAL.add(values);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         if (printAtConsole==true) {
        	 for (Object row: resultAL) {
        		 String[] rowStrings = (String[]) row;
        		 Stream.of(rowStrings).forEach(e -> System.out.print("\t"+ e));
        		 System.out.println();
        	 }
         }
         
         return resultAL;
 	}
 	 
 	
 	 /**
 	  * Read the contents and returns it as a 2D array
 	  * @param separators
 	  * @param printAtConsole
 	  * @return
 	  */
 	public String[][]  readContentsAs2DArrayOfString(String separators, boolean printAtConsole) {
 	
 			ArrayList<String[]>  al = readContentsAsArrayListOfArraysToStrings(separators, printAtConsole);
 			
 			int rows = al.size();
 			int cols = al.get(0).length;
 			
 			
 			String[][] pin = new String[rows][cols];
 			
 			for (int i=0; i<rows; i++) {
 				for (int j=0; j<cols; j++) {
 					pin[i][j] = al.get(i)[j];
 				}
 			}
 			
 			
 			return pin;
 			
 	}
 	 
 	 public ArrayList<String> readContentsAsArraylistOfStrings() {
 		 ArrayList resultAL = new ArrayList();
 		 String line = "";
         try {
             while ((line = bfr.readLine()) != null) {
                 resultAL.add(line);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
        
         return resultAL;
 	}
 	
 	/**
 	 * It takes as input an arraylist (of tables to strings) and a column number
 	 * and returns an arrayList with those strings that occur  in column i
 	 * @param a  ArrayList of arrays to strings
 	 * @param i  column number starting from 1
 	 * @return An arrayList with those strings that occur  in column i
 	 */
 	public  ArrayList getValueColumn(ArrayList a, int i) {
 		ArrayList ra = new ArrayList();
 		for (Object o: a) {
 			String[] sa = ((String[]) o);
 			ra.add(sa[i-1]);
 		}
 		return ra;
 	}

 	
	
     /**
      * Just for testing
      * @return
      */
	public String  read(){
		String line;
		
		try {
			while ((line = bfr.readLine()) != null) {   
				String[] tmp2 = line.split(","); // reading the tokens of a line
				System.out.println(tmp2);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		
		return null;
	}
}


/**
 * 
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *  Test the above code
 */

class CSVReaderClient {
	public static void main(String[] lala) {
		CSVReader btest = new CSVReader("datafilesOutput/Connections.txt");
		//btest.read();
				
		String[][] pin = btest.readContentsAs2DArrayOfString(";", true);
		
		// print the 2d array by row
		Arrays.stream(pin)
	    .map(a -> String.join(" ", a))
	        .forEach(System.out::println);
		
		
		/*
		// print the 2d array as a list
		Stream.of(pin)
	    .flatMap(Stream::of)
	        .forEach(System.out::println);
		*/
		
		/*
		ArrayList<String[]> al = btest.readContentsAsArrayListOfArraysToStrings(";", true);
		for (String[] row: al) {
			  System.out.println(row);
		}
		System.out.println(al);
		*/
	}
}