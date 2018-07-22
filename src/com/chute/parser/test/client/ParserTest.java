package com.chute.parser.test.client;

import com.chute.parser.event.config.EventParserConfig;

public class ParserTest {

    public ParserTest() { }

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        EventParserConfig cfg = EventParserConfig.getInstance();
        System.out.println(cfg);
    }
}
