package components;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import msgTypes.SyncSLActivation;

import org.slf4j.LoggerFactory;

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
import constant.SREConst;

public class SREComponent extends ComponentDefinition {

	Negative<SlRequest> slReq = negative(SlRequest.class);
	// Object sourceReady = 0;
	//public final String tempDir = "/home/ziwei/workspace/KompicsSRE/tmp/";
	//final String jarName = "test";
	private Map<String, Component> storletQueue;
	private static final org.slf4j.Logger log = LoggerFactory
			.getLogger(SREComponent.class);
	public SREComponent() {
		storletQueue = new HashMap<String, Component>();
		subscribe(slTriggerH, slReq);
		subscribe(slSyncH, slReq);
		subscribe(slDeleteH, slReq);
	}

	Handler<AsyncTrigger> slTriggerH = new Handler<AsyncTrigger>() {
		public void handle(AsyncTrigger slEvent) {
			Component storletWrapper;
			if (!storletQueue.containsKey(slEvent.getSlID())) {
				storletWrapper = create(StorletWrapper.class);
				storletQueue.put(slEvent.getSlID(), storletWrapper);
				trigger(new StorletInit(slEvent.getSlID()), storletWrapper.getControl());
			}
			else{
				storletWrapper = storletQueue.get(slEvent.getSlID());
			}
			trigger(slEvent, storletWrapper.getPositive(SlRequest.class));
		}
	};
	Handler<SyncTrigger> slSyncH = new Handler<SyncTrigger>() {
		public void handle(SyncTrigger slEvent) {
			SyncSLActivation syncAct = slEvent.getSyncAct();
			Component storletWrapper;
			if (!storletQueue.containsKey(syncAct.getStorlet_name())) {
				storletWrapper = create(StorletWrapper.class);
				storletQueue.put(syncAct.getStorlet_name(), storletWrapper);
				trigger(new StorletInit(syncAct.getStorlet_name()), storletWrapper.getControl());
			}
			else{
				storletWrapper = storletQueue.get(syncAct.getStorlet_name());
			}
			trigger(slEvent, storletWrapper.getPositive(SlRequest.class));
		}
	};
	Handler<SlDelete> slDeleteH = new Handler<SlDelete>() {
		public void handle(SlDelete slEvent) {
			String id = slEvent.getSlID();

			storletQueue.remove(id);
			File workFolder = new File(SREConst.slFolderPath + File.separator
					+ id);
			Utils.deleteFileOrDirectory(workFolder);
			log.info("Delete Storlet " + id);
		}
	};

}
