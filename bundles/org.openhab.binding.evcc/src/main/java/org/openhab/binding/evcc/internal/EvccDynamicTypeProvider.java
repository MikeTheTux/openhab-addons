/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EvccDynamicTypeProvider} is an instance of a {@link AbstractStorageBasedTypeProvider} for the
 * Evcc binding
 *
 * @author MikeTheTux - Initial contribution
 */
@Component(service = { EvccDynamicTypeProvider.class, ChannelGroupTypeProvider.class })
@NonNullByDefault
public class EvccDynamicTypeProvider extends AbstractStorageBasedTypeProvider {

    @Activate
    public EvccDynamicTypeProvider(@Reference StorageService storageService) {
        super(storageService);
    }

    public void removeChannelGroupTypesForThing(ThingUID uid) {
        String thingUid = uid.getAsString() + ":";
        getChannelGroupTypes(null).stream().map(ChannelGroupType::getUID)
                .filter(c -> c.getAsString().startsWith(thingUid)).forEach(this::removeChannelGroupType);
    }
}
