/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.guntamatic.internal;

import static org.openhab.binding.guntamatic.internal.GuntamaticBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelDefinitionBuilder;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide channelTypes for Guntamatic Heating Systems
 *
 * @author Weger Michael - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, ChannelGroupTypeProvider.class, GuntamaticChannelTypeProvider.class })
@NonNullByDefault
public class GuntamaticChannelTypeProvider implements ChannelTypeProvider, ChannelGroupTypeProvider {
    private final Map<String, ChannelType> channelTypes = new ConcurrentHashMap<>();
    private final Map<String, ChannelGroupType> channelGroupTypes = new ConcurrentHashMap<>();
    private final List<ChannelDefinition> channelDefinitions = new ArrayList<ChannelDefinition>();

    private final Logger logger = LoggerFactory.getLogger(GuntamaticChannelTypeProvider.class);

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (channelTypes.containsKey(channelTypeUID.getAsString())) {
            return channelTypes.get(channelTypeUID.getAsString());
        }
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        return channelGroupTypes.values();
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        if (channelGroupTypes.containsKey(channelGroupTypeUID.getAsString())) {
            return channelGroupTypes.get(channelGroupTypeUID.getAsString());
        }
        return null;
    }

    public void addChannelType(ChannelTypeUID channelTypeUID, String label, String itemType, String description,
            boolean advanced, String pattern) {
        try {

            StateDescriptionFragmentBuilder stateDescriptionFragmentBuilder = StateDescriptionFragmentBuilder.create()
                    .withReadOnly(true);
            if (!pattern.isEmpty()) {
                stateDescriptionFragmentBuilder.withPattern(pattern);
            }
            StateChannelTypeBuilder stateChannelTypeBuilder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                    .withDescription(description).isAdvanced(advanced)
                    .withStateDescriptionFragment(stateDescriptionFragmentBuilder.build());
            channelTypes.put(channelTypeUID.getAsString(), stateChannelTypeBuilder.build());

            channelDefinitions.add(new ChannelDefinitionBuilder("foo", channelTypeUID).build());
        } catch (Exception e) {
            logger.warn("Failed creating channelType {}: {} ", channelTypeUID, e.getMessage());
        }
    }

    public void addChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, String label, String itemType,
            String description, boolean advanced, String pattern) {

        try {
            ChannelGroupType channelGroupType = ChannelGroupTypeBuilder.instance(channelGroupTypeUID, "Channel Group")
                    .withDescription("Channel Group").withChannelDefinitions(channelDefinitions).build();
            channelGroupTypes.put(channelGroupTypeUID.getAsString(), channelGroupType);
        } catch (Exception e) {
            logger.warn("Failed creating channelType {}: {} ", channelGroupTypeUID, e.getMessage());
        }
    }
}
