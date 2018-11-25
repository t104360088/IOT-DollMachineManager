package com.example.rd_air01.iot_dollmachinemanager;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.ArrayList;

public class SignUpActivity extends AppCompatActivity {

    private Button signUpBtn;
    private ListView dataList;
    private ProgressBar progressBar;
    private EditText emailField, machineNumField;
    private UserManager userManager = UserManager.getInstance();
    private ArrayAdapter<String> arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpBtn = findViewById(R.id.signUpBtn);
        dataList = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        emailField = findViewById(R.id.emailField);
        machineNumField = findViewById(R.id.machineNumField);



        arrayAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1);

        ArrayList<User> userList = userManager.getUserList();

        Log.d("數量：", String.valueOf(userList.size()));

        for (int i = 0; i < userList.size(); i++) {
            arrayAdapter.add("機台編號：" + String.valueOf(userList.get(i).machine_num) +
                    "\n管理者：" + String.valueOf(userList.get(i).email));
        }
        dataList.setAdapter(arrayAdapter);


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = emailField.getText().toString().trim();
                String machine_num = machineNumField.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(machine_num)) {
                    Toast.makeText(getApplicationContext(), "請輸入完整資料", Toast.LENGTH_SHORT).show();
                    return;
                }

                signUp(email, machine_num);
            }
        });

    }

    private void signUp(final String email, final String machine_num) {

        //驗證機台編號是否被使用
        ArrayList<User> userList = userManager.getUserList();
        Log.d("清單數量：", String.valueOf(userManager.getUserList().size()));
        for (int i = 0; i < userList.size(); i++) {

            Log.d("所有機台編號：", String.valueOf(userList.get(i).machine_num));
            if (String.valueOf(userList.get(i).machine_num).equals(machine_num)) {
                Toast.makeText(this, "此機台編號已被使用", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        String pwd = "123456";

        userManager.auth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "註冊失敗：" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(SignUpActivity.this, "註冊完成", Toast.LENGTH_SHORT).show();

                        User user = new User();
                        user.machine_num = Integer.parseInt(machine_num);
                        user.email = email;
                        user.right = 1;

                        userManager.updateUserData(user);
                        sendResetPwdMail(email);

                        emailField.setText("");
                        machineNumField.setText("");
                    }
                });
    }

    private void sendResetPwdMail(String email) {

        userManager.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "已送出密碼，請台主至信箱查看", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "寄送密碼失敗：" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
