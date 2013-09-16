package components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import events.ExecutionInfo;
import events.MyTimeout;
import events.StorletInit;
import events.StorletLoadingFault;
import events.SyncTrigger;
import porttypes.ExeStatus;
import porttypes.SlRequest;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.timer.CancelTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;
import util.FakeCCIClient;
import util.MemoryWarningSystem;
import util.MemoryWarningSystem.Listener;

import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import constant.SREConst;

//memory management

public class StorletWrapper extends ComponentDefinition {
	Negative<SlRequest> slReq = negative(SlRequest.class);
	Positive<ExeStatus> exeStatus = positive(ExeStatus.class);

	// private static boolean started = false;
	// private static int loadedSl = 0;//for benchmarking
	private Storlet storlet;

	public static Map<String, Set> slLogTable = new HashMap<String, Set>();
	public static Map<String, AsyncTrigger> actLogTable = new HashMap<String, AsyncTrigger>();

	private static ClientInterface oClient;

	private static final Logger logger = Logger.getLogger(StorletWrapper.class);
	// private static final Logger benchLogger = Logger.getLogger("benchmark");
	// private static final JavaSysMon sysMon = new JavaSysMon();
	// private static long startMem = sysMon.physical().getFreeBytes();
	long timeConstraint = -1;

	static {
		String storageServiceUrl = SREConst.ccsURL;
		if (SREConst.user != null && SREConst.tenant != null
				&& SREConst.password != null) {
			AuthToken authToken = new AuthToken(SREConst.user, SREConst.tenant,
					SREConst.password);
			oClient = new CdmiRestClient(storageServiceUrl,
					authToken.getAuthenticationString());
		} else {
			oClient = new CdmiRestClient(storageServiceUrl);
		}
	}

	public StorletWrapper() {
		// PropertyConfigurator.configure("log4j.properties");
		subscribe(handleInit, control);
		subscribe(slAsyncTriggerH, slReq);
		subscribe(slSyncTriggerH, slReq);

	}

	private Handler<StorletInit> handleInit = new Handler<StorletInit>() {
		public void handle(StorletInit init) {
			// if (started == false){
			// startMem = sysMon.physical().getFreeBytes();
			// started = true;
			// }
			logger.info("Init phase, loading storlet with slID: "
					+ init.getSlID());
			storlet = loadStorlet(init.getSlID());

			// logger.info("storlet with slID: "+init.getSlID()+" loaded");

			// logAndIncreament();
		}
	};

	Handler<AsyncTrigger> slAsyncTriggerH = new Handler<AsyncTrigger>() {

		public void handle(AsyncTrigger trigger) {

			// messages++;
			// need evaluate the event before execution?
			if (storlet == null) {
				logger.info("Won't execute trigger since storlet loading failed");
				return;
			}
			logger.info("Activation: " + trigger.getActId()
					+ ". Async triggering storlet with slID: "
					+ trigger.getSlID() + " by handler: "
					+ trigger.getHandlerId());
			// long startTime = System.currentTimeMillis();
			trigger(new ExecutionInfo("start", trigger.getSlID(),
					trigger.getHandlerId(), timeConstraint), exeStatus);
			try {
			storlet.getTriggerHandler(trigger.getHandlerId()).execute(
					trigger.getEventModel());
			}
			catch (Exception e){
				logger.info("storlet execution failed", e);
			}
			trigger(new ExecutionInfo("stop", trigger.getSlID(),
					trigger.getHandlerId()), exeStatus);
			if (slLogTable.containsKey(trigger.getSlID())) {
				Set set = slLogTable.get(trigger.getSlID());
				set.add(trigger.getActId());
			} else {
				Set set = new HashSet();
				set.add(trigger.getActId());
				slLogTable.put(trigger.getSlID(), set);
			}
			actLogTable.put(trigger.getActId(), trigger);

		}
	};

	Handler<SyncTrigger> slSyncTriggerH = new Handler<SyncTrigger>() {
		public void handle(SyncTrigger trigger) {
			// messages++;
			try {
				logger.info("Sync triggering storlet with slName: "
						+ trigger.getSyncAct().getStorlet_name() + " port: "
						+ trigger.getSyncAct().getPort() + " params: "
						+ trigger.getSyncAct().getParameter());
				SyncOutputStream os = new SyncOutputStream(new Socket(
						"localhost", trigger.getSyncAct().getPort()));
				// scheduleTimer();
				// MemoryWarningSystem system = new MemoryWarningSystem();
				// system.addListener(new Listener() {
				// @Override
				// public void memoryUsageLow(long consumedMemory) {
				// System.out.println("concumed: "+consumedMemory);
				// }
				// });
				trigger(new ExecutionInfo("start", trigger.getSyncAct()
						.getStorlet_name(), "Sync"), exeStatus);
				storlet.get(os, trigger.getSyncAct().getParameter());
				trigger(new ExecutionInfo("stop", trigger.getSyncAct()
						.getStorlet_name(), "Sync"), exeStatus);

				// trigger(new CancelTimeout(timeoutId), timer);
				// logger.info("Sync triggering storlet with slName: "+trigger.getSyncAct().getStorlet_name()
				// +" completed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				trigger(new ExecutionInfo("stop", trigger.getSyncAct()
						.getStorlet_name(), "Sync"), exeStatus);
				logger.error(String.format("Error in sync triggering Storlet(%s)", trigger.getSyncAct()
						.getStorlet_name()), e);
			} catch (StorletException e) {
				// TODO Auto-generated catch block
				trigger(new ExecutionInfo("stop", trigger.getSyncAct()
						.getStorlet_name(), "Sync"), exeStatus);
				logger.error(String.format("Error in sync triggering Storlet(%s)", trigger.getSyncAct()
						.getStorlet_name()), e);
			}

		}
	};

	private Storlet loadStorlet(String slID) {

		// long startTime = System.currentTimeMillis();
		// MemoryStats startMs = sysMon.physical();
		// System.out.println("loading");
		Storlet storlet = null;

		File workFolder = new File(SREConst.slFolderPath + File.separator
				+ slID);
		Utils.deleteFileOrDirectory(workFolder);
		workFolder.mkdirs();
		ObjIdentifier storletInstanceId = ObjIdentifier.createFromString(slID);
		// ObjIdentifier storletInstanceId =
		// ObjIdentifier.createFromString(slID.substring(0, 26));// for
		// benchmarking, 3 is the number of suffix digits
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
			// fake cciclient for testing
//			Map<String, Object> slMD = FakeCCIClient
//					.getObjectMetadataEntries("");
			// TODO temp, remove
			ObjIdentifier storletDefinitionId = ObjIdentifier
					.createFromString((String) slMD
							.get(SPEConstants.STORLET_TAG_CODEOBJ));
			String slCodeType = (String) slMD
					.get(SPEConstants.STORLET_TAG_CODETYPE);

			// TODO temp, remove
			// keep the old Object Service unzip jar file API

			// check if code is stored in a different object
			// if yes download it and store in work folder

			if (!storletDefinitionId.equals(storletInstanceId)) {
				// Encoded String Stream

				 InputStream slDefinitionContentEncodedStream = oClient
				 .getObjectContentsAsStream(
				 storletDefinitionId.getTenantName(),
				 storletDefinitionId.getContainerName(),
				 storletDefinitionId.getObjectName());
				// Fake cciclient for testing
//				InputStream slDefinitionContentEncodedStream = FakeCCIClient
//						.getObjectContentsAsStream("");
				Utils.extractJarContents(
						Utils.decodeStream(slDefinitionContentEncodedStream),
						workFolder.getAbsolutePath());

				// TODO temp, remove
				byte[] slDefinitionContentBytes = Utils
						.inputStreamToByteArray(Utils
								.decodeStream(slDefinitionContentEncodedStream));
				logger.info("-----[TEMP]Extracted storlet DEFINITION content for "
						+ storletDefinitionId
						+ " into "
						+ workFolder.getAbsolutePath()
						+ "\n\t\t[exists="
						+ workFolder.exists()
						+ ":dir="
						+ workFolder.isDirectory()
						+ "]\n\t\t"
						+ Arrays.toString(workFolder.listFiles())
						+ "\n\t\tSize = " + slDefinitionContentBytes.length);
			}

			// ------------get the data-----------------
			 InputStream slInstanceContentEncodedStream = oClient
			 .getObjectContentsAsStream(
			 storletInstanceId.getTenantName(),
			 storletInstanceId.getContainerName(),
			 storletInstanceId.getObjectName());
//			InputStream slInstanceContentEncodedStream = FakeCCIClient
//					.getObjectContentsAsStream("");
			// IMPORTANT: Storlet files will overwrite Definition files
			Utils.extractJarContents(
					Utils.decodeStream(slInstanceContentEncodedStream),
					workFolder.getAbsolutePath());
			// long endTime = System.currentTimeMillis();
			// CpuTimes endCpuTimes = sysMon.cpuTimes();
			// MemoryStats endMs = sysMon.physical();
			// TODO temp, remove
			// byte[] slInstanceContentBytes =
			// Utils.inputStreamToByteArray(Utils
			// .decodeStream(slInstanceContentEncodedStream));
			// logger.info("-----[TEMP]Extracted storlet INSTANCE content for "
			// + storletInstanceId + " into "
			// + workFolder.getAbsolutePath() + "\n\t\t[exists="
			// + workFolder.exists() + ":dir=" + workFolder.isDirectory()
			// + "]\n\t\t" + Arrays.toString(workFolder.listFiles())
			// + "\n\t\tSize = " + slInstanceContentBytes.length);
			// oClient.unzipJar(workFolder.getAbsolutePath());//my unpack
			// method, slower
			// Load storlet class from jar & activate it
			// load constraints
			logger.info("storlet class unziped: " + storletInstanceId.toString());
			Properties constraints = getConstraintsFromFile(workFolder);
			if (constraints.get("time") != null){
				System.out.println(constraints.get("time"));
				timeConstraint = Long.parseLong(constraints.get("time").toString());
			}
			logger.info("storlet properties loaded: ");
			long startTime = System.currentTimeMillis();
			// CpuTimes startCpuTimes = sysMon.cpuTimes();
			File jarFile = new File(workFolder,
					SPEConstants.STORLET_CODE_FILENAME);
			Class<? extends Storlet> storletClass = (Class<? extends Storlet>) Utils
					.loadStorletClass(jarFile, slCodeType);
			logger.info("storlet class loaded: " + storletClass.getName());

			storlet = Storlet.createStorlet(storletClass, workFolder,
					SREConst.ccsURL);
			long endTime = System.currentTimeMillis();
			// CpuTimes endCpuTimes = sysMon.cpuTimes();
			logger.info("load the storlet cost " + (endTime - startTime));
			// benchLogger.info((endTime-startTime) + " on " +
			// slID.substring(26, slID.length()));
			// benchLogger.info((endMs.getFreeBytes()-startMs.getFreeBytes()));

		} catch (Exception e) {
			logger.error(String.format("Error in loadStorlet(%s)", slID), e);
			trigger(new StorletLoadingFault(e, slID), this.control);
		}
		// if (!SREConst.noCache)
		// storletQueue.put(slID, storlet);

		return storlet;
	}

	private Properties getConstraintsFromFile(File workingDirectory) {
		Properties params = new Properties();
		FileInputStream fis;
		try {
			File file = new File(workingDirectory.getAbsolutePath()
					+ File.separator
					+ SPEConstants.STORLET_CONSTRAINTS_FILENAME);
			if (file.exists()) {
				fis = new FileInputStream(workingDirectory.getAbsolutePath()
						+ File.separator
						+ SPEConstants.STORLET_CONSTRAINTS_FILENAME);
				params.load(fis);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.info(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info(e.getMessage());
		}
		return params;
	}
	// private synchronized void logAndIncreament(){
	// loadedSl++;
	// //if (loadedSl%10 == 1){
	// long memCost = (startMem - sysMon.physical().getFreeBytes());
	// benchLogger.info(memCost);
	// //}
	//
	// }
}