/**
 * 
 */
package core;

/**
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *
 */
public class FileSystemToKGclient {
	public static void main(String[] args) {
		//String startFolder = null;  // for opening the file selection frame
    	String startFolder = "C:\\Users\\tzitzik\\Documents\\YandT\\Yannis\\My_TEACHING\\CS_561\\Software\\FStoKGtestDataFolder\\DemoFolder";
    	String outputFile  =  "datafilesOutput/fileSystemKG.ttl";  
    	FileSystemToKG.traverseAndCreateKG(startFolder, outputFile); // creates the knowlege graph
    	FileSystemToKG.open(outputFile); // opens the ttl using the associated application/editor (if any) in the host computing system
		
	}
	
}
