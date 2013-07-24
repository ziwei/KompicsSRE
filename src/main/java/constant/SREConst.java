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
	//public static final String logFilePath = logProp.getProperty("log4j.appender.stdout.File");
	
	public static final String ccsURL = prop.getProperty("ccsURL");
	public static final String objsURL = prop.getProperty("objsURL");
	
	public static final boolean noCache = "true".equals(prop.getProperty("noCache"));

}
