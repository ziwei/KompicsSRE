package util;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Configurator {
	private static Properties prop = new Properties();
	private static String root = "";
	public static void init(String rootPath, String configPath){
		try {
			root = rootPath;
			prop.load(new FileReader(root+configPath));
			System.setProperty("SRERootPath", root);
			//logProp.load(SREConst.class.getResourceAsStream("/log4j.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String getRoot(){
		return root;
	}
	public static String config (String key){
		
		return prop.getProperty(key);
	}
}
