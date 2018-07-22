package com.chute.parser.event.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
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

	public  int 		TRUNCATE 				= 10000;
	public  String 		LINE_BREAKER 			= "([\r\n]+)";
	public  int 		LINE_BREAKER_LOOKBEHIND = 10000;
	public  boolean   	SHOULD_LINEMERGE 		= true;
	public  boolean 	BREAK_ONLY_BEFORE_DATE 	= true;
 	public  int 		MAX_EVENTS 				= -1;  // -1 for all events
 	public  String 		EVENT_BREAKER 			= null;//"Event End"; //"\\x0a{2}";

	public  List<PatternValues> patternValuesList = new ArrayList<>();

	private int pIndex = 0;
    private static EventParserConfig instance = null;

	public EventParserConfig() {
	}

	public void setConfigurationName(String name) {
	    for (int i = 0; i < patternValuesList.size() && name != null; i++) {
	        if (name.equalsIgnoreCase(patternValuesList.get(i).getNAME())) {
	            pIndex = i;
	            break;
            }
        }
    }

	public  String get(String key) {
		switch(key) {
			case "BREAK_ONLY_BEFORE":		return getPatternValuesList().get(pIndex).getBREAK_ONLY_BEFORE();
			case "MUST_BREAK_AFTER":		return getPatternValuesList().get(pIndex).getMUST_BREAK_AFTER();
			case "MUST_NOT_BREAK_AFTER":	return getPatternValuesList().get(pIndex).getMUST_NOT_BREAK_AFTER();
			case "MUST_NOT_BREAK_BEFORE":	return getPatternValuesList().get(pIndex).getMUST_NOT_BREAK_BEFORE();
			case "HEADER_PATTERN": 			return getPatternValuesList().get(pIndex).getHEADER_PATTERN();
			default:
				return null;
		}
	}

	public int getTRUNCATE() {
		return TRUNCATE;
	}

	public void setTRUNCATE(int TRUNCATE) {
		this.TRUNCATE = TRUNCATE;
	}

	public String getLINE_BREAKER() {
		return LINE_BREAKER;
	}

	public void setLINE_BREAKER(String LINE_BREAKER) {
		this.LINE_BREAKER = LINE_BREAKER;
	}

	public int getLINE_BREAKER_LOOKBEHIND() {
		return LINE_BREAKER_LOOKBEHIND;
	}

	public void setLINE_BREAKER_LOOKBEHIND(int LINE_BREAKER_LOOKBEHIND) {
		this.LINE_BREAKER_LOOKBEHIND = LINE_BREAKER_LOOKBEHIND;
	}

	public boolean isSHOULD_LINEMERGE() {
		return SHOULD_LINEMERGE;
	}

	public void setSHOULD_LINEMERGE(boolean SHOULD_LINEMERGE) {
		this.SHOULD_LINEMERGE = SHOULD_LINEMERGE;
	}

	public boolean isBREAK_ONLY_BEFORE_DATE() {
		return BREAK_ONLY_BEFORE_DATE;
	}

	public void setBREAK_ONLY_BEFORE_DATE(boolean BREAK_ONLY_BEFORE_DATE) {
		this.BREAK_ONLY_BEFORE_DATE = BREAK_ONLY_BEFORE_DATE;
	}

	public int getMAX_EVENTS() {
		return MAX_EVENTS;
	}

	public void setMAX_EVENTS(int MAX_EVENTS) {
		this.MAX_EVENTS = MAX_EVENTS;
	}

	public String getEVENT_BREAKER() {
		return EVENT_BREAKER;
	}

	public void setEVENT_BREAKER(String EVENT_BREAKER) {
		this.EVENT_BREAKER = EVENT_BREAKER;
	}

	public List<PatternValues> getPatternValuesList() {
		return patternValuesList;
	}

	public void setPatternValuesList(List<PatternValues> patternValuesList) {
		this.patternValuesList = patternValuesList;
	}

    public static EventParserConfig getInstance() {
	    if (instance == null) {
            Yaml yaml = new Yaml();
            InputStream inputStream = EventParserConfig.class.getResourceAsStream("/config.yml");
            instance = yaml.loadAs(inputStream, EventParserConfig.class);
        }
        return instance;
    }
}
