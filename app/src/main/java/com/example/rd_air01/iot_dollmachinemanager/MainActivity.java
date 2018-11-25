package com.example.rd_air01.iot_dollmachinemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ListView dataList;
    private Button signUpBtn, resetPwdBtn, signOutBtn;
    private UserManager userManager = UserManager.getInstance();
    private ArrayAdapter<String> arrayAdapter;
    private final String[] serviceItem = {"經營狀況", "聯絡台主", "移除台主"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUpBtn = findViewById(R.id.signUpBtn);
        resetPwdBtn = findViewById(R.id.resetPwdBtn);
        signOutBtn = findViewById(R.id.signOutBtn);
        dataList = findViewById(R.id.listView);

        final User user = userManager.getCurrentUser();
        String token = FirebaseInstanceId.getInstance().getToken();
        UserManager.getInstance().updateToken(user.machine_num, token);

        layoutListView();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (user.right == 1) {
                    Toast.makeText(MainActivity.this, "此功能僅場主可使用", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });

        resetPwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userManager.auth.sendPasswordResetEmail(user.email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "已送出修改信，請至信箱查看", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "修改信寄送失敗："+ task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userManager.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
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
                AlertDialog.Builder dialog_list = new AlertDialog.Builder(MainActivity.this);
                dialog_list.setTitle("服務項目");
                dialog_list.setItems(serviceItem, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                        //取得點擊item對應的機台編號
                        Log.d("位置：", String.valueOf(machineNumMap.get(position)));

                        switch (which)
                        {
                            case 0:

                                break;

                            case 1:

                                break;

                            case 2:

                                AlertDialog removeAccount = new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("移除台主")
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setMessage("確定要移除此台主帳號嗎？")
                                        .setCancelable(false)  //關閉手機功能鍵
                                        .setPositiveButton("確定",new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                userManager.removeUserData(Integer.parseInt(String.valueOf(machineNumMap.get(position))));
                                                userManager.loadUserList();
                                                Toast.makeText(MainActivity.this, "已移除此台主", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .show();
                                //delete_the_advertisement.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                                //delete_the_advertisement.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                                break;
                        }
                    }
                });
                dialog_list.show();
            }
        });
    }

    /*
    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot){

        arrayAdapter.remove("機台編號：" + String.valueOf(dataSnapshot.child("machine_num").getValue()) +
                "\n管理者：" + String.valueOf(dataSnapshot.child("email").getValue()));
        arrayAdapter.notifyDataSetChanged();
        dataList.setAdapter(arrayAdapter);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot,String s){}

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot,String s){}

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot,String s){}

    @Override
    public void onCancelled(DatabaseError databaseError){} */
}
