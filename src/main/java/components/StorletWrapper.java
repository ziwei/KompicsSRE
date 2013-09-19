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
import eu.visioncloud.storlet.common.TriggerHandler;
import eu.visioncloud.storlet.common.Utils;
import gr.ntua.vision.monitoring.dispatch.VismoEventDispatcher;

import events.AsyncTrigger;
import events.ExecutionInfo;
import events.StorletInit;
import events.StorletLoadingFault;
import events.SyncTrigger;
import porttypes.ExeStatus;
import porttypes.SlRequest;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;

import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import constant.SREConst;


public class StorletWrapper extends ComponentDefinition {
	Negative<SlRequest> slReq = negative(SlRequest.class);
	Positive<ExeStatus> exeStatus = positive(ExeStatus.class);

	// private static int loadedSl = 0;//for benchmarking
	private Storlet storlet;

	public static Map<String, Set> slLogTable = new HashMap<String, Set>();
	public static Map<String, AsyncTrigger> actLogTable = new HashMap<String, AsyncTrigger>();

	private static ClientInterface oClient;
	final VismoEventDispatcher dispatcher = new VismoEventDispatcher("SRE");//can add config file param
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
				TriggerHandler th = storlet.getTriggerHandler(trigger
						.getHandlerId());

				if (th != null){
					long startTime = System.currentTimeMillis();
					th.execute(trigger.getEventModel());
					long endTime = System.currentTimeMillis();
					ObjIdentifier slID = ObjIdentifier.createFromString(trigger.getSlID());
					dispatcher.newEvent().field("tenantID", slID.getTenantName()).field("containerID", slID.getContainerName())
					.field("objectID", slID.getObjectName()).field("end_time", endTime).field("start_time", startTime)
					.field("count", 1).field("storletType", "STORLET").send();
					logger.info("Billing event \n{\n\t tenantID : " + slID.getTenantName() + "\n\t containerID : " + slID.getContainerName()
							+ "\n\t objectID : " + slID.getObjectName() + "\n\t end_time : " + endTime + "\n\t start_time : "
							+ startTime + "\n\t count : 1" + "\n\t storletType : STORLET" + "\n}");
					dispatcher.newEvent().field("storletId", trigger.getSlID()).field("end_time", endTime)
					.field("start_time", startTime).field("objectId", trigger.getEventModel().getObjectName())
					.field("machineId", trigger.getEventModel().getPosition()).send();
					logger.info("SLA event \n{\n\t storletId : " + trigger.getSlID() + "\n\t end_time : " 
							+ endTime + "\n\t start_time : " + startTime + "\n\t objectId : " + trigger.getEventModel().getObjectName()
							+ "\n\t machineId : " + trigger.getEventModel().getPosition() + "\n}");
				}
				else
					logger.info("no valid triggers");
			} catch (Exception e) {
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
				trigger(new ExecutionInfo("start", trigger.getSyncAct()
						.getStorlet_name(), "Sync"), exeStatus);
				long startTime = System.currentTimeMillis();
				storlet.get(os, trigger.getSyncAct().getParameter());
				long endTime = System.currentTimeMillis();
				ObjIdentifier slID = ObjIdentifier.createFromString(trigger.getSyncAct().getStorlet_name());
				dispatcher.newEvent().field("tenantID", slID.getTenantName()).field("containerID", slID.getContainerName())
				.field("objectID", slID.getObjectName()).field("end_time", endTime).field("start_time", startTime)
				.field("count", 1).field("storletType", "STORLET").send();
				logger.info("Billing event \n{\n\t tenantID : " + slID.getTenantName() + "\n\t containerID : " + slID.getContainerName()
						+ "\n\t objectID : " + slID.getObjectName() + "\n\t end_time : " + endTime + "\n\t start_time : "
						+ startTime + "\n\t count : 1" + "\n\t storletType : STORLET"+ "\n}");
				dispatcher.newEvent().field("storletId", trigger.getSyncAct().getStorlet_name()).field("end_time", endTime)
				.field("start_time", startTime).field("objectId", slID.getObjectName())
				.field("machineId", SREConst.externalip).send();
				logger.info("SLA event \n{\n\t storletId : " + trigger.getSyncAct().getStorlet_name() + "\n\t end_time : " 
				+ endTime + "\n\t start_time : " + startTime + "\n\t objectId : " + slID.getObjectName() + "\n\t machineId : "
				+ SREConst.externalip+ "\n}");
				trigger(new ExecutionInfo("stop", trigger.getSyncAct()
						.getStorlet_name(), "Sync"), exeStatus);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				trigger(new ExecutionInfo("stop", trigger.getSyncAct()
						.getStorlet_name(), "Sync"), exeStatus);
				logger.error(String.format(
						"Error in sync triggering Storlet(%s)", trigger
								.getSyncAct().getStorlet_name()), e);
			} catch (StorletException e) {
				// TODO Auto-generated catch block
				trigger(new ExecutionInfo("stop", trigger.getSyncAct()
						.getStorlet_name(), "Sync"), exeStatus);
				logger.error(String.format(
						"Error in sync triggering Storlet(%s)", trigger
								.getSyncAct().getStorlet_name()), e);
			}

		}
	};

	private Storlet loadStorlet(String slID) {
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
			// TODO temp, remove
			ObjIdentifier storletDefinitionId = ObjIdentifier
					.createFromString((String) slMD
							.get(SPEConstants.STORLET_TAG_CODEOBJ));
			String slCodeType = (String) slMD
					.get(SPEConstants.STORLET_TAG_CODETYPE);

			// TODO temp, remove

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

			// IMPORTANT: Storlet files will overwrite Definition files
			Utils.extractJarContents(
					Utils.decodeStream(slInstanceContentEncodedStream),
					workFolder.getAbsolutePath());
			logger.info("storlet class unziped: "
					+ storletInstanceId.toString());
			Properties constraints = getConstraintsFromFile(workFolder);
			if (constraints.get("time") != null) {
				System.out.println(constraints.get("time"));
				timeConstraint = Long.parseLong(constraints.get("time")
						.toString());
			}
			logger.info("storlet properties loaded: ");
			
			// CpuTimes startCpuTimes = sysMon.cpuTimes();
			File jarFile = new File(workFolder,
					SPEConstants.STORLET_CODE_FILENAME);
			Class<? extends Storlet> storletClass = (Class<? extends Storlet>) Utils
					.loadStorletClass(jarFile, slCodeType);
			logger.info("storlet class loaded: " + storletClass.getName());

			storlet = Storlet.createStorlet(storletClass, workFolder,
					SREConst.ccsURL);
			
			// CpuTimes endCpuTimes = sysMon.cpuTimes();
			//logger.info("load the storlet cost " + (endTime - startTime));

		} catch (Exception e) {
			logger.error(String.format("Error in loadStorlet(%s)", slID), e);
			trigger(new StorletLoadingFault(e, slID), this.control);
		}

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
}