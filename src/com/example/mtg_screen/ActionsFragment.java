package com.example.mtg_screen;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Almazko
 */
public class ActionsFragment  extends Fragment {

    enum STAGE {DISPOSAL, GAME}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.action, container, false);
        View vStartImage = v.findViewById(R.id.panel_actions_start);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyActivity activity = (MyActivity) getActivity();
                if (activity != null) {
                    activity.start();
                }
            }
        };

        v.setOnClickListener(listener);
        vStartImage.setOnClickListener(listener);

        return v;
    }

}
