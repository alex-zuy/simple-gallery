package com.example.alex.simplegallery;

import android.content.Context;
import android.content.res.Resources;
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
        setUpDurationPicker((NumberPicker) view.findViewById(R.id.numberPicker));
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
        return getContext().getResources().getInteger(R.integer.pref_sliding_interval_default);
    }

    private void setUpDurationPicker(final NumberPicker picker) {
        final Resources resources = getContext().getResources();
        picker.setMinValue(resources.getInteger(R.integer.pref_sliding_interval_min));
        picker.setMaxValue(resources.getInteger(R.integer.pref_sliding_interval_max));
        picker.setValue(this.getPersistedInt(getDefaultValue()));
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                currentValue = newVal;
            }
        });
    }
}
