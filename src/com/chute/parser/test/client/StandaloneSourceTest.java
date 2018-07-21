package com.chute.parser.test.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.flume.Event;
import org.slf4j.LoggerFactory;

import com.chute.parser.event.EventParser;
import com.chute.parser.source.CustomEventSource;

public class StandaloneSourceTest {

	public StandaloneSourceTest() {
	}

	public void run() {
		try {
			// processing input line by line
			EventParser multilineEventParser = new EventParser();
			List<Event> parsedEvents = new ArrayList<>();
//			BufferedReader inputReader = new BufferedReader(new FileReader(new File("/data/work/SingleWindowEvent.txt")));
			BufferedReader inputReader = new BufferedReader(new FileReader(new File("/data/work/oracle_text_audit.log")));
			char[] inputBuffer = new char[512];
			int totalCount = 0;
			int bytesRead = inputReader.read(inputBuffer, 0, inputBuffer.length);
			while (bytesRead > 0) {
				String datagram = new String(inputBuffer);
				try {
					// one line may contain multiple events, so always expect many events, thus prepare a List as the store
					multilineEventParser.parseEventsString(datagram, parsedEvents);  // \n has been consumed by reader, add it here again in order to complete the line as it was sent from client
//System.out.println("CustomNetcatSource: parsed events: "+parsedEvents.size());
					if (parsedEvents.size() > 0) { // if event spans multiple lines, the List may be empty until event end is detected
						totalCount += parsedEvents.size();
						for (Event ev : parsedEvents) {
							System.out.println(ev+" "+new String(ev.getBody())); // send events to channel
						}
					}
					parsedEvents.clear();
				} catch (Exception ex) {
					LoggerFactory.getLogger(CustomEventSource.class).error(null, ex);
				}
				bytesRead = inputReader.read(inputBuffer, 0, inputBuffer.length);
			}
			inputReader.close();
			System.out.println("******************************\nTotal events: "+totalCount);
		} catch (IOException ex) {
			LoggerFactory.getLogger(CustomEventSource.class).error(null, ex);
		}
	}

	public static void main(String[] args) {
		new StandaloneSourceTest().run();
	}

}
