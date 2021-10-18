package org.smartregister.brac.hnpp.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.brac.hnpp.R;

public class PaymentHistoryViewHolder extends RecyclerView.ViewHolder {

    public TextView paymentDate;
    public TextView serviceType;
    public TextView price;
    public TextView status;
    public TextView quantity;

    public PaymentHistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        paymentDate = itemView.findViewById(R.id.payment_date_history);
        serviceType = itemView.findViewById(R.id.service_type_history_payment);
        price = itemView.findViewById(R.id.price_history_payment);
        status = itemView.findViewById(R.id.status_history_payment);
        quantity = itemView.findViewById(R.id.quantity_history_payment);
    }
}