package org.remotelogger.http_poster;

public class PostStringTask {
	public PostStringTask(final String urlToPost, final String dataToPost) {
		url = urlToPost;
		data = dataToPost;
	}

	public String url;
	public String data;
}