package util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import components.SREComponent;

import eu.visioncloud.storlet.common.SPEConstants;
import eu.visioncloud.storlet.common.Utils;

public class FakeObjectService {
	final String workDir = "/home/ziwei/workspace/KompicsSRE/test/";
	private static final Logger logger = Logger.getLogger(FakeObjectService.class);
	private static byte[] encodedByteArrayInst;
	private static byte[] encodedByteArrayDef;
	/*static{
		PropertyConfigurator.configure("log4j.properties");
		
		File file = new File("/home/ziwei/workspace/KompicsSRE/test/ExampleStorlet/ExampleStorlet.jar");
		FileInputStream fInput = null;
		try {
			fInput = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedInputStream bInput = new BufferedInputStream(fInput);
		byte[] rawByteArray = null;
		try {
			rawByteArray = Utils.inputStreamToByteArray(bInput);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		encodedByteArray = Utils.encodeByteArray(rawByteArray);
		
		//input = new ByteArrayInputStream(encodedByteArray);
	}*/
	
	static{
		PropertyConfigurator.configure("log4j.properties");
		
		File file = new File("/home/ziwei/workspace/KompicsSRE/test/ExampleStorletDef/ExampleStorletDef.jar");
		FileInputStream fInput = null;
		try {
			fInput = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedInputStream bInput = new BufferedInputStream(fInput);
		byte[] rawByteArray = null;
		try {
			rawByteArray = Utils.inputStreamToByteArray(bInput);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		encodedByteArrayDef = Utils.encodeByteArray(rawByteArray);
		
		//input = new ByteArrayInputStream(encodedByteArray);
		
		File file1 = new File("/home/ziwei/workspace/KompicsSRE/test/ExampleStorlet/ExampleStorlet.jar");
		FileInputStream fInput1 = null;
		try {
			fInput1 = new FileInputStream(file1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedInputStream bInput1 = new BufferedInputStream(fInput1);
		byte[] rawByteArray1 = null;
		try {
			rawByteArray1 = Utils.inputStreamToByteArray(bInput1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		encodedByteArrayInst = Utils.encodeByteArray(rawByteArray1);
	}
	
	public synchronized Map<String, Object> getObjectMetadataEntries(String tenant, String container, String object) throws IOException{
		long startTime = System.currentTimeMillis();
		logger.info("getObjectMetadataEntries at: "+startTime);
		FileInputStream fis = new FileInputStream(
				workDir + File.separator + object + File.separator + "Metadata");
		Properties params = new Properties();
		params.load(fis);

		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put(SPEConstants.STORLET_TAG_CODEOBJ, params.get(SPEConstants.STORLET_TAG_CODEOBJ));
		metadata.put(SPEConstants.STORLET_TAG_CODETYPE, params.get(SPEConstants.STORLET_TAG_CODETYPE));
		return metadata;
	}
	
	public synchronized InputStream getObjectContentsAsStream(String tenant, String container, String object) throws IOException{
		//File file = new File("/home/ziwei/workspace/KompicsSRE/test/" + object + File.separator + object + ".jar");
//		File file = new File("/home/ziwei/workspace/KompicsSRE/test/" + object + File.separator + object + ".jar");
//		FileInputStream fInput = new FileInputStream(file);
//		BufferedInputStream bInput = new BufferedInputStream(fInput);
//		byte[] rawByteArray = Utils.inputStreamToByteArray(bInput);
//		byte[] encodedByteArray = Utils.encodeByteArray(rawByteArray);
		
		//InputStream input = new ByteArrayInputStream(encodedByteArray);
		long endTime = System.currentTimeMillis();
		logger.info("getObjectContentsAsStream finished at: "+endTime);
		//return new ByteArrayInputStream(encodedByteArray);
		if (object.endsWith("Def"))
			return new ByteArrayInputStream(encodedByteArrayDef);
		else 
			return new ByteArrayInputStream(encodedByteArrayInst);
	}
	
//	public void unzipJar(String dir) throws IOException {
//		JarFile jar = new JarFile("/home/ziwei/workspace/KompicsSRE/test/test.jar");
//		Enumeration enumEntries = jar.entries();
//		//java.io.File subDir = new File(dir + java.io.File.separator + jarName);
//		//subDir.mkdir();
//		while (enumEntries.hasMoreElements()) {
//			java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries
//					.nextElement();
//			java.io.File f = new java.io.File(dir + java.io.File.separator
//					+ file.getName());
//			//System.out.println(dir);
//			if (file.isDirectory()) { // if its a directory, create it
//				f.mkdir();
//				continue;
//			}
//			java.io.InputStream is;
//			try {
//				is = jar.getInputStream(file);
//				java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
//				while (is.available() > 0) { // write contents of 'is' to
//												// 'fos'
//					fos.write(is.read());
//				}
//				fos.close();
//				is.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} // get the input stream
//
//		}
//	}

}
