package org.smartregister.unicef.dghs.activity;


import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.smartregister.CoreLibrary;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.location.HALocation;
import org.smartregister.unicef.dghs.model.TikaInfoModel;
import org.smartregister.unicef.dghs.model.VaacineInfo;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.view.activity.SecuredActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import id.zelory.compressor.FileUtil;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class TikaCardViewActivity extends SecuredActivity {
    public static String PUT_EXTRA_COMES = "is_comes_from";
    private int STORAGE_PERMISSION_CODE = 122;
    TikaInfoModel tikaInfoModel;
    String baseEntityId;
    boolean isComesFromGuest = false;
    ImageView qrcodeImage;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_tika_card);
        baseEntityId = getIntent().getStringExtra("BASE_ENTITY_ID");
        isComesFromGuest = getIntent().getBooleanExtra(PUT_EXTRA_COMES,false);
//        tikaInfoModel = isComesFromGuest?HnppDBUtils.getTikaDetailsForGuestProfike(baseEntityId):HnppDBUtils.getTikaDetails(baseEntityId);
        qrcodeImage = findViewById(R.id.qrcodeImage);
        //if(tikaInfoModel == null){
            tikaInfoModel = HnppDBUtils.getTikaDetailsFromClient(baseEntityId);
        //}
        ((TextView) findViewById(R.id.nameTxt)).setText(tikaInfoModel.name);
        ((TextView) findViewById(R.id.dayTxt)).setText(tikaInfoModel.birthDay);
        ((TextView) findViewById(R.id.monthTxt)).setText(tikaInfoModel.birthMonth);
        ((TextView) findViewById(R.id.yearTxt)).setText(tikaInfoModel.birthYear);
        ((TextView) findViewById(R.id.registrationNoTxt)).setText(tikaInfoModel.registrationNo);
        ((TextView) findViewById(R.id.registrationDateTxt)).setText(tikaInfoModel.registrationDate);
        ((TextView) findViewById(R.id.bridTxt)).setText(tikaInfoModel.brid);
        ((TextView) findViewById(R.id.montheNameTxt)).setText(tikaInfoModel.motherName);
        ((TextView) findViewById(R.id.fatherNameTxt)).setText(tikaInfoModel.fatherName);
        ((TextView) findViewById(R.id.houseNoTxt)).setText(tikaInfoModel.houseHoldNo);
        ((TextView) findViewById(R.id.villageTxt)).setText(tikaInfoModel.village);
        ((TextView) findViewById(R.id.upazilaTxt)).setText(tikaInfoModel.upazilla);
        ((TextView) findViewById(R.id.districtTxt)).setText(tikaInfoModel.district);
        ((TextView) findViewById(R.id.unionTxt)).setText(tikaInfoModel.union);
        ((TextView) findViewById(R.id.wardTxt)).setText(tikaInfoModel.wardNo);
        ((TextView) findViewById(R.id.centerNameTxt)).setText(tikaInfoModel.centerName);
        ((TextView) findViewById(R.id.subBlockTxt)).setText(tikaInfoModel.subBlock);
        ((TextView) findViewById(R.id.genderTxt)).setText(tikaInfoModel.gender);
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);
        ((TextView) findViewById(R.id.haNameTxt)).setText(fullName);
        //updateDistrict();
        updateTikaDate();
        updateVaccineDate();
        findViewById(R.id.download_pdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        STORAGE_PERMISSION_CODE);
            }
        });
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        generateQrcode();

    }
    private void generateQrcode(){
        String baseUrl = CoreLibrary.getInstance().context().
                configuration().dristhiBaseURL().replace("/opensrp/","");
        //base_entity,registrationId,division,district,dob,brid,gender,http://unicef-ha.mpower-social.com/opensrp-dashboard/epi-card.html
        String text =baseEntityId+","+tikaInfoModel.registrationNo+","+tikaInfoModel.divisionId+","+tikaInfoModel.districtId+","+tikaInfoModel.dob+","+tikaInfoModel.genderEnglish+", "+baseUrl+"/opensrp-dashboard/view-epi-card.html?id="+baseEntityId;
        try {
            Log.v("SCANNER","scan result:"+text);
            qrcodeImage.setImageBitmap(generateQRCode(text,700));
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    private Bitmap generateQRCode(String text, int size) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }
//    private void updateDistrict(){
//        if(isComesFromGuest){
//            ((TextView) findViewById(R.id.districtTxt)).setText(tikaInfoModel.district);
//            ((TextView) findViewById(R.id.upazilaTxt)).setText(tikaInfoModel.upazilla);
//        }else{
//            try{
//                HALocation haLocation = HnppApplication.getHALocationRepository().getLocationByBaseEntityId(baseEntityId);
//                ((TextView) findViewById(R.id.districtTxt)).setText(haLocation.district.name);
//                ((TextView) findViewById(R.id.upazilaTxt)).setText(haLocation.upazila.name);
//                tikaInfoModel.divisionId = haLocation.division.id+"";
//                tikaInfoModel.districtId = haLocation.district.id+"";
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//
//        }
//
//    }
    private void updateTikaDate(){

        DateTime donDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(tikaInfoModel.dob);
        ((TextView) findViewById(R.id.atBirthTikaGettingDate)).setText(tikaInfoModel.dob);
        LocalDate sixweek = new LocalDate(donDate);
        LocalDate sixWeekV = sixweek.plusDays(42);
        String s = DateTimeFormat.forPattern("yyyy-MM-dd").print(sixWeekV);
        ((TextView) findViewById(R.id.sixWeekDate)).setText(s);
        LocalDate tenWeekV = sixweek.plusDays(28);
//        String t = DateTimeFormat.forPattern("yyyy-MM-dd").print(tenWeekV);
//        ((TextView) findViewById(R.id.tenWeekDate)).setText(t);
        LocalDate fourTeenWeekV = tenWeekV.plusDays(28);
//        String f = DateTimeFormat.forPattern("yyyy-MM-dd").print(fourTeenWeekV);
        //((TextView) findViewById(R.id.fourteenWeekDate)).setText(f);
        LocalDate nineMonthV = sixweek.plusDays(274);
        String n = DateTimeFormat.forPattern("yyyy-MM-dd").print(nineMonthV);
        ((TextView) findViewById(R.id.nineMonthDate)).setText(n);
        LocalDate eighteenMonthV = sixweek.plusDays(455);
        String e = DateTimeFormat.forPattern("yyyy-MM-dd").print(eighteenMonthV);
        ((TextView) findViewById(R.id.eighteenMonthDate)).setText(e);
    }
    private void updateVaccineDate(){
        ArrayList<VaacineInfo> vaacineInfos = HnppDBUtils.getVaccineInfo(baseEntityId);
        for (VaacineInfo vaacineInfo:vaacineInfos){
            switch (vaacineInfo.vaccineName){
                case "bcg":
                    ((TextView) findViewById(R.id.bcg_1)).setText(vaacineInfo.vaccineDate);
                    break;
                case "opv_0":
                    //((TextView) findViewById(R.id.opv_1)).setText(vaacineInfo.vaccineDate);
                    break;
                case "opv_1":
                    ((TextView) findViewById(R.id.opv_1)).setText(vaacineInfo.vaccineDate);
                    if(vaacineInfo.invalid==0){
                        DateTime donDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(vaacineInfo.vaccineDate);
                        LocalDate sixWeekV = new LocalDate(donDate);
//                    String s = DateTimeFormat.forPattern("yyyy-MM-dd").print(sixWeekV);
//                    ((TextView) findViewById(R.id.sixWeekDate)).setText(s);
                        //update tenweek
                        LocalDate tenWeekV = sixWeekV.plusDays(28);
                        String t = DateTimeFormat.forPattern("yyyy-MM-dd").print(tenWeekV);
                        ((TextView) findViewById(R.id.tenWeekDate)).setText(t);
                        ((TextView) findViewById(R.id.tenWeekDate)).setBackgroundResource(R.drawable.table_bg);
                    }

                    break;
                case "opv_2":
                    ((TextView) findViewById(R.id.opv_2)).setText(vaacineInfo.vaccineDate);
                    if(vaacineInfo.invalid==0){
                        DateTime d = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(vaacineInfo.vaccineDate);
                        LocalDate fourTeenWeekV = new LocalDate(d);
                        LocalDate fourteenWeekDate = fourTeenWeekV.plusDays(28);
                        String f = DateTimeFormat.forPattern("yyyy-MM-dd").print(fourteenWeekDate);
                        ((TextView) findViewById(R.id.fourteenWeekDate)).setText(f);
                        ((TextView) findViewById(R.id.fourteenWeekDate)).setBackgroundResource(R.drawable.table_bg);
                    }

                    break;
                case "opv_3":
                    ((TextView) findViewById(R.id.opv_3)).setText(vaacineInfo.vaccineDate);
                    break;
                case "penta_1":
                    ((TextView) findViewById(R.id.penta_1)).setText(vaacineInfo.vaccineDate);
                    if(vaacineInfo.invalid==0){
                        DateTime donDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(vaacineInfo.vaccineDate);
                        LocalDate sixWeekV = new LocalDate(donDate);
//                    String s = DateTimeFormat.forPattern("yyyy-MM-dd").print(sixWeekV);
//                    ((TextView) findViewById(R.id.sixWeekDate)).setText(s);
                        //update tenweek
                        LocalDate tenWeekV = sixWeekV.plusDays(28);
                        String t = DateTimeFormat.forPattern("yyyy-MM-dd").print(tenWeekV);
                        ((TextView) findViewById(R.id.tenWeekDate)).setText(t);
                        ((TextView) findViewById(R.id.tenWeekDate)).setBackgroundResource(R.drawable.table_bg);
                    }
                    break;
                case "penta_2":
                    ((TextView) findViewById(R.id.penta_2)).setText(vaacineInfo.vaccineDate);
                    if(vaacineInfo.invalid==0){
                        DateTime d = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(vaacineInfo.vaccineDate);
                        LocalDate fourTeenWeekV = new LocalDate(d);
                        LocalDate fourteenWeekDate = fourTeenWeekV.plusDays(28);
                        String f = DateTimeFormat.forPattern("yyyy-MM-dd").print(fourteenWeekDate);
                        ((TextView) findViewById(R.id.fourteenWeekDate)).setText(f);
                        ((TextView) findViewById(R.id.fourteenWeekDate)).setBackgroundResource(R.drawable.table_bg);
                    }
                    break;
                case "penta_3":
                    ((TextView) findViewById(R.id.penta_3)).setText(vaacineInfo.vaccineDate);
                    break;
                case "fipv_1":
                    ((TextView) findViewById(R.id.fipv_1)).setText(vaacineInfo.vaccineDate);
                    if(vaacineInfo.invalid==0){
                        DateTime donDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(vaacineInfo.vaccineDate);
                        LocalDate sixWeekV = new LocalDate(donDate);
//                    String s = DateTimeFormat.forPattern("yyyy-MM-dd").print(sixWeekV);
//                    ((TextView) findViewById(R.id.sixWeekDate)).setText(s);
                        //update tenweek
                        LocalDate tenWeekV = sixWeekV.plusDays(28);
                        String t = DateTimeFormat.forPattern("yyyy-MM-dd").print(tenWeekV);
                        ((TextView) findViewById(R.id.tenWeekDate)).setText(t);
                        ((TextView) findViewById(R.id.tenWeekDate)).setBackgroundResource(R.drawable.table_bg);
                    }
                    break;
                case "fipv_2":
                    ((TextView) findViewById(R.id.fipv_3)).setText(vaacineInfo.vaccineDate);
                    break;
                case "fipv_3":
                    ((TextView) findViewById(R.id.fipv_3)).setText(vaacineInfo.vaccineDate);
                    break;
                case "pcv_1":
                    ((TextView) findViewById(R.id.pcv_1)).setText(vaacineInfo.vaccineDate);
                    if(vaacineInfo.invalid==0){
                        DateTime donDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(vaacineInfo.vaccineDate);
                        LocalDate sixWeekV = new LocalDate(donDate);
//                    String s = DateTimeFormat.forPattern("yyyy-MM-dd").print(sixWeekV);
//                    ((TextView) findViewById(R.id.sixWeekDate)).setText(s);
                        //update tenweek
                        LocalDate tenWeekV = sixWeekV.plusDays(28);
                        String t = DateTimeFormat.forPattern("yyyy-MM-dd").print(tenWeekV);
                        ((TextView) findViewById(R.id.tenWeekDate)).setText(t);
                        ((TextView) findViewById(R.id.tenWeekDate)).setBackgroundResource(R.drawable.table_bg);
                    }
                    break;
                case "pcv_2":
                    ((TextView) findViewById(R.id.pcv_2)).setText(vaacineInfo.vaccineDate);
                    if(vaacineInfo.invalid==0){
                        DateTime d = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(vaacineInfo.vaccineDate);
                        LocalDate fourTeenWeekV = new LocalDate(d);
                        LocalDate fourteenWeekDate = fourTeenWeekV.plusDays(28);
                        String f = DateTimeFormat.forPattern("yyyy-MM-dd").print(fourteenWeekDate);
                        ((TextView) findViewById(R.id.fourteenWeekDate)).setText(f);
                        ((TextView) findViewById(R.id.fourteenWeekDate)).setBackgroundResource(R.drawable.table_bg);
                    }
                    break;
                case "pcv_3":
                    ((TextView) findViewById(R.id.pcv_3)).setText(vaacineInfo.vaccineDate);
                    break;
                case "mr_1":
                    ((TextView) findViewById(R.id.textView43)).setText(vaacineInfo.vaccineDate);
                    DateTime dob = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(tikaInfoModel.dob);
                    LocalDate dobL = new LocalDate(dob);
                    DateTime dV = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(vaacineInfo.vaccineDate);
                    LocalDate dVL = new LocalDate(dV);
                    if(dVL.toDate().getTime()>dobL.plusDays(390).toDate().getTime()){
                        LocalDate mr2Date  = dVL.plusDays(30);
                        String ff = DateTimeFormat.forPattern("yyyy-MM-dd").print(mr2Date);
                        ((TextView) findViewById(R.id.eighteenMonthDate)).setText(ff);
                        ((TextView) findViewById(R.id.eighteenMonthDate)).setBackgroundResource(R.drawable.table_bg);
                    }
                    break;

                case "mr_2":
                    ((TextView) findViewById(R.id.textView50)).setText(vaacineInfo.vaccineDate);
                    break;

            }
        }


    }

    @Override
    protected void onResumption() {

    }
    private  void openPDF(String pdfFilePath) {
       try{
           File file = new File(pdfFilePath);
           Uri uri = FileProvider.getUriForFile(this,"org.smartregister.unicef.dghs.fileprovider",file);

           Intent intent = new Intent(Intent.ACTION_VIEW);
           intent.setDataAndType(uri, "application/pdf");
           intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
           startActivity(intent);
       }catch (Exception e){

       }
    }
    private void openImage(String imageFilePath) {
        try{
            File file = new File(imageFilePath);
            Uri uri = FileProvider.getUriForFile(this,"org.smartregister.unicef.dghs.fileprovider",file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/*");
            intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(intent);
        }catch (Exception e){

        }
    }
    private void takeScreenShot()
    {
        ArrayList<File> files = new ArrayList<>();
        for(int i =0;i<2;i++){
            View view = null;
            if(i == 0){
                view = findViewById(R.id.tika_card_view_1);
            }
            if(i==1){
                view = findViewById(R.id.tika_card_view_2);
            }
            Bitmap b = getBitmapFromView(view);

            //Save bitmap

            String filePath = getExternalFilesDir(null) + "/vaccine";
            File file = new File(filePath);
            if(!file.exists()){
                file.mkdir();
            }
            String pdfFilePath = (file.getAbsolutePath() + "/"+ "vaccine_card+"+i+".jpg");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(pdfFilePath);
                b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                //MediaStore.Images.Media.insertImage(getContentResolver(), b, "Screen", "screen");
                files.add(new File(pdfFilePath));
//                openPDF(pdfFilePath);
            }catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        createPdf(this,files);



    }
    private void createPdf(Context context, ArrayList<File> data){
        File pdfFile = new File(context.getExternalCacheDir().getAbsolutePath() + File.separator + "Tika_card_"+System.currentTimeMillis()+".pdf");
        new AsyncTask<Void, Void, Void>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            protected Void doInBackground(Void... voids) {
                PdfDocument document = new PdfDocument();
                try {
                    for(File item:data) {
                        Bitmap bitmap = BitmapFactory.decodeFile(item.getAbsolutePath());
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
                        PdfDocument.Page page = document.startPage(pageInfo);
                        Canvas canvas = page.getCanvas();
                        Paint paint = new Paint();
                        paint.setColor(Color.parseColor("#ffffff"));
                        canvas.drawPaint(paint);
                        canvas.drawBitmap(bitmap, 0, 0, null);
                        document.finishPage(page);
                    }
                    document.writeTo(new FileOutputStream(pdfFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    document.close();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                hideProgressDialog();
                if(pdfFile.exists() && pdfFile.length()>0) {
                    openPDF(pdfFile.getAbsolutePath());
                }else {
                    Toast.makeText(context, "Something went wrong creating the PDF :(", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();

    }
    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(TikaCardViewActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(TikaCardViewActivity.this, new String[] { permission }, requestCode);
        }
        else {
            showProgressDialog("টিকা কার্ডটি বানানো হচ্ছে ....");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    takeScreenShot();
                }
            },2000);
        }
    }
    private ProgressDialog dialog;

    private void showProgressDialog(String text) {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage(text);
            dialog.setCancelable(false);
            dialog.show();
        }

    }

    private void hideProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


         if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showProgressDialog("টিকা কার্ডটি বানানো হচ্ছে ....");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        takeScreenShot();
                    }
                },2000);
            }
            else {
                Toast.makeText(TikaCardViewActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
