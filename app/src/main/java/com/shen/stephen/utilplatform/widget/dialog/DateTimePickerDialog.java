package com.shen.stephen.utilplatform.widget.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.shen.stephen.utilplatform.widget.PkiActivity;
import com.shen.stephen.utilplatform.util.PkiTimeUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ChengCn on 12/22/2015.
 */
public class DateTimePickerDialog extends BaseDialog {
    /**
     * Date mode dialog.
     */
    public static final int TIME_PICKER_MODE_DATE = 0;

    /**
     * Time mode dialog.
     */
    public static final int TIME_PICKER_MODE_TIME = 1;

    /**
     * Datetime mode dialog.
     */
    public static final int TIME_PICKER_MODE_DATETIME = 2;

    private Calendar mCalendar;
    private int mMode;
    private boolean mIsfurture;

    private DialogPresentInterface.DialogButtonClickListener listener;

    public DateTimePickerDialog() {
        mCalendar = Calendar.getInstance();
    }

    /**
     * The util method to create a date time picker dialog.
     *
     * @param mode        the mode of the picker dialog. {@link #TIME_PICKER_MODE_DATE}, {@link #TIME_PICKER_MODE_DATETIME}, {@link #TIME_PICKER_MODE_TIME}
     * @param defaultTime the default time
     * @param listener    the dialog button click listener
     * @return the new instance of {@link DateTimePickerDialog}
     */
    public static DateTimePickerDialog newInstance(int mode, long defaultTime, boolean isfurture, DialogPresentInterface.DialogButtonClickListener listener) {
        DateTimePickerDialog dialog = new DateTimePickerDialog();
        dialog.mCalendar.setTimeInMillis(defaultTime);
        dialog.listener = listener;
        dialog.mMode = mode;
        dialog.mIsfurture = isfurture;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Dialog dialog;
        if (mMode == TIME_PICKER_MODE_DATE || mMode == TIME_PICKER_MODE_DATETIME) {
            dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mCalendar.set(Calendar.YEAR, year);
                    mCalendar.set(Calendar.MONTH, monthOfYear);
                    mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    if (mMode == TIME_PICKER_MODE_DATETIME) {
                        // reset tge mode value to avoid redundancy create time pick
                        mMode = TIME_PICKER_MODE_TIME;
                        ((PkiActivity) getActivity()).showFragmentDialog(DialogBuilder.buildDateTimePickDialog(mMode, mCalendar.getTimeInMillis(), mIsfurture, listener), "timePicker");
                    } else if (mMode == TIME_PICKER_MODE_DATE) {
                        PkiTimeUtil.changeToStartOfDay(mCalendar);
                        listener.onClickRightButton(null, mCalendar.getTimeInMillis());
                        dismissAllowingStateLoss();
                        mMode = -1;
                    } else {
                        dismissAllowingStateLoss();
                    }
                }
            }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));

            if (mIsfurture) {
                ((DatePickerDialog) dialog).getDatePicker().setMaxDate(System.currentTimeMillis());
            }

        } else {
            dialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    mCalendar.set(Calendar.MINUTE, minute);
                    mCalendar.set(Calendar.MILLISECOND, 0);
                    listener.onClickRightButton(null, mCalendar.getTimeInMillis());
                    dismissAllowingStateLoss();
                }
            }, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true);

        }

        return dialog;
    }
}
