package com.shen.stephen.utilplatform.log;

import android.util.Log;

import com.shen.stephen.utilplatform.BuildConfig;
import com.shen.stephen.utilplatform.util.StrUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PLog {

	/**
	 * The mask of the policy.
	 */
	public static final int POLICY_MASK = 0x3f;

	/**
	 * The GLOBAL_TAG of the Application
	 */
	private static final String GLOBAL_TAG = "EAM";

	/**
	 * Whether to enable the log
	 */
	private static boolean mIsEnabled = true;

	/**
	 * Whether to enable log to the console
	 */
	private static boolean mIsLog2Console = true;

	/**
	 * Whether to enable log to the file
	 */
	private static boolean mIsLog2File = BuildConfig.DEBUG;

	/**
	 * Specify which will log to console. If the app is a release version the
	 * default only log the error, warning and info logs.
	 */
	private static int mLog2ConsolePolicy = BuildConfig.DEBUG ? POLICY_MASK
			: (OSLogLevel.ERROR | OSLogLevel.WARN | OSLogLevel.INFO);

	/**
	 * Specify which will log to file.
	 */
	private static int mLog2FilePolicy = BuildConfig.DEBUG ? POLICY_MASK
			: (OSLogLevel.ERROR | OSLogLevel.WARN | OSLogLevel.INFO);

	/**
	 * Enable the specified level log can write to console.
	 * 
	 * @param level
	 *            the log level, use '|' to separate different levels, such as
	 *            if you want enable error and verbose logs use (
	 *            {@link OSLogLevel#ERROR} | {@link OSLogLevel#VERBOSE}).
	 *
	 */
	public static void enableLog2Console(int level) {
		mLog2ConsolePolicy |= (level & POLICY_MASK);
	}

	/**
	 * Enable the specified level log can can write to file.
	 * 
	 * @param level
	 *            the log level, use '|' to separate different levels, such as
	 *            if you want enable error and verbose logs use (
	 *            {@link OSLogLevel#ERROR} | {@link OSLogLevel#VERBOSE}).
	 *
	 */
	public static void enableLog2File(int level) {
		mLog2FilePolicy |= (level & POLICY_MASK);
	}
	
	/**
	 * Disable the specified level log can write to console.
	 * 
	 * @param level
	 *            the log level, use '|' to separate different levels, such as
	 *            if you want enable error and verbose logs use (
	 *            {@link OSLogLevel#ERROR} | {@link OSLogLevel#VERBOSE}).
	 *
	 */
	public static void disableLog2Console(int level) {
		mLog2ConsolePolicy &= ~(level & POLICY_MASK);
	}

	/**
	 * Disable the specified level log can can write to file.
	 * 
	 * @param level
	 *            the log level, use '|' to separate different levels, such as
	 *            if you want enable error and verbose logs use (
	 *            {@link OSLogLevel#ERROR} | {@link OSLogLevel#VERBOSE}).
	 *
	 */
	public static void disableLog2File(int level) {
		mLog2FilePolicy &= ~(level & POLICY_MASK);
	}

	private static void log(int level, String tag, String msg, Throwable tr) {
		if (!mIsEnabled) {
			return;
		}

		String curTag = getCurrentTag(tag);

		if (mIsLog2Console && (mLog2ConsolePolicy & level) != 0) {
			log2Console(level, curTag, msg, tr);
		}

		if (mIsLog2File && (mLog2FilePolicy & level) != 0) {
			log2File(level, curTag, msg, tr);
		}
	}

	/**
	 * Get the final tag from the tag.
	 *
	 * @param tag
	 */
	private static String getCurrentTag(String tag) {
		if (!StrUtil.isEmpty(tag)) {
			return tag;
		}

		if (!StrUtil.isEmpty(GLOBAL_TAG)) {
			return GLOBAL_TAG;
		}

		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		if (stacks.length >= 4) {
			return stacks[3].getClassName();
		}

		return null;
	}

	/**
	 * write the log messages to the console.
	 *
	 * @param level
	 * @param tag
	 * @param msg
	 * @param thr
	 */
	protected static void log2Console(int level, String tag, String msg,
			Throwable thr) {
		switch (level) {
		case OSLogLevel.VERBOSE:

			if (thr == null) {
				Log.v(tag, msg);
			} else {
				Log.v(tag, msg, thr);
			}

			break;
		case OSLogLevel.DEBUG:
			if (thr == null) {
				Log.d(tag, msg);
			} else {
				Log.d(tag, msg, thr);
			}
			break;
		case OSLogLevel.INFO:
			if (thr == null) {
				Log.i(tag, msg);
			} else {
				Log.i(tag, msg, thr);
			}
			break;
		case OSLogLevel.WARN:
			if (thr == null) {
				Log.w(tag, msg);
			} else if (StrUtil.isEmpty(msg)) {
				Log.w(tag, thr);
			} else {
				Log.w(tag, msg, thr);
			}

			break;
		case OSLogLevel.ERROR:
			if (thr == null) {
				Log.e(tag, msg);
			} else {
				Log.e(tag, msg, thr);
			}

			break;
		case OSLogLevel.ASSERT:
			if (thr == null) {
				Log.wtf(tag, msg);
			} else if (StrUtil.isEmpty(msg)) {
				Log.wtf(tag, thr);
			} else {
				Log.wtf(tag, msg, thr);
			}

			break;
		default:
			break;
		}
	}

	/**
	 * write the log messages to the file.
	 *
	 * @param level
	 * @param tag
	 * @param msg
	 * @param thr
	 */
	private static void log2File(int level, String tag, String msg,
			Throwable thr) {
		Log2File.log2file(format(level, tag, msg, thr));
	}

	private static String format(int level, String tag, String msg, Throwable tr) {
		if (StrUtil.isEmpty(tag) || StrUtil.isEmpty(msg)) {
			return "";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSSZ", Locale.US);
		StringBuffer buffer = new StringBuffer();
		buffer.append(OSLogLevel.level2String(level));
		buffer.append("\t");
		buffer.append(formatter.format(System.currentTimeMillis()));
		buffer.append("\t");
		buffer.append(tag);
		buffer.append("\t");
		buffer.append(msg);
		if (tr != null) {
			buffer.append(System.getProperty("line.separator"));
			buffer.append(Log.getStackTraceString(tr));
		}

		return buffer.toString();
	}

	/**
	 * is the log enabled
	 */
	public static boolean isEnabled() {
		return mIsEnabled;
	}

	/**
	 * enable or disable the log, the default value is true.
	 *
	 * @param enabled
	 *            whether to enable the log
	 */
	public static void setEnabled(boolean enabled) {
		mIsEnabled = enabled;
	}

	/**
	 * is the Log2Console enabled
	 */
	public static boolean isLog2ConsoleEnabled() {
		return mIsLog2Console;
	}

	/**
	 * enable or disable writing the log to the console. the default value is
	 * true.
	 *
	 * @param enabled
	 *            whether to enable the log
	 */
	public static void setLog2ConsoleEnabled(boolean enabled) {
		mIsLog2Console = enabled;
	}

	/**
	 * is the Log2Console enabled
	 */
	public static boolean isLog2FileEnabled() {
		return mIsLog2File;
	}

	/**
	 * enable or disable writing the log to the file. the default value is
	 * false.
	 *
	 * @param enabled
	 *            whether to enable the log
	 */
	public static void setLog2FileEnabled(boolean enabled) {
		mIsLog2File = enabled;
	}

	/**
	 * Checks to see whether or not a log for the specified tag is loggable at
	 * the specified level. The default level of any tag is set to INFO. This
	 * means that any level above and including INFO will be logged. Before you
	 * make any calls to a logging method you should check to see if your tag
	 * should be logged.
	 *
	 * @param tag
	 *            The tag to check
	 * @param level
	 *            The level to check
	 * @return Whether or not that this is allowed to be logged.
	 */
	public static boolean isLoggable(String tag, int level) {
		return Log.isLoggable(tag, level);
	}

	/**
	 * Low-level logging call.
	 *
	 * @param priority
	 *            The priority/type of this log message
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @return The number of bytes written.
	 */
	public static int println(int priority, String tag, String msg) {
		return Log.println(priority, tag, msg);
	}

	/**
	 * Handy function to get a loggable stack trace from a Throwable
	 *
	 * @param tr
	 *            An exception to log
	 * @return
	 */
	public static String getStackTraceString(Throwable tr) {
		return Log.getStackTraceString(tr);
	}

	/**
	 * Send a DEBUG log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void d(String tag, String msg) {
		log(OSLogLevel.DEBUG, tag, msg, null);
	}

	/**
	 * Send a DEBUG log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void d(String tag, String msg, Object... args) {
		log(OSLogLevel.DEBUG, tag, String.format(msg, args), null);
	}

	/**
	 * Send a DEBUG log message.
	 */
	public static void d(String msg) {
		log(OSLogLevel.DEBUG, GLOBAL_TAG, msg, null);
	}

	/**
	 * Send a DEBUG log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void d(String tag, String msg, Throwable thr) {
		log(OSLogLevel.DEBUG, tag, msg, thr);
	}

	/**
	 * Send a DEBUG log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void d(String msg, Throwable thr) {
		log(OSLogLevel.DEBUG, GLOBAL_TAG, msg, thr);
	}

	/**
	 * Send a ERROR log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void e(String tag, String msg) {
		log(OSLogLevel.ERROR, tag, msg, null);
	}

	/**
	 * Send a ERROR log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void e(String tag, String msg, Object... args) {
		log(OSLogLevel.ERROR, tag, String.format(msg, args), null);
	}

	/**
	 * Send an ERROR log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void e(String msg) {
		log(OSLogLevel.ERROR, GLOBAL_TAG, msg, null);
	}

	/**
	 * Send a ERROR log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void e(String tag, String msg, Throwable thr) {
		log(OSLogLevel.ERROR, tag, msg, thr);
	}

	/**
	 * Send an ERROR log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void e(String msg, Throwable thr) {
		log(OSLogLevel.ERROR, GLOBAL_TAG, msg, thr);
	}

	/**
	 * Send a INFO log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void i(String tag, String msg) {
		log(OSLogLevel.INFO, tag, msg, null);
	}

	/**
	 * Send an INFO log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void i(String msg) {
		log(OSLogLevel.INFO, GLOBAL_TAG, msg, null);
	}

	/**
	 * Send a INFO log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void i(String tag, String msg, Object... args) {
		log(OSLogLevel.INFO, tag, String.format(msg, args), null);
	}

	/**
	 * Send a INFO log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void i(String tag, String msg, Throwable thr) {
		log(OSLogLevel.INFO, tag, msg, thr);
	}

	/**
	 * Send a INFO log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void i(String msg, Throwable thr) {
		log(OSLogLevel.INFO, GLOBAL_TAG, msg, thr);
	}

	/**
	 * Send a VERBOSE log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void v(String tag, String msg) {
		log(OSLogLevel.VERBOSE, tag, msg, null);
	}

	/**
	 * Send a VERBOSE log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void v(String msg) {
		log(OSLogLevel.VERBOSE, GLOBAL_TAG, msg, null);
	}

	/**
	 * Send a VERBOSE log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void v(String tag, String msg, Object... args) {
		log(OSLogLevel.VERBOSE, tag, String.format(msg, args), null);
	}

	/**
	 * Send a VERBOSE log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void v(String tag, String msg, Throwable thr) {
		log(OSLogLevel.VERBOSE, tag, msg, thr);
	}

	/**
	 * Send a VERBOSE log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void v(String msg, Throwable thr) {
		log(OSLogLevel.VERBOSE, GLOBAL_TAG, msg, thr);
	}

	/**
	 * Send an empty WARN log message and log the exception.
	 *
	 * @param thr
	 *            An exception to log
	 */
	public static void w(Throwable thr) {
		log(OSLogLevel.WARN, null, null, thr);
	}

	/**
	 * Send a WARN log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void w(String tag, String msg) {
		log(OSLogLevel.WARN, tag, msg, null);
	}

	/**
	 * Send a WARN log message
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void w(String msg) {
		log(OSLogLevel.WARN, null, msg, null);
	}

	/**
	 * Send a WARN log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void w(String tag, String msg, Object... args) {
		log(OSLogLevel.WARN, tag, String.format(msg, args), null);
	}

	/**
	 * Send a WARN log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void w(String tag, String msg, Throwable thr) {
		log(OSLogLevel.WARN, tag, msg, thr);
	}

	/**
	 * Send a WARN log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void w(String msg, Throwable thr) {
		log(OSLogLevel.WARN, null, msg, thr);
	}

	/**
	 * Send an empty What a Terrible Failure log message and log the exception.
	 *
	 * @param thr
	 *            An exception to log
	 */
	public static void wtf(Throwable thr) {
		log(OSLogLevel.ASSERT, null, null, thr);
	}

	/**
	 * Send a What a Terrible Failure log message.
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void wtf(String tag, String msg) {
		log(OSLogLevel.ASSERT, tag, msg, null);
	}

	/**
	 * Send a What a Terrible Failure log message
	 *
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void wtf(String msg) {
		log(OSLogLevel.ASSERT, GLOBAL_TAG, msg, null);
	}

	/**
	 * Send a What a Terrible Failure log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void wtf(String tag, String msg, Throwable thr) {
		log(OSLogLevel.ASSERT, tag, msg, thr);
	}

	/**
	 * Send a What a Terrible Failure log message and log the exception.
	 *
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            An exception to log
	 */
	public static void wtf(String msg, Throwable thr) {
		log(OSLogLevel.ASSERT, GLOBAL_TAG, msg, thr);
	}

	public static final class OSLogLevel {
		private OSLogLevel() {
			// avoid construct outside.
		}

		/**
		 * PLog level verbose.
		 */
		public static final int VERBOSE = 1;

		/**
		 * PLog level debug.
		 */
		public static final int DEBUG = 2;

		/**
		 * PLog level information.
		 */
		public static final int INFO = 4;

		/**
		 * PLog level warning.
		 */
		public static final int WARN = 8;

		/**
		 * PLog level error.
		 */
		public static final int ERROR = 16;

		/**
		 * PLog level assert.
		 */
		public static final int ASSERT = 32;

		/**
		 * convert level to corresponding description.
		 * 
		 * @param level
		 *            the PLog level
		 * @return return level description string.
		 */
		public static String level2String(int level) {
			String ret = StrUtil.EMPTYSTRING;
			switch (level) {
			case VERBOSE:
				ret = "V";
				break;
			case DEBUG:
				ret = "D";
				break;
			case INFO:
				ret = "I";
				break;
			case WARN:
				ret = "W";
				break;
			case ERROR:
				ret = "E";
				break;
			case ASSERT:
				ret = "A";
				break;

			default:
				break;
			}

			return ret;
		}
	}
}
