package com.example.rd_air01.iot_dollmachinemanager;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.rd_air01.iot_dollmachinemanager.rawData.RawData;
import com.example.rd_air01.iot_dollmachinemanager.rawData.RawDataManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class ManageStatusActivity extends AppCompatActivity {

    private FloatingActionButton reloadBtn;
    private ListView dataList;
    private ArrayAdapter<String> arrayAdapter;
    private RawDataManager rawDataManager = RawDataManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_status);

        reloadBtn = findViewById(R.id.reloadBtn);
        dataList = findViewById(R.id.listView);

        //獲取來自MasterActivity的user機台編號
        Intent intent = getIntent();
        final int userMachineNum = intent.getIntExtra("userMachineNum", 0);

        //設定標題欄title
        setTitle(String.valueOf(userMachineNum) + "號機台經營狀況");

        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rawDataManager.loadRawDataList(userMachineNum);
                layoutListView();
            }
        });
    }

    private void layoutListView() {

        //設定清單樣式與來源
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1);

        ArrayList<RawData> rawDataList = rawDataManager.getRawDataList();
        Collections.reverse(rawDataList); //倒敘排列

        for (int i = 0; i < rawDataList.size(); i++) {
            arrayAdapter.add("時間：" + timeConverter(rawDataList.get(i).timestamp) +
                    "\n獲利：" + String.valueOf(rawDataList.get(i).coin * 10) + "元" +
                    "\n出貨次數：" + String.valueOf(rawDataList.get(i).goods) + "次");
        }
        dataList.setAdapter(arrayAdapter);
    }

    private String timeConverter(String timestamp) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp) * 1000);
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd  HH 時");

        return timeFormat.format(calendar.getTime());
    }
}
