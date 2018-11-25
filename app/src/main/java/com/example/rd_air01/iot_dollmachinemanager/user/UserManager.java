package com.example.rd_air01.iot_dollmachinemanager.user;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserManager {

    private static User mUser;
    private static ArrayList<User> userList = new ArrayList<>();
    private static UserManager instance;
    private DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    public FirebaseAuth auth = FirebaseAuth.getInstance();

    private UserManager() {}

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }



    public String getUID() {
        return auth.getUid();
    }

    public void signOut() {
        mUser = null;
        userList.clear();
        auth.signOut();
    }

    //向資料庫取得所有user資料
    public void loadUserList() {

        Query query = mDatabaseRef.child("user");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    userList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        userList.add(snapshot.getValue(User.class)); //轉換成user物件後加入清單
                    }

                } else {
                    Log.d("使用者資料：", "不存在");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //回傳所有user清單
    public ArrayList<User> getUserList() {
        return userList;
    }

    //從所有user清單中，回傳特定user資料
    public User getUserData(String email) {

        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).email.equals(email) ) {
                return userList.get(i);
            }
        }
        return null;
    }

    //回傳目前user資料
    public User getCurrentUser() {
        if (mUser == null) {
            mUser = getUserData(auth.getCurrentUser().getEmail());
        }
        return mUser;
    }

    //上傳user資料
    public void updateUserData(User user) {
        mDatabaseRef.child("user").child(String.valueOf(user.machine_num)).setValue(user);
    }

    //上傳裝置ID
    public void updateToken(int machineNum, String token) {
        mDatabaseRef.child("user").child(String.valueOf(machineNum)).child("token").setValue(token);
    }

    //移除user資料
    public void removeUserData(User user) {
        mDatabaseRef.child("user").child(String.valueOf(user.machine_num)).removeValue();
    }

    //移除user資料
    public void removeUserData(int machineNum) {
        mDatabaseRef.child("user").child(String.valueOf(machineNum)).removeValue();
    }
}
