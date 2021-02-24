package com.installwizard.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.installwizard.domain.SensorConnectivity;
import com.installwizard.domain.SensorCoordinate;
import com.installwizard.domain.SensorData;
import com.installwizard.domain.SensorDist;

@Service
public class SensorService {

	// non zero to limit number of rows
	private int noRows = 0;

	@Autowired
	private JdbcTemplate jtm;

	/*
	 * get all sensor coordinate
	 */
	
	public List<SensorCoordinate> findAllSensor() {
		String sql = "SELECT * FROM SensorCoordinate";
		List<SensorCoordinate> sensors = jtm.query(sql, new BeanPropertyRowMapper(SensorCoordinate.class));
		return sensors;
	}

	/*
	 * get all sensor connectivity
	 */
	
	public List<SensorConnectivity> findAllSensorConnectivity() {
		String sql = "SELECT * FROM SensorConnectivity";
		List<SensorConnectivity> sensorConnectivities = jtm.query(sql,
				new BeanPropertyRowMapper(SensorConnectivity.class));
		return sensorConnectivities;
	}

	/*
	 * get all sensor distance
	 */
	
	public List<SensorDist> findAllSensorDist() {
		String sql = "SELECT * FROM SensorDist";
		List<SensorDist> sensorDists = jtm.query(sql, new BeanPropertyRowMapper(SensorDist.class));
		return sensorDists;
	}

	/*
	 * get all sensors based on distance 
	 */
	
	public List<SensorDist> findAllSensorDist(String sensor, Double distance) {
		String sql = "SELECT * FROM SensorDist where frommoteid = " + Integer.parseInt(sensor) + " and distance <= "
				+ distance;
		List<SensorDist> sensorDists = jtm.query(sql, new BeanPropertyRowMapper(SensorDist.class));
		return sensorDists;
	}

	/*
	 * get all raw data 
	 */
	
	public List<SensorData> findAllSensorData() {
		String sql = "";
		if (noRows > 0) {
			sql = "SELECT * FROM SensorData LIMIT " + noRows;
		} else {
			sql = "SELECT * FROM SensorData";
		}
		List<SensorData> sensorData = jtm.query(sql, new BeanPropertyRowMapper(SensorData.class));
		return sensorData;
	}

	/*
	 * get all raw data for the search criteria 
	 */
	
	public List<SensorData> findCriteriaSensorData(String startDate, String endDate, String startTime, String endTime,
			String sensor) {

		String sql = "";
		sql = "DELETE from SensorDataTemp";
		int i = jtm.update(sql);

		if (startDate.equals("")) {
			if (endDate.equals("")) {
				if (noRows > 0) {
					sql = "SELECT * FROM SensorData " + " where substr(time,0,5) = '" + startTime + "'" + " LIMIT "
							+ noRows;
				} else {
					sql = "SELECT * FROM SensorData " + " where substr(time,0,5) = '" + startTime + "'";
				}
			} else {
				if (noRows > 0) {
					sql = "SELECT * FROM SensorData where date <= '" + endDate + "'" + " and substr(time,0,5) = '"
							+ startTime + "'" + " ORDER BY MOTEID, DATE, TIME LIMIT " + noRows;
				} else {
					sql = "SELECT * FROM SensorData where date <= '" + endDate + "'" + " and substr(time,0,5) = '"
							+ startTime + "'" + " ORDER BY MOTEID, DATE, TIME ";
				}
			}
		} else {
			if (endDate.equals("")) {
				if (noRows > 0) {
					sql = "SELECT * FROM SensorData where date >= '" + startDate + "'" + " and substr(time,0,5) = '"
							+ startTime + "'" + " ORDER BY MOTEID, DATE, TIME LIMIT " + noRows;
				} else {
					sql = "SELECT * FROM SensorData where date >= '" + startDate + "'" + " and substr(time,0,5) = '"
							+ startTime + "'" + " ORDER BY MOTEID, DATE, TIME ";
				}
			} else {
				if (noRows > 0) {
					sql = "SELECT * FROM SensorData where date >= '" + startDate + "' and date <= '" + endDate + "'"
							+ " and substr(time,0,5) = '" + startTime + "'" + " ORDER BY MOTEID, DATE, TIME LIMIT "
							+ noRows;
				} else {
					sql = "SELECT * FROM SensorData where date >= '" + startDate + "' and date <= '" + endDate + "'"
							+ " and substr(time,0,5) = '" + startTime + "'" + " ORDER BY MOTEID, DATE, TIME ";
				}
			}
		}

		sql = "INSERT INTO SensorDataTemp " + sql;
		i = jtm.update(sql);
		sql = "SELECT * FROM SensorDataTemp";
		System.out.println("sql " + sql);
		List<SensorData> sensorData = jtm.query(sql, new BeanPropertyRowMapper(SensorData.class));

		return sensorData;
	}
	
	/*
	 * get only the raw data
	 */

	public List<SensorData> fetchCriteriaSensorData(String startDate, String endDate, String sensor) {

		String sql = "";

		if (!sensor.equals("0")) {

			if (startDate.equals("")) {

				if (endDate.equals("")) {
					if (noRows > 0) {
						sql = "SELECT * FROM SensorData where moteid = " + Integer.parseInt(sensor)
								+ " ORDER BY DATE, TIME LIMIT " + noRows;
					} else {
						sql = "SELECT * FROM SensorData where moteid = " + Integer.parseInt(sensor)
								+ " ORDER BY DATE, TIME";
					}
				} else {
					if (noRows > 0) {
						sql = "SELECT * FROM SensorData where date <= '" + endDate + "' and moteid = "
								+ Integer.parseInt(sensor) + " ORDER BY DATE, TIME LIMIT " + noRows;
					} else {
						sql = "SELECT * FROM SensorData where date <= '" + endDate + "' and moteid = "
								+ Integer.parseInt(sensor) + " ORDER BY DATE, TIME";
					}
				}
			} else {
				if (endDate.equals("")) {
					if (noRows > 0) {
						sql = "SELECT * FROM SensorData where date >= '" + startDate + "' and moteid = "
								+ Integer.parseInt(sensor) + " ORDER BY DATE, TIME LIMIT " + noRows;
					} else {
						sql = "SELECT * FROM SensorData where date >= '" + startDate + "' and moteid = "
								+ Integer.parseInt(sensor) + " ORDER BY DATE, TIME";
					}
				} else {
					if (noRows > 0) {
						sql = "SELECT * FROM SensorData where date >= '" + startDate + "' and date <= '" + endDate
								+ "' and moteid = " + Integer.parseInt(sensor) + " ORDER BY DATE, TIME LIMIT " + noRows;
					} else {
						sql = "SELECT * FROM SensorData where date >= '" + startDate + "' and date <= '" + endDate
								+ "' and moteid = " + Integer.parseInt(sensor) + " ORDER BY DATE, TIME";

					}
				}
			}
		} else {
			if (startDate.equals("")) {
				if (endDate.equals("")) {
					if (noRows > 0) {
						sql = "SELECT * FROM SensorData LIMIT " + noRows;
					} else {
						sql = "SELECT * FROM SensorData LIMIT";
					}
				} else {
					if (noRows > 0) {
						sql = "SELECT * FROM SensorData where date <= '" + endDate + "'"
								+ " ORDER BY MOTEID, DATE, TIME LIMIT " + noRows;
					} else {
						sql = "SELECT * FROM SensorData where date <= '" + endDate + "'"
								+ " ORDER BY MOTEID, DATE, TIME";
					}
				}
			} else {
				if (endDate.equals("")) {
					if (noRows > 0) {
						sql = "SELECT * FROM SensorData where date >= '" + startDate + "'"
								+ " ORDER BY MOTEID, DATE, TIME LIMIT " + noRows;
					} else {
						sql = "SELECT * FROM SensorData where date >= '" + startDate + "'"
								+ " ORDER BY MOTEID, DATE, TIME";
					}
				} else {
					if (noRows > 0) {
						sql = "SELECT * FROM SensorData where date >= '" + startDate + "' and date <= '" + endDate + "'"
								+ " ORDER BY MOTEID, DATE, TIME LIMIT " + noRows;
					} else {
						sql = "SELECT * FROM SensorData where date >= '" + startDate + "' and date <= '" + endDate + "'"
								+ " ORDER BY MOTEID, DATE, TIME";
					}
				}
			}

		}

		List<SensorData> sensorData = jtm.query(sql, new BeanPropertyRowMapper(SensorData.class));

		return sensorData;
	}
	
	/*
	 * get average for dimension for all sensors
	 */

	public double avgDimension(String dimension, String startDate, String endDate, String startTime, String sensors,
			boolean flag) {

		String sql = "";

		if (noRows > 0) {
			sql = "SELECT AVG(" + dimension + ") FROM SensorDataTemp where moteid in (" + sensors + ")" + " LIMIT "
					+ noRows;
		} else {
			sql = "SELECT AVG(" + dimension + ") FROM SensorDataTemp where moteid in (" + sensors + ")";
		}
		System.out.println("sql " + sql);
		Double sum = jtm.queryForObject(sql, Double.class);
		if (sum == null) {
			sum = 0.0;
		}
		return sum;
	}
	
	/*
	 * get average dimension for a sensor
	 */

	public double avgDimension(String dimension, String startDate, String endDate, String startTime, String sensor) {

		String sql = "";

		if (noRows > 0) {
			sql = "SELECT AVG(" + dimension + ") FROM SensorDataTemp" + " where moteid = " + Integer.parseInt(sensor)
					+ " LIMIT " + noRows;
		} else {
			sql = "SELECT AVG(" + dimension + ") FROM SensorDataTemp" + " where moteid = " + Integer.parseInt(sensor);
		}
		System.out.println("sql " + sql);
		Double sum = jtm.queryForObject(sql, Double.class);
		if (sum == null) {
			sum = 0.0;
		}
		return sum;
	}

	/*
	 * get count of rows for dimension 
	 */
	
	public double getActual(String dimension, String startDate, String endDate, String startTime, String sensor) {

		String sql = "";
		
		if (noRows > 0) {
			sql = "SELECT COUNT(" + dimension + ") FROM SensorDataTemp" + " where moteid = " + Integer.parseInt(sensor)
					+ " LIMIT " + noRows;
		} else {
			sql = "SELECT COUNT(" + dimension + ") FROM SensorDataTemp" + " where moteid = " + Integer.parseInt(sensor);
		}
		System.out.println("sql " + sql);
		Double count = jtm.queryForObject(sql, Double.class);
		if (count == null) {
			count = 0.0;
		}
		return count;
	}
	
	/*
	 * get all raw data from temp
	 */

	public List<SensorData> findAllSensorDataTemp(String moteid) {
		String sql = "SELECT * FROM SensorDataTemp where moteid = " + Integer.parseInt(moteid);
		List<SensorData> sensorDataTemp = jtm.query(sql, new BeanPropertyRowMapper(SensorData.class));
		return sensorDataTemp;
	}
	
	/*
	 * insert raw data into table
	 */

	public int insertData(SensorData sensorData) {
		String sql = "INSERT INTO SensorData (date, time, epoch, moteid, temperature, humidity, light, voltage) VALUES ('"
				+ sensorData.getDate() + "','" + sensorData.getTime() + "'," + sensorData.getEpoch() + ","
				+ sensorData.getMoteid() + "," + sensorData.getTemperature() + "," + sensorData.getHumidity() + ","
				+ sensorData.getLight() + "," + sensorData.getVoltage() + ")";
		int count = jtm.update(sql);
		return count;
	}

	/*
	 * insert sensor distance data 
	 */
	
	public int insertData(SensorDist sensorDist) {
		String sql = "INSERT INTO SensorDist (frommoteid, tomoteid, distance) VALUES (" + sensorDist.getFromMoteId()
				+ "," + sensorDist.getToMoteId() + "," + sensorDist.getDistance() + ")";
		int count = jtm.update(sql);
		return count;
	}

	/*
	 * to get the first date of raw data 
	 */
	
	public List<SensorData> findSensorData() {
		String sql = "SELECT date FROM SensorData LIMIT 1";
		List<SensorData> sensorData = jtm.query(sql, new BeanPropertyRowMapper(SensorData.class));
		return sensorData;
	}

}
