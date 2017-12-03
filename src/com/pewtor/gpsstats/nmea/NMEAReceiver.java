package com.pewtor.gpsstats.nmea;


public class NMEAReceiver implements DataConsumer {

	private StringBuffer nmeaSentence = new StringBuffer();
	private boolean gotStart = false;
	private long startTime = 0;
	private SentenceConsumer sentenceConsumer = null;
	
	public NMEAReceiver() {
	}
	
	public NMEAReceiver( SentenceConsumer sentenceConsumer ) {
		this.sentenceConsumer = sentenceConsumer;
	}

	@Override
	public void processBuffer(long timestamp, byte[] buffer) {
		
		for ( int b = 0; b < buffer.length; b++ ) {
        	if ( !this.gotStart ) {
        		if ( buffer[b] == '$' ) {
        			gotStart = true;
        			startTime = timestamp;
    				nmeaSentence.append( Character.toChars(buffer[b]) );
        		}
        	} else {
        		if ( buffer[b] == '$' ) {
        			// Found another start, treat sentence as invalid
        			System.err.println( "Truncated Sentence: [" + nmeaSentence.toString() + "]");
        			nmeaSentence.setLength(0);
        			gotStart = true;
        			startTime = timestamp;
    				nmeaSentence.append( Character.toChars(buffer[b]) );
        		} else {
        			if ( (buffer[b] == '\r') || (buffer[b] == '\n') ) {
        				// Reached the end of the sentence, create a sentence object
        				Sentence sentence = new Sentence( startTime, nmeaSentence.toString() );
        				
        				// and pass it to the sentence consumer
        				if ( sentenceConsumer != null ) {
        					sentenceConsumer.processSentence( sentence );
        				}
        				
        				// and start again
        				nmeaSentence.setLength(0);
        				this.gotStart = false;
        			} else {
        				if ( buffer[b] != 0 ) {
        					nmeaSentence.append( Character.toChars(buffer[b]) );
        				}
        			}
        		}
        	}
        }
	}
	
}
