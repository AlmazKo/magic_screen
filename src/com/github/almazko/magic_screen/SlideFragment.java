package com.github.almazko.magic_screen;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;

/**
 * @author Almazko
 */
public class SlideFragment extends Fragment {

    private static final String INDEX = "index";
    private int index;

    public static int getResId(String variableName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    int getIndex() {
        return index;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        index = b.getInt(INDEX);
        if (savedInstanceState != null && savedInstanceState.containsKey(INDEX)) {
            index = savedInstanceState.getInt(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.details, container, false);
        ImageView iv = (ImageView) v.findViewById(R.id.scr_background);
        int resourceId = getResId("bg_" + index, R.drawable.class);
        iv.setImageResource(resourceId);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(INDEX, index);
        super.onSaveInstanceState(outState);
    }

    public static SlideFragment newInstance(int i) {

        SlideFragment sf = new SlideFragment();
        Bundle args = new Bundle();
        args.putInt(INDEX, i);
        sf.setArguments(args);

        return sf;
    }

    public static SlideFragment newInstance(Bundle bundle) {
        int index = bundle.getInt(INDEX);
        return newInstance(index);
    }

    @Override
    public String toString() {
        return "SlideFragment{" + index + '}';
    }
}
