package org.qtjambi.maven.plugins.utils.shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

public class StreamReaderThread implements Runnable {
	public static final Integer DEFAULT_SAVED_VALUE = Integer.valueOf(-1);
	private final int DEFAULT_SAVED_MAXIMUM_LENGTH = 16384;

	private InputStream inStream;
	private InputStreamReader isr;
	private BufferedReader br;
	private Log log;
	private String prefix;

	// Variables relating to saving of the data
	private List<String> savedList;
	private int currentSavedLength;
	private Integer savedMaximumLength;
	private boolean savedListOverflowFlag;

	public StreamReaderThread(InputStream inStream) {
		this.inStream = inStream;
	}

	public void run() {
		try {
			// This uses a default character-set to transform raw byte[] data
			isr = new InputStreamReader(inStream);
			br = new BufferedReader(isr);
			String s;
			while((s = br.readLine()) != null) {
				if(savedList != null) {		// should we be saving the data ?
					final int sLen = s.length();
					if(savedListOverflowFlag == false) {	// Not yet overflowed
						if(savedMaximumLength != null && sLen + currentSavedLength > savedMaximumLength) {
							int left = savedMaximumLength.intValue() - currentSavedLength;
							if(left > 0)
								savedList.add(s.substring(0, left));	// clip
							savedListOverflowFlag = true;
						} else {
							savedList.add(s);
						}
					}
					currentSavedLength += sLen;		// always update the length (even when we truncate)
				}
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

	public void saveStream(Integer savedMaximumLength) {
		if(savedList == null) {
			savedList = new ArrayList<String>();
			if(savedMaximumLength != null && savedMaximumLength.intValue() < 0)
				this.savedMaximumLength = Integer.valueOf(DEFAULT_SAVED_MAXIMUM_LENGTH);
			else
				this.savedMaximumLength = savedMaximumLength;
		}
	}

	public void saveStream() {
		saveStream(Integer.valueOf(-1));
	}

	public String getStreamAsString() {
		StringBuffer sb = new StringBuffer();
		if(savedList != null) {
			for(String s : savedList) {
				sb.append(s);
			}
		}
		return sb.toString();
	}

	public boolean isSavedListOverflowFlag() {
		return this.savedListOverflowFlag;
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
