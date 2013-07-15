package fakeStorletInterface;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

//import org.codehaus.jackson.annotate.JsonIgnore;

@XmlRootElement
public class EventModel implements Serializable {
	private static final long serialVersionUID = 1L;

	// this is how a message looks like
	// {
	// "containerName":"containerName",
	// "metadataDiff":{
	// "key3":"value3",
	// "key2":"value2"
	// },
	// "objectName":"objName",
	// "eventType":"createDObj",
	// "position":"9.148.42.99",
	// "rFlag":false,
	// "metadata":{
	// "key2":"value4",
	// "key1":"value1"
	// }
	// "userName";"value"
	// }
	
	private Map<String, String> oldMetadata;
	private Map<String, String> newMetadata;
	private boolean rFlag;
	private String objectName;
	private String containerName;
	private String position;
	private String eventType;
	private String tenantName;
	private String userName;
	
	/**
	 * DO NOT call this from the programming environment!
	 */
	public EventModel() {
		oldMetadata = new HashMap<String, String>();
		newMetadata = new HashMap<String, String>();
		rFlag = true;
		objectName = "something.txt";
		containerName = "container";
		position = "127.0.0.1";
		tenantName = "";
		userName = "";
	}
	
	/**
	 * DO NOT call this from the programming environment!
	 */
	public void setOldMetadata(Map<String, String> oldMetadata) {
		this.oldMetadata = oldMetadata;
	}

	/**
	 * DO NOT call this from the programming environment!
	 */
	public void setNewMetadata(Map<String, String> newMetadata) {
		this.newMetadata = newMetadata;
	}

	/**
	 * DO NOT call this from the programming environment!
	 */
	public void setrFlag(boolean rFlag) {
		this.rFlag = rFlag;
	}

	/**
	 * DO NOT call this from the programming environment!
	 */
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	/**
	 * DO NOT call this from the programming environment!
	 */
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	/**
	 * DO NOT call this from the programming environment!
	 */
	public void setPosition(String position) {
		this.position = position;
	}

	/**
	 * DO NOT call this from the programming environment!
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * DO NOT call this from the programming environment!
	 */
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	/**
	 * DO NOT call this from the programming environment!
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	



	public String getUserName() {
		return userName;
	}

	/**
	 * Previous/old metadata of the object that caused the trigger
	 */
	public Map<String, String> getOldMetadata() {
		return oldMetadata;
	}

	/**
	 * Current/new metadata of the object that caused the trigger
	 */
	public Map<String, String> getNewMetadata() {
		return newMetadata;
	}

	/**
	 * Specifies if the trigger was caused by a replication event. If true, the
	 * source of the original event was another container replica as the
	 * storlet. If false, the source of the original event was the same
	 * container replica as the storlet.
	 */
	public boolean isrFlag() {
		return rFlag;
	}

	/**
	 * Object name of the object that caused the trigger
	 */
	public String getObjectName() {
		return objectName;
	}

	/**
	 * Container name of the object that caused the trigger
	 */
	public String getContainerName() {
		return containerName;
	}

	/**
	 * Tenant name of the object that caused the trigger
	 */
	public String getTenantName() {
		return tenantName;
	}

	/**
	 * DO NOT call this from the programming environment!
	 */
	public String getPosition() {
		return position;
	}

	/**
	 * DO NOT call this from the programming environment!
	 */
	public String getEventType() {
		return eventType;
	}

	@Override
	public String toString() {

		String tenantNameString = "\tTenant Name : " + tenantName
				+ "\n";
		String containerNameString = "\tContainer Name : " + containerName
				+ "\n";
		String objectNameString = "\tObject Name : " + objectName + "\n";
		String rflagString = "\tReplica Flag : " + rFlag + "\n";
		String positionString = "\tPosition : " + position + "\n";
		String oldMetadataString = "\tOld Metadata : " + oldMetadata + "\n";
		String newMetadataString = "\tNew Metadata : " + newMetadata + "\n";
		String typeString = "\tEventType: " + eventType + "\n";
		String userNameString = "\tUserName: " + userName + "\n";
		
		return "{\n\t<EVENT>\n" + tenantNameString +containerNameString + objectNameString
				+ rflagString + positionString + oldMetadataString
				+ newMetadataString + typeString + userNameString+"}";
	}

	@Override
	public boolean equals(Object object) {
		EventModel other = (EventModel) object;

		if (false == Utils.equalityGeneric(other.tenantName,
				this.tenantName))
			return false;
		
		if (false == Utils.equalityBooleans(other.rFlag, this.rFlag))
			return false;

		if (false == Utils.equalityGeneric(other.objectName, this.objectName))
			return false;

		if (false == Utils.equalityGeneric(other.containerName,
				this.containerName))
			return false;

		if (false == Utils.equalityGeneric(other.position, this.position))
			return false;

		if (false == Utils.equalityGeneric(other.eventType, this.eventType))
			return false;

		if (false == Utils.equalityStringMaps(other.newMetadata, this.newMetadata))
			return false;

		if (false == Utils.equalityStringMaps(other.oldMetadata,
				this.oldMetadata))
			return false;
		if (false == Utils.equalityGeneric(other.userName,
				this.userName))
			return false;

		return true;
	}

//	@JsonIgnore
	public ObjIdentifier getObjeIdentifier() {
		ObjIdentifier res = new ObjIdentifier(tenantName, containerName,
				objectName);
		return res;
	}

}