package com.pewtor.gpsstats.serial;

import java.util.ArrayList;
import java.util.List;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

import com.pewtor.gpsstats.nmea.DataConsumer;
import com.pewtor.gpsstats.nmea.Port;

public class ComPort implements Port {
	
	public static List<String> getAvailablePorts() {
		
		List<String> portList = new ArrayList<String>();
		
		String[] portNames = SerialPortList.getPortNames();
		for ( String pn : portNames ) {
			portList.add(pn);
		}
		
		return portList;
	}
	
	private String portName = null;
	
	private SerialPort serialPort = null;
	
	private DataConsumer dataConsumer = null;
	
	private long byteCount = 0;
	
	private long startTime = 0;
	
	public ComPort( String portName, DataConsumer dataConsumer ) {
	
		this.portName = portName;
		this.dataConsumer = dataConsumer;
	}
	
	public boolean start() {
		
		boolean started = false;
		
		serialPort = new SerialPort(this.portName); 
        try {
            serialPort.openPort();
            serialPort.setParams(4800, 8, 1, 0);
            
            byteCount = 0;
            startTime = System.nanoTime();
            
			serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
	        serialPort.addEventListener(new SerialPortReader());	
            started = true;
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }		
		
		return started;
	}
	
	public boolean stop() {
		
		boolean stopped = false;
		
		try {
			serialPort.removeEventListener();
		} catch (SerialPortException e) {
            System.out.println(e);
		}
		
		return stopped;
	}
	
	public long getByteCount() {
		
		return byteCount;
	}
	
	public long getBytesPerSecond() {
		
		long curCount = byteCount;
		long start = startTime;
		long duration = (System.nanoTime() - start) / 1000000000;
		long bps = 0;
		
		if ( duration > 0 ) {
			bps = curCount / duration;			
		}
		
		return bps;
	}

	class SerialPortReader implements SerialPortEventListener {
		
		public SerialPortReader() {
		}
		
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()){
                if(event.getEventValue() > 0) {
                	byteCount += event.getEventValue(); 
                    try {
                        byte buffer[] = serialPort.readBytes(event.getEventValue());
                        dataConsumer.processBuffer(System.nanoTime(), buffer);;
                    }
                    catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                }
            }
        }
    }	
}
