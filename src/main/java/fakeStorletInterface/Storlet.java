package fakeStorletInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

//import eu.visioncloud.cci.client.CdmiRestClient;
//import eu.visioncloud.cci.client.ClientInterface;

/**
 * Storlet, the unit of computation in Vision Cloud.
 */

public abstract class Storlet {

	// Storlets are stateless, they should not be persisted
	private Map<Object, TriggerHandler> triggerHandlers = new HashMap<Object, TriggerHandler>();
	private File workingDirectory = null;
	private AuthToken authToken = null;
	private Properties properties = null;
	private Logger logger = null;
	//private ClientInterface storageClient = null;

	protected Storlet() {
	}

	private void contextualize(File workingDirectory, String contentCentricUrl)
			throws StorletException, IOException {
		if (Utils.anyAreNull(workingDirectory, contentCentricUrl))
			throw new StorletException("Parameters may not be null");

		if (false == workingDirectory.exists())
			throw new StorletException("Working directory does not exist");

		this.workingDirectory = workingDirectory;
		this.properties = getPropertiesFromFile(workingDirectory);
		this.authToken = getAuthTokenFromFile(workingDirectory);

		// TODO temp, remove
		getLogger()
				.debug(String
						.format("[Storlet(%s).contextualize()] Authentication Details = ",
								this.getClass().getName(),
								authToken.getDecodedAuthenticationString()));

		//this.storageClient = new CdmiRestClient(contentCentricUrl,authToken.getAuthenticationString());

		// initialize trigger map
		for (TriggerHandler triggerHandler : getTriggerHandlers()) {
			triggerHandlers.put(triggerHandler.getId(), triggerHandler);
		}
	}

	// TODO maybe the storlet should get the Properties as a parameter here,
	// rather than finding it in its working directory?
	public static Storlet createStorlet(Class<? extends Storlet> storletType,
			File workingDirectory, String contentCentricUrl)
			throws StorletException {
		try {
			Storlet storlet = storletType.newInstance();
			storlet.contextualize(workingDirectory, contentCentricUrl);
			return storlet;
		} catch (Exception e) {
			throw new StorletException(
					"Could not instantiate Storlet instance", e.getCause());
		}
	}

	/**
	 * Read storlet parameters/constants from workingDirectory
	 * 
	 * @throws IOException
	 */
	private static Properties getPropertiesFromFile(File workingDirectory)
			throws IOException {
		FileInputStream fis = new FileInputStream(
				workingDirectory.getAbsolutePath() + File.separator
						+ SPEConstants.STORLET_PARAMS_FILENAME);
		Properties params = new Properties();
		params.load(fis);
		return params;
	}

	/**
	 * Read storlet authentication token from workingDirectory
	 * 
	 * @throws IOException
	 */
	private static AuthToken getAuthTokenFromFile(File workingDirectory)
			throws IOException {
		RandomAccessFile authFile = new RandomAccessFile(new File(
				workingDirectory.getAbsolutePath() + File.separator
						+ SPEConstants.STORLET_AUTH_FILENAME), "r");
		return new AuthToken(authFile.readLine());
	}

	// TODO need logic for extracting Trigger String to be somewhere, should it
	// be here?
	// public static Set<Trigger> getTriggers(Class<? extends Storlet>
	// storletType)
	// throws IllegalArgumentException, InstantiationException,
	// IllegalAccessException, InvocationTargetException {
	// return storletType.newInstance().getTriggers();
	// }

	/**
	 * Return the triggers that the Storlet wishes to be executed on
	 */
	protected abstract Set<TriggerHandler> getTriggerHandlers();

	/**
	 * Return TriggerHandler matching a given TriggerHandler ID
	 * 
	 * @throws StorletException
	 */
	public final TriggerHandler getTriggerHandler(Object id)
			throws StorletException {
		return triggerHandlers.get(id);
	}

	/**
	 * Return a {@link Logger} instance. All text written by this
	 * {@link Storlet} instance will be written to this logger.
	 * 
	 * @return a new {@link Logger} instance
	 * 
	 * @throws StorletException
	 */
	final Logger getLogger() throws StorletException {
		if (null == logger) {
			// create a logger and a matching log file
			logger = Logger.getLogger(workingDirectory.getName());
			FileAppender fileAppender;
			try {
				fileAppender = new FileAppender(new SimpleLayout(),
						workingDirectory.getPath() + File.separator
								+ workingDirectory.getName());
			} catch (IOException e) {
				throw new StorletException(e.getMessage(), e.getCause());
			}
			logger.addAppender(fileAppender);
		}
		return logger;
	}

	@Override
	public boolean equals(Object object) {
		Storlet other = (Storlet) object;

		if (false == Utils.equalityGenericMaps(this.triggerHandlers,
				other.triggerHandlers))
			return false;

		if (false == Utils.equalityGeneric(other.authToken, this.authToken))
			return false;

		if (false == Utils.equalityGeneric(other.workingDirectory,
				this.workingDirectory))
			return false;

		if (false == Utils.equalityGeneric(other.properties, this.properties))
			return false;

		return true;
	}

	/**
	 * Return a {@link ClientInterface} instance, for accessing VisionCloud
	 * content centric storage and object storage services
	 * 
	 * @return a new {@link ClientInterface} instance
	 * @throws StorletException
	 */
//	final ClientInterface getStorageClient() {
//		return storageClient;
//	}

	final String getProperty(String key) {
		return properties.getProperty(key);
	}

	// TODO remove later
	final File getWorkingDirectory() {
		return workingDirectory;
	}

	public final void get(SyncOutputStream os, String param)
			throws StorletException {
		onGet(os, param, getLogger(), null);
	}

	public void onGet(SyncOutputStream os, String param, Logger logger,
			Object storageClient) throws StorletException {
		try {
			String helloWorld = "";
			os.configure(helloWorld.length());
			os.write(helloWorld.getBytes());
			os.close();
		} catch (IOException e) {
			throw new StorletException(e.getCause());
		}
	}
}
