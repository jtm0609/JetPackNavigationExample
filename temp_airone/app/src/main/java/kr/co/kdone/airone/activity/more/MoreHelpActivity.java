package kr.co.kdone.airone.activity.more;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;

import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;

/**
 * ikHwang 2019-06-04 오전 9:52 더보기 공기질 정보 화면
 */
public class MoreHelpActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = getClass().getSimpleName();

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    HashMap<String, String> listDatatitle;
    int lastClickedPosition = 0;

    private boolean backType = false;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_help);

        isUserInfoSection(this);

        if(getIntent().hasExtra("backType")){
            backType = getIntent().getBooleanExtra("backType", false);
        }

        // get the listview
        expListView = findViewById(R.id.listView);
        expListView.setGroupIndicator(null);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setImageResource(backType ? R.drawable.icon_header_back : R.drawable.icon_header_close);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, listDatatitle);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                Boolean isExpand = (!parent.isGroupExpanded(groupPosition));
                // 이 전에 열려있던 group 닫기
                parent.collapseGroup(lastClickedPosition);
                if(isExpand){
                    parent.expandGroup(groupPosition);
                }
                lastClickedPosition = groupPosition;
                expListView.setSelection(groupPosition);
                return true;
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();

        if(backType){
            overridePendingTransition(0, R.anim.slide_right_out);
        }else{
            overridePendingTransition(0, R.anim.slide_up_out);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                finish();
                break;
            default:
                break;
        }
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        listDatatitle = new HashMap<>();

        int pcd = CleanVentilationApplication.getInstance().getRoomControllerDevicePCD();
        // ikHwang 2019-07-25 오후 1:41 실내공기질, PM1.0 추가
        if(CleanVentilationApplication.getInstance().hasRoomController() && 5 <= pcd) {
            listDataHeader.add(getString(R.string.activity_more_help_list6_title));
            listDataHeader.add(getString(R.string.activity_more_help_list5_title));
        }

        listDataHeader.add(getString(R.string.activity_more_help_list2_title));
        listDataHeader.add(getString(R.string.activity_more_help_list1_title));
        listDataHeader.add(getString(R.string.activity_more_help_list3_title));
        listDataHeader.add(getString(R.string.activity_more_help_list4_title));

        // ikHwang 2019-07-25 오후 1:44 실내공기질, PM1.0 추가
        List<String> total = new ArrayList<>();
        total.add(getRawData(R.raw.more_help_description6));

        List<String> inside = new ArrayList<>();
        inside.add(getRawData(R.raw.more_help_description1));

        List<String> pm1 = new ArrayList<>();
        pm1.add(getRawData(R.raw.more_help_description5));

        List<String> outside = new ArrayList<>();
        outside.add(getRawData(R.raw.more_help_description2));

        List<String> microdust = new ArrayList<>();
        microdust.add(getRawData(R.raw.more_help_description3));

        List<String> dust = new ArrayList<>();
        dust.add(getRawData(R.raw.more_help_description4));

        if(5 <= pcd) {
            listDataChild.put(listDataHeader.get(0), total); // Header, Child data
            listDataChild.put(listDataHeader.get(1), pm1); // Header, Child data
            listDataChild.put(listDataHeader.get(2), outside);
            listDataChild.put(listDataHeader.get(3), inside); // Header, Child data
            listDataChild.put(listDataHeader.get(4), microdust);
            listDataChild.put(listDataHeader.get(5), dust);

            listDatatitle.put(listDataHeader.get(0), ""); // Header, Child data
            listDatatitle.put(listDataHeader.get(1), "(㎍/㎥)"); // Header, Child data
            listDatatitle.put(listDataHeader.get(2), "(㎍/㎥)"); // Header, Child data
            listDatatitle.put(listDataHeader.get(3), "(㎍/㎥)");
            listDatatitle.put(listDataHeader.get(4), "(ppm)");
            listDatatitle.put(listDataHeader.get(5), "(ppb)");
        }else{
            listDataChild.put(listDataHeader.get(0), outside);
            listDataChild.put(listDataHeader.get(1), inside); // Header, Child data
            listDataChild.put(listDataHeader.get(2), microdust);
            listDataChild.put(listDataHeader.get(3), dust);

            listDatatitle.put(listDataHeader.get(0), "(㎍/㎥)"); // Header, Child data
            listDatatitle.put(listDataHeader.get(1), "(㎍/㎥)");
            listDatatitle.put(listDataHeader.get(2), "(ppm)");
            listDatatitle.put(listDataHeader.get(3), "(ppb)");
        }
    }

    private String getRawData(int rawID) {
        String result = null;
        try {
            InputStream is = getResources().openRawResource(rawID);
            byte[] b = new byte[is.available()];
            is.read(b);
            result = new String(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<String> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<String>> _listDataChild;
        private HashMap<String, String> _listDataTitle;

        public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                     HashMap<String, List<String>> listChildData,
                                     HashMap<String, String> listChildTitle) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
            this._listDataTitle = listChildTitle;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final String childText = (String) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.expandlistview_list_item, null);
            }

            TextView txtListChild = convertView.findViewById(R.id.lblListItem);
            txtListChild.setText(Html.fromHtml(childText));

            ImageView img_title = convertView.findViewById(R.id.img_title);
            if(getString(R.string.activity_more_help_list1_title).equals(getGroup(groupPosition))){
                img_title.setImageResource(R.drawable.airinfo_icon_pm10);
            }else if(getString(R.string.activity_more_help_list2_title).equals(getGroup(groupPosition))){
                img_title.setImageResource(R.drawable.airinfo_icon_pm25);
            }else if(getString(R.string.activity_more_help_list3_title).equals(getGroup(groupPosition))){
                img_title.setImageResource(R.drawable.airinfo_icon_co2);
            }else if(getString(R.string.activity_more_help_list4_title).equals(getGroup(groupPosition))){
                img_title.setImageResource(R.drawable.airinfo_icon_tvoc);
            }else if(getString(R.string.activity_more_help_list5_title).equals(getGroup(groupPosition))){
                img_title.setImageResource(R.drawable.airinfo_icon_pm1);
            }else if(getString(R.string.activity_more_help_list6_title).equals(getGroup(groupPosition))){
                img_title.setImageResource(R.drawable.airinfo_icon_total);
            }

            TextView text_title = convertView.findViewById(R.id.text_title);
            if(getString(R.string.activity_more_help_list6_title).equals(getGroup(groupPosition))){
                String strTitle = _listDataHeader.get(groupPosition);
                text_title.setText(strTitle);
            }else{
                String strTitle = _listDataHeader.get(groupPosition).replaceFirst(" ", "");
                strTitle = strTitle.replace("(", "/");
                strTitle = strTitle.replace(")", "");
                text_title.setText(strTitle);
            }

            TextView text_unit = convertView.findViewById(R.id.text_unit);
            if(getString(R.string.activity_more_help_list6_title).equals(getGroup(groupPosition))){
                text_unit.setVisibility(View.GONE);
            }else{
                text_unit.setVisibility(View.VISIBLE);
                text_unit.setText(_listDataTitle.get(getGroup(groupPosition)));
            }

            ImageView img_bottom = convertView.findViewById(R.id.img_bottom);
            if(getString(R.string.activity_more_help_list1_title).equals(getGroup(groupPosition))){
                img_bottom.setImageResource(R.drawable.airinfo_bg_pm10);
            }else if(getString(R.string.activity_more_help_list2_title).equals(getGroup(groupPosition))){
                img_bottom.setImageResource(R.drawable.airinfo_bg_pm25);
            }else if(getString(R.string.activity_more_help_list3_title).equals(getGroup(groupPosition))){
                img_bottom.setImageResource(R.drawable.airinfo_bg_co2);
            }else if(getString(R.string.activity_more_help_list4_title).equals(getGroup(groupPosition))){
                img_bottom.setImageResource(R.drawable.airinfo_bg_tvoc1);
            }else if(getString(R.string.activity_more_help_list5_title).equals(getGroup(groupPosition))){
                img_bottom.setImageResource(R.drawable.airinfo_bg_pm1_0);
            }else if(getString(R.string.activity_more_help_list6_title).equals(getGroup(groupPosition))){
                img_bottom.setImageResource(R.drawable.airinfo_bg_total);
            }

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.expandlistview_list_group, null);
            }

            TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
            lblListHeader.setText(headerTitle);
            ImageView imgArrow = convertView.findViewById(R.id.imgArrow);
            if(isExpanded){
                lblListHeader.setTextColor(Color.rgb(0x36,0x90,0xB8));
                imgArrow.setImageResource(R.drawable.icon_list_accodion_arrow_active);
            } else {
                lblListHeader.setTextColor(Color.rgb(0x00,0x00,0x00));
                imgArrow.setImageResource(R.drawable.icon_list_accodion_arrow_default);
            }

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
