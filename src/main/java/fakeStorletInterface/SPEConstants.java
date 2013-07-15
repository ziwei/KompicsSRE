package fakeStorletInterface;

import java.io.IOException;
import java.util.Properties;

public class SPEConstants {

	// TODO not tested yet
	// reads property file on every access, less efficient, more flexible
	private static String get(String constantKey) {
		try {
			Properties speConfigProperties = new Properties();
			speConfigProperties.load(SPEConstants.class
					.getResourceAsStream("/spe.properties"));
			return speConfigProperties.getProperty(constantKey);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// reads property file on startup only, more efficient, less flexible
	// private static Properties speConfigProperties = new Properties();
	//
	// private static String get(String constantKey) {
	// return speConfigProperties.getProperty(constantKey);
	// }
	//
	// static {
	// try {
	// speConfigProperties.load(SPEConstants.class
	// .getResourceAsStream("/spe.properties"));
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	// For testing purposes only
	public static String HELLO_WORLD = get("test.helloworld");

	// Storlet Metadata Tags
	public static final String STORLET_TAG_TEMPLATE_DESCRIPTION =get("storlet.tag.template.description");
	public static final String STORLET_TAG_CODEOBJ = get("storlet.tag.codeobj");
	public static final String STORLET_TAG_CODETYPE = get("storlet.tag.codetype");
	public static final String STORLET_TAG_TRIGGERS = get("storlet.tag.triggers");

	public static final String STORLET_PARAMS_FILENAME = get("storlet.params.filename");
	public static final String STORLET_AUTH_FILENAME = get("storlet.auth.filename");
	public static final String STORLET_CONSTRAINTS_FILENAME = get("storlet.constraints.filename");
	public static final String STORLET_CODE_FILENAME = get("storlet.code.filename");
	
	public static final String CDMI_TYPE_CONTAINER = get("cdmi.type.container");
	public static final String CDMI_TYPE_OBJECT = get("cdmi.type.object");
	public static final String CDMI_VERSION_TAG = get("cdmi.version.tag");
	public static final String CDMI_MIMETYPE_TAG = get("cdmi.mimetype.tag");
	public static final String CDMI_METADATA_TAG = get("cdmi.metadata.tag");
	
	public static final String[] TRIGGER_TYPES = {"createDObj","deleteDObj", "putMD", "getDObj"};
}
