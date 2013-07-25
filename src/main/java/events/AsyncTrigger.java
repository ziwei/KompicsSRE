package events;

import eu.visioncloud.storlet.common.EventModel;

public class AsyncTrigger extends SlOperation {
	private String slID;
	private String handlerId;
	private EventModel em;
	private String actId;

	public AsyncTrigger(String slID, String handler, EventModel eventModel, String activationId) {
		this.setSlID(slID);
		this.setHandlerId(handler);
		this.setEventModel(eventModel);
		this.setActId(activationId);
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

	public String getActId() {
		return actId;
	}

	public void setActId(String actId) {
		this.actId = actId;
	}

}
