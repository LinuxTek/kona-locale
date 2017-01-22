/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.locale;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Collection of utilities to help work with locales
 * 
 * @version 1.0
 * @since 1.0
 */

public class KLocaleUtil {
	private static Logger logger = Logger.getLogger(KLocaleUtil.class);
	private static String bundleName = null;
	private static Locale defaultLocale = null;

	@SuppressWarnings("serial")
	public static class PhoneNumberFormat extends Format {
		private DecimalFormat df;

	    public PhoneNumberFormat() {
	        df = new DecimalFormat("##########");
	        df.setParseIntegerOnly(true);
	    }
	       
	    /** Format a number as a phone number */
	    public String format(Number num) {
	        StringBuffer sb = new StringBuffer();
	        String seg1, seg2, seg3;

	        String result = df.format(num);
	        int len = result.length();
	        if (len <= 4)
	            return result;
	           
	        seg3 = result.substring(len - 4, len);
	        seg2 = result.substring(Math.max(0, len - 7), len - 4); 
	        seg1 = result.substring(Math.max(0, len - 10), Math.max(0, len - 7));
	          
	        if(seg1 != null && seg1.trim().length() > 0) 
	            sb.append('(').append(seg1).append(") ");

	        if(seg2 != null && seg2.trim().length() > 0) 
	            sb.append(seg2).append('-'); 

	        sb.append(seg3);
	        return sb.toString();
	    }
	       
	    public StringBuffer format(Object obj, StringBuffer toAppendTo,
	    		FieldPosition pos) {
	        return toAppendTo.append(format((Number)obj));
	    }
	       
	    public Object parseObject(String source, ParsePosition pos) {
	        return df.parseObject(source, pos);
	    }
	} 
    
	public static void setBundleName(String bundleName) {
		KLocaleUtil.bundleName = bundleName;
	}

	public static void setDefaultLocale(Locale defaultLocale) {
		KLocaleUtil.defaultLocale = defaultLocale;
	}

	public static ResourceBundle getBundle() {
		return (getBundle(defaultLocale));
	}

	public static ResourceBundle getBundle(Locale locale) {
		ResourceBundle bundle = null;
		try {
			bundle = ResourceBundle.getBundle(bundleName, locale);
		} catch (MissingResourceException e) {
			throw new IllegalStateException("Resource Bundle not found:\n"
					+ "\tbundle: " + bundleName + "\n" + "\tlocale: " + locale
					+ "\n");
		}
		return (bundle);
	}

	public static String tr(String key) {
		return (tr(key, defaultLocale, (Object[]) null));
	}

	public static String tr(String key, Object... args) {
		return (tr(key, defaultLocale, args));
	}

	public static String tr(String key, Locale locale) {
		return (tr(key, locale, (Object[]) null));

	}

	/*
	 * public static String tr(String key, Locale locale, List args) { if (args
	 * == null) return (tr(key, locale, null));
	 * 
	 * return (tr(key, locale, args.toArray())); }
	 */

	public static String tr(String key, Locale locale, Object... args) {
		if (key == null)
			throw new NullPointerException("Key cannot be null");

		ResourceBundle bundle = getBundle(locale);
		String s = bundle.getString(key);

		if (s == null)
			throw new IllegalStateException("Message string not found: " + key);

		if (args != null)
			s = MessageFormat.format(s, args);

		return (s);
	}

	/**
	 * Note: this is only for currency accounts. Non currency balances should be
	 * presented as is since they could potentially be fractional (e.g. .1982732
	 * points) or might not require decimal precision at all.
	 */
	public static String getLocalizedDecimal(Number amt, Locale locale) {
		if (locale == null)
			locale = defaultLocale;

		logger.debug("default locale: " + locale + "\ncountry: "
				+ locale.getCountry());

		NumberFormat fmt = NumberFormat.getInstance(locale);
		DecimalFormat dfmt = (DecimalFormat) fmt;
		dfmt.setGroupingUsed(true);

		Currency currency = Currency.getInstance(locale);
		int digits = currency.getDefaultFractionDigits();
		dfmt.setMinimumFractionDigits(digits);
		dfmt.setMaximumFractionDigits(digits);
		// dfmt.setCurrency(currency);

		// DecimalFormatSymbols symbols = dfmt.getDecimalFormatSymbols();
		// logger.debug("SYMBOLS: " + symbols);
		// logger.debug("GROUPING SYM: " + symbols.getGroupingSeparator());

		// It appears that Java uses 0xA0 for the space character
		// as the grouping separator for certain locales (e.g. French).
		// 0xA0 is a non-breaking character that doesn't display correctly.
		// Hack: replace all 0xA0 with 0x20 (regular space).

		String amtStr = dfmt.format(amt);
		amtStr = amtStr.replace((char) 0xA0, (char) 0x20);
		return (amtStr);
	}

	// FIXME
	public static String getLocalizedCurrency(BigDecimal amount, Locale locale) {
		String isoCode = getCurrencyISOCode(locale);
		String localizedAmt = getLocalizedDecimal(amount, locale);
		return (localizedAmt + " " + isoCode);
	}

	public static String formatPhoneNumber(String phoneNumber) {
		return (formatPhoneNumber(phoneNumber, Locale.getDefault()));
	}

	// return a locale formatted phone number from the raw digits
	public static String formatPhoneNumber(String phoneNumber, Locale locale) {
		if (phoneNumber.length() == 11)
			phoneNumber = phoneNumber.substring(1);

		if (phoneNumber.length() != 10) {
			logger.warn("Invalid phone number: " + phoneNumber);
			return (phoneNumber);
		}

		try {
			Long number = Long.valueOf(phoneNumber);
			PhoneNumberFormat format = new PhoneNumberFormat();
			return (format.format(number));
		} catch (Exception e) {
			logger.error("Error formatting phone number: " + phoneNumber, e);
			return (phoneNumber);
		}
	}

    /*
	public static String toRawPhoneNumber(String phoneNumber) {
		return (toRawPhoneNumber(phoneNumber, Locale.getDefault()));
	}

	// return the raw digits from a locale formatted phone number
	public static String toRawPhoneNumber(String phoneNumber, Locale locale) {
		// get rid of all non-digits
		phoneNumber = phoneNumber.replaceAll("\\D", "");
        
		if (!KValidator.isPhoneNumber(phoneNumber)) {
			return null;
		}

        // FIXME: assume US locale for now
		if (phoneNumber.length() == 10)
			phoneNumber = "+1" + phoneNumber;

		return (phoneNumber);
	}
    */
    
	public static Map<String,String> extractPhoneNumbers(String input) throws NumberParseException {
        return extractPhoneNumbers(input, null);
	}
    
    /**
     * Extract phone numbers from a text as map of <PhoneNumberFormat.E164, Raw Match> entries.
     * 
     * @param input
     * @param country
     * @return
     * @throws NumberParseException 
     */
	public static Map<String,String> extractPhoneNumbers(String input, String country) throws NumberParseException {
		if (country == null || country.trim().length() == 0) {
			country = "US";
		}
        
		Iterable<PhoneNumberMatch> matches = PhoneNumberUtil.getInstance().findNumbers(input, country);
        
        Map<String,String> result = new HashMap<String,String>();
        
        for (PhoneNumberMatch match : matches) {
            //PhoneNumber number = match.number();
            String rawNumber = match.rawString();
            String e164Number = toE164PhoneNumber(rawNumber, country);
            result.put(e164Number, rawNumber);
        }
        
        return result;
	}
    
	public static String toE164PhoneNumber(String number) throws NumberParseException {
        return toE164PhoneNumber(number, null);
        
	}
    
    /**
     * Convert a locally formatted phone number to the E.164 standard (e.g. +12125551212).
     * 
     * If the number is already in E.164 format, it simply returns that value 
     * (ignoring the country code if one was passed in)
     * 
     * @param number
     * @param 2 letter country code (e.g. US, UK, CN, etc.)
     * @return
     * @throws NumberParseException
     */
	public static String toE164PhoneNumber(String number, String country) throws NumberParseException {
		String phoneNumber = null;

		if (country == null || country.trim().length() == 0) {
			country = "US";
		}
        
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber proto = phoneUtil.parse(number, country);
		boolean isValid = phoneUtil.isValidNumber(proto); 
		if (isValid) {
			phoneNumber = phoneUtil.format(proto, 
					com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.E164);
		}

		logger.debug("toPhoneNumber: [" + number + "] => [" + phoneNumber + "]");
		return phoneNumber;
	}

	// FIXME
	private static String getCurrencyISOCode(Locale locale) {
		return (new String());
	}
}
