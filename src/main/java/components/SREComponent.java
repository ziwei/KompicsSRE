package components;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import porttypes.SRESl;
import porttypes.SlRequest;
import events.SlDelete;
import events.SyncTrigger;
import events.AsyncTrigger;
import events.StorletInit;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.Stop;
import se.sics.kompics.address.Address;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.WebRequest;
import util.JarInJarClassLoader;

public class SREComponent extends ComponentDefinition {
	private Address self;
	private Map<String, Component> storletQueue;
	Negative<SlRequest> slReq = negative(SlRequest.class);

	// Positive<SRESl> sreSlOut = positive(SRESl.class);
	public SREComponent() {
		storletQueue = new HashMap();
		subscribe(slTriggerH, slReq);
		subscribe(slSyncH, slReq);
		subscribe(slDeleteH, slReq);
	}

	Handler<AsyncTrigger> slTriggerH = new Handler<AsyncTrigger>() {
		public void handle(AsyncTrigger slEvent) {

			String storletName = slEvent.getContent();
			String id = slEvent.getStorletId();
			System.out.println("SRE is triggering the storlet with ID " + id);
			if (!storletQueue.containsKey(slEvent.getStorletId())) {
				System.out.println("Storlet not exists");
				Component newStorlet = create(StorletWrapper.class);
				storletQueue.put(id, newStorlet);
				trigger(new StorletInit(), newStorlet.getControl()); //init storletinit obj!!!
//
				trigger(slEvent, newStorlet.getPositive(SlRequest.class));
//					// trigger(new Stop(), newStorlet.getControl());
			} else {
				System.out.println("Storlet exists");
				Component existStorlet = storletQueue.get(id);
				trigger(slEvent, existStorlet.getPositive(SlRequest.class));
				// trigger(new Stop(), existStorlet.getControl());
			}

		}
	};
	Handler<SyncTrigger> slSyncH = new Handler<SyncTrigger>() {
		public void handle(SyncTrigger slEvent) {
			String storletName = slEvent.getContent();
			String id = slEvent.getStorletId();

			if (storletQueue.containsKey(id)) {
				System.out.println("SRE is sync triggering the storlet with ID "
						+ id);
				storletQueue.remove(id);
			} else {
				System.out.println("storlet with ID " + id + " not exists");
			}
		}
	};
	Handler<SlDelete> slDeleteH = new Handler<SlDelete>() {
		public void handle(SlDelete slEvent) {
			String id = slEvent.getStorletId();
			if (storletQueue.containsKey(id)) {
				System.out.println("SRE is deleting the storlet with ID " + id);
				storletQueue.remove(id);
			} else {
				System.out.println("storlet with ID " + id + " not exists");
			}
		}
	};

}
