package storlets4testing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.visioncloud.cci.client.ClientInterface;
import eu.visioncloud.storlet.common.EventModel;
import eu.visioncloud.storlet.common.Storlet;
import eu.visioncloud.storlet.common.StorletException;
import eu.visioncloud.storlet.common.SyncOutputStream;
import eu.visioncloud.storlet.common.TriggerHandler;

import tests.SPETestConstants;

public class HelloFileStorlet extends Storlet {

	@Override
	protected Set<TriggerHandler> getTriggerHandlers() {
		System.out.println("storlets cool 2 1");
		TriggerHandler handler = new TriggerHandler(this, "handlerH") {
			@Override
			public void onExecute(EventModel event, Logger logger,
					ClientInterface storageClient) throws StorletException {
				try {
					String filePath = event.getNewMetadata().get(
							SPETestConstants.KEY_PATH);
					File file = new File(filePath);
					FileOutputStream fos = new FileOutputStream(file);
					String fileContents = getProperty(SPETestConstants.PROPERTIES_TEMP_FILE_CONTENTS_KEY);
					fos.write(fileContents.getBytes());
					fos.flush();
					fos.close();
					System.out.println("Hello async exec suceess");
				} catch (IOException e) {
					throw new StorletException(e.getCause());
				}
			}
		};
		System.out.println("storlets cool 2 2");
		Set<TriggerHandler> triggerHandlers = new HashSet<TriggerHandler>();
		triggerHandlers.add(handler);
		return triggerHandlers;
	}

	@Override
	public void onGet(SyncOutputStream os, String param, Logger logger,
			ClientInterface storageClient) throws StorletException {
		System.out.println("Hello sync exec suceess");
		// TODO Auto-generated method stub

	}
}
