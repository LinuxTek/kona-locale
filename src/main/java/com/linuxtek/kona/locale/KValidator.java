/*
 * Copyright (C) 2012 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.locale;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.linuxtek.kona.util.KDateUtil;

/**
 * Methods to validate emails, phone numbers, etc.
 *
 *  http://interviewjava.blogspot.com/2008/03/some-commonly-used-validations-like.html
 * @version 1.0
 * @since 1.0
 */

public class KValidator {
    private static Logger logger = Logger.getLogger(KValidator.class);


	public static boolean isEmail(String email) {
		boolean isValid = false;

		if (email == null)
			return (false);

		/*
		 * Examples: The following email addresses will pass validation
		 * asdf@tttdd.com xyzzz@tyyy.in zxg.asd@tx.govt
		 */

		String emailExpression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;

		Pattern pattern = Pattern.compile(emailExpression,
				Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(inputStr);

		if (matcher.matches())
			isValid = true;

		return (isValid);
	}
	
	public static boolean isEmail2(String email) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}

    /**
    * isPhoneNumberValid: Validate phone number using Java reg ex. This method
    * checks if the input string is a valid phone number.
    *
    * @param phoneNumber
    *            String. Phone number to validate
    * @return boolean: true if phone number is valid, false otherwise.
    */

	/*
    public static boolean isPhoneNumber(String phoneNumber) 
    {
        boolean isValid = false;

        if (phoneNumber == null)
            return (false);

        //
        // Examples: Matches following phone numbers: (123)456-7890,
        // 123-456-7890, 1234567890, (123)-456-7890
        //
        // should also match: 123.456.7890, +12345678900, 12345678900
        // 
        // 

        String phoneNumberExpression = 
            // "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";
            "^\\+?1?\\(?(\\d{3})\\)?[\\s\\.-]?(\\d{3})[\\.-]?(\\d{4})$";

        CharSequence inputStr = phoneNumber;
        Pattern pattern = Pattern.compile(phoneNumberExpression);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) 
            isValid = true;

        return(isValid);
    }
	*/
    
	
    public static boolean isPhoneNumber(String phoneNumber) {
		return isPhoneNumber(phoneNumber, "US");
	}
	
    public static boolean isPhoneNumber(String phoneNumber, String countryCode) {
    	if (phoneNumber == null || countryCode == null) return false;
    	
    	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    	PhoneNumber proto;
		try {
			proto = phoneUtil.parse(phoneNumber, countryCode);
		} catch (NumberParseException e) {
			return false;
		}
    	return phoneUtil.isValidNumber(proto);
	}
	
    public static boolean isE164PhoneNumber(String phoneNumber) {
    	if (phoneNumber == null || !phoneNumber.startsWith("+")) return false;
    	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    	
    	// NOTE: defaultRegion can be null if phoneNumber is guaranteed to start with a "+"
    	PhoneNumber proto;
		try {
			proto = phoneUtil.parse(phoneNumber, null);
		} catch (NumberParseException e) {
			return false;
		}
    	return phoneUtil.isValidNumber(proto); 
    }


    /**
     * isSSNValid: Validate Social Security number (SSN) using Java reg ex. This
     * method checks if the input string is a valid SSN.
     *
     * @param ssn
     *            String. Social Security number to validate
     * @return boolean: true if social security number is valid, false
     *         otherwise.
     */

    public static boolean isSSN(String ssn) 
    {
        boolean isValid = false;

        if (ssn == null)
            return (false);

        /*
         * SSN format xxx-xx-xxxx, xxxxxxxxx, xxx-xxxxxx; xxxxx-xxxx: Examples:
         * 879-89-8989; 869878789 etc.
         */

        String ssnExpression = "^\\d{3}[- ]?\\d{2}[- ]?\\d{4}$";
        CharSequence inputStr = ssn;
        Pattern pattern = Pattern.compile(ssnExpression);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches())
            isValid = true;

        return (isValid);
    }

    /**
     * isNumeric: Validate a number using Java regex. This method checks if the
     * input string contains all numeric characters.
     *
     * @param email
     *            String. Number to validate
     * @return boolean: true if the input is all numeric, false otherwise.
     */

    public static boolean isNumeric(String number) 
    {
        boolean isValid = false;

        if (number == null)
            return (false);

        /*
         * Number: A numeric value will have following format:
         *
         * ^[-+]? : Starts with an optional "+" or "-" sign. [0-9]* : May have
         * one or more digits. \\.? : May contain an optional "." (decimal
         * point) character. [0-9]+$ : ends with numeric digit.
         *
         */

        String regex = "((-|\\+)?[0-9]+(\\.[0-9]+)?)+";
        //String regex = "^[-+]?[0-9]*\\.?[0-9]+$";
        CharSequence inputStr = number;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches())
            isValid = true;

        return (isValid);
    }

    // varMap: rule variable map
    private static JEP getJEP(Map<String,Object> varMap) {
        //Map<String,Object> varMap = getRuleVarMap();

        if (varMap == null) {
            logger.debug("getJEP: varMap is null");
            return (null);
        }

        JEP jep = new JEP();
        jep.setAllowUndeclared(true);

        Set<String> keySet = varMap.keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            String name = it.next();
            Object value = varMap.get(name);
            jep.addVariable(name, value);
        }

        logger.debug("getJEP: all vars added: " + getJEPSymbolTable(jep));
        return (jep);
    }

    @SuppressWarnings("rawtypes")
	private static String getJEPSymbolTable(JEP jep) {
        StringBuffer s = new StringBuffer();
        SymbolTable syms = jep.getSymbolTable();
        Iterator it = syms.keySet().iterator();

        while (it.hasNext()) {
            String name = (String) it.next();
            Object value = syms.getValue(name);
            s.append("\n\nname: ");
            s.append(name);
            s.append("\nvalue: ");
            s.append(value);
        }
        return (s.toString());
    }

    public static Boolean matchRule(Map<String,Object> varMap, String rule) {
        if (rule == null) {
            return (false);
        }

        // fix me .. should cache each jep instance for each unique varMap
        JEP jep = getJEP(varMap);

        if (jep == null) {
            return (false);
        }

        logger.debug("evaluating rule: \n\t[" + rule + "]");

        jep.parseExpression(rule);

        if (jep.hasError()) {
            logger.error("JEP parse error: " + jep.getErrorInfo());
            logger.error(getJEPSymbolTable(jep));
            return (false);
        }

        Double ok = (Double) jep.getValueAsObject();

        if (ok != null && ok.equals(1.0)) {
            logger.debug("rule matched: \n\t[" + rule + "]");
            return (true);
        }
            
        // ok can be null if rule contained a variable not included
        // in the varMap

        logger.debug("no rule match: \n\t[" + rule + "]");
        return (false);
    }

    // simply checks if the given month and year have already passed
    public static Boolean isExpirationDateValid(Integer month, Integer year) {
        int day = 1;

        if (month < 0) {
            return (false);
        }

        if (year < 0) {
            return (false);
        }

        month += 1;
        if (month == 13) {
            month = 1;
            year += 1;
        }

        Date expireDate = KDateUtil.getDate(year, month, day);
        Date today = new Date();

        if (today.getTime() > expireDate.getTime()) {
            return (false);
        }
        return (true);
    }
}
