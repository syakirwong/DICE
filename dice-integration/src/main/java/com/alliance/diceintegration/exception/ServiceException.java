package com.alliance.diceintegration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ServiceException extends Exception {
	private static final long serialVersionUID = 1L;

	private final int errorCode;
	private String description;
	private Object[] messageArgs;

	public ServiceException(int errorCode, String message, String description) {
		super(message);
		this.errorCode = errorCode;
		this.description = description;

		log.error(String.format("Someone is throwing %s - %s - %s ", this.errorCode, message, this.description));
	}

	public ServiceException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public ServiceException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
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