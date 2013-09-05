package storlets4testing;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.visioncloud.cci.client.ClientInterface;
import eu.visioncloud.storlet.common.EventModel;
import eu.visioncloud.storlet.common.Storlet;
import eu.visioncloud.storlet.common.StorletException;
import eu.visioncloud.storlet.common.SyncOutputStream;
import eu.visioncloud.storlet.common.TriggerHandler;

public class ExampleStorlet extends Storlet {
	public static void main(String[] args) {

		try {
			Storlet.createStorlet(ExampleStorlet.class, new File("/tmp/"),
					"127.0.0.1");
		} catch (StorletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("hello world");
	}

	@Override
	protected Set<TriggerHandler> getTriggerHandlers() {

		// create a new handler called "update"
		TriggerHandler handler = new TriggerHandler(this, "update") {

			@Override
			public void onExecute(EventModel event, Logger logger,
					ClientInterface storageClient) throws StorletException {

				// get data from the storlet.properties
				System.out.println("executing async...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("executed async");
			}
		};

		// create a trigger set and return it
		Set<TriggerHandler> triggerHandlers = new HashSet<TriggerHandler>();
		triggerHandlers.add(handler);
		return triggerHandlers;
	}

	@Override
	public void onGet(SyncOutputStream os, String param, Logger logger,
			ClientInterface storageClient) throws StorletException {
		// this storlet will return hello world when it get is called
		// logging an access if VISION-Cloud works the same as in the other
		// handler
		System.out.println("executing sync");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("executed sync");

	}

}