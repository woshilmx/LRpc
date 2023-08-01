package com.lmx.core.shudown;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
@Slf4j
public class LrpcShutdoemHooh extends Thread {
    @Override
    public void run() {
        log.info("开始准备停机");
//        开启挡板
        ShudownHookContant.IS_OPEN = new AtomicBoolean(true);
//        计数器阻塞
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (ShudownHookContant.REQUEST_COUNT.sum() == 0) {
                break;
            }
        }
//        开始停机
        log.info("已停机");

    }
}
