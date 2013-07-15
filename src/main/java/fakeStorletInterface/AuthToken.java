package fakeStorletInterface;

//import eu.visioncloud.storlet.deprecated.ObjectServiceInterface;

public class AuthToken {
	private static final String createAuthToken(String username, String tenant,
			String password) {
		if (Utils.anyAreNull(tenant, username, password))
			throw new NullPointerException("Parameters can not be null");
		String buildString = username + "@" + tenant + ":" + password;		
		return "Basic " + new String(Utils.encodeByteArray(buildString.getBytes()));
	}

	
	private final String authTokenString;

	/**
	 * Creates an authentication token. It is used to authenticate a
	 * {@link Storlet} when connecting to VisionCloud services, e.g. it will be
	 * used by the {@link ObjectServiceInterface} when connecting to the object
	 * service.
	 * 
	 * @param tenant
	 *            tenant to use for authentication
	 * 
	 * @param username
	 *            user name to authenticate
	 * 
	 * @param password
	 *            password to use for authentication
	 */
	public AuthToken(String tenant, String username, String password) {
		this.authTokenString = AuthToken.createAuthToken(username, tenant,
				password);
	}

	public AuthToken(String authTokenString) {
		this.authTokenString = authTokenString;
	}
	
	public String getAuthenticationString() {
		return authTokenString;
	}

	public String getDecodedAuthenticationString() {
		String encodedPart = authTokenString.substring("Basic ".length());
		return "Basic "+ new String(Utils.decodeByteArray(encodedPart.getBytes()));
	}

//	String getUsername() {
//		String decodedAuthTokenString = new String(
//				Utils.decodeByteArray(authTokenString.getBytes()));
//		String[] authTokenArgs = decodedAuthTokenString.split("[@:]");
//		return authTokenArgs[0];
//	}
//
//	String getTenant() {
//		String decodedAuthTokenString = new String(
//				Utils.decodeByteArray(authTokenString.getBytes()));
//		String[] authTokenArgs = decodedAuthTokenString.split("[@:]");
//		return authTokenArgs[1];
//	}
//
//	String getPassword() {
//		String decodedAuthTokenString = new String(
//				Utils.decodeByteArray(authTokenString.getBytes()));
//		String[] authTokenArgs = decodedAuthTokenString.split("[@:]");
//		return authTokenArgs[2];
//	}

	@Override
	public boolean equals(Object object) {
		AuthToken other = (AuthToken) object;

		if (false == Utils.equalityGeneric(other.authTokenString,
				this.authTokenString))
			return false;
		return true;
	}

}
