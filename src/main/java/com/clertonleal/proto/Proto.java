package com.clertonleal.proto;

import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Proto {

    private static final String TAG = Proto.class.getName();
    private static final int INVALID_FIELD = -1;

    public static <T> T object(Cursor cursor, Class<T> clazz) {
        try {
            final T instance = clazz.newInstance();
            for(Field field : getFields(clazz)) {
                setFieldInObject(instance, field, cursor);
            }

            return instance;
        } catch (InstantiationException e) {
            Log.e(TAG, "Error to instantiate a object of " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "The default constructor of " + clazz.getName() + " is not visible", e);
        }

        return null;
    }

    public static <T> List<T> list(Cursor cursor, Class<T> clazz) {
        final List<T> list =  new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                final T instance = object(cursor, clazz);
                if (instance != null) {
                    list.add(instance);
                }
            } while (cursor.moveToNext());

        }

        return list;
    }

    private static void setFieldInObject(Object o, Field field, Cursor cursor) {
        String columnName = field.getName();

        final DatabaseField annotation = field.getAnnotation(DatabaseField.class);
        if (annotation != null) {
            annotation.columnName();
        }

        final int fieldIndex = cursor.getColumnIndex(columnName);
        if (fieldIndex == INVALID_FIELD) {
            Log.e(TAG, "Dot find column to to field: " + field.getName());
            return;
        }

        Object result = null;
        final Class clazz = field.getType();
        if (clazz == Integer.class || clazz == Integer.TYPE) {
            result = cursor.getInt(fieldIndex);
        } else if (clazz == Long.class || clazz == Long.TYPE) {
            result = cursor.getLong(fieldIndex);
        } else if (clazz == Double.class || clazz == Double.TYPE) {
            result = cursor.getDouble(fieldIndex);
        } else if (clazz == String.class) {
            result = cursor.getString(fieldIndex);
        } else if (clazz == Boolean.class || clazz == Boolean.TYPE) {
            result = cursor.getInt(fieldIndex) > 0;
        } else if (clazz == Date.class) {
            result = new Date(cursor.getLong(fieldIndex));
        }

        if (result != null) {
            field.setAccessible(true);
            try {
                field.set(o, result);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Error to set value: " + result + " in field: " + field.getName(), e);
            }
        }
    }

    private static List<Field> getFields(Class clazz) {
        Class actualClass = clazz;
        final List<Field> fields = new ArrayList<>();
        do {
            fields.addAll(Arrays.asList(actualClass.getDeclaredFields()));
            actualClass = actualClass.getSuperclass();
        } while (actualClass != null);

        return fields;
    }

}