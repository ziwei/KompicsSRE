package fakeStorletInterface;

public class ObjIdentifier {
	private final String tenantName;
	private final String containerName;
	private final String objectName;

	public ObjIdentifier(String tenantName, String containerName,
			String objectName) {
		if (Utils.anyAreNull(tenantName, containerName, objectName))
			throw new NullPointerException("Parameters may not be null");

		this.tenantName = tenantName;
		this.containerName = containerName;
		this.objectName = objectName;
	}

	public String getTenantName() {
		return tenantName;
	}

	public String getContainerName() {
		return containerName;
	}

	public String getObjectName() {
		return objectName;
	}

	public String getObjID() {
		return tenantName + "." + containerName + "." + objectName;
	}

	public static ObjIdentifier createFromString(String objID) {
		String ident[] = objID.split("\\.");
		return new ObjIdentifier(ident[0], ident[1], ident[2]);
	}

	
	@Override
	public boolean equals(Object object) {
		ObjIdentifier other = (ObjIdentifier) object;
		return this.hashCode() == other.hashCode();
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return getObjID();
	}

}
