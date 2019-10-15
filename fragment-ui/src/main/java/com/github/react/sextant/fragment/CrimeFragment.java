package com.github.react.sextant.fragment;

//import android.app.Fragment;    //操作系统内置版本
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment; //支持库版本
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.react.sextant.fragment.util.Crime;

public class CrimeFragment extends Fragment {

    //Fragment生命周期
    @Override
    public void onCreate(Bundle savedInstanceState){//这里的onCreate是公共方法，在Activity内是受保护方法
        super.onCreate(savedInstanceState);
//        getArguments().getString("FragmentArgsString","ABCD");
    }

    /**
     * onCreateView
     * 实例化fragment视图的布局，然后将实例化的View返回给托管activity
     * **/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){

        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        /**
         * 从fragment中启动activity
         * **/
        v.findViewById(R.id.mButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CrimeActivity.class);
                startActivity(intent);
            }
        });

        //使用额外空间数据进行传递
        EditText a = (EditText)v.findViewById(R.id.mEditText);
        a.setText(new Crime().getId().toString());


        a.setText(saveInstanceState.getString("FragmentArgsString","ABCD"));

        /**
         * fragment argument
         * **/
        Bundle args = new Bundle();
        args.putInt("FragmentArgsInt",1234);
        args.putString("FragmentArgsString",new Crime().getId().toString());
        this.setArguments(args);

        return v;
    }
}
