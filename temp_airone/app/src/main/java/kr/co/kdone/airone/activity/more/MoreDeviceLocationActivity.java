package kr.co.kdone.airone.activity.more;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.activity.SplashActivity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_GPS_OFF;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;

/**
 * ikHwang 2019-06-04 오전 9:50 더보기 위치 변경
 */
public class MoreDeviceLocationActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Activity act;
    private RelativeLayout layoutMapView;
    private TextView txtLocation;

    private String gid, area;
    private double lat;
    private double lng;
    private MapView mapView;
    private MapPOIItem marker;
    private MapPoint gpsLatLng;
    private MapPoint selectLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_device_location);

        act = this;

        gid = getIntent().getStringExtra("gid");
        lat = Double.parseDouble(getIntent().getStringExtra("lat"));
        lng = Double.parseDouble(getIntent().getStringExtra("lng"));

        layoutMapView = findViewById(R.id.layout_map_view);
        txtLocation = findViewById(R.id.txtLocation);

        initMap();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, 123);
            return;
        }else{
            setGpsLocation();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_GPS_OFF:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.layoutCurrentLocation:
                if (checkGPSService()) {
                    if (gpsLatLng != null) {
                        setMarker(gpsLatLng);
                    } else {
                        setGpsLocation();
                        CommonUtils.showToast(MoreDeviceLocationActivity.this, getString(R.string.gps_info_error));
                    }
                }
                break;

            case R.id.layoutOK:
                if (selectLatLng == null) {
                    CommonUtils.showToast(MoreDeviceLocationActivity.this, getString(R.string.toast_result_no_location));
                } else {
                    displayProgress(this, "", "");
                    try {
                        HttpApi.PostV2ChangeLocation( CleanVentilationApplication.getInstance().getUserInfo().getId(),
                                    (float)selectLatLng.getMapPointGeoCoord().latitude,
                                    (float)selectLatLng.getMapPointGeoCoord().longitude,
                                    area, gid,
                                new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        DismissConnectDialog();
                                        CommonUtils.showToast(MoreDeviceLocationActivity.this, getString(R.string.toast_result_can_not_change_device_info));
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        try {
                                            String strResponse = response.body().string();

                                            if(TextUtils.isEmpty(strResponse)){
                                                CommonUtils.showToast(MoreDeviceLocationActivity.this, getString(R.string.toast_result_can_not_change_device_info));
                                            }else {
                                                CommonUtils.customLog(TAG, "device/change-location : " + strResponse, Log.ERROR);

                                                JSONObject json_data = new JSONObject(strResponse);

                                                switch (json_data.getInt("code")){
                                                    case HttpApi.RESPONSE_SUCCESS:
                                                        CommonUtils.showToast(MoreDeviceLocationActivity.this, getString(R.string.toast_result_change_save));
                                                        finish();
                                                        break;

                                                    default:
                                                        CommonUtils.showToast(MoreDeviceLocationActivity.this, getString(R.string.toast_result_can_not_change_device_info));
                                                        break;
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            CommonUtils.showToast(MoreDeviceLocationActivity.this, getString(R.string.toast_result_can_not_change_device_info));
                                        } finally {
                                            DismissConnectDialog();
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        DismissConnectDialog();
                        CommonUtils.showToast(MoreDeviceLocationActivity.this, getString(R.string.toast_result_can_not_change_device_info));
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_right_out);
    }

    public void initMap() {
        mapView = new MapView(act);
        layoutMapView.addView(mapView);
        mapView.setMapViewEventListener(mapViewEventListener);

        selectLatLng = MapPoint.mapPointWithGeoCoord(lat, lng);
        setMarker(selectLatLng);
    }

    private void setMarker(final MapPoint point) {
        MapReverseGeoCoder geoCoder = new MapReverseGeoCoder(getString(R.string.kakao_map_key), point, new MapReverseGeoCoder.ReverseGeoCodingResultListener() {
            @Override
            public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
                CommonUtils.customLog(TAG, "address : " + s, Log.ERROR);

                if (marker != null) {
                    mapView.removePOIItem(marker);
                }

                if(s.contains("동 ")){
                    s = s.substring(0,s.indexOf("동 ")) + "동";
                }else if(s.contains("면 ")){
                    s = s.substring(0,s.indexOf("면 ")) + "면";
                }else if(s.contains("읍 ")){
                    s = s.substring(0,s.indexOf("읍 ")) + "읍";
                }

                txtLocation.setText(s);
                area = s;

                marker = new MapPOIItem();
                marker.setItemName(s);
                marker.setTag(0);
                marker.setMapPoint(point);
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.map_pin);

                mapView.addPOIItem(marker);
                mapView.setMapCenterPoint(point, true);
            }

            @Override
            public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
                if (marker != null) {
                    mapView.removePOIItem(marker);
                }
                txtLocation.setText(R.string.unknown_location);

                marker = new MapPOIItem();
                marker.setItemName(getString(R.string.unknown_location));
                marker.setTag(0);
                marker.setMapPoint(point);
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.map_pin);

                mapView.addPOIItem(marker);
                mapView.setMapCenterPoint(point, true);
            }
        }, act);
        geoCoder.startFindingAddress();

        selectLatLng = point;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setGpsLocation();
        }
    }

    private void setGpsLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, 123);
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location lastLocation = null;

        lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(null != lastLocation){
                    gpsLatLng = MapPoint.mapPointWithGeoCoord(lastLocation.getLatitude(), lastLocation.getLongitude());
                }else{
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    if(lastLocation != null){
                gpsLatLng = MapPoint.mapPointWithGeoCoord(lastLocation.getLatitude(), lastLocation.getLongitude());
            }else{
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1,
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                CommonUtils.customLog(TAG, "NETWORK_PROVIDER lat : " + latitude + ", lng : " + longitude, Log.ERROR);

                                if(null == gpsLatLng){
                                    gpsLatLng = MapPoint.mapPointWithGeoCoord(latitude, longitude);
                                }
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                            }
                        });

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1,
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                CommonUtils.customLog(TAG, "GPS_PROVIDER lat : " + latitude + ", lng : " + longitude, Log.ERROR);

                                if(null == gpsLatLng){
                                    gpsLatLng = MapPoint.mapPointWithGeoCoord(latitude, longitude);
                                }
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                            }
                        });
            }
        }
    }

    /**
     * 위치정보 ON/OFF 체크
     */
    public boolean checkGPSService() {
        LocationManager manager = (LocationManager) act.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS OFF 일때 Dialog 표시
            Intent intent = new Intent(act, PopupActivity.class);
            intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
            intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.ok));
            intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.gps_off));
            act.startActivityForResult(intent, RESULT_GPS_OFF);
            act.overridePendingTransition(0, 0);
            return false;
        } else {
            return true;
        }
    }

    MapView.MapViewEventListener mapViewEventListener = new MapView.MapViewEventListener() {
        @Override
        public void onMapViewInitialized(MapView mapView) {
        }

        @Override
        public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
        }

        @Override
        public void onMapViewZoomLevelChanged(MapView mapView, int i) {
        }

        @Override
        public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
            setMarker(mapPoint);
        }

        @Override
        public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
        }

        @Override
        public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
        }

        @Override
        public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        }

        @Override
        public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
        }

        @Override
        public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        }
    };
}
