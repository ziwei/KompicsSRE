package components;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import events.AsyncTrigger;
import events.StorletInit;
import events.SyncTrigger;
import fakeStorletInterface.EventModel;
import fakeStorletInterface.Storlet;
import fakeStorletInterface.StorletException;
import porttypes.SlRequest;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import util.JarInJarClassLoader;

public class StorletWrapper extends ComponentDefinition {
	Negative<SlRequest> slReq = negative(SlRequest.class);
	int messages;
	Storlet storlet;

	public StorletWrapper() {
		subscribe(handleInit, control);
		subscribe(slAsyncTriggerH, slReq);
		subscribe(slSyncTriggerH, slReq);
	}

	private Handler<StorletInit> handleInit = new Handler<StorletInit>() {
		public void handle(StorletInit init) {
			// System.out.println("Storlet " +
			// this.getClass().getSimpleName()+" init");
			try {
				storlet = Storlet.createStorlet(init.getStorletType(), init.getWorkingDir(), init.getContentCentricUrl());
			} catch (StorletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//storlet.getTriggerHandler(HandlerID).execute(event);
		}
	};
	
	

	Handler<AsyncTrigger> slAsyncTriggerH = new Handler<AsyncTrigger>() {
		public void handle(AsyncTrigger trigger) {
			messages++;
			System.out.println("Storlet" + this.getClass().getSimpleName()
					+ " got " + messages + " msgs");
			
			EventModel event = new EventModel();
			event.setOldMetadata(trigger.getOldMeta());
			event.setNewMetadata(trigger.getNewMeta());
			event.setrFlag(trigger.getFlag());
			try {
				storlet.getTriggerHandler(trigger.getHandler()).execute(event);
			} catch (StorletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	};

	Handler<SyncTrigger> slSyncTriggerH = new Handler<SyncTrigger>() {
		public void handle(SyncTrigger trigger) {
			messages++;
			System.out.println("Storlet" + this.getClass().getSimpleName()
					+ " got " + messages + " msgs");
			try {
				storlet.get(null, null);
			} catch (StorletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};
	
}
