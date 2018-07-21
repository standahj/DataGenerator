package com.chute.parser.event.config;

public class PatternValues {

    public static String    NAME;
    /*
     * Windows Log
     *
    public static String 	BREAK_ONLY_BEFORE     	= "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+(\\d+\\s+\\w+\\s+\\w+)";  // detect the event identifier on the line (multiple matches for merged line possible)
    public static String	MUST_BREAK_AFTER     	= "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+(\\d+\\s+\\w+\\s+)(\\w+)((?:(?!\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+\\1).*)\\1)(?:(?!\\2).)";  // detect event identifier change
    public static String 	MUST_NOT_BREAK_AFTER 	= "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+(\\d+\\s+\\w+\\s+)(\\w+)((?:(?!\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+\\1).*)\\1)\\2";  // ensure that the same type of event identifier causes event continuation
    public static String 	MUST_NOT_BREAK_BEFORE 	= "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+(\\d+\\s+\\w+\\s+PT)";  // 'Report' event use PT subtype as event continuation line, so do not start new event for PT

    public static String    HEADER_PATTERN			= "([\\w]+)=([^\n]+)\n";
     */
	/*
	 * oracle_audit_log
	 */
    public static String 	BREAK_ONLY_BEFORE     	= "\\w{3}\\s+\\w{3}\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2}\\s+\\d{4}\\s+[\\+-]\\d{2}:\\d{2}";  // detect the event identifier on the line (multiple matches for merged line possible)
    public static String	MUST_BREAK_AFTER     	= null;
    public static String 	MUST_NOT_BREAK_AFTER 	= null;
    public static String 	MUST_NOT_BREAK_BEFORE 	= null;

    public static String    HEADER_PATTERN			= "[\r\n]*([\\s\\w\\$]+):\\[*\\d*\\]*\\s+[\"']([^\"']+)[\"']";

    public PatternValues() {

    }
}
