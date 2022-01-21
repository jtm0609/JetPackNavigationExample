package kr.co.kdone.airone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import kr.co.kdone.airone.activity.SplashActivity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.SharedPrefUtil;

/**
 * ikHwang 2019-06-04 오전 9:47 핑거 푸시
 */
public class IntentService extends FirebaseMessagingService {
    private final String TAG = getClass().getSimpleName();

    @Override
    public void onNewToken(String token) {
        CommonUtils.customLog(TAG, "Refreshed token : " + token, Log.ERROR);
        SharedPrefUtil.putString(SharedPrefUtil.FCMKEY, token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> item = remoteMessage.getData();
        for (String key : item.keySet()) {
            CommonUtils.customLog(TAG, "&&&& Key : " + key + " = " + item.get(key), Log.ERROR);
        }

        if (remoteMessage.getData().size() > 0) {
            //알림 터치시 사용할 이벤트임.
            Intent startIntent = new Intent(getApplicationContext(), SplashActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String description = getResources().getString(R.string.app_name);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(getPackageName(), description, importance);
                channel.setDescription(description);

                mNotificationManager.createNotificationChannel(channel);
            }

            if (remoteMessage.getData().containsKey("Args") && remoteMessage.getData().containsKey("Message")) {
                try {
                    String args = URLDecoder.decode(remoteMessage.getData().get("Args"), "utf-8");
                    JSONObject data = new JSONObject(args);

                    Iterator<String> iter = data.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        startIntent.putExtra(key, data.getString(key));
                    }

                    String msg = remoteMessage.getData().get("Message");

                    final int currentID = (int) System.currentTimeMillis();
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), currentID, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    //pendingintent 는 인텐트 객체 이회에 부가 정보가 더 설정되어야 하므로 pending intent 객체에 실어서 보냄.
                    //intent 발생을 의뢰해야 할 때 사용한다.
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getPackageName())
                            .setSmallIcon(R.drawable.small_noti_icon_airone)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.kr_android_app_512))
                            .setContentText(msg)
                            .setContentTitle(getResources().getString(R.string.app_name));

                    NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(mBuilder);
                    bigTextStyle.bigText(msg);

                    mBuilder.setContentIntent(pi);  //인텐트 발생 의뢰를 위한 객체를 실어서 보냄.
                    mBuilder.setAutoCancel(true);
                    mBuilder.setStyle(bigTextStyle);
                    mNotificationManager.notify(currentID, mBuilder.build()); //알림을 noti 하게 한다.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}