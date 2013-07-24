package util;

import java.io.BufferedInputStream;
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

import eu.visioncloud.storlet.common.SPEConstants;

public class FakeObjectService {
	final String workDir = "/home/ziwei/workspace/KompicsSRE/test/";
	public Map<String, Object> getObjectMetadataEntries(String tenant, String container, String object) throws IOException{
		
		FileInputStream fis = new FileInputStream(
				workDir + File.separator + "Metadata");
		Properties params = new Properties();
		params.load(fis);

		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put(SPEConstants.STORLET_TAG_CODEOBJ, params.get(SPEConstants.STORLET_TAG_CODEOBJ));
		metadata.put(SPEConstants.STORLET_TAG_CODETYPE, params.get(SPEConstants.STORLET_TAG_CODETYPE));
		return metadata;
		
	}
	
	public InputStream getObjectContentsAsStream(String tenant, String container, String object) throws FileNotFoundException{
		File file = new File("/home/ziwei/workspace/KompicsSRE/test/test.jar");
		FileInputStream fInput = new FileInputStream(file);
		BufferedInputStream bInput = new BufferedInputStream(fInput);
		DataInputStream dInput = new DataInputStream(bInput);
		return dInput;
	}
	
	public void unzipJar(String dir) throws IOException {
		JarFile jar = new JarFile("/home/ziwei/workspace/KompicsSRE/test/test.jar");
		Enumeration enumEntries = jar.entries();
		//java.io.File subDir = new File(dir + java.io.File.separator + jarName);
		//subDir.mkdir();
		while (enumEntries.hasMoreElements()) {
			java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries
					.nextElement();
			java.io.File f = new java.io.File(dir + java.io.File.separator
					+ file.getName());
			System.out.println(dir);
			if (file.isDirectory()) { // if its a directory, create it
				f.mkdir();
				continue;
			}
			java.io.InputStream is;
			try {
				is = jar.getInputStream(file);
				java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
				while (is.available() > 0) { // write contents of 'is' to
												// 'fos'
					fos.write(is.read());
				}
				fos.close();
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // get the input stream

		}
	}

}
