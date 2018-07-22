package com.chute.parser.event.config;

public class PatternValues {

    public  String    NAME;
    /*
     * Windows Log
     *
    public  String 	BREAK_ONLY_BEFORE     	= "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+(\\d+\\s+\\w+\\s+\\w+)";  // detect the event identifier on the line (multiple matches for merged line possible)
    public  String	MUST_BREAK_AFTER     	= "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+(\\d+\\s+\\w+\\s+)(\\w+)((?:(?!\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+\\1).*)\\1)(?:(?!\\2).)";  // detect event identifier change
    public  String 	MUST_NOT_BREAK_AFTER 	= "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+(\\d+\\s+\\w+\\s+)(\\w+)((?:(?!\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+\\1).*)\\1)\\2";  // ensure that the same type of event identifier causes event continuation
    public  String 	MUST_NOT_BREAK_BEFORE 	= "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{3}\\s+(\\d+\\s+\\w+\\s+PT)";  // 'Report' event use PT subtype as event continuation line, so do not start new event for PT

    public  String    HEADER_PATTERN			= "([\\w]+)=([^\n]+)\n";
     */
	/*
	 * oracle_audit_log
	 */
    public  String 	BREAK_ONLY_BEFORE     	= "\\w{3}\\s+\\w{3}\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2}\\s+\\d{4}\\s+[\\+-]\\d{2}:\\d{2}";  // detect the event identifier on the line (multiple matches for merged line possible)
    public  String	MUST_BREAK_AFTER     	= null;
    public  String 	MUST_NOT_BREAK_AFTER 	= null;
    public  String 	MUST_NOT_BREAK_BEFORE 	= null;

    public  String    HEADER_PATTERN			= "[\r\n]*([\\s\\w\\$]+):\\[*\\d*\\]*\\s+[\"']([^\"']+)[\"']";

    public PatternValues() {

    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getBREAK_ONLY_BEFORE() {
        return BREAK_ONLY_BEFORE;
    }

    public void setBREAK_ONLY_BEFORE(String BREAK_ONLY_BEFORE) {
        this.BREAK_ONLY_BEFORE = BREAK_ONLY_BEFORE;
    }

    public String getMUST_BREAK_AFTER() {
        return MUST_BREAK_AFTER;
    }

    public void setMUST_BREAK_AFTER(String MUST_BREAK_AFTER) {
        this.MUST_BREAK_AFTER = MUST_BREAK_AFTER;
    }

    public String getMUST_NOT_BREAK_AFTER() {
        return MUST_NOT_BREAK_AFTER;
    }

    public void setMUST_NOT_BREAK_AFTER(String MUST_NOT_BREAK_AFTER) {
        this.MUST_NOT_BREAK_AFTER = MUST_NOT_BREAK_AFTER;
    }

    public String getMUST_NOT_BREAK_BEFORE() {
        return MUST_NOT_BREAK_BEFORE;
    }

    public void setMUST_NOT_BREAK_BEFORE(String MUST_NOT_BREAK_BEFORE) {
        this.MUST_NOT_BREAK_BEFORE = MUST_NOT_BREAK_BEFORE;
    }

    public String getHEADER_PATTERN() {
        return HEADER_PATTERN;
    }

    public void setHEADER_PATTERN(String HEADER_PATTERN) {
        this.HEADER_PATTERN = HEADER_PATTERN;
    }
}
