package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.SearchHHMemberContract;
import org.smartregister.brac.hnpp.model.HHMemberProperty;
import org.smartregister.brac.hnpp.model.SearchHHMemberModel;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
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
        selectedList.clear();
        selectedList.addAll(previousList);
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
    public void fetchAdult(String village, String claster) {
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
        view.updateAdapter(model.searchHH(name));

    }
    public void searchAdo(String name) {
        view.updateAdapter(model.searchAdo(name));

    }
    public void searchWomen(String name) {
        view.updateAdapter(model.searchWomen(name));

    }
    public void searchChild(String name) {
        view.updateAdapter(model.searchChild(name));

    }
    public void searchNcd(String name) {
        view.updateAdapter(model.searchNcd(name));

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
            Iterator<HHMemberProperty> it = selectedList.iterator();
            while (it.hasNext()){
               try{
                   HHMemberProperty prev = it.next();
                   if (prev.getBaseEntityId().equals(hhMemberProperty.getBaseEntityId())){
                       it.remove();
                   }else{
                       selectedList.add(hhMemberProperty);
                   }
               }catch (ConcurrentModificationException e){
                   e.printStackTrace();
                   break;
               }

            }
        }

    }
}
