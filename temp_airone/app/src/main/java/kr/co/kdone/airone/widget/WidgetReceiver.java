package kr.co.kdone.airone.widget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.data.home.CommonHomeInfo;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HomeInfoDataParser;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.utils.SharedPrefUtil;
import libs.espressif.utils.TextUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static kr.co.kdone.airone.utils.CommonUtils.getColorIdIdToSensorLevel;
import static kr.co.kdone.airone.utils.CommonUtils.getIconIdToTotalLevel;
import static kr.co.kdone.airone.utils.CommonUtils.getStringIdToSenerLevel;


/**
 * 위젯의 기능 및 화면 갱신을 처리하기 위한 Receiver.
 */

public class WidgetReceiver extends BroadcastReceiver {
    private String TAG = WidgetReceiver.class.getSimpleName();
    private Context mContext;

    private long WIDGET_DELAY_TIME = 60 * 1000;

    private CommonHomeInfo homeInfo;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();

        CommonUtils.customLog(TAG,"action : " + action, Log.ERROR);

         if(action.equals(ACTION_APPWIDGET_UPDATE) || action.equals(WidgetUtils.WIDGET_REPLACE_ACTION)){
            String rcID = SharedPrefUtil.getString(SharedPrefUtil.ROOM_CONTROLLER_ID, "");

            if(action.equals(WidgetUtils.WIDGET_REPLACE_ACTION)){
                long lastUpdateTime = SharedPrefUtil.getLong(SharedPrefUtil.WIDGET_LAST_UPDATE_TIME, 0);
                long currentTime = System.currentTimeMillis();

                if((currentTime - lastUpdateTime) < WIDGET_DELAY_TIME){
//                    return;
                }

                SharedPrefUtil.putLong(SharedPrefUtil.WIDGET_LAST_UPDATE_TIME, currentTime);
                changeLayout(context, null, -1, 3);
            }

            if(TextUtils.isEmpty(rcID)){
                changeLayout(context, null, -1, 1);
            }else{
                getInsideData(rcID);
            }
        }else if(action.equals(WidgetUtils.WIDGET_UPDATE_ACTION)){
            try {
                if(CleanVentilationApplication.getInstance().getHomeList().size() > 0)
                    homeInfo = CleanVentilationApplication.getInstance().getHomeList().get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            changeLayout(mContext, null, -1, 2);
        }
    }

    private void getInsideData(final String rcId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpApi.PostV2GetMainInfoWidget( //V2A 적용.
                            rcId,
                            new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    changeLayout(mContext, null, -1, 0);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        String strResponse =  response.body().string();

                                        CommonUtils.customLog(TAG, "main/widget : " + strResponse, Log.ERROR);

                                        if(android.text.TextUtils.isEmpty(strResponse)){
                                            changeLayout(mContext, null, -1, 0);
                                        }else{
                                            JSONObject rawJson = new JSONObject(strResponse);

                                            switch (rawJson.optInt("code", 0)){
                                                case HttpApi.RESPONSE_SUCCESS:
                                                    if(rawJson.has("data")){
                                                        // ikHwang 2019-05-21 오후 1:32 메인화면 이동시 화면을 구성하기 위해 데이터 파싱
                                                        HomeInfoDataParser.paserHomeInfo(CleanVentilationApplication.getInstance(), rawJson.getJSONObject("data"), false);
                                                    }

                                                    homeInfo = CleanVentilationApplication.getInstance().getHomeList().get(0);

                                                    changeLayout(mContext, null, -1, 2);
                                                    break;

                                                default:
                                                    changeLayout(mContext, null, -1, 0);
                                                    break;
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        changeLayout(mContext, null, -1, 0);
                                    }
                                }
                            });
                }catch (Exception e){
                    e.printStackTrace();
                    changeLayout(mContext, null, -1, 0);
                }
            }
        }).start();
    }

    private void changeLayout(Context context, RemoteViews remoteViews, int widgetId, int state){
        if(remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget_new);
        }

        try {
            switch (state){
                case 1: // 로그인 오류 (자동로그인 안되어 있음)
                    remoteViews.setViewVisibility(R.id.layoutControl, View.INVISIBLE);
                    remoteViews.setViewVisibility(R.id.base_info, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.base_progress, View.GONE);
                    remoteViews.setTextViewText(R.id.text_info, context.getString(R.string.layout_widget_new_str_17));

                    if(widgetId < 0) {
                        ComponentName componentName = new ComponentName(context, WidgetProvider.class);
                        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
                    }
                    return;

                case 2: // 데이터 있음
                    remoteViews.setViewVisibility(R.id.layoutControl, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.base_info, View.GONE);
                    remoteViews.setViewVisibility(R.id.base_progress, View.GONE);
                    break;

                case 3: // 로딩 프로그래스바
                    remoteViews.setViewVisibility(R.id.layoutControl, View.INVISIBLE);
                    remoteViews.setViewVisibility(R.id.base_info, View.GONE);
                    remoteViews.setViewVisibility(R.id.base_progress, View.VISIBLE);

                    if(widgetId < 0) {
                        ComponentName componentName = new ComponentName(context, WidgetProvider.class);
                        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
                    }
                    return;

                default: // 정보 조회 오류
                    remoteViews.setViewVisibility(R.id.layoutControl, View.INVISIBLE);
                    remoteViews.setViewVisibility(R.id.base_info, View.VISIBLE);
                    remoteViews.setTextViewText(R.id.text_info, context.getString(R.string.layout_widget_new_str_14));
                    remoteViews.setViewVisibility(R.id.base_progress, View.GONE);
                    break;
            }

            if(null == homeInfo){
                remoteViews.setViewVisibility(R.id.layoutControl, View.INVISIBLE);
                remoteViews.setViewVisibility(R.id.base_info, View.VISIBLE);
                remoteViews.setTextViewText(R.id.text_info, context.getString(R.string.layout_widget_new_str_14));

                return;
            }else{
                if(CleanVentilationApplication.getInstance().isIsOldUser()){
                    remoteViews.setViewVisibility(R.id.layoutOnOff, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.base_pm1, View.GONE);
                    remoteViews.setViewVisibility(R.id.layouttotal, View.GONE);
                }else{
                    remoteViews.setViewVisibility(R.id.layoutOnOff, View.GONE);
                    remoteViews.setViewVisibility(R.id.base_pm1, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.layouttotal, View.VISIBLE);
                }
            }

            try {
                if(null != CleanVentilationApplication.getInstance().getHomeList().get(0).getInside()){
                    String updateTime = SharedPrefUtil.getString(SharedPrefUtil.WIDGET_UPDATE_TIME, "");
                    remoteViews.setTextViewText(R.id.txtLocationTime, TextUtils.isEmpty(updateTime) ? "" : updateTime + " " + context.getString(R.string.layout_widget_new_str_18));

                    if(CleanVentilationApplication.getInstance().isIsOldUser()){
                        if(null != CleanVentilationApplication.getInstance().getHomeList().get(0).getWidgetRCMode()){
                            if(2 == CleanVentilationApplication.getInstance().getHomeList().get(0).getWidgetRCMode().getPower()){
                                remoteViews.setTextColor(R.id.txtOnOff, context.getResources().getColor(R.color.control_power_on));
                                remoteViews.setTextViewText(R.id.txtOnOff, context.getString(R.string.layout_widget_new_str_2));
                            }else{
                                remoteViews.setTextColor(R.id.txtOnOff, context.getResources().getColor(R.color.control_power_off));
                                remoteViews.setTextViewText(R.id.txtOnOff, context.getString(R.string.layout_widget_new_str_1));
                            }
                        }
                    }else{
                        int insideTotalLevel = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTotalLevel();
                        remoteViews.setImageViewResource(R.id.img_widget_total, getIconIdToTotalLevel(insideTotalLevel));
                        remoteViews.setTextViewText(R.id.text_widget_total, String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTotalValue())));

                        int insidePM1Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDust1Level();
                        setImageView(context, remoteViews,"inside_pm1_level_%01d", insidePM1Level);

                        remoteViews.setTextViewText(R.id.text_inside_pm1_level, context.getString(getStringIdToSenerLevel(insidePM1Level)));
                        remoteViews.setTextViewText(R.id.text_inside_pm1_unit, String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDust1Value())) + " "  + context.getString(R.string.layout_widget_new_str_6));
                    }

                    int insidePM25Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDustLevel();
                    setImageView(context, remoteViews,"inside_pm25_level_%01d", insidePM25Level);

                    remoteViews.setTextViewText(R.id.text_inside_pm25_level, context.getString(getStringIdToSenerLevel(insidePM25Level)));
                    remoteViews.setTextViewText(R.id.text_inside_pm25_unit, String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDustValue())) + " " + context.getString(R.string.layout_widget_new_str_6));

                    int insideCO2Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getCo2Level();
                    setImageView(context, remoteViews,"inside_co2_level_%01d", insideCO2Level);

                    remoteViews.setTextViewText(R.id.text_inside_co2_level, context.getString(getStringIdToSenerLevel(insideCO2Level)));
                    remoteViews.setTextViewText(R.id.text_inside_co2_unit, String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getCo2Value())) + " "  + context.getString(R.string.layout_widget_new_str_7));

                    int insideTVOCLevel = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTvocLevel();
                    setImageView(context, remoteViews,"inside_tvoc_level_%01d", insideTVOCLevel);

                    remoteViews.setTextViewText(R.id.text_inside_tvoc_level, context.getString(getStringIdToSenerLevel(insideTVOCLevel)));
                    remoteViews.setTextViewText(R.id.text_inside_tvoc_unit, String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTvocValue())) + " "  + context.getString(R.string.layout_widget_new_str_8));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(widgetId < 0) {
                ComponentName componentName = new ComponentName(context, WidgetProvider.class);
                AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * ikHwang 2019-08-09 오전 10:29 위젯에서 View 객체를 사용할 수 없어 xml의 stroke값이 적용되지 않아 레이아웃을 레벨별로 만들어 숨김 처리함
     * @param context
     * @param remoteViews
     * @param format
     * @param nLevel
     */
    private void setImageView(Context context, RemoteViews remoteViews, String format, int nLevel){
        for(int i=0; i<5; i++){
            String str = String.format(format, i);
            int resId = context.getResources().getIdentifier(str, "id", context.getPackageName());

            if(i == nLevel){
                remoteViews.setViewVisibility(resId, View.VISIBLE);
            }else{
                remoteViews.setViewVisibility(resId, View.GONE);
            }
        }
    }
}
