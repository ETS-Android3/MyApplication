package com.github.arialyy.ariatest;

import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;


public class MainActivity extends AppCompatActivity {
    private static final String DOWNLOAD_URL =
            "http://static.gaoshouyou.com/d/d4/4f/d6d48db3794fb9ecf47e83c346570881.apk";
    final View view = getLayoutInflater().inflate(R.layout.activity_main, null);
    TextView mSize = (TextView) view.findViewById(R.id.size);
    TextView mSpeed = (TextView) view.findViewById(R.id.speed);

    HorizontalProgressBarWithNumber mPb = (HorizontalProgressBarWithNumber) view.findViewById(R.id.progressBar);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
//        Aria.download(this).register();



        // 开始
        view.findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Aria.download(this)
                        .load(DOWNLOAD_URL)
                        .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/aria_test.apk")
                        .start();

            }
        });

        //暂停
        view.findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Aria.download(this).load(DOWNLOAD_URL).pause();
            }
        });

        //删除任务
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Aria.download(this).load(DOWNLOAD_URL).cancel();
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        Aria.download(this).addSchedulerListener(new MySchedulerListener());
    }

    private class MySchedulerListener extends Aria.DownloadSchedulerListener {

        @Override public void onTaskStart(DownloadTask task) {

            mSize.setText(task.getConvertFileSize());
        }

        @Override public void onTaskStop(DownloadTask task) {
            Toast.makeText(MainActivity.this, "停止下载", Toast.LENGTH_SHORT).show();
        }

        @Override public void onTaskCancel(DownloadTask task) {
            Toast.makeText(MainActivity.this, "取消下载", Toast.LENGTH_SHORT).show();
            mSize.setText("0m");
            mSpeed.setText("");
            mPb.setProgress(0);
        }

        @Override public void onTaskFail(DownloadTask task) {
            Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
        }

        @Override public void onTaskComplete(DownloadTask task) {
            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
        }

        @Override public void onTaskRunning(DownloadTask task) {
            mSpeed.setText(task.getConvertSpeed());
            mPb.setProgress(task.getPercent());
        }
    }

}