package fakeStorletInterface;

import java.io.File;

import org.apache.log4j.Logger;

//import eu.visioncloud.cci.client.ClientInterface;

/**
 * Contains the computation that should be executed when the matching
 * {@link Trigger} is received
 */
public abstract class TriggerHandler {
	private Storlet storlet = null;
	private String id = null;

	public TriggerHandler(Storlet storlet, String id) {
		this.storlet = storlet;
		this.id = id;
	}

	/**
	 * Returns client for accessing object service and content centric storage
	 * 
	 * @return {@link ClientInterface} reference
	 * @throws StorletException
	 */
//	protected final ClientInterface getStorageClient() throws StorletException {
//		return storlet.getStorageClient();
//	}

	/**
	 * Provides a reference to storlet properties
	 */
	protected final String getProperty(String key) throws StorletException {
		return storlet.getProperty(key);
	}

	/**
	 * Provides a reference to storlet logger
	 * 
	 * @throws StorletException
	 */
	protected final Logger getLogger() throws StorletException {
		return storlet.getLogger();
	}

	// TODO remove later
	/**
	 * Provides access to a working directory on local file system
	 */
	protected final File getWorkingDirectory() {
		return storlet.getWorkingDirectory();
	}

	/**
	 * DO NOT call this from the programming environment!
	 */
	public final void execute(EventModel event) throws StorletException {
		onExecute(event, storlet.getLogger(), null);
	}

	/**
	 * Override this method to specify the computation logic (handler) of a
	 * storlet
	 * 
	 * @param event
	 *            the event that caused the trigger
	 * 
	 * @throws StorletException
	 */
	public abstract void onExecute(EventModel event, Logger logger,
			Object storageClient) throws StorletException;

	// TODO this is expletive lame, but it's something
	@Override
	public boolean equals(Object object) {
		TriggerHandler other = ((TriggerHandler) object);
		return Utils.equalityGeneric(this.id, other.id);
	}

	String getId() {
		return id;
	}
}
