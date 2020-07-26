package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.ForumDetailsContract;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.family.util.AppExecutors;

public class ForumDetailsPresenter implements ForumDetailsContract.Presenter {
    AppExecutors appExecutors;
    ForumDetailsContract.View view;
    public ForumDetailsPresenter(ForumDetailsContract.View view){
        this.view = view;
        appExecutors = new AppExecutors();
    }
    @Override
    public void fetchAdo(String village, String claster) {

    }

    @Override
    public void fetchWomen(String village, String claster) {

    }

    @Override
    public void processChildForumEvent(ForumDetails forumDetails) {
        view.showProgressBar();
        Runnable runnable = () -> {
            try {
                Visit visit = HnppJsonFormUtils.processAndSaveForum(HnppConstants.EVENT_TYPE.FORUM_CHILD,forumDetails);
                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();
                    if(visit != null){
                        view.showSuccessMessage("Successfully added");

                    }else{
                        view.showFailedMessage("Fail to add");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();
                    view.showFailedMessage(e.getMessage());

                });
            }


        };
        appExecutors.diskIO().execute(runnable);

    }

    @Override
    public void fetchNcd(String village, String claster) {

    }

    @Override
    public void searchHH(String name) {

    }

    @Override
    public ForumDetailsContract.View getView() {
        return null;
    }
}
