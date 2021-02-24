package com.installwizard.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.installwizard.domain.Sensor;
import com.installwizard.domain.SensorCoordinate;
import com.installwizard.domain.SensorData;
import com.installwizard.domain.SensorDist;
import com.installwizard.kafka.MessageStorage;
import com.installwizard.service.KafkaProducer;
import com.installwizard.service.SensorService;

//@RestController
//@RequestMapping(value="/jsa/kafka")
@Controller
public class MessageController {

	private static final String DIMENSION_TEMPERATURE = "temperature";
	private static final String DIMENSION_HUMIDITY = "humidity";
	private static final String DIMENSION_LIGHT = "light";

	@Autowired
	KafkaProducer producer;

	@Autowired
	MessageStorage storage;

	@Autowired
	SensorService sensorService;

	/*
	 * search raw data using kafka
	 */

	// @GetMapping(value="/producer")
	@RequestMapping(value = "/producer", method = { RequestMethod.POST, RequestMethod.GET })
	public String producer(HttpServletRequest request, HttpServletResponse response, Model model) {
		// producer.send(data);

		String startDate = request.getParameter("startdate");
		String endDate = request.getParameter("enddate");
		String sensorselected = request.getParameter("sensor");
		String startTime = request.getParameter("starttime");
		String endTime = request.getParameter("endtime");

		// for auto refresh of web page
		
		String refresh = request.getParameter("refresh");

		if (!refresh.equals("")) {
			startDate = (String) request.getSession().getAttribute("startDate");
			endDate = (String) request.getSession().getAttribute("endDate");
			sensorselected = (String) request.getSession().getAttribute("sensorselected");
			startTime = (String) request.getSession().getAttribute("startTime");
			endTime = (String) request.getSession().getAttribute("endTime");
		}
		
		if (startDate == null || startTime == null || startDate.equals("") || startTime.equals("")) {
			List<SensorCoordinate> sensors = sensorService.findAllSensor();
			model.addAttribute("sensors", sensors);
			return "indexkafka";
		}

		// increment hour by 1 from session data for refresh

		if (!refresh.equals("") && startTime != null && !startTime.equals("")) {
			Integer minute = Integer.parseInt(startTime.substring(4, 5));
			Integer hour = Integer.parseInt(startTime.substring(1, 2));
			minute = minute + 1;
			if (minute == 60) {
				minute = 0;
				hour = hour + 1;
				if (hour == 24) {
					hour = hour - 1;
				}
			}
			startTime = String.format("%02d", hour) + ":" + String.format("%02d", minute);
		}

		if (startDate != null && !startDate.equals("")) {
			endDate = startDate;
		}

		if (startTime == null || startTime.equals("")) {
			startTime = "01:01";
		}
		endTime = startTime;
		
		model.addAttribute("stdate", startDate);
		model.addAttribute("enddate", endDate);
		model.addAttribute("sensorselected", Integer.parseInt(sensorselected));
		model.addAttribute("sttime", startTime);
		model.addAttribute("endtime", endTime);
		request.getSession().setAttribute("startDate", startDate);
		request.getSession().setAttribute("endDate", endDate);
		request.getSession().setAttribute("sensorselected", sensorselected);
		request.getSession().setAttribute("startTime", startTime);
		request.getSession().setAttribute("endTime", endTime);

//		Properties prop = new Properties();
//		String configpath = new File(".").getAbsolutePath();
//		try {
//			prop.load(new FileInputStream(configpath + "/config.properties"));
//		} catch (IOException e3) {
//			e3.printStackTrace();
//		}
//		Double distance = Double.parseDouble(prop.getProperty("distancebetweensensors"));
//		int disponibilityexpectedforonehour = Integer.parseInt(prop.getProperty("disponibilityexpectedforonehour"));
		int disponibilityexpectedforonehour = 5;
		Double distance = (double) 4000; 
		// fetch the raw data

		List<SensorData> sensorDataList = sensorService.findCriteriaSensorData(startDate, endDate, startTime, endTime,
				sensorselected);
		model.addAttribute("sensordata", sensorDataList);
		List<SensorCoordinate> sensors = sensorService.findAllSensor();
		model.addAttribute("sensors", sensors);

		// calculate quality measure

		List<Sensor> qualityMeasures = new ArrayList<Sensor>();
		if (sensorDataList != null && sensorDataList.size() > 0) {
			qualityMeasures = getQualityMeasures(sensorselected, sensors, startDate, endDate, distance,
					disponibilityexpectedforonehour, startTime);
		}

		Double totalTemperatureAccuracy = 0.0;
		Double totalTemperatureDisponibility = 0.0;
		Double totalTemperaturePrecision = 0.0;
		Double totalHumidityAccuracy = 0.0;
		Double totalHumidityDisponibility = 0.0;
		Double totalHumidityPrecision = 0.0;
		Double totalLightAccuracy = 0.0;
		Double totalLightDisponibility = 0.0;
		Double totalLightPrecision = 0.0;

		// kafka message
		
		// averaage quality measure
		
		List<Sensor> quality = new ArrayList<Sensor>();

		for (Sensor sensor : qualityMeasures) {
			Gson gson = new Gson();
			String jsonString = gson.toJson(sensor);

			producer.send(jsonString);

			String messages = storage.toString();
			try {
				Files.write(Paths.get("C:\\Users\\mhlaca\\Desktop\\test.txt"), startTime.getBytes(), StandardOpenOption.APPEND);
			    Files.write(Paths.get("C:\\Users\\mhlaca\\Desktop\\test.txt"), messages.getBytes(), StandardOpenOption.APPEND);
			}catch (IOException e) {
			    System.out.println("errore dati");
			}
			
			Sensor sensorTemp = gson.fromJson(messages, Sensor.class);

			totalTemperatureAccuracy = totalTemperatureAccuracy + sensor.getTemperatureAccuracy();
			totalTemperatureDisponibility = totalTemperatureDisponibility + sensor.getTemperatureDisponsibility();
			totalTemperaturePrecision = totalTemperaturePrecision + sensor.getTemperaturePrecision();
			totalHumidityAccuracy = totalHumidityAccuracy + sensor.getHumidityAccuracy();
			totalHumidityDisponibility = totalHumidityDisponibility + sensor.getHumidityDisponsibility();
			totalHumidityPrecision = totalHumidityPrecision + sensor.getHumidityPrecision();
			totalLightAccuracy = totalLightAccuracy + sensor.getLightAccuracy();
			totalLightDisponibility = totalLightDisponibility + sensor.getHumidityDisponsibility();
			totalLightPrecision = totalLightPrecision + sensor.getLightPrecision();

			quality.add(sensorTemp);
		}
		
		if (qualityMeasures.size() > 0) {
			Sensor sensor = new Sensor();
			sensor.setMoteid(0);
			sensor.setTemperatureAccuracy(totalTemperatureAccuracy / qualityMeasures.size());
			sensor.setTemperatureDisponsibility(totalTemperatureDisponibility / qualityMeasures.size());
			sensor.setTemperaturePrecision(totalTemperaturePrecision / qualityMeasures.size());
			sensor.setHumidityAccuracy(totalHumidityAccuracy / qualityMeasures.size());
			sensor.setHumidityDisponsibility(totalHumidityDisponibility / qualityMeasures.size());
			sensor.setHumidityPrecision(totalHumidityPrecision / qualityMeasures.size());
			sensor.setLightAccuracy(totalLightAccuracy / qualityMeasures.size());
			sensor.setLightDisponsibility(totalLightDisponibility / qualityMeasures.size());
			sensor.setLightPrecision(totalLightPrecision / qualityMeasures.size());
			quality.add(sensor);
		}
		model.addAttribute("sensorqa", quality);
		model.addAttribute("refresh", "true");

		return "indexkafka";

	}

	@GetMapping(value = "/consumer")
	public String getAllRecievedMessage() {
		String messages = storage.toString();
		// System.out.println(messages);
		storage.clear();

		return messages;
	}

	/*
	 * calculate precision
	 */

	public Double getPrecision(String dimension, List<SensorData> sensorDataTempList) {

		Double precision = 0.0;

		Double totalPrecision = 0.0;

		int count = 2;
		Double previous = 0.0;
		Double next = 0.0;
		Double expected = 0.0;
		for (int i = 0; i < sensorDataTempList.size(); i++) {
			count = 2;
			if (i == 0) {
				count = count - 1;
				if (dimension.equals(DIMENSION_TEMPERATURE)) {
					if (i + 1 < sensorDataTempList.size()) {
						next = sensorDataTempList.get(i + 1).getTemperature();
					} else {
						next = 0.0;
						count = count - 1;
					}
					if (count == 0) {
						expected = 0.0;
					} else {
						expected = (previous + next) / count;
					}
					precision = (1 - ((Math.abs(sensorDataTempList.get(i).getTemperature() - expected)) / expected))
							* 100;
				} else if (dimension.equals(DIMENSION_HUMIDITY)) {
					if (i + 1 < sensorDataTempList.size()) {
						next = sensorDataTempList.get(i + 1).getHumidity();
					} else {
						next = 0.0;
						count = count - 1;
					}
					if (count == 0) {
						expected = 0.0;
					} else {
						expected = (previous + next) / count;
					}
					precision = (1 - ((Math.abs(sensorDataTempList.get(i).getHumidity() - expected)) / expected)) * 100;
				} else if (dimension.equals(DIMENSION_LIGHT)) {
					if (i + 1 < sensorDataTempList.size()) {
						next = sensorDataTempList.get(i + 1).getLight();
					} else {
						next = 0.0;
						count = count - 1;
					}
					if (count == 0) {
						expected = 0.0;
					} else {
						expected = (previous + next) / count;
					}
					precision = (1 - ((Math.abs(sensorDataTempList.get(i).getLight() - expected)) / expected)) * 100;
				}
			} else {
				if (dimension.equals(DIMENSION_TEMPERATURE)) {
					previous = sensorDataTempList.get(i - 1).getTemperature();
					if (i + 1 < sensorDataTempList.size()) {
						next = sensorDataTempList.get(i + 1).getTemperature();
					} else {
						next = 0.0;
						count = count - 1;
					}
					expected = (previous + next) / count;
					precision = (1 - ((Math.abs(sensorDataTempList.get(i).getTemperature() - expected)) / expected))
							* 100;
				} else if (dimension.equals(DIMENSION_HUMIDITY)) {
					previous = sensorDataTempList.get(i - 1).getHumidity();
					if (i + 1 < sensorDataTempList.size()) {
						next = sensorDataTempList.get(i + 1).getHumidity();
					} else {
						next = 0.0;
						count = count - 1;
					}
					expected = (previous + next) / count;
					precision = (1 - ((Math.abs(sensorDataTempList.get(i).getHumidity() - expected)) / expected)) * 100;
				} else if (dimension.equals(DIMENSION_LIGHT)) {
					previous = sensorDataTempList.get(i - 1).getLight();
					if (i + 1 < sensorDataTempList.size()) {
						next = sensorDataTempList.get(i + 1).getLight();
					} else {
						next = 0.0;
						count = count - 1;
					}
					expected = (previous + next) / count;
					precision = (1 - ((Math.abs(sensorDataTempList.get(i).getLight() - expected)) / expected)) * 100;
				}
			}
			totalPrecision = totalPrecision + precision;
		}

		if (sensorDataTempList.size() > 0) {
			totalPrecision = totalPrecision / sensorDataTempList.size();
		}

		return totalPrecision;

	}

	/*
	 * calculate quality measure
	 */

	public List<Sensor> getQualityMeasures(String sensorselected, List<SensorCoordinate> sensors, String startDate,
			String endDate, Double distance, int disponibilityexpectedforonehour, String startTime) {

		List<Sensor> qualityMeasures = new ArrayList<Sensor>();
		for (SensorCoordinate sensor : sensors) {
			Sensor sensorRow = new Sensor();
			List<SensorDist> sensorDists = sensorService.findAllSensorDist(sensor.getMoteid() + "", distance);

			String moteids = "";

			for (SensorDist sensorDist : sensorDists) {
				moteids = moteids + sensorDist.getToMoteId() + ",";
			}
			moteids = moteids + sensor.getMoteid();
			List<SensorData> sensorDataTempList = sensorService.findAllSensorDataTemp(sensor.getMoteid() + "");
			if (sensorDataTempList.size() > 0) {
				if (sensorselected != null && !sensorselected.equals("") && !sensorselected.equals("0")) {

					if (sensorselected.equals(sensor.getMoteid() + "")) {

						sensorRow.setMoteid(sensor.getMoteid());
						double measuredValue = sensorService.avgDimension(DIMENSION_TEMPERATURE, startDate, endDate,
								startTime, moteids, true);
						double meanVal = sensorService.avgDimension(DIMENSION_TEMPERATURE, startDate, endDate,
								startTime, sensor.getMoteid() + "");
						double temperatureaccuracy = (1 - ((Math.abs(measuredValue - meanVal)) / meanVal)) * 100;
						sensorRow.setTemperatureAccuracy(Math.max(0,temperatureaccuracy));
						double actualVal = sensorService.getActual(DIMENSION_TEMPERATURE, startDate, endDate, startTime,
								sensor.getMoteid() + "");
						double temperatureDisponibility = (1 - ((Math.abs(actualVal - disponibilityexpectedforonehour))
								/ disponibilityexpectedforonehour)) * 100;
						sensorRow.setTemperatureDisponsibility(Math.max(0,temperatureDisponibility));
						double temperatureprecision = getPrecision(DIMENSION_TEMPERATURE, sensorDataTempList);
						sensorRow.setTemperaturePrecision(Math.max(0,temperatureprecision));
						measuredValue = sensorService.avgDimension(DIMENSION_HUMIDITY, startDate, endDate, startTime,
								moteids, true);
						meanVal = sensorService.avgDimension(DIMENSION_HUMIDITY, startDate, endDate, startTime,
								sensor.getMoteid() + "");
						double humidityaccuracy = (1 - ((Math.abs(measuredValue - meanVal)) / meanVal)) * 100;
						sensorRow.setHumidityAccuracy(Math.max(0,humidityaccuracy));
						sensorRow.setHumidityDisponsibility(Math.max(0,temperatureDisponibility));
						double humidityprecision = getPrecision(DIMENSION_HUMIDITY, sensorDataTempList);
						sensorRow.setHumidityPrecision(Math.max(0,humidityprecision));
						measuredValue = sensorService.avgDimension(DIMENSION_LIGHT, startDate, endDate, startTime,
								moteids, true);
						meanVal = sensorService.avgDimension(DIMENSION_LIGHT, startDate, endDate, startTime,
								sensor.getMoteid() + "");
						double lightaccuracy = (1 - ((Math.abs(measuredValue - meanVal)) / meanVal)) * 100;
						sensorRow.setLightAccuracy(Math.max(0,lightaccuracy));
						sensorRow.setLightDisponsibility(Math.max(0,temperatureDisponibility));
						double lightprecision = getPrecision(DIMENSION_LIGHT, sensorDataTempList);
						sensorRow.setLightPrecision(Math.max(0,lightprecision));
						qualityMeasures.add(sensorRow);
						break;
					}
				} else {
					sensorRow.setMoteid(sensor.getMoteid());
					double measuredValue = sensorService.avgDimension(DIMENSION_TEMPERATURE, startDate, endDate,
							startTime, moteids, true);
					double meanVal = sensorService.avgDimension(DIMENSION_TEMPERATURE, startDate, endDate, startTime,
							sensor.getMoteid() + "");
					double temperatureaccuracy = (1 - ((Math.abs(measuredValue - meanVal)) / meanVal)) * 100;
					sensorRow.setTemperatureAccuracy(Math.max(0,temperatureaccuracy));
					double actualVal = sensorService.getActual(DIMENSION_TEMPERATURE, startDate, endDate, startTime,
							sensor.getMoteid() + "");
					double temperatureDisponibility = (1 - ((Math.abs(actualVal - disponibilityexpectedforonehour))
							/ disponibilityexpectedforonehour)) * 100;
					sensorRow.setTemperatureDisponsibility(Math.max(0,temperatureDisponibility));
					double temperatureprecision = getPrecision(DIMENSION_TEMPERATURE, sensorDataTempList);
					sensorRow.setTemperaturePrecision(Math.max(0,temperatureprecision));
					measuredValue = sensorService.avgDimension(DIMENSION_HUMIDITY, startDate, endDate, startTime,
							moteids, true);
					meanVal = sensorService.avgDimension(DIMENSION_HUMIDITY, startDate, endDate, startTime,
							sensor.getMoteid() + "");
					double humidityaccuracy = (1 - ((Math.abs(measuredValue - meanVal)) / meanVal)) * 100;
					sensorRow.setHumidityAccuracy(Math.max(0,humidityaccuracy));
					sensorRow.setHumidityDisponsibility(Math.max(0,temperatureDisponibility));
					double humidityprecision = getPrecision(DIMENSION_HUMIDITY, sensorDataTempList);
					sensorRow.setHumidityPrecision(Math.max(0,humidityprecision));
					measuredValue = sensorService.avgDimension(DIMENSION_LIGHT, startDate, endDate, startTime, moteids,
							true);
					meanVal = sensorService.avgDimension(DIMENSION_LIGHT, startDate, endDate, startTime,
							sensor.getMoteid() + "");
					double lightaccuracy = (1 - ((Math.abs(measuredValue - meanVal)) / meanVal)) * 100;
					sensorRow.setLightAccuracy(Math.max(0,lightaccuracy));
					sensorRow.setLightDisponsibility(Math.max(0,temperatureDisponibility));
					double lightprecision = getPrecision(DIMENSION_LIGHT, sensorDataTempList);
					sensorRow.setLightPrecision(Math.max(0,lightprecision));
					qualityMeasures.add(sensorRow);
				}
			}

		}

		return qualityMeasures;
	}

}