/*
port for ExecutionInfo events, for SRE detect execution start and finish
 */
package porttypes;

import events.ExecutionInfo;
import se.sics.kompics.PortType;

public class ExeStatus extends PortType {
	{
		positive(ExecutionInfo.class);
		negative(ExecutionInfo.class);
	}
}
