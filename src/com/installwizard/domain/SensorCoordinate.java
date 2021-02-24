package com.installwizard.domain;

public class SensorCoordinate {

	private Long id;
	private int moteid;
	private double xcoordinate;
	private double ycoordinate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getMoteid() {
		return moteid;
	}

	public void setMoteid(int moteid) {
		this.moteid = moteid;
	}

	public double getXcoordinate() {
		return xcoordinate;
	}

	public void setXcoordinate(double xcoordinate) {
		this.xcoordinate = xcoordinate;
	}

	public double getYcoordinate() {
		return ycoordinate;
	}

	public void setYcoordinate(double ycoordinate) {
		this.ycoordinate = ycoordinate;
	}

}
