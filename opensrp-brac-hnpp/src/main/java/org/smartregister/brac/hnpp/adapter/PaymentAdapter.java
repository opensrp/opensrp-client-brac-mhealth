package org.smartregister.brac.hnpp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.PaymentActivity;
import org.smartregister.brac.hnpp.holder.PaymentViewHolder;
import org.smartregister.brac.hnpp.model.Payment;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import java.util.ArrayList;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentViewHolder> {
    private ArrayList<Payment> contentList;
    private Context context;
    private Runnable runnable;

    public PaymentAdapter(Context context,Runnable runnable) {
        this.context = context;
        this.runnable = runnable;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<Payment> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PaymentViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_payment_list, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder paymentViewHolder, @SuppressLint("RecyclerView") int i) {
        Payment content = contentList.get(i);
        paymentViewHolder.packageNameTV.setText(HnppConstants.targetTypeMapping.get(content.getServiceType() + ""));
        paymentViewHolder.unitPriceTV.setText(content.getUnitPrice() + "");
        paymentViewHolder.quantityTV.setText(content.getQuantity() + "");
        paymentViewHolder.numberTV.setText(content.getPayFor() + "");
        //       paymentViewHolder.priceTV.setText(Double.valueOf(paymentViewHolder.quantityTV.getText().toString())*Double.valueOf(paymentViewHolder.unitPriceTV.getText().toString())+"");
        paymentViewHolder.priceTV.setText(content.getTotal() + " Taka");

        paymentViewHolder.increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(content.getQuantity()>content.getPayFor()){
                    content.setPayFor(content.getPayFor()+1);
                    content.setTotal(content.getPayFor() * content.getUnitPrice());
                }
                notifyItemChanged(i);
                runnable.run();

            }
        });

        paymentViewHolder.decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(content.getPayFor()>0){
                    content.setPayFor(content.getPayFor()- 1);
                    content.setTotal(content.getPayFor() * content.getUnitPrice());

                }
                notifyItemChanged(i);
                runnable.run();

            }
        });
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    //call this method from confirm button

    public ArrayList<Payment> getPaymentWithoutZero(){
        totalPayableAmount = 0;
        ArrayList<Payment> details = new ArrayList<>();
        for(Payment payment : contentList){
            if(payment.getTotal()>0){
                details.add(payment);
                totalPayableAmount = totalPayableAmount + payment.getTotal();
            }
        }
        return details;

    }

    private int totalPayableAmount = 0;
    //get total price
    public int getTotalPayableAmount() {
        return totalPayableAmount;
    }


}
