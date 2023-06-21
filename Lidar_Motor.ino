#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include <Wire.h>
#include <LIDARLite.h>
#include <Servo.h>

// Set these to run example.
#define FIREBASE_HOST "temi-bb3c2-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "BMEV757wSYWnLY9p4t2DroKaVMdcDfJzNCz5vtsi"

#define WIFI_SSID "Oys"
#define WIFI_PASSWORD "z1177618"

LIDARLite myLidarLite;
Servo myServo;
int previousDistance = 0;

void setup()
{
  Serial.begin(115200); // Initialize serial connection to display distance readings
  
  // connect to wifi.
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH); // Connect to Firebase

  myServo.attach(D5);

  myLidarLite.begin(0, true); // Set configuration to default and I2C to 400 kHz
  myLidarLite.configure(0); // Change this number to try out alternate configurations
  
  myServo.write(45);
  delay(1000);
}

void loop()
{
  int live = getValue("/member/two");
  Serial.print("Live");
  Serial.println(live);
  if(live == 1){
    int game = getValue("/game");
    Serial.print("Game");
    Serial.println(game);
    if (game == 1){ // 센서 on firebase 변수 들어오면
      Serial.println("Start!");
      previousDistance = -1;
      // 테미 구호 제창만큼 딜레이
      for (int i=0; i<10; i++){
        // 조교 테미가 감지
        Catching(); // Catching 안의 delay를 통해 시간초 조절 지금은 1초를 60번 돌리니 1분동안 탐지
        
        if(getValue("/member/two")==0){ // 사용자 탈락시
          // if(){ // 테미 멈춤 시
          Movemotor();
          Serial.println("Catch!");
          break;
        }
      }
      // firebase에 구호 제창 변수 입력
      Firebase.set("/game", 0);
      Serial.println("End!");
    } else{
      Serial.print("Game");
      Serial.println(game);
    }
  } else{
    Serial.print("Live");
    Serial.println(live);
  }
}


// 파이어 베이스 값 가져오는 함수
int getValue(String path)
{
  int value = -1;
  FirebaseObject firebaseData = Firebase.get(path);

  if (firebaseData.success()){
    value = firebaseData.getJsonVariant().as<int>();
  } else {
    Serial.println("ERROR!");
  }

  return value;
}


// 조교 테미가 사용자 잡는 함수
void Catching()
{
  int currentDistance = myLidarLite.distance(); // Get current distance measurement

  if ((abs(currentDistance - previousDistance) > ((currentDistance / 200) + 100)) && previousDistance != -1) {
    Firebase.set("/member/one", 0);
  }

  Serial.println(currentDistance); // Print current distance to serial monitor

  previousDistance = currentDistance; // Update previous distance for next iteration

  delay(1000); // Wait for 1 second
}


void Movemotor()
{
    myServo.write(45);
    delay(1000);
    myServo.write(135);
    delay(1000);
    myServo.write(45);
    delay(1000);
}