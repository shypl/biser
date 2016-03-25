package org.shypl.biser.csi;

import org.shypl.common.util.StringUtils;
import org.slf4j.Logger;

public class CommunicationLoggingUtils {

	private static final String ARGS_EMPTY = "()";
	private static final String ARGS_SEP   = StringUtils.STRING_SEQUENCE_SEPARATOR;
	private static final String RESULT     = ": ";

	public static void logServerCall(Logger logger, String serviceName, String methodName) {
		logCall(false, logger, serviceName, methodName);
	}

	public static void logServerCall(Logger logger, String serviceName, String methodName, Object arg) {
		logCall(false, logger, serviceName, methodName, arg);
	}

	public static void logServerCall(Logger logger, String serviceName, String methodName, Object arg1, Object arg2) {
		logCall(false, logger, serviceName, methodName, arg1, arg2);
	}

	public static void logServerCall(Logger logger, String serviceName, String methodName, Object[] args) {
		logCall(false, logger, serviceName, methodName, args);
	}

	public static void logServerResponse(Logger logger, String serviceName, String methodName, Object result) {
		if (logger.isTraceEnabled()) {
			StringBuilder message = createMessage(false, serviceName, methodName);
			message.append(RESULT);
			StringUtils.toString(message, result);
			logger.trace(message.toString());
		}
	}

	public static void logClientCall(Logger logger, String serviceName, String methodName) {
		logCall(true, logger, serviceName, methodName);
	}

	public static void logClientCall(Logger logger, String serviceName, String methodName, Object arg) {
		logCall(true, logger, serviceName, methodName, arg);
	}

	public static void logClientCall(Logger logger, String serviceName, String methodName, Object arg1, Object arg2) {
		logCall(true, logger, serviceName, methodName, arg1, arg2);
	}

	public static void logClientCall(Logger logger, String serviceName, String methodName, Object[] args) {
		logCall(true, logger, serviceName, methodName, args);
	}

	private static void logCall(boolean cts, Logger logger, String serviceName, String methodName) {
		if (logger.isTraceEnabled()) {
			logger.trace(createMessage(cts, serviceName, methodName).append(ARGS_EMPTY).toString());
		}
	}

	private static void logCall(boolean cts, Logger logger, String serviceName, String methodName, Object arg) {
		if (logger.isTraceEnabled()) {
			StringBuilder message = createMessage(cts, serviceName, methodName);
			message.append('(');
			StringUtils.toString(message, arg);
			message.append(')');
			logger.trace(message.toString());
		}
	}

	private static void logCall(boolean cts, Logger logger, String serviceName, String methodName, Object arg1, Object arg2) {
		if (logger.isTraceEnabled()) {
			StringBuilder message = createMessage(cts, serviceName, methodName);
			message.append('(');
			StringUtils.toString(message, arg1);
			message.append(ARGS_SEP);
			StringUtils.toString(message, arg2);
			message.append(')');
			logger.trace(message.toString());
		}
	}

	private static void logCall(boolean cts, Logger logger, String serviceName, String methodName, Object[] args) {
		if (logger.isTraceEnabled()) {
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

			logger.trace(message.toString());
		}
	}

	private static StringBuilder createMessage(boolean cts, String serviceName, String methodName) {
		return new StringBuilder()
			.append(cts ? '>' : '<')
			.append(' ')
			.append(serviceName)
			.append('.')
			.append(methodName);
	}
}
