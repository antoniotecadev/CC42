package com.antonioteca.cc42.utility;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class TimePickerPreference extends DialogPreference {

    private int hour;
    private int minute;

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText("OK");
        setNegativeButtonText("Cancelar");
    }

    public void setTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        persistString(formatTime(hour, minute));
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        String time = getPersistedString((String) defaultValue);
        setTime(parseHour(time), parseMinute(time));
    }

    private int parseHour(String time) {
        return Integer.parseInt(time.split(":")[0]);
    }

    private int parseMinute(String time) {
        return Integer.parseInt(time.split(":")[1]);
    }

    private String formatTime(int hour, int minute) {
        return String.format("%02d:%02d", hour, minute);
    }
}