package com.example.rd_air01.iot_dollmachinemanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.rd_air01.iot_dollmachinemanager.rawData.RawData;
import com.example.rd_air01.iot_dollmachinemanager.rawData.RawDataManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class ManageStatusActivity extends AppCompatActivity {

    private FloatingActionButton reloadBtn;
    private Button timeQueryBtn;
    private Spinner filterSpinner;
    private ListView dataListView;
    private String[] filterList = {"所有紀錄", "最高營收", "最高出貨", "最低營收", "最低出貨"};
    private ArrayAdapter<String> spinnerAdapter;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<RawData> currentList = new ArrayList<>();
    private RawDataManager rawDataManager = RawDataManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_status);

        filterSpinner = findViewById(R.id.filterSpinner);
        timeQueryBtn = findViewById(R.id.timeQueryBtn);
        reloadBtn = findViewById(R.id.reloadBtn);
        dataListView = findViewById(R.id.listView);

        //獲取來自MasterActivity的user機台編號
        Intent intent = getIntent();
        final int userMachineNum = intent.getIntExtra("userMachineNum", 0);

        //設定標題欄title
        setTitle(String.valueOf(userMachineNum) + "號機台經營狀況");

        //建立下拉式選單與監聽事件
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterList);
        filterSpinner.setAdapter(spinnerAdapter);
        filterSpinner.setOnItemSelectedListener(new SpinnerItemSelectedListener());

        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reload(userMachineNum);
            }
        });

        timeQueryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeQuery();
            }
        });
    }

    //監聽下拉式選單的item觸發
    class SpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener
    {

        public void onItemSelected(AdapterView<?>v, View iv, int pos, long id) {
            String item = String.valueOf(v.getItemAtPosition(pos));
            Toast.makeText(ManageStatusActivity.this, item, Toast.LENGTH_SHORT).show();

            //搜尋邏輯排序 測試中
            if (currentList.size() == 0) { return; }
            int baseCoin = currentList.get(0).coin;
            int baseGoods = currentList.get(0).goods;
            ArrayList<RawData> newRawDataList = new ArrayList<>();

            switch (pos) {

                case 0:
                    newRawDataList = currentList;
                    break;

                case 1:
                    //排序若值大於基準，就清掉目前清單再加入，若相等則直接加入
                    for (int i = 0; i < currentList.size(); i++) {
                        int currentCoin = currentList.get(i).coin;
                        if (currentCoin > baseCoin) {
                            baseCoin = currentCoin; //更新最大值
                            newRawDataList.clear();
                            newRawDataList.add(currentList.get(i));
                        } else if (currentCoin == baseCoin) {
                            newRawDataList.add(currentList.get(i));
                        }
                    }
                    break;

                case 2:
                    for (int i = 0; i < currentList.size(); i++) {
                        int currentGoods = currentList.get(i).goods;
                        if (currentGoods > baseGoods) {
                            baseGoods = currentGoods; //更新最大值
                            newRawDataList.clear();
                            newRawDataList.add(currentList.get(i));
                        } else if (currentGoods == baseGoods) {
                            newRawDataList.add(currentList.get(i));
                        }
                    }
                    break;

                case 3:
                    for (int i = 0; i < currentList.size(); i++) {
                        int currentCoin = currentList.get(i).coin;
                        if (currentCoin < baseCoin) {
                            baseCoin = currentCoin; //更新最小值
                            newRawDataList.clear();
                            newRawDataList.add(currentList.get(i));
                        } else if (currentCoin == baseCoin) {
                            newRawDataList.add(currentList.get(i));
                        }
                    }
                    break;

                case 4:
                    for (int i = 0; i < currentList.size(); i++) {
                        int currentGoods = currentList.get(i).goods;
                        if (currentGoods < baseGoods) {
                            baseGoods = currentGoods; //更新最小值
                            newRawDataList.clear();
                            newRawDataList.add(currentList.get(i));
                        } else if (currentGoods == baseGoods) {
                            newRawDataList.add(currentList.get(i));
                        }
                    }
                    break;
            }
            layoutListView(newRawDataList);
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    //重新載入資料庫的原始資料
    private void reload(int userMachineNum) {

        rawDataManager.loadRawDataList(userMachineNum);
        ArrayList<RawData> rawDataList = rawDataManager.getRawDataList();

        timeQueryBtn.setText("所有時間");
        layoutListView(rawDataList);
        currentList = rawDataList;
        filterSpinner.setSelection(0); //預設篩選清單的第0項被點擊
    }

    //搜尋特定時間資料
    private void timeQuery() {

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(ManageStatusActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                //選取時間後將資料轉換成時間戳記
                String time = String.valueOf(year)
                        + "/" + String.valueOf(month + 1)
                        + "/" + String.valueOf(day);
                long cuzTimestamp = timeConvertTimestamp(time);

                ArrayList<RawData> rawDataList = rawDataManager.getRawDataList();
                ArrayList<RawData> newRawDataList = new ArrayList<>();

                //篩選出選定日期的所有資料
                for (int i = 0; i < rawDataList.size(); i++) {
                    long timestamp = Long.parseLong(rawDataList.get(i).timestamp);

                    if ((cuzTimestamp - 1) < timestamp) {
                        if (timestamp < (cuzTimestamp + 86400)) {
                            newRawDataList.add(rawDataList.get(i));
                        }
                    }
                }
                timeQueryBtn.setText(time);
                layoutListView(newRawDataList);
                currentList = newRawDataList;
                filterSpinner.setSelection(0); //預設篩選清單的第0項被點擊
            }
        }, year, month, day).show();
    }

    //代入rawData將清單layout
    private void layoutListView(ArrayList<RawData> rawDataList) {

        //設定清單樣式與來源
        listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1);

        Collections.reverse(rawDataList); //倒敘排列

        for (int i = 0; i < rawDataList.size(); i++) {
            listAdapter.add("時間：" + timestampConvertTime(rawDataList.get(i).timestamp) +
                    "\n獲利：" + String.valueOf(rawDataList.get(i).coin * 10) + "元" +
                    "\n出貨次數：" + String.valueOf(rawDataList.get(i).goods) + "次");
        }
        dataListView.setAdapter(listAdapter);
    }

    //時間戳記轉成時間
    private String timestampConvertTime(String timestamp) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp) * 1000);
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd  HH 時");

        return timeFormat.format(calendar.getTime());
    }

    //時間轉成時間戳記
    private long timeConvertTimestamp(String time) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = null;
        try {
            date = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime()/1000;
    }
}
