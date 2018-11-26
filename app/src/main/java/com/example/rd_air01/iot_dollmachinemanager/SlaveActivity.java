package com.example.rd_air01.iot_dollmachinemanager;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class SlaveActivity extends AppCompatActivity {

    private FloatingActionButton reloadBtn;
    private ListView dataList;
    private ArrayAdapter<String> arrayAdapter;
    private UserManager userManager = UserManager.getInstance();
    private RawDataManager rawDataManager = RawDataManager.getInstance();
    private User user = userManager.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slave);
        setTitle("台主管理");

        reloadBtn = findViewById(R.id.reloadBtn);
        dataList = findViewById(R.id.listView);

        String token = FirebaseInstanceId.getInstance().getToken();
        UserManager.getInstance().updateToken(user.machine_num, token);

        //取得原始資料id的意圖，建立監聽事件，等待loader完成後將清單layout
        Intent intent = getIntent();
        int rawDataPreLoaderId = intent.getIntExtra("rawDataPreLoaderId", 0);
        PreLoader.listenData(rawDataPreLoaderId, new Listener());

        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int num = userManager.getCurrentUser().machine_num;
                rawDataManager.loadRawDataList(num);
                layoutListView();
            }
        });
    }

    //使用時機：已取得資料庫的rawData
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

    //監聽是否load完rawData，完成後將清單layout
    class Listener implements DataListener<ArrayList<RawData>> {
        @Override
        public void onDataArrived(ArrayList<RawData> rawDataList) {

            Log.d("載入", "slave listener");
            //adapter初始化
            arrayAdapter = new ArrayAdapter<>(SlaveActivity.this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1);

            Collections.reverse(rawDataList); //倒敘排列

            for (int i = 0; i < rawDataList.size(); i++) {
                arrayAdapter.add("時間：" + timeConverter(rawDataList.get(i).timestamp) +
                        "\n獲利：" + String.valueOf(rawDataList.get(i).coin * 10) + "元" +
                        "\n出貨次數：" + String.valueOf(rawDataList.get(i).goods) + "次");
            }
            dataList.setAdapter(arrayAdapter);
        }
    }

    //時間戳記轉換
    private String timeConverter(String timestamp) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp) * 1000);
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd  HH 時");

        return timeFormat.format(calendar.getTime());
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
