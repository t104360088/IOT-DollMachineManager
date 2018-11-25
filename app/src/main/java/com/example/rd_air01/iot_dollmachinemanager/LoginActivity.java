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

import com.example.rd_air01.iot_dollmachinemanager.rawData.RawDataManager;
import com.example.rd_air01.iot_dollmachinemanager.user.User;
import com.example.rd_air01.iot_dollmachinemanager.user.UserManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn;
    private CheckBox keepCheckBox;
    private EditText emailField, pwdField;
    private ProgressBar progressBar;
    private final UserManager userManager = UserManager.getInstance();
    private TextInputLayout emailLayout, pwdLayout;

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
                            /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();*/

                            //測試
                            final User user = userManager.getCurrentUser();
                            if (user.right == 2) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                RawDataManager rawDataManager = RawDataManager.getInstance();
                                rawDataManager.loadRawDataList(user.machine_num);
                                Intent intent = new Intent(LoginActivity.this, SlaveActivity.class);
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
}
