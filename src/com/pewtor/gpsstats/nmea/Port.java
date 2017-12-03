package com.pewtor.gpsstats.nmea;

import jssc.SerialPort;
import jssc.SerialPortException;

public interface Port {

	public boolean start();
	
	public boolean stop();
	
	public long getByteCount();
	
	public long getBytesPerSecond();
	
}
