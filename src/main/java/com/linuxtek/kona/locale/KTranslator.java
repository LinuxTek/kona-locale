/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.locale;

import java.util.Locale;

/**
 * Interface to translate message strings to the specified locale.
 */

public interface KTranslator {
    public String tr(String key);
    public String tr(String key, Locale locale);
    public String tr(String key, Object... args);
    public String tr(String key, Locale locale, Object... args);
}    
