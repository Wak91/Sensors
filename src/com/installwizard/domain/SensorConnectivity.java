package com.installwizard.domain;

public class SensorConnectivity {

	private Long id;
	private int sendMoteId;
	private int receiverMoteId;
	private double connectivityMeasure;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getSendMoteId() {
		return sendMoteId;
	}

	public void setSendMoteId(int sendMoteId) {
		this.sendMoteId = sendMoteId;
	}

	public int getReceiverMoteId() {
		return receiverMoteId;
	}

	public void setReceiverMoteId(int receiverMoteId) {
		this.receiverMoteId = receiverMoteId;
	}

	public double getConnectivityMeasure() {
		return connectivityMeasure;
	}

	public void setConnectivityMeasure(double connectivityMeasure) {
		this.connectivityMeasure = connectivityMeasure;
	}
}
