package org.smartregister.unicef.mis.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.DateTime;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.ServiceSchedule;
import org.smartregister.immunization.domain.ServiceType;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.fragment.ServiceDialogFragment;
import org.smartregister.immunization.fragment.UndoServiceDialogFragment;
import org.smartregister.immunization.fragment.UndoVaccinationDialogFragment;
import org.smartregister.immunization.fragment.VaccinationDialogFragment;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.RecurringServiceUtils;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.immunization.view.ServiceGroup;
import org.smartregister.immunization.view.VaccineGroup;
import org.smartregister.service.AlertService;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.QRScannerActivity;
import org.smartregister.unicef.mis.contract.HPVImmunizationContract;
import org.smartregister.unicef.mis.interactor.HPVImmunizationInteractor;
import org.smartregister.unicef.mis.location.HALocation;
import org.smartregister.unicef.mis.location.HPVLocation;
import org.smartregister.unicef.mis.model.GlobalLocationModel;
import org.smartregister.unicef.mis.utils.FormApplicability;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppDBUtils;
import org.smartregister.unicef.mis.utils.OtherVaccineContentData;
import org.smartregister.view.fragment.BaseProfileFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static org.smartregister.util.Utils.getName;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WomanImmunizationFragment extends BaseProfileFragment implements HPVImmunizationContract.InteractorCallBack {
    private CommonPersonObjectClient childDetails;
    private static final String TAG = WomanImmunizationFragment.class.getCanonicalName();
    private static final String DIALOG_TAG = "DIALOG_TAAAGGG";
    private static final String EXTRA_CHILD_DETAILS = "child_details";

    private ArrayList<VaccineGroup> vaccineGroups;
    private ArrayList<ServiceGroup> serviceGroups;

    private static final ArrayList<String> COMBINED_VACCINES;
    private static final HashMap<String, String> COMBINED_VACCINES_MAP;

    private static final boolean isChildActive = true;

    //    ChildWomanImmunizationFragment cia;
    LinearLayout vaccine_group_canvas_ll,service_group_canvas_ll,hpvPanel;
    Button hpvEnrollmentBtn,hpvVaccineGivenBtn;
    Activity mActivity;
    TextView tdPanel;
    CardView tdCardView;
    static {
        COMBINED_VACCINES = new ArrayList<>();
        COMBINED_VACCINES_MAP = new HashMap<>();
        COMBINED_VACCINES.add("Measles 1");
        COMBINED_VACCINES_MAP.put("Measles 1", "Measles 1 / MR 1");
        COMBINED_VACCINES.add("MR 1");
        COMBINED_VACCINES_MAP.put("MR 1", "Measles 1 / MR 1");
        COMBINED_VACCINES.add("Measles 2");
        COMBINED_VACCINES_MAP.put("Measles 2", "Measles 2 / MR 2");
        COMBINED_VACCINES.add("MR 2");
        COMBINED_VACCINES_MAP.put("MR 2", "Measles 2 / MR 2");
    }
    public void setChildDetails(CommonPersonObjectClient childDetails){
        this.childDetails = childDetails;

    }
    public static WomanImmunizationFragment newInstance(Bundle bundle) {
        Bundle args = bundle;

        WomanImmunizationFragment fragment = new WomanImmunizationFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<org.smartregister.immunization.domain.jsonmapping.VaccineGroup> womanVaccines = VaccinatorUtils.getSupportedWomanVaccines(mActivity);
        VaccineSchedule.init(womanVaccines, null, "woman");
    }

    @Override
    protected void onCreation() {
        //Overriden
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) getActivity();
    }
    @Override
    protected void onResumption() {
        //Overriden
        if (vaccineGroups != null) {
            vaccine_group_canvas_ll.removeAllViews();
            vaccineGroups = null;
        }

        if (serviceGroups != null) {
            service_group_canvas_ll.removeAllViews();
            serviceGroups = null;
        }

        updateViews();

        startServices();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.immunization_activity_main, container, false);
        fragmentView.findViewById(R.id.showTikaCardBtn).setVisibility(View.GONE);
        tdPanel = fragmentView.findViewById(R.id.tdTxt);
        tdCardView = fragmentView.findViewById(R.id.tdCard);
        tdPanel.setText(getString(R.string.td_immunization_records));
        vaccine_group_canvas_ll = fragmentView.findViewById(R.id.vaccine_group_canvas_ll);
        service_group_canvas_ll = fragmentView.findViewById(R.id.service_group_canvas_ll);
//        cia = new WomanImmunizationFragment(fragmentView,getActivity());
        hpvEnrollmentBtn = fragmentView.findViewById(R.id.hpvEnrollBtn);
        hpvPanel = fragmentView.findViewById(R.id.hpv_panel);
        hpvVaccineGivenBtn = fragmentView.findViewById(R.id.hpvVaccineBtn);
        hpvEnrollmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(HnppConstants.isConnectedToInternet(getActivity())){
                    enrollment();
                }else{
                    Toast.makeText(getActivity(),getString(R.string.no_internet_connectivity),Toast.LENGTH_LONG).show();
                }

            }
        });
        hpvVaccineGivenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendHPVVaccine();
            }
        });
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int age = FormApplicability.getAge(childDetails);
        Log.v(TAG,"onViewCreated>>>age:"+age);
        if(age>=10 && age <=15){
            updateHPVPanel();
            tdPanel.setVisibility(View.GONE);
            tdCardView.setVisibility(View.GONE);
        }
        if(age>=15){
            tdPanel.setVisibility(View.VISIBLE);
            tdCardView.setVisibility(View.VISIBLE);
        }

    }
    ArrayList<HPVLocation> centerList = new ArrayList<>();
    HPVImmunizationInteractor interactor;
    private void updateHPVPanel(){
        interactor = new HPVImmunizationInteractor();
        interactor.fetchDataFromOffline(childDetails.entityId(), this);


    }
    @Override
    public void onUpdateList(ArrayList<HPVLocation> list) {
        centerList = list;

    }

    @Override
    public void enrolSuccessfully(String message) {
        hideProgressDialog();
        Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();
        hpvEnrollmentBtn.setVisibility(View.GONE);
        hpvVaccineGivenBtn.setVisibility(View.VISIBLE);

    }

    @Override
    public void enrolFail(String message) {
        hideProgressDialog();
        Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateOtherVaccine(OtherVaccineContentData otherVaccineContentData) {
        if(otherVaccineContentData==null){
            hpvPanel.setVisibility(View.VISIBLE);
            hpvEnrollmentBtn.setVisibility(View.VISIBLE);
            hpvVaccineGivenBtn.setVisibility(View.GONE);
            interactor.fetchCenterList(childDetails.entityId(),this);
        }else{
            hpvPanel.setVisibility(View.VISIBLE);
            hpvEnrollmentBtn.setVisibility(View.GONE);
            hpvVaccineGivenBtn.setVisibility(View.VISIBLE);
            if(otherVaccineContentData.vaccineDate!=null){
                hpvVaccineGivenBtn.setText("✓ HPV given date:"+otherVaccineContentData.vaccineDate);
                hpvVaccineGivenBtn.setEnabled(false);
            }

        }

    }

    @Override
    public void onUpdateFromOnline() {
        if(HnppConstants.isConnectedToInternet(getActivity())){
            interactor.fetchOtherVaccineData(childDetails.entityId(), this);
        }else{
            Toast.makeText(getActivity(),getString(R.string.no_internet_connectivity),Toast.LENGTH_LONG).show();
        }
    }

    HPVLocation selectedHpvLocation = null;
    private void enrollment(){
        String blockId =  HnppDBUtils.getBlocksIdFromMember(childDetails.entityId());
        HALocation selectedLocation = HnppApplication.getHALocationRepository().getLocationByBlock(blockId);
        StringBuilder builder = new StringBuilder();
        if(selectedLocation!=null){
            builder.append(this.getString(R.string.division)+" : "+selectedLocation.division.name+"("+selectedLocation.division.code+")");
            builder.append("\n");
            builder.append(this.getString(R.string.district)+" : "+selectedLocation.district.name+"("+selectedLocation.district.code+")");
            builder.append("\n");
            builder.append(this.getString(R.string.upazila)+" : "+selectedLocation.upazila.name+"("+selectedLocation.upazila.code+")");
            builder.append("\n");
            builder.append(this.getString(R.string.paurosova)+" : "+selectedLocation.paurasava.name+"("+selectedLocation.paurasava.code+")");
            builder.append("\n");
            builder.append(this.getString(R.string.union_zone)+" : "+selectedLocation.union.name+"("+selectedLocation.union.code+")");
            builder.append("\n");
            builder.append(this.getString(R.string.old_ward)+" : "+selectedLocation.old_ward.name+"("+selectedLocation.old_ward.code+")");
            builder.append("\n");
            builder.append(this.getString(R.string.new_ward)+" : "+selectedLocation.ward.name+"("+selectedLocation.ward.code+")");
            builder.append("\n");

        }
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_hpv_enrolment);
        TextView textViewTitle = dialog.findViewById(R.id.text_tv);
        Spinner centerSpinner = dialog.findViewById(R.id.block_spinner);
        textViewTitle.setText(builder.toString());
        ArrayAdapter<HPVLocation> centerAdapter =  new ArrayAdapter<HPVLocation>(getActivity(), android.R.layout.simple_spinner_item, centerList);
        centerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        centerSpinner.setAdapter(centerAdapter);
        centerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position == -1) return;
                selectedHpvLocation = centerList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showProgressDialog("Enrollment kora hocce");
                if(HnppConstants.isConnectedToInternet(getActivity()) && selectedHpvLocation!=null){
                    interactor.postEnrolmentData(childDetails.entityId(),selectedHpvLocation,WomanImmunizationFragment.this);
                }else{
                   //
                }
            }
        });
        dialog.show();
    }

    private void sendHPVVaccine() {
        OtherVaccineContentData content = HnppDBUtils.getMemberInfo(childDetails.entityId());
        if(content==null) return;
        String date = HnppConstants.YYMMDD.format(System.currentTimeMillis());
        content.date = date;
        String buttonName= getString(R.string.other_vaccine_button,content.vaccine_name);
        StringBuilder builder = new StringBuilder();
        String name = content.firstName;//+" "+content.lastName;
        builder.append(this.getString(R.string.name,name)+"\n");
        builder.append(this.getString(R.string.father_name,content.fatherNameEn==null?"":content.fatherNameEn)+"\n");
        builder.append(this.getString(R.string.mother_name,content.mothernameEn==null?"":content.mothernameEn)+"\n");
        builder.append(this.getString(R.string.dob, content.dob)+"\n");
        builder.append(this.getString(R.string.bid,content.brn));
        if(TextUtils.isEmpty(content.brn)){
            Toast.makeText(getActivity(),"BRN not found",Toast.LENGTH_LONG).show();
            return;
        }
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_qrscan);
        TextView textViewTitle = dialog.findViewById(R.id.text_tv);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        titleTxt.setText(buttonName);
        TextView vaccineDateTxt = dialog.findViewById(R.id.vaccine_date_tv);
        vaccineDateTxt.setText(content.date);
        dialog.findViewById(R.id.date_picker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(vaccineDateTxt);
            }
        });
        textViewTitle.setText(builder.toString());
        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                content.date = vaccineDateTxt.getText().toString();
                if(HnppConstants.isConnectedToInternet(getActivity())){
                    sendOtherVaccineInfo(content);
                }else{
                    saveOtherVaccineInfo(content);
                }
            }
        });
        dialog.show();
    }
    private void sendOtherVaccineInfo(OtherVaccineContentData contentData){
        showProgressDialog("saving....");
        HnppConstants.sendOtherVaccineSingleData(contentData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(String s) {
                        Log.v("OTHER_VACCINE","onNext>>s:"+s);
                        try{
                            hideProgressDialog();
                        }catch (Exception e){

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("OTHER_VACCINE",""+e);
                        try{
                            hideProgressDialog();
                        }catch (Exception e1){

                        }
                    }

                    @Override
                    public void onComplete() {
                        Log.v("OTHER_VACCINE","completed");
                        try{
                            hideProgressDialog();
                        }catch (Exception e){

                        }
                        try{
                            HnppConstants.showOneButtonDialog(getActivity(), "টিকা প্রদানের তথ্য সফল ভাবে হালনাগাদ করা হয়েছে", "", new Runnable() {
                                @Override
                                public void run() {
                                   hpvVaccineGivenBtn.setEnabled(false);
                                    interactor.fetchOtherVaccineData(childDetails.entityId(), WomanImmunizationFragment.this);
                                }
                            });
                        }catch (Exception e){

                        }

                    }
                });
        //OtherVaccineJob.scheduleJobImmediately(OtherVaccineJob.TAG);

    }
    private void saveOtherVaccineInfo(OtherVaccineContentData contentData){
        showProgressDialog("saving....");
        HnppConstants.saveOtherVaccineData(contentData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(String s) {
                        Log.v("OTHER_VACCINE","onNext>>s:"+s);
                        try{
                            hideProgressDialog();
                        }catch (Exception e){

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("OTHER_VACCINE",""+e);
                        try{
                            hideProgressDialog();
                        }catch (Exception e1){

                        }
                    }

                    @Override
                    public void onComplete() {
                        Log.v("OTHER_VACCINE","completed");
                        try{
                            hideProgressDialog();
                        }catch (Exception e){

                        }
                        try{
                            HnppConstants.showOneButtonDialog(getActivity(), "টিকা প্রদানের তথ্য সফল ভাবে হালনাগাদ করা হয়েছে", "", new Runnable() {
                                @Override
                                public void run() {
                                    hpvVaccineGivenBtn.setEnabled(false);
                                }
                            });
                        }catch (Exception e){

                        }

                    }
                });
        //OtherVaccineJob.scheduleJobImmediately(OtherVaccineJob.TAG);

    }
    private ProgressDialog dialog;

    private void showProgressDialog(String text) {
        if (dialog == null) {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage(text);
            dialog.setCancelable(false);
            dialog.show();
        }

    }

    private void hideProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
    private void showDatePicker(TextView vaccineDateTxt) {
        String[] yyMMdd = vaccineDateTxt.getText().toString().split("-");
        DatePickerDialog fromDialog = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                String fromDate = yr + "-" + HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+HnppConstants.addZeroForMonth(dayOfMonth+"");
                vaccineDateTxt.setText(fromDate);
            }
        },Integer.parseInt(yyMMdd[0]),Integer.parseInt(yyMMdd[1]),Integer.parseInt(yyMMdd[2]));
        fromDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        fromDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 259200000L);
        fromDialog.show();
    }

    private boolean isDataOk() {
        return childDetails != null && childDetails.getDetails() != null;
    }

    public void updateViews() {

        VaccineRepository vaccineRepository = ImmunizationLibrary.getInstance().vaccineRepository();
        AlertService alertService = ImmunizationLibrary.getInstance().context().alertService();

        UpdateViewTask updateViewTask = new UpdateViewTask();
        updateViewTask.setVaccineRepository(vaccineRepository);
        updateViewTask.setAlertService(alertService);
        org.smartregister.util.Utils.startAsyncTask(updateViewTask, null);
    }

    long timeDiff = 0l;

    private void updateVaccinationViews(List<Vaccine> vaccineList, List<Alert> alerts) {

        if (vaccineGroups == null) {
            vaccineGroups = new ArrayList<>();
            List<org.smartregister.immunization.domain.jsonmapping.VaccineGroup> supportedVaccines =
                    VaccinatorUtils.getSupportedWomanVaccines(mActivity);
            final long fifteen_years = 15 * 365 * 24 * 60 * 60 * 1000l;
            final long fiftifive_years = 55 * 365 * 24 * 60 * 60 * 1000l;
            for (Alert alert : alerts) {
                String alert_name = alert.scheduleName();
                for (Vaccine vaccine : vaccineList) {
                    String vaccine_name = vaccine.getName();

                    if (!alert_name.equalsIgnoreCase(vaccine_name)) {
                        try {

                            if (!(timeDiff >= fifteen_years && timeDiff <= fiftifive_years)) {
                                Alert o = new Alert(alert.caseId(), alert.scheduleName(), alert.visitCode(), AlertStatus.upcoming, alert.startDate(), alert.expiryDate());
                                alerts.set(alerts.indexOf(alert), o);
                            }
                        } catch (Exception e) {

                        }
                    }
                }
                try {

                    if (!(timeDiff >= fifteen_years && timeDiff <= fiftifive_years)) {
                        Alert o = new Alert(alert.caseId(), alert.scheduleName(), alert.visitCode(), AlertStatus.upcoming, alert.startDate(), alert.expiryDate());
                        alerts.set(alerts.indexOf(alert), o);
                    }
                } catch (Exception e) {

                }
            }
            for (org.smartregister.immunization.domain.jsonmapping.VaccineGroup vaccineGroupObject : supportedVaccines) {
                //Add BCG2 special vaccine to birth vaccine group
                VaccinateActionUtils.addBcg2SpecialVaccine(mActivity, vaccineGroupObject, vaccineList);

                addVaccineGroup(-1, vaccineGroupObject, vaccineList, alerts);
            }
        }
    }


    private void addVaccineGroup(int canvasId, org.smartregister.immunization.domain.jsonmapping.VaccineGroup vaccineGroupData, List<Vaccine> vaccineList, List<Alert> alerts) {
        VaccineGroup curGroup = new VaccineGroup(mActivity);
        curGroup.setChildActive(isChildActive);
        curGroup.setData(vaccineGroupData, childDetails, vaccineList, alerts, "woman");
        curGroup.setOnRecordAllClickListener(new VaccineGroup.OnRecordAllClickListener() {
            @Override
            public void onClick(VaccineGroup vaccineGroup, ArrayList<VaccineWrapper> dueVaccines) {
                addVaccinationDialogFragment(dueVaccines, vaccineGroup);
            }
        });
        curGroup.setOnVaccineClickedListener(new VaccineGroup.OnVaccineClickedListener() {
            @Override
            public void onClick(VaccineGroup vaccineGroup, VaccineWrapper vaccine) {
                ArrayList<VaccineWrapper> vaccineWrappers = new ArrayList<>();
                vaccineWrappers.add(vaccine);
                addVaccinationDialogFragment(vaccineWrappers, vaccineGroup);
            }
        });
        curGroup.setOnVaccineUndoClickListener(new VaccineGroup.OnVaccineUndoClickListener() {
            @Override
            public void onUndoClick(VaccineGroup vaccineGroup, VaccineWrapper vaccine) {
                addVaccineUndoDialogFragment(vaccineGroup, vaccine);
            }
        });

        LinearLayout parent;
        if (canvasId == -1) {
            Random r = new Random();
            canvasId = r.nextInt(4232 - 213) + 213;
            parent = new LinearLayout(mActivity);
            parent.setId(canvasId);
            vaccine_group_canvas_ll.addView(parent);
        } else {
            parent = (LinearLayout) vaccine_group_canvas_ll.findViewById(canvasId);
            parent.removeAllViews();
        }
        parent.addView(curGroup);
        curGroup.setTag(R.id.vaccine_group_vaccine_data, vaccineGroupData);
        curGroup.setTag(R.id.vaccine_group_parent_id, String.valueOf(canvasId));
        vaccineGroups.add(curGroup);
    }

    private void addVaccineUndoDialogFragment(VaccineGroup vaccineGroup, VaccineWrapper vaccineWrapper) {
//        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
//        Fragment prev = getActivity().getFragmentManager().findFragmentByTag(DIALOG_TAG);
//        if (prev != null) {
//            ft.remove(prev);
//        }
//
//        ft.addToBackStack(null);
        vaccineGroup.setModalOpen(true);

        UndoVaccinationDialogFragment undoVaccinationDialogFragment = UndoVaccinationDialogFragment.newInstance(vaccineWrapper);
        undoVaccinationDialogFragment.show(getChildFragmentManager(), DIALOG_TAG);
    }

    private void addServiceUndoDialogFragment(ServiceGroup serviceGroup, ServiceWrapper serviceWrapper) {
//        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
//        Fragment prev = getActivity().getFragmentManager().findFragmentByTag(DIALOG_TAG);
//        if (prev != null) {
//            ft.remove(prev);
//        }
//
//        ft.addToBackStack(null);
        serviceGroup.setModalOpen(true);

        UndoServiceDialogFragment undoServiceDialogFragment = UndoServiceDialogFragment.newInstance(serviceWrapper);
        undoServiceDialogFragment.show(getChildFragmentManager(), DIALOG_TAG);
    }

//    public void launchDetailActivity(Context fromContext, CommonPersonObjectClient childDetails) {
//        Intent intent = new Intent(fromContext, DetailActivity.class);
//        Bundle bundle = new Bundle();
//
//        bundle.putSerializable(EXTRA_CHILD_DETAILS, childDetails);
//        intent.putExtras(bundle);
//
//        fromContext.startActivity(intent);
//    }


    public void onVaccinateToday(ArrayList<VaccineWrapper> tags, View v) {
        if (tags != null && !tags.isEmpty()) {
            View view = getLastOpenedView();
            saveVaccine(tags, view);
        }
    }


    public void onVaccinateEarlier(ArrayList<VaccineWrapper> tags, View v) {
        if (tags != null && !tags.isEmpty()) {
            View view = getLastOpenedView();
            saveVaccine(tags, view);
        }
    }


    public void onUndoVaccination(VaccineWrapper tag, View v) {
        org.smartregister.util.Utils.startAsyncTask(new UndoVaccineTask(tag, v), null);
    }

    public void addVaccinationDialogFragment(ArrayList<VaccineWrapper> vaccineWrappers, VaccineGroup vaccineGroup) {

//        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
//        Fragment prev = getActivity().getFragmentManager().findFragmentByTag(DIALOG_TAG);
//        if (prev != null) {
//            ft.remove(prev);
//        }
//
//        ft.addToBackStack(null);
        vaccineGroup.setModalOpen(true);
        String dobString = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "dob", false);

        Date dob = Calendar.getInstance().getTime();
        if (!TextUtils.isEmpty(dobString)) {
            DateTime dateTime = new DateTime(dobString);
            dob = dateTime.toDate();
        }

        List<Vaccine> vaccineList = ImmunizationLibrary.getInstance().vaccineRepository()
                .findByEntityId(childDetails.entityId());
        if (vaccineList == null) vaccineList = new ArrayList<>();

        VaccinationDialogFragment vaccinationDialogFragment = VaccinationDialogFragment.newInstance(dob, vaccineList, vaccineWrappers, true);
        vaccinationDialogFragment.show(getChildFragmentManager(), DIALOG_TAG);
    }

    public void addServiceDialogFragment(ServiceWrapper serviceWrapper, ServiceGroup serviceGroup) {

//        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
//        Fragment prev = getActivity().getFragmentManager().findFragmentByTag(DIALOG_TAG);
//        if (prev != null) {
//            ft.remove(prev);
//        }
//
//        ft.addToBackStack(null);
        serviceGroup.setModalOpen(true);
        String dobString = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "dob", false);
        DateTime dob = DateTime.now();
        if (!TextUtils.isEmpty(dobString)) {
            dob = new DateTime(dobString);
        }

        List<ServiceRecord> serviceRecordList = ImmunizationLibrary.getInstance().recurringServiceRecordRepository()
                .findByEntityId(childDetails.entityId());

        ServiceDialogFragment serviceDialogFragment = ServiceDialogFragment.newInstance(dob, serviceRecordList, serviceWrapper, true);
        serviceDialogFragment.show(getChildFragmentManager(), DIALOG_TAG);
    }

    private void saveVaccine(ArrayList<VaccineWrapper> tags, final View view) {
        if (tags.isEmpty()) {
            return;
        }

        VaccineRepository vaccineRepository = ImmunizationLibrary.getInstance().vaccineRepository();

        VaccineWrapper[] arrayTags = tags.toArray(new VaccineWrapper[tags.size()]);
        SaveVaccinesTask backgroundTask = new SaveVaccinesTask();
        backgroundTask.setVaccineRepository(vaccineRepository);
        backgroundTask.setView(view);
        org.smartregister.util.Utils.startAsyncTask(backgroundTask, arrayTags);

    }

    private void saveVaccine(VaccineRepository vaccineRepository, VaccineWrapper tag) {
        if (tag.getUpdatedVaccineDate() == null) {
            return;
        }


        Vaccine vaccine = new Vaccine();
        if (tag.getDbKey() != null) {
            vaccine = vaccineRepository.find(tag.getDbKey());
        }
        vaccine.setBaseEntityId(childDetails.entityId());
        vaccine.setName(tag.getName());
        vaccine.setDate(tag.getUpdatedVaccineDate().toDate());
        vaccine.setAnmId(ImmunizationLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM());

        String lastChar = vaccine.getName().substring(vaccine.getName().length() - 1);
        if (StringUtils.isNumeric(lastChar)) {
            vaccine.setCalculation(Integer.valueOf(lastChar));
        } else {
            vaccine.setCalculation(-1);
        }

        vaccine.setTeam("testTeam");
        vaccine.setTeamId("testTeamId");
        vaccine.setChildLocationId("testChildLocationId");
        vaccineRepository.add(vaccine);
        tag.setDbKey(vaccine.getId());
    }

    private void updateVaccineGroupViews(View view, final ArrayList<VaccineWrapper> wrappers, List<Vaccine> vaccineList) {
        updateVaccineGroupViews(view, wrappers, vaccineList, false);
    }

    private void updateVaccineGroupViews(View view, final ArrayList<VaccineWrapper> wrappers, final List<Vaccine> vaccineList, final boolean undo) {
        if (view == null || !(view instanceof VaccineGroup)) {
            return;
        }
        final VaccineGroup vaccineGroup = (VaccineGroup) view;
        vaccineGroup.setModalOpen(false);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (undo) {
                vaccineGroup.setVaccineList(vaccineList);
                vaccineGroup.updateWrapperStatus(wrappers, "woman");
            }
            vaccineGroup.updateViews(wrappers);

        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (undo) {
                        vaccineGroup.setVaccineList(vaccineList);
                        vaccineGroup.updateWrapperStatus(wrappers, "woman");
                    }
                    vaccineGroup.updateViews(wrappers);
                }
            });
        }
    }

    public void startServices() {
        VaccineRecurringServiceJob.scheduleJobImmediately(VaccineRecurringServiceJob.TAG);

    }

    @SuppressLint("StaticFieldLeak")
    private class SaveVaccinesTask extends AsyncTask<VaccineWrapper, Void, Pair<ArrayList<VaccineWrapper>, List<Vaccine>>> {

        private View view;
        private VaccineRepository vaccineRepository;
        private AlertService alertService;
        private List<String> affectedVaccines;
        private List<Vaccine> vaccineList;
        private List<Alert> alertList;

        public void setView(View view) {
            this.view = view;
        }

        public void setVaccineRepository(VaccineRepository vaccineRepository) {
            this.vaccineRepository = vaccineRepository;
            alertService = ImmunizationLibrary.getInstance().context().alertService();
            affectedVaccines = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(Pair<ArrayList<VaccineWrapper>, List<Vaccine>> pair) {
            updateVaccineGroupViews(view, pair.first, pair.second);

            updateVaccineGroupsUsingAlerts(affectedVaccines, vaccineList, alertList);
        }

        @Override
        protected Pair<ArrayList<VaccineWrapper>, List<Vaccine>> doInBackground(VaccineWrapper... vaccineWrappers) {

            ArrayList<VaccineWrapper> list = new ArrayList<>();
            if (vaccineRepository != null) {
                for (VaccineWrapper tag : vaccineWrappers) {
                    saveVaccine(vaccineRepository, tag);
                    list.add(tag);
                }
            }

            Pair<ArrayList<VaccineWrapper>, List<Vaccine>> pair = new Pair<>(list, vaccineList);
            String dobString = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "dob", false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                affectedVaccines = VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime, "woman");
            }
            vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());
            alertList = alertService.findByEntityIdAndAlertNames(childDetails.entityId(),
                    VaccinateActionUtils.allAlertNames("woman"));

            return pair;
        }
    }

    private String constructChildName() {
        String firstName = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "first_name", true);
        String lastName = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "last_name", true);
        return getName(firstName, lastName).trim();
    }


    private VaccineGroup getLastOpenedView() {
        if (vaccineGroups == null) {
            return null;
        }

        for (VaccineGroup vaccineGroup : vaccineGroups) {
            if (vaccineGroup.isModalOpen()) {
                return vaccineGroup;
            }
        }

        return null;
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateViewTask extends AsyncTask<Void, Void, Map<String, NamedObject<?>>> {

        private VaccineRepository vaccineRepository;
        private RecurringServiceTypeRepository recurringServiceTypeRepository;
        private RecurringServiceRecordRepository recurringServiceRecordRepository;
        private AlertService alertService;

        public void setVaccineRepository(VaccineRepository vaccineRepository) {
            this.vaccineRepository = vaccineRepository;
        }

        public void setRecurringServiceTypeRepository(RecurringServiceTypeRepository recurringServiceTypeRepository) {
            this.recurringServiceTypeRepository = recurringServiceTypeRepository;
        }

        public void setRecurringServiceRecordRepository(RecurringServiceRecordRepository recurringServiceRecordRepository) {
            this.recurringServiceRecordRepository = recurringServiceRecordRepository;
        }

        public void setAlertService(AlertService alertService) {
            this.alertService = alertService;
        }


        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Map<String, NamedObject<?>> map) {

            List<Vaccine> vaccineList = new ArrayList<>();

            Map<String, List<ServiceType>> serviceTypeMap = new LinkedHashMap<>();
            List<ServiceRecord> serviceRecords = new ArrayList<>();

            List<Alert> alertList = new ArrayList<>();

            if (map.containsKey(Vaccine.class.getName())) {
                NamedObject<?> namedObject = map.get(Vaccine.class.getName());
                if (namedObject != null) {
                    vaccineList = (List<Vaccine>) namedObject.object;
                }

            }

            if (map.containsKey(ServiceType.class.getName())) {
                NamedObject<?> namedObject = map.get(ServiceType.class.getName());
                if (namedObject != null) {
                    serviceTypeMap = (Map<String, List<ServiceType>>) namedObject.object;
                }

            }

            if (map.containsKey(ServiceRecord.class.getName())) {
                NamedObject<?> namedObject = map.get(ServiceRecord.class.getName());
                if (namedObject != null) {
                    serviceRecords = (List<ServiceRecord>) namedObject.object;
                }

            }

            if (map.containsKey(Alert.class.getName())) {
                NamedObject<?> namedObject = map.get(Alert.class.getName());
                if (namedObject != null) {
                    alertList = (List<Alert>) namedObject.object;
                }

            }

            //updateServiceViews(serviceTypeMap, serviceRecords, alertList);
            updateVaccinationViews(vaccineList, alertList);
        }

        @Override
        protected Map<String, NamedObject<?>> doInBackground(Void... voids) {
            String dobString = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "dob", false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime, "woman");
//                ServiceSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime);
            }

            List<Vaccine> vaccineList = new ArrayList<>();

            Map<String, List<ServiceType>> serviceTypeMap = new LinkedHashMap<>();
//            List<ServiceRecord> serviceRecords = new ArrayList<>();

            List<Alert> alertList = new ArrayList<>();
            if (vaccineRepository != null) {
                vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());

            }

//            if (recurringServiceRecordRepository != null) {
//                serviceRecords = recurringServiceRecordRepository.findByEntityId(childDetails.entityId());
//            }

//            if (recurringServiceTypeRepository != null) {
//                List<ServiceType> serviceTypes = recurringServiceTypeRepository.fetchAll();
//                for (ServiceType serviceType : serviceTypes) {
//                    String type = serviceType.getType();
//                    List<ServiceType> serviceTypeList = serviceTypeMap.get(type);
//                    if (serviceTypeList == null) {
//                        serviceTypeList = new ArrayList<>();
//                    }
//                    serviceTypeList.add(serviceType);
//                    serviceTypeMap.put(type, serviceTypeList);
//                }
//            }

            if (alertService != null) {
                alertList = alertService.findByEntityId(childDetails.entityId());
            }

            Map<String, NamedObject<?>> map = new HashMap<>();

            NamedObject<List<Vaccine>> vaccineNamedObject = new NamedObject<>(Vaccine.class.getName(), vaccineList);
            map.put(vaccineNamedObject.name, vaccineNamedObject);

//            NamedObject<Map<String, List<ServiceType>>> serviceTypeNamedObject = new NamedObject<>(ServiceType.class.getName(), serviceTypeMap);
//            map.put(serviceTypeNamedObject.name, serviceTypeNamedObject);
//
//            NamedObject<List<ServiceRecord>> serviceRecordNamedObject = new NamedObject<>(ServiceRecord.class.getName(), serviceRecords);
//            map.put(serviceRecordNamedObject.name, serviceRecordNamedObject);

            NamedObject<List<Alert>> alertsNamedObject = new NamedObject<>(Alert.class.getName(), alertList);
            map.put(alertsNamedObject.name, alertsNamedObject);

            return map;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UndoVaccineTask extends AsyncTask<Void, Void, Void> {

        private VaccineWrapper tag;
        private View v;
        private final VaccineRepository vaccineRepository;
        private final AlertService alertService;
        private List<Vaccine> vaccineList;
        private List<Alert> alertList;
        private List<String> affectedVaccines;

        public UndoVaccineTask(VaccineWrapper tag, View v) {
            this.tag = tag;
            this.v = v;
            vaccineRepository = ImmunizationLibrary.getInstance().vaccineRepository();
            alertService = ImmunizationLibrary.getInstance().context().alertService();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (tag != null) {

                if (tag.getDbKey() != null) {
                    Long dbKey = tag.getDbKey();
                    vaccineRepository.deleteVaccine(dbKey);
                    String dobString = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "dob", false);
                    if (!TextUtils.isEmpty(dobString)) {
                        DateTime dateTime = new DateTime(dobString);
                        affectedVaccines = VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime, "woman");
                        vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());
                        alertList = alertService.findByEntityIdAndAlertNames(childDetails.entityId(),
                                VaccinateActionUtils.allAlertNames("woman"));
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);

            // Refresh the vaccine group with the updated vaccine
            tag.setUpdatedVaccineDate(null, false);
            tag.setDbKey(null);

            View view = getLastOpenedView();

            ArrayList<VaccineWrapper> wrappers = new ArrayList<>();
            wrappers.add(tag);
            updateVaccineGroupViews(view, wrappers, vaccineList, true);
            updateVaccineGroupsUsingAlerts(affectedVaccines, vaccineList, alertList);
        }
    }

    private void updateVaccineGroupsUsingAlerts(List<String> affectedVaccines, List<Vaccine> vaccineList, List<Alert> alerts) {
        if (affectedVaccines != null && vaccineList != null) {
            // Update all other affected vaccine groups
            HashMap<VaccineGroup, ArrayList<VaccineWrapper>> affectedGroups = new HashMap<>();
            for (String curAffectedVaccineName : affectedVaccines) {
                boolean viewFound = false;
                // Check what group it is in
                for (VaccineGroup curGroup : vaccineGroups) {
                    ArrayList<VaccineWrapper> groupWrappers = curGroup.getAllVaccineWrappers();
                    if (groupWrappers == null) groupWrappers = new ArrayList<>();
                    for (VaccineWrapper curWrapper : groupWrappers) {
                        String curWrapperName = curWrapper.getName();

                        // Check if current wrapper is one of the combined vaccines
                        if (COMBINED_VACCINES.contains(curWrapperName)) {
                            // Check if any of the sister vaccines is currAffectedVaccineName
                            String[] allSisters = COMBINED_VACCINES_MAP.get(curWrapperName).split(" / ");
                            for (int i = 0; i < allSisters.length; i++) {
                                if (allSisters[i].replace(" ", "").equalsIgnoreCase(curAffectedVaccineName.replace(" ", ""))) {
                                    curWrapperName = allSisters[i];
                                    break;
                                }
                            }
                        }

                        if (curWrapperName.replace(" ", "").toLowerCase()
                                .contains(curAffectedVaccineName.replace(" ", "").toLowerCase())) {
                            if (!affectedGroups.containsKey(curGroup)) {
                                affectedGroups.put(curGroup, new ArrayList<VaccineWrapper>());
                            }

                            affectedGroups.get(curGroup).add(curWrapper);
                            viewFound = true;
                        }

                        if (viewFound) break;
                    }

                    if (viewFound) break;
                }
            }

            for (VaccineGroup curGroup : affectedGroups.keySet()) {
                try {
                    vaccineGroups.remove(curGroup);
                    addVaccineGroup(Integer.valueOf((String) curGroup.getTag(R.id.vaccine_group_parent_id)),
                            //TODO if error use immediately below
                            // (org.smartregister.immunization.domain.jsonmapping.VaccineGroup) curGroup.getTag(R.id.vaccine_group_vaccine_data),
                            curGroup.getVaccineData(),
                            vaccineList, alerts);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
        }
    }

    private class NamedObject<T> {
        public final String name;
        public final T object;

        public NamedObject(String name, T object) {
            this.name = name;
            this.object = object;
        }
    }

    //Recurring Service

    public void onGiveToday(ServiceWrapper tag, View v) {
        if (tag != null) {
            View view = RecurringServiceUtils.getLastOpenedServiceView(serviceGroups);
            saveService(tag, view);
        }
    }


    public void onGiveEarlier(ServiceWrapper tag, View v) {
        if (tag != null) {
            View view = RecurringServiceUtils.getLastOpenedServiceView(serviceGroups);
            saveService(tag, view);
        }
    }


    public void onUndoService(ServiceWrapper tag, View v) {
        org.smartregister.util.Utils.startAsyncTask(new UndoServiceTask(tag), null);
    }

    public void saveService(ServiceWrapper tag, final View view) {
        if (tag == null) {
            return;
        }

        ServiceWrapper[] arrayTags = {tag};
        SaveServiceTask backgroundTask = new SaveServiceTask();
        String providerId = ImmunizationLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();

        backgroundTask.setProviderId(providerId);
        backgroundTask.setView(view);
        org.smartregister.util.Utils.startAsyncTask(backgroundTask, arrayTags);
    }


    @SuppressLint("StaticFieldLeak")
    public class SaveServiceTask extends AsyncTask<ServiceWrapper, Void, Triple<ArrayList<ServiceWrapper>, List<ServiceRecord>, List<Alert>>> {

        private View view;
        private String providerId;

        public void setView(View view) {
            this.view = view;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        @Override
        protected void onPostExecute(Triple<ArrayList<ServiceWrapper>, List<ServiceRecord>, List<Alert>> triple) {
            RecurringServiceUtils.updateServiceGroupViews(view, triple.getLeft(), triple.getMiddle(), triple.getRight());
        }

        @Override
        protected Triple<ArrayList<ServiceWrapper>, List<ServiceRecord>, List<Alert>> doInBackground(ServiceWrapper... params) {

            ArrayList<ServiceWrapper> list = new ArrayList<>();

            for (ServiceWrapper tag : params) {
                RecurringServiceUtils.saveService(tag, childDetails.entityId(), providerId, null);
                list.add(tag);


                ServiceSchedule.updateOfflineAlerts(tag.getType(), childDetails.entityId(), org.smartregister.util.Utils.dobToDateTime(childDetails));
            }

            RecurringServiceRecordRepository recurringServiceRecordRepository = ImmunizationLibrary.getInstance().recurringServiceRecordRepository();
            List<ServiceRecord> serviceRecordList = recurringServiceRecordRepository.findByEntityId(childDetails.entityId());

            RecurringServiceTypeRepository recurringServiceTypeRepository = ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
            List<ServiceType> serviceTypes = recurringServiceTypeRepository.fetchAll();
            String[] alertArray = VaccinateActionUtils.allAlertNames(serviceTypes);

            AlertService alertService = ImmunizationLibrary.getInstance().context().alertService();
            List<Alert> alertList = alertService.findByEntityIdAndAlertNames(childDetails.entityId(), alertArray);

            return Triple.of(list, serviceRecordList, alertList);

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UndoServiceTask extends AsyncTask<Void, Void, Void> {

        private View view;
        private ServiceWrapper tag;
        private List<ServiceRecord> serviceRecordList;
        private ArrayList<ServiceWrapper> wrappers;
        private List<Alert> alertList;

        public UndoServiceTask(ServiceWrapper tag) {
            this.tag = tag;
            this.view = RecurringServiceUtils.getLastOpenedServiceView(serviceGroups);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (tag != null) {

                if (tag.getDbKey() != null) {
                    RecurringServiceRecordRepository recurringServiceRecordRepository = ImmunizationLibrary.getInstance().recurringServiceRecordRepository();
                    Long dbKey = tag.getDbKey();
                    recurringServiceRecordRepository.deleteServiceRecord(dbKey);

                    serviceRecordList = recurringServiceRecordRepository.findByEntityId(childDetails.entityId());

                    wrappers = new ArrayList<>();
                    wrappers.add(tag);

                    ServiceSchedule.updateOfflineAlerts(tag.getType(), childDetails.entityId(), org.smartregister.util.Utils.dobToDateTime(childDetails));

                    RecurringServiceTypeRepository recurringServiceTypeRepository = ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
                    List<ServiceType> serviceTypes = recurringServiceTypeRepository.fetchAll();
                    String[] alertArray = VaccinateActionUtils.allAlertNames(serviceTypes);

                    AlertService alertService = ImmunizationLibrary.getInstance().context().alertService();
                    alertList = alertService.findByEntityIdAndAlertNames(childDetails.entityId(), alertArray);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);

            tag.setUpdatedVaccineDate(null, false);
            tag.setDbKey(null);

            RecurringServiceUtils.updateServiceGroupViews(view, wrappers, serviceRecordList, alertList, true);
        }
    }
    public void updateImmunizationView(){
        if (vaccineGroups != null) {
            vaccine_group_canvas_ll.removeAllViews();
            vaccineGroups = null;
        }

        if (serviceGroups != null) {
            service_group_canvas_ll.removeAllViews();
            serviceGroups = null;
        }
        updateViews();
    }

}

