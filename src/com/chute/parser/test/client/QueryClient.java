package com.chute.parser.test.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryClient {

	private static String hostname;
	private static int    port;

	public QueryClient() {
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String lbEndpoint = args != null && args.length > 0 ? args[0] : "localhost:8080";
		String[] tokens = lbEndpoint.split(":");
		port = 8080;
		hostname = "localhost";
		if (tokens.length > 1) {
			try {
				String portToken = tokens[tokens.length-1];
				port = Integer.parseInt(portToken);
			} catch (Exception x) {
				port = 8080;
			}
		}
		if (tokens.length == 3) {
			hostname = tokens[0]+":"+tokens[1];
		} else {
			hostname = tokens[0];
		}
		System.out.println("QueryClient: Using Source URL: "+hostname+" port: "+port);
		try {
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			final Socket netCatSource = new Socket(hostname, port);
			System.out.println("Type SOLR query (including the URL part) & Hit <Enter> key to begin:");
			
			Runnable consumer = new Runnable() { 
				@Override
				public void run() {
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader (netCatSource.getInputStream()));
						System.out.println("QueryClient: ************************************************* Waiting: ");
						String data = in.readLine();
						System.out.println("QueryClient: *************************************************\n RECEIVED: "+data);
						while (data != null) {
							data = in.readLine();
							System.out.println("QueryClient: *************************************************\n RECEIVED: "+data);
						}
					} catch (Exception ex) {
						Logger.getLogger(QueryClient.class.getName()).log(Level.SEVERE, null, ex);
					} finally {
						try {
							netCatSource.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			};
			// start the client reading responses in separate thread
			new Thread(consumer).start();
			
			// Producer part
			try (OutputStream source= netCatSource.getOutputStream()) {
				@SuppressWarnings("unused")
				String queryString = stdIn.readLine();
				boolean userInput = queryString != null && queryString.length() > 0;
				int count = 0;
				while (userInput) {
						try {
							source.write((queryString+"\n").getBytes());
							// 		                    System.out.println("["+data+"]");
							count++;
							source.flush();
						} catch (Exception ex) {
							Logger.getLogger(DataSourceClient.class.getName()).log(Level.SEVERE, null, ex);
							System.out.println("QueryClient: *********************** Exception: "+ex.getMessage());
						}
					System.out.println("QueryClient: *********************** Queries SENT: "+count);
					System.out.println(">> Ctrl-C to quit:");
					queryString = stdIn.readLine();
					userInput = queryString != null && queryString.length() > 0;
				}
			}
		} catch (Exception se) {
			Logger.getLogger(DataSourceClient.class.getName()).log(Level.SEVERE, "QueryClient error while sending log data", se);
			System.out.println("QueryClient: *********************** Exception: "+se.getMessage());
		}
	}

	public static final SimpleDateFormat TFMT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	public static String updateLine(String line, Date timestamp) {
		if (line == null || line.indexOf(',') < 0) {
			return line;
		}
		String replacement = line;
		String[] parts = line.split(",");
		boolean altered = false;
		for (int i = 0; i < parts.length; i++) {
			try {
				if (parts[i].length() > 0) {
					long v = Long.parseLong(parts[i]);
					if (v != 0 && v != 1 && v != 175 && v != 900) {
						long m = (long)(v*(Math.random() * 0.4 - 0.2));
						if (m != 0L) {
							parts[i] = Long.toString(v + m);
							altered = true;
						}
					}
				}
			} catch (Exception e) {}
		}
		if (altered) {
			StringBuilder b = new StringBuilder(parts[0]);
			for (int i = 1; i < parts.length; i++) {
				b.append(",").append(parts[i]);
			}
			replacement = b.toString();
		}
		replacement = replacement.replaceAll("#TIMESTAMP#", TFMT.format(timestamp));
//		System.out.println(">>: "+line+"\n<<: "+replacement);
		return replacement;
	}
}
