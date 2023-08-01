package com.lmx.core.shudown;

import com.sun.javafx.logging.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * 停机时涉及到的全部变量
 */
public class ShudownHookContant {
    public static AtomicBoolean IS_OPEN = new AtomicBoolean(false);
    public static LongAdder REQUEST_COUNT = new LongAdder();
}
