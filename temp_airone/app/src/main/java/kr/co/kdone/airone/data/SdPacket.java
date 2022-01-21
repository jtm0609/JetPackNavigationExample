package kr.co.kdone.airone.data;

import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_STATUS_ON;

/**
 * 상태 정보 획득을 위한 패ㅣ킷
 */

public class SdPacket {
    public int errorCD; //2byte 02 CD(715)통신에러(룸콘/컨트롤러) *기기별코드표 확인
    public int errorLevel; //1byte 01:default, N:Level값

    public int errorLevel_L; // 0-NotError, 1-level1(에러상태 표시, 정상운전), 2-level2(에러상태 표시, 운전정지), 3-level3(에러상태 표시, 운전정지)
    public int errorLevel_H; // 0-NotAlarm, 1-level1(화면표시), 2-level2(화면표시 & 소리알림), 3-level3(화면표시 & 소리알림)

    public int operationStatus; //1byte 정지/운전 상태  01:정지, 02:가동
    public int operationMode; //1byte 동작모드 01:대기, 02:냉방, 03:제습, 04:일반환기, 05:배기, 06:요리, 07:청소, 08:공기청정 09:황사, 0A:난방, 0B:가습, 0C:자동운전, 0D:건강호흡, 0E:피부보호, 0F:급기
    public int oduOperationStatus; //1byte ODU 동작상태  01:대기, 02:냉방, 03:제습, 04:일반환기, 05:배기, 06:요리, 07:청소, 08:공기청정 09:황사, 0A:난방, 0B:가습, 0C:자동운전, 0D:건강호흡, 0E:피부보호, 0F:급기
    public int option; //1byte 옵션기능 01:없음, 02:터보운전, 03:절전운전, 04:숙면모드
    public int reservation; //1byte 예약기능   01:없음, 02:취침예약, 03:운전예약, 04:정지예약
    public int setWindLevel;//1byte 풍량 설정 값 01:미풍, 02:약풍, 03:강풍, 04:자동
    public int currontOduWindLevel; //1byte ODU 풍량 현재값 01:미풍, 02:약풍, 03:강풍, 04:자동
    public int windLevelPercent; //1byte 풍량 설정 값(중국향) 5~99% -> HEX 변환 후 전달 ex> 95 -> 0x5F
    public int setTemp;//1byte 설정온도 온도*2 ->HEX변환 후 전달.
    public int curruntTemp; //1byte 현재온도 온도*2 ->HEX변환 후 전달.
    public int setHumi; //1byte 설정습도  온도*2 ->HEX변환 후 전달.
    public int curruntHumi; //1byte 현재습도  온도*2 ->HEX변환 후 전달.
    public int setReservationTimer; //1byte 예약 운전 설정 값   분단위 -> HEX 전달
    public int remainReservationTimer; //1byte 남은 운전 예약 시간 값    분단위 -> HEX 전달
    public int setSwing; //1byte 루퍼 스윙 동작 설정 값  01:swing off, 02:swing on
    public int flagHepaFilter; //1byte 헤파필터 시간경과 플래그  01:Normal, 02:리셋시간 Over
    public int flagFreeFilter; //1byte 프리필터 시간경과 플래그  01:Normal, 02:리셋시간 Over
    public int usingTimeOduFilter; //1byte ODU 필터 시간   1시간 단위 -> HEX 전달 ex>10시간 -> 0x0A
    public SensorValue dustSensor; //먼지센서값
    public SensorValue co2Sensor; //co2 센서값
    public SensorValue vocSensor; //VOC 센서값
    public int flagInnerHeaterOperation;  //1byte 내부 히터 가동 플래그   01:비가동, 02:가동
    public int flagOuterCloseInput; //1byte  외부 접점 입력 플래그   01:내부, 02:내부접점입력(후드입력)
    public int oduHeatSourceOperationRequest; //1byte ODU 열원 가동/정지 요청상태  01:비요청, 02:요청
    public int oduRoomsFanOperationRequest; //1byte ODU 각실 FLAP/FAN동작 요청상태   01:비요청, 02:요청
    public int indoorUnitLightingLevel; //1byte 실내기 라이팅 밝기 값  HEX(0~100%)
    public int indoorUnitSoundLevel; //1byte 실내기 부저음 음량 값  HEX(0~100%)
    public int controlRoom1; //1byte 1번방 제어
    public int controlRoom2; //1byte 2번방 제어
    public int controlRoom3; //1byte 3번방 제어
    public int controlRoom4; //1byte 4번방 제어
    public int controlRoom5; //1byte 5번방 제어
    public int controlRoom6; //1byte 6번방 제어
    public int controlRoom7; //1byte 7번방 제어
    public int controlRoom8; //1byte 8번방 제어
    public int controlDressRoom; //1byte 각실 제어(드레스룸)
    public int usingTimeOduFreeFilter; //2byte ODU 프리필터 사용시간
    public int deepSleepUse; // 1byte 숙면모드 설정/해제
    public int deepSleepStart; // 1byte 숙면모드 시작시간
    public int deepSleepEnd; // 1byte 숙면모드 종료시간
    public int sbErrorCD; // 2byte 센싱박스 에러코드
    public int sbErrorLevel; // 1byte 01:default, N:level값
    public int sbDustSensor; // 1byte 먼지센서 상태 01:정상, 02:오류
    public int sbCO2Sensor; // 1byte CO2센서 상태 01:정상, 02:오류
    public int sbVocSensor; // 1byte Voc센서 상태 01:정상, 02:오류
    public int sbDustLevel; // 1byte 먼지센서 정성값 01:좋음, 02:보통, 03:나쁨, 04:매우나쁨, 00:오류
    public int sbDustValue; // 2byte 먼지센서 정량값 2byte Hex 값  ex : 1000 -> 03E8, 0000:오류
    public int sbCO2Level; // 1byte CO2센서 정성값 01:좋음, 02:보통, 03:나쁨, 04:매우나쁨, FF:오류
    public int sbCO2Value; // 2byte CO2센서 정량값 2byte Hex 값  ex : 1000 -> 03E8
    public int sbVocLevel; // 1byte VOC센서 정성값 01:좋음, 02:보통, 03:나쁨, 04:매우나쁨, FF:오류
    public int sbVocValue; // 2byte VOC센서 정량값 2byte Hex 값  ex : 1000 -> 03E8
    //2019.04.15 추가
    public int dustPMSensorLevel; // 1byte 먼지센서(PM 1.0) 도출 정성값. 좋음 : 01, 보통 : 02, 나쁨 : 03, 매우나쁨 : 04      *해당 센서 오류시 default 0x00
    public int dustPMSensorValue; // 2byte 먼지센서(PM 1.0) 도출 정량값. 2byte Hex 값  ex : 1000 -> 03E8

    public int sbAirLevel; // 1byte 통합공기질 정성값 01:좋음, 02:보통, 03:나쁨, 04:매우나쁨, FF:오류
    public int sbAirValue; // 2byte 통합공기질 정량값 2byte Hex 값  ex : 1000 -> 03E8
    public int sbInsideHeat; // 2byte 현재온도 온도*2 ->HEX변환 후 전달.
    public int sbInsideHum; // 1byte 현재습도 습도*2 ->HEX변환 후 전달.
    public int sbPower; // 1byte 센싱박스 전원 01:Off, 02:On
    public long lastUpdateTime;

    public int condensationOperation;       // 결로운전 요청상태   01:비요청, 02:요청
    public int bypassOperation;             // 바이패스 요청상태   01:비요청, 02:요청
    public int connectedHomenet;            // 홈넷 연동 상태      미연동 : 01, 연동 : 02

    public int oduFreeFilterAlarm;          // 프리필터 청소 알림 플래그 (사용안함)
    public int oduHepaFilterAlarm;          // 전자헤파 필터 청소 알림 플래그 (사용안함)
    public int oduFreeAndHepaFilterAlarm;   // 프리 + 전자헤파 필터 청소 알림 플래그 (사용안함)

    public int diffuserNumber;              // 전동 디퓨저 번호
    public int diffuserModel;               // 1 : sa디퓨저, 2 : ea디퓨저
    public int diffuserRoomNumber;          // 방번호
    public int filterUseTime;               // 필터 사용시간

    public int uvLedMode;                  // UV LED 설정상태 1 - 해제, 2 - 설정
    public int uvLedState;                  // UV LED 상태 1 - Off, 2 - On

    public SdPacket() {
        dustSensor = new SensorValue(); //먼지센서값
        co2Sensor = new SensorValue(); //co2 센서값
        vocSensor = new SensorValue(); //VOC 센서값
        lastUpdateTime = -1;
    }

    public class SensorValue {
        public int level; //1byte 레벨  01:좋음, 02:보통, 03:나쁨, 04:매우나쁨
        public int value; //2byte 값  ex>1000 -> 0x03E8
    }
}
