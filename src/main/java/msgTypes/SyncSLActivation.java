/*
Container for sync trigger event body, same as old SRE
 */
package msgTypes;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class SyncSLActivation implements Serializable {
	private static final long serialVersionUID = 1L;
	private String storlet_name;
	private int port;
	private String parameter;
	
	public String getStorlet_name() {
		return storlet_name;
	}
	public void setStorlet_name(String storlet_name) {
		this.storlet_name = storlet_name;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	
	@Override
	public String toString() {
		return storlet_name + " "+ port + " " + parameter;
	}

}