package org.remotelogger.core;

public interface OnTaskExecutionListener<TTaskResult> {
	void onTaskCompleted(TTaskResult result);
	void onTaskFailed(Exception e);
}