package com.aril.arilbatchsdk.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
public class MessageSourceService {
    private final MessageSource messageSource;
    private final Locale locale;

    private static final String[] EMPTY_ARRAY = new String[]{};

    public MessageSourceService(MessageSource messageSource) {
        this.messageSource = messageSource;
        this.locale = Locale.getDefault();
    }

    public String getMessage(String key) {
        return getMessage(key, null);
    }

    public String getMessage(String key, String[] args) {
        return getMessage(key, args, locale);
    }

    public String getMessage(String key, String[] args, Locale requestedLocale) {
        try {
            requestedLocale = Objects.requireNonNullElse(requestedLocale, Locale.getDefault());
            args = Objects.requireNonNullElse(args, EMPTY_ARRAY);
            return messageSource.getMessage(key, args, requestedLocale);
        } catch (Exception ex) {
            log.error("Unexpected exception occurred on message-localization", ex);
            return key;
        }
    }
}
