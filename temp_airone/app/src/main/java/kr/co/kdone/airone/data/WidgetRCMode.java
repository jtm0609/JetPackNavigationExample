package kr.co.kdone.airone.data;

/**
 * ikHwang 2019-08-09 오전 10:36 룸콘 동작 상태
 * 위젯에서 룸콘의 동작 상태를 확인하기 위해 사용
 */
public class WidgetRCMode {
    int power;
    int mode;
    int optionMode;
    int oduMode;
    int uvLedMode;
    int uvLedState;

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getOptionMode() {
        return optionMode;
    }

    public void setOptionMode(int optionMode) {
        this.optionMode = optionMode;
    }

    public int getOduMode() {
        return oduMode;
    }

    public void setOduMode(int oduMode) {
        this.oduMode = oduMode;
    }

    public int getUvLedMode() {
        return uvLedMode;
    }

    public void setUvLedMode(int uvLedMode) {
        this.uvLedMode = uvLedMode;
    }

    public int getUvLedState() {
        return uvLedState;
    }

    public void setUvLedState(int uvLedState) {
        this.uvLedState = uvLedState;
    }
}
