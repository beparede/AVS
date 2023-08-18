#include <Arduino.h>
#if defined(ESP32)
#include <WiFi.h>
#include <FirebaseESP32.h>
#endif

// Provide the token generation process info.
#include <addons/TokenHelper.h>

// Provide the RTDB payload printing info and other helper functions.
#include <addons/RTDBHelper.h>

// library timer interruptions
#include <esp_timer.h>

// ===========================
// Enter your WiFi credentials
// ===========================
const char* ssid = "INGEMEDICA";
const char* password = "aaabbbcccd";

// ====================
// Firebase Credentials
// ====================
// Define the API Key
#define API_KEY "AIzaSyCqp3N6lLFLPNJz4BqWa_tYaihNlknjY-Y"

// Define the RTDB URL
#define DATABASE_URL "control-panel-solar-1cb81-default-rtdb.firebaseio.com" //<databaseName>.firebaseio.com or <databaseName>.<region>.firebasedatabase.app

// Define the user Email and password that alreadey registerd or added in your project */
#define USER_EMAIL "panelsolar_1@controlpanel.com"
#define USER_PASSWORD "panelsolar_1"

// Define Firebase Data object
FirebaseData fbdo;

// Define Firebase Auth
FirebaseAuth auth;

// Define Firebase Config
FirebaseConfig configFire;

// Define pins for the Foto-Resistors
#define PHOTO_RESISTOR_1         36
#define PHOTO_RESISTOR_2         39

// Define pinf for stepper motor
#define EN                      4
#define DIR                     2
#define STEP                    15

// Define the photoresist variables
float volagePhotoResistor1 = 2000.0;
float volagePhotoResistor2 = 3600.0;

// Variables de optimizacion
unsigned long lastSampleTime = 0;

// Define the parameters for stepper motor
float anglePosition = 0.0;
float angleSP = 0.0;
const float angleStep = 1.8;
float angleCalc = 0.0;

// boolean variables
bool statePulse = false;

// Control Mode
String controlMode;

void myTimerCallback(void* arg);

void setup() {
  // Initialize serial communication at 115200 bits per second:
  Serial.begin(115200);

  //Set Motor controller pines
  pinMode(EN, OUTPUT); 
	pinMode(DIR, OUTPUT); 
	pinMode(STEP, OUTPUT);

  //Set the resolution to 12 bits (0-4096)
  analogReadResolution(12);

  //Set the configuration of interrupt by timer
  esp_timer_create_args_t timerConfig;
  timerConfig.callback = myTimerCallback;
  timerConfig.arg = NULL;
  esp_timer_handle_t timerHandle;
  esp_timer_create(&timerConfig, &timerHandle);
  esp_timer_start_periodic(timerHandle, 3000000); // Set the interruption time every 3s

  // begin Wifi connection 
  WiFi.begin(ssid, password);
  WiFi.setSleep(false);

  Serial.print("WiFi connecting ");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.print(WiFi.localIP());
  Serial.println("' to connect");

  // begin firebase services integration
  Serial.printf("Firebase Client v%s\n\n", FIREBASE_CLIENT_VERSION);

  /* Assign the api key (required) */
  configFire.api_key = API_KEY;

  /* Assign the user sign in credentials */
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  /* Assign the RTDB URL (required) */
  configFire.database_url = DATABASE_URL;

  /* Assign the callback function for the long running token generation task */
  configFire.token_status_callback = tokenStatusCallback; // see addons/TokenHelper.h

  Firebase.begin(&configFire, &auth);

  // Comment or pass false value when WiFi reconnection will control by your code or third party library
  Firebase.reconnectWiFi(true);

  Firebase.setDoubleDigits(5);

  // Enable stepper motor 
  digitalWrite(EN, LOW);

}

void loop() {
 
  unsigned long currentTime = millis();

  if (currentTime - lastSampleTime >= 1000){
    lastSampleTime = currentTime;
    
    // Read and update database every 1s
    if (Firebase.getJSON(fbdo, "/Data")) {
      
      Serial.println("Exite Data");
      
      //volagePhotoResistor1 = analogReadMilliVolts(PHOTO_RESISTOR_1);
      //volagePhotoResistor2 = analogReadMilliVolts(PHOTO_RESISTOR_2);
  
      // print out the values you read:
      //Serial.printf("ADC analog value = %d\n",volagePhotoResistor1);
      //Serial.printf("ADC millivolts value = %d\n",volagePhotoResistor2);

      // Update the values of sensors
      Firebase.setFloat(fbdo, F("Data/PhotoResistor1"), volagePhotoResistor1);
      Firebase.setFloat(fbdo, F("Data/PhotoResistor2"), volagePhotoResistor2);
      
      // Read the control mode
      controlMode = Firebase.getString(fbdo, F("/Data/ControlMode")) ? fbdo.to<const char *>() : fbdo.errorReason().c_str();

      // Read the manual control angle set point  
      String angleSPstr = Firebase.getString(fbdo, F("/Data/AngleSetPoint")) ? fbdo.to<const char *>() : fbdo.errorReason().c_str();
      angleSP = angleSPstr.toFloat();

      // Update the values of position angle and calculated angle
      Firebase.setFloat(fbdo, F("/Data/AnglePosition"), anglePosition);
      Firebase.setFloat(fbdo, F("/Data/AngleCalculated"), angleCalc);

    } else {
      Serial.println("No exite base de datos");
      
    }
  }
}

void myTimerCallback(void* arg) {
  float angleObj;

  // Define the angle that stepper motor must move it.
  if(controlMode == "Auto"){  
    Serial.println("Se activo el control automatico");
    angleCalc = 30.0;//controlAuto(volageFotoResistor1,volageFotoResistor2); 
    angleObj = angleCalc;

  } else if(controlMode == "Manual"){
    angleObj = angleSP;
    Serial.println("Se activo el control manual");
    
    Serial.printf("SetPoint en el control manual: %f\n", angleSP );

  }

  // define the direction of stepper motor
  if(anglePosition < angleObj){
    digitalWrite(DIR, HIGH);
    if (statePulse){
      anglePosition += angleStep;
      Serial.printf("Sumo el angluo %f\n",anglePosition);

    }
  }
  else if(anglePosition > angleObj){
    digitalWrite(DIR, LOW); 
    if (statePulse){
      anglePosition -= angleStep;
      Serial.printf("Resto el angulo %f\n",anglePosition);

    }
  }

  // define the step pulses of stepper motor
  if(statePulse){
    digitalWrite(STEP, HIGH);
    statePulse = false;

  } else{
    digitalWrite(STEP, LOW);
    statePulse = true;
  }
}