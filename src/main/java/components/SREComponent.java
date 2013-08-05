package components;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import msgTypes.SyncSLActivation;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.LoggerFactory;

import com.jezhumble.javasysmon.JavaSysMon;

import porttypes.SlRequest;
import eu.visioncloud.storlet.common.Utils;
import events.SlDelete;
import events.SyncTrigger;
import events.AsyncTrigger;
import events.StorletInit;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import web.SREJettyWebServer;
import constant.SREConst;

public class SREComponent extends ComponentDefinition {

	Negative<SlRequest> slReq = negative(SlRequest.class);
	private Map<String, Component> storletQueue;
	private static final Logger logger = Logger.getLogger(SREComponent.class);
	int counter = 0;
	private static final Logger benchLogger = Logger.getLogger("benchmark");
	private static final JavaSysMon sysMon = new JavaSysMon();
	public SREComponent() {
		PropertyConfigurator.configure("log4j.properties");
		storletQueue = new HashMap<String, Component>();
		subscribe(slTriggerH, slReq);
		subscribe(slSyncH, slReq);
		subscribe(slDeleteH, slReq);
	}

	Handler<AsyncTrigger> slTriggerH = new Handler<AsyncTrigger>() {
		public void handle(AsyncTrigger slEvent) {
			logger.info("received an Async Trigger with slID: "+slEvent.getSlID()+" activatiinID: "+slEvent.getActId());
			Component storletWrapper;
			if (!storletQueue.containsKey(slEvent.getSlID())) {
				logger.info("storlet not exists");
				storletWrapper = create(StorletWrapper.class);
				storletQueue.put(slEvent.getSlID(), storletWrapper);
				//Increament();
				//trigger(new StorletInit(slEvent.getSlID()+counter), storletWrapper.getControl());
				
				trigger(new StorletInit(slEvent.getSlID()), storletWrapper.getControl());
				logger.info("storlet wrapper created, storlet instantiating");
			}
			else{
				logger.info("storlet exists");
				storletWrapper = storletQueue.get(slEvent.getSlID());
				logger.info("storlet loaded");
			}
			trigger(slEvent, storletWrapper.getPositive(SlRequest.class));
			logger.info("Async Trigger Event forwarded");
		}
	};
	Handler<SyncTrigger> slSyncH = new Handler<SyncTrigger>() {
		public void handle(SyncTrigger slEvent) {
			SyncSLActivation syncAct = slEvent.getSyncAct();
			logger.info("received an Sync Trigger for slName: "+syncAct.getStorlet_name());
			Component storletWrapper;
			if (!storletQueue.containsKey(syncAct.getStorlet_name())) {
				logger.info("storlet not exists");
				storletWrapper = create(StorletWrapper.class);
				storletQueue.put(syncAct.getStorlet_name(), storletWrapper);
				logger.info("storlet wrapper created, storlet instantiating");
				trigger(new StorletInit(syncAct.getStorlet_name()), storletWrapper.getControl());
			}
			else{
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
			logger.info("received an Deletion Request for slID: " + slEvent.getSlID());
			destroy(storletQueue.get(slEvent.getSlID()));
			storletQueue.remove(slEvent.getSlID());
			File workFolder = new File(SREConst.slFolderPath + File.separator
					+ slEvent.getSlID());
			Utils.deleteFileOrDirectory(workFolder);
		}
	};
	private synchronized void Increament(){
		counter++;
	}
}
