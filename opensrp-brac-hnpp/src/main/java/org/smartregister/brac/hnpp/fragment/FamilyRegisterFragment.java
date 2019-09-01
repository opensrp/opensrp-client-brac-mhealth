package org.smartregister.brac.hnpp.fragment;

import android.view.View;
import android.widget.Toast;

import org.smartregister.brac.hnpp.provider.HHRegisterProvider;
import org.smartregister.chw.core.fragment.CoreFamilyRegisterFragment;
import org.smartregister.chw.core.provider.CoreRegisterProvider;
import org.smartregister.brac.hnpp.R;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;

import java.util.Set;

public class FamilyRegisterFragment extends CoreFamilyRegisterFragment {

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        CoreRegisterProvider chwRegisterProvider = new HHRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, chwRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout).setVisibility(View.VISIBLE);
        dueOnlyLayout.setVisibility(View.GONE);
    }

    @Override
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        if (view.getId() == R.id.filter_sort_layout) {
            Toast.makeText(getContext(), "sdfdafd", Toast.LENGTH_SHORT).show();
        }
    }
}
