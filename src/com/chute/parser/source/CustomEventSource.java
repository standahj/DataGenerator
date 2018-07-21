package com.chute.parser.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.source.AbstractSource;
import org.slf4j.LoggerFactory;

import com.chute.parser.event.EventParser;
import com.chute.parser.event.config.EventParserConfig;

/**
 * 
 * @author shejny
 *
 *	CustomEventSource provides equivalent functionality as NetcatSource but instead just copying the input line by line as event to a channel,
 *  it calls an EventParser, which is able to join multiple lines into one event based on regExp criteria for event boundaries
 *  
 *   This is event driven source though it can also be converted to poll-able source by un-commenting process(), getBackOffSleepIncrement(), getBackOffSleepInterval() and
 *   changing implemented interface from EventDrivenSource to PollableSource    
 */
public class CustomEventSource extends AbstractSource implements Configurable, EventDrivenSource {
	public CustomEventSource() {}

	private int port = -1;							// listening port this source binds to
	private ServerSocket serverSocket;				// Server socket that will listen on the above port for client connections
	private BlockingQueue<Event> distributedQueue;  // used for PollableSource implementation only. Not used in EventDrivenImplementation
	private boolean keepListening = true;			// allows for graceful exit from socket listening loop on shutdown

	/**
	 * Read configuration values for this source from flume-ng configuration properties file
	 * 
	 * All values except 'port' have default value provided in EventParserConfig class.
	 * This method is called by Flume-ng framework in the Source initialization phase.
	 * 
	 */
	@Override
	public void configure(Context context) {
		/*
		port = context.getInteger("port");
		String regexp = context.getString("BREAK_ONLY_BEFORE");
		if (regexp != null) {
			EventParserConfig.BREAK_ONLY_BEFORE = regexp;
		}
		System.out.println("Regexp BREAK_ONLY_BEFORE: "+EventParserConfig.BREAK_ONLY_BEFORE);
		regexp = context.getString("MUST_BREAK_AFTER");
		if (regexp != null) {
			EventParserConfig.MUST_BREAK_AFTER = regexp;
		}
		System.out.println("Regexp MUST_BREAK_AFTER: "+EventParserConfig.MUST_BREAK_AFTER);
		regexp = context.getString("MUST_NOT_BREAK_AFTER");
		if (regexp != null) {
			EventParserConfig.MUST_NOT_BREAK_AFTER = regexp;
		}
		System.out.println("Regexp MUST_NOT_BREAK_AFTER: "+EventParserConfig.MUST_NOT_BREAK_AFTER);
		regexp = context.getString("MUST_NOT_BREAK_BEFORE");
		if (regexp != null) {
			EventParserConfig.MUST_NOT_BREAK_BEFORE = regexp;
		}
		System.out.println("Regexp MUST_NOT_BREAK_BEFORE: "+EventParserConfig.MUST_NOT_BREAK_BEFORE);
		regexp = context.getString("EVENT_BREAKER");
		if (regexp != null) {
			EventParserConfig.EVENT_BREAKER = regexp;
		}
		System.out.println("Regexp EVENT_BREAKER: "+EventParserConfig.EVENT_BREAKER);
		regexp = context.getString("LINE_BREAKER");
		if (regexp != null) {
			EventParserConfig.LINE_BREAKER = regexp;
		}
		System.out.println("Regexp LINE_BREAKER: "+EventParserConfig.LINE_BREAKER);
		Boolean flag = context.getBoolean("BREAK_ONLY_BEFORE_DATE");
		if (flag != null) {
			EventParserConfig.BREAK_ONLY_BEFORE_DATE = flag;
		}
		System.out.println("Flag BREAK_ONLY_BEFORE_DATE: "+EventParserConfig.BREAK_ONLY_BEFORE_DATE);
		flag = context.getBoolean("SHOULD_LINEMERGE");
		if (flag != null) {
			EventParserConfig.SHOULD_LINEMERGE = flag;
		}
		System.out.println("Flag SHOULD_LINEMERGE: "+EventParserConfig.SHOULD_LINEMERGE);
		Integer limit = context.getInteger("LINE_BREAKER_LOOKBEHIND");
		if (limit != null) {
			EventParserConfig.LINE_BREAKER_LOOKBEHIND = limit;
		}
		System.out.println("Value LINE_BREAKER_LOOKBEHIND: "+EventParserConfig.LINE_BREAKER_LOOKBEHIND);
		limit = context.getInteger("MAX_EVENTS");
		if (limit != null) {
			EventParserConfig.MAX_EVENTS = limit;
		}
		limit = context.getInteger("TRUNCATE");
		System.out.println("Value MAX_EVENTS: "+EventParserConfig.MAX_EVENTS);
		if (limit != null) {
			EventParserConfig.TRUNCATE = limit;
		}
		System.out.println("value TRUNCATE: "+EventParserConfig.TRUNCATE);
*/
		//		System.out.println("****** Configuring CustomEventSource on port: "+port);
	}

	/**
	 * This method is called by Flume-ng framework to start this source.
	 * The socket binds to the configured port and start listening for connections.
	 * The actual client connection to host:port is the event that triggers EventParser instantiation and it starts new thread
	 * to handle this client connection in parallel.
	 * 
	 * keepListening facilitates graceful exist from the parallel thread and also from the socket listening loop on flume-ng shutdown
	 */
	@Override
	public void start() {
		distributedQueue = new ArrayBlockingQueue<Event>(3000, keepListening);
		try {
			//			System.out.println("****** Starting CustomEventSource on port: "+port);
			LoggerFactory.getLogger(CustomEventSource.class).info("****** Starting CustomEventSource on port: "+port);
			serverSocket = new ServerSocket(port);
			// the listener socket runs in a thread, because the start() method must actually finish, so cannot do a blocking operation like accept() here.
			new Thread(new Runnable() { 
				@Override
				public void run() { 
					/*
					 * Implement a Socket listener for events. Using closure rather then anonymous class for better performance 
					 */
					while (keepListening) {  // facilitate graceful exit by setting it to false
						try {
							// wait for a client connection
							final Socket remote = serverSocket.accept();
							// remote is now the connected socket
							final EventParser multilineEventParser = new EventParser();
							// run the client connection event parsing in separate thread to free this loop to accept another connection
							new Thread(new Runnable() { 
								@Override
								public void run() {
									try {
										// processing input line by line
										List<Event> parsedEvents = new ArrayList<>();
										BufferedReader inputReader = new BufferedReader(new InputStreamReader(remote.getInputStream()));
										char[] inputBuffer = new char[512];
										int bytesRead = inputReader.read(inputBuffer, 0, inputBuffer.length);
										while (bytesRead > 0 && keepListening) {
											String datagram = new String(inputBuffer);
											try {
												// one line may contain multiple events, so always expect many events, thus prepare a List as the store
												multilineEventParser.parseEventsString(datagram, parsedEvents);  // 
System.out.println("CustomNetcatSource: parsed events: "+parsedEvents.size());
												if (parsedEvents.size() > 0) { // if event spans multiple lines, the List may be empty until event end is detected
													getChannelProcessor().processEventBatch(parsedEvents); // send events to channel
												}
												parsedEvents.clear();
											} catch (Exception ex) {
												LoggerFactory.getLogger(CustomEventSource.class).error(null, ex);
											}
											bytesRead = inputReader.read(inputBuffer, 0, inputBuffer.length);
										}
									} catch (IOException ex) {
										LoggerFactory.getLogger(CustomEventSource.class).error(null, ex);
									}
								}
							}).start();
						} catch (Exception e) {
							System.out.println("Error: " + e);
						}
					}
				}
			}).start();
		} catch (Exception x) {
			LoggerFactory.getLogger(CustomEventSource.class).error(x.getMessage(), x);
		}
	}

	/**
	 * Called by flume-ng on shutdown.
	 */
	@Override
	public void stop () {
		// Disconnect from external client and do any additional cleanup
		// (e.g. releasing resources or nulling-out field values) ..
		keepListening = false;
		try {
			serverSocket.close();
		} catch (IOException ex) {
			LoggerFactory.getLogger(CustomEventSource.class).error(ex.getMessage(), ex);
		}
	}

	// the next method is needed to implement PollableSource interface. It was deprecated in favour of EventDrivenSource which has better performance 
	/*
	@Deprecated
	@Override
	public Status process() throws EventDeliveryException {
		Status status = Status.BACKOFF;
		try {
			// This try clause includes whatever Channel/Event operations you want to do
			// Receive new data
			// using event-driven source - do no processing here
			List<Event> e = readEvents();

			if (e != null) {
				// Store the Event into this Source's associated Channel(s)
				getChannelProcessor().processEventBatch(e);
				status = Status.READY;
			} else {
				status = Status.BACKOFF;
			}
		} catch (Throwable t) {
			// Log exception, handle individual exceptions as needed
			status = Status.BACKOFF;

			// re-throw all Errors
			if (t instanceof Error) {
				throw (Error)t;
			}
		} finally {
		}
		return status;
	}
	 */
	/*
	 * readEvents() is part of PollableSource implementation and has been deprecated in favor of event driven handling due to performance reasons.
	 * Left here for for completeness sake. 
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private List<Event> readEvents() {
		Event event = null;
		try {
			//			System.out.println("CustomEventSource- readEvents(): "+distributedQueue+" keepListening="+keepListening);
			event = distributedQueue.poll(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			event = null;
		}
		//		System.out.println("CustomEventSource- readEvents(): "+distributedQueue+" keepListening="+keepListening+" got event: "+event);
		if (event == null)
			return null;
		List<Event> events = new ArrayList<>();
		events.add(event);
		distributedQueue.drainTo(events);
		return events;
	}

	/*
	 * PollableSource Implementation, deprecated.
	@Deprecated
	@Override
	public long getBackOffSleepIncrement() {
		return 1;
	}

	@Deprecated
	@Override
	public long getMaxBackOffSleepInterval() {
		return 500;
	}
	 */
}	