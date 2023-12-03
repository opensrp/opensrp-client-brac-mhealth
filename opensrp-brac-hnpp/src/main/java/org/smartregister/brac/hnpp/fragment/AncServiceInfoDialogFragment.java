package org.smartregister.brac.hnpp.fragment;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.AncServiceInfoPagerAdapter;
import org.smartregister.brac.hnpp.fragment.anc_inofo.TextFragment;
import org.smartregister.brac.hnpp.fragment.anc_inofo.ImageFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AncServiceInfoDialogFragment extends DialogFragment {
    private List<Fragment> fragments = new ArrayList<>();
    ViewPager viewPager;
    ImageView dotOne,dotTwo;
    TextView second_tv;
    Button nextBt;
    LinearLayout secondLay;

    public AncServiceInfoDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(android.app.DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anc_service_info_dialog, container, false);
        initUi(view);

        AncServiceInfoPagerAdapter adapter = new AncServiceInfoPagerAdapter(getChildFragmentManager());

        fragments.add(new TextFragment());
        fragments.add(new ImageFragment());

        adapter.setupFragment(fragments);

        viewPager.setAdapter(adapter);

        setupTimer();

        setupDot(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
               setupDot(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });


        nextBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    private void setupTimer() {
        String FORMAT = "%02d";
        CountDownTimer countDownTimer = new CountDownTimer(11000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                second_tv.setText(String.format(
                        FORMAT,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        )
                )+"s");
            }

            @Override
            public void onFinish() {
                nextBt.setEnabled(true);
                secondLay.setVisibility(View.INVISIBLE);
            }
        };

        countDownTimer.start();
    }

    private void initUi(View view) {
        viewPager = view.findViewById(R.id.intro_viewpager);
        nextBt = view.findViewById(R.id.next_bt);
        dotOne = view.findViewById(R.id.dot_one);
        dotTwo = view.findViewById(R.id.dot_two);
        second_tv = view.findViewById(R.id.second_tv);
        secondLay = view.findViewById(R.id.second_lay);
    }

    private void setupDot(int pos) {
        if(pos == 0){
            dotOne.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getActivity(), R.color.black), PorterDuff.Mode.SRC_IN));
            dotTwo.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getActivity(), R.color.grey), PorterDuff.Mode.SRC_IN));
        }else {
            dotOne.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getActivity(), R.color.grey), PorterDuff.Mode.SRC_IN));
            dotTwo.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getActivity(), R.color.black), PorterDuff.Mode.SRC_IN));
        }
    }
}