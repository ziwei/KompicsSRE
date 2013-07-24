package components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import eu.visioncloud.storlet.common.ObjIdentifier;
import eu.visioncloud.storlet.common.SPEConstants;
import eu.visioncloud.storlet.common.Storlet;
import eu.visioncloud.storlet.common.StorletException;
import eu.visioncloud.storlet.common.SyncOutputStream;
import eu.visioncloud.storlet.common.Utils;
import events.AsyncTrigger;
import events.SlOperation;
import events.StorletInit;
import events.SyncTrigger;
import porttypes.SlRequest;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import tests.SPETestConstants;

import util.FakeObjectService;
import util.JarInJarClassLoader;
import util.StorletInitSample;

import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import constant.SREConst;

public class StorletWrapper extends ComponentDefinition {
	Negative<SlRequest> slReq = negative(SlRequest.class);
	int messages;
	private SlOperation event;
	private Storlet storlet;
	private static final org.slf4j.Logger log = LoggerFactory
			.getLogger(StorletWrapper.class);
	
	private static FakeObjectService oClient = new FakeObjectService();
	
	public StorletWrapper() {
		subscribe(handleInit, control);
		subscribe(slAsyncTriggerH, slReq);
		subscribe(slSyncTriggerH, slReq);
	}

	private Handler<StorletInit> handleInit = new Handler<StorletInit>() {
		public void handle(StorletInit init) {
//			if (init.getSocket() != null) {
//				socket = init.getSocket();
//			}
//			// storlet = StorletInitSample.createSampleStorlet();
			storlet = loadStorlet(init.getSlID());
//			storlet = init.getSlInst();
//			if (init.getEvent().getClass().equals(AsyncTrigger.class)){
//				AsyncTrigger event = (AsyncTrigger) init.getEvent();
//				storlet = loadStorlet(event.getSlID());
//				try {
//					storlet.getTriggerHandler(event.getHandlerId()).execute(
//							event.getEventModel());
//				} catch (StorletException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			else if(init.getEvent().getClass().equals(SyncTrigger.class)){
//				SyncTrigger event = (SyncTrigger) init.getEvent();
//				storlet = loadStorlet(event.getSyncAct().getStorlet_name());
//				try {
//					SyncOutputStream os = new SyncOutputStream(new Socket(localhost, event.getSyncAct().getPort()));
//					storlet.get(os, trigger.getSyncAct().getParameter());
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (StorletException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
		}
	};

	Handler<AsyncTrigger> slAsyncTriggerH = new Handler<AsyncTrigger>() {

		public void handle(AsyncTrigger trigger) {
			messages++;
			System.out.println("Storlet Async " + storlet.getClass().toString()
					+ " got " + messages + " msgs");
			// need evaluate the event before execution
			try {
				storlet.getTriggerHandler(trigger.getHandlerId()).execute(
						trigger.getEventModel());
			} catch (StorletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	Handler<SyncTrigger> slSyncTriggerH = new Handler<SyncTrigger>() {
		public void handle(SyncTrigger trigger) {
			messages++;
			try {
				SyncOutputStream os = new SyncOutputStream(new Socket("localhost", trigger.getSyncAct().getPort()));
				storlet.get(os, trigger.getSyncAct().getParameter());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StorletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};
	
	private Storlet loadStorlet(String slID) {
		// convert the slID into something easier to handle
		
		Storlet storlet = null;
		ObjIdentifier storletInstanceId = ObjIdentifier.createFromString(slID);

		// if cache is activated try to read from cache first
//		if (!SREConst.noCache && StorletWrapper.storletQueue.containsKey(slID)) {
//			return StorletWrapper.storletQueue.get(slID);
//		}
		// create and clean storlet work directory
		File workFolder = new File(SREConst.slFolderPath + File.separator
				+ slID);
		Utils.deleteFileOrDirectory(workFolder);
		workFolder.mkdirs();

		// TODO temp, remove
		log.info("-----[TEMP]Created work directory for "
				+ storletInstanceId.toString() + " at "
				+ workFolder.getAbsolutePath() + "\n\t\t[exists="
				+ workFolder.exists() + ":dir=" + workFolder.isDirectory()
				+ "]\n\t\t" + Arrays.toString(workFolder.listFiles()));

		try {
			Map<String, Object> slMD = oClient.getObjectMetadataEntries(
					storletInstanceId.getTenantName(),
					storletInstanceId.getContainerName(),
					storletInstanceId.getObjectName());
			// TODO temp, remove
			log.info("-----[TEMP]Metadata for " + slID + " is: "
					+ slMD.toString());

			ObjIdentifier storletDefinitionId = ObjIdentifier
					.createFromString((String)slMD
							.get(SPEConstants.STORLET_TAG_CODEOBJ));
			String slCodeType = (String)slMD.get(SPEConstants.STORLET_TAG_CODETYPE);

			// TODO temp, remove
			log.info("-----[TEMP]ObjIdentifier for storlet: "
					+ storletDefinitionId.toString());
			log.info("-----[TEMP]Storlet code type: " + slCodeType);

			//keep the old Object Service unzip jar file API
			
			// check if code is stored in a different object
			// if yes download it and store in work folder
//			if (!storletDefinitionId.equals(storletInstanceId)) {
//				// Encoded String Stream
//				InputStream slDefinitionContentEncodedStream = oClient
//						.getObjectContentsAsStream(
//								storletDefinitionId.getTenantName(),
//								storletDefinitionId.getContainerName(),
//								storletDefinitionId.getObjectName());
//				Utils.extractJarContents(
//						Utils.decodeStream(slDefinitionContentEncodedStream),
//						workFolder.getAbsolutePath());
//
//				// TODO temp, remove
//				byte[] slDefinitionContentBytes = Utils
//						.inputStreamToByteArray(Utils
//								.decodeStream(slDefinitionContentEncodedStream));
//				log.info("-----[TEMP]Extracted storlet DEFINITION content for "
//						+ storletDefinitionId + " into "
//						+ workFolder.getAbsolutePath() + "\n\t\t[exists="
//						+ workFolder.exists() + ":dir="
//						+ workFolder.isDirectory() + "]\n\t\t"
//						+ Arrays.toString(workFolder.listFiles())
//						+ "\n\t\tSize = " + slDefinitionContentBytes.length);
//			}

			// ------------get the data-----------------
//			InputStream slInstanceContentEncodedStream = oClient
//					.getObjectContentsAsStream(
//							storletInstanceId.getTenantName(),
//							storletInstanceId.getContainerName(),
//							storletInstanceId.getObjectName());
//			// IMPORTANT: Storlet files will overwrite Definition files
//			Utils.extractJarContents(
//					Utils.decodeStream(slInstanceContentEncodedStream),
//					workFolder.getAbsolutePath());
			oClient.unzipJar(workFolder.getAbsolutePath());

			// TODO temp, remove
//			byte[] slInstanceContentBytes = Utils.inputStreamToByteArray(Utils
//					.decodeStream(slInstanceContentEncodedStream));
//			log.info("-----[TEMP]Extracted storlet INSTANCE content for "
//					+ storletInstanceId + " into "
//					+ workFolder.getAbsolutePath() + "\n\t\t[exists="
//					+ workFolder.exists() + ":dir=" + workFolder.isDirectory()
//					+ "]\n\t\t" + Arrays.toString(workFolder.listFiles())
//					+ "\n\t\tSize = " + slInstanceContentBytes.length);

			// Load storlet class from jar & activate it
			File jarFile = new File(workFolder,
					SPEConstants.STORLET_CODE_FILENAME);
			Class<? extends Storlet> storletClass = (Class<? extends Storlet>) Utils
					.loadStorletClass(jarFile, slCodeType);
			storlet = Storlet.createStorlet(storletClass, workFolder,
					SREConst.ccsURL);
			//trigger(new StorletInit(storlet, socket), newStorletWrapper.getControl());
		} catch (Exception e) {
			log.error(String.format("Error in loadStorlet(%s)", slID), e);
		}
//		if (!SREConst.noCache)
//			storletQueue.put(slID, storlet);
		return storlet;
	}
//	private Storlet createStorlet(StorletInit si) throws IOException,
//			StorletException, ClassNotFoundException, URISyntaxException,
//			InstantiationException, IllegalAccessException {
//		// System.out.println("ready to load class");
//		Properties props = new Properties();
//		// props.setProperty(SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_KEY,
//		// SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_VAL);
//		// String authString = "";
//		// String contentCentricUrl = "/";
//		// Storlet sl = null;
//		// File dir1 = null;
//		// dir1 = tempDir.newFolder();
//		File propsFile1 = new File(si.getWorkingDir().getAbsolutePath()
//				+ File.separator + "params.properties");
//		// File authFile1 = new
//		// File(si.getWorkingDir().getAbsolutePath()+File.separator+SPEConstants.STORLET_AUTH_FILENAME);
//		// Utils.byteArrayToFile(authString.getBytes(),
//		// authFile1.getAbsolutePath());
//		props.store(new FileOutputStream(propsFile1), "temp property file 1");
//
//		ClassLoader prevCl = Thread.currentThread().getContextClassLoader();
//		System.out.println("class load from "
//				+ si.getWorkingDir().getAbsolutePath());
//		JarInJarClassLoader jijLoader = new JarInJarClassLoader(
//				new URL("file:///" + si.getWorkingDir().getAbsolutePath()
//						+ "/src.jar"), prevCl);
//		System.out.println("loader inited, find class " + si.getStorletType());
//		// Thread.currentThread().setContextClassLoader(jijLoader);
//		// jijLoader.searchAndAddNestedJars();
//		@SuppressWarnings("unchecked")
//		Class<? extends Storlet> storletClass = (Class<? extends Storlet>) jijLoader
//				.findClass(si.getStorletType());
//		System.out.println("class loaded");
//		Storlet sl = Storlet.createStorlet(storletClass, si.getWorkingDir(),
//				si.getContentCentricUrl());
//		// Thread.currentThread().setContextClassLoader(prevCl);
//		System.out.println("storlets created");
//		return sl;
//	}
}
