package fakeStorletInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SyncOutputStream extends OutputStream {
	private boolean isConfig = false;
	private boolean isOpen = false;
	private final Socket outSock;
	private final OutputStream out;
	public static final long UNKNOWN_LENGTH = -1;
	
	public SyncOutputStream(Socket sock) throws IOException {
		this.outSock = sock;
		this.out = sock.getOutputStream();
	}
	
	public void configure(long lengthOfStream) throws IOException {
		if(isConfig)
			throw new IOException("Internal error, this can't be called more then once");
		out.write(ByteBuffer.allocate(8).putLong(lengthOfStream).array());
		this.isConfig = true;
		this.isOpen = true;
	}
	
	@Override
	public void close() throws IOException {
		if(!isOpen) return;
		if(!isConfig)throw new IOException("call configure before calling this function");
		
		out.write("".getBytes());
		out.flush();
		out.close();
		isOpen = false;
		outSock.close();
	}
	
	@Override
	public void write(int b) throws IOException {
		if(!isConfig)throw new IOException("call configure before calling this function");
		out.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		if(!isConfig)throw new IOException("call configure before calling this function");
		out.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if(!isConfig)throw new IOException("call configure before calling this function");
		out.write(b, off, len);
	}
}
