/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.device.ADEUNISGasMeter.ADEUNISGasMeterHandler;
import org.openhab.binding.wmbus.device.EngelmannHeatMeter.EngelmannHeatMeterHandler;
import org.openhab.binding.wmbus.device.Meter;
import org.openhab.binding.wmbus.device.UnknownMeter.UnknownWMBusDeviceHandler;
import org.openhab.binding.wmbus.discovery.CompositeMessageListener;
import org.openhab.binding.wmbus.handler.KamstrupMultiCal302Handler;
import org.openhab.binding.wmbus.handler.QundisQCaloricHandler;
import org.openhab.binding.wmbus.handler.QundisQHeatHandler;
import org.openhab.binding.wmbus.handler.QundisQWaterHandler;
import org.openhab.binding.wmbus.handler.TechemHKVHandler;
import org.openhab.binding.wmbus.handler.WMBusBridgeHandler;
import org.openhab.binding.wmbus.handler.WMBusMessageListener;
import org.openhab.binding.wmbus.handler.WMBusVirtualBridgeHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * The {@link WMBusHandlerFactory} class defines WMBusHandlerFactory. This class is the main entry point of the binding.
 *
 * @author Hanno - Felix Wagner - Roman Malyugin - Initial contribution
 */

@Component(service = { WMBusHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class })
public class WMBusHandlerFactory extends BaseThingHandlerFactory {

    // private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final Set<Meter> knownDeviceTypes = Collections.synchronizedSet(new LinkedHashSet<>());

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(WMBusHandlerFactory.class);

    private final Set<ThingTypeUID> supportedThingTypes = new LinkedHashSet<>();

    private final CompositeMessageListener messageListener = new CompositeMessageListener();

    public WMBusHandlerFactory() {
        logger.debug("wmbus binding starting up.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return getSupportedThingTypes().contains(thingTypeUID);
    }

    protected Set<ThingTypeUID> getSupportedThingTypes() {
        if (supportedThingTypes.isEmpty()) {
            supportedThingTypes.addAll(calculateSupportedThingTypes());
        }

        return supportedThingTypes;
    }

    private Set<ThingTypeUID> calculateSupportedThingTypes() {
        Set<ThingTypeUID> knownDevices = knownDeviceTypes.stream().map(Meter::getSupportedThingTypes)
                .flatMap(Collection::stream).collect(Collectors.toSet());

        return ImmutableSet.<ThingTypeUID> builder()
                .addAll(Iterables.concat(WMBusBridgeHandler.SUPPORTED_THING_TYPES,
                        QundisQCaloricHandler.SUPPORTED_THING_TYPES, QundisQWaterHandler.SUPPORTED_THING_TYPES,
                        QundisQHeatHandler.SUPPORTED_THING_TYPES, KamstrupMultiCal302Handler.SUPPORTED_THING_TYPES,
                        WMBusVirtualBridgeHandler.SUPPORTED_THING_TYPES, knownDevices))
                .build();
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_BRIDGE)) {
            // create handler for WMBus bridge
            logger.debug("Creating (handler for) WMBus bridge.");
            if (thing instanceof Bridge) {
                WMBusBridgeHandler handler = new WMBusBridgeHandler((Bridge) thing);
                handler.registerWMBusMessageListener(messageListener);
                return handler;
            } else {
                return null;
            }
            // add new devices here
        }
        if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_VIRTUAL_BRIDGE)) {
            logger.debug("Creating (handler for) WMBus virtual bridge.");
            if (thing instanceof Bridge) {
                return new WMBusVirtualBridgeHandler((Bridge) thing);
            } else {
                return null;
            }
            // add new devices here
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_TECHEM_HKV)) {
            logger.debug("Creating (handler for) TechemHKV device.");
            return new TechemHKVHandler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_QUNDIS_QCALORIC_5_5)) {
            logger.debug("Creating (handler for) Qundis Qcaloric 5,5 device.");
            return new QundisQCaloricHandler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_QUNDIS_QWATER_5_5)) {
            logger.debug("Creating (handler for) Qundis Qwater 5,5 device.");
            return new QundisQWaterHandler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_QUNDIS_QHEAT_5)) {
            logger.debug("Creating (handler for) Qundis Qheat 5 device.");
            return new QundisQHeatHandler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_KAMSTRUP_MULTICAL_302)) {
            logger.debug("Creating (handler for) Kamstrup MultiCal 302 device.");
            return new KamstrupMultiCal302Handler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_ADEUNIS_GAS_METER_3)) {
            logger.debug("Creating (handler for) ADEUNIS_RF Gas Meter (v.3) device.");
            return new ADEUNISGasMeterHandler(thing);
        } else if (thingTypeUID.equals(WMBusBindingConstants.THING_TYPE_ENGELMANN_SENSOSTAR)) {
            logger.debug("Creating (handler for) Engelmann Heat Meter device.");
            return new EngelmannHeatMeterHandler(thing);
        } else {
            logger.debug("Creating (handler for) Unknown device.");
            return new UnknownWMBusDeviceHandler(thing);
        }
    }

    @Override
    @Activate
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Override
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void registerWMBusMessageListener(WMBusMessageListener wmBusMessageListener) {
        messageListener.addMessageListener(wmBusMessageListener);
    }

    public void unregisterWMBusMessageListener(WMBusMessageListener wmBusMessageListener) {
        messageListener.removeMessageListener(wmBusMessageListener);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addKnownDevice(Meter meter) {
        knownDeviceTypes.add(meter);
        supportedThingTypes.clear();
    }

    protected void removeKnownDevice(Meter meter) {
        knownDeviceTypes.remove(meter);
        supportedThingTypes.clear();
    }

}
