package com.chute.parser.event;

import com.chute.parser.event.config.EventParserConfig;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventParser {

	private enum BREAK_TYPE { BEFORE, AFTER };
	private static Logger logger = LoggerFactory.getLogger(EventParser.class);
	private static int logLevel = 0;  // set to 1 to enable debug output to console
	EventParserConfig eventParserConfig = EventParserConfig.getInstance();

	private Pattern eventBreakerPattern = eventParserConfig.getEVENT_BREAKER() != null ? Pattern.compile(eventParserConfig.getEVENT_BREAKER(), Pattern.DOTALL) : null;
	private Pattern lineBreakerPattern = Pattern.compile(eventParserConfig.getLINE_BREAKER(), Pattern.DOTALL);

	// these are initialized per selected config type
	private Pattern breakOnlyBeforePattern = null;
	private Pattern mustBreakAfterPattern = null;
	private Pattern mustNotBreakAfterPattern = null;
	private Pattern mustNotBreakBeforePattern = null;
	private Pattern headerPattern = null;
	
	private String carryOverChunk = "";
	private int lc = 0;
	private String eventIdentifier = null;

	public EventParser() {
	    init();
	    info();
	}

	public EventParser(String name) {
	    eventParserConfig.setConfigurationName(name);
	    init();
	    info();
	}

	private void init() {
        breakOnlyBeforePattern = eventParserConfig.get("BREAK_ONLY_BEFORE") != null ? Pattern.compile(eventParserConfig.get("BREAK_ONLY_BEFORE"), Pattern.DOTALL) : null;
        mustBreakAfterPattern = eventParserConfig.get("MUST_BREAK_AFTER") != null ? Pattern.compile(eventParserConfig.get("MUST_BREAK_AFTER"), Pattern.DOTALL) : null;
        mustNotBreakAfterPattern = eventParserConfig.get("MUST_NOT_BREAK_AFTER") != null ? Pattern.compile(eventParserConfig.get("MUST_NOT_BREAK_AFTER"), Pattern.DOTALL) : null;
        mustNotBreakBeforePattern = eventParserConfig.get("MUST_NOT_BREAK_BEFORE") != null ? Pattern.compile(eventParserConfig.get("MUST_NOT_BREAK_BEFORE"), Pattern.DOTALL) : null;
        headerPattern = eventParserConfig.get("HEADER_PATTERN") != null ? Pattern.compile(eventParserConfig.get("HEADER_PATTERN"), Pattern.DOTALL) : null;
    }

	private void info() {
        LOG("eventBreakerPattern = "+NVL(eventBreakerPattern));
        LOG("lineBreakerPattern = "+NVL(lineBreakerPattern));
        LOG("breakOnlyBeforePattern = "+NVL(breakOnlyBeforePattern));
        LOG("mustBreakAfterPattern = "+NVL(mustBreakAfterPattern));
        LOG("mustNotBreakAfterPattern = "+NVL(mustNotBreakAfterPattern));
        LOG("mustNotBreakBeforePattern = "+NVL(mustNotBreakBeforePattern));
        LOG("headerPattern = "+NVL(headerPattern));
    }

	private String NVL(Pattern p) { return p != null ? p.pattern() : "null"; }
	/**
	 * Modified method that uses String input to which it applies RegExp to extract single/multiline 
	 * 
	 * @param unprocessedData  data to process (contains only unprocessed data)
	 * @param distributedQueue   output writer
	 * 
	 * @return  number of events written
	 * 
	 * @throws IOException
	 */
	public int parseEventsString(String unprocessedData, Collection<Event> distributedQueue) throws IOException {
		// initialize Pattern variables
		// process data
		// if there is clear event pattern defined, use it rather then merging event body line-by-line
		if (eventBreakerPattern != null) {
			return parseEvents(unprocessedData, eventBreakerPattern.matcher(unprocessedData), distributedQueue);
		}
		Matcher lineBreakerMatcher = lineBreakerPattern.matcher(unprocessedData);
		// If we wish to handle only single line events, it is the same as if we had event_breaker defined to be EOL char
		if (lineBreakerPattern != null && !eventParserConfig.isSHOULD_LINEMERGE()) {
			return parseEvents(unprocessedData, lineBreakerMatcher, distributedQueue);
		}

		// At this point we know that multi-line events are requested (SHOULD_LINEMERGE= true)
		int numProcessed = 0;
		String line = "";
		int position = 0;
		boolean lineBreakerMatcherFind = lineBreakerMatcher.find();
		String lineBreakReplacement = lineBreakerMatcherFind && lineBreakerMatcher.groupCount() > 0 ? lineBreakerMatcher.group(1) : "";
		boolean enterLoop = true; //make sure that this is true first time so the loop enters at least one time 
        LOG("LINE-unprocessedData ("+unprocessedData.length()+"): "+unprocessedData+" \nlineBreakerMatcherFind="+(lineBreakerMatcherFind || numProcessed == 0));
		while (lineBreakerMatcherFind || enterLoop) {
//			LOG("position:"+position+", lineBreakerMatcher.start():"+(lineBreakerMatcherFind ? lineBreakerMatcher.start() : unprocessedData.length()-1)+", unprocessedData.lenth():"+unprocessedData.length());
			line = carryOverChunk + line + (lineBreakerMatcherFind ?
					(unprocessedData.substring(position, lineBreakerMatcher.start()) + lineBreakReplacement) :
					 unprocessedData.substring(position));
            position = lineBreakerMatcherFind ? lineBreakerMatcher.end() : unprocessedData.length();
            carryOverChunk = "";
            LOG("LINE ("+(lc++)+"/"+line.length()+"): ["+line+"]");
            // test for the breaker conditions
            // Note: each condition regexp can match multiple times on one line, thus collect those to array
            List<MatchedRegion> breakOnlyBeforeList 	= new ArrayList<>();
            List<MatchedRegion> mustBreakAfterList 		= new ArrayList<>();
            List<MatchedRegion> mustNotBreakAfterList 	= new ArrayList<>();
            List<MatchedRegion> mustNotBreakBeforeList 	= new ArrayList<>();
            
            if (breakOnlyBeforePattern != null) {
            	Matcher breakOnlyBeforeMatcher 		= breakOnlyBeforePattern.matcher(line);
	            while (breakOnlyBeforeMatcher.find()) {
	            	if (eventIdentifier == null) {
	            		eventIdentifier = breakOnlyBeforeMatcher.group();
	            	}
	            	LOG("BREAK_ONLY_BEFORE: "+breakOnlyBeforeMatcher.start()+", "+breakOnlyBeforeMatcher.end()+"  "+eventIdentifier);
	            	breakOnlyBeforeList.add(new MatchedRegion(breakOnlyBeforeMatcher.start(), breakOnlyBeforeMatcher.end(), BREAK_TYPE.BEFORE));
	            }
            }
            if (mustBreakAfterPattern != null) {
            	Matcher mustBreakAfterMatcher 		= mustBreakAfterPattern.matcher(line);
	            while (mustBreakAfterMatcher.find()) {
	            	mustBreakAfterList.add(new MatchedRegion(mustBreakAfterMatcher.start(), mustBreakAfterMatcher.end(), BREAK_TYPE.AFTER));
	            	LOG("MUST_BREAK_AFTER: "+mustBreakAfterMatcher.pattern());
	            	LOG("MUST_BREAK_AFTER: "+mustBreakAfterMatcher.start()+", "+ mustBreakAfterMatcher.end()+"  "+mustBreakAfterMatcher.group());
	            	for (int i = 0; i <= mustBreakAfterMatcher.groupCount(); i++) {
	            		LOG("MUST_BREAK_AFTER group("+i+"): "+mustBreakAfterMatcher.group(i));
	            	}
	            }
            }
            if (mustNotBreakAfterPattern != null) {
            	Matcher mustNotBreakAfterMatcher 		= mustNotBreakAfterPattern.matcher(line);
	            while (mustNotBreakAfterMatcher.find()) {
	            	mustNotBreakAfterList.add(new MatchedRegion(mustNotBreakAfterMatcher.start(), mustNotBreakAfterMatcher.end(), BREAK_TYPE.AFTER));
	            	LOG("MUST_NOT_BREAK_AFTER: "+mustNotBreakAfterMatcher.start()+", "+mustNotBreakAfterMatcher.end()+"  "+mustNotBreakAfterMatcher.group());
	            }
            }
            if (mustNotBreakBeforePattern != null) {
            	Matcher mustNotBreakBeforeMatcher 		= mustNotBreakBeforePattern.matcher(line);
	            while (mustNotBreakBeforeMatcher.find()) {
	            	mustNotBreakBeforeList.add(new MatchedRegion(mustNotBreakBeforeMatcher.start(), mustNotBreakBeforeMatcher.end(), BREAK_TYPE.BEFORE));
	            	LOG("MUST_NOT_BREAK_BEFORE: "+mustNotBreakBeforeMatcher.start()+","+ mustNotBreakBeforeMatcher.end()+"  "+mustNotBreakBeforeMatcher.group());
	            }
            }
            
            // MustNot condition takes precedence over break-on condition 
            List<MatchedRegion> breakOnlyBeforeListCopy 	= new ArrayList<>(breakOnlyBeforeList.size());
            breakOnlyBeforeListCopy.addAll(breakOnlyBeforeList);
            if (breakOnlyBeforeList.size() > 0) {
            	removeBreaksClashingWithMustNotCondition(breakOnlyBeforeList, mustNotBreakAfterList, mustNotBreakBeforeList);
            }
            if (mustBreakAfterList.size() > 0) {
            	removeBreaksClashingWithMustNotCondition(mustBreakAfterList, mustNotBreakAfterList, mustNotBreakBeforeList);
            }
            
            // Now determine the break points
            List<Integer> breakPoints = new ArrayList<>(breakOnlyBeforeList.size() + mustBreakAfterList.size());
            if (breakOnlyBeforeList.size() > 0) {
            	for (MatchedRegion breakBefore : breakOnlyBeforeList) {
            		addBreakPoint(breakPoints,breakBefore.start);
            	}
            }
            if (mustBreakAfterList.size() > 0) {
            	for (MatchedRegion breakAfter : mustBreakAfterList) {
            		MatchedRegion breakBefore = null;
            		/*
            		 * The negative back-reference will report match after first non-matching character, which means that the break-point may be set in middle of the word.
            		 * if using BREAK_ONLY_BEFORE most likely this token will overlap the same part and properly identify the break-point region, 
            		 * so I will lookup that BREAK_ONLY_BEFORE and if exist use it as breakpoint instead  
            		 */
            		for (int b = breakOnlyBeforeListCopy.size() - 1; b >= 0; b--) {
            			if (breakOnlyBeforeListCopy.get(b).end >= breakAfter.end && breakOnlyBeforeListCopy.get(b).start >= breakAfter.start && breakOnlyBeforeListCopy.get(b).start <= breakAfter.end) {
            				breakBefore = breakOnlyBeforeListCopy.get(b);
            				break;
            			}
            		}
            		if (breakBefore == null ) {
            			addBreakPoint(breakPoints,breakAfter.end);
            		} else {
            			if (!breakPoints.contains(breakBefore.start)) {
            				addBreakPoint(breakPoints,breakBefore.start);
            			}
            		}
            	}
            }
            // Sort breakpoints using natural (ascending) order which is the default sorting algorithm here:
            Collections.sort(breakPoints);
            int breakPosition = 0;
            if (breakPoints.size() > 0) {
	            for (Integer breakIndex : breakPoints) {
	            	if (breakIndex > breakPosition && breakIndex < line.length()) {
	            		String eventBody = line.substring(breakPosition, breakIndex);
	            		Matcher cleanser = breakOnlyBeforePattern.matcher(eventBody);
	            		if (cleanser.find()) {
	            			eventIdentifier = cleanser.group();
	            			StringBuilder eventIdentifierReplacement = new StringBuilder(); // allow the parts of identifier to become part of the event body
	            			int maxEnd = -1;
	            			if (cleanser.groupCount() > 0) {
	            				for (int g = 1; g <= cleanser.groupCount(); g++) {
	            					if (cleanser.start(g) >= maxEnd) {
	            						eventIdentifierReplacement.append(cleanser.group(g));
	            						maxEnd = cleanser.end(g);
	            					}
	            				}
	            			}
	            			eventBody = eventBody.replaceAll(eventParserConfig.get("BREAK_ONLY_BEFORE"), eventIdentifierReplacement.toString());
	            		}
	            		eventBody = eventBody.replaceAll("(?s)"+lineBreakReplacement+"$", ""); // remove last replacement
	                    Event event = EventBuilder.withBody(eventBody, Charset.forName("UTF-8"));
        				Map<String, String> headerMap = new HashMap<>();
	                    if (headerPattern != null) {
	            			Matcher headers = headerPattern.matcher(eventBody);
	            			while (headers.find()) {
	            				headerMap.put(headers.group(1), headers.group(2));
	            			}
	                    } else {
	            			String[] headers = eventIdentifier.split("[ \t]+");
	        				int hIndex = 0;
	            			for (String header : headers) {
	            				headerMap.put("H"+(hIndex++), header);
	            			}
	                    }
						event.setHeaders(headerMap);
	                    LOG("*****************************************************************\nBODY:("+breakPosition+", "+breakIndex+") [["+eventBody+"]]");
	        		    numProcessed++;
	        		    distributedQueue.add(event);
	            		breakPosition = breakIndex;
	            	}
	            }
            } else {
            	/*
                Event event = EventBuilder.withBody(line, Charset.forName("UTF-8"));;
                System.out.println("BODY:("+breakPosition+", to-end) [["+line+"]]");
    		    numProcessed++;
    		    distributedQueue.add(event);
    		    */
            	carryOverChunk = line;
            }
            if (breakPoints.size() > 0 && breakPosition < line.length() - 1) {
            	carryOverChunk = line.substring(breakPosition);
            	if (carryOverChunk.length() > eventParserConfig.getLINE_BREAKER_LOOKBEHIND()) {
            		carryOverChunk = carryOverChunk.substring(carryOverChunk.length()- eventParserConfig.getLINE_BREAKER_LOOKBEHIND());
            	}
            	LOG("CarryOver CHUNK: "+carryOverChunk);
            	line = "";
            	breakPoints.clear();
            }
            
            if (eventParserConfig.getMAX_EVENTS() > 0 && numProcessed >= eventParserConfig.getMAX_EVENTS()) {
            	break;
            }
            enterLoop = position < unprocessedData.length();
            lineBreakerMatcherFind = lineBreakerMatcher.find();
		}
		LOG("parseEventsString() : num="+numProcessed+" queue.size="+distributedQueue.size());
		return numProcessed;
	}
	
	private void addBreakPoint(List<Integer> breakPoints, int end) {
		breakPoints.add(end);
		LOG("Add breakpoint: "+end);
	}

	private void removeBreaksClashingWithMustNotCondition(List<MatchedRegion> breakList, List<MatchedRegion> mustNotBreakAfterList, List<MatchedRegion> mustNotBreakBeforeList) {
        if (breakList.size() > 0 && (mustNotBreakAfterList.size() > 0 || mustNotBreakBeforeList.size() > 0)) {
        	// detected before-breaker, need to cross-check it with MustNot conditions
        	List<MatchedRegion> copy = new ArrayList<>(breakList.size());
        	copy.addAll(breakList);
        	if (mustNotBreakAfterList.size() > 0) {
            	for (MatchedRegion region : copy) {
            		for (MatchedRegion notAfter : mustNotBreakAfterList) {
            			if (region.overlap(notAfter)  && region.end <= notAfter.end) {
            				breakList.remove(region);
            				LOG("REMOVING BREAKER DUE TO OVERLap "+region.start+"/"+region.end);
            				break;
            			}
            		}
            	}
            	copy.clear();
            	copy.addAll(breakList);  // reset the copy for next loop
        	}
        	if (mustNotBreakBeforeList.size() > 0) {
            	for (MatchedRegion region : copy) {
            		for (MatchedRegion notBefore : mustNotBreakBeforeList) {
            			if (region.overlap(notBefore) && region.start >= notBefore.start) {
            				breakList.remove(region);
            				LOG("REMOVING BREAKER DUE TO OVERLap "+region.start+"/"+region.end);
            				break;
            			}
            		}
            	}
        	}
        }
	}
	
	private int parseEvents(String unprocessedData, Matcher eventBreakerMatcher, Collection<Event> distributedQueue) throws IOException {
		int numProcessed = 0;
		int position = 0;
		String line = "";
		LOG("eventBreakerMatcher/parseEvents: "+(lc++)+" UNPROCESSED: [["+unprocessedData+"]]");
		while (eventBreakerMatcher.find()) {
            if (eventParserConfig.getMAX_EVENTS() >= 0 && numProcessed >= eventParserConfig.getMAX_EVENTS()) {
            	break;
            }
			line = carryOverChunk + unprocessedData.substring(position, eventBreakerMatcher.start());
			carryOverChunk = "";
            Event event = EventBuilder.withBody(line.getBytes());
			Map<String, String> headerMap = new HashMap<>();
			Matcher headers = headerPattern.matcher(line);
			while (headers.find()) {
				headerMap.put(headers.group(1), headers.group(2));
			}
			event.setHeaders(headerMap);

		    numProcessed++;
		    distributedQueue.add(event);
            position = eventBreakerMatcher.end();
		}
		if (position < unprocessedData.length()) {
			carryOverChunk += unprocessedData.substring(position);
		}
		return numProcessed;
	}

	/**
	 * Original methods that uses ByteBuffer (modified from the Upwork task definition to avoid converting 
	 * byte[] to char[] and back to byte[] when reading from file
	 * 
	 * @param bytes  ByteBuffer with pos and limit properties defining the unprocessed data region 
	 * @param distributedQueue Result write
	 * 
	 * @return number of events written
	 * 
	 * @throws IOException
	 */
	public int processEvents(ByteBuffer bytes, BlockingQueue<Event> distributedQueue)  throws IOException {

	      int numProcessed = 0;

	      boolean foundNewLine = true;
	      while (foundNewLine) {
	        foundNewLine = false;

	        int limit = bytes.limit();
	        int mark = bytes.position();
	        for (int pos = bytes.position(); pos < limit; pos++) {
	          if (bytes.get(pos) == '\n') {

	        	// build event object
	            byte[] body = new byte[pos - mark];
	            bytes.get(body);
	            Event event = EventBuilder.withBody(body);

	            // process event
			    numProcessed++;
			    distributedQueue.add(event);
			    
	            // advance position after data is consumed
	            bytes.position(pos + 1); // skip newline
	            foundNewLine = true;

	            break;
	          }
	        }

	      }

	      return numProcessed;
	    }
	
		/**
		 * A utility class to help track matched breakers
		 * @author shejny
		 *
		 */
		public class MatchedRegion {
			public int start, end, breakPoint;
			public BREAK_TYPE type;
			public MatchedRegion(int start, int end, BREAK_TYPE type) {
				this.start 	= start;
				this.end 	= end;
				this.type	= type;
				breakPoint  = type == BREAK_TYPE.BEFORE ? start : end;
			}
			public boolean overlap(MatchedRegion other) {
				boolean isOverlap = other != null;
				if (isOverlap) {
					isOverlap = (start >= other.start && start <= other.end) || 
								(start <= other.start && end   >= other.end) ||
								(end   >= other.start && end   <= other.end) ||
								(start >= other.start && end   <= other.end);
				}
				return isOverlap;
			}
			@Override
			public boolean equals(Object other) {
				return MatchedRegion.class.isInstance(other) && start == ((MatchedRegion)other).start && end == ((MatchedRegion)other).end;
			}
			public void merge(MatchedRegion other) {
				start = other.start < start ? other.start : start;
				end   = other.end   > end   ? other.end   : end;
			}
		}
		
		private static void LOG(String msg) {
			if (logLevel  == 1) {
				System.out.println(msg);
			}
			logger.debug(msg);
		}
	}
