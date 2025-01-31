package com.aril.arilbatchsdk.core.item.excel.support;

import com.aril.valhala.util.LocaleUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum ExcelThousandSeparatorFormat {
    DOT("###.###,##", new Locale("tr", "TR")),// Turkish-style dot
    COMMA("###,###.##", Locale.ENGLISH); // English-style comma

    public static final List<ExcelThousandSeparatorFormat> _VALUES = List.of(values());

    private final String format;
    private final Locale locale;

    public static ExcelThousandSeparatorFormat findByLocale(Locale locale) {
        locale = Objects.requireNonNullElse(locale, LocaleUtils.getSystemLocale());

        for (ExcelThousandSeparatorFormat separatorFormat : _VALUES) {
            if (separatorFormat.getLocale().getLanguage().equals(locale.getLanguage())) {
                return separatorFormat;
            }
        }

        return ExcelThousandSeparatorFormat.COMMA;
    }
}
