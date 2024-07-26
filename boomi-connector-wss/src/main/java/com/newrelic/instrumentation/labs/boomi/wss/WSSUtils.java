package com.newrelic.instrumentation.labs.boomi.wss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.boomi.connector.wss.OperationConfig;
import com.boomi.connector.wss.OperationConfig.Key;
import com.boomi.connector.wss.route.MutableUrlPath;
import com.boomi.connector.wss.route.UrlPath;
import com.boomi.connector.wss.route.WebserviceConfig;
import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.Config;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Transaction;
import com.newrelic.api.agent.TransactionNamePriority;

public class WSSUtils implements AgentConfigListener {

	private static final String URIMASKSSETTING = "Boomi.uri_masks";
	private static final String URIMASKENABLEDSSETTING = "Boomi.uri_masking_enabled";
	private static final String URIIGNORESSETTING = "Boomi.uri_ignores";
	private static final ArrayList<String> uri_masks = new ArrayList<>();
	private static final ArrayList<Pattern> uri_patterns = new ArrayList<>();
	private static final ArrayList<String> uri_ignores = new ArrayList<>();
	private static final ArrayList<Pattern> uri_ignore_patterns = new ArrayList<>();
	private static final WSSUtils INSTANCE = new WSSUtils();
	private static boolean masking_enabled = false;

	static {	
		init();
	}

	private WSSUtils() {

	}

	private static void init() {
		ServiceFactory.getConfigService().addIAgentConfigListener(INSTANCE);
		Config agentConfig = NewRelic.getAgent().getConfig();
		String masks = agentConfig.getValue(URIMASKSSETTING);
		HashMap<String, Object>  attributes = new HashMap<>();
		if(masks != null && !masks.isEmpty()) {
			String[] values = masks.split(",");
			for(String value : values) {
				try {
					Pattern pattern = Pattern.compile(value);
					uri_patterns.add(pattern);
					uri_masks.add(value);
				} catch (Exception e) {
					NewRelic.getAgent().getLogger().log(Level.FINE, e, "Boomi URL Mask Error: This regex in masks is invalid: {0}", value);
				}
			}
		}
		attributes.put("URI Masks", uri_masks);
		String ignores = agentConfig.getValue(URIIGNORESSETTING);
		if(ignores != null && !ignores.isEmpty()) {
			String[] values = ignores.split(",");
			if(!uri_ignores.isEmpty()) uri_ignores.clear();
			for(String value : values) {
				try {
					Pattern pattern = Pattern.compile(value);
					uri_ignore_patterns.add(pattern);
					uri_ignores.add(value);
				} catch (Exception e) {
					NewRelic.getAgent().getLogger().log(Level.FINE, e, "Boomi URL Ignores Error: This regex in ignores is invalid: {0}", value);
				}
			}
			
		}
		attributes.put("URI Ignores", uri_ignores);
		
		Object obj = agentConfig.getValue(URIMASKENABLEDSSETTING);
		if (obj != null) {
			if (obj instanceof Boolean) {
				masking_enabled = (Boolean)obj;
			} else {
				String temp = obj.toString();
				masking_enabled = Boolean.parseBoolean(temp);
			}
		}
		attributes.put("URI Masksing Enabled", masking_enabled);
		NewRelic.getAgent().getInsights().recordCustomEvent("Boomi_URI_Masking", attributes);
		
	}
	
	public static void nameTransaction(HttpServletRequest request) {
		MutableUrlPath urlPath = MutableUrlPath.create(request.getPathInfo());
		String method = request.getMethod();
		
		StringBuffer sb = new StringBuffer("/");
		
		for(int i=0;i<urlPath.length();i++) {
			String segment = urlPath.get(i);

			boolean ignore = false;
			for(Pattern pattern : uri_ignore_patterns) {
				Matcher matcher = pattern.matcher(segment);
				if(matcher.matches()) {
					ignore = true;
					break;
				}
			}
			
			if(!ignore) {
				boolean matched = false;

				for(Pattern pattern : uri_patterns) {
					Matcher matcher = pattern.matcher(segment);
					if(matcher.matches()) {
						matched = true;
						break;
					}
				}
				if(matched) {
					sb.append('*');
				} else {
					sb.append(segment);
				}
				if(i < urlPath.length() - 1) {
					sb.append('/');
				}
			}
		}
		

		if(method != null) {
			NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "Boomi-Connector", "WSServlet", sb.toString() + " (" + method + ")");
		} else {
			NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "Boomi-Connector", "WSServlet", sb.toString());
		}
	}

	public static void nameTransaction(WebserviceConfig wsConfig, OperationConfig opConfig, HttpServletRequest request) {
		if(masking_enabled) {
			nameTransaction(request);
			return;
		}
		String path = null;
		String operation = null;
		UrlPath uriPath = null;
		String method = null;

		if(request != null) {
			method = request.getMethod();
		}

		if(wsConfig != null) {
			uriPath = wsConfig.getUrlPath();

			if(uriPath != null) {
				path = uriPath.toString();
			}
		}

		if(opConfig != null) {
			Key key = opConfig.getKey();
			if(key != null) {
				if(path == null) {
					path = key.getUrlPath();
				}
				operation = key.getName();
				if(method == null) {
					method = key.getMethod();
				}
			}
		}

		if(path == null) {
			if(uriPath == null) {
				String tmp = request.getPathInfo();
				if(tmp.startsWith("/ws/rest")) {
					tmp = tmp.replace("/ws/rest", "");
				}
				uriPath = MutableUrlPath.create(tmp);
				if (uriPath != null) {
					StringBuffer sb = new StringBuffer();
					int length = uriPath.length();
					if (length > 0) {
						sb.append("/" + uriPath.get(0));
						if(length > 1) {
							sb.append("/" + uriPath.get(0));
						}
						path = sb.toString();

						if(length > 2) {
							operation = uriPath.get(length-1);
						}
					}

				}
			}
		}
		if(path != null) {
			Transaction transaction = NewRelic.getAgent().getTransaction();
			if(operation != null) {
				transaction.setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "Boomi-Connector", "WSServlet", path, "/*/",operation + " (" + method + ")");
			} else {
				transaction.setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "Boomi-Connector", "WSServlet", path, "/*"  + " (" + method + ")");
			}
		}
	}

	@Override
	public void configChanged(String appName, AgentConfig agentConfig) {
		HashMap<String, Object> attributes = new HashMap<>();
		String value = agentConfig.getValue(URIMASKSSETTING);
		if(value != null && !value.isEmpty()) {
			String[] values = value.split(",");
			if(!uri_masks.isEmpty()) uri_masks.clear();
			for(String mask : values) {
				uri_masks.add(mask);
			}
		}
		
		attributes.put("URI Masks", uri_masks);
		
		value = agentConfig.getValue(URIIGNORESSETTING);
		if(value != null && !value.isEmpty()) {
			String[] values = value.split(",");
			if(!uri_ignores.isEmpty()) uri_ignores.clear();
			for(String mask : values) {
				uri_ignores.add(mask);
			}
		}
		attributes.put("URI Ignores", uri_ignores);

		Object obj = agentConfig.getValue(URIMASKENABLEDSSETTING);
		if (obj != null) {
			if (obj instanceof Boolean) {
				masking_enabled = (Boolean)obj;
			} else {
				String temp = obj.toString();
				masking_enabled = Boolean.parseBoolean(temp);
			}
		}
		attributes.put("URI Masksing Enabled", masking_enabled);
		NewRelic.getAgent().getInsights().recordCustomEvent("Boomi_URI_Masking", attributes);
	}
}
