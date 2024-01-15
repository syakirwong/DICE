package com.alliance.diceintegration.controller;

import org.springframework.http.HttpHeaders;

import java.util.Locale;

public abstract class BaseController {
    public Locale getLocale(HttpHeaders headers) {
		if (headers.get("accept-language") != null) {
			return new Locale(headers.get("accept-language").get(0));
		}

		return Locale.ENGLISH;
	}
}
