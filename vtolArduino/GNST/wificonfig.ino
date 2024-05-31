#include <WiFi.h>
#include <EEPROM.h>
#include <WebServer.h>

#define EEPROM_SIZE 96
#define SSID_ADDR 0
#define PASS_ADDR 32

WebServer server(80);
char ssid[32];
char password[32];

void setup() {
  Serial.begin(115200);
  EEPROM.begin(EEPROM_SIZE);
  setupWiFi();
}

void loop() {
  server.handleClient();
}

// Sets up WiFi connection or starts the config portal if connection fails
void setupWiFi() {
  // Read stored SSID and Password from EEPROM
  for (int i = 0; i < 32; ++i) {
    ssid[i] = char(EEPROM.read(SSID_ADDR + i));
    password[i] = char(EEPROM.read(PASS_ADDR + i));
  }
  ssid[31] = '\0'; // Ensure null-termination
  password[31] = '\0'; // Ensure null-termination

  // Attempt to connect to WiFi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  for (int i = 0; i < 30; ++i) {
    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("\nConnected to WiFi");
      Serial.print("IP address: ");
      Serial.println(WiFi.localIP());
      return;
    }
    delay(1000);
    Serial.print(".");
  }
  Serial.println("\nFailed to connect to WiFi");

  // Start the fallback config portal
  startWifiPortal("GroundstationConfig");
}

// Starts the configuration portal to input new WiFi credentials
void startWifiPortal(const char* hotspotName) {
  WiFi.softAP(hotspotName);
  Serial.println("Started Config Portal");
  Serial.print("AP IP address: ");
  Serial.println(WiFi.softAPIP());

  // Serve the configuration page
  server.on("/", HTTP_GET, []() {
    server.send(200, "text/html", R"(
      <form action="/save" method="POST">
        SSID: <input type="text" name="ssid"><br>
        Password: <input type="text" name="password"><br>
        <input type="submit" value="Save">
      </form>
    )");
  });

  // Handle form submission and save credentials
  server.on("/save", HTTP_POST, []() {
    if (server.hasArg("ssid") && server.hasArg("password")) {
      String newSSID = server.arg("ssid");
      String newPassword = server.arg("password");
      for (int i = 0; i < 32; ++i) {
        EEPROM.write(SSID_ADDR + i, i < newSSID.length() ? newSSID[i] : 0);
        EEPROM.write(PASS_ADDR + i, i < newPassword.length() ? newPassword[i] : 0);
      }
      EEPROM.commit();
      server.send(200, "text/plain", "Credentials Saved! Rebooting...");
      delay(1000);
      ESP.restart();
    } else {
      server.send(400, "text/plain", "Missing SSID or Password");
    }
  });

  server.begin();
}
