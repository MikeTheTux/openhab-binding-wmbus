/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device.techem;

import java.util.List;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openmuc.jmbus.wireless.WMBusMessage;

/**
 * The {@link TechemSmokeDetector} class covers smoke detector devices at very basic level.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class TechemSmokeDetector extends TechemDevice {

    public TechemSmokeDetector(WMBusMessage originalMessage, WMBusAdapter adapter, Variant variant, List<Record<?>> records) {
        super(originalMessage, adapter, variant, records);
    }

}
