/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device.diehl;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.binding.wmbus.device.diehl.handler.DiehlThingHandler;
import org.openhab.io.transport.mbus.wireless.FilteredKeyStorage;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DiehlHandlerFactory} covers logic specific to Diehl devices.
 *
 * @author Łukasz Dywicki - Initial contribution
 */
@Component(service = { DiehlHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class })
public class DiehlHandlerFactory extends BaseThingHandlerFactory {

    // OpenHAB logger
    private final Logger logger = LoggerFactory.getLogger(DiehlHandlerFactory.class);
    private KeyStorage keyStorage;
    private UnitRegistry unitRegistry;

    public DiehlHandlerFactory() {
        logger.debug("Diehl handler factory starting up.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return DiehlBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(DiehlBindingConstants.THING_TYPE_HEAT_COST_ALLOCATOR)) {
            logger.debug("Creating handler for Diehl heat cost allocator {}", thing.getUID().getId());
            return new DiehlThingHandler(thing, new FilteredKeyStorage(keyStorage, thing), unitRegistry);
        }

        logger.warn("Unsupported thing type {}. This handler factory does not support {}", thingTypeUID,
                thing.getUID());

        return null;
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

    @Reference
    public void setKeyStorage(KeyStorage keyStorage) {
        this.keyStorage = keyStorage;
    }

    public void unsetKeyStorage(KeyStorage keyStorage) {
        this.keyStorage = null;
    }

    @Reference
    protected void setUnitRegistry(UnitRegistry unitRegistry) {
        this.unitRegistry = unitRegistry;
    }

    protected void unsetUnitRegistry(UnitRegistry unitRegistry) {
        this.unitRegistry = null;
    }

}
