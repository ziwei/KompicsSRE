package events;

import se.sics.kompics.Event;

public class ExecutionInfo extends Event {
	String status;
	String slID;
	String handler;
	public ExecutionInfo(String status, String id, String h){
		this.status = status;
		slID = id;
		handler = h;
	}
	
	public String getStatus(){
		return status;
	}
	public String getSlID(){
		return slID;
	}
	public String getHandler(){
		return handler;
	}
}
