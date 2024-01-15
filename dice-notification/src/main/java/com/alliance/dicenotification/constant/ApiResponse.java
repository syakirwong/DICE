package com.alliance.dicenotification.constant;

import java.util.List;

import org.springframework.http.HttpStatus;

public class ApiResponse {

	public int responseCode;
	public boolean success;
	public String message;
	public Object result;
	public List<ErrorDetails> errors;
	
	public ApiResponse(int responseCode, boolean success, String message) {
		this.responseCode = responseCode;
		this.success = success;
		this.message = message == null ? "Success" : message;
	}

	public ApiResponse(int responseCode, boolean success, String message, Object result) {
		this.responseCode = responseCode;
		this.success = success;
		this.message = message == null ? "Success" : message;
		this.result = result;
	}
	
	public void setErrors(List<ErrorDetails> errors) {
		this.errors = errors;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public boolean getSuccess() {
		return success;
	}
	
	public HttpStatus getHttpStatus() {
		return HttpStatus.valueOf(responseCode);
	}

	public String getMessage() {
		return message;
	}

	public Object getResult() {
		return result;
	}

	// CODES ARE FROM https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
	// INFORMATION
	public static final int HTTP_RESPONSE_CONTINUE = 100;
	public static final int HTTP_RESPONSE_SWITCHING_PROTOCOLS = 101;
	public static final int HTTP_RESPONSE_PROCESSING = 102;
	public static final int HTTP_RESPONSE_EARLY_HINTS_RFC8297 = 103;

	// SUCCESS
	public static final int HTTP_RESPONSE_OK = 200;
	public static final int HTTP_RESPONSE_CREATED = 201;
	public static final int HTTP_RESPONSE_ACCEPTED = 202;
	public static final int HTTP_RESPONSE_NON_AUTHORIZED = 203;
	public static final int HTTP_RESPONSE_NO_CONTENT = 204;
	public static final int HTTP_RESPONSE_RESET_CONTENT = 205;
	public static final int HTTP_RESPONSE_PARTIAL_CONTENT = 206;
	public static final int HTTP_RESPONSE_MULTI_STATUS = 207;
	public static final int HTTP_RESPONSE_ALREADY_REPORTED = 208;
	public static final int HTTP_RESPONSE_IM_USED = 226;

	// REDIRECTION
	public static final int HTTP_RESPONSE_MULTIPLE_CHOICE = 300;
	public static final int HTTP_RESPONSE_MOVED_PERMANENTLY = 301;
	public static final int HTTP_RESPONSE_FOUND = 302;
	public static final int HTTP_RESPONSE_SEE_OTHER = 303;
	public static final int HTTP_RESPONSE_NOT_MODIFIED = 304;
	public static final int HTTP_RESPONSE_USE_PROXY = 305;
	public static final int HTTP_RESPONSE_SWITCH_PROXY = 306;
	public static final int HTTP_RESPONSE_TEMPORARY_REDIRECT = 307;
	public static final int HTTP_RESPONSE_PERMANENT_REDIRECT = 308;

	// CLIENT ERRORS
	public static final int HTTP_RESPONSE_BAD_REQUEST = 400;
	public static final int HTTP_RESPONSE_UNAUTHORIZED_RFC7235 = 401;
	public static final int HTTP_RESPONSE_PAYMENT_REQUIRED = 402;
	public static final int HTTP_RESPONSE_FORBIDDEN = 403;
	public static final int HTTP_RESPONSE_NOT_FOUND = 404;
	public static final int HTTP_RESPONSE_METHOD_NOT_ALLOWED = 405;
	public static final int HTTP_RESPONSE_NOT_ACCEPTABLE = 406;
	public static final int HTTP_RESPONSE_PROXY_AUTHENTICATION_REQUIRED_RFC7235 = 407;
	public static final int HTTP_RESPONSE_REQUEST_TIMEOUT = 408;
	public static final int HTTP_RESPONSE_CONFLICT = 409;
	public static final int HTTP_RESPONSE_GONE = 410;
	public static final int HTTP_RESPONSE_LENGTH_REQUIRED = 411;
	public static final int HTTP_RESPONSE_PRECONDITION_FAILED_RFC7232 = 412;
	public static final int HTTP_RESPONSE_PAYLOAD_TOO_LARGE_RFC7231 = 413;
	public static final int HTTP_RESPONSE_URI_TOO_LONG_RFC7231 = 414;
	public static final int HTTP_RESPONSE_UNSUPPORTED_MEDIA_TYPE_RFC7231 = 415;
	public static final int HTTP_RESPONSE_RANGE_NOT_SATISFIABLE_RFC7233 = 416;
	public static final int HTTP_RESPONSE_EXPECTATION_FAILED = 417;
	public static final int HTTP_RESPONSE_IM_TEAPOT_RFC2324_RFC7168 = 418;
	public static final int HTTP_RESPONSE_MISDIRECTED_REQUEST_RFC7540 = 421;
	public static final int HTTP_RESPONSE_UNPROCESSABLE_ENTITY_RFC4918 = 422;
	public static final int HTTP_RESPONSE_LOCKED_RFC4918 = 423;
	public static final int HTTP_RESPONSE_FAILED_DEPENDENCY_RFC4918 = 424;
	public static final int HTTP_RESPONSE_TOO_EARLY_RFC8470 = 425;
	public static final int HTTP_RESPONSE_UPGRADE_REQUIRED = 426;
	public static final int HTTP_RESPONSE_PRECONDITION_REQUIRED_RFC6585 = 428;
	public static final int HTTP_RESPONSE_TOO_MANY_REQUEST_RFC6585 = 429;
	public static final int HTTP_RESPONSE_REQUEST_HEADER_FIELDS_TOO_LARGE_RFC6585 = 431;
	public static final int HTTP_RESPONSE_NA_FOR_LEGAL_REASON_RFC7725 = 451;

	// SERVER ERROR
	public static final int HTTP_RESPONSE_INTERNAL_SERVER_ERROR = 500;
	public static final int HTTP_RESPONSE_NOT_IMPLEMENTED = 501;
	public static final int HTTP_RESPONSE_BAD_GATEWAY = 502;
	public static final int HTTP_RESPONSE_SERVICE_UNAVAILABLE = 503;
	public static final int HTTP_RESPONSE_GATEWAY_TIMEOUT = 504;
	public static final int HTTP_RESPONSE_HTTP_VERSION_NOT_SUPPORTED = 505;
	public static final int HTTP_RESPONSE_VARIANT_ALSO_NEGOTIATES_RFC2295 = 506;
	public static final int HTTP_RESPONSE_INSUFFICIENT_STORAGE_RFC4918 = 507;
	public static final int HTTP_RESPONSE_LOOP_DETECTED_RFC5892 = 508;
	public static final int HTTP_RESPONSE_NOT_EXTENDED_RFC2274 = 510;
	public static final int HTTP_RESPONSE_NETWORK_AUTHENTICATION_REQUIRED_RFC6585 = 511;

	// CUSTOM RESPONSE CODE
	public static final int HTTP_RESPONSE_JWT_EXCEPTION = 900;
	public static final int HTTP_RESPONSE_JWT_EXCEPTION_EXPIRED = 901;
	public static final int HTTP_RESPONSE_JWT_EXCEPTION_UNSUPPORTED = 902;
	public static final int HTTP_RESPONSE_JWT_EXCEPTION_MALFORMED = 903;
	public static final int HTTP_RESPONSE_JWT_EXCEPTION_SIGNATURE = 904;
	public static final int HTTP_RESPONSE_JWT_EXCEPTION_ILLEGAL = 905;
	public static final int HTTP_RESPONSE_JWT_INVALID_ACCESS_TOKEN = 999;
}
