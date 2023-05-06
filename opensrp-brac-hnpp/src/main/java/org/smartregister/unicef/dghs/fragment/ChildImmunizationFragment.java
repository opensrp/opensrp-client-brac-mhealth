package org.smartregister.unicef.dghs.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.joda.time.DateTime;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.immunization.fragment.VaccinationEditDialogFragment;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.PaymentActivity;
import org.smartregister.unicef.dghs.activity.TikaCardViewActivity;
import org.smartregister.unicef.dghs.activity.WebViewActivity;
import org.smartregister.unicef.dghs.job.HnppSyncIntentServiceJob;
import org.smartregister.unicef.dghs.job.VisitLogServiceJob;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
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
import org.smartregister.immunization.service.intent.RecurringIntentService;
import org.smartregister.immunization.service.intent.VaccineIntentService;
import org.smartregister.immunization.util.RecurringServiceUtils;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.immunization.view.ServiceGroup;
import org.smartregister.immunization.view.VaccineGroup;
import org.smartregister.service.AlertService;
import org.smartregister.unicef.dghs.utils.GrowthUtil;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.util.DateUtil;
import org.smartregister.util.Utils;
import org.smartregister.view.fragment.BaseProfileFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static org.smartregister.util.Utils.getName;

public class ChildImmunizationFragment extends BaseProfileFragment implements  SyncStatusBroadcastReceiver.SyncStatusListener {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    public void setChildDetails(CommonPersonObjectClient childDetails){
        this.childDetails = childDetails;
    }
    public static ChildImmunizationFragment newInstance(Bundle bundle) {
        Bundle args = bundle;

        ChildImmunizationFragment fragment = new ChildImmunizationFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity = null;
    }

    Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreation() {
        //Overriden
    }

    @Override
    protected void onResumption() {
        //Overriden
        if (vaccineGroups != null) {
            LinearLayout vaccineGroupCanvasLL = (LinearLayout) view.findViewById(R.id.vaccine_group_canvas_ll);
            vaccineGroupCanvasLL.removeAllViews();
            vaccineGroups = null;
        }

        if (serviceGroups != null) {
            LinearLayout serviceGroupCanvasLL = (LinearLayout) view.findViewById(R.id.service_group_canvas_ll);
            serviceGroupCanvasLL.removeAllViews();
            serviceGroups = null;
        }

        updateViews();

        startServices();
    }

//    ChildChildImmunizationFragment cia;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.immunization_activity_main, container, false);
        this.view = fragmentView;
        fragmentView.findViewById(R.id.showTikaCardBtn).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                String childId = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "base_entity_id", false);
                startActivity(new Intent(mActivity, TikaCardViewActivity.class).putExtra("BASE_ENTITY_ID",childId));
//                SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(ChildImmunizationFragment.this);
//                HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);


//                Utils.startAsyncTask(new AsyncTask() {
//                    @Override
//                    protected void onPostExecute(Object o) {
//                        super.onPostExecute(o);
//                        if(o instanceof String){
//                            String pdfFilePath = (String)o;
//                            openPDF(pdfFilePath);
//                        }
//
//                    }
//
//                    @Override
//                    protected Object doInBackground(Object[] objects) {
//                        try {
//                            String htmlUrl = "http://unicef-ha.mpower-social.com/opensrp-dashboard/people/mobile/e9c1679c-6651-4bf3-a1ab-a180752ce124-pros/vaccine-card.html";
//                            File htmlFile = new File(htmlUrl);
//                            String filePath = mActivity.getExternalFilesDir(null) + "/vaccine";
//                            File file = new File(filePath);
//                            if(!file.exists()){
//                                file.mkdir();
//                            }
//                            String pdfFilePath = (file.getAbsolutePath() + "/"+ "vaccine_card.pdf");
//                            Log.v("VACCINE_CARD","vaccine_file>>"+pdfFilePath);
//                            String html = FileUtils.readFileToString(htmlFile, "UTF-8");
//
//                            // Create PDF document
//                            PDDocument document = new PDDocument();
//                            PDPage page = new PDPage();
//                            document.addPage(page);
//
//                            // Create font
//                            PDFont font = PDType1Font.HELVETICA_BOLD;
//
//                            // Create stream for HTML file
//                            PDStream stream = new PDStream(document);
//                            OutputStream outputStream = stream.createOutputStream();
//                            outputStream.write(html.getBytes("UTF-8"));
//                            outputStream.close();
//
//                            // Convert HTML to PDF
//                            PDFTextStripper pdfStripper = new PDFTextStripper();
//                            pdfStripper.setSortByPosition(true);
//                            pdfStripper.setStartPage(0);
//                            pdfStripper.setEndPage(Integer.MAX_VALUE);
//                            String parsedText = pdfStripper.getText(document);
//                            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
//                            pdfStripper.writeText(document, dummy);
//
//                            // Save PDF file
//                            document.save(pdfFilePath);
//                            document.close();
//
//                            return pdfFilePath;
//
//
//                        } catch (MalformedURLException e) {
//                            e.printStackTrace();
//                            return null;
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            return null;
//                        }
//                    }
//                },null);

            }
        });
//        cia = new ChildImmunizationFragment(fragmentView,mActivity);
        return fragmentView;
    }

    private void openPDF(String pdfFilePath) {
        File file = new File(pdfFilePath);
        Uri uri = FileProvider.getUriForFile(mActivity,"org.smartregister.unicef.dghs.fileprovider",file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        startActivity(intent);
    }

    // Data
//    private CommonPersonObjectClient childDetails = Utils.dummyDetatils();
    private CommonPersonObjectClient childDetails;
    private static final String TAG = ChildImmunizationFragment.class.getCanonicalName();
    private static final String DIALOG_TAG = "DIALOG_TAAAGGG";
    private static final String EXTRA_CHILD_DETAILS = "child_details";

    private ArrayList<VaccineGroup> vaccineGroups;
    private ArrayList<ServiceGroup> serviceGroups;

    private static final ArrayList<String> COMBINED_VACCINES;
    private static final HashMap<String, String> COMBINED_VACCINES_MAP;

    private static final boolean isChildActive = true;

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


    View view;

    private boolean isDataOk() {
        return childDetails != null && childDetails.getDetails() != null;
    }

    public void updateViews() {


        updateAgeViews();
        updateChildIdViews();

        VaccineRepository vaccineRepository = ImmunizationLibrary.getInstance().vaccineRepository();

        RecurringServiceTypeRepository recurringServiceTypeRepository = ImmunizationLibrary.getInstance().recurringServiceTypeRepository();

        RecurringServiceRecordRepository recurringServiceRecordRepository = ImmunizationLibrary.getInstance().recurringServiceRecordRepository();

        AlertService alertService = ImmunizationLibrary.getInstance().context().alertService();

        UpdateViewTask updateViewTask = new UpdateViewTask();
        updateViewTask.setVaccineRepository(vaccineRepository);
        updateViewTask.setRecurringServiceTypeRepository(recurringServiceTypeRepository);
        updateViewTask.setRecurringServiceRecordRepository(recurringServiceRecordRepository);
        updateViewTask.setAlertService(alertService);
        org.smartregister.util.Utils.startAsyncTask(updateViewTask, null);
    }

    private void updateChildIdViews() {
        String name = "";
        String childId = "";
        if (isDataOk()) {
            name = constructChildName();
            childId = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "unique_id", false);
        }

        TextView nameTV = (TextView) view.findViewById(R.id.name_tv);
        nameTV.setText(name);
        TextView childIdTV = (TextView) view.findViewById(R.id.child_id_tv);
        childIdTV.setText(String.format("%s: %s", "ID", childId));
    }

    private void updateAgeViews() {
        String formattedAge = "";
        String formattedDob = "";
        if (isDataOk()) {
            String dobString = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "dob", false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                Date dob = dateTime.toDate();
                formattedDob = DATE_FORMAT.format(dob);
                long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();

                if (timeDiff >= 0) {
                    formattedAge = DateUtil.getDuration(timeDiff);
                }
            }
        }
        TextView dobTV = (TextView) view.findViewById(R.id.dob_tv);
        dobTV.setText(String.format("%s: %s", "Birth Date", formattedDob));
        TextView ageTV = (TextView) view.findViewById(R.id.age_tv);
        ageTV.setText(String.format("%s: %s", "Age", formattedAge));
    }

    private void updateServiceViews(Map<String, List<ServiceType>> serviceTypeMap, List<ServiceRecord> serviceRecordList, List<Alert> alerts) {

        Map<String, List<ServiceType>> foundServiceTypeMap = new LinkedHashMap<>();
        if (serviceGroups == null) {
            for (String type : serviceTypeMap.keySet()) {
                if (foundServiceTypeMap.containsKey(type)) {
                    continue;
                }

                for (ServiceRecord serviceRecord : serviceRecordList) {
                    if (serviceRecord.getSyncStatus().equals(RecurringServiceTypeRepository.TYPE_Unsynced)) {
                        if (serviceRecord.getType().equals(type)) {
                            foundServiceTypeMap.put(type, serviceTypeMap.get(type));
                            break;
                        }
                    }
                }

                if (foundServiceTypeMap.containsKey(type)) {
                    continue;
                }

                for (Alert a : alerts) {
                    if (StringUtils.containsIgnoreCase(a.scheduleName(), type)
                            || StringUtils.containsIgnoreCase(a.visitCode(), type)) {
                        foundServiceTypeMap.put(type, serviceTypeMap.get(type));
                        break;
                    }
                }

            }

            if (foundServiceTypeMap.isEmpty()) {
                return;
            }


            serviceGroups = new ArrayList<>();
            LinearLayout serviceGroupCanvasLL = (LinearLayout) view.findViewById(R.id.service_group_canvas_ll);

            ServiceGroup curGroup = new ServiceGroup(mActivity);
            curGroup.setChildActive(isChildActive);
            curGroup.setData(childDetails, foundServiceTypeMap, serviceRecordList, alerts);
            curGroup.setOnServiceClickedListener(new ServiceGroup.OnServiceClickedListener() {
                @Override
                public void onClick(ServiceGroup serviceGroup, ServiceWrapper
                        serviceWrapper) {
                    addServiceDialogFragment(serviceWrapper, serviceGroup);
                }
            });
            curGroup.setOnServiceUndoClickListener(new ServiceGroup.OnServiceUndoClickListener() {
                @Override
                public void onUndoClick(ServiceGroup serviceGroup, ServiceWrapper serviceWrapper) {
                    addServiceUndoDialogFragment(serviceGroup, serviceWrapper);
                }
            });
            serviceGroupCanvasLL.addView(curGroup);
            serviceGroups.add(curGroup);
        }

    }

    private void updateVaccinationViews(List<Vaccine> vaccineList, List<Alert> alerts) {

        if (vaccineGroups == null) {
            vaccineGroups = new ArrayList<>();
            List<org.smartregister.immunization.domain.jsonmapping.VaccineGroup> supportedVaccines =
                    VaccinatorUtils.getSupportedVaccines(mActivity);

            for (org.smartregister.immunization.domain.jsonmapping.VaccineGroup vaccineGroupObject : supportedVaccines) {
                //Add BCG2 special vaccine to birth vaccine group
                VaccinateActionUtils.addBcg2SpecialVaccine(mActivity, vaccineGroupObject, vaccineList);

                addVaccineGroup(-1, vaccineGroupObject, vaccineList, alerts);
            }
        }
    }


    private void addVaccineGroup(int canvasId, org.smartregister.immunization.domain.jsonmapping.VaccineGroup vaccineGroupData, List<Vaccine> vaccineList, List<Alert> alerts) {
        LinearLayout vaccineGroupCanvasLL = (LinearLayout) view.findViewById(R.id.vaccine_group_canvas_ll);
        VaccineGroup curGroup = new VaccineGroup(mActivity);
        curGroup.setChildActive(isChildActive);
        curGroup.setData(vaccineGroupData, childDetails, vaccineList, alerts, "child");
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
                ArrayList<VaccineWrapper> vaccineWrappers = new ArrayList<>();
                vaccineWrappers.add(vaccine);
                addVaccinationDialogFragment(vaccineWrappers, vaccineGroup);
            }
        });
        curGroup.setOnVaccineInvalidClickListener(new VaccineGroup.OnVaccineInvalidClickListener() {
            @Override
            public void onInvalidClick(VaccineGroup vaccineGroup, VaccineWrapper vaccine) {
                ArrayList<VaccineWrapper> vaccineWrappers = new ArrayList<>();
                vaccineWrappers.add(vaccine);
                addVaccinationDialogFragment(vaccineWrappers, vaccineGroup);
            }
        });

        LinearLayout parent;
        if (canvasId == -1) {
            Random r = new Random();
            canvasId = r.nextInt(4232 - 213) + 213;
            parent = new LinearLayout(mActivity);
            parent.setId(canvasId);
            vaccineGroupCanvasLL.addView(parent);
        } else {
            parent = (LinearLayout) view.findViewById(canvasId);
            parent.removeAllViews();
        }
        parent.addView(curGroup);
        curGroup.setTag(R.id.vaccine_group_vaccine_data, vaccineGroupData);
        curGroup.setTag(R.id.vaccine_group_parent_id, String.valueOf(canvasId));
        vaccineGroups.add(curGroup);
    }

    private void addVaccineUndoDialogFragment(VaccineGroup vaccineGroup, List<VaccineWrapper> vaccineWrappers) {
////        FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
////        Fragment prev = mActivity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
////        if (prev != null) {
////            ft.remove(prev);
////        }
////
////        ft.addToBackStack(null);
//        vaccineGroup.setModalOpen(true);
//
//        UndoVaccinationDialogFragment undoVaccinationDialogFragment = UndoVaccinationDialogFragment.newInstance(vaccineWrapper);
//        undoVaccinationDialogFragment.show(getChildFragmentManager(), DIALOG_TAG);
//        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_TAG);
//        if (prev != null) {
//            ft.remove(prev);
//        }
//        ft.addToBackStack(null);

        String dobString = Utils.getValue(childDetails.getColumnmaps(), "dob", false);
        Date dob = Calendar.getInstance().getTime();
        if (!TextUtils.isEmpty(dobString)) {
            DateTime dateTime = new DateTime(dobString);
            dob = dateTime.toDate();
        }

        List<Vaccine> vaccineList = ImmunizationLibrary.getInstance().vaccineRepository()
                .findByEntityId(childDetails.entityId());
        if (vaccineList == null) vaccineList = new ArrayList<>();

        VaccinationEditDialogFragment vaccinationDialogFragment = VaccinationEditDialogFragment.newInstance(getActivity(), dob, vaccineList, vaccineWrappers, vaccineGroup, true);
        vaccinationDialogFragment.show(getChildFragmentManager(), DIALOG_TAG);
    }

    private void addServiceUndoDialogFragment(ServiceGroup serviceGroup, ServiceWrapper serviceWrapper) {
//        FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
//        Fragment prev = mActivity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
//        if (prev != null) {
//            ft.remove(prev);
//        }
//
//        ft.addToBackStack(null);
        serviceGroup.setModalOpen(true);

        UndoServiceDialogFragment undoServiceDialogFragment = UndoServiceDialogFragment.newInstance(serviceWrapper);
        undoServiceDialogFragment.show(getChildFragmentManager(), DIALOG_TAG);
    }



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

//        FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
//        Fragment prev = mActivity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
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

//        FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
//        Fragment prev = mActivity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
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
        HnppConstants.isViewRefresh = true;
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
        vaccine.setVaccineDueDate(tag.getVaccineDate());
        vaccine.setAnmId(ImmunizationLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM());

        String lastChar = vaccine.getName().substring(vaccine.getName().length() - 1);
        if (StringUtils.isNumeric(lastChar)) {
            vaccine.setCalculation(Integer.valueOf(lastChar));
        } else {
            vaccine.setCalculation(-1);
        }
        String anm = FamilyLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();

        vaccine.setTeam(FamilyLibrary.getInstance().context().allSharedPreferences().fetchDefaultTeam(anm));
        vaccine.setTeamId(FamilyLibrary.getInstance().context().allSharedPreferences().fetchDefaultTeamId(anm));
        vaccine.setChildLocationId("testChildLocationId");
        vaccineRepository.add(vaccine);
        tag.setDbKey(vaccine.getId());
        if(tag.getDbKey()!=null){
            GrowthUtil.updateLastVaccineDate(childDetails.entityId(),DATE_FORMAT.format(vaccine.getDate()),vaccine.getName());
        }
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
                vaccineGroup.updateWrapperStatus(wrappers, "child");
            }
            vaccineGroup.updateViews(wrappers);

        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (undo) {
                        vaccineGroup.setVaccineList(vaccineList);
                        vaccineGroup.updateWrapperStatus(wrappers, "child");
                    }
                    vaccineGroup.updateViews(wrappers);
                }
            });
        }
    }

    public void startServices() {
        VaccineRecurringServiceJob.scheduleJobImmediately(VaccineRecurringServiceJob.TAG);

    }

    @Override
    public void onSyncStart() {

    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {

    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        String childId = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "base_entity_id", false);
        startActivity(new Intent(mActivity, WebViewActivity.class).putExtra("BASE_ENTITY_ID",childId));

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
            if(mActivity==null || mActivity.isFinishing()) return;
            updateVaccineGroupViews(view, pair.first, pair.second);

            updateVaccineGroupsUsingAlerts(affectedVaccines, vaccineList, alertList);
            VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
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
                affectedVaccines = VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime, "child");
            }
            vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());
            alertList = alertService.findByEntityIdAndAlertNames(childDetails.entityId(),
                    VaccinateActionUtils.allAlertNames("child"));

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
            if(mActivity == null || mActivity.isFinishing()) return;

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

            updateServiceViews(serviceTypeMap, serviceRecords, alertList);
            updateVaccinationViews(vaccineList, alertList);
        }

        @Override
        protected Map<String, NamedObject<?>> doInBackground(Void... voids) {
            String dobString = org.smartregister.util.Utils.getValue(childDetails.getColumnmaps(), "dob", false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime, "child");
                ServiceSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime);
            }

            List<Vaccine> vaccineList = new ArrayList<>();

            Map<String, List<ServiceType>> serviceTypeMap = new LinkedHashMap<>();
            List<ServiceRecord> serviceRecords = new ArrayList<>();

            List<Alert> alertList = new ArrayList<>();
            if (vaccineRepository != null) {
                vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());

            }

            if (recurringServiceRecordRepository != null) {
                serviceRecords = recurringServiceRecordRepository.findByEntityId(childDetails.entityId());
            }

            if (recurringServiceTypeRepository != null) {
                List<ServiceType> serviceTypes = recurringServiceTypeRepository.fetchAll();
                for (ServiceType serviceType : serviceTypes) {
                    String type = serviceType.getType();
                    List<ServiceType> serviceTypeList = serviceTypeMap.get(type);
                    if (serviceTypeList == null) {
                        serviceTypeList = new ArrayList<>();
                    }
                    serviceTypeList.add(serviceType);
                    serviceTypeMap.put(type, serviceTypeList);
                }
            }

            if (alertService != null) {
                alertList = alertService.findByEntityId(childDetails.entityId());
            }

            Map<String, NamedObject<?>> map = new HashMap<>();

            NamedObject<List<Vaccine>> vaccineNamedObject = new NamedObject<>(Vaccine.class.getName(), vaccineList);
            map.put(vaccineNamedObject.name, vaccineNamedObject);

            NamedObject<Map<String, List<ServiceType>>> serviceTypeNamedObject = new NamedObject<>(ServiceType.class.getName(), serviceTypeMap);
            map.put(serviceTypeNamedObject.name, serviceTypeNamedObject);

            NamedObject<List<ServiceRecord>> serviceRecordNamedObject = new NamedObject<>(ServiceRecord.class.getName(), serviceRecords);
            map.put(serviceRecordNamedObject.name, serviceRecordNamedObject);

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
                        affectedVaccines = VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime, "child");
                        vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());
                        alertList = alertService.findByEntityIdAndAlertNames(childDetails.entityId(),
                                VaccinateActionUtils.allAlertNames("child"));
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


}
