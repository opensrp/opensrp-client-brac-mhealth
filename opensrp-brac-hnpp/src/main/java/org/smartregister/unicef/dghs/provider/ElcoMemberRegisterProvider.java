package org.smartregister.unicef.dghs.provider;

import android.content.Context;
import android.view.View;

import org.smartregister.unicef.dghs.task.UpdateElcoServiceTask;
import org.smartregister.chw.core.holders.RegisterViewHolder;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.util.Utils;

import java.util.Set;

public class ElcoMemberRegisterProvider extends HnppAllMemberRegisterProvider {
    public ElcoMemberRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, commonRepository, visibleColumns, onClickListener, paginationClickListener);
    }

    @Override
    protected void populateLastColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        Utils.startAsyncTask(new UpdateElcoServiceTask(context, viewHolder, pc.entityId()), null);

    }
}
