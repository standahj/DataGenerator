package com.chute.parser.test.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataSourceClient {

	private static String hostname;
	private static int    port;

	private static long sleepInBetween = 5L;
	
	public DataSourceClient() {
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String lbEndpoint = args != null && args.length > 1 ? args[1] : "localhost:8080";
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
		String sleepSeconds = args != null && args.length > 2 ? args[2] : "5";
		try {
			sleepInBetween = Long.parseLong(sleepSeconds);
		} catch (Exception x) {
			sleepInBetween = 5L;
		}
		
		System.out.println("DataSourceClient: Using Source URL: "+hostname+" port: "+port);
		try {
//			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			final Socket netCatSource = new Socket(hostname, port);
//			System.out.println("Hit <Enter> key to begin:");
			// Producer part
			@SuppressWarnings("unused")
			boolean userInput = true;
			while (userInput) {
				File arg = new File(args[0]);
				System.out.println("DataSourceClient: *********************** sending file: "+arg.getAbsolutePath());
				Date now = new Date();
				try (BufferedReader logData = new BufferedReader(new FileReader(arg))) {
					String data = null;
					int count = 0;
					OutputStream source = netCatSource.getOutputStream(); 
					while ((data = logData.readLine()) != null) {
						try {
							source.write((updateLine(data,now)+"\n").getBytes());
							// 		                    System.out.println("["+data+"]");
							count++;
							source.flush();
//s							Thread.sleep(5);
						} catch (Exception ex) {
							Logger.getLogger(DataSourceClient.class.getName()).log(Level.SEVERE, null, ex);
							System.out.println("DataSourceClient: *********************** Exception: "+ex.getMessage());
						}
					}
					System.out.println("DataSourceClient: *********************** LINES SENT: "+count+" in "+(System.currentTimeMillis()-now.getTime())+"ms");
				}
				System.out.println(">> Ctrl-C to quit:");
				try {
					if (sleepInBetween > 0L) {
						Thread.sleep(sleepInBetween * 1000L);
					}
				} catch (Exception x) {
					userInput = false;
				}
			}
		} catch (Exception se) {
			Logger.getLogger(DataSourceClient.class.getName()).log(Level.SEVERE, "DataSourceClient error while sending log data", se);
			System.out.println("DatasourceClient: *********************** Exception: "+se.getMessage());
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
