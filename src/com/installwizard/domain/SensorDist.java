package com.installwizard.domain;

public class SensorDist {

	private Long id;
	private int fromMoteId;
	private int toMoteId;
	private double distance;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getFromMoteId() {
		return fromMoteId;
	}

	public void setFromMoteId(int fromMoteId) {
		this.fromMoteId = fromMoteId;
	}

	public int getToMoteId() {
		return toMoteId;
	}

	public void setToMoteId(int toMoteId) {
		this.toMoteId = toMoteId;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

}
