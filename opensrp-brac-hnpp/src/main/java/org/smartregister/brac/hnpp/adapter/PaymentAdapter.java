package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.PaymentActivity;
import org.smartregister.brac.hnpp.holder.NotificationViewHolder;
import org.smartregister.brac.hnpp.holder.PaymentViewHolder;
import org.smartregister.brac.hnpp.model.Notification;
import org.smartregister.brac.hnpp.model.Payment;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.PaymentDetails;

import java.util.ArrayList;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentViewHolder>{
    private ArrayList<Payment> contentList;
    private ArrayList<PaymentDetails> paymentDetailsArrayList;
    private Context context;
    int quantity, unitPrice, price;
    int total = 0; int amount = 0,totalAmount = 0;
    private PaymentActivity.listener totalListener;
    //private PaymentAdapter.OnClickAdapter onClickAdapter;

    public PaymentAdapter(Context context) {
        this.context = context;
        contentList = new ArrayList<>();
        paymentDetailsArrayList = new ArrayList<>();
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
        paymentViewHolder.packageNameTV.setText(HnppConstants.targetTypeMapping.get(content.getServiceType()+""));
        paymentViewHolder.unitPriceTV.setText(content.getUnitPrice()+"");
        paymentViewHolder.quantityTV.setText(content.getQuantity()+"");
        paymentViewHolder.numberTV.setText(content.getQuantity()+"");
 //       paymentViewHolder.priceTV.setText(Double.valueOf(paymentViewHolder.quantityTV.getText().toString())*Double.valueOf(paymentViewHolder.unitPriceTV.getText().toString())+"");
        paymentViewHolder.priceTV.setText(content.getTotal()+"");
        amount = addPayment(Integer.valueOf(paymentViewHolder.priceTV.getText().toString()));
        totalAmount = amount;
        totalListener.addsum(amount);
        totalListener.addsumpay(amount);
        paymentViewHolder.increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Integer.valueOf(paymentViewHolder.quantityTV.getText().toString()) > Integer.valueOf(paymentViewHolder.numberTV.getText().toString())){
                    int oldPrice = 0,currentPrice = 0;
                    oldPrice = Integer.valueOf(paymentViewHolder.priceTV.getText().toString());
                    quantity = Integer.valueOf(paymentViewHolder.numberTV.getText().toString());
                    quantity = quantity+1;
                    paymentViewHolder.numberTV.setText(quantity+"");
                    unitPrice = Integer.valueOf(paymentViewHolder.unitPriceTV.getText().toString());
                    price = quantity*unitPrice;
                    paymentViewHolder.priceTV.setText(price+"");
                    currentPrice = Integer.valueOf(paymentViewHolder.priceTV.getText().toString());
                    int mainPrice = currentPrice - oldPrice;
                    //notifyDataSetChanged();
                    totalAmount = plusPayment(mainPrice,totalAmount);
                    totalListener.addsumpay(totalAmount);

                        PaymentDetails paymentDetails = new PaymentDetails();
                        paymentDetails.setServiceType(content.getServiceType());
                        paymentDetails.setServiceCode(content.getServiceCode());
                        paymentDetails.setUnitPrice(unitPrice);
                        paymentDetails.setPayFor(quantity);
                        if(paymentDetails != null){
                            paymentDetailsArrayList.add(paymentDetails);
                            totalListener.getPaymentDetailsObject(paymentDetailsArrayList);
                        }


                }
            }
        });

        paymentViewHolder.decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Integer.valueOf(paymentViewHolder.numberTV.getText().toString())>0){
                    int oldPrice = 0,currentPrice = 0;
                    oldPrice = Integer.valueOf(paymentViewHolder.priceTV.getText().toString());
                    quantity = Integer.valueOf(paymentViewHolder.numberTV.getText().toString());
                    quantity = quantity-1;
                    paymentViewHolder.numberTV.setText(quantity+"");
                    unitPrice = Integer.valueOf(paymentViewHolder.unitPriceTV.getText().toString());
                    price = quantity*unitPrice;
                    paymentViewHolder.priceTV.setText(price+"");
                    currentPrice = Integer.valueOf(paymentViewHolder.priceTV.getText().toString());
                    int mainPrice = oldPrice - currentPrice;
                   // notifyDataSetChanged();
                    totalAmount = minusPayment(mainPrice,totalAmount);
                    totalListener.addsumpay(totalAmount);

                        PaymentDetails paymentDetails = new PaymentDetails();
                        paymentDetails.setServiceType(content.getServiceType());
                        paymentDetails.setServiceCode(content.getServiceCode());
                        paymentDetails.setUnitPrice(unitPrice);
                        paymentDetails.setPayFor(quantity);
                        if(paymentDetails != null){
                            paymentDetailsArrayList.add(paymentDetails);
                            totalListener.getPaymentDetailsObject(paymentDetailsArrayList);
                        }

                }

            }
        });

        if(content.getTotal() > 0){
            PaymentDetails paymentDetails = new PaymentDetails();
            paymentDetails.setServiceType(content.getServiceType());
            paymentDetails.setServiceCode(content.getServiceCode());
            paymentDetails.setUnitPrice(content.getUnitPrice());
            paymentDetails.setPayFor(content.getQuantity());
            if(paymentDetails != null){
                paymentDetailsArrayList.add(paymentDetails);
                totalListener.getPaymentDetailsObject(paymentDetailsArrayList);
            }
        }



    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public void setListener(PaymentActivity.listener listener) {
        totalListener = listener;
    }

    public int addPayment(int amount){
        total = total+amount;
        return total;
    }
    public int plusPayment(int price,int amount){
        amount = price+amount;
        return amount;
    }
    public int minusPayment(int price,int amount){
        amount = amount-price;
        return amount;
    }

}
