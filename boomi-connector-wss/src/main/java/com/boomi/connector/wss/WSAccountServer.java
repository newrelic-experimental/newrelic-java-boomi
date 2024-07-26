package com.boomi.connector.wss;

import com.boomi.connector.wss.OperationConfig.Key;
import com.boomi.execution.ExecutionResponse;
import com.boomi.execution.ListenerTaskTemplate;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.Transaction;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class WSAccountServer {

	@Trace
	public ExecutionResponse invokeOperation(OperationConfig operation, ListenerTaskTemplate taskTemplate, RequestLogExec reqLogExec, long maxWaitTime) {
		Transaction transaction = NewRelic.getAgent().getTransaction();

		if(operation != null) {
			TracedMethod traced = NewRelic.getAgent().getTracedMethod();
			String name = operation.getName();
			if(name != null) traced.addCustomAttribute("Operation-Name", name);
			String objectName = operation.getObjectName();
			if(objectName != null) traced.addCustomAttribute("Operation-ObjectName", objectName);
			Key key = operation.getKey();
			if(key != null) {
				String path = key.getUrlPath();
				if(path != null && !path.isEmpty()) {
					traced.addCustomAttribute("OP_Path", path);
				}
				String method = key.getMethod();
				if(method != null) {
					traced.addCustomAttribute("OP_Method", method);
				}
				String keyName = key.getName();
				if(keyName != null) {
					traced.addCustomAttribute("OP_Name", keyName);
				}

				if(!transaction.isTransactionNameSet()) {
					StringBuffer sb = new StringBuffer();

					if(path != null) {
						sb.append(path);
						sb.append("/*");
						if(keyName != null && !keyName.isEmpty()) {
							sb.append("/");
							sb.append(keyName);
						}

						if(method != null) {
							sb.append(" ( ");
							if(!method.isEmpty()) {
								sb.append(method);
							} else {
								sb.append("Unknown Method");
							}
							sb.append(" )");
						}
					}
					String txnName = sb.toString();
					if(!txnName.isEmpty()) {
						NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "Boomi", txnName);
					}
				}
			}
		}

		ExecutionResponse exResp = Weaver.callOriginal();

		return exResp;
	}

	@Weave
	public static final class RequestLogExec {

	}
}
