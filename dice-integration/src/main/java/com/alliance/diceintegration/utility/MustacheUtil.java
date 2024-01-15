package com.alliance.diceintegration.utility;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.springframework.stereotype.Component;

import com.alliance.diceintegration.constant.MustacheItem;
import com.alliance.diceintegration.exception.ServiceException;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MustacheUtil {
    public static String fillContentByMustache(String content, MustacheItem item) throws ServiceException {

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile(new StringReader(content), "");

        StringWriter writer = new StringWriter();
        try {
            m.execute(writer, item).flush();
        } catch (IOException e1) {
            log.error(e1.toString());
        }

        return writer.toString();
    }
}