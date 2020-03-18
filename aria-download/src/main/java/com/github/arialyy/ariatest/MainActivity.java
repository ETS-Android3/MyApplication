package com.github.arialyy.ariatest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;


public class MainActivity extends AppCompatActivity {
    private static final String DOWNLOAD_URL =
            "http://static.gaoshouyou.com/d/d4/4f/d6d48db3794fb9ecf47e83c346570881.apk";
    String filePath = Environment.getExternalStorageDirectory().getPath() + "/aria_test.apk";
    BaseDownloadTask taskId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);
        final View main = LayoutInflater.from(this).inflate(R.layout.activity_main2, null);
        final HorizontalProgressBarWithNumber mPb = (HorizontalProgressBarWithNumber) main.findViewById(R.id.progressBar);
        final TextView mSize = (TextView) main.findViewById(R.id.size);
        final TextView mSpeed = (TextView) main.findViewById(R.id.speed);

        //创建任务
        FileDownloader.setup(this);

        taskId = FileDownloader.getImpl().create(DOWNLOAD_URL)
                .setPath(filePath)
                .addHeader("Accept-Encoding","identity")
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.120 Safari/537.36")
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        System.out.println("luokun:pending "+taskId+", "+task);
                    }


                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        System.out.println("luokun:connected "+taskId+", "+task);
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, final long soFarBytes, final long totalBytes) {

//                                        mSize.setText((int)(soFarBytes/totalBytes));
//                                        mSpeed.setText(speed1/1024+"mb");

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                System.out.println("luokun:progress, soFarBytes: "+soFarBytes+", totalBytes: "+totalBytes);
                                mPb.setProgress((int)(soFarBytes/totalBytes));
                                mSpeed.setText("luokun");
                                main.invalidate();
                                main.requestLayout();
                            }
                        });

                    }


                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                        System.out.println("luokun:blockComplete "+taskId+", "+task);
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        System.out.println("luokun:paused, soFarBytes: "+soFarBytes+", totalBytes: "+totalBytes);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        System.out.println("luokun:completed "+taskId+", "+task);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        System.out.println("luokun:error "+taskId+", "+task);
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        System.out.println("luokun:warn "+taskId+", "+task);
                    }
                });


    }

    /**
     * isUsing 表示以前有下载过，存在缓存的断点文件
     * isRunning 为true时不允许#start() #reuse()
     * **/
    public void onClick(View v){
        if(v.getId()==R.id.start){
            System.out.println("luokunm:"+Environment.getExternalStorageDirectory().getPath() + "/aria_test.apk");
            if(!taskId.isRunning()){
                if(taskId.isUsing()){
                    taskId.reuse();
                }
                taskId.start();
            }

//            if(!taskId.isRunning()&&taskId.pause()){ //继续
//                taskId.reuse();
//                taskId.start();
//            }

        }else if(v.getId()==R.id.stop){//暂停
            taskId.pause(); // return true表示暂停成功 false表示暂停失败（可能已经暂停了，然后又按了一次）
        }else if(v.getId()==R.id.cancel){//取消并清除temp文件
            FileDownloader.getImpl().clear(taskId.getId(),filePath);
        }
    }
}