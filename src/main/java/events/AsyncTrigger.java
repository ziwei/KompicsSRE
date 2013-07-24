package events;

import eu.visioncloud.storlet.common.EventModel;

public class AsyncTrigger extends SlOperation {
	private String slID;
	private String handlerId;
	private EventModel em;

	public AsyncTrigger(String slID, String h, EventModel e) {
		this.setSlID(slID);
		this.setHandlerId(h);
		this.setEventModel(e);
		// TODO Auto-generated constructor stub
	}

	public String getHandlerId() {
		return handlerId;
	}

	public void setHandlerId(String handlerId) {
		this.handlerId = handlerId;
	}

	public EventModel getEventModel() {
		return em;
	}

	public void setEventModel(EventModel em) {
		this.em = em;
	}

	public String getSlID() {
		return slID;
	}

	public void setSlID(String slID) {
		this.slID = slID;
	}

}
