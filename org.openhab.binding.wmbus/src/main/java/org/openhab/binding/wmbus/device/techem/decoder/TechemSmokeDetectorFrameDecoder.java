/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder;

import java.util.ArrayList;
import java.util.List;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.Record;
import org.openhab.binding.wmbus.device.techem.TechemSmokeDetector;
import org.openhab.binding.wmbus.device.techem.Variant;
import org.openmuc.jmbus.SecondaryAddress;

class TechemSmokeDetectorFrameDecoder extends AbstractTechemFrameDecoder<TechemSmokeDetector> {

    private final Variant[] variants;

    TechemSmokeDetectorFrameDecoder(Variant ... variants) {
        super(variants[0]);
        this.variants = variants;
    }

    @Override
    protected TechemSmokeDetector decode(WMBusDevice device, SecondaryAddress address, byte[] buffer) {
        int offset = address.asByteArray().length + 2; // 2 first bytes of data is CRC
        Buffer buff = new Buffer(device.getOriginalMessage(), address);
        int coding = buff.skip(2).readByte() & 0xFF;

        for (Variant variant : variants) {
            if (variant.getCoding() == coding) {
                List<Record<?>> records = new ArrayList<>();
                records.add(new Record<>(Record.Type.STATUS, ((Byte) buff.readByte()).intValue()));
                records.add(new Record<>(Record.Type.CURRENT_READING_DATE, buff.skip(2).readCurrentDate()));
                buff.skip(2);
                records.add(new Record<>(Record.Type.CURRENT_READING_DATE_SMOKE, buff.readPastDate()));
                records.add(new Record<>(Record.Type.RSSI, device.getOriginalMessage().getRssi()));

                return new TechemSmokeDetector(device.getOriginalMessage(), device.getAdapter(), variant, records);
            }
        }

        return null;

    }

}
