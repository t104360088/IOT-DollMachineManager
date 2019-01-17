package com.example.rd_air01.iot_dollmachinemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.rd_air01.iot_dollmachinemanager.rawData.RawDataManager;
import com.example.rd_air01.iot_dollmachinemanager.user.User;
import com.example.rd_air01.iot_dollmachinemanager.user.UserManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;

public class MasterActivity extends AppCompatActivity {

    private ListView dataList;
    private FloatingActionButton reloadBtn;
    private UserManager userManager = UserManager.getInstance();
    private ArrayAdapter<String> arrayAdapter;
    private final String[] serviceItem = {"經營狀況", "聯絡台主", "移除台主"};
    private final User user = userManager.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        setTitle("場主管理");

        reloadBtn = findViewById(R.id.reloadBtn);
        dataList = findViewById(R.id.listView);

        String token = FirebaseInstanceId.getInstance().getToken();
        UserManager.getInstance().updateToken(user.machine_num, token);

        layoutListView();

        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userManager.loadUserList();
                layoutListView();
            }
        });
    }

    private void layoutListView() {

        //設定清單樣式與來源
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1);

        ArrayList<User> userList = userManager.getUserList();
        final HashMap machineNumMap = new HashMap();

        for (int i = 0; i < userList.size(); i++) {
            arrayAdapter.add("機台編號：" + String.valueOf(userList.get(i).machine_num) +
                    "\n管理者：" + String.valueOf(userList.get(i).email));
            machineNumMap.put(i, userList.get(i).machine_num); //用字典儲存每個機台編號
        }
        dataList.setAdapter(arrayAdapter);

        //設定清單項目點擊事件
        dataList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l)
            {
                AlertDialog.Builder dialog_list = new AlertDialog.Builder(MasterActivity.this);
                dialog_list.setTitle("服務項目");
                dialog_list.setItems(serviceItem, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                        //取得點擊item對應的機台編號
                        final int userMachineNum = Integer.parseInt(machineNumMap.get(position).toString());

                        switch (which)
                        {
                            case 0:

                                RawDataManager rawDataManager = RawDataManager.getInstance();
                                rawDataManager.loadRawDataList(userMachineNum);
                                Intent intent = new Intent(MasterActivity.this, ManageStatusActivity.class);
                                intent.putExtra("userMachineNum", userMachineNum);
                                startActivity(intent);
                                break;

                            case 1:

                                AlertDialog callAccount = new AlertDialog.Builder(MasterActivity.this)
                                        .setTitle("聯絡台主")
                                        .setIcon(R.mipmap.image_login)
                                        .setMessage("確定要撥打給此台主嗎？")
                                        .setCancelable(false)  //關閉手機功能鍵
                                        .setPositiveButton("確定",new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                //撥打電話，用機台編號找到對應的user資料的電話號碼
                                                String phone = userManager.getUserData(userMachineNum).phone;
                                                if (phone == null) {
                                                    Toast.makeText(MasterActivity.this, "無聯絡電話", Toast.LENGTH_SHORT).show();
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

                            case 2:

                                AlertDialog removeAccount = new AlertDialog.Builder(MasterActivity.this)
                                        .setTitle("移除台主")
                                        .setIcon(R.mipmap.image_login)
                                        .setMessage("確定要移除此台主帳號嗎？")
                                        .setCancelable(false)  //關閉手機功能鍵
                                        .setPositiveButton("確定",new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                userManager.removeUserData(Integer.parseInt(String.valueOf(machineNumMap.get(position))));
                                                userManager.loadUserList();
                                                Toast.makeText(MasterActivity.this, "已移除此台主", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .show();
                                break;
                        }
                    }
                });
                dialog_list.show();
            }
        });
    }

    //創建標題功能項
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.master, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //控制標題功能項
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_signUp:

                startActivity(new Intent(MasterActivity.this, SignUpActivity.class));
                break;

            case R.id.action_resetPwd:

                userManager.auth.sendPasswordResetEmail(user.email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MasterActivity.this, "已送出修改信，請至信箱查看", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MasterActivity.this, "修改信寄送失敗："+ task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;

            case R.id.action_signOut:

                userManager.signOut();
                startActivity(new Intent(MasterActivity.this, LoginActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
