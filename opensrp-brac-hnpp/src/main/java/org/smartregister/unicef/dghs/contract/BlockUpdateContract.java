package org.smartregister.unicef.dghs.contract;

import android.content.Context;

public interface BlockUpdateContract {

    public interface Presenter{
        void getBlock();

        void updateBlock(String newBlocks);

        View getView();
    }

    public interface View{

        void showProgressBar();

        void hideProgressBar();

        void updateAdapter();

        void onBlockUpdated();

        Context getContext();

    }

}
