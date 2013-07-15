package fakeStorletInterface;

public class StorletException extends Exception {

	private static final long serialVersionUID = 1L;

	public StorletException(String arg0) {
		super(arg0);
	}

	public StorletException(Throwable arg0) {
		super(arg0);
	}

	public StorletException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
