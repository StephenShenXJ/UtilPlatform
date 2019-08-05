package com.shen.stephen.utilplatform.util;

import android.util.Log;
import android.util.SparseArray;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OSTimeUtil {
    /**
     * The number of hours in a day.
     */
    public static final short HOURS_PER_DAY = 24;

    /**
     * The number of moths in a year.
     */
    public static final int MONTH_PER_YEAR = 12;

    /**
     * The number of milliseconds in a day.
     */
    public static final int MILLIS_IN_DAY = 86400000;

    /**
     * The number of milliseconds in a hour.
     */
    public static final int MILLIS_IN_HOUR = 3600000;

    /**
     * The number of day in a week.
     */
    private static final int DAYS_PER_WEEK = 7;

    /**
     * The number of milliseconds in a week.
     */
    private static final long MILLIS_IN_WEEK = DAYS_PER_WEEK * MILLIS_IN_DAY;

    /**
     * check whether the two date format string is represent same date.
     * <B>Note:</B> the format of the string is "MM/dd/yyyy",
     * "mm/dd/yyyy hh:MM:ss".
     *
     * @param dateStr1
     * the date format string,
     * @param dateStr2
     * the other date format string
     * @return true if is same format, otherwise false.
     */
    private final static String PATTERN = "GyMdkHmsSEDFwWahKzZLc";
    private final static String REG_EXP_PATTERN_AFTER = "[^GyMdkHmsSEDFwWahKzZLc\\-/\\.]{0,}";
    private final static String REG_EXP_PATTERN_BEFORE = "[^GyMdkHmsSEDFwWahKzZLc\\-/\\.]{0,}";
    private final static String REG_EXP_PATTERN_SEPARATOR = "[/\\-\\.]{2}"; //separator
    private final static Calendar mTempCalendar = Calendar.getInstance();

    public static boolean isSameDate(String dateStr1, String dateStr2) {
        if (StrUtil.isEmpty(dateStr1) && StrUtil.isEmpty(dateStr2)) {
            return true;
        } else if (StrUtil.isEmpty(dateStr1) || StrUtil.isEmpty(dateStr2)) {
            return false;
        } else if (dateStr1.equals(dateStr2)) {
            return true;
        }

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        try {
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTime(format.parse(dateStr1));
            c2.setTime(format.parse(dateStr2));
            return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                    && (c1.get(Calendar.DAY_OF_YEAR) == c2
                    .get(Calendar.DAY_OF_YEAR));
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("OsTimeUtil","Parse string to date error, " + e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * Format the date to the specified format string.
     *
     * @param date      the UTC date time as long.
     * @param formatter the formatter like "yyyyMMdd hh:mm:ss"
     * @return the formatted date String
     */
    public static String formatDate(long date, String formatter) {
        if (StrUtil.isEmpty(formatter) || date <= 0) {
            return StrUtil.EMPTYSTRING;
        }

        Date d = new Date(date);
        SimpleDateFormat format = new SimpleDateFormat(formatter,
                Locale.getDefault());
        return format.format(d);
    }

    private static SimpleDateFormat customizeDateFormat(String template) {
        if (template == null) {
            return (SimpleDateFormat) DateFormat.getDateTimeInstance();
        }
        SparseArray<String> array = new SparseArray<String>();
        StringBuilder builder = new StringBuilder();
        SimpleDateFormat localeFormat;
        int length = template.length();
        char last = 0, next;
        for (int i = 0; i < length; i++) {
            next = template.charAt(i);
            if (last == 0) {
                last = next;
                builder.append(last);
            } else if (last == next) {
                if (builder.toString().length() < 4) {
                    builder.append(next);
                }
            } else {
                genCharArray(array, builder.toString(), last);
                last = next;
                builder = new StringBuilder();
                builder.append(last);
            }
        }
        genCharArray(array, builder.toString(), last);

        String mouthField = array.get(DateFormat.MONTH_FIELD);
        if (mouthField != null) {
            localeFormat = genLocaleFormat(mouthField.length(), array.get(PATTERN.indexOf('E')) != null);
        } else {
            localeFormat = genLocaleFormat(4, false);
        }
        String templateTmp = localeFormat.toPattern();
        templateTmp = customizeTemplate(templateTmp, 'y', array);
        templateTmp = customizeTemplate(templateTmp, 'M', array);
        templateTmp = customizeTemplate(templateTmp, 'd', array);

        String component = array.get(DateFormat.HOUR0_FIELD);
        if (!StrUtil.isEmpty(component)) {
            templateTmp += " ";
            templateTmp += component;

        } else {
            component = array.get(DateFormat.HOUR1_FIELD);
            if (!StrUtil.isEmpty(component)) {
                templateTmp += " ";
                templateTmp += component;

            }
        }

        component = array.get(DateFormat.MINUTE_FIELD);
        if (!StrUtil.isEmpty(component)) {
            templateTmp += ":";
            templateTmp += component;

        }

        component = array.get(DateFormat.SECOND_FIELD);
        if (!StrUtil.isEmpty(component)) {
            templateTmp += ":";
            templateTmp += component;
        }

        component = array.get(DateFormat.MILLISECOND_FIELD);
        if (!StrUtil.isEmpty(component)) {
            templateTmp += " ";
            templateTmp += component;
        }

        component = array.get(DateFormat.AM_PM_FIELD);
        if (!StrUtil.isEmpty(component)) {
            templateTmp += " ";
            templateTmp += component;
        }

        component = array.get(DateFormat.TIMEZONE_FIELD);
        if (!StrUtil.isEmpty(component)) {
            templateTmp += " ";
            templateTmp += component;
        } else {
            component = array.get(PATTERN.indexOf('Z'));
            if (!StrUtil.isEmpty(component)) {
                templateTmp += " ";
                templateTmp += component;
            }
        }

        localeFormat.applyPattern(templateTmp);
        return localeFormat;
    }

    private static void genCharArray(SparseArray<String> array, String value,
                                     char c) {
        int index = PATTERN.indexOf(c);
        if (index == -1) {
            return;
        }
        if (index == DateFormat.HOUR0_FIELD || index == DateFormat.HOUR1_FIELD
                || index == DateFormat.HOUR_OF_DAY0_FIELD
                || index == DateFormat.HOUR_OF_DAY1_FIELD) {
            index = DateFormat.HOUR0_FIELD;
        }
        array.put(index, value);
    }

    private static SimpleDateFormat genLocaleFormat(int countMouth, boolean hasWeek) {
        SimpleDateFormat localeTemplate = null;
        int dateType = DateFormat.DEFAULT;
        if (hasWeek) {
            dateType = DateFormat.FULL;
        } else if (countMouth == 3) {
            dateType = DateFormat.MEDIUM;
        } else if (countMouth == 4) {
            dateType = DateFormat.LONG;
        } else {
            dateType = DateFormat.SHORT;
        }
        localeTemplate = (SimpleDateFormat) DateFormat.getDateInstance(
                dateType, Locale.getDefault());
        return localeTemplate;
    }

    private static String customizeTemplate(String template, char c,
                                            SparseArray<String> array) {

        String regExpHead;
        String regExp;
        int index = PATTERN.indexOf(c);
        String value = array.get(index);
        if (value != null) {
            regExp = c + "+";
            template = template.replaceAll(regExp, value);
        } else if (value == null) {
            regExpHead = c + "+";
            regExp = REG_EXP_PATTERN_BEFORE + regExpHead
                    + REG_EXP_PATTERN_AFTER;
            template = template.replaceAll(regExp, "");
            template = tweakTemplate(template);
        }
        return template;
    }

    /**
     * Do some tweak for the template. for example trim the separator that start or end of the template. e.g: tweak /MM/yyyy to MM/yyyy or dd//yyyy to dd/yyyy.
     *
     * @param template the time format template.
     * @return the correct template.
     */
    private static String tweakTemplate(String template) {
        String separator = getTimeSeparator(template);
        template = template.replaceAll(REG_EXP_PATTERN_SEPARATOR, separator);

        // Replace the start separator
        template = template.replaceAll("^" + separator, "");


        // Replace the end separator
        template = template.replaceAll(separator + "$", "");
        return template;
    }

    private static String getTimeSeparator(String template) {
        String result = StrUtil.EMPTYSTRING;
        Pattern pattern = Pattern.compile("[/\\-\\.]");
        Matcher matcher = pattern.matcher(template);
        if (matcher.find()) {
            result = matcher.group(0);
        }
        return result;
    }

    public static String formatLocaleDateTime(String format, long date) {
        return customizeDateFormat(format).format(date);
    }

    /**
     * Convert utc time to the specify formatter string
     *
     * @param localTime the local time in millisecond
     * @param formatter the formatter
     * @return the formatted date String.
     */
    public static String formatLocalDate(long localTime, String formatter) {
        long time = localTime - TimeZone.getDefault().getRawOffset();
        return formatDate(time, formatter);
    }

    /**
     * convert the utc time to local time.
     *
     * @param utcTime the time in millisecond
     */
    public static long getLocalTime(long utcTime) {
        return utcTime + TimeZone.getDefault().getRawOffset();
    }

    /**
     * Get utc time in millisecond.
     */
    public static long getUtcTime() {
        return System.currentTimeMillis();
    }

    /**
     * Get utc time in millisecond.
     *
     * @param localTime the local time in millisecond.
     */
    public static long getUtcTime(long localTime) {
        return localTime - TimeZone.getDefault().getRawOffset();
    }

    /**
     * Check the specify time whether is today or not.
     *
     * @param timeInMillis the time in millisecond
     * @param isUtc        Specify whether the time is utc time.
     * @return true if in today, otherwise false.
     */
    public static boolean isToday(long timeInMillis, boolean isUtc) {
        Calendar today = Calendar.getInstance();
        today.setTime(new Date(isUtc ? timeInMillis : getUtcTime(timeInMillis)));
        return isToday(today);
    }

    /**
     * Check the specify time whether is today or not.
     *
     * @param date the time in millisecond
     * @return true if in today, otherwise false.
     */
    public static boolean isToday(Calendar date) {
        mTempCalendar.setTimeInMillis(System.currentTimeMillis());
        return date.get(Calendar.YEAR) == mTempCalendar.get(Calendar.YEAR)
                && date.get(Calendar.DAY_OF_YEAR) == mTempCalendar
                .get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isCurrentYear(long timeInMillis, boolean isUtc) {
        mTempCalendar.setTimeInMillis(System.currentTimeMillis());
        int currentYear = mTempCalendar.get(Calendar.YEAR);
        mTempCalendar.setTime(new Date(isUtc ? timeInMillis
                : getUtcTime(timeInMillis)));
        int mYear = mTempCalendar.get(Calendar.YEAR);
        return currentYear == mYear;
    }

    /**
     * Format a formatted date time string to the other specified formatter date
     * time string.
     *
     * @param inputFormat  the input formatter.
     * @param outputFormat the output formatter.
     * @param inputDate    the input formatted date time string.
     * @return the formatted date time string that match the specified output
     * formatter, "" if the input formatted date time string is not
     * match the specified input formatter.
     */
    public static String formateDateFromstring(String inputFormat,
                                               String outputFormat, String inputDate) {

        Date parsed = null;
        String outputDate = "";

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat,
                Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat,
                Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return outputDate;
    }

    /**
     * Parse a date format string to date
     *
     * @param dateStr the format date string
     * @return the parsed date. if the format string is not invalidate return null.
     */
    public static Date parseDate(String dateStr) {
        if (StrUtil.isEmpty(dateStr)) {
            return null;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            final Date parsedDate = formatter.parse(dateStr);
            return parsedDate;
        } catch (ParseException e) {
            Log.w("Date: ", dateStr + " not in validate format");
            return null;
        }
    }

    /**
     * @return the number of the month from <code>mMinDate</code> to <code>mMaxDate</code>
     */
    public static int getMonthsSinceDate(Calendar startDate, Calendar endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        int year = startDate.get(Calendar.YEAR);
        int month = startDate.get(Calendar.MONTH);

        int year1 = endDate.get(Calendar.YEAR);
        int month1 = endDate.get(Calendar.MONTH);

        int result;
        if (year == year1) {
            result = month1 - month;
        } else {
            result = MONTH_PER_YEAR * (year1 - year) + month1 - month;
        }

        return result;
    }

    /**
     * @return Returns the number of weeks between the current <code>date</code>
     * and the <code>mMinDate</code>.
     */
    public static int getWeeksSinceDate(Calendar startDate, Calendar endDate, int firstDayOfWeek) {
        long endTimeMillis = endDate.getTimeInMillis()
                + endDate.getTimeZone().getOffset(endDate.getTimeInMillis());
        long startTimeMillis = startDate.getTimeInMillis()
                + startDate.getTimeZone().getOffset(startDate.getTimeInMillis());
        long dayOffsetMillis = ((long) (startDate.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek)
                * MILLIS_IN_DAY);
        return (int) ((endTimeMillis - startTimeMillis + dayOffsetMillis) / MILLIS_IN_WEEK);
    }

    /**
     * @return Returns the number of weeks between the current <code>date</code>
     * and the <code>mMinDate</code>.
     */
    public static int getDaysSinceDate(Calendar startDate, Calendar endDate) {
        mTempCalendar.setTimeInMillis(endDate.getTimeInMillis());
        mTempCalendar.add(Calendar.DAY_OF_MONTH, 1);
        changeToStartOfDay(mTempCalendar);
        long endTimeMillis = mTempCalendar.getTimeInMillis()
                + endDate.getTimeZone().getOffset(endDate.getTimeInMillis());

        mTempCalendar.setTimeInMillis(startDate.getTimeInMillis());
        changeToStartOfDay(mTempCalendar);
        long startTimeMillis = mTempCalendar.getTimeInMillis()
                + startDate.getTimeZone().getOffset(startDate.getTimeInMillis());

        return (int) ((endTimeMillis - startTimeMillis) / MILLIS_IN_DAY);
    }

    /**
     * Check the two date whether is in same day.
     *
     * @param l the one of the date
     * @param r the other date
     * @return true if the two date is in same day. otherwise false.
     */
    public static boolean isSameDay(Calendar l, Calendar r) {
        if (l == null || r == null) {
            return false;
        }

        return l.get(Calendar.YEAR) == r.get(Calendar.YEAR)
                && l.get(Calendar.MONTH) == r.get(Calendar.MONTH)
                && l.get(Calendar.DAY_OF_MONTH) == r.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Change the specified date time to start of the day.
     *
     * @param date
     */
    public static void changeToStartOfDay(Calendar date) {
        if (date == null) {
            return;
        }
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Check the specified time whether is start of a day.
     *
     * @param timeInMillis the time in millisecond
     * @return true if it is the start of a day, otherwise false.
     */
    public static boolean isStartOfDay(long timeInMillis) {
        mTempCalendar.setTimeInMillis(timeInMillis);
        return mTempCalendar.get(Calendar.HOUR_OF_DAY) == 0 && mTempCalendar.get(Calendar.MINUTE) == 0 && mTempCalendar.get(Calendar.SECOND) == 0 && mTempCalendar.get(Calendar.MILLISECOND) == 0;
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    /**
     * Change the specified time to the start of month.
     *
     * @param time         the time in millisecond
     * @param tempCalendar the temp calendar.
     * @return the time that start of month of the specified time.
     */
    public static long changeTime2StartOfMonth(long time, Calendar tempCalendar) {
        tempCalendar.setTimeInMillis(time);
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        OSTimeUtil.changeToStartOfDay(tempCalendar);
        return tempCalendar.getTimeInMillis();
    }

    /**
     * Get the time that 2 months after of before the specified time.
     *
     * @param time         the time in millisecond.
     * @param isAfter      indicate that is after or before, true is after, false for before.
     * @param tempCalendar the temp calendar.
     * @return the time that 2 months after of before the specified time.
     */
    public static long get2MonthTimeAfterOrBefore(long time, boolean isAfter, Calendar tempCalendar) {
        tempCalendar.setTimeInMillis(time);
        tempCalendar.add(Calendar.MONTH, isAfter ? 2 : -2);
        return tempCalendar.getTimeInMillis();
    }

    /**
     * Get hourly time from now by the specify hour offset. e.g: Current time is 5/23/2016 17:28, if hour offset -1,it will return the time of 5/23/2016 16:00 in millisecond
     *
     * @param hourOffset the hour offset from now, negative specify before time, positive is after time.
     */
    public static long getHourTimeFromNow(int hourOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hourOffset);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
