package org.smartregister.unicef.dghs.provider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jeasy.rules.api.Rules;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.chw.core.model.ChildVisit;
import org.smartregister.chw.core.provider.ChwAncRegisterProvider;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewProvider;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.provider.FamilyDueRegisterProvider;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.text.MessageFormat;
import java.util.Set;

import static org.smartregister.family.util.Utils.getName;

/**
 * Created by keyman on 14/01/2019.
 */

public class HnppFamilyDueRegisterProvider implements RecyclerViewProvider<HnppFamilyDueRegisterProvider.DueViewHolder> {

    private final LayoutInflater inflater;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    private View.OnClickListener onClickListener;
    private View.OnClickListener paginationClickListener;

    private Context context;
    private CommonRepository commonRepository;

    public HnppFamilyDueRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.visibleColumns = visibleColumns;

        this.onClickListener = onClickListener;
        this.paginationClickListener = paginationClickListener;

        this.context = context;
        this.commonRepository = commonRepository;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, DueViewHolder viewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        populatePatientColumn(pc, client, viewHolder);
        // populateIdentifierColumn(pc, viewHolder);

        viewHolder.status.setVisibility(View.INVISIBLE);
       // Utils.startAsyncTask(new UpdateAsyncTask(viewHolder, pc), null);
    }

    @Override
    public void getFooterView(RecyclerView.ViewHolder viewHolder, int currentPageCount, int totalPageCount, boolean hasNext, boolean hasPrevious) {
        FooterViewHolder footerViewHolder = (FooterViewHolder) viewHolder;
        footerViewHolder.pageInfoView.setText(
                MessageFormat.format(context.getString(org.smartregister.R.string.str_page_info), currentPageCount,
                        totalPageCount));

        footerViewHolder.nextPageView.setVisibility(hasNext ? View.VISIBLE : View.INVISIBLE);
        footerViewHolder.previousPageView.setVisibility(hasPrevious ? View.VISIBLE : View.INVISIBLE);

        footerViewHolder.nextPageView.setOnClickListener(paginationClickListener);
        footerViewHolder.previousPageView.setOnClickListener(paginationClickListener);
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, final DueViewHolder viewHolder) {

        String firstName = org.smartregister.chw.core.utils.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String middleName = org.smartregister.chw.core.utils.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
        String lastName = org.smartregister.chw.core.utils.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);

        String patientName = org.smartregister.family.util.Utils.getName(firstName, middleName, lastName);

        String dob = org.smartregister.chw.core.utils.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
        String dobString = Utils.getDuration(dob);
        String yearSub =  dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : "0";
        if(!TextUtils.isEmpty(yearSub) && Integer.parseInt(yearSub) >=3){
            viewHolder.dueBtn.setText("শিশু ফলোআপ");
        }else{
            viewHolder.dueBtn.setText("ভ্যাকসিনেশন");
        }
        fillValue(viewHolder.patientNameAge, patientName);
        viewHolder.lastVisit.setVisibility(View.VISIBLE);

        viewHolder.lastVisit.setText(context.getString(R.string.age,dobString) );
        // Update UI cutoffs
        viewHolder.patientNameAge.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen.member_due_list_title_size));

        viewHolder.nextArrow.setVisibility(View.GONE);
        viewHolder.dueButtonLayout.setVisibility(View.VISIBLE);


//        String lastVisit = org.smartregister.chw.core.utils.Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.LAST_HOME_VISIT, false);
//        if (StringUtils.isNotBlank(lastVisit)) {
//            // String lastVisitString = Utils.actualDuration(context, Utils.getDuration(lastVisit));
//            String lastVisitString = org.smartregister.chw.core.utils.Utils.actualDaysBetweenDateAndNow(context, lastVisit);
//            viewHolder.lastVisit.setText(String.format(context.getString(R.string.last_visit_prefix), lastVisitString));
//            viewHolder.lastVisit.setVisibility(View.VISIBLE);
//        } else {
//            viewHolder.lastVisit.setVisibility(View.INVISIBLE);
//        }
        viewHolder.dueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.nextArrow.performClick();
            }
        });
        viewHolder.nextArrowColumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.nextArrow.performClick();
            }
        });

        viewHolder.statusColumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.patientColumn.performClick();
            }
        });

        attachPatientOnclickListener(viewHolder.patientColumn, client);

        attachNextArrowOnclickListener(viewHolder.nextArrow, client);
    }

    private void attachPatientOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseFamilyProfileMemberFragment.CLICK_VIEW_NORMAL);
    }

    private void attachNextArrowOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseFamilyProfileMemberFragment.CLICK_VIEW_NEXT_ARROW);
    }

    private void updateDueColumn(DueViewHolder viewHolder, ChildVisit childVisit) {
        if (childVisit != null) {
            viewHolder.status.setVisibility(View.VISIBLE);
            if (childVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.DUE.name())) {
                viewHolder.status.setImageResource(org.smartregister.chw.core.utils.Utils.getDueProfileImageResourceIDentifier());
            } else if (childVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.OVERDUE.name())) {
                viewHolder.status.setImageResource(org.smartregister.chw.core.utils.Utils.getOverDueProfileImageResourceIDentifier());
            } else {
                viewHolder.status.setVisibility(View.INVISIBLE);
            }
        } else {
            viewHolder.status.setVisibility(View.INVISIBLE);
        }
    }

//    private ChildVisit retrieveChildVisitList(Rules rules, CommonPersonObjectClient pc) {
//        String dobString = org.smartregister.chw.core.utils.Utils.getDuration(org.smartregister.chw.core.utils.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false));
//        String lastVisitDate = org.smartregister.chw.core.utils.Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.LAST_HOME_VISIT, false);
//        String visitNotDone = org.smartregister.chw.core.utils.Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.VISIT_NOT_DONE, false);
//        String strDateCreated = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.DATE_CREATED, false);
//        long lastVisit = 0, visitNot = 0, dateCreated = 0;
//        if (!TextUtils.isEmpty(lastVisitDate)) {
//            lastVisit = Long.valueOf(lastVisitDate);
//        }
//        if (!TextUtils.isEmpty(visitNotDone)) {
//            visitNot = Long.valueOf(visitNotDone);
//        }
//        if (!TextUtils.isEmpty(strDateCreated)) {
//            dateCreated = org.smartregister.family.util.Utils.dobStringToDateTime(strDateCreated).getMillis();
//        }
//        return CoreChildUtils.getChildVisitStatus(context, rules, dobString, lastVisit, visitNot, dateCreated);
//    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption, FilterOption searchFilter, SortOption sortOption) {
        return null;
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {//Implement Abstract Method
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    @Override
    public LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public DueViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.view_member_due, parent, false);

        /*
        ConfigurableViewsHelper helper = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper();
        if (helper.isJsonViewsEnabled()) {

            ViewConfiguration viewConfiguration = helper.getViewConfiguration(Constants.CONFIGURATION.HOME_REGISTER_ROW);
            ViewConfiguration commonConfiguration = helper.getViewConfiguration(COMMON_REGISTER_ROW);

            if (viewConfiguration != null) {
                return helper.inflateDynamicView(viewConfiguration, commonConfiguration, view, R.id.register_columns, false);
            }
        }*/

        return new DueViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder createFooterHolder(ViewGroup parent) {
        View view = inflater.inflate(org.smartregister.family.R.layout.smart_register_pagination, parent, false);
        if (Utils.metadata().familyDueRegister.showPagination) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
        return new FooterViewHolder(view);
    }

    @Override
    public boolean isFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        return FooterViewHolder.class.isInstance(viewHolder);
    }


    public static void fillValue(TextView v, String value) {
        if (v != null)
            v.setText(value);

    }
//    private class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {
//        private final DueViewHolder viewHolder;
//        private final CommonPersonObjectClient pc;
//
//        private final Rules rules;
//
//        private ChildVisit childVisit;
//
//        private UpdateAsyncTask(DueViewHolder viewHolder, CommonPersonObjectClient pc) {
//            this.viewHolder = viewHolder;
//            this.pc = pc;
//            this.rules = HnppApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.HOME_VISIT);
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            childVisit = retrieveChildVisitList(rules, pc);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void param) {
//            // Update status column
//            updateDueColumn(viewHolder, childVisit);
//        }
//    }
    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////

    public class DueViewHolder extends RecyclerView.ViewHolder {
        public ImageView status;
        public CustomFontTextView patientNameAge;
        public TextView lastVisit;
        public ImageView nextArrow;

        public View patientColumn;
        public View nextArrowColumn;
        public View statusColumn;
        public View registerColumns;
        public View lineView;
        public LinearLayout dueButtonLayout;
        public Button dueBtn;
        public DueViewHolder(View itemView) {
            super(itemView);

            status = itemView.findViewById(org.smartregister.family.R.id.status);

            patientNameAge = itemView.findViewById(org.smartregister.family.R.id.patient_name_age);
            lastVisit = itemView.findViewById(org.smartregister.family.R.id.last_visit);
            dueButtonLayout = itemView.findViewById(org.smartregister.family.R.id.due_button_wrapper);
            dueBtn = itemView.findViewById(org.smartregister.family.R.id.due_button);

            nextArrow = itemView.findViewById(org.smartregister.family.R.id.next_arrow);
            lineView = itemView.findViewById(R.id.line_view);
            patientColumn = itemView.findViewById(org.smartregister.family.R.id.patient_column);
            nextArrowColumn = itemView.findViewById(org.smartregister.family.R.id.next_arrow_column);
            statusColumn = itemView.findViewById(org.smartregister.family.R.id.status_layout);
            registerColumns = itemView.findViewById(org.smartregister.family.R.id.register_columns);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public TextView pageInfoView;
        public Button nextPageView;
        public Button previousPageView;

        public FooterViewHolder(View view) {
            super(view);

            nextPageView = view.findViewById(org.smartregister.R.id.btn_next_page);
            previousPageView = view.findViewById(org.smartregister.R.id.btn_previous_page);
            pageInfoView = view.findViewById(org.smartregister.R.id.txt_page_info);
        }
    }

}
