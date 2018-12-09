package com.example.rd_air01.iot_dollmachinemanager;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.billy.android.preloader.PreLoader;
import com.billy.android.preloader.interfaces.DataListener;
import com.example.rd_air01.iot_dollmachinemanager.rawData.RawData;
import com.example.rd_air01.iot_dollmachinemanager.rawData.RawDataManager;
import com.example.rd_air01.iot_dollmachinemanager.user.User;
import com.example.rd_air01.iot_dollmachinemanager.user.UserManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class SlaveActivity extends AppCompatActivity {

    private FloatingActionButton reloadBtn;
    private Button timeQueryBtn;
    private Spinner filterSpinner;
    private ListView dataListView;
    private String[] filterList = {"所有紀錄", "最高營收", "最高出貨", "最低營收", "最低出貨"};
    private ArrayAdapter<String> spinnerAdapter;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<RawData> currentList = new ArrayList<>();
    private UserManager userManager = UserManager.getInstance();
    private RawDataManager rawDataManager = RawDataManager.getInstance();
    private User user = userManager.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slave);
        setTitle("台主管理");

        filterSpinner = findViewById(R.id.filterSpinner);
        timeQueryBtn = findViewById(R.id.timeQueryBtn);
        reloadBtn = findViewById(R.id.reloadBtn);
        dataListView = findViewById(R.id.listView);

        String token = FirebaseInstanceId.getInstance().getToken();
        UserManager.getInstance().updateToken(user.machine_num, token);

        //取得原始資料id的意圖，建立監聽事件，等待loader完成後將清單layout
        Intent intent = getIntent();
        int rawDataPreLoaderId = intent.getIntExtra("rawDataPreLoaderId", 0);
        PreLoader.listenData(rawDataPreLoaderId, new Listener());

        //建立下拉式選單與監聽事件
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterList);
        filterSpinner.setAdapter(spinnerAdapter);
        filterSpinner.setOnItemSelectedListener(new SpinnerItemSelectedListener());

        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reload();
            }
        });

        timeQueryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeQuery();
            }
        });
    }

    //監聽是否load完rawData，完成後將清單layout
    class Listener implements DataListener<ArrayList<RawData>> {
        @Override
        public void onDataArrived(ArrayList<RawData> rawDataList) {

            Log.d("載入", "slave listener");
            layoutListView(rawDataList);
            currentList = rawDataList;
        }
    }

    //監聽下拉式選單的item觸發
    class SpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener
    {

        public void onItemSelected(AdapterView<?>v, View iv, int pos, long id) {
            String item = String.valueOf(v.getItemAtPosition(pos));
            Toast.makeText(SlaveActivity.this, item, Toast.LENGTH_SHORT).show();

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
    private void reload() {

        int num = userManager.getCurrentUser().machine_num;
        rawDataManager.loadRawDataList(num);
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

        new DatePickerDialog(SlaveActivity.this, new DatePickerDialog.OnDateSetListener() {
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

    //創建標題功能項
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.slave, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //控制標題功能項
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_call:

                AlertDialog callAccount = new AlertDialog.Builder(SlaveActivity.this)
                        .setTitle("聯絡場主")
                        .setIcon(R.mipmap.image_login)
                        .setMessage("確定要撥打給場主嗎？")
                        .setCancelable(false)  //關閉手機功能鍵
                        .setPositiveButton("確定",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //撥打電話，用位置找到對應的機台編號，再用機台編號找到對應的user資料
                                ArrayList<User> userList = userManager.getUserList();
                                String phone = "";
                                for (int i = 0; i < userList.size(); i++) {
                                    if (userList.get(i).right == 2) {
                                        phone = userList.get(i).phone;
                                    }
                                }
                                if (phone == null || phone.isEmpty()) {
                                    Toast.makeText(SlaveActivity.this, "場主沒有留下聯絡電話", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                                startActivity(call);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;

            case R.id.action_resetPwd:

                userManager.auth.sendPasswordResetEmail(user.email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SlaveActivity.this, "已送出修改信，請至信箱查看", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SlaveActivity.this, "修改信寄送失敗："+ task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;

            case R.id.action_signOut:

                userManager.signOut();
                startActivity(new Intent(SlaveActivity.this, LoginActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
