package org.smartregister.unicef.mis.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.model.PaymentHistory;
import org.smartregister.unicef.mis.utils.HnppConstants;

import java.util.ArrayList;

public class PaymentHistoryAdapter  extends RecyclerView.Adapter<PaymentHistoryViewHolder> {
    private ArrayList<PaymentHistory> contentList;
    private Context context;

    public PaymentHistoryAdapter(Context context) {
        this.context = context;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<PaymentHistory> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public PaymentHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PaymentHistoryViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_history_payment_list, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentHistoryViewHolder paymenthistoryViewHolder, @SuppressLint("RecyclerView") int i) {
        PaymentHistory content = contentList.get(i);
        paymenthistoryViewHolder.paymentDate.setText(content.getPaymentDate());
        //paymenthistoryViewHolder.serviceType.setText(content.getServiceType() + "");

        paymenthistoryViewHolder.serviceType.setText(HnppConstants.targetTypeMapping.get(content.getServiceType() + ""));
        paymenthistoryViewHolder.quantity.setText(content.getQuantity()+"");
        paymenthistoryViewHolder.price.setText(content.getPrice() );
        paymenthistoryViewHolder.status.setText(content.getStatus());
        if(content.getStatus()!=null && content.getStatus().equalsIgnoreCase("COMPLETED")){
            paymenthistoryViewHolder.status.setTextColor(context.getResources().getColor(R.color.alert_complete_green));
        }else if(content.getStatus()!=null && content.getStatus().equalsIgnoreCase("FAILED")){
            paymenthistoryViewHolder.status.setTextColor(context.getResources().getColor(R.color.alert_urgent_red));
        }
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }
}