package com.leanforge.game;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import java.time.OffsetDateTime;
import java.util.*;

@Configuration
public class MongoConfig extends AbstractMongoConfiguration {

    @Bean
    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(Arrays.asList(
                new OffsetDateTimeToStringConverter(),
                new StringToOffsetDateTimeConverter()
        ));
    }

    @Override
    protected String getDatabaseName() {
        return "locker";
    }

    @Override
    public Mongo mongo() {
        return new MongoClient("mongodb");
    }

    public class OffsetDateTimeToStringConverter implements Converter<OffsetDateTime, String> {

        @Override
        public String convert(OffsetDateTime source) {
            return source == null ? null : source.toString();
        }

    }

    public class StringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {

        @Override
        public OffsetDateTime convert(String source) {
            return source == null ? null : OffsetDateTime.parse(source);
        }

    }
}
