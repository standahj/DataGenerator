package com.chute.parser.event.config;


import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlEventParserConfig {

    public YamlEventParserConfig() {

    }

    public YamlEventParserConfig(String resourceFileName) {
        Constructor constructor = new Constructor(EventParserConfig .class);//Car.class is root
        Yaml yaml = new Yaml(constructor);
    }
}
