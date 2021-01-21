package com.github.react.sextant.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.react.sextant.fragment.util.List2;

public class ListFragment2 extends ListFragment {

    int mCurCheckPosition = 0;
    int mShownCheckPosition = -1;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setListAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                List2.title)); //使用静态数组填充列表
        if (savedInstanceState != null) {
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
            mShownCheckPosition = savedInstanceState.getInt("shownChoice", -1);
        }
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//        showDetails(mCurCheckPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curChoice", mCurCheckPosition);
        outState.putInt("shownChoice", mShownCheckPosition);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
//        showDetails(position);
    }

    /**
     *显示listview item 详情
     */
//    void showDetails(int index) {
//        mCurCheckPosition = index;
//        getListView().setItemChecked(index, true);
//
//        if (mShownCheckPosition != mCurCheckPosition) {
//
//            DetailsFragment2 df = DetailsFragment2.newInstance(index);
//            FragmentTransaction ft = getFragmentManager()
//                    .beginTransaction();
//            ft.replace(R.id.details, df);
//            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//            ft.commit();
//            mShownCheckPosition = index;
//        }
//    }

}