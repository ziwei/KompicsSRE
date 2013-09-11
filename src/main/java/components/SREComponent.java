package components;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import msgTypes.SyncSLActivation;

import org.apache.log4j.Logger;

import constant.SREConst;

import porttypes.ExeStatus;
import porttypes.SlRequest;
import eu.visioncloud.storlet.common.Utils;
import events.ExecutionInfo;
import events.MyTimeout;
import events.SlDelete;
import events.StorletLoadingFault;
import events.SyncTrigger;
import events.AsyncTrigger;
import events.StorletInit;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.kompics.timer.CancelTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public class SREComponent extends ComponentDefinition {

	Negative<SlRequest> slReq = negative(SlRequest.class);
	Positive<Timer> timer = positive(Timer.class);

	private Map<String, Component> storletQueue;
	private Map<String, UUID> timeoutIds;
	private static final Logger logger = Logger.getLogger(SREComponent.class);

	// int counter = 0;
	// private static final Logger benchLogger = Logger.getLogger("benchmark");
	// private static final JavaSysMon sysMon = new JavaSysMon();

	public SREComponent() {
		// PropertyConfigurator.configure("log4j.properties");
		storletQueue = new HashMap<String, Component>();
		timeoutIds = new HashMap<String, UUID>();
		subscribe(slTriggerH, slReq);
		subscribe(slSyncH, slReq);
		subscribe(slDeleteH, slReq);
		subscribe(handleTimtout, timer);
	}

	Handler<MyTimeout> handleTimtout = new Handler<MyTimeout>() {

		@Override
		public void handle(MyTimeout event) {
			// TODO Auto-generated method stub
			System.out.println("the execution of " + event.getSlID() + "."
					+ event.getHandler() + " timeout");

		}

	};

	Handler<ExecutionInfo> handlerExeEvent = new Handler<ExecutionInfo>() {

		@Override
		public void handle(ExecutionInfo event) {
			// TODO Auto-generated method stub
			if (event.getTimeConstraint() != -1) {
				if (event.getStatus().equals("start"))
					scheduleTimer(event.getTimeConstraint(), event.getSlID(),
							event.getHandler());
				else if (event.getStatus().equals("stop"))
					trigger(new CancelTimeout(timeoutIds.get(event.getSlID()
							+ event.getHandler())), timer);
			}
		}

	};

	Handler<StorletLoadingFault> faultH = new Handler<StorletLoadingFault>() {
		public void handle(StorletLoadingFault fault) {
			// System.out.println(fault.getSlID());
			Component delSl = storletQueue.get(fault.getSlID());
			if (null != delSl) {
				destroy(delSl);
				storletQueue.remove(fault.getSlID());
				File workFolder = new File(SREConst.slFolderPath
						+ File.separator + fault.getSlID());
				Utils.deleteFileOrDirectory(workFolder);
			}
		}
	};

	Handler<AsyncTrigger> slTriggerH = new Handler<AsyncTrigger>() {
		public void handle(AsyncTrigger slEvent) {
			logger.info("received an Async Trigger with slID: "
					+ slEvent.getSlID() + " activatiinID: "
					+ slEvent.getActId());
			Component storletWrapper;
			if (!storletQueue.containsKey(slEvent.getSlID())) {
				logger.info("storlet not exists");
				storletWrapper = create(StorletWrapper.class);
				subscribe(faultH, storletWrapper.getControl());
				subscribe(handlerExeEvent,
						storletWrapper.getNegative(ExeStatus.class));
				storletQueue.put(slEvent.getSlID(), storletWrapper);
				// Increament();
				// trigger(new StorletInit(slEvent.getSlID()+counter),
				// storletWrapper.getControl());

				trigger(new StorletInit(slEvent.getSlID()),
						storletWrapper.getControl());
				logger.info("storlet wrapper created, storlet instantiating");
			} else {
				logger.info("storlet exists");
				storletWrapper = storletQueue.get(slEvent.getSlID());
				logger.info("storlet loaded");
			}
			trigger(slEvent, storletWrapper.getPositive(SlRequest.class));

			logger.info("Async Trigger Event processed");
		}
	};
	Handler<SyncTrigger> slSyncH = new Handler<SyncTrigger>() {
		public void handle(SyncTrigger slEvent) {
			SyncSLActivation syncAct = slEvent.getSyncAct();
			logger.info("received an Sync Trigger for slName: "
					+ syncAct.getStorlet_name());
			Component storletWrapper;
			if (!storletQueue.containsKey(syncAct.getStorlet_name())) {
				logger.info("storlet not exists");
				storletWrapper = create(StorletWrapper.class);
				subscribe(faultH, storletWrapper.getControl());
				storletQueue.put(syncAct.getStorlet_name(), storletWrapper);
				logger.info("storlet wrapper created, storlet instantiating");
				trigger(new StorletInit(syncAct.getStorlet_name()),
						storletWrapper.getControl());
			} else {
				logger.info("storlet exists");
				storletWrapper = storletQueue.get(syncAct.getStorlet_name());
				logger.info("storlet loaded");
			}
			trigger(slEvent, storletWrapper.getPositive(SlRequest.class));
			logger.info("Sync Trigger Event forwarded");
		}
	};
	Handler<SlDelete> slDeleteH = new Handler<SlDelete>() {
		public void handle(SlDelete slEvent) {
			logger.info("received an Deletion Request for slID: "
					+ slEvent.getSlID());
			Component delSl = storletQueue.get(slEvent.getSlID());
			if (null != delSl) {
				destroy(delSl);
				storletQueue.remove(slEvent.getSlID());
				File workFolder = new File(SREConst.slFolderPath
						+ File.separator + slEvent.getSlID());
				Utils.deleteFileOrDirectory(workFolder);
			}
		}
	};

	private void scheduleTimer(long delay, String slID, String handler) {
		ScheduleTimeout st = new ScheduleTimeout(delay);
		st.setTimeoutEvent(new MyTimeout(st, slID, handler));
		UUID timeoutId = st.getTimeoutEvent().getTimeoutId();
		timeoutIds.put(slID + handler, timeoutId);
		trigger(st, timer);
	}
	// private synchronized void Increament() {
	// counter++;
	// }
}
