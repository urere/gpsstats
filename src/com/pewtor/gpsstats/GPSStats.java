package com.pewtor.gpsstats;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.pewtor.gpsstats.file.NMEAFilePort;
import com.pewtor.gpsstats.nmea.NMEAReceiver;
import com.pewtor.gpsstats.nmea.Port;
import com.pewtor.gpsstats.nmea.Sentence;
import com.pewtor.gpsstats.nmea.SentenceConsumer;
import com.pewtor.gpsstats.serial.ComPort;


public class GPSStats implements SentenceConsumer {

	public static void main(String[] args) throws Exception {
		
		GPSStats gpsStats = new GPSStats();
		
		gpsStats.start();
		
		/*
		NMEAReceiver nmeaReceiver = new NMEAReceiver( gpsStats );
		
		ComPort port = new ComPort( "COM7", nmeaReceiver );
//		NMEAFilePort port = new NMEAFilePort( "./data/CAPTURE11.TXT", nmeaReceiver );
		
		port.start();
		
		int i = 20;
		do {
			Thread.sleep(1000);
			System.out.println( "Bytes: " + port.getByteCount() + " Rate: " + port.getBytesPerSecond() + "bps");
			gpsStats.printStats();
			System.out.println();
			
			i -= 1;
		} while ( i > 0 );
		
		
		port.stop();
		*/
	}
	
	private Map<String,SentenceStats> stats = new HashMap<String,SentenceStats>();
	
	private int sentenceErrorCount = 0;
	
	private boolean trace = false;
	
	private GPSStats() {
	}
	
	private void start() throws Exception {
		
		System.out.println( "GPS Statistics");

		NMEAReceiver nmeaReceiver = null;
		Port port = null;
		
		
		BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
		boolean running = true;
		do {
			System.out.print( "> " );
			System.out.flush();
			String cmdLine = in.readLine();
			
			boolean processed = false;
			if ( cmdLine != null ) {
				
				// Extract any argument
				String argument = "";
				int fs = cmdLine.indexOf(" ");
				if ( fs != -1 ) {
					argument = cmdLine.substring(fs+1);
					cmdLine = cmdLine.substring(0, fs);
				}
				
				if ( cmdLine.equalsIgnoreCase("help") ) {
					
					// Display help
					processed = false;
					
				} else if ( cmdLine.equalsIgnoreCase("ports") ) {
					
					// Display available com ports
					System.out.println( "Available Serial Ports: " + ComPort.getAvailablePorts());
					processed = true;
					
				} else if ( cmdLine.equalsIgnoreCase("serial") ) {	
					
					// Open specified serial port
					if ( argument.length() > 0 ) {
						nmeaReceiver = new NMEAReceiver( this );
						port = new ComPort( argument, nmeaReceiver );
					} else {
						System.out.println( "ERROR: No com port specified." );
					}
					processed = true;
					
				} else if ( cmdLine.equalsIgnoreCase("file") ) {

					// Open specified file
					if ( argument.length() > 0 ) {
						nmeaReceiver = new NMEAReceiver( this );
						port = new NMEAFilePort( argument, nmeaReceiver );
					} else {
						System.out.println( "ERROR: No file specified." );
					}
					processed = true;
					
				} else if ( cmdLine.equalsIgnoreCase("start") ) {
					
					// Start collecting if port is open
					if ( port != null ) {
						clear();
						port.start();
					} else {
						System.out.println( "ERROR: No open port." );
					}
					processed = true;
					
				} else if ( cmdLine.equalsIgnoreCase("stop") ) {
					
					// Stop collecting if port is open
					if ( port != null ) {
						port.stop();
					} else {
						System.out.println( "ERROR: No open port." );
					}
					processed = true;
					
				} else if ( cmdLine.equalsIgnoreCase("print") ) {
					
					// Print current stats if port is open
					if ( port != null ) {
						System.out.println( "Bytes: " + port.getByteCount() + " Rate: " + port.getBytesPerSecond() + "bps");
						System.out.println( "Checksum Errors: " + sentenceErrorCount );
						printStats();
						System.out.println();
					} else {
						System.out.println( "ERROR: No open port." );
					}
					processed = true;
					
				} else if ( cmdLine.equalsIgnoreCase("trace") ) {
					
					if ( argument.equalsIgnoreCase("on") ) {
						trace = true;
					} else if ( argument.equalsIgnoreCase("off") ) {
						trace = false;
					} else {
						trace = !trace;
					}
					if ( trace ) {
						System.out.println( "trace is ON");
					} else {
						System.out.println( "trace is OFF");
					}
					
					processed = true;
					
				} else if ( cmdLine.equalsIgnoreCase("quit") ) {
					
					if ( port != null ) {
						port.stop();
					}
					processed = true;
					running = false;
				} 
			}
			if ( !processed ) {
				System.out.println( "Commands: help, ports, serial <portname>, file <filename>, start, stop, print, trace on|off, quit" );
			}
		} while ( running );
	}
	
	private void clear() {
		stats.clear();
		sentenceErrorCount = 0;
	}
	
	public void printStats() {
		
		// sort by key which will give order by  sentence and then talker
		Object[] keys = stats.keySet().toArray();
		Arrays.sort(keys);
		
		for ( int k = 0; k < keys.length; k++ ) {
			
			SentenceStats sentenceStats = stats.get( keys[k] );
			
			StringBuffer line = new StringBuffer();
			line.append( sentenceStats.getTalkerID() );
			line.append( " " );
			line.append( sentenceStats.getSentenceID() );
			line.append( " " );
			line.append( padLeft(sentenceStats.getRxCount()) );
			line.append( " " );
			line.append( padLeft(sentenceStats.getErrorCount()) );
			line.append( " " );
			line.append( sentenceStats.getDescription() );
			
			System.out.println( line.toString() );
		}
	}
	
	public static String padLeft(int n) {
	    return String.format("%05d", n );
	}	

	@Override
	public void processSentence(Sentence sentence) {
		
		if ( trace ) { 
			System.out.println( sentence.toString() );
		}
		
		if ( sentence.isChecksumValid() ) {
			String key = sentence.getSentenceID() + ":" + sentence.getTalkerID();
			SentenceStats sentenceStats = stats.get(key);
			if ( sentenceStats == null ) {
				sentenceStats = new SentenceStats( sentence.getTalkerID(), sentence.getSentenceID() );
				stats.put(key, sentenceStats);
			}
			sentenceStats.addSentence(sentence);
		} else {
			this.sentenceErrorCount += 1;
		}
	}

}

class SentenceStats {
	
	private String talkerID;
	
	private String sentenceID;
	
	private String description;
	
	private int rxCount = 0;
	
	private int errorCount = 0;
	
	public SentenceStats( String talkerID, String sentenceID ) {
		
		this.talkerID = talkerID;
		this.sentenceID = sentenceID;
		this.description = Sentence.getSentenceDescription(sentenceID);
	}
	
	public void addSentence( Sentence sentence ) {
		
		rxCount += 1;
		if ( (sentence.isChecksumPresent()) && (!sentence.isChecksumValid()) ) {
			errorCount += 1;
		}
	}
	
	public String getTalkerID() {
		return talkerID;
	}

	public String getSentenceID() {
		return sentenceID;
	}

	public String getDescription() {
		return description;
	}

	public int getRxCount() {
		return rxCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public String toString() {
		
		StringBuffer msg = new StringBuffer();
		
		msg.append( "Talker: [" + talkerID + "]" );
		msg.append( " " );
		msg.append( "Sentence: [" + sentenceID + "]" );			
		msg.append( " " );
		msg.append( " Received: [" + rxCount + "]" );
		msg.append( " " );
		msg.append( " Errors: [" + errorCount + "]" );
		
		return msg.toString();
	}
}

	
	
