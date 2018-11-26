package com.example.rd_air01.iot_dollmachinemanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.billy.android.preloader.PreLoader;
import com.billy.android.preloader.interfaces.DataLoader;
import com.example.rd_air01.iot_dollmachinemanager.rawData.RawData;
import com.example.rd_air01.iot_dollmachinemanager.rawData.RawDataManager;
import com.example.rd_air01.iot_dollmachinemanager.user.User;
import com.example.rd_air01.iot_dollmachinemanager.user.UserManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn;
    private CheckBox keepCheckBox;
    private EditText emailField, pwdField;
    private ProgressBar progressBar;
    private final UserManager userManager = UserManager.getInstance();
    private TextInputLayout emailLayout, pwdLayout;
    private int userMachineNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginBtn = findViewById(R.id.loginBtn);
        emailField = findViewById(R.id.emailField);
        pwdField = findViewById(R.id.pwdField);
        emailLayout = findViewById(R.id.emailLayout);
        pwdLayout = findViewById(R.id.pwdLayout);
        keepCheckBox = findViewById(R.id.keepCheckBox);
        progressBar = findViewById(R.id.progressBar);

        //載入儲存於手機內部的帳密
        keepCheckBox.setChecked(true);
        emailField.setText(getSharedPreferences("userDefault", MODE_PRIVATE).getString("Email", ""));
        pwdField.setText(getSharedPreferences("userDefault", MODE_PRIVATE).getString("Pwd", ""));

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userManager.loadUserList();
                String emailText = emailField.getText().toString();
                String pwdText = pwdField.getText().toString();
                if (TextUtils.isEmpty(emailText) || TextUtils.isEmpty(pwdText)) {
                    Toast.makeText(LoginActivity.this, "資料輸入不完整", Toast.LENGTH_SHORT).show();
                    return;
                }

                login(emailText, pwdText);
            }
        });
    }

    private void login(final String email, final String passwd) {

        progressBar.setVisibility(View.VISIBLE);
        userManager.auth.signInWithEmailAndPassword(email, passwd)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        else {
                            Log.d("使用者登入：", "手動登入");
                            saveAccount(email, passwd);

                            //判斷使用者權限
                            final User user = userManager.getCurrentUser();
                            if (user.right == 2) {
                                Intent intent = new Intent(LoginActivity.this, MasterActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                //建立加載器
                                userMachineNum = user.machine_num; //儲存於全域變數，供加載器使用
                                int preLoaderId = PreLoader.preLoad(new Loader());
                                Intent intent = new Intent(LoginActivity.this, SlaveActivity.class);
                                intent.putExtra("rawDataPreLoaderId", preLoaderId);
                                startActivity(intent);
                                finish();
                            }
                        }
                        emailField.setText("");
                        pwdField.setText("");
                    }
                });
    }

    //儲存帳號資料至本地
    private void saveAccount(String email, String passwd) {

        //儲存帳密於手機內部
        SharedPreferences pref = getSharedPreferences("userDefault", MODE_PRIVATE);
        if (keepCheckBox.isChecked()) {
            pref.edit().putString("Email", email).putString("Pwd", passwd).apply();
        } else {
            pref.edit().remove("Email").remove("Pwd").apply();
        }
    }

    //加載器，先載入原始資料，完成後會調用目的地的監聽器
    class Loader implements DataLoader<ArrayList<RawData>> {
        @Override
        public ArrayList<RawData> loadData() {

            RawDataManager rawDataManager = RawDataManager.getInstance();
            rawDataManager.loadRawDataList(userMachineNum);
            try {
                Thread.sleep(2000);

            } catch (InterruptedException ignored) {
            }
            ArrayList<RawData> rawDataList = rawDataManager.getRawDataList();
            Log.d("載入", "login loader");
            Log.d("載入", "原始資料數量：" + String.valueOf(rawDataList.size()));
            return rawDataList;
        }
    }
}
