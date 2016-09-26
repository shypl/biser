package org.shypl.biser.csi;

import org.shypl.common.util.StringUtils;
import org.slf4j.Logger;

public class CommunicationLoggingUtils {
	
	private static final String ARGS_EMPTY = "()";
	private static final String ARGS_SEP   = StringUtils.STRING_SEQUENCE_SEPARATOR;
	private static final String RESULT     = ": ";
	
	public static void logGlobalServerCall(Logger logger, String serviceName, String methodName) {
		logCall(CTS.GLOBAL_SERVER, logger, serviceName, methodName);
	}
	
	public static void logGlobalServerCall(Logger logger, String serviceName, String methodName, Object arg) {
		logCall(CTS.GLOBAL_SERVER, logger, serviceName, methodName, arg);
	}
	
	public static void logGlobalServerCall(Logger logger, String serviceName, String methodName, Object arg1, Object arg2) {
		logCall(CTS.GLOBAL_SERVER, logger, serviceName, methodName, arg1, arg2);
	}
	
	public static void logGlobalServerCall(Logger logger, String serviceName, String methodName, Object[] args) {
		logCall(CTS.GLOBAL_SERVER, logger, serviceName, methodName, args);
	}
	
	public static void logServerCall(Logger logger, String serviceName, String methodName) {
		logCall(CTS.SERVER, logger, serviceName, methodName);
	}
	
	public static void logServerCall(Logger logger, String serviceName, String methodName, Object arg) {
		logCall(CTS.SERVER, logger, serviceName, methodName, arg);
	}
	
	public static void logServerCall(Logger logger, String serviceName, String methodName, Object arg1, Object arg2) {
		logCall(CTS.SERVER, logger, serviceName, methodName, arg1, arg2);
	}
	
	public static void logServerCall(Logger logger, String serviceName, String methodName, Object[] args) {
		logCall(CTS.SERVER, logger, serviceName, methodName, args);
	}
	
	public static void logServerResponse(Logger logger, String serviceName, String methodName, Object result) {
		if (logger.isDebugEnabled()) {
			StringBuilder message = createMessage(CTS.SERVER, serviceName, methodName);
			message.append(RESULT);
			StringUtils.toString(message, result);
			logger.debug(message.toString());
		}
	}
	
	public static void logClientCall(Logger logger, String serviceName, String methodName) {
		logCall(CTS.CLIENT, logger, serviceName, methodName);
	}
	
	public static void logClientCall(Logger logger, String serviceName, String methodName, Object arg) {
		logCall(CTS.CLIENT, logger, serviceName, methodName, arg);
	}
	
	public static void logClientCall(Logger logger, String serviceName, String methodName, Object arg1, Object arg2) {
		logCall(CTS.CLIENT, logger, serviceName, methodName, arg1, arg2);
	}
	
	public static void logClientCall(Logger logger, String serviceName, String methodName, Object[] args) {
		logCall(CTS.CLIENT, logger, serviceName, methodName, args);
	}
	
	private static void logCall(CTS cts, Logger logger, String serviceName, String methodName) {
		if (logger.isDebugEnabled()) {
			logger.debug(createMessage(cts, serviceName, methodName).append(ARGS_EMPTY).toString());
		}
	}
	
	private static void logCall(CTS cts, Logger logger, String serviceName, String methodName, Object arg) {
		if (logger.isDebugEnabled()) {
			StringBuilder message = createMessage(cts, serviceName, methodName);
			message.append('(');
			StringUtils.toString(message, arg);
			message.append(')');
			logger.debug(message.toString());
		}
	}
	
	private static void logCall(CTS cts, Logger logger, String serviceName, String methodName, Object arg1, Object arg2) {
		if (logger.isDebugEnabled()) {
			StringBuilder message = createMessage(cts, serviceName, methodName);
			message.append('(');
			StringUtils.toString(message, arg1);
			message.append(ARGS_SEP);
			StringUtils.toString(message, arg2);
			message.append(')');
			logger.debug(message.toString());
		}
	}
	
	private static void logCall(CTS cts, Logger logger, String serviceName, String methodName, Object[] args) {
		if (logger.isDebugEnabled()) {
			StringBuilder message = createMessage(cts, serviceName, methodName);
			message.append('(');
			
			boolean sep = false;
			for (Object arg : args) {
				if (sep) {
					message.append(ARGS_SEP);
				}
				else {
					sep = true;
				}
				StringUtils.toString(message, arg);
			}
			
			message.append(')');
			
			logger.debug(message.toString());
		}
	}
	
	private static StringBuilder createMessage(CTS cts, String serviceName, String methodName) {
		return new StringBuilder()
			.append(cts.value)
			.append(' ')
			.append(serviceName)
			.append('.')
			.append(methodName);
	}
	
	enum CTS {
		GLOBAL_SERVER("<<<"),
		SERVER("<"),
		CLIENT(">"),;
		
		public final String value;
		
		CTS(String value) {
			this.value = value;
		}
	}
}
