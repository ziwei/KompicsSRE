package events;

public class SyncTrigger extends SlOperation {
	private String content;
	public SyncTrigger(String slID, String c) {
		super(slID);
		this.content = c;
		// TODO Auto-generated constructor stub
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
