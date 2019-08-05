package com.shen.stephen.utilplatform.log;

import com.shen.stephen.utilplatform.Constants;
import com.shen.stephen.utilplatform.PKIApplication;
import com.shen.stephen.utilplatform.util.FileUtils;
import com.shen.stephen.utilplatform.util.PkiTimeUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Write the Log to the file
 * 
 * Created by snowdream on 10/20/13.
 */
public class Log2File {
	private static final String LOG_FILE_NAME = "EAM-";
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	private static String log_file_suffix = PkiTimeUtil.formatDate(System.currentTimeMillis(), "yyyy-MM-dd");
	private static boolean isCleaned = false;
	private static final long TEN_DAYS_IN_MILLIS = 10 * 24 * 60 * 60 * 1000;

	/**
	 * Set the ExecutorService
	 * 
	 * @param executor
	 *            the ExecutorService
	 */
	protected static void setExecutor(ExecutorService executor) {
		Log2File.executor = executor;
	}

	protected static void log2file(final String str) {
		if (executor != null) {
			cleanLogs();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					PrintWriter out = null;
					String path = getLogFilePath();
					File file = FileUtils.GetFileFromPath(path);

					try {
						out = new PrintWriter(new BufferedWriter(
								new FileWriter(file, true)));
						out.println(str);
						out.flush();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						out.close();
					}
				}
			});
		}
	}

	private static final String getLogFilePath() {
		File logDirect = FileUtils
				.getLogsDirectory(PKIApplication.getContext());
		String path = logDirect.getAbsolutePath() + Constants.CacheFiles.SEPARATOR
				+ LOG_FILE_NAME + log_file_suffix + ".log";

		return path;
	}

	/**
	 * Clean the logs files before ten days
	 */
	private static void cleanLogs() {
		if (isCleaned) {
			return;
		}
		File logDirect = FileUtils
				.getLogsDirectory(PKIApplication.getContext());
		if (logDirect != null && logDirect.isDirectory()) {
			File[] logs = logDirect.listFiles();
			if (logs != null) {
				for (File log : logs) {
					if (log.lastModified() + TEN_DAYS_IN_MILLIS < System
							.currentTimeMillis()) {
						log.delete();
					}
				}
			}
		}

		isCleaned = true;
	}
}
