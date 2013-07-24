package events;

import msgTypes.SyncSLActivation;

public class SyncTrigger extends SlOperation {
	private SyncSLActivation syncAct;

	public SyncTrigger(SyncSLActivation syncAct) {
		this.setSyncAct(syncAct);
		// TODO Auto-generated constructor stub
	}

	public SyncSLActivation getSyncAct() {
		return syncAct;
	}

	public void setSyncAct(SyncSLActivation syncAct) {
		this.syncAct = syncAct;
	}

}
