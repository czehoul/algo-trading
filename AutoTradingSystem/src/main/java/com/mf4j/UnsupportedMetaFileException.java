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

public class UnsupportedMetaFileException
extends Exception {
    static Logger logger = LoggerFactory.getLogger((String)UnsupportedMetaFileException.class.getName());

    public UnsupportedMetaFileException(String message) {
        super(message);
        logger.debug(MFUtils.stackTrace2String(this));
    }
}

