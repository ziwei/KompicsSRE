package events;

import se.sics.kompics.Init;

public class StorletInit extends Init {
	// private URL url;
	// private String storletName;
	private String slID;
	public StorletInit(String id) {
		this.setSlID(id);
	
	}
	public String getSlID() {
		return slID;
	}
	public void setSlID(String slID) {
		this.slID = slID;
	}
	
}
