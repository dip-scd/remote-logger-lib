package org.remotelogger.http_poster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import org.remotelogger.core.DataConverter;
import org.remotelogger.core.OnTaskExecutionListener;
import org.remotelogger.core.TaskWithListeners;
import org.remotelogger.core.TasksQueryProcessor;

public class StringPoster implements DataConverter<PostStringTask, String> {

	public class PostDataTask extends TaskWithListeners<String> {

		PostStringTask data = null;

		public PostDataTask() {
			super(null);
		};

		public PostDataTask(final PostStringTask dataToPost, final OnConvertedDataListener<String> listener) {
			super(new OnTaskExecutionListener<String>() {
				@Override
				public void onTaskCompleted(final String result) {
					listener.onConvertedDataReceived(result);
				}

				@Override
				public void onTaskFailed(final Exception e) {
					listener.onDataConvertionFailed(e);
				}
			});
			this.data = dataToPost;
		}

		protected void processInputStream(final InputStream stream, final PostDataTask task) {
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			final byte buffer[] = new byte[1024];
			int n = 0;
			try {
				while ((n = stream.read(buffer)) != -1) {
					if (task.cancelFlag) {
						return;
					}
					os.write(buffer, 0, n);
				}
			} catch (final IOException e) {
				task.notifyListenersAboutFail(e);
			}
			task.notifyListenersAboutComplete(parseResponse(os));
		}

		protected void processOutputStream(final OutputStream stream, final PostDataTask task) {
			try {
				writeDataToBuffer(this.data, stream);
			} catch (final IOException e) {
				task.notifyListenersAboutFail(e);
			}
		}

		@Override
		public void run() {
			try {
				final URL u = new URL(urlForPost(this.data));
				final HttpURLConnection conn = (HttpURLConnection) u.openConnection();
				setupConnection(conn);
				conn.connect();
				final OutputStream os = conn.getOutputStream();
				processOutputStream(os, this);
				final InputStream is = conn.getInputStream();
				processInputStream(is, this);
				conn.disconnect();
			} catch (final Exception e) {
				notifyListenersAboutFail(e);
			}
		}

		protected void setupConnection(final HttpURLConnection conn) {
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			try {
				conn.setRequestMethod("POST");
			} catch (final ProtocolException e) {
				// do nothing
			}
		}

	}

	public static String mapToString(final String[][] dict) {
		String ret = "";
		final int count = dict.length;
		int i = 0;
		for (final String[] pair : dict) {
			i++;
			try {
				ret += URLEncoder.encode(pair[0], "UTF-8") + "=" + URLEncoder.encode(pair[1], "UTF-8");
			} catch (final UnsupportedEncodingException e) {
				return null;
			}
			if (i < count) {
				ret += "&";
			}
		}
		return ret;
	}

	TasksQueryProcessor<String> tasksProcessor = new TasksQueryProcessor<String>();
	
	@Override
	public void convert(final PostStringTask value, final OnConvertedDataListener<String> listener) {
		if (value != null && listener != null) {
			this.tasksProcessor.addTask(new PostDataTask(value, listener));
		}
	}

	protected String parseResponse(final ByteArrayOutputStream receivedDataStream) {
		return new String(receivedDataStream.toByteArray());
	}

	public void post(final String urlToPost, final String dataToPost, final OnConvertedDataListener<String> listener) {
		convert(new PostStringTask(urlToPost, dataToPost), listener);
	}
	
	protected String urlForPost(final PostStringTask dataToPost) {
		return dataToPost != null ? dataToPost.url : null;
	}

	protected void writeDataToBuffer(final PostStringTask dataToPost, final OutputStream stream) throws IOException {
		stream.write(dataToPost.data.getBytes());
	}
}
