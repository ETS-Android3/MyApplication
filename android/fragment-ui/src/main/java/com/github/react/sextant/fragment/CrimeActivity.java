package com.github.react.sextant.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.github.react.sextant.fragment.util.Crime;

public class CrimeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_crime);
        setContentView(R.layout.main_layout);

        //为协同工作，Activity类中相应添加了FragmentManager类。
        //FragmentManager类负责管理fragment并将它们的视图
        //添加到activity的视图层级结构中

//        FragmentManager fm = getSupportFragmentManager();//在extends Activity中则使用getFragmentManager()
//
//        /***
//         * Fragment事务
//         *
//         * 创建一个新的fragment事务，执行一个fragment添加操作，然后提交该事务
//         *
//         * FragmentManager.beginTransaction() 创建并返回FragmentTransaction实例
//         * **/
//        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
//        if(fragment == null) {
//            fragment = new CrimeFragment();
//            /**
//             * fragment argument
//             *
//             * 必须在fragment创建后、添加给activity前完成
//             * 使用getArguments()获得传递的数据
//             *
//             * 优点：比额外空间好在 回收内存时还要考虑实例变量会不会被回收
//             *
//             * setArguments方法常写在Fragments的静态方法内,比如newInstance(),getInstance()
//             * **/
//            Bundle args = new Bundle();
//            args.putInt("FragmentArgsInt",1234);
//            args.putString("FragmentArgsString",new Crime().getId().toString());
//            fragment.setArguments(args);
//            fm.beginTransaction()
//                    .add(R.id.fragment_container, fragment)
//                    .commit();
//        }
    }
}