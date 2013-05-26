package org.remotelogger.core;

import java.util.LinkedList;
import java.util.List;


public abstract class TaskWithListeners<TTaskResult> implements Runnable {

	protected boolean cancelFlag = false;
	protected volatile List<OnTaskExecutionListener<TTaskResult>> listeners = new LinkedList<OnTaskExecutionListener<TTaskResult>>();

	public TaskWithListeners(final OnTaskExecutionListener<TTaskResult> listener) {
		addListener(listener);
	}

	public void addListener(final OnTaskExecutionListener<TTaskResult> listener) {
		if (listener != null) {
			if (!this.listeners.contains(listener)) {
				this.listeners.add(listener);
			}
		}
	}

	public void removeListner(final OnTaskExecutionListener<TTaskResult> listener) {
		if (listener != null) {
			this.listeners.remove(listener);
		}
	}

	public void cancel() {
		this.cancelFlag = true;
	}

	public boolean isCancelled() {
		return this.cancelFlag;
	}

	public void notifyListenersAboutComplete(final TTaskResult result) {
		synchronized (listeners) {
			for (final OnTaskExecutionListener<TTaskResult> listener : this.listeners) {
				listener.onTaskCompleted(result);
			}
			this.listeners.clear();
		}
	}

	public void notifyListenersAboutFail(final Exception e) {
		synchronized (listeners) {
			for (final OnTaskExecutionListener<TTaskResult> listener : this.listeners) {
				listener.onTaskFailed(e);
			}
			this.listeners.clear();
		}
	}
}
