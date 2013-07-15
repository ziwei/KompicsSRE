package events;

import se.sics.kompics.Event;

public class SlOperation extends Event {
	private String storletId;

	public SlOperation(String slID){
		this.setStorletId(slID);
	}

	public String getStorletId() {
		return storletId;
	}
	public void setStorletId(String storletId) {
		this.storletId = storletId;
	}
}
