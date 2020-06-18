/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.linuxforhealth.connect.processor;

import org.apache.camel.Exchange;
import org.apache.camel.ExtendedExchange;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests {@link FormatMessageProcessor} processor
 */
public class FormatMessageTest extends CamelTestSupport {

    private Exchange mockedExchange;
    private FormatMessageProcessor formatMessage;

    private Exchange createMockExchange() {
        Exchange mockedExchange = new DefaultExchange(context);
        mockedExchange.getIn().setHeader("timestamp", "1592514822");
        mockedExchange.adapt(ExtendedExchange.class).setFromRouteId("hl7-v2-mllp");
        mockedExchange.getIn().setHeader("routeUrl", "netty:tcp://localhost:2575?sync=true&encoders=#hl7encoder&decoders=#hl7decoder");
        mockedExchange.getIn().setHeader("dataStoreUrl", "kafka:HL7v2_ADT?brokers=localhost:9092");
        mockedExchange.getIn().setHeader("dataFormat", "hl7-v2");
        mockedExchange.getIn().setHeader("uuid", "ID-MBP-2-attlocal-net-1592229483323-2-1");
        byte[] data = new byte[] {123, 34, 114, 101, 115, 111};
        mockedExchange.getIn().setBody(data);

        return mockedExchange;
    }

    /**
     * Configures a mocked exchange fixture
     */
    @BeforeEach
    public void beforeEach() {
        mockedExchange = createMockExchange();
        formatMessage = new FormatMessageProcessor();
    }

    /**
     * Tests {@link FormatMessageProcessor#process(Exchange)} to validate that the message body matches an expected result
     */
    @Test
    public void testProcess() {
        formatMessage.process(mockedExchange);
        String expectedBody = "{\"meta\":{\"routeId\":\"hl7-v2-mllp\","+
            "\"uuid\":\"ID-MBP-2-attlocal-net-1592229483323-2-1\","+
            "\"routeUrl\":\"netty:tcp://localhost:2575?sync=true&encoders=#hl7encoder&decoders=#hl7decoder\","+
            "\"dataFormat\":\"hl7-v2\",\"timestamp\":\"1592514822\","+
            "\"dataStoreUrl\":\"kafka:HL7v2_ADT?brokers=localhost:9092\"},"+
            "\"data\":[123, 34, 114, 101, 115, 111]}";
        String actualBody = mockedExchange.getIn().getBody(String.class);
        Assertions.assertEquals(expectedBody, actualBody);
    }
}
