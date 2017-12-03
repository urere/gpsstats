package com.pewtor.gpsstats.nmea;

public interface DataConsumer {
	public void processBuffer( long timestamp, byte[] buffer );
}
