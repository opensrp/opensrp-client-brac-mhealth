package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.SearchHHMemberContract;
import org.smartregister.brac.hnpp.model.HHMemberProperty;
import org.smartregister.brac.hnpp.model.SearchHHMemberModel;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.Iterator;

public class SearchHHMemberPresenter implements SearchHHMemberContract.Presenter {
    SearchHHMemberContract.View view;
    SearchHHMemberModel model;
    AppExecutors appExecutors;
    private ArrayList<HHMemberProperty> selectedList = new ArrayList<>();

    public ArrayList<HHMemberProperty> getSelectedList() {
        return selectedList;
    }
    public void updatePreviousSelectedList(ArrayList<HHMemberProperty> previousList){
        selectedList  = previousList;
    }

    public SearchHHMemberPresenter(SearchHHMemberContract.View view){
        this.view = view;
        model = new SearchHHMemberModel();
        appExecutors = new AppExecutors();

    }
    @Override
    public void fetchHH(String village, String claster) {
        view.showProgressBar();
        Runnable runnable = () -> {
            model.fetchHH(village,claster);
            appExecutors.mainThread().execute(() ->{
                view.hideProgressBar();
                view.updateAdapter(model.getHhList());
            });

        };
        appExecutors.diskIO().execute(runnable);

    }

    @Override
    public void fetchAdo(String village, String claster) {
        view.showProgressBar();
        Runnable runnable = () -> {
            model.fetchAdo(village,claster);
            appExecutors.mainThread().execute(() ->{
                view.hideProgressBar();
                view.updateAdapter(model.getAdoArrayList());
            });

        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchNcd(String village, String claster) {
        view.showProgressBar();
        Runnable runnable = () -> {
            model.fetchNcd(village,claster);
            appExecutors.mainThread().execute(() ->{
                view.hideProgressBar();
                view.updateAdapter(model.getNcdArrayList());
            });

        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchWomen(String village, String claster) {
        view.showProgressBar();
        Runnable runnable = () -> {
            model.fetchWomen(village,claster);
            appExecutors.mainThread().execute(() ->{
                view.hideProgressBar();
                view.updateAdapter(model.getWomenArrayList());
            });

        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchChild(String village, String claster) {
        view.showProgressBar();
        Runnable runnable = () -> {
            model.fetchChild(village,claster);
            appExecutors.mainThread().execute(() ->{
                view.hideProgressBar();
                view.updateAdapter(model.getChildArrayList());
            });

        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void searchHH(String name) {
        view.updateAdapter(model.searchByName(name));

    }

    @Override
    public SearchHHMemberContract.View getView() {
        return view;
    }
    public void updateHH(HHMemberProperty hhMemberProperty){
        selectedList.add(hhMemberProperty);
    }
    public void updateList(HHMemberProperty hhMemberProperty){
        if(selectedList.size() == 0){
            selectedList.add(hhMemberProperty);
        }else{
            for (Iterator<HHMemberProperty> it = selectedList.iterator(); it.hasNext();){
                HHMemberProperty prev = it.next();
                if (prev.getBaseEntityId().equals(hhMemberProperty.getBaseEntityId())){
                    it.remove();
                }else{
                    selectedList.add(hhMemberProperty);
                }
            }
        }

    }
}
