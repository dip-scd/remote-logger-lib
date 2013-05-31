package org.remotelogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Calendar;

import org.remotelogger.core.DataConverter.OnConvertedDataListener;
import org.remotelogger.http_poster.StringPoster;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class D {

	public static final String SERVER_URL = "http://remote-logger-demo.appspot.com";

	public static boolean remoteLog = false; // set false to disable remote
	// logging
	private static String appName = null;
	public static boolean debug = false; // set false to disable debugging
	private static String deviceId = "";
	private static String deviceName = Build.BRAND + " " + Build.DEVICE;

	private static int CALLER_POINT_STACK_TRACE_ELEMENT_NUMBER = 4;
	private static final String LOG_URL = SERVER_URL + "/api?op=put_message";
	private static final int REMOTE_SEVERITY_DEBUG = 1;
	private static final int REMOTE_SEVERITY_ERROR = 3;
	private static final int REMOTE_SEVERITY_INFO = 0;
	private static final int REMOTE_SEVERITY_WARNING = 2;

	private static OnConvertedDataListener<String> dataListener = new OnConvertedDataListener<String>() {
		@Override
		public void onConvertedDataReceived(final String data) {
		}

		@Override
		public void onDataConvertionFailed(final Exception e) {
		}
	};

	private static StringPoster stringPoster = new StringPoster();

	public static void d(final String message) {
		log(Log.DEBUG, message);
	}

	public static void disable(Context context) {
		disableDebug();
		disableRemoteLogging();
	}

	private static void disableDebug() {
		D.debug = false;
	}

	private static void disableRemoteLogging() {
		D.remoteLog = false;
	}

	public static void e(final String message) {
		log(Log.ERROR, message);
	}

	public static void e(final Throwable throwable) {
		if (throwable != null) {
			log(Log.ERROR, stackTraceStringOfTheThrowable(throwable));
		}
	}

	public static void enable(Context context) {
		enableDebug();
		enableRemoteLogging(context);
	}

	private static void enableDebug() {
		D.debug = true;
	}

	private static void enableRemoteLogging(final Context contextToUse) {
		if (D.appName == null) {
			D.appName = getApplicationName(contextToUse);
		}

		try {
			final TelephonyManager manager = (TelephonyManager) contextToUse
					.getSystemService(Context.TELEPHONY_SERVICE);
			D.deviceId = manager.getDeviceId();
		} catch (final SecurityException e) {
			// add use-permission READ_PHONE_STATE to prevent this exception
			// until this, random number will be used
			D.deviceId = String.valueOf((int) (Math.random() * 10000000));
		}

		D.remoteLog = true;
		D.i("Remote logging started");
	}

	private static String getApplicationName(final Context contextToUse) {
		String nameToUse = contextToUse.getApplicationInfo().name;
		if (nameToUse != null) {
			return nameToUse;
		}
		final Resources r = contextToUse.getResources();
		nameToUse = r.getString(contextToUse.getApplicationInfo().labelRes);
		if (nameToUse != null) {
			return nameToUse;
		}
		nameToUse = contextToUse.getApplicationInfo().processName;
		if (nameToUse != null) {
			return nameToUse;
		}
		return nameToUse;
	}

	private static String getCurrentLongTimestampString() {
		final long time = System.currentTimeMillis();
		final int offset = Calendar.getInstance().getTimeZone().getOffset(time);
		return String.valueOf((time + offset));
	}

	private static String getStackTrace() {
		String ret = "";
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		for (int i = 4; i < ste.length - 1; i++) {
			ret += "\n" + ste[i].toString();
		}
		return ret;
	}

	private static String getStackTrace(int maxStackLength) {
		String ret = "";
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		for (int i = 4; i < Math.min(maxStackLength + 4, ste.length - 1); i++) {
			ret += "\n" + ste[i].toString();
		}
		return ret;
	}

	public static void i(final String message) {
		log(Log.INFO, message);
	}

	private static void log(final int type, String message) {
		if (!debug) {
			return;
		}

		String caller = "D";

		String callPoint = "";
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();

		if (ste != null && ste.length > CALLER_POINT_STACK_TRACE_ELEMENT_NUMBER) {
			callPoint += ste[CALLER_POINT_STACK_TRACE_ELEMENT_NUMBER]
					.getFileName()
					+ ":"
					+ ste[CALLER_POINT_STACK_TRACE_ELEMENT_NUMBER]
							.getLineNumber();
		}

		message = callPoint + " " + message;

		try {
			switch (type) {
			case Log.ERROR:
				Log.e(caller, message);
				break;
			case Log.INFO:
				Log.i(caller, message);
				break;
			case Log.VERBOSE:
				Log.v(caller, message);
				break;
			case Log.DEBUG:
				Log.d(caller, message);
				break;
			case Log.WARN:
				Log.w(caller, message);
				break;
			default:
				Log.i(caller, message);
				break;
			}
		} catch (Exception e) {

		}

		if (remoteLog) {
			r(message, type);
		}
	}
	
	private static int logSeverityToRemoteSeverity(final int severity) {
		switch (severity) {
		case Log.ERROR:
			return REMOTE_SEVERITY_ERROR;
		case Log.INFO:
			return REMOTE_SEVERITY_INFO;
		case Log.VERBOSE:
			return REMOTE_SEVERITY_INFO;
		case Log.DEBUG:
			return REMOTE_SEVERITY_DEBUG;
		case Log.WARN:
			return REMOTE_SEVERITY_WARNING;
		default:
			return REMOTE_SEVERITY_INFO;
		}
	}

	public static void printCurrentStackTrace() {
		String logString = "" + getStackTrace(); // if you'll call
													// logCurrentStackTrace(String
													// customMessage)
													// instead this code, extra
													// stack trace element will
													// be displayed
													// see getStackTrace(). So
													// small duplicating left
													// for a while
		log(Log.DEBUG, logString);
	}

	public static void printCurrentStackTrace(int maxStackLength) {
		String logString = "" + getStackTrace(maxStackLength); // if you'll call
																// logCurrentStackTrace(String
																// customMessage)
		// instead this code, extra stack trace element will be displayed
		// see getStackTrace(). So small duplicating left for a while
		log(Log.DEBUG, logString);
	}

	public static void printCurrentStackTrace(String customMessage) {
		String logString = customMessage + "" + getStackTrace();
		log(Log.DEBUG, logString);
	}

	public static void printCurrentStackTrace(String customMessage,
			int maxStackLength) {
		String logString = customMessage + "" + getStackTrace(maxStackLength);
		log(Log.DEBUG, logString);
	}

	private static void r(final String message, final int severity) {
		if (!debug || message == null) {
			return;
		}

		final int remoteSeverity = logSeverityToRemoteSeverity(severity);
		final String[][] dict = { { "time", getCurrentLongTimestampString() },
				{ "message", message.replaceAll("\n", "<br>") },
				{ "device", deviceName }, { "app", appName },
				{ "device_id", deviceId },
				{ "severity", String.valueOf(remoteSeverity) } };
		stringPoster
				.post(LOG_URL, StringPoster.mapToString(dict), dataListener);
	}

	private static String stackTraceStringOfTheThrowable(Throwable e) {
		if (e == null) {
			return "";
		}

		String ret = "";
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		e.printStackTrace(ps);
		ret = os.toString();
		return ret;
	}

	public static void v(final String message) {
		log(Log.VERBOSE, message);
	}

	public static void w(final String message) {
		log(Log.WARN, message);
	}
}
