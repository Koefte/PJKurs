#include <WiFi.h>
#include <EEPROM.h>
#include <WebServer.h>
#include <esp_now.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>


#define EEPROM_SIZE 96
#define SSID_ADDR 0
#define PASS_ADDR 32

//Variables TO CAHNGE
String serverURL = "http://192.168.137.1:3001";
String requestPath = "/api/users";
int stationID = 100;
uint8_t broadcastAddress[] = {0x32, 0xAE, 0xA4, 0x07, 0x0D, 0x66};
const char* serverUrl = "https://vtol.weylyn.net/api/requests";


float latitude,longitude;
int altitude; 
WebServer server(80);

int state=0;
char ssid[32];
char password[32];


typedef struct struct_message {
  float a;
  float b;
  bool c;
} struct_message;
struct_message myData;
esp_now_peer_info_t peerInfo;
void OnDataSent(const uint8_t *mac_addr, esp_now_send_status_t status) {
  if(status == ESP_NOW_SEND_SUCCESS) {
    Serial.println("Delivery Success");
    state = 4;
    return;
  }
  Serial.println(status == ESP_NOW_SEND_SUCCESS ? "Delivery Success" : "Delivery Fail");
}
 

void setup() {
  Serial.begin(115200);
  EEPROM.begin(EEPROM_SIZE);
  readCredentials();
  
  if (!connectToWiFi()) {
    startConfigPortal();
  }
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
  doc["hardwareID"] = stationID;

  String jsonStr;
  serializeJson(doc, jsonStr);

  int httpResponseCode = http.POST(jsonStr);

  if (httpResponseCode > 0) {
      String response = http.getString();
      Serial.println("Response: " + response);
      state = 2;
      Serial.println("GeoString is valid");
      Serial.println(response);
      convertGeoString(response);
  } else {
    Serial.print("Error occurred: ");
    Serial.println(httpResponseCode);
  }
  http.end();
  delay(1000);
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
  sscanf(coords.c_str(), "%f,%f.%d", &latitude, &longitude, &altitude);

  // Print the parsed values
  Serial.print("Latitude: ");
  Serial.println(latitude, 6);
  Serial.print("Longitude: ");
  Serial.println(longitude, 6);
  Serial.print("Altitude: ");
  Serial.println(altitude);
}
void loop() {
  switch(state){
    case 0:
      server.handleClient();
      break;
    case 1:
      getGoeString();
      break;
    case 2: 
      setupESPNOW();
      break;
    case 3: 
      sendGeoStringESPNOW();
      break;
    case 4:
      state = 1;
      break;
    default:
      ESP.restart();
      break;
  }
}
void setupESPNOW(){ 
  WiFi.disconnect(true);
  Serial.println("DisconnectedFromServer");
  Serial.println("StartingESPNOWHotspot");
  delay(1000);
  WiFi.mode(WIFI_STA);
  // Init ESP-NOW
  if (esp_now_init() != ESP_OK) {
    Serial.println("Error initializing ESP-NOW");
    return;
  }
  // Once ESPNow is successfully Init, we will register for Send CB to
  // get the status of Transmitted packet
  esp_now_register_send_cb(OnDataSent);
  // Register peer
  memcpy(peerInfo.peer_addr, broadcastAddress, 6);
  peerInfo.channel = 0;  
  peerInfo.encrypt = false;
  // Add peer        
  if (esp_now_add_peer(&peerInfo) != ESP_OK){
    Serial.println("Failed to add peer");
    return;
  }
  state = 3;
}
void sendGeoStringESPNOW(){
  myData.a = latitude;
  myData.b = longitude;
  myData.c = altitude;
  esp_err_t result = esp_now_send(broadcastAddress, (uint8_t *) &myData, sizeof(myData));
   
  if (result == ESP_OK) {
    Serial.println("Sent with success");
  }
  else {
    Serial.println("Error sending the data");
  }
  delay(2000);
}
bool connectToWiFi() {
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  for (int i = 0; i < 30; ++i) {
    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("\nConnected to WiFi");
      Serial.println("IP address: ");
      Serial.println(WiFi.localIP());
      state = 1;
      return true;
    }
    delay(100);
    Serial.print(".");
  }
  Serial.println("\nFailed to connect to WiFi");
  return false;
}
void startConfigPortal() {
  WiFi.softAP("GroundstationConfig");
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
