package components;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

//import deprecated.FakeObjectService;
import eu.visioncloud.cci.client.CdmiRestClient;
import eu.visioncloud.cci.client.ClientInterface;
import eu.visioncloud.storlet.common.AuthToken;
import eu.visioncloud.storlet.common.ObjIdentifier;
import eu.visioncloud.storlet.common.SPEConstants;
import eu.visioncloud.storlet.common.Storlet;
import eu.visioncloud.storlet.common.StorletException;
import eu.visioncloud.storlet.common.SyncOutputStream;
import eu.visioncloud.storlet.common.Utils;
import events.AsyncTrigger;
import events.MyTimeout;
import events.StorletInit;
import events.StorletLoadingFault;
import events.SyncTrigger;
import porttypes.SlRequest;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.timer.CancelTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;
import util.Configurator;


import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;


public class StorletWrapper extends ComponentDefinition {
	Negative<SlRequest> slReq = negative(SlRequest.class);
	Positive<Timer> timer = positive(Timer.class);
	UUID timeoutId;
	//private static boolean started = false;
	//private static int loadedSl = 0;//for benchmarking
	private Storlet storlet;
	
	public static Map<String, Set> slLogTable = new HashMap<String, Set>();
	public static Map<String, AsyncTrigger> actLogTable = new HashMap<String, AsyncTrigger>();
	
	private static ClientInterface oClient;
	private static final Logger logger = Logger.getLogger(StorletWrapper.class);
	//private static final Logger benchLogger = Logger.getLogger("benchmark");
	//private static final JavaSysMon sysMon = new JavaSysMon();
	//private static long startMem = sysMon.physical().getFreeBytes();
	
	static{
		String storageServiceUrl = Configurator.config("storageServiceUrl");
		if (Configurator.config("tenant") != null || Configurator.config("user") != null
				|| Configurator.config("password") != null){
		AuthToken authToken = new AuthToken(Configurator.config("tenant"), Configurator.config("user"), Configurator.config("password"));
		oClient = new CdmiRestClient(storageServiceUrl, authToken.getAuthenticationString());
		}
		else{
		oClient = new CdmiRestClient(storageServiceUrl);
		}
	}
	
	public StorletWrapper() {
		//PropertyConfigurator.configure("log4j.properties");
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handleStop, control);
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
			
			//logger.info("storlet with slID: "+init.getSlID()+" loaded");
			
			//logAndIncreament();
		}
	};
	
	private Handler<Start> handleStart = new Handler<Start>() {//cancel when stop?

		@Override
		public void handle(Start event) {
			// TODO Auto-generated method stub
			long delay = 5000;
			ScheduleTimeout st = new ScheduleTimeout(delay);
			st.setTimeoutEvent(new MyTimeout(st));
			timeoutId = st.getTimeoutEvent().getTimeoutId();
			trigger(st, timer);
		}
		
	};
	
	private Handler<Start> handleStop = new Handler<Start>() {//cancel when stop?

		@Override
		public void handle(Start event) {
			// TODO Auto-generated method stub
			trigger(new CancelTimeout(timeoutId), timer);
		}
		
	};
	
	Handler<MyTimeout> handleTimtout = new Handler<MyTimeout>() {

		@Override
		public void handle(MyTimeout event) {
			// TODO Auto-generated method stub
			System.out.println("the execution timeout");
		}
		
	};

	Handler<AsyncTrigger> slAsyncTriggerH = new Handler<AsyncTrigger>() {

		public void handle(AsyncTrigger trigger) {
			
			//messages++;
			// need evaluate the event before execution?
			if (storlet == null){
				logger.info("Won't execute trigger since storlet loading failed");
				return;
			}
			try {
				logger.info("Activation: "+trigger.getActId()+". Async triggering storlet with slID: "+trigger.getSlID() +" by handler: " + trigger.getHandlerId());
				//long startTime = System.currentTimeMillis();
				storlet.getTriggerHandler(trigger.getHandlerId()).execute(
						trigger.getEventModel());
				
				if (slLogTable.containsKey(trigger.getSlID())){
					Set set = slLogTable.get(trigger.getSlID());
					set.add(trigger.getActId());
				}
				else{
					Set set = new HashSet();
					set.add(trigger.getActId());
					slLogTable.put(trigger.getSlID(), set);
				}
				actLogTable.put(trigger.getActId(), trigger);
					
				//long endTime = System.currentTimeMillis();
				//logger.info("Activation: "+trigger.getActId()+". Async triggering storlet with slID: "+
				//trigger.getSlID() +" by handler: " + trigger.getHandlerId()+" completed, "+"duration: "+ (endTime-startTime) + " Misec");
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
		
		File workFolder = new File(Configurator.config("slFolderPath") + File.separator
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
					Configurator.config("contentCentricUrl"));
			long endTime = System.currentTimeMillis();
			//CpuTimes endCpuTimes = sysMon.cpuTimes();
			logger.info("load the storlet cost " + (endTime-startTime));
			//benchLogger.info((endTime-startTime) + " on " + slID.substring(26, slID.length()));
			//benchLogger.info((endMs.getFreeBytes()-startMs.getFreeBytes()));
			
		} catch (Exception e) {
			logger.error(String.format("Error in loadStorlet(%s)", slID), e);
			trigger(new StorletLoadingFault(e, slID), this.control);
		}
//		if (!SREConst.noCache)
//			storletQueue.put(slID, storlet);
		
		return storlet;
	}
	
//	private synchronized void logAndIncreament(){
//		loadedSl++;
//		//if (loadedSl%10 == 1){
//			long memCost = (startMem - sysMon.physical().getFreeBytes());
//			benchLogger.info(memCost);
//		//}
//		
//	}
}