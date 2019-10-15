package com.github.react.sextant.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class CrimeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_crime);

        //为协同工作，Activity类中相应添加了FragmentManager类。
        //FragmentManager类负责管理fragment并将它们的视图
        //添加到activity的视图层级结构中

        FragmentManager fm = getSupportFragmentManager();//在extends Activity中则使用getFragmentManager()

        /***
         * Fragment事务
         *
         * 创建一个新的fragment事务，执行一个fragment添加操作，然后提交该事务
         *
         * FragmentManager.beginTransaction() 创建并返回FragmentTransaction实例
         * **/
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment == null) {
            fragment = new CrimeFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}