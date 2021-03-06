CREATE TABLE IF NOT EXISTS SensorCoordinate
(
	ID BIGINT AUTO_INCREMENT PRIMARY KEY ,
	moteid NUMBER(3) NOT NULL,
	xcoordinate NUMBER(4,1) NOT NULL,
	ycoordinate NUMBER(4,1) NOT NULL
);
CREATE TABLE IF NOT EXISTS SensorConnectivity
(
	ID BIGINT AUTO_INCREMENT PRIMARY KEY,
	sendMoteId NUMBER(3) NOT NULL,
	receiverMoteId NUMBER(3) NOT NULL,
	connectivityMeasure NUMBER(24,20) NOT NULL
);
CREATE TABLE IF NOT EXISTS SensorData
(
	ID BIGINT AUTO_INCREMENT PRIMARY KEY ,
	date VARCHAR2(10) NOT NULL,
	time VARCHAR2(15) NOT NULL,
	epoch  NUMBER(5) NOT NULL,
	moteid NUMBER(3) NOT NULL,
	temperature NUMBER(10,6) NOT NULL,
	humidity NUMBER(10,6) NOT NULL,
	light NUMBER(10,6) NOT NULL,
	voltage NUMBER(10,6) NOT NULL
);
CREATE TABLE IF NOT EXISTS SensorDist
(
	ID BIGINT AUTO_INCREMENT PRIMARY KEY ,
	frommoteid NUMBER(3) NOT NULL,
	tomoteid NUMBER(3) NOT NULL,
	distance NUMBER(10,2) NOT NULL
);
CREATE TABLE IF NOT EXISTS SensorDataTemp
(
	ID BIGINT AUTO_INCREMENT PRIMARY KEY ,
	date VARCHAR2(10) NOT NULL,
	time VARCHAR2(15) NOT NULL,
	epoch  NUMBER(5) NOT NULL,
	moteid NUMBER(3) NOT NULL,
	temperature NUMBER(10,6) NOT NULL,
	humidity NUMBER(10,6) NOT NULL,
	light NUMBER(10,6) NOT NULL,
	voltage NUMBER(10,6) NOT NULL
);
