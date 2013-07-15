package fakeStorletInterface;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
//import javax.ws.rs.core.MediaType;

//import org.antlr.runtime.RecognitionException;
//import org.apache.commons.codec.binary.Base64;

//import eu.visioncloud.cci.client.CdmiRestClient;
//import eu.visioncloud.cci.client.ClientInterface;
//import eu.visioncloud.cci.client.ContentCentricException;
//import eu.visioncloud.storlet.compilation.JavaMemFileManager;
//import eu.visioncloud.storlet.compilation.SourceMemFileObject;
//import eu.visioncloud.storlet.loading.ClassLoadingObjectInputStream;
//import eu.visioncloud.storlet.loading.JarInJarClassLoader;

public class Utils {

	// random generator based on
	// "The Answer to the Ultimate Question of Life, the Universe, and Everything"
	private static Random random = new Random(42);

	public static ByteArrayOutputStream createJar(String inputLibPath,
			String inputBinPath) throws IOException {
		// creating manifest and jar file
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,
				"1.0");

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		JarOutputStream target = new JarOutputStream(bout, manifest);

		// add all jar-libaries in the lib folder to the jar
		File libs = new File(inputLibPath);
		if (!libs.exists() || !libs.isDirectory())
			throw new IOException(
					"Input lib folder path does not exist, or is named incorrectly: "
							+ inputLibPath);
		addJarEntry(libs, target, libs);

		// add all code files to the jar
		File bin = new File(inputBinPath);
		if (!(bin.exists() && bin.isDirectory()))
			throw new IOException("Input bin folder path does not exist: "
					+ inputBinPath);
		addJarEntry(bin, target, bin);
		target.close();

		return bout;
	}

	/**
	 * @source file to be added
	 * @target jar stream to add file to basePath prefix of the file that has to
	 *         be extracted
	 **/
	private static void addJarEntry(File source, JarOutputStream target,
			File basePath) throws IOException {
		BufferedInputStream in = null;
		try {
			String sourceName = source.getAbsolutePath().substring(
					basePath.getAbsolutePath().length());
			// known bug library can't deal with \ separator
			sourceName = sourceName.replace("\\", "/");

			if (sourceName.startsWith("/"))
				sourceName = sourceName.substring(1);

			// write directory
			if (source.isDirectory()) {
				if (!sourceName.isEmpty()) {
					// known bug directories have to end on /
					if (!sourceName.endsWith("/"))
						sourceName += "/";

					// create entry
					JarEntry entry = new JarEntry(sourceName);
					entry.setTime(source.lastModified());
					target.putNextEntry(entry);
					target.closeEntry();

				}
				// recursion to write sub files
				for (File nestedFile : source.listFiles()) {
					addJarEntry(nestedFile, target, basePath);
				}
			}
			// write files
			else {
				// create entry
				JarEntry entry = new JarEntry(sourceName);
				entry.setTime(source.lastModified());
				target.putNextEntry(entry);
				// write file
				in = new BufferedInputStream(new FileInputStream(source));
				byte[] buffer = new byte[1024];
				while (true) {
					int count = in.read(buffer);
					if (count == -1)
						break;
					target.write(buffer, 0, count);
				}
				target.closeEntry();
			}
		} finally {
			if (in != null)
				in.close();
		}
	}

	public static ByteArrayOutputStream createSLContent(Properties params,
			String authToken) throws IOException {
		return createSLContent(params, null, null, authToken);
	}


	public static ByteArrayOutputStream createSLContent(Properties params,
			Properties constraints, byte[] jar, String authToken)
			throws IOException {
		// creating manifest and jar file
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,
				"1.0");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JarOutputStream target = new JarOutputStream(baos, manifest);

		JarEntry entry;

		// create state entry
		entry = new JarEntry(SPEConstants.STORLET_PARAMS_FILENAME);
		entry.setTime(System.currentTimeMillis());
		target.putNextEntry(entry);
		params.store(target, "");
		target.closeEntry();

		// add autentication token
		entry = new JarEntry(SPEConstants.STORLET_AUTH_FILENAME);
		entry.setTime(System.currentTimeMillis());
		target.putNextEntry(entry);
		target.write(authToken.getBytes());
		target.closeEntry();

		// create constraints entry
		if (null != constraints) {
			entry = new JarEntry(SPEConstants.STORLET_CONSTRAINTS_FILENAME);
			entry.setTime(System.currentTimeMillis());
			target.putNextEntry(entry);
			constraints.store(target, "");
			target.closeEntry();
		}

		// create jar entry
		if (null != jar) {
			entry = new JarEntry(SPEConstants.STORLET_CODE_FILENAME);
			entry.setTime(System.currentTimeMillis());
			target.putNextEntry(entry);
			target.write(jar);
			target.closeEntry();
		}

		target.close();

		return baos;
	}

	
	public static File extractJarContents(InputStream is, String targetPath)
			throws IOException {
		JarInputStream jarin = new JarInputStream(is);
		JarEntry jarEntry = jarin.getNextJarEntry();
		while (jarEntry != null) {
			extractJarContentEntry(jarin, jarEntry, targetPath);
			jarEntry = jarin.getNextJarEntry();
		}

		return new File(targetPath);
	}

	private static void extractJarContentEntry(JarInputStream jin, JarEntry e,
			String dir) throws IOException {
		File f = new File(dir + File.separatorChar
				+ e.getName().replace('/', File.separatorChar));
		if (e.isDirectory()) {
			if (!f.exists() && !f.mkdirs() || !f.isDirectory()) {
				// localize this mesg.
				throw new IOException(f + ": could not create directory");
			}
		} else {
			if (f.getParent() != null) {
				File d = new File(f.getParent());
				if (!d.exists() && !d.mkdirs() || !d.isDirectory()) {
					// localize this mesg.
					throw new IOException(d + ": could not create directory");
				}
			}
			OutputStream os = new FileOutputStream(f);
			byte[] b = new byte[512];
			int len;
			while ((len = jin.read(b, 0, b.length)) != -1) {
				os.write(b, 0, len);
			}
			jin.closeEntry();
			os.close();
		}
	}

	/**
	 * Inject Storlet Definition
	 * 
	 * @throws ContentCentricException
	 */
	// TODO no JUnit test
	public static void injectStorletDefinition(
			ObjIdentifier definitionObjectId, byte[] content,
			String storletDescription, AuthToken authToken,
			String storageServiceUrl) throws ContentCentricException {
		// Instantiate Object Service Client
		ClientInterface cci = new CdmiRestClient(storageServiceUrl,
				authToken.getAuthenticationString());

		// Define Object Metadata
		// TODO Multipart vs Regular, test
		// Map<String, String> objectMetadata = new HashMap<String, String>();
		Map<String, Object> objectMetadata = new HashMap<String, Object>();
		objectMetadata.put(SPEConstants.STORLET_TAG_TEMPLATE_DESCRIPTION,
				null == storletDescription ? "Unknown" : storletDescription);
		objectMetadata
				.put(SPEConstants.CDMI_MIMETYPE_TAG, MediaType.TEXT_PLAIN);

		// Create Object (Content, Triggers, Metadata Tags)
		// cci.createObjectWithMetadata(definitionObjectId.getTenantName(),
		// definitionObjectId.getContainerName(), definitionObjectId
		// .getObjectName(), new String(encodeByteArray(content)),
		// objectMetadata);
		// TODO Multipart vs Regular, test
		cci.createObjectWithMultipartMime(definitionObjectId.getTenantName(),
				definitionObjectId.getContainerName(),
				definitionObjectId.getObjectName(), objectMetadata,
				new ByteArrayInputStream(encodeByteArray(content)));
	}

	/**
	 * Inject Storlet
	 * 
	 * @throws ContentCentricException
	 * @throws StorletException
	 */
	// TODO no JUnit test
	public static void injectStorlet(ObjIdentifier definitionObjectId,
			ObjIdentifier storletObjectId, byte[] storletContent,
			String storletClassName, String triggersString,
			AuthToken authToken, String storageServiceUrl)
			throws ContentCentricException, StorletException {
		// Check trigger definition syntax
		Trigger.createTriggers(triggersString);

		// Instantiate Object Service Client
		ClientInterface cci = new CdmiRestClient(storageServiceUrl,authToken.getAuthenticationString());

		// Define Object Metadata
		// TODO Multipart vs Regular, test
		// Map<String, String> objectMetadata = new HashMap<String, String>();
		Map<String, Object> objectMetadata = new HashMap<String, Object>();
		objectMetadata.put(SPEConstants.STORLET_TAG_CODEOBJ,
				definitionObjectId.getObjID());
		objectMetadata.put(SPEConstants.STORLET_TAG_CODETYPE, storletClassName);
		objectMetadata.put(SPEConstants.STORLET_TAG_TRIGGERS, triggersString);
		objectMetadata
				.put(SPEConstants.CDMI_MIMETYPE_TAG, MediaType.TEXT_PLAIN);

		// Build Object Contents
		String storletContentString = new String(
				Utils.encodeByteArray(storletContent));

		// Create Object (Content, Triggers, Metadata Tags)
		// TODO Multipart vs Regular, test
		// cci.createObjectWithMetadata(storletObjectId.getTenantName(),
		// storletObjectId.getContainerName(),
		// storletObjectId.getObjectName(), storletContentString,
		// objectMetadata);
		cci.createObjectWithMultipartMime(storletObjectId.getTenantName(),
				storletObjectId.getContainerName(),
				storletObjectId.getObjectName(), objectMetadata,
				new ByteArrayInputStream(storletContentString.getBytes()));
	}

	// Dynamic Loading

	public static Storlet loadClassAndMarshalEncodedStorletFromFile(
			String jarPath, String serializedObjectPath)
			throws StorletException, IOException, ClassNotFoundException {
		File serializedStorletFile = new File(serializedObjectPath);
		byte[] encodedStorlet = Utils.fileToByteArray(serializedStorletFile);
		return loadClassAndMarshalEncodedStorlet(jarPath, encodedStorlet);
	}

	public static Storlet loadClassAndMarshalEncodedStorlet(String jarPath,
			byte[] encodedStorlet) throws StorletException, IOException,
			ClassNotFoundException {
		URL url = new File(jarPath).toURI().toURL();
		URL[] urls = new URL[] { url };
		URLClassLoader loader = new URLClassLoader(urls,
				Storlet.class.getClassLoader());
		byte[] decodedStorlet = decodeByteArray(encodedStorlet);
		ByteArrayInputStream bais = new ByteArrayInputStream(decodedStorlet);
		ObjectInputStream oin = new ClassLoadingObjectInputStream(bais, loader);
		Storlet storlet = (Storlet) oin.readObject();
		oin.close();
		return storlet;
	}

	// Serializing

	// public static void serializeStorletAndTriggersToFile(
	// Class<? extends Storlet> storletClass,
	// String serializedStorletPath, String serializedTriggersPath)
	// throws InstantiationException, IllegalAccessException,
	// StorletException, IOException {
	// // replace nulls, this may cause errors
	// Storlet storlet = Storlet.createStorlet(storletClass, null, null);
	//
	// serializeAndEncodeObjectToFile(storlet, serializedStorletPath);
	// // old
	// // serializeAndEncodeObjectToFile(storlet.getTriggers(),
	// // serializedTriggersPath);
	// // new, but needs more thought, e.g. where does the triggersString
	// // come from?
	// this string should come from somewhere... a function parameter?;
	// String triggersString = "";
	// byte[] encodedTriggers = encodeByteArray(triggersString.getBytes());
	// bytesToFile(encodedTriggers,serializedTriggersPath);
	// }

	public static File serializeAndEncodeObjectToFile(Object object,
			String serializedObjectPath) throws InstantiationException,
			IllegalAccessException, IOException, StorletException {
		byte[] encodedObject = serializeAndEncodeObject(object);
		return byteArrayToFile(encodedObject, serializedObjectPath);
	}

	public static File byteArrayToFile(byte[] bytes, String path)
			throws IOException {
		File f = new File(path);
		if (f.exists()) {
			Utils.deleteFileOrDirectory(f);
			f.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(bytes);
		fos.flush();
		fos.close();
		return f;
	}

	public static byte[] serializeAndEncodeObject(Object object)
			throws IOException {
		byte[] serializedObject = serializeObject(object);
		return encodeByteArray(serializedObject);
	}

	public static byte[] serializeObject(Object object) throws IOException {
		ByteArrayOutputStream boutObject = new ByteArrayOutputStream();
		ObjectOutput outObject = new ObjectOutputStream(boutObject);
		outObject.writeObject(object);
		outObject.close();
		return boutObject.toByteArray();
	}

	// Marshalling

	public static Object marshalAndDecodeObjectFromFile(
			String encodedObjectPath, Class<?> objectType) throws IOException,
			ClassNotFoundException {
		File serializedObjectFile = new File(encodedObjectPath);
		byte[] encodedObject = Utils.fileToByteArray(serializedObjectFile);
		return marshalAndDecodeObject(encodedObject, objectType);
	}

	public static Object marshalAndDecodeObjectFromString(String encodedObject,
			Class<?> objectType) throws IOException, ClassNotFoundException {
		return marshalAndDecodeObject(encodedObject.getBytes(), objectType);
	}

	public static Object marshalAndDecodeObject(byte[] encodedObject,
			Class<?> objectType) throws IOException, ClassNotFoundException {
		byte[] decodedObject = decodeByteArray(encodedObject);
		return marshalObject(decodedObject, objectType);
	}

	public static Object marshalObject(byte[] decodedObject, Class<?> objectType)
			throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(decodedObject);
		ObjectInputStream oin = new ObjectInputStream(bais);
		Object object = objectType.cast(oin.readObject());
		oin.close();
		return object;
	}

	public static Set<Trigger> marshalAndDecodeTriggers(
			String encodedTriggersString) throws IOException,
			ClassNotFoundException {
		byte[] decodedObject = decodeByteArray(encodedTriggersString.getBytes());
		ByteArrayInputStream bais = new ByteArrayInputStream(decodedObject);
		ObjectInputStream oin = new ObjectInputStream(bais);
		@SuppressWarnings("unchecked")
		Set<Trigger> triggers = (Set<Trigger>) oin.readObject();
		oin.close();
		return triggers;
	}

	// Byte Array

	public static byte[] fileToEncodedByteArray(File file) throws IOException {
		return encodeByteArray(fileToByteArray(file));
	}

	public static byte[] fileToByteArray(File file) throws IOException {
		return inputStreamToByteArray(new FileInputStream(file));
	}

	public static InputStream decodeStream(InputStream encodedStream)
			throws IOException {
		byte[] encodedBytes = Utils.inputStreamToByteArray(encodedStream);
		byte[] decodedBytes = Utils.decodeByteArray(encodedBytes);
		return new ByteArrayInputStream(decodedBytes);
	}

	public static byte[] inputStreamToByteArray(InputStream inputStream)
			throws IOException {
		byte[] result;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int numberBytesRead;
		byte[] data = new byte[16384];

		while ((numberBytesRead = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, numberBytesRead);
		}

		buffer.flush();
		result = buffer.toByteArray();
		inputStream.close();
		return result;
	}

	public static byte[] concatByteArrays(byte[] first, byte[] second) {
		byte[] third = new byte[first.length + second.length];

		int iThird = 0;
		for (int i = 0; i < first.length; i++) {
			third[iThird] = first[i];
			iThird++;
		}

		for (int i = 0; i < second.length; i++) {
			third[iThird] = second[i];
			iThird++;
		}

		return third;
	}

	// Encode Decode

	public static byte[] encodeByteArray(byte[] byteArray) {
		return Base64.encodeBase64(byteArray);
	}

	public static byte[] decodeByteArray(byte[] encodedByteArray) {
		return Base64.decodeBase64(encodedByteArray);
	}

	// Files & Folders

	public static String createFilePath(String homeDirectoryPath,
			String containerName, String objectName) {
		return homeDirectoryPath + File.separator + containerName + ":"
				+ objectName + ":" + random.nextLong();
	}

	public static boolean deleteFileOrDirectory(File fileOrDirectory) {
		if (anyAreNull(fileOrDirectory))
			throw new NullPointerException("Parameter can not be null");

		if (false == fileOrDirectory.exists())
			return true;

		// delete sub files and folders
		if (fileOrDirectory.isDirectory())
			for (File file : fileOrDirectory.listFiles())
				if (false == deleteFileOrDirectory(file))
					return false;

		// directory now empty, delete it
		return fileOrDirectory.delete();
	}

	// Checks

	public static boolean anyAreNull(Object... objects) {
		for (Object object : objects)
			if (null == object)
				return true;
		return false;
	}

	public static boolean equalityGeneric(Object object1, Object object2) {
		if (object1 == object2)
			return true;

		// one is null and the other is not
		if (false == ((null == object1) == (null == object2)))
			return false;
		if (false == ((null == object1) || (object1.equals(object2))))
			return false;
		return true;
	}

	public static boolean equalityStringMaps(Map<String, String> map1,
			Map<String, String> map2) {
		if (map1 == map2)
			return true;

		// one is null and the other is not
		if (false == ((null == map1) == (null == map2)))
			return false;

		if (false == (null == map1)) {

			// TODO add again if situation with "cdmi_" changes
			// if (false == ((map1.size() == map2.size())))
			// return false;

			for (String key : map1.keySet()) {
				if (true == key.startsWith("cdmi_"))
					continue;
				String val1 = map1.get(key);
				String val2 = map2.get(key);

				// one is null and the other is not
				if (false == ((null == val1) == (null == val2)))
					return false;
				if (false == ((null == val1) || val1.equals(val2)))
					return false;
			}

			for (String key : map2.keySet()) {
				if (true == key.startsWith("cdmi_"))
					continue;
				String val1 = map1.get(key);
				String val2 = map2.get(key);

				// one is null and the other is not
				if (false == ((null == val1) == (null == val2)))
					return false;
				if (false == ((null == val1) || val1.equals(val2)))
					return false;
			}
		}

		return true;
	}

	public static boolean equalityGenericMaps(Map<?, ?> map1, Map<?, ?> map2) {
		if (map1 == map2)
			return true;

		// one is null and the other is not
		if (false == ((null == map1) == (null == map2)))
			return false;

		if (false == (null == map1)) {

			if (false == ((map1.size() == map2.size())))
				return false;

			for (Object key : map1.keySet()) {
				Object val1 = map1.get(key);
				Object val2 = map2.get(key);

				// one is null and the other is not
				if (false == ((null == val1) == (null == val2)))
					return false;
				if (false == ((null == val1) || val1.equals(val2)))
					return false;
			}

			for (Object key : map2.keySet()) {
				Object val1 = map1.get(key);
				Object val2 = map2.get(key);

				// one is null and the other is not
				if (false == ((null == val1) == (null == val2)))
					return false;
				if (false == ((null == val1) || val1.equals(val2)))
					return false;
			}
		}

		return true;
	}

	public static boolean equalityArrays(byte[] array1, byte[] array2) {
		return Arrays.equals(array1, array2);
	}

	public static boolean equalityArrays(Object[] array1, Object[] array2) {
		return Arrays.equals(array1, array2);
	}

	public static boolean equalityBooleans(boolean boolean1, boolean boolean2) {
		if (false == (boolean1 == boolean2))
			return false;
		return true;
	}

	public static boolean equalityInputStreams(InputStream inputStream1,
			InputStream inputStream2) throws IOException {

		if (inputStream1 == inputStream2)
			return true;

		// one is null and the other is not
		if (false == ((null == inputStream1) == (null == inputStream2)))
			return false;

		if (false == (null == inputStream1)) {
			do {
				int byte1;
				byte1 = inputStream1.read();
				int byte2 = inputStream2.read();

				if (false == (byte1 == byte2))
					return false;

				if (-1 == byte1)
					break;
			} while (true);
		}

		return true;
	}

	// TODO equalityJars

	public static Class<? extends Storlet> loadStorletClass(File jarFile,
			String className) throws FileNotFoundException, IOException,
			URISyntaxException, ClassNotFoundException {
		URL jarFileUrl = jarFile.toURI().toURL();

		// URLClassLoader loader = new URLClassLoader(new URL[] { jarFileUrl },
		// ClassLoader.getSystemClassLoader());

		// TODO find out why getSystemClassLoader() does NOT work
		// JarInJarClassLoader loader = new JarInJarClassLoader(jarFileUrl,
		// ClassLoader.getSystemClassLoader());
		JarInJarClassLoader loader = new JarInJarClassLoader(jarFileUrl,
				Storlet.class.getClassLoader());
		loader.searchAndAddNestedJars();
		return (Class<? extends Storlet>) Class.forName(className, false,
				loader);
	}

	public static List<byte[]> byteCodeAsBytes(String sourceCode,
			String className) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		List<JavaFileObject> srcFiles = new ArrayList<JavaFileObject>();
		srcFiles.add(new SourceMemFileObject(className, sourceCode));

		JavaFileManager fileManager = new JavaMemFileManager();
		compiler.getTask(null, fileManager, null, null, null, srcFiles).call();

		List<byte[]> compiledClasses = ((JavaMemFileManager) fileManager)
				.getClassBytes(className);

		return compiledClasses;
	}

	public static void byteCodeToFile(String sourceCode, String className,
			File outDir) throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		List<JavaFileObject> srcFiles = new ArrayList<JavaFileObject>();
		srcFiles.add(new SourceMemFileObject(className, sourceCode));

		JavaFileManager fileManager = new JavaMemFileManager();
		compiler.getTask(null, fileManager, null, null, null, srcFiles).call();

		((JavaMemFileManager) fileManager).writeClassBytes(className, outDir);
	}
	
	public static String  jsonUnEscape(String string){
	      
        string = string.replace("\\b", "\b");
        string = string.replace("\\t", "\t");
        string = string.replace("\\n", "\n");
        string = string.replace("\\f", "\f");
        string = string.replace("\\r", "\r");
  
		char         c1 = 0;
		int          i;
		int          len = string.length();
		StringBuffer sb = new StringBuffer(len);
		boolean removed = false;
		for (i = 0; i < len; i += 1) {
			c1 = string.charAt(i);
			if(c1=='\\' && !removed){
				removed = true;
			}else{
				sb.append(c1);
				removed = false;
			}
		}
		return sb.toString();
}
}
