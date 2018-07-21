package com.chute.parser.test.client;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ParserTest {

    public ParserTest() {

    }

    static ParserTest instance = new ParserTest();

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        Yaml yaml = new Yaml();
        InputStream inputStream = instance.getClass().getResourceAsStream("/config.yml");
        Object cfg = yaml.load(inputStream);
        System.out.println(cfg);
    }
}
