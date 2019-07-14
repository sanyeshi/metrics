package org.metric.transport.http;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import org.metrics.util.StringUtil;

public class HttpClientSender implements HttpSender {

	private CloseableHttpAsyncClient client;
	private RequestConfig requestConfig;

	public HttpClientSender() {
		this(HttpAsyncClients.createDefault());
	}

	public HttpClientSender(CloseableHttpAsyncClient client) {
		this.requestConfig = RequestConfig.copy(RequestConfig.DEFAULT).setSocketTimeout(5000)
				.setConnectTimeout(5000).setConnectionRequestTimeout(5000).build();
		this.client=client;
		this.client.start();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				client.close();
			} catch (IOException e) {
			}
		}));
	}

	@Override
	public Response send(Request request) throws Throwable {
		
		HttpRequestBase httpRequest=getRequest(request);
		HttpResponse httpResponse = client.execute(httpRequest, null).get();
		int code = httpResponse.getStatusLine().getStatusCode();
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null && entity.getContent() != null) {
			return new Response(code, EntityUtils.toString(entity));
		}
		return new Response(code, StringUtil.EMPTY_STRING);
	}

	private HttpRequestBase getRequest(Request request) {
		HttpRequestBase httpRequest = null;
		Method method = request.getMethod();
		if (method == Method.GET) {
			httpRequest = new HttpGet();
		} else if (method == Method.HEAD) {
			httpRequest = new HttpHead();
		} else if (method == Method.OPTIONS) {
			httpRequest = new HttpOptions();
		} else if (method == Method.POST) {
			httpRequest = new HttpPost();
		} else if (method == Method.PUT) {
			httpRequest = new HttpPut();
		} else if (method == Method.DELETE) {
			httpRequest = new HttpDelete();
		}
		httpRequest.setURI(request.getUrI());
		httpRequest.setConfig(requestConfig);
		Map<String, String> headers = request.getRequestHeaders();
		for (Entry<String, String> header : headers.entrySet()) {
			httpRequest.addHeader(header.getKey(), header.getValue());
		}
		if (!(httpRequest instanceof HttpEntityEnclosingRequestBase)) {
			return httpRequest;
		}
		//POST、PUT
		byte[] data=request.getEntity();
		if (data == null||data.length==0) {
			return httpRequest;
		}
		String contentType = headers.get(HTTP.CONTENT_TYPE);
		String contentEncoding = headers.get(HTTP.CONTENT_ENCODING);
		if (contentType == null || contentEncoding == null) {
			throw new IllegalArgumentException("Entity is not empty,"
					+ "Content-Type and Content-Encoding is required");
		}
		ByteArrayEntity entity = new ByteArrayEntity(data);
		entity.setContentType(contentType);
		entity.setContentEncoding(contentEncoding);
		((HttpEntityEnclosingRequestBase)httpRequest).setEntity(entity);

		return httpRequest;
	}

}
