package events;

import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

public class MyTimeout extends Timeout {
	private String slID;
	private String handler;
	public MyTimeout(ScheduleTimeout request, String id, String h) {
		super(request);
		setSlID(id);
		setHandler(h);
		// TODO Auto-generated constructor stub
	}
	public String getSlID() {
		return slID;
	}
	public void setSlID(String slID) {
		this.slID = slID;
	}
	public String getHandler() {
		return handler;
	}
	public void setHandler(String handler) {
		this.handler = handler;
	}

}
