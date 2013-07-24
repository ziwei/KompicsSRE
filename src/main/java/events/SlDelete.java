package events;

public class SlDelete extends SlOperation {
	private String slID;
	public SlDelete(String slID) {
		this.setSlID(slID);
		// TODO Auto-generated constructor stub
	}
	public String getSlID() {
		return slID;
	}
	public void setSlID(String slID) {
		this.slID = slID;
	}

}
