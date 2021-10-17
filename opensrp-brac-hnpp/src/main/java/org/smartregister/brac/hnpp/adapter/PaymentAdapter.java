package org.smartregister.brac.hnpp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
    public interface OnClickAdapter{
        void onClickItem(int position);
    }
    private ArrayList<Payment> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public PaymentAdapter(Context context,OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
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
    public void onBindViewHolder(@NonNull PaymentViewHolder paymentViewHolder, int i) {
        Payment content = contentList.get(i);
        content.setConsiderChange(false);
        paymentViewHolder.numberTV.setTag(i);
        //paymentViewHolder.packageNameTV.setText(content.getServiceType() + "");

        paymentViewHolder.packageNameTV.setText(HnppConstants.targetTypeMapping.get(content.getServiceType() + ""));
        paymentViewHolder.unitPriceTV.setText(content.getUnitPrice() + "");
        paymentViewHolder.quantityTV.setText(content.getQuantity() + "");
        if(!content.isEmpty()){
            paymentViewHolder.numberTV.setText(content.getPayFor() + "");
            paymentViewHolder.priceTV.setText(content.getTotal() + " Taka");
        }else{
            paymentViewHolder.numberTV.setText("");
            paymentViewHolder.priceTV.setText("0 Taka");
        }
        content.setConsiderChange(true);

        Log.v("TEXT_EDIT","0");
        if(content.getPayFor()==0){
            paymentViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.alert_expired));
        }else{
            paymentViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        }
        if(content.isSelected()){
            paymentViewHolder.checkBox.setImageResource(R.drawable.ic_checked_f);
            paymentViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
            paymentViewHolder.numberTV.setEnabled(true);

        }else{
            paymentViewHolder.checkBox.setImageResource(R.drawable.ic_unchecked_f);
            paymentViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.alert_expired));
            paymentViewHolder.numberTV.setEnabled(false);
        }
        paymentViewHolder.numberTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.v("TEXT_EDIT","onTextChanged>>"+s);
                String value = s.toString();
                if(!TextUtils.isEmpty(value)){
                    int payFor = Integer.parseInt(value);
                    if(payFor<= content.getQuantity()){
                        paymentViewHolder.numberTV.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {



                if (content.isConsiderChange())
                {
                String value = s.toString();
                try{
                    if(!TextUtils.isEmpty(value)){
                        content.setEmpty(false);

                        int payFor = Integer.parseInt(value);
                        Log.v("TEXT_EDIT",">>>"+payFor+":quantity:"+content.getQuantity());
                        if(payFor<= content.getQuantity()){
                            Log.v("TEXT_EDIT",">>>not error"+payFor+":quantity:"+content.getQuantity());
                            content.setPayFor(payFor);
                            content.setTotal(content.getPayFor() * content.getUnitPrice());
                            onClickAdapter.onClickItem(i);
                        }else{
                            Log.v("TEXT_EDIT",">>>error"+payFor+":quantity:"+content.getQuantity());
                            paymentViewHolder.numberTV.setError("Less then quantity");
                        }
                    }else{
                        content.setEmpty(true);
                        paymentViewHolder.numberTV.setError("Empty");
                    }
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }

                }




            }
        });

        paymentViewHolder.increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!content.isSelected()) return;

                if(content.getQuantity()>content.getPayFor()){
                    content.setPayFor(content.getPayFor()+1);
                    content.setTotal(content.getPayFor() * content.getUnitPrice());
                }
                onClickAdapter.onClickItem(i);

            }
        });

        paymentViewHolder.decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!content.isSelected()) return;
                if(content.getPayFor()>0){
                    content.setPayFor(content.getPayFor()- 1);
                    content.setTotal(content.getPayFor() * content.getUnitPrice());

                }
                onClickAdapter.onClickItem(i);

            }
        });
        paymentViewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content.setSelected(!content.isSelected());
                if(!content.isSelected()){
                    content.setPayFor(0);
                    content.setTotal(0);
                }else{
                    content.setPayFor(content.getQuantity());
                    content.setTotal(content.getPayFor() * content.getUnitPrice());
                }

                onClickAdapter.onClickItem(i);
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
            if(!payment.isEmpty() && payment.getTotal()>0 && payment.isSelected()){
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
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override public int getItemViewType(int position) {
        return position;
    }

}
