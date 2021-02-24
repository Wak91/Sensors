package com.installwizard.kafka;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class MessageStorage {
	
	private List<String> storage = new ArrayList<String>();
	
	public void put(String message){
		storage.add(message);
	}
	
	public String toString(){
		StringBuffer info = new StringBuffer();
		storage.forEach(msg->info.append(msg));
		return info.toString();
	}
	
	public void clear(){
		storage.clear();
	}
}
