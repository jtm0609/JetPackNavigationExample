package kr.co.kdone.airone.data;

import static kr.co.kdone.airone.utils.ProtocolType.MODE_NOT_USED;
import static kr.co.kdone.airone.utils.ProtocolType.USE_NOT;

/**
 * DID 패킷 정보.
 */
public class DidPacket {
    public int vspRpmControl;                   // 1byte VSP/PRM 제어   01:RPM제어 02:VSP제어, 03:DF-VSP제어, 04:KPV-VSP제어
    public int oduModel;                        // 1byte ODU Model 판단   01:11KW, 02: 9KW, 03:7KW 04:5KW, 05:PV히터형, 06:온수형, 07:일반형
    public int maxFilterUseTime = 0;            // 필터 사용량 (룸콘에서 변경 가능하여 추가함)
    public int apsHasFlag = 0;                  // ODU에서 APS 기능 적용 유무
    public int oduCapacity = 0;                 // 확인불가 : 01 , 10S: 02, 15S : 03, 20S : 04, 25S : 05, 30S : 06
    public int oduHeaterCapacity = 0;           // 확인불가 : 01 , S0(히터 X) : 02, S6(600W) : 03
    public int uvLED = 0;                       // UV LED
    public int supportByPass = 0;
    public long lastUpdateTime;

    public ModeOperation coldMode;              // 냉방모드
    public ModeOperation hotMode;               // 난방모드
    public ModeOperation dehumiMode;            // 제습모드
    public ModeOperation humiMode;              // 가습모드
    public ModeOperation ventiMode;             // 환기모드
    public ModeOperation exhaustMode;           // 배기모드
    public ModeOperation airCleanMode;          // 공기청정모드
    public ModeOperation kitchenMode;           // 주방모드
    public ModeOperation cleanMode;             // 청소모드
    public ModeOperation dustMode;              // 황사모드
    public ModeOperation healthMode;            // 건강호흡모드
    public ModeOperation skinMode;              // 피부보습모드
    public ModeOperation autoMode;              // 자동모드
    public ModeOperation aerationMode;          // 급기모드
    public ModeOperation bypassMode;            // 바이패스 모드

    public SetMinMax coldSetTemp;               // 냉방 희망온도 설정값
    public SetMinMax dehumiSetHumi;             // 제습 희망습도 설정값
    public SetMinMax humiSetHumi;               // 가습 희망습도 설정값
    public SetMinMax autoSetTemp;               // 자동 희망온도 설정값
    public SetMinMax autoSetHumi;               // 자동 희망온도 설정값
    public SetMinMax hotSetTemp;                // 온풍 희망습도 설정값
    public SetMinMax pwrSavingColdSetTemp;      // 절전냉방 희망온도 설정값

    public DidPacket() {
        coldMode = new ModeOperation();
        hotMode = new ModeOperation();
        dehumiMode = new ModeOperation();
        humiMode = new ModeOperation();
        ventiMode = new ModeOperation();
        exhaustMode = new ModeOperation();
        airCleanMode = new ModeOperation();
        kitchenMode = new ModeOperation();
        cleanMode = new ModeOperation();
        dustMode = new ModeOperation();
        healthMode = new ModeOperation();
        skinMode = new ModeOperation();
        autoMode = new ModeOperation();
        aerationMode = new ModeOperation();
        bypassMode = new ModeOperation();

        coldSetTemp = new SetMinMax();
        dehumiSetHumi = new SetMinMax();
        humiSetHumi = new SetMinMax();
        autoSetTemp = new SetMinMax();
        autoSetHumi = new SetMinMax();
        hotSetTemp = new SetMinMax();
        pwrSavingColdSetTemp = new SetMinMax();
        lastUpdateTime = -1;
    }

    public class ModeOperation {
        public int modeUse; //1byte 모드-운전 유무설정     01:유, 02: 무
        public int turboUse; //1byte 터보운전 유무설정     01:유, 02: 무
        public int pwrSavingUse; //1byte 절전운전 유무설정     01:유, 02: 무
        public int sleepUse; //1byte 취침운전 유무설정     01:유, 02: 무
        public int reservationOffUse; //1byte 꺼짐예약 유무설정     01:유, 02: 무
        public int reservationOnUse; //1byte 켜짐예약 유무설정     01:유, 02: 무

        public ModeOperation() {
            modeUse = MODE_NOT_USED;
            turboUse = MODE_NOT_USED;
            pwrSavingUse = MODE_NOT_USED;
            sleepUse = MODE_NOT_USED;
            reservationOffUse = MODE_NOT_USED;
            reservationOnUse = MODE_NOT_USED;
        }
    }

    public class SetMinMax {
        public int min; // //1byte 온도*2 -> HEX변환 후 전달 ex>35도 35*2 -> 0x46
        public int max; // //1byte 온도*2
    }
}
