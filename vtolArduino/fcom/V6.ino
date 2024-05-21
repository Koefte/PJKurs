#include <GPSHelper.h>
#include <ListLibrary.h>
#include <Wire.h>
#include <QMC5883LCompass.h>
#include <cmath>

#define PPM_FRAME_LENGTH 22500
#define PPM_PULSE_LENGTH 300
#define PPM_CHANNELS 8
#define DEFAULT_CHANNEL_VALUE 1500
#define OUTPUT_PIN 14
#define GPS_RX_PIN 16
#define GPS_TX_PIN 17
#define echoPin 2
#define trigPin 4





int COPTERFLY = 0;
int PLANE = 1;
int COPTERSTART = 2;
int COPTERLAND = 3;
int COPTERHOVER = 4;

int flightState = 0;

float distanceSafe = 0.1;
long duration, distance;
float homeLat,homeLog,startingALT;
float startLatWay=0;
float startLogWay=0;
uint16_t channelValue[PPM_CHANNELS] = { 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500 };
QMC5883LCompass compass;
hw_timer_t *timer = NULL;
const float low_threshold = 2.0;   // meters
const float high_threshold = 5.0;  // meters
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
  delay(100);
  Serial.begin(9600);
  Serial.println("FCOMV1: wating for timer");
  delay(100);
  Serial.println("FCOMV1: setupcompass");
  setUPCompass();
  Serial.println("FCOMV1: setupPPM");
  setUPPPM();
  Serial.println("FCOMV1: setupDistanceSensor");
  setUPSonic();
  Serial.println("FCOMV1: setupGPS");
  setUPGPS();
  Serial.println("FCOMV1: FoundSats");
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
void loop() {
  flyVtol(6.044819457976702,50.71835025951335,0.5,0);
  delay(100);
}
void flyVtol(float Tlog, float Tlat, float Talt, int mode) {
  //collect data
  gps.fetch();  //fetch GPS to ensure updated data
  float thro = 0;
  float roll = 0.5;
  float pitch = 0.5;
  float yaw = 0.5;
  float lat = gps.getLatitude();
  float log = gps.getLongitude();
  float alt = getCombHeight(startingALT - gps.getAltitude(), getDistance());
  float Tangle = gps.getAngle(Tlat, Tlog);
  float angle = getBearing();
  float speed = updateGPSSpeed();
  float distanceToTarget = calculateDistanceToTarget(lat,log,Tlat,Tlog);


  if (mode == COPTERFLY) { 
    //control height
    thro = pidTHROConCOPTER(Talt,alt);
    //rotate to target
    yaw = pYAWCon(Tangle,angle);
    //move foreward
    pitch = pPitchConCOPTERFLY(distanceToTarget);
    //stay on track
  }
  if(mode == COPTERSTART){
    if(flightState!=2){
      armVtol(true);
    }
    thro = pidTHROConCOPTER(Talt, alt);
  }
  if(mode == COPTERLAND){
    thro = pidTHROConCOPTER(startingALT, alt);
  } 
  if(mode == PLANE){
    //controll speed:
    
    //controll height:

    //controll heading:
    yaw = pYAWCon(Tangle,angle);
  } 
  writePPMSignals(thro, roll, pitch, yaw); //send signals to fcon
}
float pPitchConCOPTERFLY(float pDistance){
  float output = 0.5;
  float scaledErr = sigmoid(pDistance)-0.5;
  return output + scaledErr;
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
float pidTHROConCOPTER(float tHeight, float cHeight) {
    const int error_division_factor = 3;
    bool isnegative = false;
    float error = tHeight - cHeight;
    float error_scaled;
    float output;

    // Check for negative errors (preparation for conversion)
    if (error != 0) {
        if (0 > error) {
            isnegative = true;
            error = abs(error);
        }
        error_scaled = scaleToUnit(error/error_division_factor);
        if (isnegative == false) {
            error_scaled = 1 - error_scaled;
        } else {
            error_scaled = -1 + error_scaled;
        }
    }
    output = 0.5+error_scaled;
    Serial.println(output);
    return output;
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
