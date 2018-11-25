package com.example.rd_air01.iot_dollmachinemanager.rawData;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RawDataManager {

    //private static User mUser;
    private static ArrayList<RawData> rawDataList = new ArrayList<>();
    private static RawDataManager instance;
    private DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();

    private RawDataManager() {}

    public static RawDataManager getInstance() {
        if (instance == null) {
            instance = new RawDataManager();
        }
        return instance;
    }

    //向資料庫取得特定machineNum的所有資料
    public void loadRawDataList(int machineNum) {

        Query query = mDatabaseRef.child("rawData").child(String.valueOf(machineNum));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    rawDataList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        rawDataList.add(snapshot.getValue(RawData.class)); //轉換成RawData物件後加入清單
                    }

                } else {
                    Log.d("原始資料：", "不存在");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //回傳所有user清單
    public ArrayList<RawData> getRawDataList() {
        return rawDataList;
    }

    /*
    //回傳特定user資料
    public User getUserData(String email) {

        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).email.equals(email) ) {
                return userList.get(i);
            }
        }
        return null;
    }

    //從所有user清單中，回傳目前user資料
    public User getCurrentUser() {
        if (mUser == null) {
            mUser = getUserData(auth.getCurrentUser().getEmail());
        }
        return mUser;
    }
    */

}
