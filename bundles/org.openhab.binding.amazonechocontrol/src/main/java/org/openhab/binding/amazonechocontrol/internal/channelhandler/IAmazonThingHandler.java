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
package org.openhab.binding.amazonechocontrol.internal.channelhandler;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.core.types.State;

/**
 * The {@link IAmazonThingHandler} is used from ChannelHandlers to communicate
 * with the thing
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public interface IAmazonThingHandler {

    void updateChannelState(String channelId, State state);

    void startAnnouncement(Device device, String speak, String bodyText, @Nullable String title,
            @Nullable Integer volume) throws IOException, URISyntaxException;
}
