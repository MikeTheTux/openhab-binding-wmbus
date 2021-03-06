/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.itron;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wmbus.BindingConfiguration;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.discovery.WMBusDiscoveryParticipant;
import org.openmuc.jmbus.DeviceType;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers itron devices and adds additional layer for decoding on top of generic WM-Bus devices.
 *
 * @author Łukasz Dywicki - initial contribution.
 */
@Component
public class ItronDiscoveryParticipant implements WMBusDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(ItronDiscoveryParticipant.class);

    private BindingConfiguration configuration;

    @Override
    public @NonNull Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return ItronBindingConstants.SUPPORTED_THING_TYPES;
    }

    @Override
    public @Nullable ThingUID getThingUID(WMBusDevice device) {
        ThingTypeUID type = ItronBindingConstants.THING_TYPE_ITRON_SMOKE_DETECTOR;
        if (configuration.getIncludeBridgeUID()) {
            return new ThingUID(type, device.getAdapter().getUID(), device.getDeviceId());
        }
        return new ThingUID(type, device.getDeviceId());
    }

    @Override
    public DiscoveryResult createResult(WMBusDevice device) {
        if (isItronSmokeDetector(device)) {
            String label = "Itron smoke detector #" + device.getDeviceId();

            Map<String, Object> properties = new HashMap<>();
            properties.put(WMBusBindingConstants.PROPERTY_DEVICE_ADDRESS, device.getDeviceAddress());
            properties.put(Thing.PROPERTY_VENDOR, "Itron");
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getDeviceId());
            SecondaryAddress secondaryAddress = device.getOriginalMessage().getSecondaryAddress();
            properties.put(Thing.PROPERTY_MODEL_ID, secondaryAddress.getVersion());
            properties.put(WMBusBindingConstants.PROPERTY_DEVICE_ENCRYPTED, device.isEnrypted());

            ThingUID thingUID = getThingUID(device);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(WMBusBindingConstants.PROPERTY_DEVICE_ADDRESS).withLabel(label)
                    .withThingType(ItronBindingConstants.THING_TYPE_ITRON_SMOKE_DETECTOR)
                    .withBridge(device.getAdapter().getUID()).withLabel(label).withTTL(getTimeToLive()).build();
        }

        return null;
    }

    private boolean isItronSmokeDetector(WMBusDevice device) {
        WMBusMessage message = device.getOriginalMessage();

        if (!ItronBindingConstants.ITRON_MANUFACTURER_ID.equals(message.getSecondaryAddress().getManufacturerId()) ||
            message.getSecondaryAddress().getDeviceType() != DeviceType.SMOKE_DETECTOR) {
            return false;
        }

        return true;
    }

    @Reference
    public void setBindingConfiguration(BindingConfiguration configuration) {
        this.configuration = configuration;
    }

    public void unsetBindingConfiguration(BindingConfiguration configuration) {
        this.configuration = null;
    }

    private Long getTimeToLive() {
        return configuration.getTimeToLive();
    }

}
