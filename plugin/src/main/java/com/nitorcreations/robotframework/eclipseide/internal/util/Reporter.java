/**
 * Copyright 2015 Nitor Creations Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nitorcreations.robotframework.eclipseide.internal.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Reporter {

    private static final long REPORT_INTERVAL = 10000;
    private static final long CLEAN_INTERVAL = 60000;

    private static final Map<String, Long> reportedMessages = new HashMap<String, Long>(); // msg, lastReportedTimestamp

    private static long lastCleaned;

    /**
     * Report message to console unless the exact same message has already been reported within REPORT_INTERVAL.
     * 
     * @param msg
     *            the message to report
     */
    public static synchronized void report(String msg) {
        Long lastReported = reportedMessages.get(msg);
        long now = System.currentTimeMillis();
        if (lastReported != null && now - lastReported < REPORT_INTERVAL) {
            return;
        }
        reportedMessages.put(msg, now);
        System.out.println(msg);
        conditionallyClean(now);
    }

    private static void conditionallyClean(long now) {
        if (now - lastCleaned < CLEAN_INTERVAL) {
            return;
        }
        Iterator<Long> it = reportedMessages.values().iterator();
        while (it.hasNext()) {
            if (now - it.next() >= REPORT_INTERVAL) {
                it.remove();
            }
        }
    }
}
