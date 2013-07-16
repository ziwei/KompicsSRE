package components;


import static tests.SPETestConstants.VALUE1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.rules.TemporaryFolder;

import eu.visioncloud.ExampleStorlet;
import eu.visioncloud.storlet.common.EventModel;
import eu.visioncloud.storlet.common.SPEConstants;
import eu.visioncloud.storlet.common.Storlet;
import eu.visioncloud.storlet.common.StorletException;
import eu.visioncloud.storlet.common.Utils;
import events.AsyncTrigger;
import events.StorletInit;
import events.SyncTrigger;
import porttypes.SlRequest;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import tests.HelloFileStorlet;
import tests.SPETestConstants;
import util.JarInJarClassLoader;

public class StorletWrapper extends ComponentDefinition {
	Negative<SlRequest> slReq = negative(SlRequest.class);
	int messages;
	Storlet storlet;
	public TemporaryFolder tempDir = new TemporaryFolder();
	Properties props = new Properties();
	
	public StorletWrapper() {
		subscribe(handleInit, control);
		subscribe(slAsyncTriggerH, slReq);
		subscribe(slSyncTriggerH, slReq);
	}

	private Handler<StorletInit> handleInit = new Handler<StorletInit>() {
		public void handle(StorletInit init) {
			// System.out.println("Storlet " +
			// this.getClass().getSimpleName()+" init");
			
			props.setProperty(SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_KEY,
					SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_VAL);

			String authString = "";
			String contentCentricUrl = "/";

			File dir1 = null;
			try {
				dir1 = tempDir.newFolder();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			File propsFile1 = new File(dir1, SPEConstants.STORLET_PARAMS_FILENAME);
			File authFile1 = new File(dir1, SPEConstants.STORLET_AUTH_FILENAME);
			try {
				Utils.byteArrayToFile(authString.getBytes(),
						authFile1.getAbsolutePath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				props.store(new FileOutputStream(propsFile1), "temp property file 1");
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//System.out.println("workDir "+ "ccu "+ contentCentricUrl);
			try {
				storlet = Storlet.createStorlet(HelloFileStorlet.class, dir1, contentCentricUrl);
				System.out.println("Storlet created succ");
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
			System.out.println("Storlet Async " + trigger.getHandlerId()
					+ " got " + messages + " msgs");
			
			File workDir = null;
			try {
				workDir = tempDir.newFolder();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			File propsFile = new File(workDir, SPEConstants.STORLET_PARAMS_FILENAME);

			String authString = "";
			File authFile = new File(workDir, SPEConstants.STORLET_AUTH_FILENAME);
			try {
				Utils.byteArrayToFile(authString.getBytes(), authFile.getAbsolutePath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			props.setProperty(SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_KEY,
					SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_VAL);
			try {
				props.store(new FileOutputStream(propsFile), "temp property file");
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String contentCentricUrl = "/";


			// Create event to trigger storlet
			Map<String, String> metadata = new HashMap<String, String>();
			metadata.put(SPETestConstants.KEY1, VALUE1);
			File tempFile = null;
			try {
				tempFile = tempDir.newFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			metadata.put(SPETestConstants.KEY_PATH, tempFile.getAbsolutePath());

			EventModel event = new EventModel();
			event.setOldMetadata(metadata);
			event.setNewMetadata(metadata);
			event.setrFlag(false);
			try {
				storlet.getTriggerHandler(trigger.getHandlerId()).execute(event);
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
	
	private void buildProperties(File dir1, String contentCentricUrl) throws IOException{
		Properties props = new Properties();
		props.setProperty(SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_KEY,
				SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_VAL);

		String authString = "";
		contentCentricUrl = "/";

		dir1 = tempDir.newFolder();
		File propsFile1 = new File(dir1, SPEConstants.STORLET_PARAMS_FILENAME);
		File authFile1 = new File(dir1, SPEConstants.STORLET_AUTH_FILENAME);
		Utils.byteArrayToFile(authString.getBytes(),
				authFile1.getAbsolutePath());
		props.store(new FileOutputStream(propsFile1), "temp property file 1");
	}
}
