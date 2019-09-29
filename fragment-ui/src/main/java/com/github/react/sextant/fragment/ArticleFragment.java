package com.github.react.sextant.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ArticleFragment extends Fragment {
    private View mView;
    private Button mButton;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        // args 3 false to 动态添加UI
        View v = inflater.inflate(R.layout.article_view, container, false);

        createIU(v);

        return v;
    }

    public void createIU(View v){
//        mView = getLayoutInflater().inflate(R.layout.article_view,null);
        mButton = (Button)v.findViewById(R.id.buttons);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StartFromFragment.class);
                startActivity(intent);
            }
        });
    }
}