package org.metric.transport.http;

import org.metrics.util.StringUtil;

public class ConsoleSender implements HttpSender{

	@Override
	public Response send(Request request) throws Throwable {
		System.out.println(request);
		return new Response(200,StringUtil.EMPTY_STRING);
	}

}
