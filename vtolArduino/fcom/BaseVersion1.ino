#include <GPSHelper.h>
#include <Wire.h>
#include <QMC5883LCompass.h>
#include <cmath>
#include <ESP32Servo.h>
#include <WiFi.h>
#include <EEPROM.h>
#include <WebServer.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

#define EEPROM_SIZE 96
#define SSID_ADDR 0
#define PASS_ADDR 32
#define PPM_FRAME_LENGTH 22500
#define PPM_PULSE_LENGTH 300
#define PPM_CHANNELS 8
#define DEFAULT_CHANNEL_VALUE 1500
#define OUTPUT_PIN 14
#define GPS_RX_PIN 16
#define GPS_TX_PIN 17
#define echoPin 2
#define trigPin 4


#define travelALT 5
#define droppRadius 10
#define travelSpeed 40

#define COPTERARM 0
#define PLANETarget 1
#define COPTERSTART 2
#define TRANSITION 3
#define PLANEHome 4
#define DROPHOVER 5
#define COPTERLAND 6
#define COPTERDISARM 7
#define WAITFORINPUT 8
#define RUNHOTSPOT 9
#define TRANSITION2 10
#define WAITFORRESET 11



float maxChangePerCycleYaw = 0.1;
float previousYawOutput = 0.5;


int vtolID = 100;
const char* serverUrl = "https://vtol.weylyn.net/api/requests";
WebServer server(80);
char ssid[32];
char password[32];


int flightState = WAITFORINPUT;
const int servoPin = 33;
float distanceSafe = 0.1;
float rawDistance = 0;
long duration, distance;
float homeLat,homeLog,startingALT;
float tLat=0;
float tLog=0;
float tAlt=0;
uint16_t channelValue[PPM_CHANNELS] = { 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500 };
QMC5883LCompass compass;
Servo myServo;
hw_timer_t *timer = NULL;
const float low_threshold = 2.0;   // meters
const float high_threshold = 5.0;  // meters
float latitude,longitude;
int altitude; 
portMUX_TYPE timerMux = portMUX_INITIALIZER_UNLOCKED;
GPSHelper gps(GPS_RX_PIN, GPS_TX_PIN);

enum ppmState_e {
  PPM_STATE_IDLE,
  PPM_STATE_PULSE,
  PPM_STATE_FILL,
  PPM_STATE_SYNC
};

void IRAM_ATTR onPpmTimer() {

  static uint8_t ppmState = PPM_STATE_IDLE;
  static uint8_t ppmChannel = 0;
  static uint8_t ppmOutput = LOW;
  static int usedFrameLength = 0;
  int currentChannelValue;

  portENTER_CRITICAL(&timerMux);

  if (ppmState == PPM_STATE_IDLE) {
    ppmState = PPM_STATE_PULSE;
    ppmChannel = 0;
    usedFrameLength = 0;
    ppmOutput = LOW;
  }

  if (ppmState == PPM_STATE_PULSE) {
    ppmOutput = HIGH;
    usedFrameLength += PPM_PULSE_LENGTH;
    ppmState = PPM_STATE_FILL;

    timerAlarmWrite(timer, PPM_PULSE_LENGTH, true);
  } else if (ppmState == PPM_STATE_FILL) {
    ppmOutput = LOW;
    currentChannelValue = channelValue[ppmChannel];

    ppmChannel++;
    ppmState = PPM_STATE_PULSE;

    if (ppmChannel >= PPM_CHANNELS) {
      ppmChannel = 0;
      timerAlarmWrite(timer, PPM_FRAME_LENGTH - usedFrameLength, true);
      usedFrameLength = 0;
    } else {
      usedFrameLength += currentChannelValue - PPM_PULSE_LENGTH;
      timerAlarmWrite(timer, currentChannelValue - PPM_PULSE_LENGTH, true);
    }
  }
  portEXIT_CRITICAL(&timerMux);
  digitalWrite(OUTPUT_PIN, ppmOutput);
}
float sigmoid(float x) {
  return 1 / (1 + exp(-x));
}

void setup() {
  Serial.begin(9600);
  if(setUPServer()){
    Serial.println("SetupServer success");
    delay(100);
    Serial.println("FCOMV1: wating for timer");
    delay(10000);
    Serial.println("FCOMV1: setupcompass");
    setUPCompass();
    Serial.println("FCOMV1: setupPPM");
    setUPPPM();
    Serial.println("FCOMV1: setupDistanceSensor");
    setUPSonic();
    Serial.println("FCOMV1: setupGPS");
    setUPGPS();
    Serial.println("FCOMV1: FoundSats");
    setUPSERVO();
    delay(1000);
    servoDROP(false);
  }
}

bool setUPServer(){
  EEPROM.begin(EEPROM_SIZE);
  readCredentials();
  if (!connectToWiFi()) {
    flightState = RUNHOTSPOT;
    startConfigPortal();
    return false;
  }
  return true;
}
void getGoeString(){
  delay(1000);
  if (!connectToWiFi()) {
    ESP.restart();
  }
   HTTPClient http;

  http.begin(serverUrl);
  http.addHeader("Content-Type", "application/json");

  StaticJsonDocument<200> doc;
  doc["hardwareID"] = vtolID;

  String jsonStr;
  serializeJson(doc, jsonStr);

  int httpResponseCode = http.POST(jsonStr);

  if (httpResponseCode > 0) {
      String response = http.getString();
      Serial.println("Response: " + response);
      Serial.println(response);
      convertGeoString(response);
  } else {
    Serial.print("Error occurred: ");
    Serial.println(httpResponseCode);
  }
  http.end();
  delay(1000);
} 
bool connectToWiFi() {
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  for (int i = 0; i < 30; ++i) {
    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("\nConnected to WiFi");
      Serial.println("IP address: ");
      Serial.println(WiFi.localIP());
      return true;
    }
    delay(100);
    Serial.print(".");
  }
  Serial.println("\nFailed to connect to WiFi");
  return false;
}
void startConfigPortal() {
  WiFi.softAP("VtolConfigWIFI");
  Serial.println("Started Config Portal");
  Serial.print("IP address: ");
  Serial.println(WiFi.softAPIP());

  server.on("/", HTTP_GET, []() {
    server.send(200, "text/html", R"(
      <form action="/save" method="POST">
        SSID: <input type="text" name="ssid"><br>
        Password: <input type="text" name="password"><br>
        <input type="submit" value="Save">
      </form>
    )");
  });

  server.on("/save", HTTP_POST, []() {
    if (server.hasArg("ssid") && server.hasArg("password")) {
      String newSSID = server.arg("ssid");
      String newPassword = server.arg("password");
      saveCredentials(newSSID, newPassword);
      server.send(200, "text/plain", "Credentials Saved! Rebooting...");
      delay(1000);
      ESP.restart();
    } else {
      server.send(400, "text/plain", "Missing SSID or Password");
    }
  });

  server.begin();
}
void readCredentials() {
  for (int i = 0; i < 32; ++i) {
    ssid[i] = char(EEPROM.read(SSID_ADDR + i));
    password[i] = char(EEPROM.read(PASS_ADDR + i));
  }
  ssid[31] = '\0'; // Ensure null-termination
  password[31] = '\0'; // Ensure null-termination
  Serial.print("Read SSID: ");
  Serial.println(ssid);
  Serial.print("Read Password: ");
  Serial.println(password);
}
void saveCredentials(String newSSID, String newPassword) {
  for (int i = 0; i < 32; ++i) {
    EEPROM.write(SSID_ADDR + i, i < newSSID.length() ? newSSID[i] : 0);
    EEPROM.write(PASS_ADDR + i, i < newPassword.length() ? newPassword[i] : 0);
  }
  EEPROM.commit();
  Serial.println("Credentials saved to EEPROM");
}
void convertGeoString(String response){
   // Find the position of the coordinates array in the JSON response
  int coordsStart = response.indexOf("[\"");
  int coordsEnd = response.indexOf("\"]", coordsStart);

  if (coordsStart == -1 || coordsEnd == -1) {
    Serial.println("Failed to find coordinates in response.");
    return;
  }

  // Extract the coordinates string
  String coords = response.substring(coordsStart + 2, coordsEnd);

  // Print the coordinates string
  Serial.print("Coords: ");
  Serial.println(coords);

  // Parse the coordinates using sscanf
  sscanf(coords.c_str(), "%f,%f,%d", &latitude, &longitude, &altitude);
  tLat = latitude;
  tLog = longitude;
  tAlt = altitude;
  if(tLat == 0||tLog ==0){
    ESP.restart();
  } 
  // Print the parsed values
  Serial.print("Latitude: ");
  Serial.println(latitude, 6);
  Serial.print("Longitude: ");
  Serial.println(longitude, 6);
  Serial.print("Altitude: ");
  Serial.println(altitude);
}
void setUPSERVO(){
  myServo.attach(servoPin);
}
void armVtol(bool state) {
  if (state) {
    flightState = 2;          //armed flight mode
    channelValue[5] = 2000;  //DroneMode
    delay(3000);
    channelValue[0] = 1000;  //CH1 THRO
    delay(100);
    channelValue[4] = 2000;  //aux1 ARM
    delay(2000);
  } else {
    flightState = 1;          //disarmend fllight mode
    channelValue[4] = 1000;  //aux1 ARM
  }

}
void servoDROP(bool state){
  if(state){
    //myServo.write(100);
  }else{
    //myServo.write(40);
  }
}
void loop() {
  flyVtol();
  delay(100);
}
void flyVtol() {
  //collect data
  gps.fetch();  //fetch GPS to ensure updated data
  float thro = 0;
  float roll = 0.5;
  float pitch = 0.5;
  float yaw = 0.5;
  float lat = gps.getLatitude();
  float log = gps.getLongitude();
  float alt = getCombHeight(startingALT - gps.getAltitude(), getDistance());
  float angle = getBearing();
  float speed = updateGPSSpeed();
  switch(flightState){ 
    case COPTERARM:
      armVtol(true);
      flightState = COPTERSTART;
      delay(1000);
      break;

    case COPTERSTART:
      yaw = pYAWCon(gps.getAngle(homeLat, homeLog),angle);
      pitch = pPitchConCOPTERFLY(calculateDistanceToTarget(lat,log,homeLat,homeLog));
      thro = 1;
      if(alt > travelALT){ 
        flightState = TRANSITION;
        thro = 0.6;
      }
      break;

    case TRANSITION:
      channelValue[5] = 1500;  //TransMode
      delay(1000);
      channelValue[5] = 1000;  //PlaneMode
      delay(500);
      thro = 0.4;
      flightState = PLANETarget;
      break;

    case PLANETarget:
      yaw = pYAWCon((gps.getAngle(tLat, tLog)),angle);
      pitch = pPitchCon(travelALT,alt);
      thro = correctSpeed(speed,travelSpeed);
      if(travelALT - travelALT*0.2 > alt  || alt > travelALT + travelALT*0.2 ){ 
        flightState = COPTERLAND;
      }
      if(droppRadius > calculateDistanceToTarget(lat,log,tLat,tLog)){ 
        flightState = DROPHOVER;
        thro = 0.5;
      }
      break;
    case DROPHOVER:
      channelValue[5] = 1500;  //TransMode
      delay(1000);
      channelValue[5] = 2000;  //CopterMode
      delay(500);
      servoDROP(true);         //DROP PAYLOAD
      delay(500);
      channelValue[5] = 1500;  //TransMode
      delay(1000);
      channelValue[5] = 1000;  //PlaneMode
      delay(500);
      flightState = PLANEHome;
      break;

    case PLANEHome:
      yaw = pYAWCon((gps.getAngle(homeLat, homeLog)),angle);
      pitch = pPitchCon(travelALT,alt);
      thro = correctSpeed(speed,travelSpeed);
      if(travelALT - travelALT*0.5 > alt  || alt > travelALT + travelALT*0.5 ){ 
        flightState = COPTERLAND;
      }
      if(droppRadius > calculateDistanceToTarget(lat,log,homeLat,homeLog)){ 
        flightState = TRANSITION2;
        thro = 0.5;
      }
      break;
    case TRANSITION2:
      channelValue[5] = 1500;  //TransMode
      delay(1000);
      channelValue[5] = 2000;  //CopterMode
      delay(500);
      flightState = COPTERLAND;
      break;
    case COPTERLAND:
      if(alt - startingALT > 2){ 
        thro=0.25;
      }else{ 
        thro=0.5;
         if(rawDistance>1000){ 
          flightState = COPTERDISARM;
         }
      }  
      break;

    case COPTERDISARM:
      armVtol(false);
      flightState = WAITFORRESET;
      break;
    case WAITFORINPUT:
      delay(1000);
      getGoeString();
      flightState = COPTERARM;
    case RUNHOTSPOT:
      server.handleClient();
      break;
    case WAITFORRESET:
      break;
    default:
      ESP.restart();
  }

  writePPMSignals(thro, roll, pitch, yaw); //send signals to fcon
}
float pPitchConCOPTERFLY(float pDistance){
  float output = 0.5;
  float scaledErr = sigmoid(pDistance)-0.5;
  return output + scaledErr;
}
float correctSpeed(float currSpeed, float targetSpeed){
  float error = constrain(targetSpeed-currSpeed,0,20)/60;  //constrain output between 0 and 0.3
  return 0.3+error;
}
float calculateDistanceToTarget(float currentLat, float currenLon,float targetLat, float targetLon) {
    // Get current GPS data
    float currLat = currentLat;
    float currLon = currenLon;

    // Convert latitude and longitude from degrees to radians
    float currLatRad = radians(currLat);
    float currLonRad = radians(currLon);
    float targetLatRad = radians(targetLat);
    float targetLonRad = radians(targetLon);

    // Haversine formula to calculate distance
    float dLat = targetLatRad - currLatRad;
    float dLon = targetLonRad - currLonRad;
    float a = pow(sin(dLat / 2), 2) + cos(currLatRad) * cos(targetLatRad) * pow(sin(dLon / 2), 2);
    float c = 2 * atan2(sqrt(a), sqrt(1 - a));
    float distance = 6371000 * c; // Earth radius in meters

    return distance; // Distance in meters
}
float pPitchCon(float pTHeight, float pHeight) {
    float desiredOutput = 0.5;
    float error = pHeight-pTHeight;   //Calc error
    error = constrain(error,-20,20);  //constrain error between -20 and 20
    error = error/60;                  //scale error to -0.3 and 0.3
    desiredOutput+= error;
    return desiredOutput;
}
float pYAWCon(float pTAngle, float pAngle) {
    // Calculate absolute difference between the angles
    float diff = abs(pTAngle - pAngle);
    // Check if the difference is greater than 180 degrees
    if (diff > 180) {
        // Subtract 360 degrees from the larger angle
        if (pTAngle > pAngle) {
            pTAngle -= 360;
        } else {
            pAngle -= 360;
        }
    }
    // Calculate the error angle
    float error = pTAngle - pAngle + 180;
    float mappedAngleErr = error / 360 - 0.5;
    // Calculate the desired output
    float desiredOutput = 0.5 - mappedAngleErr/2;
      // Limit the change in output
    float change = desiredOutput - previousYawOutput;
    if (change > maxChangePerCycleYaw) {
        desiredOutput = previousYawOutput + maxChangePerCycleYaw;
    } else if (change < -maxChangePerCycleYaw) {
        desiredOutput = previousYawOutput - maxChangePerCycleYaw;
    }

    // Update the previous output for the next cycle
    previousYawOutput = desiredOutput;
    return desiredOutput;
}
float scaleToUnit(float value) {
        return 1 / (1 + value);
}
void writePPMSignals(float thro, float roll, float pitch, float yaw) {
  // Add new values to the smoothedValues array
  channelValue[0] = 1000 * thro + 1000;  // CH1 THRO
  channelValue[1] = 1000 * roll + 1000;  // CH2 ROLL
  channelValue[2] = 1000 * pitch + 1000; // CH3 PITCH
  channelValue[3] = 1000 * yaw + 1000;   // CH4 YAW
}
void setUPGPS() {
  gps.begin();
  if (gps.fetch()) {
    while (7 > gps.getSatellites()) {
      Serial.println(gps.getSatellites());
      delay(1000);
    }
  }
  startingALT = gps.getAltitude();
  homeLat = gps.getLatitude();
  homeLog = gps.getLongitude();
}
void setUPCompass() {
  compass.init();
  compass.setCalibrationOffsets(247.00, 892.00, -109.00);
  compass.setCalibrationScales(0.93, 1.02, 1.07);
}
void setUPSonic() {
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
}
float getBearing() {
  compass.read();
  return compass.getAzimuth()+180;
}
void setUPPPM() {
  pinMode(OUTPUT_PIN, OUTPUT);
  timer = timerBegin(0, 80, true);
  timerAttachInterrupt(timer, &onPpmTimer, true);
  timerAlarmWrite(timer, 12000, true);
  timerAlarmEnable(timer);
}
float getDistance() {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  duration = pulseIn(echoPin, HIGH);
  distance = duration / 58.2;
  rawDistance = distance;
  if (distance > 500) {
    return distanceSafe;  // Return last value above 500
  } else {
    distanceSafe = distance;
    return distance;
  }
}
float updateGPSSpeed() {
    static float prevLat = gps.getLatitude();
    static float prevLon = gps.getLongitude();
    static unsigned long prevTime = millis();

    // Calculate elapsed time since last GPS reading
    unsigned long currTime = millis();
    unsigned long elapsedTime = currTime - prevTime;

    // Convert latitude and longitude from degrees to radians
    float prevLatRad = radians(prevLat);
    float prevLonRad = radians(prevLon);
    float currLatRad = radians(gps.getLatitude());
    float currLonRad = radians(gps.getLongitude());

    // Haversine formula to calculate distance
    float dLat = currLatRad - prevLatRad;
    float dLon = currLonRad - prevLonRad;
    float a = pow(sin(dLat / 2), 2) + cos(prevLatRad) * cos(currLatRad) * pow(sin(dLon / 2), 2);
    float c = 2 * atan2(sqrt(a), sqrt(1 - a));
    float distance = 6371000 * c; // Earth radius in meters

    // Calculate speed (distance / time)
    float speed = distance / (elapsedTime / 1000.0); // Convert elapsed time to seconds

    // Update previous GPS data for next iteration
    prevLat = gps.getLatitude();
    prevLon = gps.getLongitude();
    prevTime = currTime;

    return speed; // Speed in meters per second
}
float getCombHeight(float heightGps, float heightSonic) {
  float ultrasonicWeight = sigmoid(heightSonic - low_threshold);
  float gpsWeight = 1 - ultrasonicWeight;
  return ((heightSonic / 100.0) * ultrasonicWeight) + (heightGps * gpsWeight);
}
