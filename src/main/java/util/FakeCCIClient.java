package util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;

import eu.visioncloud.storlet.common.SPEConstants;
import eu.visioncloud.storlet.common.Utils;

public class FakeCCIClient {
	public static Map<String, Object> getObjectMetadataEntries(String name){
		HashMap<String, Object> slMD = new HashMap<String, Object>();
		slMD.put(SPEConstants.STORLET_TAG_CODEOBJ, "sics.vision.ExampleStorlet");
		slMD.put(SPEConstants.STORLET_TAG_CODETYPE, "storlets4testing.ExampleStorlet");
		return slMD;
	}
	
	public static InputStream getObjectContentsAsStream(String name) throws IOException{
		FileInputStream jarInputStream = new FileInputStream("/home/ziwei/workspace/KompicsSRE/storletsources/ExampleStorlet/ExampleStorlet.jar");
		BufferedInputStream buffInputStream = new BufferedInputStream(jarInputStream);
		byte[] encodedByteArray = Utils.encodeByteArray(Utils.inputStreamToByteArray(buffInputStream));
		return new ByteArrayInputStream(encodedByteArray);
	}
}
