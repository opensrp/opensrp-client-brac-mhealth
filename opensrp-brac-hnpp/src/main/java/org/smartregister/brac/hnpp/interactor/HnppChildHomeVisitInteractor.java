package org.smartregister.brac.hnpp.interactor;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Pair;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.actionhelper.ImmunizationActionHelper;
import org.smartregister.brac.hnpp.actionhelper.ImmunizationValidator;
import org.smartregister.brac.hnpp.actionhelper.VitaminaAction;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.VaccineDisplay;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.fragment.BaseAncHomeVisitFragment;
import org.smartregister.chw.anc.fragment.BaseHomeVisitImmunizationFragment;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.dao.VisitDao;
import org.smartregister.chw.core.interactor.CoreChildHomeVisitInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.RecurringServiceUtil;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.core.utils.VaccineScheduleUtil;
import org.smartregister.chw.core.utils.VisitVaccineUtil;
import org.smartregister.domain.Alert;
import org.smartregister.family.util.Constants;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.domain.jsonmapping.Vaccine;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.util.FormUtils;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public  class HnppChildHomeVisitInteractor implements CoreChildHomeVisitInteractor.Flavor {
    protected LinkedHashMap<String, BaseAncHomeVisitAction> actionList;
    protected Context context;
    protected Map<String, List<VisitDetail>> details = null;
    protected MemberObject memberObject;
    protected BaseAncHomeVisitContract.View view;
    protected Date dob;
    protected Boolean editMode = false;
    protected Boolean vaccinesDefaultChecked = true;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());


    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        actionList = new LinkedHashMap<>();
        context = view.getContext();
        this.memberObject = memberObject;
        editMode = view.getEditMode();
        try {
            this.dob = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(memberObject.getDob());
        } catch (ParseException e) {
            Timber.e(e);
        }
        this.view = view;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), CoreConstants.EventType.CHILD_HOME_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        Map<String, ServiceWrapper> serviceWrapperMap =
                RecurringServiceUtil.getRecurringServices(
                        memberObject.getBaseEntityId(),
                        new DateTime(dob),
                        CoreConstants.SERVICE_GROUPS.CHILD
                );

        CoreConstants.JSON_FORM.setLocaleAndAssetManager(HnppApplication.getCurrentLocale(), HnppApplication.getInstance().getApplicationContext().getAssets());
        bindEvents(serviceWrapperMap);
        return actionList;
    }

    protected void bindEvents(Map<String, ServiceWrapper> serviceWrapperMap) throws BaseAncHomeVisitAction.ValidationException {
        try {

            evaluateImmunization();
            evaluateVitaminA(serviceWrapperMap);
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
    protected int immunizationCeiling() {
        return 24;
    }
    protected void evaluateImmunization() throws Exception {
        //int age = getAgeInMonths();
//        if (getAgeInMonths() >= immunizationCeiling()) return;

        List<VaccineGroup> childVaccineGroups = getVaccineGroups();
        List<Vaccine> specialVaccines = getSpecialVaccines();
        List<org.smartregister.immunization.domain.Vaccine> vaccines = getVaccineRepo().findByEntityId(memberObject.getBaseEntityId());

        List<VaccineRepo.Vaccine> allVacs = VaccineRepo.getVaccines(CoreConstants.SERVICE_GROUPS.CHILD);
        Map<String, VaccineRepo.Vaccine> vaccinesRepo = new HashMap<>();
        for (VaccineRepo.Vaccine vaccine : allVacs) {
            vaccinesRepo.put(vaccine.display().toLowerCase().replace(" ", ""), vaccine);
        }

        Map<VaccineGroup, List<Pair<VaccineRepo.Vaccine, Alert>>> pendingVaccines = VisitVaccineUtil.generateVisitVaccines(
                memberObject.getBaseEntityId(),
                vaccinesRepo,
                new DateTime(dob),
                childVaccineGroups,
                specialVaccines,
                vaccines,
                details
        );

        ImmunizationValidator validator = new ImmunizationValidator(childVaccineGroups, specialVaccines, CoreConstants.SERVICE_GROUPS.CHILD, vaccines);

        for (Map.Entry<VaccineGroup, List<Pair<VaccineRepo.Vaccine, Alert>>> entry : pendingVaccines.entrySet()) {
            // add the objects to be displayed here

            List<VaccineWrapper> wrappers = VisitVaccineUtil.wrapVaccines(entry.getValue());
            List<VaccineDisplay> displays = VisitVaccineUtil.toDisplays(wrappers);

            String title = MessageFormat.format(context.getString(org.smartregister.chw.core.R.string.immunizations_count), getVaccineTitle(entry.getKey().name, context));
            BaseHomeVisitImmunizationFragment fragment =
                    BaseHomeVisitImmunizationFragment.getInstance(view, memberObject.getBaseEntityId(), details, displays, vaccinesDefaultChecked);

            validator.addFragment(title, fragment, entry.getKey(), new DateTime(dob));

            BaseAncHomeVisitAction action = getBuilder(title)
                    .withOptional(false)
                    .withDetails(details)
                    .withDestinationFragment(fragment)
                    .withHelper(new ImmunizationActionHelper(context, wrappers))
                    .withDisabledMessage(context.getString(org.smartregister.chw.core.R.string.fill_earler_immunization))
                    .withValidator(validator)
                    .build();

            actionList.put(title, action);
        }

    }
    public static String getVaccineTitle(String name, Context context) {
        return "Birth".equals(name) ? context.getString(R.string.at_birth) : name.replace("Weeks", "উইকস").toLowerCase().replace("Months", "মাস").toLowerCase();
    }
    @VisibleForTesting
    public BaseAncHomeVisitAction.Builder getBuilder(String title) {
        return new BaseAncHomeVisitAction.Builder(context, title);
    }

    @VisibleForTesting
    public JSONObject getFormJson(String name) throws Exception {
        return FormUtils.getInstance(context).getFormJson(name);
    }

    @VisibleForTesting
    public JSONObject getFormJson(String name, String baseEntityID) throws Exception {
        return HnppJsonFormUtils.getJson(name, baseEntityID);
    }

    @VisibleForTesting
    public VaccineRepository getVaccineRepo() {
        return CoreChwApplication.getInstance().vaccineRepository();
    }

    @VisibleForTesting
    public List<VaccineGroup> getVaccineGroups() {
        return VaccineScheduleUtil.getVaccineGroups(HnppApplication.getInstance().getApplicationContext(), CoreConstants.SERVICE_GROUPS.CHILD);
    }

    @VisibleForTesting
    public List<Vaccine> getSpecialVaccines() {
        return VaccinatorUtils.getSpecialVaccines(context);
    }


    protected void evaluateVitaminA(Map<String, ServiceWrapper> serviceWrapperMap) throws Exception {
        ServiceWrapper serviceWrapper = serviceWrapperMap.get("Vitamin A");
        if (serviceWrapper == null) return;

        Alert alert = serviceWrapper.getAlert();
        if (alert == null || new LocalDate().isBefore(new LocalDate(alert.startDate()))) return;


        final String serviceIteration = serviceWrapper.getName().substring(serviceWrapper.getName().length() - 1);

        String title = context.getString(R.string.vitamin_a_number_dose, Utils.getDayOfMonthWithSuffix(Integer.valueOf(serviceIteration), context));

        // alert if overdue after 14 days
        boolean isOverdue = new LocalDate().isAfter(new LocalDate(alert.startDate()).plusDays(14));
        String dueState = !isOverdue ? context.getString(R.string.due) : context.getString(R.string.overdue);

        VitaminaAction helper = new VitaminaAction(context, serviceIteration, alert);
        JSONObject jsonObject = getFormJson(CoreConstants.JSON_FORM.CHILD_HOME_VISIT.getVitaminA(), memberObject.getBaseEntityId());

        Map<String, List<VisitDetail>> details = getDetails(CoreConstants.EventType.VITAMIN_A);

        // Before pre-processing
        setMinDate(jsonObject, "vitamin_a{0}_date", memberObject.getDob());

        JSONObject preProcessObject = helper.preProcess(jsonObject, serviceIteration);
        if (details != null && details.size() > 0) JsonFormUtils.populateForm(jsonObject, details);

        BaseAncHomeVisitAction action = getBuilder(title)
                .withHelper(helper)
                .withDetails(details)
                .withOptional(false)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withPayloadType(BaseAncHomeVisitAction.PayloadType.SERVICE)
                .withPayloadDetails(MessageFormat.format("Vitamin_A{0}", serviceIteration))
                .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, null, preProcessObject, details, serviceIteration))
                .withScheduleStatus(!isOverdue ? BaseAncHomeVisitAction.ScheduleStatus.DUE : BaseAncHomeVisitAction.ScheduleStatus.OVERDUE)
                .withSubtitle(MessageFormat.format("{0} {1}", dueState, DateTimeFormat.forPattern("dd MMM yyyy").print(new DateTime(serviceWrapper.getVaccineDate()))))
                .build();

        // don't show if its after now
        if (!serviceWrapper.getVaccineDate().isAfterNow()) actionList.put(title, action);
    }
    private void setMinDate(JSONObject jsonObject, String dateFieldKey, String minDateString) {
        try {
            Date minDate = dateFormat.parse(minDateString);
            String parsedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(minDate);
            JSONObject fieldJSONObject = HnppJsonFormUtils.getFieldJSONObject(HnppJsonFormUtils.fields(jsonObject), dateFieldKey);
            fieldJSONObject.put(JsonFormConstants.MIN_DATE, parsedDate);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    protected Map<String, List<VisitDetail>> getDetails(String eventName) {
        if (!editMode)
            return null;

        Map<String, List<VisitDetail>> visitDetails = null;
        Visit lastVisit = getVisitRepository().getLatestVisit(memberObject.getBaseEntityId(), eventName);
        if (lastVisit != null) {
            visitDetails = VisitUtils.getVisitGroups(getVisitDetailsRepository().getVisits(lastVisit.getVisitId()));
        }

        return visitDetails;
    }

    @VisibleForTesting
    public Map<String, ServiceWrapper> getServices() {
        return RecurringServiceUtil.getRecurringServices(
                memberObject.getBaseEntityId(),
                new DateTime(dob),
                CoreConstants.SERVICE_GROUPS.CHILD
        );
    }

    protected VisitRepository getVisitRepository() {
        return AncLibrary.getInstance().visitRepository();
    }

    protected VisitDetailsRepository getVisitDetailsRepository() {
        return AncLibrary.getInstance().visitDetailsRepository();
    }
    private String getVaccineTitle(String name) {
        if ("Birth".equals(name)) {
            return context.getString(R.string.at_birth);
        }

        return name.replace("Weeks", context.getString(org.smartregister.chw.core.R.string.abbrv_weeks))
                .replace("Months", context.getString(org.smartregister.chw.core.R.string.abbrv_months))
                .replace(" ", "");
    }

    protected int getAgeInMonths() {
        return Months.monthsBetween(new LocalDate(dob), new LocalDate()).getMonths();
    }
}