package org.qtjambi.buildtools.generator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.Pipe.SourceChannel;

/**
 * Class which encapsulates System.in as a selectable channel.
 * Instantiate this class, call start() on it to run the background
 * draining thread, then call getStdinChannel() to get a SelectableChannel
 * object which can be used with a Selector object.
 *
 * @author Ron Hitchens (ron@ronsoft.com)
 * created: Jan 2003
 */
public class SystemInSelectable {
	//private static SystemCopyThread systemInCopyThread;
	private Pipe pipe;
	private SystemCopyThread copyThread;

	public SystemInSelectable(InputStream in) throws IOException {
		pipe = Pipe.open();

		copyThread = new SystemCopyThread(in, pipe.sink(), true);
		if(in == System.in)
			copyThread.setName("systemInSelectableCopyThread");
		else
			copyThread.setName(in.toString() + "SelectableCopyThread");
	}

	public SystemInSelectable() throws IOException {
		this(System.in);
	}

	public void start() {
		copyThread.start();
	}

	public SourceChannel getStdinChannel() throws IOException {
		SourceChannel channel = pipe.source();

		channel.configureBlocking(false);

		return channel;
	}

	protected void finalize() {
		copyThread.shutdown();
	}

	// Could make this a static singleton pattern, so that even if this remains
	//  blocked we can disconnect/reconnect around the blocked thread
	public static class SystemCopyThread extends Thread {
		private boolean keepRunning = true;
		private byte[] bytes = new byte[512*8];
		private ByteBuffer buffer = ByteBuffer.wrap(bytes);
		private InputStream in;
		private WritableByteChannel out;
		private boolean closeOut;

		private SystemCopyThread(InputStream in, WritableByteChannel out, boolean closeOut) {
			this.in = in;
			this.out = out;
			this.closeOut = closeOut;
			this.setDaemon(true);
		}

		public void shutdown() {
			synchronized(this) {
				keepRunning = false;
				this.interrupt();
			}
		}
		public WritableByteChannel disconnectOut() {
			WritableByteChannel tmpOut;
			synchronized(this) {
				tmpOut = out;
				out = null;
			}
			return tmpOut;
		}
		public void connectOut(WritableByteChannel out) {
			synchronized(this) {
				this.out = out;
			}
		}

		public void run() {
			try {
				while(true) {
					synchronized(this) {
						if(!keepRunning)
							break;
					}
					int count = in.read(bytes);

					if(count < 0)
						break;

					buffer.clear().limit(count);
					// This allows for disconnection
					WritableByteChannel tmpOut;
					synchronized(this) {
						tmpOut = out;
						if(tmpOut == null)
							break;
					}
					if(tmpOut != null)
						tmpOut.write(buffer);
				}

				if(closeOut) {
					try {
						WritableByteChannel tmpOut;
						synchronized(this) {
							tmpOut = out;
							// not sure what happens when caller and us call close() at same time
							// the caller would presumably disconnect us first, if we have
							// closeOut==true, so that the caller can close() the channel itself.
							// so putting it inside the lock ensure no overlapping close() calls.
							if(tmpOut != null)
								tmpOut.close();	// maybe this should be inside lock anyway
							// This also has the effect of signalling to the caller we already
							// called close, since disconnectOut() will now return null.  So now
							// disconnect software function has clear hand over for who needs to
							// call close() and if close has/has not been called already.
							out = null;
						}
					} catch(IOException eat) {
					}
				}
			} catch(IOException e) {
				System.err.println("Exception from " + Thread.currentThread().getName() + "[" + Thread.currentThread().getId() + "] thread:");
				e.printStackTrace();
			}
		}
	}
}
