package com.installwizard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.installwizard.kafka.MessageStorage;

@Service
public class KafkaProducer {
	private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Value("${jsa.kafka.topic}")
	String kafkaTopic;
	
	@Autowired
	MessageStorage storage;
	
	public void send(String message) {
	    log.info("sending data='{}'", message);
	    kafkaTemplate.send(kafkaTopic, message);
	    storage.put(message);
	}
	
}
