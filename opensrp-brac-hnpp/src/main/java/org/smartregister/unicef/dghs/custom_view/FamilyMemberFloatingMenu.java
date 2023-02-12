package org.smartregister.unicef.dghs.custom_view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartregister.unicef.dghs.listener.OnClickFloatingMenu;


public class FamilyMemberFloatingMenu extends LinearLayout implements View.OnClickListener {
    public FloatingActionButton fab;
    public OnClickFloatingMenu onClickFloatingMenu;
    private RelativeLayout activityMain;
    private LinearLayout menuBar;
    private Animation fabOpen, fabClose, rotateForward, rotateBack;
    private boolean isFabMenuOpen = false;
    private View callLayout;
    private View referLayout;

    public FamilyMemberFloatingMenu(Context context) {
        super(context);
        initUi();
    }

    public void initUi() {
        inflate(getContext(), org.smartregister.chw.core.R.layout.view_individual_floating_menu, this);
        activityMain = findViewById(org.smartregister.chw.core.R.id.activity_main);
        menuBar = findViewById(org.smartregister.chw.core.R.id.menu_bar);
        fab = findViewById(org.smartregister.chw.core.R.id.fab);

        fabOpen = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.rotate_forward);
        rotateBack = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.rotate_back);

        callLayout = findViewById(org.smartregister.chw.core.R.id.call_layout);
        callLayout.setOnClickListener(this);

        referLayout = findViewById(org.smartregister.chw.core.R.id.refer_to_facility_layout);
        referLayout.setOnClickListener(this);

        callLayout.setClickable(false);
        referLayout.setClickable(false);

        menuBar.setVisibility(GONE);
    }

    public FamilyMemberFloatingMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUi();
    }

    public FamilyMemberFloatingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUi();
    }

    public View getCallLayout() {
        return callLayout;
    }

    public void setClickListener(OnClickFloatingMenu onClickFloatingMenu) {
        this.onClickFloatingMenu = onClickFloatingMenu;
    }

    public void animateFAB() {
        if (menuBar.getVisibility() == GONE) {
            menuBar.setVisibility(VISIBLE);
        }

        if (isFabMenuOpen) {
            activityMain.setBackgroundResource(org.smartregister.chw.core.R.color.transparent);

            fab.startAnimation(rotateBack);
            fab.setImageResource(org.smartregister.chw.core.R.drawable.ic_edit_white);

            callLayout.startAnimation(fabClose);
            referLayout.startAnimation(fabClose);

            callLayout.setClickable(false);
            referLayout.setClickable(false);
            isFabMenuOpen = false;

        } else {
            activityMain.setBackgroundResource(org.smartregister.chw.core.R.color.grey_tranparent_50);

            fab.startAnimation(rotateForward);
            fab.setImageResource(org.smartregister.chw.core.R.drawable.ic_input_add);

            callLayout.startAnimation(fabOpen);
            referLayout.startAnimation(fabOpen);

            callLayout.setClickable(true);
            referLayout.setClickable(true);

            isFabMenuOpen = true;
        }
    }

    @Override
    public void onClick(View v) {
        onClickFloatingMenu.onClickMenu(v.getId());
    }

    public void redrawWithOption(FamilyMemberFloatingMenu menu, boolean has_phone) {
        TextView callTextView = menu.findViewById(org.smartregister.chw.core.R.id.CallTextView);
        TextView callTextViewHint = menu.findViewById(org.smartregister.chw.core.R.id.CallTextViewHint);

        if (has_phone) {

            callTextViewHint.setVisibility(GONE);
            menu.getCallLayout().setOnClickListener(menu);
            callTextView.setTypeface(null, Typeface.NORMAL);
            callTextView.setTextColor(menu.getResources().getColor(android.R.color.black));
            ((FloatingActionButton) menu.findViewById(org.smartregister.chw.core.R.id.callFab)).getDrawable().setAlpha(255);

        } else {

            callTextViewHint.setVisibility(VISIBLE);
            menu.getCallLayout().setOnClickListener(null);
            callTextView.setTypeface(null, Typeface.ITALIC);
            callTextView.setTextColor(menu.getResources().getColor(org.smartregister.chw.core.R.color.grey));
            ((FloatingActionButton) menu.findViewById(org.smartregister.chw.core.R.id.callFab)).getDrawable().setAlpha(122);
        }
    }

    public void hideFab() {
        fab.hide();
    }
}
