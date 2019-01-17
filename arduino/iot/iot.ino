
#include <WiFi.h>
#include <time.h>
#include <FirebaseESP32.h>
#include <ArduinoJson.h>

#define FIREBASE_HOST "HostName"
#define FIREBASE_AUTH "KeyName"
#define WIFI_SSID "MyASUS"
#define WIFI_PASSWORD "00000000"

const char* ntpServer = "pool.ntp.org";
const long  gmtOffset_sec = 3600;
const int   daylightOffset_sec = 3600;

const int irPin = 34;
int coin = 0;
time_t timer = getTimeStamp();
time_t uploadTime = timer + 10;

int getGoods = 0;
int trigPin = 18;                  //Trig Pin
int echoPin = 19;                  //Echo Pin
long duration, cm, inches;

void setup() {

  Serial.begin(115200);
  pinMode(irPin, INPUT);
  pinMode(echoPin, INPUT);
  pinMode(trigPin, OUTPUT);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);

  //init and get the time
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
}

void loop() {

  timer = getTimeStamp(); //更新時間
  
  //avoid wrong value of timestamp 
  if (timer < 100) { return; }
  
  //紅外線感測
  /*bool isDetected = digitalRead(irPin);
    
  if (isDetected) {
      Serial.println("投幣");
      coin = 1 + coin;
  }*/

  //超音波感測
  ultrasound();

  //上傳資料
  if (timer > uploadTime) {
    Serial.print("上傳");

    //get json
    StaticJsonBuffer<200> jsonBuffer;
    JsonObject& rawData = jsonBuffer.createObject();
    rawData["coin"] = coin;
    rawData["goods"] = getGoods;
    rawData["machine_num"] = 2;
    rawData["timestamp"] = String(getTimeStamp());
  
    Firebase.set("/rawData/2/" + String(getTimeStamp()), rawData);
    
    Serial.println(coin);
    uploadTime = timer + 10;
    coin = 0;
    getGoods = 0;
  }
  delay(500);
}

time_t getTimeStamp() {
  time_t t = time(NULL);
  return t;
}

void ultrasound() {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(5);
  digitalWrite(trigPin, HIGH);     // 給 Trig 高電位，持續 10微秒
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  pinMode(echoPin, INPUT);             // 讀取 echo 的電位
  duration = pulseIn(echoPin, HIGH);   // 收到高電位時的時間
 
  cm = (duration/2) / 29.1;         // 將時間換算成距離 cm
  
  if (cm < 15) {
    Serial.println("出貨");
    getGoods = 1 + getGoods;
  }
}
