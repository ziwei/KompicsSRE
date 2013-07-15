package storlets4testing;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import fakeStorletInterface.Storlet;

//import eu.visioncloud.cci.client.ClientInterface;
//import eu.visioncloud.cci.client.ContentCentricException;
//import eu.visioncloud.storlet.common.EventModel;
//import eu.visioncloud.storlet.common.Storlet;
//import eu.visioncloud.storlet.common.StorletException;
//import eu.visioncloud.storlet.common.SyncOutputStream;
//import eu.visioncloud.storlet.common.TriggerHandler;
//import eu.visioncloud.storlet.common.Utils;

public class ExampleStorlet extends Storlet {

	public static void main(String[] args) {
		
		try {
			Storlet.createStorlet(ExampleStorlet.class, new File("/tmp/"), "127.0.0.1");
		} catch (StorletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		System.out.println("hello world");
	}
	
	@Override
	protected Set<TriggerHandler> getTriggerHandlers() {

		// create a new handler called "update"
		TriggerHandler handler = new TriggerHandler(this, "update") {

			@Override
			public void onExecute(EventModel event, Logger logger,
					ClientInterface storageClient) throws StorletException {
				try {
					// get data from the storlet.properties
					String myKey = getProperty("myKey");
					String myKeyValue = getProperty("myKeyValue");

					// write to the logger
					logger.info(myKey);
					logger.info(myKeyValue);

					// create new object
					HashMap<String, String> metadata = new HashMap<String, String>();
					metadata.put(myKey, myKeyValue);
					storageClient.createObjectWithMetadata(
							event.getTenantName(), event.getContainerName(),
							event.getObjectName() + "-UpdatedCopy",
							"Hello World From An Object", metadata);
				} catch (ContentCentricException e) {
					throw new StorletException(e);
				}
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
		String helloWorld = "hello world";

		String[] params = param.split("\\.");

		try {
			logger.info(String
					.format("ExampleStorlet onGet params: Tenant=%s, Container=%s, Object=%s ",
							params[0], params[1], params[2]));

			InputStream objectContentStream = storageClient
					.getObjectContentsAsStream(params[0], params[1], params[2]);

			byte[] objectContent = Utils
					.inputStreamToByteArray(objectContentStream);

			// IMPORTANT the OutputStream has to be configured first
			// It takes the length of the Stream you are about to send
			// If you don't know the length set it to
			// SyncOutputStream.UNKNOWN_LENGTH
			// Note if length is not set object service will answer the user
			// before the Stream is closed
			os.configure(objectContent.length);
			logger.info(String.format("ExampleStorlet wrote %s bytes",
					objectContent.length));

			// write some data
			os.write(objectContent);
			logger.info(String.format("ExampleStorlet wrote %s", new String(
					objectContent)));

			// close the stream, if you forget the SRE will close it
			os.close();
			logger.info("ExampleStorlet closed stream");
		} catch (Exception e) {
			logger.error("ExampleStorlet onGet failed", e);
		}
	}
}