package com.boomi.execution;

import com.boomi.container.config.AccountConfig;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;

@Weave
public abstract class ExecutionSubmission {

	@NewField
	public Token token = null;
	
	public ExecutionSubmission(final ExecutionTaskConfig config, final ExecutionDetails details,
			final String localHostId, final AccountConfig accountConfig,
			final ExecutionForker.ResponseMode responseMode, final ExecutionForker.Callback callback) {
		
	}
	
	public abstract String getName();
}
