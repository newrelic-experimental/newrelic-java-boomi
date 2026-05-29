package com.boomi.execution.forker;

import org.restlet.data.Response;
import org.restlet.resource.Representation;

import com.boomi.util.retry.RetryStrategy;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class ForkerClient {

	public abstract String getRpcUrl();
	
	@Trace
	public Response doGet(String url, RetryStrategy retry) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","ForkerClient","doGet");
		traced.addCustomAttribute("RPC-URL", url);
		return Weaver.callOriginal();
	}
	
	public Response doPost(String url, Representation rep, RetryStrategy retry) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.setMetricName("Custom","ForkerClient","doPost");
		traced.addCustomAttribute("RPC-URL", url);
		return Weaver.callOriginal();
	}
}
