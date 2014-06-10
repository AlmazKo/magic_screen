package com.example.mtg_screen;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mtg_screen.MyActivity.Stage;

/**
 * @author Almazko
 */
public class ActionsFragment extends Fragment {

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

        switch (state) {
            case DISPOSAL:
                return onDisposalStage(inflater, container, savedInstanceState);

            case GAME:
                return onGame(inflater, container, savedInstanceState);

            case PAUSE:
                return onPause(inflater, container, savedInstanceState);
            default:
                return null;
        }
    }

    private View onDisposalStage(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.panel_actions_start, container, false);
        View vStartImage = v.findViewById(R.id.panel_actions_btn_start);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyActivity activity = (MyActivity) getActivity();
                if (activity != null) {
                    activity.start();
                    transition(Stage.GAME);
                }
            }
        };

        v.setOnClickListener(listener);
        vStartImage.setOnClickListener(listener);


        return v;
    }

    private View onGame(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.panel_actions_game, container, false);


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyActivity activity = (MyActivity) getActivity();
                if (activity != null) {
                    activity.pause();
                    transition(Stage.PAUSE);

                }
            }
        };

        v.setOnClickListener(listener);

        return v;
    }


    private View onPause(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View pauseView = inflater.inflate(R.layout.panel_actions_pause, container, false);
        View btnResume = pauseView.findViewById(R.id.panel_actions_btn_start);
        View btnRestart = pauseView.findViewById(R.id.panel_actions_btn_restart);

        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyActivity activity = (MyActivity) getActivity();
                if (activity != null) {
                    activity.resume();
                    transition(Stage.GAME);
                }
            }
        });

        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyActivity activity = (MyActivity) getActivity();
                if (activity != null) {
                    activity.reset();
                    transition(Stage.DISPOSAL);
                }
            }
        });

        return pauseView;
    }


    public static ActionsFragment newInstance(Stage stage) {

        ActionsFragment sf = new ActionsFragment();
        Bundle args = new Bundle();
        args.putSerializable("stage", stage);
        sf.setArguments(args);

        return sf;
    }

    void transition(Stage stage) {
        ActionsFragment fg = newInstance(stage);
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
