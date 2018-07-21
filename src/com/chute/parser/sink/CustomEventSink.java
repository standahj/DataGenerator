package com.chute.parser.sink;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomEventSink extends AbstractSink implements Configurable {

//	private boolean keepStreaming = true;
//	private BlockingQueue<Event> distributedQueue;
//	private int maxBytesToLog = 32756;
	private Map<String,String> arguments = new HashMap<>();

	private AbstractSource server = null;
	private AbstractSource client = null;
	
	private static final Logger logger = LoggerFactory.getLogger(CustomEventSink.class);

	/**
	 * Read / translate the Flume-ng config into values used in KV Store
	 */
	public void configure(Context context) {
		arguments.put("address", context.getString("copycatDefaultServerAddress"));
		arguments.put("clientAddress", context.getString("copycatDefaultClientAddress"));
		arguments.put("cluster", context.getString("copycatCluster"));
		arguments.put("persistenceDirectory", context.getString("copycatPersistenceDirectory"));
		arguments.put("databaseFile", context.getString("upscaleDatabaseFile"));
		arguments.put("databaseName", context.getString("upscaleDatabaseName"));
		arguments.put("debug", context.getString("debug"));
	}

	@Override
	public void start() {
		// Initialize the connection to the external repository that
		// this Sink will forward Events to ..
//		server = new DistributedKVStoreServer(new Properties(), arguments, true);
		logger.info("DistributedKVStoreServer initialized,");
//		client = new DistributedKVStoreClient(new Properties(), arguments, true);
		logger.info("DistributedKVStoreClient initialized,");
	}

	@Override
	public void stop () {
		// Disconnect from the external respository and do any
		// additional cleanup (e.g. releasing resources or nulling-out
		// field values) ..
//		server.exit();
		logger.info("CustomEventSink exit() called.");
	}

	public Status process() throws EventDeliveryException {
		Status status = null;

		// Start transaction
		Channel ch = getChannel();
		Transaction txn = ch.getTransaction();
		txn.begin();
		try {
			// This try clause includes whatever Channel operations you want to do
			// The trick part is that the Event here is possibly only partial event, and I may need more of it to combine it to entire Event
			Event event = ch.take();
//			client.submit(event.getBody());
			txn.commit();
			status = Status.READY;
		} catch (Throwable t) {
			txn.rollback();

			// Log exception, handle individual exceptions as needed
			status = Status.BACKOFF;

			// re-throw all Errors
			if (t instanceof Error) {
				throw (Error)t;
			}
		} finally {
			txn.close();
		}
		return status;
	}
}
