package kr.co.kdone.airone.data;

import android.os.Build;
import android.text.TextUtils;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import androidx.annotation.RequiresApi;

/**
 * KDAirOne_Prism
 * Class: AirMonitor
 * Created by ikHwang
 * Since : 2021-08-11 오전 10:51.
 * Description :
 */
public enum AirMonitor {
    UNKNOWN("unknown"), BOX("box"), PRISM("prism"), RADER("rader");

    private String modelName;

    AirMonitor(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    public static AirMonitor find (String modelName){
        if(TextUtils.isEmpty(modelName)) return AirMonitor.UNKNOWN;

        switch (modelName){
            case "box": return AirMonitor.BOX;
            case "prism": return AirMonitor.PRISM;
            case "rader": return AirMonitor.RADER;
            default: return AirMonitor.UNKNOWN;
        }
    }
}
