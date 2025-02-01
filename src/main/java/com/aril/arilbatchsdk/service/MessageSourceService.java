package com.aril.arilbatchsdk.service;

import com.aril.valhala.context.LocaleContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
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
        this.locale = LocaleContext.getLocale();
    }

    public String getMessage(String key) {
        return getMessage(key, null);
    }

    public String getMessage(String key, String[] args) {
        return getMessage(key, args, locale);
    }

    public String getMessage(String key, String[] args, Locale requestedLocale) {
        try {
            return messageSource.getMessage(
                    key,
                    Objects.requireNonNullElse(args, EMPTY_ARRAY),
                    Objects.requireNonNullElse(requestedLocale, LocaleContext.getLocale()));
        } catch (NoSuchMessageException ex) {
            log.error("Message key not found with: {}", key);
            return key;
        }
    }
}
