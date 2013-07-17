package events;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import eu.visioncloud.ExampleStorlet;
import eu.visioncloud.storlet.common.Storlet;

import se.sics.kompics.Init;
import util.JarInJarClassLoader;

public class StorletInit extends Init {
	private Class<? extends Storlet> storletType;
	private File workingDir;
	private String contentCentricUrl;
	private Socket socket;
	//private URL url;
	//private String storletName;
	
	public StorletInit(){
		
	}
	
	public StorletInit(Socket socket){
		this.setSocket(socket);
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

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

}
