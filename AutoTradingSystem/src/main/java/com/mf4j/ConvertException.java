/*
 * Decompiled with CFR 0_117.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.mf4j;

import com.mf4j.util.MFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertException
extends Exception {
    static Logger logger = LoggerFactory.getLogger((String)ConvertException.class.getName());

    public ConvertException(String message) {
        super(message);
        logger.debug(MFUtils.stackTrace2String(this));
    }
}

