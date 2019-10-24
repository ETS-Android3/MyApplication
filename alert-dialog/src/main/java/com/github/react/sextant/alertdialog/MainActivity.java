package com.github.react.sextant.alertdialog;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

    }

    public void openAlertDialog(View v){

        //这样做 在旋转屏幕时会被清除
        new AlertDialog.Builder(this)
                .setTitle("AlertDialog")
                .setPositiveButton("确定",null)
                .create().show();
    }

    public void openDatePickerDialog(View v){
        FragmentManager manager = getSupportFragmentManager();
        DatePickerFragment dateDialog = DatePickerFragment.newInstance(new Date(118,8,8));

        //设置目标fragment
//        dateDialog.setTargetFragment(dateDialog, REQUEST_DATE);


        dateDialog.show(manager,DIALOG_DATE);
    }
}