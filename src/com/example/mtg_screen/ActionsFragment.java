package com.example.mtg_screen;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Almazko
 */
public class ActionsFragment extends Fragment {

    enum Stage {DISPOSAL, GAME}

    private Stage state;
    private View vs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getArguments();
        if (b == null) {
            state = Stage.DISPOSAL;
        } else {
            state = (Stage) b.getSerializable("stage");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v;
        if (state == Stage.DISPOSAL) {
            v = inflater.inflate(R.layout.panel_actions_start, container, false);
            View vStartImage = v.findViewById(R.id.panel_actions_btn_start);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyActivity activity = (MyActivity) getActivity();
                    if (activity != null) {
                        activity.start();
                        fade();

                    }
                }
            };

            v.setOnClickListener(listener);
            vStartImage.setOnClickListener(listener);
        } else {
            v = inflater.inflate(R.layout.panel_actions_game, container, false);
        }

        vs =v;
        return v;
    }

    public static ActionsFragment newInstance(Stage stage) {

        ActionsFragment sf = new ActionsFragment();
        Bundle args = new Bundle();
        args.putSerializable("stage", stage);
        sf.setArguments(args);

        return sf;
    }

    void fade() {
        ActionsFragment fg = newInstance(Stage.GAME);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ft.replace(R.id.f_actions, fg);
        ft.commit();

    }

    @Override
    public String toString() {
        return "ActionsFragment{" + "state=" + state + '}';
    }
}
