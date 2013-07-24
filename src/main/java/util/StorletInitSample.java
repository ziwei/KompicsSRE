package util;

import static tests.SPETestConstants.VALUE1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.rules.TemporaryFolder;

import se.sics.kompics.ComponentDefinition;
import tests.SPETestConstants;
import eu.visioncloud.storlet.common.EventModel;
import eu.visioncloud.storlet.common.SPEConstants;
import eu.visioncloud.storlet.common.Storlet;
import eu.visioncloud.storlet.common.StorletException;
import eu.visioncloud.storlet.common.Utils;

public class StorletInitSample {
	static TemporaryFolder tempDir = new TemporaryFolder();
	static Properties props = new Properties();

	public static Storlet createSampleStorlet() throws IOException,
			StorletException, ClassNotFoundException, URISyntaxException,
			InstantiationException, IllegalAccessException {
		//System.out.println("ready to load class");
		props.setProperty(SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_KEY,
				SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_VAL);
		String authString = "";
		String contentCentricUrl = "/";
		Storlet sl = null;
		File dir1 = null;
		dir1 = tempDir.newFolder();
		File propsFile1 = new File(dir1, SPEConstants.STORLET_PARAMS_FILENAME);
		File authFile1 = new File(dir1, SPEConstants.STORLET_AUTH_FILENAME);
		Utils.byteArrayToFile(authString.getBytes(),
				authFile1.getAbsolutePath());
		props.store(new FileOutputStream(propsFile1), "temp property file 1");
		
		ClassLoader prevCl = Thread.currentThread().getContextClassLoader();
	
		JarInJarClassLoader jijLoader = new JarInJarClassLoader(new URL(
			"file:///home/ziwei/workspace/KompicsSRE/tmp/src.jar"), prevCl);
//		URLClassLoader jijLoader = new URLClassLoader(new URL[]{new URL(
//				"file:///home/ziwei/workspace/KompicsSRE/nest.jar")}, prevCl);
		Thread.currentThread().setContextClassLoader(jijLoader);
		//jijLoader.searchAndAddNestedJars();
		@SuppressWarnings("unchecked")
		Class<? extends Storlet> storletClass = (Class<? extends Storlet>) jijLoader
				.loadClass("storlets4testing.HelloFileStorlet");
		// @SuppressWarnings("unchecked")
		// Class<? extends Storlet> innerClass = (Class<? extends Storlet>)
		// jijLoader.findClass("storlets4testing.HelloFileStorlet$1");
		System.out.println("class loaded");
		// System.setSecurityManager(null);
		sl = Storlet.createStorlet(storletClass, dir1, contentCentricUrl);
		Thread.currentThread().setContextClassLoader(prevCl);
		System.out.println("storlets created");
		return sl;
	}

	public static EventModel createSampleEvent() throws IOException {
		File workDir = null;
		workDir = tempDir.newFolder();
		File propsFile = new File(workDir, SPEConstants.STORLET_PARAMS_FILENAME);
		String authString = "";
		File authFile = new File(workDir, SPEConstants.STORLET_AUTH_FILENAME);
		Utils.byteArrayToFile(authString.getBytes(), authFile.getAbsolutePath());
		props.setProperty(SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_KEY,
				SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_VAL);
		props.store(new FileOutputStream(propsFile), "temp property file");
		String contentCentricUrl = "/";
		// Create event to trigger storlet
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put(SPETestConstants.KEY1, VALUE1);
		File tempFile = null;
		tempFile = tempDir.newFile();
		metadata.put(SPETestConstants.KEY_PATH, tempFile.getAbsolutePath());
		EventModel event = new EventModel();
		event.setOldMetadata(metadata);
		event.setNewMetadata(metadata);
		event.setrFlag(false);
		return event;
	}
}
