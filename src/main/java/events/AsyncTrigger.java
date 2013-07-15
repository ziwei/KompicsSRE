package events;

import java.util.HashMap;
import java.util.Map;

public class AsyncTrigger extends SlOperation {
	private String handler;
	private String content;
	private Map<String, String> oldMeta;
	private Map<String, String> newMeta;
	private boolean flag;
	public AsyncTrigger(String slID, String h, String c) {
		super(slID);
		this.setHandler(h);
		this.setContent(c);
		// TODO Auto-generated constructor stub
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getHandler() {
		return handler;
	}
	public void setHandler(String handler) {
		this.handler = handler;
	}
	private Map<String, String> String2Metadata(){
		Map m = new HashMap();
		m.put(content, content);///////////////
		return m;
	}


	public Map<String, String> getOldMeta() {
		return oldMeta;
	}

	public void setOldMeta(Map<String, String> oldMeta) {
		this.oldMeta = oldMeta;
	}

	public Map<String, String> getNewMeta() {
		return newMeta;
	}

	public void setNewMeta(Map<String, String> newMeta) {
		this.newMeta = newMeta;
	}

	public boolean getFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
}
