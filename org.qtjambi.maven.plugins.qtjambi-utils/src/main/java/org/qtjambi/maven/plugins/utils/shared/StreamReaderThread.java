package org.qtjambi.maven.plugins.utils.shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.plugin.logging.Log;

public class StreamReaderThread implements Runnable {
	private InputStream inStream;
	private InputStreamReader isr;
	private BufferedReader br;
	private Log log;
	private String prefix;

	public StreamReaderThread(InputStream inStream) {
		this.inStream = inStream;
	}

	public void run() {
		try {
			isr = new InputStreamReader(inStream);
			br = new BufferedReader(isr);
			String s;
			while((s = br.readLine()) != null) {
				StringBuilder sb = new StringBuilder();
				if(prefix != null)
					sb.append(prefix);
				sb.append(s);
				if(log != null)
					log.info(sb.toString());
			}
			br.close();
			br = null;
			isr.close();
			isr = null;
		} catch(IOException e) {
		}
	}

	public void close() {
		if(br != null) {
			try {
				br.close();
			} catch (IOException eat) {
			}
			br = null;
		}
		if(isr != null) {
			try {
				isr.close();
			} catch (IOException eat) {
			}
			isr = null;
		}
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public void setLog(Log log) {
		this.log = log;
	}
}
