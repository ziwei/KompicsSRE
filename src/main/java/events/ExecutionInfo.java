package events;

import se.sics.kompics.Event;

public class ExecutionInfo extends Event {
	String status;
	String slID;
	String handler;
	long timeConstraint = -1;
	public ExecutionInfo(String status, String id, String h){
		this.status = status;
		slID = id;
		handler = h;
	}
	public ExecutionInfo(String status, String id, String h, long time){
		this.status = status;
		slID = id;
		handler = h;
		timeConstraint = time;
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
	public long getTimeConstraint(){
		return timeConstraint;
	}
}
