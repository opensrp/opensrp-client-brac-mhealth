package org.smartregister.unicef.mis.interactor;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.contract.MigrationContract;
import org.smartregister.unicef.mis.model.GlobalLocationModel;
import org.smartregister.unicef.mis.repository.GlobalLocationRepository;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

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
