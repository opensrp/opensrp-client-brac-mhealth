package org.smartregister.chw.core.dao;

import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.core.application.CoreChwApplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class AbstractDao {

    private static SimpleDateFormat DOB_DATE_FORMAT;
    private static SimpleDateFormat NATIVE_FORMS_DATE_FORMAT;

    public static SimpleDateFormat getDobDateFormat() {
        if (DOB_DATE_FORMAT == null)
            DOB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return DOB_DATE_FORMAT;
    }

    public static SimpleDateFormat getNativeFormsDateFormat() {
        if (NATIVE_FORMS_DATE_FORMAT == null)
            NATIVE_FORMS_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        return NATIVE_FORMS_DATE_FORMAT;
    }

    protected static void updateDB(String sql) {
        try {
            SQLiteDatabase db = CoreChwApplication.getInstance().getRepository().getWritableDatabase();
            db.rawExecSQL(sql);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /**
     * Returns a mapped pojo by reading the sqlite adapter
     * handles iteration and cursor disposable
     *
     * @param query
     * @param dataMap
     * @param <T>
     * @return
     */
    protected static <T> List<T> readData(String query, DataMap<T> dataMap) {
        Cursor cursor = null;
        try {
            List<T> list = new ArrayList<>();
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                list.add(dataMap.readCursor(cursor));
                cursor.moveToNext();
            }
            return list;
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Reads the results of a raw query and returns a list object representing all the table
     * rows and a map representing the columns
     *
     * @param query
     * @return
     */
    public static List<Map<String, String>> readData(String query, String[] selectionArgs) {
        List<Map<String, String>> list = new ArrayList<>();
        Cursor cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, selectionArgs);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Map<String, String> res = new HashMap<>();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                res.put(cursor.getColumnName(i), getCursorValue(cursor, i));
            }
            list.add(res);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    @Nullable
    protected static String getCursorValue(Cursor c, int column_index) {
        return c.getType(column_index) == Cursor.FIELD_TYPE_NULL ? null : c.getString(column_index);
    }

    @Nullable
    protected static String getCursorValue(Cursor c, String column_name) {
        return c.getType(c.getColumnIndex(column_name)) == Cursor.FIELD_TYPE_NULL ? null : c.getString(c.getColumnIndex(column_name));
    }

    @Nullable
    protected static Long getCursorLongValue(Cursor c, String column_name) {
        return c.getType(c.getColumnIndex(column_name)) == Cursor.FIELD_TYPE_NULL ? null : c.getLong(c.getColumnIndex(column_name));
    }

    public interface DataMap<T> {
        T readCursor(Cursor cursor);
    }
}
