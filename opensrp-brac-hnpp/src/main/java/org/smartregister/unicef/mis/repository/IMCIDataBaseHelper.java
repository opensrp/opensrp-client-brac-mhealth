package org.smartregister.unicef.mis.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.utils.HnppDBUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class IMCIDataBaseHelper extends SQLiteOpenHelper {
    private static IMCIDataBaseHelper sInstance;

    // ...
    public static synchronized IMCIDataBaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new IMCIDataBaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private IMCIDataBaseHelper(Context context) {
        super(context, "imci.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        copyDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public void testQuery(){
        String question = HnppDBUtils.getQuestionsByAssessmentType();
        Log.v("DB_DUMP","question>>>>>"+question);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    public  void copyDatabase() {

        Observable.create(e-> {
                    try{

                        InputStream myInput;
                        try {
                            myInput = HnppApplication.getInstance().getAssets().open("imci.db");
                            File file = new File("/data/data/"+HnppApplication.getInstance().getPackageName()+"/databases/imci.db");
                            Log.v("DB_DUMP","file>>"+file.getAbsolutePath());
                            if (!(new File(file.getAbsolutePath()).exists())) {
                                FileOutputStream fos = new FileOutputStream(file);
                                int BUFFER_SIZE = 400000;
                                byte[] buffer = new byte[BUFFER_SIZE];
                                int count = 0;
                                while ((count = myInput.read(buffer)) > 0) {
                                    fos.write(buffer, 0, count);
                                    Log.v("DB_DUMP","fos ");
                                }
                                fos.close();
                                myInput.close();
                                Log.v("DB_DUMP","DONE!!!!!!!!!!!!!!!!!!!!1");
                            }else{
                                FileOutputStream fos = new FileOutputStream(file);
                                int BUFFER_SIZE = 400000;
                                byte[] buffer = new byte[BUFFER_SIZE];
                                int count = 0;
                                while ((count = myInput.read(buffer)) > 0) {
                                    fos.write(buffer, 0, count);
                                    Log.v("DB_DUMP","fos 22");
                                }
                                fos.close();
                                myInput.close();
                                Log.v("DB_DUMP","DONE!!!!!!!!!!!!!!!!!!!!2");
                            }

                        } catch ( IOException ioException) {
                            Log.v("DB_DUMP","exception");
                            ioException.printStackTrace();
                        }
                        e.onComplete();

                    }catch (Exception ex){
                        ex.printStackTrace();
                        e.onError(ex);

                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("DB_DUMP","onError");
                    }

                    @Override
                    public void onComplete() {
                        Log.v("DB_DUMP","DONE");
                    }
                });



    }
}
