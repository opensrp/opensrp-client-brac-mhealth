package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.PaymentActivity;
import org.smartregister.brac.hnpp.holder.NotificationViewHolder;
import org.smartregister.brac.hnpp.holder.PaymentViewHolder;
import org.smartregister.brac.hnpp.model.Notification;
import org.smartregister.brac.hnpp.model.Payment;

import java.util.ArrayList;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentViewHolder>{
    private ArrayList<Payment> contentList;
    private Context context;
    int quantity, unitPrice, price;
    int total;
    private PaymentActivity.listener totalListener;
    //private PaymentAdapter.OnClickAdapter onClickAdapter;

    public PaymentAdapter(Context context) {
        this.context = context;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<Payment> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PaymentViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_payment_list, null));
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder paymentViewHolder, int position) {
        final Payment content = contentList.get(position);
        paymentViewHolder.packageNameTV.setText(content.getPackageName()+"");
        paymentViewHolder.unitPriceTV.setText(content.getUnitPrice()+"");
        paymentViewHolder.quantityTV.setText(content.getQuantity()+"");
        paymentViewHolder.numberTV.setText(content.getQuantity()+"");
        paymentViewHolder.priceTV.setText(Integer.valueOf(paymentViewHolder.quantityTV.getText().toString())*Integer.valueOf(paymentViewHolder.unitPriceTV.getText().toString())+"");

        paymentViewHolder.increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Integer.valueOf(paymentViewHolder.quantityTV.getText().toString()) > Integer.valueOf(paymentViewHolder.numberTV.getText().toString())){
                    quantity = Integer.valueOf(paymentViewHolder.numberTV.getText().toString());
                    quantity = quantity+1;
                    paymentViewHolder.numberTV.setText(quantity+"");
                    unitPrice = Integer.valueOf(paymentViewHolder.unitPriceTV.getText().toString());
                    price = quantity*unitPrice;
                    paymentViewHolder.priceTV.setText(price+"");
                    //notifyDataSetChanged();
                }
            }
        });

        paymentViewHolder.decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Integer.valueOf(paymentViewHolder.numberTV.getText().toString())>0){
                    quantity = Integer.valueOf(paymentViewHolder.numberTV.getText().toString());
                    quantity = quantity-1;
                    paymentViewHolder.numberTV.setText(quantity+"");
                    unitPrice = Integer.valueOf(paymentViewHolder.unitPriceTV.getText().toString());
                    price = quantity*unitPrice;
                    paymentViewHolder.priceTV.setText(price+"");
                    //notifyDataSetChanged();

                }

            }
        });
        int amount = addPayment(Integer.valueOf(paymentViewHolder.priceTV.getText().toString()));
        totalListener.addsum(amount);

    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public void setListener(PaymentActivity.listener listener) {
        totalListener = listener;
    }
   /* public interface OnClickAdapter {
        void onClick(int position, Payment content, PaymentViewHolder paymentViewHolder);
    }*/
    public int addPayment(int amount){
        total = total+amount;
        return total;
    }

}
