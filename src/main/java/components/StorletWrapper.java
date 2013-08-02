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

import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import constant.SREConst;

import com.jezhumble.javasysmon.CpuTimes;
import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.MemoryStats;

public class StorletWrapper extends ComponentDefinition {
	Negative<SlRequest> slReq = negative(SlRequest.class);
	//private static boolean started = false;
	private static int loadedSl = 0;
	private Storlet storlet;
	public static Map<String, Object> logTable = new HashMap<String, Object>();
	
	private static FakeObjectService oClient = new FakeObjectService();
	private static final Logger logger = Logger.getLogger(StorletWrapper.class);
	private static final Logger benchLogger = Logger.getLogger("benchmark");
	private static final JavaSysMon sysMon = new JavaSysMon();
	private static long startMem = sysMon.physical().getFreeBytes();
	
	public StorletWrapper() {
		//PropertyConfigurator.configure("log4j.properties");
		subscribe(handleInit, control);
		subscribe(slAsyncTriggerH, slReq);
		subscribe(slSyncTriggerH, slReq);
	}

	private Handler<StorletInit> handleInit = new Handler<StorletInit>() {
		public void handle(StorletInit init) {
//			if (started == false){
//				startMem = sysMon.physical().getFreeBytes();
//				started = true;
//			}
			logger.info("loading storlet with slID: "+init.getSlID());
			storlet = loadStorlet(init.getSlID());
			logger.info("storlet with slID: "+init.getSlID()+" loaded");
			
			//logAndIncreament();
		}
	};

	Handler<AsyncTrigger> slAsyncTriggerH = new Handler<AsyncTrigger>() {

		public void handle(AsyncTrigger trigger) {
			
			//messages++;
			// need evaluate the event before execution?
			try {
				logger.info("Activation: "+trigger.getActId()+". Async triggering storlet with slID: "+trigger.getSlID() +" by handler: " + trigger.getHandlerId());
				long startTime = System.currentTimeMillis();
				storlet.getTriggerHandler(trigger.getHandlerId()).execute(
						trigger.getEventModel());
				
				long endTime = System.currentTimeMillis();
				logger.info("Activation: "+trigger.getActId()+". Async triggering storlet with slID: "+
				trigger.getSlID() +" by handler: " + trigger.getHandlerId()+" completed, "+"duration: "+ (endTime-startTime) + " Misec");
			} catch (StorletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	Handler<SyncTrigger> slSyncTriggerH = new Handler<SyncTrigger>() {
		public void handle(SyncTrigger trigger) {
			//messages++;
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
		
		//long startTime = System.currentTimeMillis();
		//MemoryStats startMs = sysMon.physical();
		
		Storlet storlet = null;
		
		File workFolder = new File(SREConst.slFolderPath + File.separator
				+ slID);
		Utils.deleteFileOrDirectory(workFolder);
		workFolder.mkdirs();
		ObjIdentifier storletInstanceId = ObjIdentifier.createFromString(slID);
		//ObjIdentifier storletInstanceId = ObjIdentifier.createFromString(slID.substring(0, 26));// for benchmarking, 3 is the number of suffix digits
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
			logger.info(storletDefinitionId.toString()+" "+storletInstanceId.toString());
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
			//long endTime = System.currentTimeMillis();
			//CpuTimes endCpuTimes = sysMon.cpuTimes();
			//MemoryStats endMs = sysMon.physical();
			// TODO temp, remove
//			byte[] slInstanceContentBytes = Utils.inputStreamToByteArray(Utils
//					.decodeStream(slInstanceContentEncodedStream));
//			logger.info("-----[TEMP]Extracted storlet INSTANCE content for "
//					+ storletInstanceId + " into "
//					+ workFolder.getAbsolutePath() + "\n\t\t[exists="
//					+ workFolder.exists() + ":dir=" + workFolder.isDirectory()
//					+ "]\n\t\t" + Arrays.toString(workFolder.listFiles())
//					+ "\n\t\tSize = " + slInstanceContentBytes.length);
			//oClient.unzipJar(workFolder.getAbsolutePath());//my unpack method, slower
			// Load storlet class from jar & activate it
			long startTime = System.currentTimeMillis();
			//CpuTimes startCpuTimes = sysMon.cpuTimes();
			File jarFile = new File(workFolder,
					SPEConstants.STORLET_CODE_FILENAME);
			Class<? extends Storlet> storletClass = (Class<? extends Storlet>) Utils
					.loadStorletClass(jarFile, slCodeType);
			logger.info("storlet class loaded: " + storletClass.getName());
			storlet = Storlet.createStorlet(storletClass, workFolder,
					SREConst.ccsURL);
			long endTime = System.currentTimeMillis();
			//CpuTimes endCpuTimes = sysMon.cpuTimes();
			logger.info("load the storlet cost " + (endTime-startTime));
			//benchLogger.info((endTime-startTime) + " on " + slID.substring(26, slID.length()));
			//benchLogger.info((endMs.getFreeBytes()-startMs.getFreeBytes()));
			
		} catch (Exception e) {
			//log.error(String.format("Error in loadStorlet(%s)", slID), e);
		}
//		if (!SREConst.noCache)
//			storletQueue.put(slID, storlet);
		
		return storlet;
	}
	
	private synchronized void logAndIncreament(){
		loadedSl++;
		//if (loadedSl%10 == 1){
			long memCost = (startMem - sysMon.physical().getFreeBytes());
			benchLogger.info(memCost);
		//}
		
	}
}