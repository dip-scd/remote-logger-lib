package org.remotelogger.core;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TasksQueryProcessor<TTaskResult> {
	protected final ExecutorService executor;
	protected HashSet<TaskWrapper> tasks;
    
    private class TaskWrapper implements OnTaskExecutionListener<TTaskResult> {
    	TaskWithListeners<TTaskResult> task;
    	public TaskWrapper(TaskWithListeners<TTaskResult> task){
    		this.task = task;
    		task.addListener(TaskWrapper.this);
    	}
		
		public void onTaskFailed(Exception e) {
			dequeTask();
		}
		
		private void dequeTask() {
			tasks.remove(TaskWrapper.this);
		}
		
		public void onTaskCompleted(TTaskResult result) {
			dequeTask();
		}
		
		@Override 
		public boolean equals( Object other ) {
			return task.equals(other);    
		}
	
		@Override 
		public int hashCode() {
			return task.hashCode();
		}
		
		@Override
		public String toString() {
			return "TaskWrapper:\n"+task.toString();
		}
    }
    
    public TasksQueryProcessor() {
        this(8);
    }
    
    public TasksQueryProcessor(int threadsCount) {
        executor = Executors.newFixedThreadPool(threadsCount);
        tasks = new HashSet<TaskWrapper>();
    }
    
    public boolean taskInQuery(final TaskWithListeners<TTaskResult> task) {
    	synchronized (tasks) {
	    	for(TaskWrapper w : tasks) {
	    		if(w.equals(task)) {
	    			return true;
	    		}
	    	}
	    	return false;
    	}
    }
    
    protected TaskWrapper findTask(final TaskWithListeners<TTaskResult> task) {
    	for(TaskWrapper w : tasks) {
    		if(w.equals(task)) {
    			return w;
    		}
    	}
    	return null;
    }

	public void addTask(final TaskWithListeners<TTaskResult> task) {
		synchronized(tasks) {
			if(!taskInQuery(task)) {
				TaskWrapper taskWrapper = new TaskWrapper(task);
				tasks.add(taskWrapper);
				executor.submit(task);
			} else {
				//D.i("task already in query");
			}
		}
	}
	
	public void printTasks() {
		System.out.print("\nTasks in queue:\n");
		synchronized (tasks) {
			for(TaskWrapper task : tasks) {
				System.out.print(task.toString());
			}
			System.out.print("Tasks count: "+tasks.size()+"\n");
		}
	}

	public void cancelTask(final TaskWithListeners<TTaskResult> task) {
		if(task == null) {
			return;
		}
		
		TaskWrapper wrapper = findTask(task);
		if(wrapper == null) {
			return;
		}
		tasks.remove(wrapper);
	}

	public void cancelAllTasks() {
		clearTasks();
		executor.shutdownNow();
	}

	public void clearTasks() {
		tasks.clear();
	}
	
	public int tasksCount() {
		return tasks.size();
	}
}