package org.smartregister.brac.hnpp.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;

public class PaymentViewHolder extends RecyclerView.ViewHolder{

    public TextView packageNameTV;
    public TextView unitPriceTV;
    public TextView quantityTV;
    public TextView numberTV;
    public TextView priceTV;
    public Button increaseBtn;
    public Button decreaseBtn;

    public PaymentViewHolder(@NonNull View itemView) {
        super(itemView);
        packageNameTV = itemView.findViewById(R.id.payment_package);
        unitPriceTV = itemView.findViewById(R.id.payment_unit_price);
        quantityTV = itemView.findViewById(R.id.payment_qty);
        numberTV = itemView.findViewById(R.id.integer_number);
        priceTV = itemView.findViewById(R.id.payment_price);
        increaseBtn = itemView.findViewById(R.id.increase_btn);
        decreaseBtn = itemView.findViewById(R.id.decrease_btn);
    }
}