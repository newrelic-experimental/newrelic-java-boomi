package com.boomi.connector.wss;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.boomi.connector.wss.route.WebserviceConfig;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.boomi.wss.WSSUtils;

@Weave(type = MatchType.BaseClass)
public abstract class WSServlet {

	protected OperationConfig getOperation(WSAccountServer server, HttpServletRequest request, Properties props, WebserviceConfig wsConfig) {
		OperationConfig config = Weaver.callOriginal();
		WSSUtils.nameTransaction(wsConfig, config, request);
		return config;
	}
}
