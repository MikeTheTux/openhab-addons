/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.network.internal.utils;

import static org.openhab.binding.network.internal.utils.NetworkUtils.*;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Examines output lines of the ping command and tries to extract the contained latency value.
 *
 * @author Andreas Hirsch - Initial contribution
 */
@NonNullByDefault
public class LatencyParser {

    private static final Pattern LATENCY_PATTERN = Pattern.compile(".*time=(.*) ?(u|m)s.*");
    private final Logger logger = LoggerFactory.getLogger(LatencyParser.class);

    // This is how the input looks like on Mac and Linux:
    // ping -c 1 192.168.1.1
    // PING 192.168.1.1 (192.168.1.1): 56 data bytes
    // 64 bytes from 192.168.1.1: icmp_seq=0 ttl=64 time=1.225 ms
    //
    // --- 192.168.1.1 ping statistics ---
    // 1 packets transmitted, 1 packets received, 0.0% packet loss
    // round-trip min/avg/max/stddev = 1.225/1.225/1.225/0.000 ms

    // This is an example of an arping response on Linux:
    // arping -c 1 -i eth0 192.168.0.1
    // ARPING 192.168.0.1
    // 60 bytes from xx:xx:xx:xx:xx:xx (192.168.0.1): index=0 time=792.847 usec
    //
    // --- 192.168.0.1 statistics ---
    // 1 packets transmitted, 1 packets received, 0% unanswered (0 extra)
    // rtt min/avg/max/std-dev = 0.793/0.793/0.793/0.000 ms

    /**
     * Examine a single ping or arping command output line and try to extract the latency value if it is contained.
     *
     * @param inputLine Single output line of the ping or arping command.
     * @return Latency value provided by the ping or arping command. <code>null</code> if the provided line did not
     *         contain a latency value which matches the known patterns.
     */
    public @Nullable Duration parseLatency(String inputLine) {
        logger.debug("Parsing latency from input {}", inputLine);

        Matcher m = LATENCY_PATTERN.matcher(inputLine);
        if (m.find() && m.groupCount() >= 2) {
            if ("u".equals(m.group(2))) {
                return microsToDuration(Double.parseDouble(m.group(1).replace(",", ".")));
            }
            return millisToDuration(Double.parseDouble(m.group(1).replace(",", ".")));
        }

        logger.debug("Did not find a latency value");
        return null;
    }
}
