package kr.co.kdone.airone.fragments.prism;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.activity.register.prism.RegisterPrismDeviceActivity;
import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ONLY_REG;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_REG;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_GPS_OFF;

public class RegisterPrismDevice4Fragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private RegisterPrismDeviceActivity act;
    private RelativeLayout layoutMapView;
    private TextView txtLocation;

    private MapView mapView;
    private MapPOIItem marker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        act = (RegisterPrismDeviceActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_register_device4, container, false);

        layoutMapView = view.findViewById(R.id.layout_map_view);
        txtLocation = view.findViewById(R.id.txtLocation);

        mapView = new MapView(act);
        layoutMapView.addView(mapView);
        mapView.setMapViewEventListener(mapViewEventListener);

        view.findViewById(R.id.layoutOK).setOnClickListener(this);
        view.findViewById(R.id.layoutCurrentLocation).setOnClickListener(this);

        if (act.mode == MODE_DEVICE_REG) { //룸콘 신규 등록
            ((TextView)view.findViewById(R.id.txtOK)).setText(R.string.next);
        } else if (act.mode == MODE_DEVICE_ONLY_REG) { //룸콘 재 등록
            ((TextView)view.findViewById(R.id.txtOK)).setText(R.string.ok);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.layoutCurrentLocation:
                if (checkGPSService()) {
                    if (act.gpsLatLng != null) {
                        setMarker(act.gpsLatLng);
                    } else {
                        CommonUtils.showToast(act, getString(R.string.gps_info_error));
                    }
                }
                break;

            case R.id.layoutOK: // 룸콘 서버에 등록 확인 처리
                act.registerCount = 0;
                act.deviceCheckState();
                break;
        }
    }

    public void initMap() {
        act.selectLatLng = act.gpsLatLng == null ? MapPoint.mapPointWithGeoCoord(37.566535, 126.97796919999996) : act.gpsLatLng;
        setMarker(act.selectLatLng);
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
                act.mArea = s;

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
            act.startActivityForResult(intent,RESULT_GPS_OFF);
            act.overridePendingTransition(0,0);
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