package com.chute.parser.event.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Default values for CustomEventParser/EventParser classes. 
 * All values can be overridden by putting identically named property to flume-ng config properties file.
 * 
 * @author shejny
 *
 */
public class EventParserConfig {

	public static int 		TRUNCATE 				= 10000;
	public static String 	LINE_BREAKER 			= "([\r\n]+)";
	public static int 		LINE_BREAKER_LOOKBEHIND = 10000;
	public static boolean   SHOULD_LINEMERGE 		= true;
	public static boolean 	BREAK_ONLY_BEFORE_DATE 	= true;
 	public static int 		MAX_EVENTS 				= -1;  // -1 for all events
 	public static String 	EVENT_BREAKER 			= null;//"Event End"; //"\\x0a{2}";

	public static List<PatternValues> patternValuesList = new ArrayList<>();

	public EventParserConfig() {
	}

	public static String get(String key) {
		switch(key) {
			case "BREAK_ONLY_BEFORE":		return patternValuesList.get(0).BREAK_ONLY_BEFORE;
			case "MUST_BREAK_AFTER":		return patternValuesList.get(0).MUST_BREAK_AFTER;
			case "MUST_NOT_BREAK_AFTER":	return patternValuesList.get(0).MUST_NOT_BREAK_AFTER;
			case "MUST_NOT_BREAK_BEFORE":	return patternValuesList.get(0).MUST_NOT_BREAK_BEFORE;
			case "HEADER_PATTERN": 			return patternValuesList.get(0).HEADER_PATTERN;
			default:
				return null;
		}
	}
}
