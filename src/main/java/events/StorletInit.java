package events;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import fakeStorletInterface.Storlet;
import se.sics.kompics.Init;
import util.JarInJarClassLoader;

public class StorletInit extends Init {
	private Class<? extends Storlet> storletType;
	private File workingDir;
	private String contentCentricUrl;
	//private URL url;
	//private String storletName;
	
	public StorletInit(){
		//storletType = getStorletType(url, storletName);//url and name form jason body
	}
	
	public Class<? extends Storlet> getStorletType() {
		return storletType;
	}

	public void setStorletType(Class<? extends Storlet> storletType) {
		this.storletType = storletType;
	}

	public File getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	public String getContentCentricUrl() {
		return contentCentricUrl;
	}

	public void setContentCentricUrl(String contentCentricUrl) {
		this.contentCentricUrl = contentCentricUrl;
	}

}
