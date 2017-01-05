package com.omtech.testjsonimageparser.helper;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.OpenableColumns;

/**
 * Created by Omkar on 05/01/2017.
 */

public class ImageFileProvider extends android.support.v4.content.FileProvider {

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = super.query(uri, projection, selection, selectionArgs, sortOrder);
        Cursor override = copyCursorWithChangedDisplayName((MatrixCursor)cursor);
        cursor.close();
        return override;
    }
    private MatrixCursor copyCursorWithChangedDisplayName(MatrixCursor original) {
        MatrixCursor override = new MatrixCursor(original.getColumnNames(), original.getCount()/* == 1 */);
        if (original.moveToFirst()) {
            override.addRow(copyCurrentRowWithChangedDisplayName(original));
        }
        return override;
    }
    /** Based on super.query() */
    private Object[] copyCurrentRowWithChangedDisplayName(MatrixCursor original) {
        String[] columns = original.getColumnNames();
        Object[] row = new Object[columns.length];
        int i = 0;
        for (String column : columns) {
            int columnIndex = original.getColumnIndexOrThrow(column);
            Object value = null;
            if (OpenableColumns.DISPLAY_NAME.equals(column)) {
                value = replaceExtension(original.getString(columnIndex), ".jpg");
            } else if (OpenableColumns.SIZE.equals(column)) {
                value = original.getLong(columnIndex);
            }
            row[i++] = value;
        }
        return row;
    }

    private String replaceExtension(String name, String ext) {
        if (!name.endsWith(ext)) {
            name = name + ext;
        }
        return name;
    }
    @Override public String getType(Uri uri) { return "image/jpeg";}
}
