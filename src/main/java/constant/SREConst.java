package constant;

import java.io.IOException;
import java.util.Properties;

public class SREConst {
	private static Properties prop = new Properties();
	//private static Properties logProp = new Properties();

	static {
		try {
			prop.load(SREConst.class.getResourceAsStream("/visioncloud_SRE.conf"));
			//logProp.load(SREConst.class.getResourceAsStream("/log4j.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// SRE constants
	public static final String tmpFolderPath = prop
			.getProperty("tmpFolderPath");
	public static final String jarFolderPath = prop
			.getProperty("jarFolderPath");
	public static final String slFolderPath = prop.getProperty("slFolderPath");
	public static final String logFilePath = prop.getProperty("logFilePath");
	
	public static final String ccsURL = prop.getProperty("ccsURL");
	public static final String objsURL = prop.getProperty("objsURL");
	
	public static final String user = prop.getProperty("user");
	public static final String tenant = prop.getProperty("tenant");
	public static final String password = prop.getProperty("password");
	
	public static final String workerNumber = prop.getProperty("workerNumber");
	
	public static final String externalip = prop.getProperty("externalip");
	public static final String internalip = prop.getProperty("internalip");
	public static final String port = prop.getProperty("port");
	public static final String timeout = prop.getProperty("timeout");
	public static final String threads = prop.getProperty("threads");
	
	public static final boolean noCache = "true".equals(prop.getProperty("noCache"));

}
