/**
 * 
 */
package core;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 * Class responsible for reading a .kg file
 * and reading its properties.
 * It also offers some utility functions
 */
public class KGproperties {
	String filepath;  // of the file
	Properties kgProps;
	
	//property values
	String v_subFoldersClass=null;
	String v_traverse=null;
	String[] ignoreExts; 
	String v_readme=null;
	
	boolean getTraverse() { 
		if (v_traverse!=null) {
			if (v_traverse.equals("off")) {
				return false;
			}
		}
		return true;		
	}

	/**
	 * Returns the file extension of a file
	 * @param filename
	 * @return
	 */
	static String getFileExtension(String filename) {
		String extension = "";

		int i = filename.lastIndexOf('.');
		if (i > 0) {
		    extension = filename.substring(i+1);
		}
		return extension;
	}
	
	/**
	 * returns the filename without extension
	 * @param filename
	 * @return
	 */

	static String getFileNameWithoutExtension(String filename) {
		String basicName ="";

		int i = filename.lastIndexOf('.');
		if (i > 0) {
		    basicName = filename.substring(0, i);
		}
		return basicName;
	}

	/**
	 * Checks if a filename should be ignored based on its extension
	 * @param filename
	 * @return
	 */
	boolean shouldIgnore(String filename) {
		if (ignoreExts!=null) {
			String fext = getFileExtension(filename);
			//System.out.println("EXTENSION of current file="+fext);
			for (int i=0;i<ignoreExts.length;i++) {
				if (ignoreExts[i].equals(fext)) {
					System.out.println("Ignoring " + filename);
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Loads a .kg file and reads it properties 
	 */
	private void load() {
		
		try {
			kgProps.load(new FileInputStream(filepath));
			
			// READING PROPERTIES
			v_subFoldersClass=kgProps.getProperty("subFoldersClass");
			v_traverse=kgProps.getProperty("traverse");
			v_readme=kgProps.getProperty("readme");
			
			String ie = kgProps.getProperty("ignoreExt");
			if (ie!=null) {
				ignoreExts = ie.split(";");
			}
			
			//showAll();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			System.out.println(e);
		}

		/*
		//files=City  
		String filePropValue = kgProps.getProperty("files");
		System.out.println("Read file:"+filePropValue);
		//assertEquals("City", fileProp);
		*/        
		
	}

	/**
	 * Shows all properties read from a .kg file
	 */

	void showAll() {
		kgProps.list(System.out); // list all key-value pairs
			
		/*
		Enumeration<Object> valueEnumeration = kgProps.elements();
		while (valueEnumeration.hasMoreElements()) {
		    System.out.println(valueEnumeration.nextElement());
		}

		Enumeration<Object> keyEnumeration = kgProps.keys();
		while (keyEnumeration.hasMoreElements()) {
		    System.out.println(keyEnumeration.nextElement());
		}

		int size = kgProps.size();
		*/
	}
	
	public KGproperties(String filepath) {
		this.filepath = filepath;
		kgProps = new Properties();
		load();
	}

	public static void main(String[] lala) {
		
	}

}
