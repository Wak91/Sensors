package com.installwizard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.installwizard.domain.Sensor;
import com.installwizard.domain.SensorCoordinate;
import com.installwizard.domain.SensorData;
import com.installwizard.domain.SensorDist;
import com.installwizard.service.KafkaProducer;
import com.installwizard.service.SensorService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class IndexController {

	private static final String DIMENSION_TEMPERATURE = "temperature";
	private static final String DIMENSION_HUMIDITY = "humidity";
	private static final String DIMENSION_LIGHT = "light";

	@Autowired
	KafkaProducer kafkaProducer;

	@Autowired
	SensorService sensorService;

	/*
	 * index page
	 */

	@GetMapping("/")
	public String indexpage(Model model) {
		
		List<SensorCoordinate> sensors = sensorService.findAllSensor();
		model.addAttribute("sensors", sensors);

		return "index";
	}

	/*
	 * index page using kafka
	 */

	@GetMapping("/kafka")
	public String indexkafkapage(Model model) {

		List<SensorCoordinate> sensors = sensorService.findAllSensor();
		model.addAttribute("sensors", sensors);

		return "indexkafka";
	}

	@RequestMapping(value = "/loaddata", method = RequestMethod.GET)
	public String loaddata(HttpServletRequest request, HttpServletResponse response, Model model) {
		return "dataload";
	}

	/*
	 * load raw data
	 */

	@RequestMapping(value = "/dataload", method = RequestMethod.GET)
	public String dataload(HttpServletRequest request, HttpServletResponse response, Model model) {

		List<SensorData> sensorDataList = sensorService.findAllSensorData();
		if (sensorDataList.size() == 0) {

			Properties prop = new Properties();
			try {
				prop.load(new FileInputStream(new File(".").getAbsoluteFile() + "/config.properties"));
			} catch (IOException e3) {
				e3.printStackTrace();
			}
			String filename = prop.getProperty("filename");
			FileInputStream fstream;
			try {
				fstream = new FileInputStream(filename);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				String strLine;
				String output = "";
				Writer writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(new File(".").getAbsoluteFile() + "/errordata.txt"), "utf-8"));
				Long id = 1l;
				while ((strLine = br.readLine()) != null) {
					output = "";
					String[] column = strLine.split(" ");
					boolean flag = true;
					for (String col : column) {
						if (col.equals("")) {
							flag = false;
							break;
						}
					}
					if (column.length == 8 && flag) {
						SensorData sensorData = new SensorData();
						sensorData.setId(id);
						sensorData.setDate(column[0]);
						sensorData.setTime(column[1]);
						sensorData.setEpoch(Integer.parseInt(column[2]));
						sensorData.setMoteid(Integer.parseInt(column[3]));
						sensorData.setTemperature(Double.parseDouble(column[4]));
						sensorData.setHumidity(Double.parseDouble(column[5]));
						sensorData.setLight(Double.parseDouble(column[6]));
						sensorData.setVoltage(Double.parseDouble(column[7]));
						int success = sensorService.insertData(sensorData);
						if (success == 0) {
							output = strLine;
						}
						id++;
					} else {
						output = strLine;
					}
					/*if (!output.equals("")) {
						writer.write(output + "\n");
					}*/
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			model.addAttribute("successmessage", "Data loaded");
		} else {
			model.addAttribute("message", "Data already available");
		}
		return "dataload";
	}

	/*
	 */

	@RequestMapping(value = "/distanceload", method = RequestMethod.GET)
	public String distanceLoad(HttpServletRequest request, HttpServletResponse response, Model model) {

		List<SensorDist> sensorDists = sensorService.findAllSensorDist();

		if (sensorDists.size() > 0) {
			model.addAttribute("message", "Data already available");
		} else {

			List<SensorCoordinate> SensorCoordinateList = sensorService.findAllSensor();
			List<SensorCoordinate> SensorCoordinateListSecond = sensorService.findAllSensor();
			for (SensorCoordinate sensorFrom : SensorCoordinateList) {
				for (SensorCoordinate sensorTo : SensorCoordinateListSecond) {
					if (!(sensorTo.getMoteid() + "").equals(sensorFrom.getMoteid() + "")) {
						double distance = Math
								.pow((Math.pow(sensorTo.getXcoordinate() - sensorFrom.getXcoordinate(), 2.0)
										+ Math.pow(sensorTo.getYcoordinate() - sensorFrom.getYcoordinate(), 2.0)), 2);
						SensorDist sensorDist = new SensorDist();
						sensorDist.setFromMoteId(sensorFrom.getMoteid());
						sensorDist.setToMoteId(sensorTo.getMoteid());
						sensorDist.setDistance(distance);
						int success = sensorService.insertData(sensorDist);
					}
				}
			}
			model.addAttribute("successmessage", "Sensor distance data loaded");
		}
		return "dataload";
	}

	/*
	 * search data
	 */

	@RequestMapping(value = "/fetchdata", method = { RequestMethod.POST, RequestMethod.GET })
	public String fetch(HttpServletRequest request, HttpServletResponse response, Model model) {
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
			return "index";
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

		Properties prop = new Properties();
		//String configpath = new File(".").getAbsolutePath();
		try {
			prop.load(new FileInputStream(new File(".").getAbsolutePath() + "/config.properties"));
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		Double distance = Double.parseDouble(prop.getProperty("distancebetweensensors"));
		int disponibilityexpectedforonehour = Integer.parseInt(prop.getProperty("disponibilityexpectedforonehour"));

		List<SensorCoordinate> sensors = sensorService.findAllSensor();
		model.addAttribute("sensors", sensors);
		List<Sensor> qualityMeasures = new ArrayList<Sensor>();

		// fetch the raw data

		List<SensorData> sensorDataList = sensorService.findCriteriaSensorData(startDate, endDate, startTime, endTime,
				sensorselected);

		// calculate quality measure

		if (sensorDataList != null && sensorDataList.size() > 0) {
			qualityMeasures = getQualityMeasures(sensorselected, sensors, startDate, endDate, distance,
					disponibilityexpectedforonehour, startTime);
		}

		if (!sensorselected.equals("0")) {
			sensorDataList = sensorService.findAllSensorDataTemp(sensorselected);
		}

		model.addAttribute("sensordata", sensorDataList);

		List<Sensor> qualityMeasuresFinal = new ArrayList<Sensor>();

		qualityMeasuresFinal = qualityMeasures;

		Double totalTemperatureAccuracy = 0.0;
		Double totalTemperatureDisponibility = 0.0;
		Double totalTemperaturePrecision = 0.0;
		Double totalHumidityAccuracy = 0.0;
		Double totalHumidityDisponibility = 0.0;
		Double totalHumidityPrecision = 0.0;
		Double totalLightAccuracy = 0.0;
		Double totalLightDisponibility = 0.0;
		Double totalLightPrecision = 0.0;

		// averaage quality measure
		
			for (Sensor qualityMeasure : qualityMeasures) {
				totalTemperatureAccuracy = totalTemperatureAccuracy + qualityMeasure.getTemperatureAccuracy();
				totalTemperatureDisponibility = totalTemperatureDisponibility
						+ qualityMeasure.getTemperatureDisponsibility();
				totalTemperaturePrecision = totalTemperaturePrecision + qualityMeasure.getTemperaturePrecision();
				totalHumidityAccuracy = totalHumidityAccuracy + qualityMeasure.getHumidityAccuracy();
				totalHumidityDisponibility = totalHumidityDisponibility + qualityMeasure.getHumidityDisponsibility();
				totalHumidityPrecision = totalHumidityPrecision + qualityMeasure.getHumidityPrecision();
				totalLightAccuracy = totalLightAccuracy + qualityMeasure.getLightAccuracy();
				totalLightDisponibility = totalLightDisponibility + qualityMeasure.getHumidityDisponsibility();
				totalLightPrecision = totalLightPrecision + qualityMeasure.getLightPrecision();
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
			qualityMeasuresFinal.add(sensor);
		}

		model.addAttribute("sensorqa", qualityMeasuresFinal);
		model.addAttribute("refresh", "true");
		return "index";
	}

	/*
	 * fetch the raw data
	 */

	@RequestMapping(value = "/fetchrawdata", method = { RequestMethod.POST, RequestMethod.GET })
	public String fetchRawData(HttpServletRequest request, HttpServletResponse response, Model model) {
		String startDate = request.getParameter("startdate");
		String endDate = request.getParameter("enddate");
		String sensorselected = request.getParameter("sensor");

		if (startDate != null && !startDate.equals("")) {
			endDate = startDate;
		}
		if (endDate != null && !endDate.equals("")) {
			if (startDate == null && startDate.equals("")) {
				startDate = endDate;
			}
		}

		model.addAttribute("stdate", startDate);
		model.addAttribute("enddate", endDate);
		model.addAttribute("sensorselected", Integer.parseInt(sensorselected));

		List<SensorData> sensorDataList = sensorService.fetchCriteriaSensorData(startDate, endDate, sensorselected);
		model.addAttribute("sensordata", sensorDataList);
		List<SensorCoordinate> sensors = sensorService.findAllSensor();
		model.addAttribute("sensors", sensors);

		return "index";
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
						sensorRow.setTemperatureAccuracy(Math.max(0, temperatureaccuracy));
						double actualVal = sensorService.getActual(DIMENSION_TEMPERATURE, startDate, endDate, startTime,
								sensor.getMoteid() + "");
						double temperatureDisponibility = (1 - ((Math.abs(actualVal - disponibilityexpectedforonehour))
								/ disponibilityexpectedforonehour)) * 100;
						sensorRow.setTemperatureDisponsibility(Math.max(0, temperatureDisponibility));
						double temperatureprecision = getPrecision(DIMENSION_TEMPERATURE, sensorDataTempList);
						sensorRow.setTemperaturePrecision(Math.max(0, temperatureprecision));
						measuredValue = sensorService.avgDimension(DIMENSION_HUMIDITY, startDate, endDate, startTime,
								moteids, true);
						meanVal = sensorService.avgDimension(DIMENSION_HUMIDITY, startDate, endDate, startTime,
								sensor.getMoteid() + "");
						double humidityaccuracy = (1 - ((Math.abs(measuredValue - meanVal)) / meanVal)) * 100;
						sensorRow.setHumidityAccuracy(Math.max(0, humidityaccuracy));
						double humidityprecision = getPrecision(DIMENSION_HUMIDITY, sensorDataTempList);
						sensorRow.setHumidityPrecision(Math.max(0, humidityprecision));
						measuredValue = sensorService.avgDimension(DIMENSION_LIGHT, startDate, endDate, startTime,
								moteids, true);
						meanVal = sensorService.avgDimension(DIMENSION_LIGHT, startDate, endDate, startTime,
								sensor.getMoteid() + "");
						double lightaccuracy = (1 - ((Math.abs(measuredValue - meanVal)) / meanVal)) * 100;
						sensorRow.setLightAccuracy(Math.max(0, lightaccuracy));
						sensorRow.setLightDisponsibility(Math.max(0, temperatureDisponibility));
						double lightprecision = getPrecision(DIMENSION_LIGHT, sensorDataTempList);
						sensorRow.setLightPrecision(Math.max(0, lightprecision));
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
					sensorRow.setTemperatureDisponsibility(Math.max(0, temperatureDisponibility));
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
					sensorRow.setLightDisponsibility(Math.max(0, temperatureDisponibility));
					double lightprecision = getPrecision(DIMENSION_LIGHT, sensorDataTempList);
					sensorRow.setLightPrecision(Math.max(0,lightprecision));
					qualityMeasures.add(sensorRow);
				}
			}
		}

		return qualityMeasures;
	}

}
