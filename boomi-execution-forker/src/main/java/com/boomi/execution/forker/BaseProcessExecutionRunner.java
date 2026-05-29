package com.boomi.execution.forker;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.BaseClass)
public abstract class BaseProcessExecutionRunner {
	
	public abstract String getId();

	@Trace(dispatcher = true)
	protected void execute(String... args)  {
		NewRelic.getAgent().getTracedMethod().addCustomAttribute("WorkerID", getId());
		Weaver.callOriginal();
	}
	
	@Trace
	protected void executeImpl() {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","BaseProcessExecutionRunner",getClass().getSimpleName(),"executeImpl");
		Weaver.callOriginal();
	}
}
