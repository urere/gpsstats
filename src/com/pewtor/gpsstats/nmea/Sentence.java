package com.pewtor.gpsstats.nmea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class Sentence {
	
	private static Map<String,String> sentenceDescriptions;
	static {
		sentenceDescriptions = new HashMap<String,String>();
		sentenceDescriptions.put( "AAM", "Waypoint Arrival Alarm" );
		sentenceDescriptions.put( "ALM", "GPS Almanac Data" );
		sentenceDescriptions.put( "APA", "Autopilot Sentence 'A'" );
		sentenceDescriptions.put( "APB", "Autopilot Sentence 'B'" );
		sentenceDescriptions.put( "ASD", "Autopilot System Data" );
		sentenceDescriptions.put( "BEC", "Bearing & Distance to Waypoint, Dead Reckoning" );
		sentenceDescriptions.put( "BOD", "Bearing, Origin to Destination" );
		sentenceDescriptions.put( "BWC", "Bearing & Distance to Waypoint, Great Circle" );
		sentenceDescriptions.put( "BWR", "Bearing & Distance to Waypoint, Rhumb Line" );
		sentenceDescriptions.put( "BWW", "Bearing, Waypoint to Waypoint" );
		sentenceDescriptions.put( "DBT", "Depth Below Transducer" );
		sentenceDescriptions.put( "DCN", "Decca Position" );
		sentenceDescriptions.put( "DPT", "Depth" );
		sentenceDescriptions.put( "DSC", "Digital Selective Calling" );
		sentenceDescriptions.put( "FSI", "Frequency Set Information" );
		sentenceDescriptions.put( "GGA", "Global Positioning System Fix Data" );
		sentenceDescriptions.put( "GLC", "Geographic Position, Loran-C" );
		sentenceDescriptions.put( "GLL", "Geographic Position, Latitude/Longitude" );
		sentenceDescriptions.put( "GSA", "GPS DOP and Active Satellites" );
		sentenceDescriptions.put( "GSV", "GPS Satellites in View" );
		sentenceDescriptions.put( "GTD", "Geographic Location in Time Differences" );
		sentenceDescriptions.put( "GXA", "TRANSIT Position" );
		sentenceDescriptions.put( "HDG", "Heading, Deviation & Variation" );
		sentenceDescriptions.put( "HDM", "Heading – Magnetic" );
		sentenceDescriptions.put( "HDT", "Heading, True" );
		sentenceDescriptions.put( "HSC", "Heading Steering Command" );
		sentenceDescriptions.put( "LCD", "Loran-C Signal Data" );
		sentenceDescriptions.put( "MTA", "Air Temperature (to be phased out)" );
		sentenceDescriptions.put( "MTW", "Water Temperature" );
		sentenceDescriptions.put( "MWD", "Wind Direction" );
		sentenceDescriptions.put( "MWV", "Wind Speed and Angle" );
		sentenceDescriptions.put( "OLN", "Omega Lane Numbers" );
		sentenceDescriptions.put( "OSD", "Own Ship Data" );
		sentenceDescriptions.put( "R00", "Waypoint active route (not standard)" );
		sentenceDescriptions.put( "RMA", "Recommended Minimum Specific Loran-C Data" );
		sentenceDescriptions.put( "RMB", "Recommended Minimum Navigation Information" );
		sentenceDescriptions.put( "RMC", "Recommended Minimum Specific GPS/TRANSIT Data" );
		sentenceDescriptions.put( "ROT", "Rate of Turn" );
		sentenceDescriptions.put( "RPM", "Revolutions" );
		sentenceDescriptions.put( "RSA", "Rudder Sensor Angle" );
		sentenceDescriptions.put( "RSD", "RADAR System Data" );
		sentenceDescriptions.put( "RTE", "Routes" );
		sentenceDescriptions.put( "SFI", "Scanning Frequency Information" );
		sentenceDescriptions.put( "STN", "Multiple Data ID" );
		sentenceDescriptions.put( "TRF", "Transit Fix Data" );
		sentenceDescriptions.put( "TTM", "Tracked Target Message" );
		sentenceDescriptions.put( "VBW", "Dual Ground/Water Speed" );
		sentenceDescriptions.put( "VDR", "Set and Drift" );
		sentenceDescriptions.put( "VHW", "Water Speed and Heading" );
		sentenceDescriptions.put( "VLW", "Distance Traveled through the Water" );
		sentenceDescriptions.put( "VPW", "Speed, Measured Parallel to Wind" );
		sentenceDescriptions.put( "VTG", "Track Made Good and Ground Speed" );
		sentenceDescriptions.put( "VWR", "Relative Wind Speed and Angle" );
		sentenceDescriptions.put( "WCV", "Waypoint Closure Velocity" );
		sentenceDescriptions.put( "WNC", "Distance, Waypoint to Waypoint" );
		sentenceDescriptions.put( "WPL", "Waypoint Location" );
		sentenceDescriptions.put( "XDR", "Transducer Measurements" );
		sentenceDescriptions.put( "XTE", "Cross-Track Error, Measured" );
		sentenceDescriptions.put( "XTR", "Cross-Track Error, Dead Reckoning" );
		sentenceDescriptions.put( "ZDA", "Time & Date" );
		sentenceDescriptions.put( "ZFO", "UTC & Time from Origin Waypoint" );
		sentenceDescriptions.put( "ZTG", "UTC & Time to Destination Waypoint " );
	};
	
	public static String getSentenceDescription( String sentenceID ) {
		
		if ( sentenceDescriptions.containsKey(sentenceID) ) {
			return sentenceDescriptions.get(sentenceID);
		} else {
			return "Unknown";
		}		
	}

	private boolean gotSentence = false;
	
	private long received = 0; 
	
	private String talkerID = "";
	
	private String sentenceID = "";
	
	private List<String> fields = new ArrayList<String>();
	
	private boolean checksumPresent = false;
	
	private boolean checksumValid = false;

	public Sentence( long rxStartTime, String sentenceBuffer ) {
		
		this.received = rxStartTime;
		
		parseSentence( sentenceBuffer );
	}
	
	private boolean parseSentence( String sentenceBuffer ) {
		
		gotSentence = false;
		
		//System.err.println( "Parsing: " + sentenceBuffer );
		
		// Length must be at least 6 for $, talker and sentence identifiers
		if ( sentenceBuffer.length() >= 6 ) {
			
			// Extract identifiers
			talkerID = sentenceBuffer.substring( 1, 3);
			sentenceID = sentenceBuffer.substring( 3, 6);
			
			String[] parsedFields = sentenceBuffer.split( ",", -1 );
			for ( int f = 1; f < parsedFields.length; f++ ) {
				
				// If its the last fields, remove any checksum
				if ( f == (parsedFields.length-1)) {
					
					String field = parsedFields[f];
					int csi = field.indexOf("*");
					if ( csi != -1 ) {
						String cs = field.substring(csi+1);
						
						// Validate the checksum
						checksumPresent = true;
						checksumValid = validateChecksum( sentenceBuffer, cs );
						
						field = field.substring(0, csi);
					}
					fields.add( field );
					
				} else {
					fields.add( parsedFields[f] );
				}
			}
			
			gotSentence = true;
			
		} else {
			displayParseError( sentenceBuffer, "Too short" );
		}
		
		return gotSentence;
	}
	
	private boolean validateChecksum( String sentence, String cs ) {
		
		boolean valid = false;
		
		int csi = sentence.lastIndexOf("*");
		
		int calcCS = sentence.charAt(1);
		for ( int c = 2; c < csi; c++ ) {
			calcCS = calcCS ^ sentence.charAt(c);
		}
		
		if ( calcCS < 16 ) {
			valid = ("0" + Integer.toHexString(calcCS)).equalsIgnoreCase(cs);
			
		} else {
			valid = Integer.toHexString(calcCS).equalsIgnoreCase(cs);
		}
		
		return valid;
	}
	
	private void displayParseError( String sentenceBuffer, String message ) {
		System.err.println( "parseSentence ERROR: [" + sentenceBuffer + "] " + message );
	}

	public long getReceived() {
		return received;
	}
	
	public boolean gotSentence() {
		return gotSentence;
	}

	public String getTalkerID() {
		return talkerID;
	}

	public String getSentenceID() {
		return sentenceID;
	}
	
	public String getDescription() {
		
		return getSentenceDescription(getSentenceID());
	}

	public List<String> getFields() {
		return fields;
	}

	public boolean isChecksumPresent() {
		return checksumPresent;
	}

	public boolean isChecksumValid() {
		if ( !checksumPresent ) {
			return true;
		} else {
			return checksumValid;
		}
	}
	
	
	public String toString() {
		
		StringBuffer msg = new StringBuffer();
		
		if ( gotSentence ) {
			
			msg.append( "Talker: [" + getTalkerID() + "]" );
			msg.append( " " );
			msg.append( "Sentence: [" + getSentenceID() + "]" );
			msg.append( " " );
			msg.append( getDescription() );
			
			if ( checksumPresent ) {
				if ( !this.checksumValid ) {
					msg.append( " INVALID CHECKSUM" );
				}
			}
			
		} else {
			msg = msg.append("No sentence");
		}
		
		return msg.toString();
	}
}
