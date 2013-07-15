package porttypes;

import events.SlOperation;
import se.sics.kompics.PortType;

public class SRESl extends PortType {
	{
		positive(SlOperation.class);
		negative(SlOperation.class);
	}
}
