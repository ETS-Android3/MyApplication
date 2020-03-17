package com.github.arialyy.ariatest;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.RecordUtil;

import static com.arialyy.aria.util.CheckUtil.checkDPathConflicts;


public class MainActivity extends AppCompatActivity {
    private static final String DOWNLOAD_URL =
            "http://static.gaoshouyou.com/d/d4/4f/d6d48db3794fb9ecf47e83c346570881.apk";
    long taskId;
    View main;
    HorizontalProgressBarWithNumber mPb;
    TextView mSize;
    TextView mSpeed;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);

        //注册aria
        Aria.download(this).register();
        Aria.get(this).getDownloadConfig().setConvertSpeed(true);

        main = getLayoutInflater().inflate(R.layout.activity_main2, null);
        mPb = (HorizontalProgressBarWithNumber) main.findViewById(R.id.progressBar);
        mSize = (TextView) main.findViewById(R.id.size);
        mSpeed = (TextView) main.findViewById(R.id.speed);

    }

    public void onClick(View v){
        if(v.getId()==R.id.start){
            //创建任务
            String filePath = Environment.getExternalStorageDirectory().getPath() + "/aria_test.apk";
//            System.out.println("luokun"+checkDPathConflicts(false,filePath));
//            int state = Aria.download(this)
//                    .load(taskId).getTaskState();
//            System.out.println("luokun state"+state);
//            if(state == 3){
//                Aria.download(this)
//                        .load(taskId)
//                        .reStart();
//            }

            if(checkDPathConflicts(false,filePath)){
                taskId = Aria.download(this)
                        .load(DOWNLOAD_URL)
                        .setFilePath(filePath) //设置文件保存的完整路径
                        .create();
            }else {
                Aria.download(this)
                        .load(RecordUtil.getTaskRecord())
                        .resume();
            }

        }else if(v.getId()==R.id.stop){

            Aria.download(this)
                    .load(taskId)
                    .stop();

        }else if(v.getId()==R.id.cancel){
            Aria.download(this)
                    .load(taskId)
                    .cancel();
        }
    }

    //在这里处理任务执行中的状态，如进度进度条的刷新
    @Download.onTaskRunning protected void running(DownloadTask task) {
        if(task.getKey().equals(DOWNLOAD_URL)){
            //可以通过url判断是否是指定任务的回调
        }
        final int p = task.getPercent();  //任务进度百分比
        final String speed = task.getConvertSpeed();  //转换单位后的下载速度，单位转换需要在配置文件中打开
        final long speed1 = task.getSpeed(); //原始byte长度速度

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                mPb.setProgress(p);
                mSize.setText(speed);
                mSpeed.setText(speed1/1024+"mb");

            }
        });



    }

    @Download.onTaskComplete void taskComplete(DownloadTask task) {
        //在这里处理任务完成的状态
    }

    @Download.onTaskFail void taskFail(DownloadGroupTask task) {
        if(task.getKey().equals(DOWNLOAD_URL)){
            //任务失败时的注解，任务执行失败时进行回调
            System.out.println("luokun1："+task.getExtendField());
        }
    }
}