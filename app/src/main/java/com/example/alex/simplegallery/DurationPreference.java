package com.example.alex.simplegallery;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class DurationPreference extends DialogPreference {

    private int currentValue;

    public DurationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.duration_preference);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.numberPicker);
        picker.setMinValue(1);
        picker.setMaxValue(60);
        picker.setValue(this.getPersistedInt(getDefaultValue()));
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                currentValue = newVal;
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistInt(currentValue);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            currentValue = this.getPersistedInt(getDefaultValue());
        } else {
            currentValue = (Integer) defaultValue;
            persistInt(currentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return getDefaultValue();
    }

    private int getDefaultValue() {
        return Integer.valueOf(getContext().getResources().getString(R.string.pref_sliding_interval_default));
    }
}
