package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.ForumDetailsContract;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForumDetailsPresenter implements ForumDetailsContract.Presenter {
    AppExecutors appExecutors;
    ForumDetailsContract.View view;
    public ForumDetailsPresenter(ForumDetailsContract.View view){
        this.view = view;
        appExecutors = new AppExecutors();
    }


    @Override
    public void processAdoForum(ForumDetails forumDetails) {
        processForum(HnppConstants.EVENT_TYPE.FORUM_ADO,forumDetails);
    }

    @Override
    public void processWomenForum(ForumDetails forumDetails) {
        processForum(HnppConstants.EVENT_TYPE.FORUM_WOMEN,forumDetails);
    }

    @Override
    public void processChildForumEvent(ForumDetails forumDetails) {
       processForum(HnppConstants.EVENT_TYPE.FORUM_CHILD,forumDetails);

    }

    @Override
    public void processNcdForum(ForumDetails forumDetails) {
        processForum(HnppConstants.EVENT_TYPE.FORUM_NCD,forumDetails);
    }

    @Override
    public void processAdultForum(ForumDetails forumDetails) {
        processForum(HnppConstants.EVENT_TYPE.FORUM_ADULT,forumDetails);
    }

    private void processForum(String eventType, ForumDetails forumDetails){
        view.showProgressBar();
        Runnable runnable = () -> {
            try {
                Visit visit = HnppJsonFormUtils.processAndSaveForum(eventType,forumDetails);

                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();
                    if(visit != null){
                        VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                        view.showSuccessMessage("জমা দেয়া হয়েছে");

                    }else{
                        view.showFailedMessage("জমা দেওয়া যায়নি");
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
    public void searchHH(String name) {

    }


    @Override
    public ForumDetailsContract.View getView() {
        return null;
    }
}