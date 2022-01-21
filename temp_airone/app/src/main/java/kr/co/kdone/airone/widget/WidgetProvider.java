package kr.co.kdone.airone.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.SplashActivity;
import kr.co.kdone.airone.utils.CommonUtils;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;


/**
 * 위젯 Provider (화면 구성 및 이벤트 / Receiver / service)
 */

public class WidgetProvider  extends AppWidgetProvider {
    private static String TAG = WidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        CommonUtils.customLog(TAG,"onUpdate = " + count, Log.ERROR);

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget_new);

            //Event Refresh button
            Intent refreshIntent = new Intent(context, WidgetReceiver.class);
            refreshIntent.setAction(WidgetUtils.WIDGET_REPLACE_ACTION);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context,0, refreshIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.imgReplace, refreshPendingIntent);

            /*//Event OnOff button
            Intent onOffhIntent = new Intent(context, WidgetProvider.class);
            onOffhIntent.setAction(WidgetUtils.WIDGET_ONOFF_ACTION);
            onOffhIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent onOffPendingIntent = PendingIntent.getBroadcast(context,0, onOffhIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.imgOnOff, onOffPendingIntent);*/

            //Event login button.
            Intent appIntent = new Intent(context, SplashActivity.class);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_base, appPendingIntent);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        CommonUtils.customLog(TAG,"onReceive = " + action, Log.ERROR);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        CommonUtils.customLog(TAG,"onDeleted", Log.ERROR);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        CommonUtils.customLog(TAG,"onEnabled", Log.ERROR);

        Intent sendIntent = new Intent(ACTION_APPWIDGET_UPDATE);
        context.sendBroadcast(sendIntent, context.getString(R.string.br_permission));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        CommonUtils.customLog(TAG,"onDisabled", Log.ERROR);
    }
}