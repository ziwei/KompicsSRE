package components;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import eu.visioncloud.storlet.common.ObjIdentifier;
import eu.visioncloud.storlet.common.SPEConstants;
import eu.visioncloud.storlet.common.Storlet;
import eu.visioncloud.storlet.common.StorletException;
import eu.visioncloud.storlet.common.SyncOutputStream;
import eu.visioncloud.storlet.common.Utils;
import events.AsyncTrigger;
import events.StorletInit;
import events.SyncTrigger;
import porttypes.SlRequest;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;

import util.FakeObjectService;
import web.SREJettyWebServer;

import java.net.Socket;
import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;

import constant.SREConst;

public class StorletWrapper extends ComponentDefinition {
	Negative<SlRequest> slReq = negative(SlRequest.class);
	int messages;
	private Storlet storlet;
	
	private static FakeObjectService oClient = new FakeObjectService();
	private static final Logger logger = Logger.getLogger(StorletWrapper.class);
	
	public StorletWrapper() {
		subscribe(handleInit, control);
		subscribe(slAsyncTriggerH, slReq);
		subscribe(slSyncTriggerH, slReq);
	}

	private Handler<StorletInit> handleInit = new Handler<StorletInit>() {
		public void handle(StorletInit init) {
			logger.info("loading storlet with slID: "+init.getSlID());
			storlet = loadStorlet(init.getSlID());
			logger.info("storlet with slID: "+init.getSlID()+" loaded");
		}
	};

	Handler<AsyncTrigger> slAsyncTriggerH = new Handler<AsyncTrigger>() {

		public void handle(AsyncTrigger trigger) {
			messages++;
			// need evaluate the event before execution?
			try {
				logger.info("Activation: "+trigger.getActId()+". Async triggering storlet with slID: "+trigger.getSlID() +" by handler: " + trigger.getHandlerId());
				storlet.getTriggerHandler(trigger.getHandlerId()).execute(
						trigger.getEventModel());
				logger.info("Activation: "+trigger.getActId()+". Async triggering storlet with slID: "+trigger.getSlID() +" by handler: " + trigger.getHandlerId()+" completed");
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
				logger.info("Sync triggering storlet with slName: "+trigger.getSyncAct().getStorlet_name() +
						" port: "+trigger.getSyncAct().getPort() + " params: "+trigger.getSyncAct().getParameter());
				SyncOutputStream os = new SyncOutputStream(new Socket("localhost", trigger.getSyncAct().getPort()));
				storlet.get(os, trigger.getSyncAct().getParameter());
				logger.info("Sync triggering storlet with slName: "+trigger.getSyncAct().getStorlet_name() +" completed");
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
		File workFolder = new File(SREConst.slFolderPath + File.separator
				+ slID);
		Utils.deleteFileOrDirectory(workFolder);
		workFolder.mkdirs();

		// TODO temp, remove
		logger.info("-----[TEMP]Created work directory for "
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
			ObjIdentifier storletDefinitionId = ObjIdentifier
					.createFromString((String)slMD
							.get(SPEConstants.STORLET_TAG_CODEOBJ));
			String slCodeType = (String)slMD.get(SPEConstants.STORLET_TAG_CODETYPE);

			// TODO temp, remove
			//keep the old Object Service unzip jar file API
			
			// check if code is stored in a different object
			// if yes download it and store in work folder
			if (!storletDefinitionId.equals(storletInstanceId)) {
				// Encoded String Stream
				InputStream slDefinitionContentEncodedStream = oClient
						.getObjectContentsAsStream(
								storletDefinitionId.getTenantName(),
								storletDefinitionId.getContainerName(),
								storletDefinitionId.getObjectName());
				Utils.extractJarContents(
						Utils.decodeStream(slDefinitionContentEncodedStream),
						workFolder.getAbsolutePath());

				// TODO temp, remove
				byte[] slDefinitionContentBytes = Utils
						.inputStreamToByteArray(Utils
								.decodeStream(slDefinitionContentEncodedStream));
				logger.info("-----[TEMP]Extracted storlet DEFINITION content for "
						+ storletDefinitionId + " into "
						+ workFolder.getAbsolutePath() + "\n\t\t[exists="
						+ workFolder.exists() + ":dir="
						+ workFolder.isDirectory() + "]\n\t\t"
						+ Arrays.toString(workFolder.listFiles())
						+ "\n\t\tSize = " + slDefinitionContentBytes.length);
			}

			// ------------get the data-----------------
			InputStream slInstanceContentEncodedStream = oClient
					.getObjectContentsAsStream(
							storletInstanceId.getTenantName(),
							storletInstanceId.getContainerName(),
							storletInstanceId.getObjectName());
			// IMPORTANT: Storlet files will overwrite Definition files
			Utils.extractJarContents(
					Utils.decodeStream(slInstanceContentEncodedStream),
					workFolder.getAbsolutePath());

			// TODO temp, remove
			byte[] slInstanceContentBytes = Utils.inputStreamToByteArray(Utils
					.decodeStream(slInstanceContentEncodedStream));
			logger.info("-----[TEMP]Extracted storlet INSTANCE content for "
					+ storletInstanceId + " into "
					+ workFolder.getAbsolutePath() + "\n\t\t[exists="
					+ workFolder.exists() + ":dir=" + workFolder.isDirectory()
					+ "]\n\t\t" + Arrays.toString(workFolder.listFiles())
					+ "\n\t\tSize = " + slInstanceContentBytes.length);
			//oClient.unzipJar(workFolder.getAbsolutePath());
			// Load storlet class from jar & activate it
			File jarFile = new File(workFolder,
					SPEConstants.STORLET_CODE_FILENAME);
			Class<? extends Storlet> storletClass = (Class<? extends Storlet>) Utils
					.loadStorletClass(jarFile, slCodeType);
			logger.info("storlet class loaded: " + storletClass.getName());
			storlet = Storlet.createStorlet(storletClass, workFolder,
					SREConst.ccsURL);
			//trigger(new StorletInit(storlet, socket), newStorletWrapper.getControl());
		} catch (Exception e) {
			//log.error(String.format("Error in loadStorlet(%s)", slID), e);
		}
//		if (!SREConst.noCache)
//			storletQueue.put(slID, storlet);
		return storlet;
	}
}