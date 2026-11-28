/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler.CommandExtensionData;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;

import io.calimero.KNXException;
import io.calimero.link.KNXNetworkLink;
import io.calimero.secure.Security;

/**
 * Tests for {@link AbstractKNXClient}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class AbstractKNXClientTest {

    private static class FailTwiceClient extends AbstractKNXClient {
        private int attempts;

        FailTwiceClient(ScheduledExecutorService scheduler, StatusUpdateCallback callback) {
            super(60, new ThingUID("knx:ip:test"), 2, 50, 3, scheduler, new CommandExtensionData(new TreeMap<>()),
                    Security.newSecurity(), callback);
        }

        @Override
        protected KNXNetworkLink establishConnection() throws KNXException {
            switch (attempts++) {
                case 0:
                    throw new KNXException("gateway not reachable");
                case 1:
                    throw new IllegalStateException("no usable network interface");
                default:
                    return new DummyKNXNetworkLink();
            }
        }
    }

    @Test
    void scheduledRetryReconnectsAfterUnexpectedRuntimeException() throws Exception {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        doReturn(mock(ScheduledFuture.class)).when(scheduler).schedule(ArgumentMatchers.<Callable<Boolean>> any(),
                anyLong(), any(TimeUnit.class));
        StatusUpdateCallback callback = mock(StatusUpdateCallback.class);
        FailTwiceClient client = new FailTwiceClient(scheduler, callback);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Callable<Boolean>> retry = ArgumentCaptor.forClass(Callable.class);

        // the checked failure leaves a retry in connectJob
        client.initialize();
        verify(scheduler).schedule(retry.capture(), anyLong(), eq(TimeUnit.SECONDS));

        // the retry runs into the runtime exception and has to schedule the next attempt itself
        assertFalse(retry.getValue().call());
        verify(scheduler, times(2)).schedule(retry.capture(), anyLong(), eq(TimeUnit.SECONDS));

        assertTrue(retry.getValue().call());
        verify(callback).updateStatus(ThingStatus.ONLINE);
        assertTrue(client.isConnected());
        client.dispose();
    }
}
