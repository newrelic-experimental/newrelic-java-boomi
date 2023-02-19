package com.boomi.execution;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.BaseClass)
public abstract class ExecutionQueue {

	@Trace
	public void submit(ExecutionSubmission taskSubmission) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Boomi","Execution","ExecutionQueue","submit",taskSubmission.getName());
		if(taskSubmission.token == null) {
			Token t = NewRelic.getAgent().getTransaction().getToken();
			if(t != null && t.isActive()) {
				taskSubmission.token = t;
			} else if(t != null) {
				t.expire();
				t = null;
			}
		}
		Weaver.callOriginal();
	}
	
	@Trace(async = true)
	protected void launchExecution(ExecutionSubmission taskSubmission) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Boomi","Execution","ExecutionQueue","LaunchExecution",taskSubmission.getName());
		if(taskSubmission.token != null) {
			taskSubmission.token.linkAndExpire();
			taskSubmission.token = null;
		}
		Weaver.callOriginal();
	}
}
