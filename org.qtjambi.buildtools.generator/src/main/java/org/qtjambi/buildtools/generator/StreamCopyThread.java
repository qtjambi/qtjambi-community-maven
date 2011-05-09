package org.qtjambi.buildtools.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class StreamCopyThread extends Thread {
	private SourceChannel inChannel;
	private InputStream inStream;
	private OutputStream outStream;
	private boolean usingChannel;
	private boolean shutdownFlag;

	public StreamCopyThread(InputStream inStream, OutputStream outStream) {
		this.inStream = inStream;
		this.outStream = outStream;
	}

	public StreamCopyThread(SourceChannel inChannel, InputStream inStream, OutputStream outStream) {
		this.inChannel = inChannel;
		this.inStream = inStream;
		this.outStream = outStream;
	}

	public void run() {
		try {
			if(inChannel != null) {
				Selector sel;
				if((sel = tryChannel()) != null)
					doChannel(sel);
				else
					doStream();
			} else {
				doStream();
			}
		} catch(IOException e) {
			System.err.println("Exception from " + Thread.currentThread().getName() + "[" + Thread.currentThread().getId() + "] thread:");
			e.printStackTrace();
		}
	}

	private Selector tryChannel() {
		Selector sel = null;
		if(true)
			return sel;
		try {
			sel = Selector.open();
		} catch(IOException e) {
			// On windows this does not always work, sometimes due to firewall
			//  sometimes due to it being on a real terminal.  So we fallback
			e.printStackTrace();
		}
		return sel;
	}

	private void doChannel(Selector sel) throws IOException {
		if(Main.isVerbose())
			System.err.println("doChannel() working for " + Thread.currentThread().getName());
		usingChannel = true;
		SelectionKey selKey = null;
		try {
			if(sel == null)
				throw new NullPointerException("sel="+sel);
			selKey = inChannel.register(sel, SelectionKey.OP_READ);

			byte[] bytes = new byte[4096];
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			// This initial flag is to make sure we call selectNow()
			//  at least once and process data before doing our first
			//  check of shutdownFlag, this is so we don't exit before we
			//  looped for data.
			boolean initial = true;
			while(true) {
				// The idea is that we will loop while selectNow() keeps
				//  returning indicating.  When selectNow() stops indicating
				//  we reset initial and check shutdownFlag for the first time
				//  before them sleeping for I/O.
				boolean doRead = false;
				boolean doReadInitial = false;
				if(initial) {
					if(sel.selectNow() > 0)
						doReadInitial = true;
					else
						initial = false;
				}
				if(!initial) {
					synchronized(this) {
						if(shutdownFlag)
							break;
					}
					if(sel.select() > 0)
						doRead = true;
				}
				if(doRead || doReadInitial) {
					if(selKey.isReadable()) {	// overkill
						buffer.clear();
						int count = inChannel.read(buffer);

						if(count < 0)
							break;

						outStream.write(bytes, 0, count);
					}
				}
			}
			// cleanup 'selKey' in finally
			// cleanup 'sel' in finally
		} finally {
			if(selKey != null) {
				selKey.cancel();
				selKey = null;
			}
			if(sel != null) {
				try {
					sel.close();
				} catch(IOException eat) {
				}
				sel = null;
			}
		}
		try {
			outStream.flush();
		} catch(IOException eat) {
		}
	}

	private void doStream() throws IOException {
		if(Main.isVerbose())
			System.err.println("doStream() working for " + Thread.currentThread().getName());
		int n;
		byte[] bA = new byte[4096];
		while((n = inStream.read(bA)) > 0)
			outStream.write(bA, 0, n);
		try {
			outStream.flush();
		} catch(IOException eat) {
		}
	}

	public void shutdown() {
		synchronized(this) {
			shutdownFlag = true;
			interrupt();
		}
	}

	public boolean isUsingChannel() {
		return this.usingChannel;
	}

	public void close() {
	}
}
