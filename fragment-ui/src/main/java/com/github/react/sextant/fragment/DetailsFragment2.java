package com.github.react.sextant.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.react.sextant.fragment.util.List2;

public class DetailsFragment2 extends Fragment {

    /**     * Create a new instance of DetailsFragment, initialized to     * show the text at ’index’.     */
    public static DetailsFragment2 newInstance(int index) {
        DetailsFragment2 f = new DetailsFragment2();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        ScrollView scroller = new ScrollView(getActivity());
        TextView text = new TextView(getActivity());

        int padding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, getActivity().getResources()
                        .getDisplayMetrics());
        text.setPadding(padding, padding, padding, padding);
        scroller.addView(text);
        text.setText(List2.title.get(getArguments().getInt("index", 0)));
        return scroller;
    }
}