package org.smartregister.unicef.mis.provider;

import android.content.Context;
import android.view.View;

import org.smartregister.unicef.mis.task.UpdateWomenServiceTask;
import org.smartregister.chw.core.holders.RegisterViewHolder;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.Utils;

import java.util.Set;

public class HnppWomenServiceRegisterProvider extends HnppAllMemberRegisterProvider {
    public HnppWomenServiceRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, commonRepository, visibleColumns, onClickListener, paginationClickListener);
    }

    @Override
    protected void populateLastColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        Utils.startAsyncTask(new UpdateWomenServiceTask(context, viewHolder, pc.entityId()), null);

    }
}
