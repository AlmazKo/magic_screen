package com.example.mtg_screen;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Almazko
 */
public class SlideFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.details, container, false);

        return v;
    }

    public static SlideFragment newInstance(int i) {
        return new SlideFragment();

    }

    public static SlideFragment newInstance(Bundle bundle) {
        return new SlideFragment();
    }
}
