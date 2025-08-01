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
package org.openhab.binding.zwavejs.internal.type;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.zwavejs.internal.config.ColorCapability;
import org.openhab.core.thing.Channel;

/**
 * This class represents the result of the ZwaveJS type generator.
 * It contains a map of channels, where the key is a string and the value is a Channel object.
 *
 * @see Channel
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSTypeGeneratorResult {

    public Map<String, Channel> channels = new HashMap<>();
    public Map<String, Object> values = new HashMap<>();
    public Map<Integer, ColorCapability> colorCapabilities = new HashMap<>();
    public String location = "";
}
