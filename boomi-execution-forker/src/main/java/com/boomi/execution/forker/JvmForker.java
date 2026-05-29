package com.boomi.execution.forker;

import com.boomi.container.config.AccountConfig;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class JvmForker {

	private final String _runnerScriptBaseName = Weaver.callOriginal();
	
	public void launch(AccountConfig accountConfig, final String... scriptArgs) {
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		String acctId = accountConfig.getAccountId();
		if(acctId != null) {
			traced.addCustomAttribute("AccountID", accountConfig.getAccountId());
		}
		if(_runnerScriptBaseName != null) {
			traced.addCustomAttribute("RunnerScriptBaseName", _runnerScriptBaseName);
		}
		Weaver.callOriginal();
	}
}
