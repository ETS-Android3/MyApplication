package com.github.react.sextant.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class FragmentMangerActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_manager_activity);

        //获取FragmentManager
        FragmentManager fm = getSupportFragmentManager();

        //fragment事务
        Fragment fragment = fm.findFragmentById(R.id.article_fragment);
        if(fragment == null) {
            fragment = new ArticleFragment();
            fm.beginTransaction()
                    .add(R.id.article_fragment, fragment)
                    .commit();
        }
    }
}
