package components;

import java.io.IOException;
import eu.visioncloud.storlet.common.Storlet;
import eu.visioncloud.storlet.common.StorletException;
import eu.visioncloud.storlet.common.SyncOutputStream;
import events.AsyncTrigger;
import events.StorletInit;
import events.SyncTrigger;
import porttypes.SlRequest;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import storlets4testing.StorletInitSample;
import util.JarInJarClassLoader;
import java.net.Socket;
import java.net.URISyntaxException;

public class StorletWrapper extends ComponentDefinition {
	Negative<SlRequest> slReq = negative(SlRequest.class);
	int messages;
	private Storlet storlet;
	private Socket socket;
	

	public StorletWrapper() {
		subscribe(handleInit, control);
		subscribe(slAsyncTriggerH, slReq);
		subscribe(slSyncTriggerH, slReq);
	}

	private Handler<StorletInit> handleInit = new Handler<StorletInit>() {
		public void handle(StorletInit init) {
			if (init.getSocket() != null) {
				socket = init.getSocket();
			}
			try {
				storlet = StorletInitSample.createSampleStorlet();
			} catch (IOException  e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StorletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	Handler<AsyncTrigger> slAsyncTriggerH = new Handler<AsyncTrigger>() {
		public void handle(AsyncTrigger trigger) {
			messages++;
			System.out.println("Storlet Async " + trigger.getHandlerId()
					+ " got " + messages + " msgs");
			// need evaluate the event before execution
			try {
				storlet.getTriggerHandler(trigger.getHandlerId()).execute(
						StorletInitSample.createSampleEvent());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StorletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	Handler<SyncTrigger> slSyncTriggerH = new Handler<SyncTrigger>() {
		public void handle(SyncTrigger trigger) {
			messages++;
			if (socket == null)
				return;

			try {
				SyncOutputStream os = new SyncOutputStream(socket);
				storlet.get(os, "");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StorletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

}
