package deprecated;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import util.JarInJarClassLoader;

import eu.visioncloud.storlet.common.Storlet;
import eu.visioncloud.storlet.common.StorletException;

public class ClassLoadingThread extends Thread {
	private Storlet storlet;
	private File dir1;
	private String contentCentricUrl;
	public ClassLoadingThread(String name, File dir, String ccl){
		super(name);
		dir1 = dir;
		contentCentricUrl = ccl;
	}
	@SuppressWarnings("unchecked")
	public void run(){
		JarInJarClassLoader jijLoader = null;
		try {
			jijLoader = new JarInJarClassLoader(new URL(
					"file:///home/ziwei/workspace/KompicsSRE/nest.jar"), Thread
					.currentThread().getContextClassLoader());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			jijLoader.searchAndAddNestedJars();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Class<? extends Storlet> storletClass = null;
		try {
			storletClass = (Class<? extends Storlet>) jijLoader
					.findClass("storlets4testing.HelloFileStorlet");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// @SuppressWarnings("unchecked")
		// Class<? extends Storlet> innerClass = (Class<? extends Storlet>)
		// jijLoader.findClass("storlets4testing.HelloFileStorlet$1");
		System.out.println("class loaded");
		// System.setSecurityManager(null);
		this.setContextClassLoader(jijLoader);
		try {
			setStorlet(Storlet.createStorlet(storletClass, dir1, contentCentricUrl));
		} catch (StorletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Storlet getStorlet() {
		return storlet;
	}
	public void setStorlet(Storlet storlet) {
		this.storlet = storlet;
	}
}
