package com.pewtor.gpsstats.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import com.pewtor.gpsstats.nmea.DataConsumer;
import com.pewtor.gpsstats.nmea.Port;

public class NMEAFilePort implements Port {
	
	private String fileName = null;
	
	private DataConsumer dataConsumer = null;
	
	private long byteCount = 0;
	
	private long startTime = 0;
	
	long nanoPerBit = 1000000000/4800;
	
	public NMEAFilePort( String fileName, DataConsumer dataConsumer ) {
	
		this.fileName = fileName;
		this.dataConsumer = dataConsumer;
	}
	
	public boolean start() {
		
		boolean started = false;

		File nmeaFile = new File(this.fileName);
            
        byteCount = 0;
        startTime = System.nanoTime();
        
        NMEAFileReader nmeaFileReader = new NMEAFileReader(nmeaFile);
        Thread theThread = new Thread(nmeaFileReader);
        theThread.start();
        
        started = true;
		
		return started;
	}
	
	public boolean stop() {
		
		boolean stopped = true;
		
		return stopped;
	}
	
	public long getByteCount() {
		
		return byteCount;
	}
	
	public long getBytesPerSecond() {
		
		long curCount = byteCount;
		long start = startTime;
		long duration = start + ((curCount * 10) * nanoPerBit);
		long bps = 0;
		
		if ( duration > 0 ) {
			bps = curCount / duration;			
		}
		
		return bps;
	}

	class NMEAFileReader implements Runnable {
		
		File nmeaFile;
		
		public NMEAFileReader( File nmeaFile ) {
			this.nmeaFile = nmeaFile;
		}

		@Override
		public void run() {
			
			try {
				BufferedInputStream in = new BufferedInputStream( new FileInputStream( nmeaFile ) );
				byte[] buffer = new byte[64];
				int rc = in.read(buffer);
				while ( rc != -1 ) {
					
					byteCount += rc;

					// Pass over a buffer
					long bufferTime = startTime + ((rc * 10) * nanoPerBit);
		            dataConsumer.processBuffer(bufferTime, Arrays.copyOf(buffer, rc));
					
					// Get the next buffer
					rc = in.read(buffer);
				}
			}
			catch ( Exception e ) {
				e.printStackTrace();
			}
		}
    }	
}
