package com.javamicroservices.commonservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.axonframework.serialization.xml.CompactDriver;

import com.thoughtworks.xstream.XStream;

@Configuration 
public class AxonConfig {

    @Bean 
    public XStream xStream() {
        XStream xStream = new XStream(new CompactDriver());
        xStream.allowTypesByWildcard(new String[] { "com.javamicroservices.**" });
        return xStream;
    }
}
