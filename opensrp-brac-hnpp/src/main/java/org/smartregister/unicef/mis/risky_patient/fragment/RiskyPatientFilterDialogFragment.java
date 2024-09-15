package org.smartregister.unicef.mis.risky_patient.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import org.smartregister.unicef.mis.R;


public class RiskyPatientFilterDialogFragment extends DialogFragment {
    static final int RESULT_CODE = 131;

    static final String VIS_TODAY = "vis_today";
    static final String VIS_NEXT_THREE = "vis_next_three";
    static final String VIS_NEXT_SEVEN = "vis_next_seven";
    static final String VIS_LAST_DAY = "vis_last_day";
    static final String VIS_LAST_THREE = "vis_last_three";
    static final String VIS_LAST_SEVEN = "vis_last_seven";
    static final String VIS_ALL_DAY = "vis_all_day";

    AppCompatButton filterBt;
    RadioGroup radioGroup;
  /*  RadioButton visitScheduleTodayCb;
    RadioButton visitScheduleNextThreeCb;
    RadioButton visitScheduleNextSevenCb;
    RadioButton visitScheduleLastCb;
    RadioButton visitScheduleLastThreeCb;
    RadioButton visitScheduleLastSevenCb;
    RadioButton allDueCb;*/

    int visitScheduleToday = 0;
    int visitScheduleNextThree = 0;
    int visitScheduleNextSeven = 0;
    int visitScheduleLastDay = 0;
    int visitScheduleLastThree = 0;
    int visitScheduleLastSeven = 0;
    int visitScheduleAllDue = 0;

    public RiskyPatientFilterDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.app.DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_risky_patient_filter_dialog, container, false);
        initUi(view);
        viewInteraction();

        return view;
    }

    private void viewInteraction() {
        filterBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert getTargetFragment() != null;

                Intent intent = new Intent();
                intent.putExtra(VIS_TODAY,visitScheduleToday);
                intent.putExtra(VIS_NEXT_THREE,visitScheduleNextThree);
                intent.putExtra(VIS_NEXT_SEVEN,visitScheduleNextSeven);
                intent.putExtra(VIS_LAST_DAY,visitScheduleLastDay);
                intent.putExtra(VIS_LAST_THREE,visitScheduleLastThree);
                intent.putExtra(VIS_LAST_SEVEN,visitScheduleLastSeven);
                intent.putExtra(VIS_ALL_DAY,visitScheduleAllDue);

                getTargetFragment().onActivityResult(getTargetRequestCode(),RESULT_CODE,intent);
                dismiss();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.visit_schedule_today_cb:
                        visitScheduleToday = 1;
                        break;
                    case R.id.visit_next_three_cb:
                        visitScheduleNextThree = 1;
                        break;
                    case R.id.visit_next_seven_cb:
                        visitScheduleNextSeven = 1;
                        break;
                    case R.id.visit_last_day:
                        visitScheduleLastDay = 1;
                        break;
                    case R.id.visit_last_three_day:
                        visitScheduleLastThree = 1;
                        break;
                    case R.id.visit_last_seven_day:
                        visitScheduleLastSeven = 1;
                        break;
                    default:
                        visitScheduleAllDue = 1;
                        break;

                }
            }
        });
        /*visitScheduleTodayCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitScheduleToday = visitScheduleTodayCb.isChecked()?1:0;
            }
        });

        visitScheduleNextThreeCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitScheduleNextThree = visitScheduleNextThreeCb.isChecked()?1:0;
            }
        });

        visitScheduleNextSevenCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitScheduleNextSeven = visitScheduleNextSevenCb.isChecked()?1:0;
            }
        });

        visitScheduleLastCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitScheduleLastDay = visitScheduleLastCb.isChecked()?1:0;
            }
        });

        visitScheduleLastThreeCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitScheduleLastThree = visitScheduleLastThreeCb.isChecked()?1:0;
            }
        });

        visitScheduleLastSevenCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitScheduleLastSeven = visitScheduleLastSevenCb.isChecked()?1:0;
            }
        });

        allDueCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitScheduleAllDue = allDueCb.isChecked()?1:0;
            }
        });*/
    }

    private void initUi(View view) {
        filterBt = view.findViewById(R.id.filter_bt);
        radioGroup = view.findViewById(R.id.radioGroup);
        /*visitScheduleNextThreeCb = view.findViewById(R.id.visit_next_three_cb);
        visitScheduleNextSevenCb = view.findViewById(R.id.visit_next_seven_cb);
        visitScheduleLastCb = view.findViewById(R.id.visit_last_day);
        visitScheduleLastThreeCb = view.findViewById(R.id.visit_last_three_day);
        visitScheduleLastSevenCb = view.findViewById(R.id.visit_last_seven_day);
        allDueCb = view.findViewById(R.id.visit_all_due);*/
    }
}