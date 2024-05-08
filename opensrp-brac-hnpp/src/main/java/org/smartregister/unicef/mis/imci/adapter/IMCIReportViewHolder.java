package org.smartregister.unicef.mis.imci.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.smartregister.unicef.mis.R;

public class IMCIReportViewHolder extends RecyclerView.ViewHolder {

    public TextView imciTypeTv;
    public TextView assessmentResultTv;
    public TextView assessmentResultText;
    public IMCIReportViewHolder(@NonNull View itemView) {
        super(itemView);
        imciTypeTv = itemView.findViewById(R.id.imci_type_tv);
        assessmentResultTv = itemView.findViewById(R.id.assessment_result_tv);
        assessmentResultText = itemView.findViewById(R.id.assesment_result_txt);
    }
}