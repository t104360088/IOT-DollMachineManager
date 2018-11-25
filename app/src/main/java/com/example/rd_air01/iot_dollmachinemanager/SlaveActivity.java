package com.example.rd_air01.iot_dollmachinemanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.rd_air01.iot_dollmachinemanager.rawData.RawData;
import com.example.rd_air01.iot_dollmachinemanager.rawData.RawDataManager;
import com.example.rd_air01.iot_dollmachinemanager.user.User;
import com.example.rd_air01.iot_dollmachinemanager.user.UserManager;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

public class SlaveActivity extends AppCompatActivity {

    private Button signOutBtn;
    private ListView dataList;
    private ArrayAdapter<String> arrayAdapter;
    private UserManager userManager = UserManager.getInstance();
    private RawDataManager rawDataManager = RawDataManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slave);

        signOutBtn = findViewById(R.id.signOutBtn);
        dataList = findViewById(R.id.listView);

        final User user = userManager.getCurrentUser();
        String token = FirebaseInstanceId.getInstance().getToken();
        UserManager.getInstance().updateToken(user.machine_num, token);

        layoutListView();

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userManager.signOut();
                startActivity(new Intent(SlaveActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void layoutListView() {

        //設定清單樣式與來源
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1);

        ArrayList<RawData> rawDataList = rawDataManager.getRawDataList();
        //final HashMap machineNumMap = new HashMap();

        for (int i = 0; i < rawDataList.size(); i++) {
            arrayAdapter.add("時間：" + String.valueOf(rawDataList.get(i).timestamp) +
                    "\n獲利：" + String.valueOf(rawDataList.get(i).coin * 10) + "元" +
                    "\n出貨次數：" + String.valueOf(rawDataList.get(i).goods) + "次");
            //machineNumMap.put(i, userList.get(i).machine_num); //用字典儲存每個機台編號
        }
        dataList.setAdapter(arrayAdapter);
    }
}
