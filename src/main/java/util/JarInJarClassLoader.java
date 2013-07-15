package util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarInJarClassLoader extends URLClassLoader {

	// List all jars (ordered)
	private List<String> nestedJarPathList = new ArrayList<String>();
	// Relation: JARs <-> Manifest
	private Map<String, Manifest> manifests = new HashMap<String, Manifest>();

	// URL of surrounding Container-JAR
	private URL outerJarURL = null;

	// Index-Map
	private Map<String, String> indexMap = new HashMap<String, String>();

	public JarInJarClassLoader(URL url) {
		super(new URL[] { url });
		this.outerJarURL = url;
	}

	public JarInJarClassLoader(URL url, ClassLoader parent) {
		super(new URL[] { url }, parent);
		this.outerJarURL = url;
	}

	public void searchAndAddNestedJars() throws FileNotFoundException,
			IOException, URISyntaxException {
		JarInputStream jarInputStream = new JarInputStream(new FileInputStream(
				new File(outerJarURL.toURI())));
		JarEntry jarEntry = jarInputStream.getNextJarEntry();
		while (jarEntry != null) {
			String pathName = jarEntry.getName();
			// TODO uncomment for debugging
			// System.out.println("pathname = " + pathName);
			if (pathName.endsWith(".jar")) {
				if (pathName.startsWith("/"))
					pathName = pathName.substring(1);
				addNestedJarPath(pathName);
			}

			jarEntry = jarInputStream.getNextJarEntry();
		}
	}

	/**
	 * Add a embedded JAR and index it.
	 * 
	 * @param nestedJarPath
	 *            relative path for the jar in the jar
	 * @throws IOException
	 */
	public void addNestedJarPath(String nestedJarPath) throws IOException {
		if (super.getResource(nestedJarPath) != null) {
			this.nestedJarPathList.add(nestedJarPath);
			Manifest manifest = null;
			try {
				manifest = new Manifest(
						this.getResourceAsStream("META-INF/MANIFEST.MF"));
			} catch (IOException e) {
				manifest = new Manifest();
			}
			this.manifests.put(nestedJarPath, manifest);
			this.indexNestedJar(nestedJarPath);
		} else
			throw new RuntimeException("Nested jar-file not found! "
					+ nestedJarPath);
	}

	/**
	 * remove nested jar and remove jar from index
	 * 
	 * @param nestedJarPath
	 *            relative path for the jar in the jar
	 * @throws IOException
	 */
	public void removeNestedJarPath(String nestedJarPath) throws IOException {
		if (this.nestedJarPathList.contains(nestedJarPath)) {
			int index = this.nestedJarPathList.indexOf(nestedJarPath);
			this.nestedJarPathList.remove(index);
			this.manifests.remove(nestedJarPath);
			Object[] keys = this.indexMap.keySet().toArray();
			for (int i = index; i < keys.length; i++)
				if (nestedJarPath.equals(this.indexMap.get(keys[i])))
					this.indexMap.remove(keys[i]);
			for (int i = 0; i < this.nestedJarPathList.size(); i++)
				this.indexNestedJar(nestedJarPathList.get(i));
		} else
			throw new RuntimeException("Nested jar-file not found!");
	}

	/**
	 * index (path -> embedded jar).
	 * 
	 * @param nestedJarPath
	 *            relative path for the jar in the jar
	 * @throws IOException
	 */
	private void indexNestedJar(String nestedJarPath) throws IOException {
		String path = "";
		ZipInputStream zipInputStream = new ZipInputStream(
				super.getResourceAsStream(nestedJarPath));
		ZipEntry zipEntry = null;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			path = zipEntry.getName();
			if (!this.indexMap.containsKey(path)) {
				this.indexMap.put(path, nestedJarPath);
			}
		}
	}

	/**
	 * if <code>super.findResource(name);</code> failes check index
	 * 
	 * @param name
	 *            rource
	 * @return a URL (internal jar:jar cant be handelt by java)
	 */
	public URL findResource(final String name) {
		URL res = super.findResource(name);
		if (res == null) {
			String _nestedJarPath = this.indexMap.get(name);
			if (_nestedJarPath != null) {
				try {
					res = new URL("jar:jar:" + this.outerJarURL + "!/"
							+ _nestedJarPath + "!/" + name);
				} catch (MalformedURLException e) {
					res = null;
				}
			}
		}
		return res;
	}

	/**
	 * internal method to find class
	 * 
	 * @param name
	 *            class
	 * @return class
	 * @throws ClassNotFoundException
	 */
	protected Class findClassInternal(String name)
			throws ClassNotFoundException {
		String resPath = name.replaceAll("\\.", "/").concat(".class");
		InputStream bCStream = getResourceAsStream(resPath);
		if (bCStream != null) {
			//System.out.println(resPath);
			String nestedJarPath = this.indexMap.get(resPath);
			// Package anlegen bzw. prüfen...
			int index = name.lastIndexOf('.');
			//System.out.println(index);
			if (index != -1) {
				String pkgname = name.substring(0, index);
				//System.out.println(pkgname);
				Package packageID = this.getPackage(pkgname);
				if (packageID == null) {
					URL packageUrl = getResource(pkgname.replaceAll("\\.", "/")
							.concat("/"));
					Manifest manifest = this.manifests.get(nestedJarPath);
					//System.out.println(manifest);
					if (manifest != null) {
						definePackage(pkgname, manifest, packageUrl);
					} else {
						definePackage(pkgname, null, null, null, null, null,
								null, null);
					}
				}
			}
			// create class...
			byte[] data = new byte[1024];
			int read = 0;

			byte[] byteCode = null;
			try {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				while ((read = bCStream.read(data, 0, 1024)) != -1) {
					byteArrayOutputStream.write(data, 0, read);
				}
				byteCode = byteArrayOutputStream.toByteArray();
				Class clazz = defineClass(name, byteCode, 0, byteCode.length);
				this.resolveClass(clazz);
				return clazz;
			} catch (IOException e) {
				throw new ClassNotFoundException(name, e.getCause());
			}
		} else {
			throw new ClassNotFoundException(name);
		}
	}

	/**
	 * @override
	 * @param name
	 *            class
	 * @return class
	 * @throws ClassNotFoundException
	 */
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			return super.findClass(name);
		} catch (ClassNotFoundException e) {
			try {
				System.out.println(name);
				return this.findClassInternal(name);
			} catch (ClassNotFoundException ce) {
				throw new ClassNotFoundException(name, ce.getCause());
			}
		}
	}

	/**
	 * resource stream using the index to find embedded jar
	 * 
	 * @param name
	 *            path to resource
	 * @return InputStream if successful or <code>null</code>
	 */
	public InputStream getResourceAsStream(String name) {
		InputStream res = super.getResourceAsStream(name);
		// TODO uncomment for debugging
		// System.out.println("name: " + name);
		// System.out.println("res: " + res);
		if (res == null) {
			String nestedJarPath = this.indexMap.get(name);
			if (nestedJarPath != null) {
				try {
					ZipInputStream zipInputStream = new ZipInputStream(
							super.getResourceAsStream(nestedJarPath));
					ZipEntry zipEntry = null;
					while ((zipEntry = zipInputStream.getNextEntry()) != null)
						if (zipEntry.getName().equals(name))
							return zipInputStream;
				} catch (IOException e) {
					res = null;
				}
			}
		}
		return res;
	}
}