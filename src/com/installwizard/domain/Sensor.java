package com.installwizard.domain;

public class Sensor {

	private int moteid;
	private double temperatureAccuracy;
	private double temperatureDisponsibility;
	private double temperaturePrecision;
	private double humidityAccuracy;
	private double humidityDisponsibility;

	private double humidityPrecision;
	private double lightAccuracy;
	private double lightDisponsibility;
	private double lightPrecision;

	public int getMoteid() {
		return moteid;
	}

	public double getTemperatureAccuracy() {
		return temperatureAccuracy;
	}

	public void setTemperatureAccuracy(double temperatureAccuracy) {
		this.temperatureAccuracy = temperatureAccuracy;
	}

	public double getTemperatureDisponsibility() {
		return temperatureDisponsibility;
	}

	public void setTemperatureDisponsibility(double temperatureDisponsibility) {
		this.temperatureDisponsibility = temperatureDisponsibility;
	}

	public double getTemperaturePrecision() {
		return temperaturePrecision;
	}

	public void setTemperaturePrecision(double temperaturePrecision) {
		this.temperaturePrecision = temperaturePrecision;
	}

	public double getHumidityAccuracy() {
		return humidityAccuracy;
	}

	public void setHumidityAccuracy(double humidityAccuracy) {
		this.humidityAccuracy = humidityAccuracy;
	}

	public double getHumidityDisponsibility() {
		return humidityDisponsibility;
	}

	public void setHumidityDisponsibility(double humidityDisponsibility) {
		this.humidityDisponsibility = humidityDisponsibility;
	}

	public double getHumidityPrecision() {
		return humidityPrecision;
	}

	public void setHumidityPrecision(double humidityPrecision) {
		this.humidityPrecision = humidityPrecision;
	}

	public double getLightAccuracy() {
		return lightAccuracy;
	}

	public void setLightAccuracy(double ligitAccuracy) {
		this.lightAccuracy = ligitAccuracy;
	}

	public double getLightDisponsibility() {
		return lightDisponsibility;
	}

	public void setLightDisponsibility(double lightDisponsibility) {
		this.lightDisponsibility = lightDisponsibility;
	}

	public double getLightPrecision() {
		return lightPrecision;
	}

	public void setLightPrecision(double lightPrecision) {
		this.lightPrecision = lightPrecision;
	}

	public void setMoteid(int moteid) {
		this.moteid = moteid;
	}

}
