package com.alliance.dicenotification.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ServiceException extends Exception {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(ServiceException.class);

	private final int errorCode;
	private String description;
	private Object[] messageArgs;

	public ServiceException(int errorCode, String message, String description) {
		super(message);
		this.errorCode = errorCode;
		this.description = description;

		logger.error(String.format("Somebody throwing %s - %s - %s ", this.errorCode, message, this.description));
	}

	public ServiceException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public ServiceException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public ServiceException(int errorCode, String message, Object[] messageArgs) {
		super(message);
		this.errorCode = errorCode;
		this.messageArgs = messageArgs;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return description;
	}
	
	public Object[] getMessageArgs() {
		return messageArgs;
	}

}