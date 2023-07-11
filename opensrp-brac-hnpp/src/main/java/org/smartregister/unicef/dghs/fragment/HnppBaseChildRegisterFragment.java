package org.smartregister.unicef.dghs.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.model.CoreChildRegisterFragmentModel;
import org.smartregister.chw.core.presenter.CoreChildRegisterFragmentPresenter;
import org.smartregister.chw.core.provider.CoreChildRegisterProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.family.fragment.NoMatchDialogFragment;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.ChildRegisterActivity;
import org.smartregister.unicef.dghs.activity.HnppChildProfileActivity;
import org.smartregister.unicef.dghs.adapter.ChildFilterTypeAdapter;
import org.smartregister.unicef.dghs.nativation.view.NavigationMenu;
import org.smartregister.unicef.dghs.utils.FilterDialog;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.FetchStatus;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;
import org.smartregister.view.fragment.BaseRegisterFragment;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import timber.log.Timber;
import static android.view.View.inflate;

public  class HnppBaseChildRegisterFragment extends BaseRegisterFragment implements android.view.View.OnClickListener, CoreChildRegisterFragmentContract.View{
    private final String DEFAULT_MAIN_CONDITION = "date_removed is null";
    String searchFilterString = "";
    protected String selectedStartDateFilterValue,selectedEndDateFilterValue,isAefiChild,isDropOutChild;
    private TextView textViewVillageNameFilter, textViewClasterNameFilter,textViewMonthNameFilter;
    private ImageView imageViewVillageNameFilter, imageViewClasterNameFilter;
    private RelativeLayout clusterView,monthFilterView;
    protected ViewGroup clients_header_layout;
    protected int month =-1,year =-1;
    public static final String CLICK_VIEW_NORMAL = "click_view_normal";
    public static final String CLICK_VIEW_DOSAGE_STATUS = "click_view_dosage_status";
    private static final String DUE_FILTER_TAG = "PRESSED";
    private View view;
    protected View dueOnlyLayout;
    private boolean dueFilterActive = false;
    boolean isExpanded = false;
    boolean isClickedAefi = false;
    boolean isClickedDropOut = false;
    ChildFilterTypeAdapter adapter;
    protected String fromDate = "";
    protected String toDate = "";
    private int oldPosition = -1;
    private  TextView filterTextTv;
    Button dropOutButton;
    Button resetFilterButton;
    Button aefiButton;
    RecyclerView filterTypeRv;
    ImageView arrowImageView;
    int layoutHeightWithExpand = 800;
    int layoutHeightWithoutExpand = 300;

    @Override
    public void setupViews(View view) {
        try{
            super.setupViews(view);
            Toolbar toolbar = view.findViewById(org.smartregister.R.id.register_toolbar);
            toolbar.setContentInsetsAbsolute(0, 0);
            toolbar.setContentInsetsRelative(0, 0);
            toolbar.setContentInsetStartWithNavigation(0);
            NavigationMenu.getInstance(getActivity(), null, toolbar);
            // Update top left icon
            qrCodeScanImageView = view.findViewById(org.smartregister.family.R.id.scanQrCode);
            if (qrCodeScanImageView != null) {
                qrCodeScanImageView.setVisibility(View.GONE);
            }

            // Update Search bar
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            View searchBarLayout = view.findViewById(org.smartregister.chw.core.R.id.search_bar_layout);
            searchBarLayout.setLayoutParams(params);
            searchBarLayout.setBackgroundResource(org.smartregister.chw.core.R.color.chw_primary);
            searchBarLayout.setPadding(searchBarLayout.getPaddingLeft(), searchBarLayout.getPaddingTop(), searchBarLayout.getPaddingRight(), (int) Utils.convertDpToPixel(10, getActivity()));


            if (getSearchView() != null) {
                getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
                getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
                getSearchView().setTextColor(getResources().getColor(org.smartregister.chw.core.R.color.text_black));
            }

            // Update title name
            ImageView logo = view.findViewById(org.smartregister.family.R.id.opensrp_logo_image_view);
            if (logo != null) {
                logo.setVisibility(View.GONE);
            }

            CustomFontTextView titleView = view.findViewById(org.smartregister.family.R.id.txt_title_label);
            if (titleView != null) {
                titleView.setVisibility(View.VISIBLE);
                titleView.setText(getString(getToolBarTitle()));
                titleView.setFontVariant(FontVariant.REGULAR);
                titleView.setPadding(0, titleView.getTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());
            }

            View navbarContainer = view.findViewById(org.smartregister.chw.core.R.id.register_nav_bar_container);
            navbarContainer.setFocusable(false);

            View topLeftLayout = view.findViewById(org.smartregister.chw.core.R.id.top_left_layout);
            topLeftLayout.setVisibility(View.GONE);

            View topRightLayout = view.findViewById(org.smartregister.chw.core.R.id.top_right_layout);
            topRightLayout.setVisibility(View.VISIBLE);

            View sortFilterBarLayout = view.findViewById(org.smartregister.chw.core.R.id.register_sort_filter_bar_layout);
            sortFilterBarLayout.setVisibility(View.GONE);

            this.view = view;

            dueOnlyLayout = view.findViewById(org.smartregister.chw.core.R.id.due_only_layout);
            dueOnlyLayout.setVisibility(View.VISIBLE);
            dueOnlyLayout.setOnClickListener(registerActionHandler);
        }catch (Exception e){
            HnppApplication.getHNPPInstance().forceLogout();
            return;
        }
        HnppConstants.updateAppBackground((view.findViewById(R.id.register_nav_bar_container)));
        HnppConstants.updateAppBackground(view.findViewById(org.smartregister.R.id.register_toolbar));
        RelativeLayout sortAndFilterView = view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout);
        sortAndFilterView.setVisibility(android.view.View.GONE);
        TextView sortView = sortAndFilterView.findViewById(R.id.sort_text_view);
        //TextView filterTextView = sortAndFilterView.findViewById(R.id.filter_text_view);
        sortView.setText(getString(R.string.sort));
        //filterTextView.setText(getString(R.string.filter));
        android.view.View searchBarLayout = view.findViewById(org.smartregister.family.R.id.search_bar_layout);
        searchBarLayout.setBackgroundResource(org.smartregister.family.R.color.customAppThemeBlue);
        if (getSearchView() != null) {
            getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
        }
        dueOnlyLayout.setVisibility(android.view.View.GONE);
        //filterTextView.setOnClickListener(registerActionHandler);
        clients_header_layout = view.findViewById(org.smartregister.chw.core.R.id.clients_header_layout);
        android.view.View filterView = inflate(getContext(), R.layout.child_list_filter_view, clients_header_layout);
        LinearLayout filterButtonLay = view.findViewById(R.id.filter_button_lay);
        View filterButtonView;

        if(checkDevice() == DeviceType.TABLET){
            filterButtonView = getLayoutInflater()
                    .inflate(R.layout.child_list_filter_buttons_tablet, filterButtonLay, false);
             layoutHeightWithExpand = 450;
             layoutHeightWithoutExpand = 150;
        }else {
            filterButtonView = getLayoutInflater()
                    .inflate(R.layout.child_list_filter_buttons_phone, filterButtonLay, false);
             layoutHeightWithExpand = 800;
             layoutHeightWithoutExpand = 300;
        }

        filterButtonLay.addView(filterButtonView);

        filterTypeRv = filterView.findViewById(R.id.filter_type_rv);
        ConstraintLayout filterDateLay = filterView.findViewById(R.id.filter_date_lay);
        arrowImageView = filterView.findViewById(R.id.arrow_image);
        filterTextTv = filterView.findViewById(R.id.filter_text_view);

        aefiButton = filterView.findViewById(R.id.aefi_button);
        dropOutButton = filterView.findViewById(R.id.drop_out_button);
        resetFilterButton = filterView.findViewById(R.id.reset_filter_button);

        setupFilterAdapter();

        aefiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               updateAefiChildButton(false);
               isClickedAefi = !isClickedAefi;
            }
        });

        dropOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               updateDropoutButton(false);
               isClickedDropOut = !isClickedDropOut;
            }
        });

        resetFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetOtherFilter();

                setupFilterAdapter();
                updateAefiChildButton(true);
                updateDropoutButton(true);
                isClickedAefi = false;
                isClickedDropOut = false;

                filterTextTv.setText(getString(R.string.no_filter_applied));
            }
        });

        arrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isExpanded){
                    clients_header_layout.getLayoutParams().height = layoutHeightWithoutExpand;
                    filterTypeRv.setVisibility(View.GONE);
                    arrowImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
                }else{
                    clients_header_layout.getLayoutParams().height = layoutHeightWithExpand;
                    filterTypeRv.setVisibility(View.VISIBLE);
                    arrowImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                }

                isExpanded = !isExpanded;
            }
        });

        filterDateLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isExpanded){
                    clients_header_layout.getLayoutParams().height = layoutHeightWithoutExpand;
                    filterTypeRv.setVisibility(View.GONE);
                    arrowImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
                }else{
                    clients_header_layout.getLayoutParams().height = layoutHeightWithExpand;
                    filterTypeRv.setVisibility(View.VISIBLE);
                    arrowImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                }

                isExpanded = !isExpanded;
            }
        });

        clients_header_layout.getLayoutParams().height = layoutHeightWithoutExpand;
        clients_header_layout.setVisibility(View.VISIBLE);
        if (getSearchCancelView() != null) {
            getSearchCancelView().setOnClickListener(this);
        }
        //setTotalPatients();
    }

    private DeviceType checkDevice() {
       try{
           DisplayMetrics metrics = new DisplayMetrics();
           getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

           float yInches= metrics.heightPixels/metrics.ydpi;
           float xInches= metrics.widthPixels/metrics.xdpi;
           double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches);
           if (diagonalInches >= 6.5){
              return DeviceType.TABLET;
           }else{
               return DeviceType.MOBILE;
           }
       }catch (Exception e){
           return DeviceType.MOBILE;
       }
    }

    void setupFilterAdapter(){
        adapter = new ChildFilterTypeAdapter(new ChildFilterTypeAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, String content) {
                if(oldPosition != position){
                    clients_header_layout.getLayoutParams().height = layoutHeightWithoutExpand;
                    filterTypeRv.setVisibility(View.GONE);
                    arrowImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
                    isExpanded = !isExpanded;
                    filterTextTv.setText(content);
                    updateContent(content);
                    updateFilterView();

                    oldPosition = position;

                    if (!filterTypeRv.isComputingLayout()) {
                        adapter.notifyDataSetChanged();
                    }

                    Log.v("FILTER_CHILD","content>>"+content);
                }
            }
        });
        adapter.setData(HnppConstants.filterTypeList);

        filterTypeRv.setLayoutManager(new GridLayoutManager(getActivity(),3));
        filterTypeRv.setAdapter(adapter);
    }

    void updateDropoutButton(boolean isReset){
        Drawable buttonDrawable = dropOutButton.getBackground();
        buttonDrawable = DrawableCompat.wrap(buttonDrawable);

        if(isReset){
            DrawableCompat.setTint(buttonDrawable, Color.GRAY);
            isDropOutChild = "";
        }else{
            if(!isClickedDropOut){
                DrawableCompat.setTint(buttonDrawable, getResources().getColor(R.color.btn_blue));
                isDropOutChild = "yes";
            }else {
                DrawableCompat.setTint(buttonDrawable, Color.GRAY);
                isDropOutChild = "";
            }
        }
        updateFilterView();
        dropOutButton.setBackground(buttonDrawable);
    }

    void updateAefiChildButton(boolean isReset){
        Drawable buttonDrawable = aefiButton.getBackground();
        buttonDrawable = DrawableCompat.wrap(buttonDrawable);

        if(isReset){
            DrawableCompat.setTint(buttonDrawable, Color.GRAY);
            isAefiChild = "";
        }else {
            if(!isClickedAefi){
                DrawableCompat.setTint(buttonDrawable, getResources().getColor(R.color.btn_blue));
                isAefiChild = "yes";
            }else {
                DrawableCompat.setTint(buttonDrawable, Color.GRAY);
                isAefiChild = "";
            }
        }
        updateFilterView();
        aefiButton.setBackground(buttonDrawable);
    }

    private void updateContent(String content) {
        resetOtherFilter();
        switch (content.toLowerCase()){
            case "today":
                 selectedStartDateFilterValue = HnppConstants.getToday();
                 break;
            case "yesterday":
                selectedStartDateFilterValue = HnppConstants.getYesterDay();
                break;
            case "tomorrow":
                selectedStartDateFilterValue = HnppConstants.getTomorrowDay();
                break;
            case "this week":
                selectedStartDateFilterValue = HnppConstants.getThisWeekDay()[0];
                selectedEndDateFilterValue = HnppConstants.getThisWeekDay()[1];
                break;

            case "last week":
                selectedStartDateFilterValue = HnppConstants.getLastWeekDay()[0];
                selectedEndDateFilterValue = HnppConstants.getLastWeekDay()[1];
                break;

            case "next week":
                selectedStartDateFilterValue = HnppConstants.geNextWeekDay()[0];
                selectedEndDateFilterValue = HnppConstants.geNextWeekDay()[1];
                break;

            case "this month":
                selectedStartDateFilterValue = HnppConstants.getThisMonth()[0];
                selectedEndDateFilterValue = HnppConstants.getThisMonth()[1];
                break;

            case "last month":
                selectedStartDateFilterValue = HnppConstants.getLastMonth()[0];
                selectedEndDateFilterValue = HnppConstants.getLastMonth()[1];
                break;

            case "next month":
                selectedStartDateFilterValue = HnppConstants.getNextMonth()[0];
                selectedEndDateFilterValue = HnppConstants.getNextMonth()[1];
                break;

            case "anytime":
                break;

            case "from - to":
                showFromToDatePicker(DatePickerType.FROM);
                break;
        }
    }

    /*
      resetting others filter data
     */
    void resetOtherFilter() {
        selectedStartDateFilterValue = "";
        selectedEndDateFilterValue = "";
        fromDate = "";
        toDate = "";
    }

    /**
     * from to date picker
     */
    private void showFromToDatePicker(DatePickerType datePickerType) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String title = "";
        if(datePickerType == DatePickerType.FROM){
            title = getString(R.string.from_date);
        }else if(datePickerType == DatePickerType.TO){
            title = getString(R.string.to_date);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, year1, month1, dayOfMonth) -> {
            String modifiedMonth;
            if(month1 < 10){
                modifiedMonth = "0"+(month1 +1);
            }else {
                modifiedMonth = ""+(month1 +1);
            }
            if(datePickerType == DatePickerType.FROM){
                fromDate = year1 +"-"+ modifiedMonth +"-"+dayOfMonth;
                showFromToDatePicker(DatePickerType.TO);
            }else if(datePickerType == DatePickerType.TO){
                toDate = year1 +"-"+ modifiedMonth +"-"+dayOfMonth;
                /*
                  from date and to date validation
                 */
                try {
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date from = sf.parse(fromDate);
                    Date to = sf.parse(toDate);
                    assert to != null;
                    if(to.before(from) || to.equals(from)){
                        toDate = "";
                        Toast.makeText(getActivity(),getString(R.string.from_to_date_validation_msg)+fromDate+")",Toast.LENGTH_SHORT).show();
                        showFromToDatePicker(DatePickerType.TO);
                    }else{
                        filterTextTv.setText(String.format("%s - %s", fromDate, toDate));
                        updateFilterView();
                    }
                } catch (ParseException e) {
                    toDate = "";
                    e.printStackTrace();
                }

            }
        },year,month,day);
        datePickerDialog.setCancelable(false);
        datePickerDialog.setMessage(title);
        datePickerDialog.show();
    }

    @Override
    public CoreChildRegisterFragmentContract.Presenter presenter() {
        return (CoreChildRegisterFragmentContract.Presenter) presenter;
    }
    @Override
    public void setUniqueID(String s) {
        if (getSearchView() != null) {
            getSearchView().setText(s);
        }
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        //// TODO: 15/08/19
    }
    protected int getToolBarTitle() {
        return org.smartregister.chw.core.R.string.child_register_title;
    }
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new CoreChildRegisterFragmentPresenter(this, new CoreChildRegisterFragmentModel(), viewConfigurationIdentifier);

    }
    @Override
    protected void onResumption() {
        if (dueFilterActive && dueOnlyLayout != null) {
            dueFilter(dueOnlyLayout);
        } else {
            super.onResumption();
        }
    }
    private void dueFilter(View dueOnlyLayout) {
        filter(searchText(), "", presenter().getDueFilterCondition());
        dueOnlyLayout.setTag(DUE_FILTER_TAG);
        switchViews(dueOnlyLayout, true);
    }
    protected void filter(String filterString, String joinTableString, String mainConditionString) {
        filters = filterString;
        joinTable = joinTableString;
        mainCondition = mainConditionString;
        filterandSortExecute(countBundle());
    }
    private String searchText() {
        return (getSearchView() == null) ? "" : getSearchView().getText().toString();
    }

    private void switchViews(View dueOnlyLayout, boolean isPress) {
        TextView dueOnlyTextView = dueOnlyLayout.findViewById(org.smartregister.chw.core.R.id.due_only_text_view);
        if (isPress) {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, org.smartregister.chw.core.R.drawable.ic_due_filter_on, 0);
        } else {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, org.smartregister.chw.core.R.drawable.ic_due_filter_off, 0);

        }
    }
    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        CoreChildRegisterProvider childRegisterProvider = new CoreChildRegisterProvider(getActivity(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(android.view.View v) {
        if (getActivity() == null) {
            return;
        }

        if (view.getTag() != null && view.getTag(org.smartregister.chw.core.R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
            if (view.getTag() instanceof CommonPersonObjectClient) {
                goToChildDetailActivity((CommonPersonObjectClient) view.getTag(), false);
            }
        } else if (view.getId() == org.smartregister.chw.core.R.id.due_only_layout) {
            toggleFilterSelection(view);
        }
        switch (v.getId()) {
            case R.id.village_filter_img:
                updateFilterView();
                break;
            case R.id.claster_filter_img:
                updateFilterView();
                break;
            case R.id.month_filter_img:
                month = -1;
                year = -1;
                updateFilterView();
                break;
            case R.id.btn_search_cancel:
                searchFilterString = "";
                month = -1;
                year = -1;
                if (getSearchView() != null) {
                    getSearchView().setText("");
                }
                clients_header_layout.setVisibility(android.view.View.GONE);
                updateFilterView();
                break;
        }
    }
    public void goToChildDetailActivity(CommonPersonObjectClient patient,
                                        boolean launchDialog) {
        if (launchDialog) {
            Timber.i(patient.name);
        }

        Intent intent = new Intent(getActivity(), HnppChildProfileActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        startActivity(intent);
    }

    public void toggleFilterSelection(View dueOnlyLayout) {
        if (dueOnlyLayout != null) {
            if (dueOnlyLayout.getTag() == null) {
                dueFilterActive = true;
                dueFilter(dueOnlyLayout);
            } else if (dueOnlyLayout.getTag().toString().equals(DUE_FILTER_TAG)) {
                dueFilterActive = false;
                normalFilter(dueOnlyLayout);
            }
        }
    }
    private void normalFilter(View dueOnlyLayout) {
        filter(searchText(), "", presenter().getMainCondition());
        dueOnlyLayout.setTag(null);
        switchViews(dueOnlyLayout, false);
    }
    protected void openFilterDialog(boolean isNeedToShowDate){
//        new FilterDialog().showDialog(isNeedToShowDate,getActivity(), new FilterDialog.OnFilterDialogFilter() {
//            @Override
//            public void onDialogPress(String ssName, String villageName, String cluster,int m, int y) {
//                mSelectedClasterName = cluster;
//                mSelectedVillageName = villageName;
//                month = m;
//                year = y;
//                if(!isNeedToShowDate) monthFilterView.setVisibility(View.INVISIBLE);
//                updateFilterView();
//            }
//        });
    }
    public void updateFilterView(){
//        if(StringUtils.isEmpty(mSelectedVillageName) && StringUtils.isEmpty(mSelectedClasterName) && month==-1 && year == -1){
//            clients_header_layout.setVisibility(android.view.View.GONE);
//        } else {
//            clients_header_layout.setVisibility(android.view.View.VISIBLE);
//        }
//        if(month == -1 && year == -1){
//            textViewMonthNameFilter.setText(getString(R.string.filter_month_name, "সকল"));
//        }else{
//            String monthYearStr = HnppJsonFormUtils.monthBanglaStr[month-1]+","+year;
//            textViewMonthNameFilter.setText(getString(R.string.filter_month_name, monthYearStr));
//        }
//        if(StringUtils.isEmpty(mSelectedVillageName)){
//            textViewVillageNameFilter.setText(getString(R.string.filter_village_name, "সকল"));
//
//        }else{
//            textViewVillageNameFilter.setText(getString(R.string.filter_village_name, mSelectedVillageName));
//        }
//
//        if(HnppConstants.isPALogin()){
//            clusterView.setVisibility(android.view.View.GONE);
//        }else{
//            if(StringUtils.isEmpty(mSelectedClasterName)){
//                textViewClasterNameFilter.setText(getString(R.string.claster_village_name, "সকল"));
//
//            }else{
//                textViewClasterNameFilter.setText(getString(R.string.claster_village_name, HnppConstants.getClusterNameFromValue(mSelectedClasterName)));
//            }
//        }
        filter(searchFilterString, "", DEFAULT_MAIN_CONDITION,false);

    }
    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        try{
            if (!SyncStatusBroadcastReceiver.getInstance().isSyncing() && (FetchStatus.fetched.equals(fetchStatus) || FetchStatus.nothingFetched.equals(fetchStatus))) {
                org.smartregister.util.Utils.showShortToast(getActivity(), getString(org.smartregister.chw.core.R.string.sync_complete));
                refreshSyncProgressSpinner();
            }
        }catch (WindowManager.BadTokenException e){

        }

    }
    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        try{
            try{
                org.smartregister.util.Utils.showShortToast(getActivity(), getString(org.smartregister.chw.core.R.string.sync_complete));
            }catch (WindowManager.BadTokenException e){
                e.printStackTrace();
            }
            refreshSyncProgressSpinner();
            if (syncProgressBar != null) {
                syncProgressBar.setVisibility(View.GONE);
            }
            if (syncButton != null) {
                syncButton.setVisibility(View.GONE);
            }
        }catch (WindowManager.BadTokenException e){

        }

    }
    @Override
    protected void refreshSyncProgressSpinner() {
        super.refreshSyncProgressSpinner();
        if (syncButton != null) {
            syncButton.setVisibility(View.GONE);
        }
    }
    protected String visitType ="";

    @Override
    public void countExecute() {
//        StringBuilder customFilter = new StringBuilder();
//        if (StringUtils.isNotBlank(searchFilterString)) {
//            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
//            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.LAST_NAME, searchFilterString));
//            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.MIDDLE_NAME, searchFilterString));
//            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.PHONE_NUMBER, searchFilterString));
//            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));
//
//        }
//        if(!StringUtils.isEmpty(mSelectedClasterName)&&!StringUtils.isEmpty(mSelectedVillageName)){
//            customFilter.append(MessageFormat.format(" and ( {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
//            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ) ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));
//
//        }else if(!StringUtils.isEmpty(mSelectedClasterName)){
//            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.CLASTER, mSelectedClasterName));
//
//        }else if(!StringUtils.isEmpty(mSelectedVillageName)){
//            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.FAMILY, HnppConstants.KEY.VILLAGE_NAME, mSelectedVillageName));
//        }
//        if(month!=-1){
//
//            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+HnppConstants.addZeroForMonth(month+"")+"'"));
//            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));
//            if(!visitType.isEmpty()){
//                customFilter.append(visitType);
//            }
//            customFilter.append(" group by ec_family_member.base_entity_id");
//        }
//        String sql = "";
//        try {
//            if(month != -1){
//                    int beforeIndex = mainSelect.indexOf("WHERE");
//                    int length = mainSelect.length();
//                    String lastPart = mainSelect.substring(beforeIndex,length);
//                    String tempmainSelect = mainSelect.substring(0,beforeIndex);
//                    sql = tempmainSelect+" inner join ec_visit_log on ec_family_member.base_entity_id = ec_visit_log.base_entity_id "+lastPart;
//                }else{
//                    sql = mainSelect;
//                }
//            if (StringUtils.isNotBlank(customFilter)) {
//                sql = sql + customFilter;
//            }
//            List<String> ids = commonRepository().findSearchIds(sql);
//            clientAdapter.setTotalcount(ids.size());
//            Log.v("VIST_QUERY","count sql:"+sql);
//        } catch (Exception e) {
//            Timber.e(e);
//        }
    }
    protected String filterandSortQuery() {
        String sql = "";
        if(month != -1){
            int beforeIndex = mainSelect.indexOf("WHERE");
            int length = mainSelect.length();
            String lastPart = mainSelect.substring(beforeIndex,length);
            String tempmainSelect = mainSelect.substring(0,beforeIndex);
            sql = tempmainSelect+" inner join ec_visit_log on ec_family_member.base_entity_id = ec_visit_log.base_entity_id "+lastPart;
        }else{
            sql = mainSelect;
        }
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(sql);
        StringBuilder customFilter = new StringBuilder();
        if (StringUtils.isNotBlank(searchFilterString)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.FIRST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.LAST_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.MIDDLE_NAME, searchFilterString));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.PHONE_NUMBER, searchFilterString));

            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", HnppConstants.TABLE_NAME.FAMILY_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID, searchFilterString));

        }
        if(!StringUtils.isEmpty(selectedStartDateFilterValue)&&!StringUtils.isEmpty(selectedEndDateFilterValue)){
            customFilter.append(MessageFormat.format(" and {0} between {1} and {2} ",HnppConstants.TABLE_NAME.CHILD+"."+ HnppConstants.KEY.DUE_VACCINE_DATE,selectedStartDateFilterValue,selectedEndDateFilterValue));
        }else if(!StringUtils.isEmpty(selectedStartDateFilterValue)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}'' ", HnppConstants.TABLE_NAME.CHILD, HnppConstants.KEY.DUE_VACCINE_DATE, selectedStartDateFilterValue));

        }else if(!StringUtils.isEmpty(selectedEndDateFilterValue)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.CHILD, HnppConstants.KEY.DUE_VACCINE_DATE, selectedEndDateFilterValue));
        }else if(!StringUtils.isEmpty(isAefiChild)){
            customFilter.append(MessageFormat.format(" and {0}.{1} = ''{2}''  ", HnppConstants.TABLE_NAME.CHILD, HnppConstants.KEY.HAS_AEFI, "yes"));
        }
        if(month!=-1){
            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+HnppConstants.addZeroForMonth(month+"")+"'"));
            customFilter.append(MessageFormat.format(" and {0} = {1} ", "strftime('%Y', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+year+"'"));
            if(!visitType.isEmpty()){
                customFilter.append(visitType);
            }
            customFilter.append(" group by ec_family_member.base_entity_id");
        }
        String query = "";
        try {
            sqb.addCondition(customFilter.toString());
            query = sqb.orderbyCondition(Sortqueries);
            query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));

        } catch (Exception e) {
            Timber.e(e);
        }
        Log.v("CHILD_FILTER","filter:"+query);

        return query;
    }
    @Override
    protected String getMainCondition() {
        return presenter().getMainCondition();
    }

    @Override
    protected String getDefaultSortQuery() {
        return presenter().getDefaultSortQuery();
    }

    @Override
    protected void startRegistration() {
        ((ChildRegisterActivity) getActivity()).startFormActivity(CoreConstants.JSON_FORM.getChildRegister(), null, null);
        //getActivity().startFormActivity(Utils.metadata().familyRegister.formName, null, null);
    }
    @Override
    protected void onViewClicked(View view) {
        if (getActivity() == null) {
            return;
        }

        if (view.getTag() != null && view.getTag(org.smartregister.chw.core.R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
            if (view.getTag() instanceof CommonPersonObjectClient) {
                goToChildDetailActivity((CommonPersonObjectClient) view.getTag(), false);
            }
        } else if (view.getId() == org.smartregister.chw.core.R.id.due_only_layout) {
            toggleFilterSelection(view);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbar = view.findViewById(org.smartregister.chw.core.R.id.register_toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);
        NavigationMenu.getInstance(getActivity(), null, toolbar);
    }
    @Override
    public void showNotFoundPopup(String uniqueId) {
        if (getActivity() == null) {
            return;
        }
        NoMatchDialogFragment.launchDialog((BaseRegisterActivity) getActivity(), DIALOG_TAG, uniqueId);
    }
}

enum DatePickerType{
    FROM,
    TO
}

enum DeviceType{
    MOBILE,
    TABLET
}
