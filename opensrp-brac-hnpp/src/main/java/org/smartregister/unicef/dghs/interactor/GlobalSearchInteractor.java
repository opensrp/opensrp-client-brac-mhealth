package org.smartregister.unicef.dghs.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.activity.GlobalSearchActivity;
import org.smartregister.unicef.dghs.contract.MigrationContract;
import org.smartregister.unicef.dghs.model.GlobalLocationModel;
import org.smartregister.unicef.dghs.repository.GlobalLocationRepository;
import org.smartregister.unicef.dghs.utils.BaseLocation;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;
import java.util.Objects;

public class GlobalSearchInteractor implements MigrationContract.Interactor {
    private AppExecutors appExecutors;
    private ArrayList<GlobalLocationModel> districtArrayList = new ArrayList<>();
    private ArrayList<GlobalLocationModel> upazilaArrayList = new ArrayList<>();
    private ArrayList<GlobalLocationModel> divisionArrayList = new ArrayList<>();


    public GlobalSearchInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }
    @Override
    public void fetchDistrict(String divisionId,MigrationContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            districtArrayList.clear();
            districtArrayList = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.DISTRICT.getValue(),Integer.parseInt(divisionId));

            appExecutors.mainThread().execute(() -> callBack.onUpdateDistrict(districtArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchUpazila(String districtId, MigrationContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            upazilaArrayList.clear();
            upazilaArrayList = HnppApplication.getGlobalLocationRepository().getLocationByTagIdWithParentId(GlobalLocationRepository.LOCATION_TAG.UPAZILA.getValue(),Integer.parseInt(districtId));
            GlobalLocationModel selectModel = new GlobalLocationModel();
            selectModel.id = -1;
            selectModel.code = "-1";
            selectModel.name = "বাছাই করুন";

            upazilaArrayList.add(0,selectModel);
            appExecutors.mainThread().execute(() -> callBack.onUpdateUpazila(upazilaArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchDivision(MigrationContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            divisionArrayList.clear();
            divisionArrayList = HnppApplication.getGlobalLocationRepository().getLocationByTagId(GlobalLocationRepository.LOCATION_TAG.DIVISION.getValue());

            appExecutors.mainThread().execute(() -> callBack.onUpdateDivision(divisionArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

}
