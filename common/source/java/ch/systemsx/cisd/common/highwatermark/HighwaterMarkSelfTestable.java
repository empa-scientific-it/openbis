/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.highwatermark;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher.HighwaterMarkState;
import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * An <code>ISelfTestable</code> implementation based on {@link HighwaterMarkWatcher}.
 * 
 * @author Christian Ribeaud
 */
public final class HighwaterMarkSelfTestable implements ISelfTestable
{
    static final String EXCEPTION_FORMAT =
            "Highwater mark reached on '%s', required: %s, found: %s.";

    private final File path;

    private final HighwaterMarkWatcher highwaterMarkWatcher;

    public HighwaterMarkSelfTestable(final File path,
            final HighwaterMarkWatcher highwaterMarkWatcher)
    {
        this.path = path;
        this.highwaterMarkWatcher = highwaterMarkWatcher;
    }

    //
    // ISelfTestable
    //

    public final void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        final HighwaterMarkState highwaterMarkState =
                highwaterMarkWatcher.getHighwaterMarkState(path);
        if (HighwaterMarkWatcher.isBelow(highwaterMarkState))
        {
            final String freeSpaceDisplayed =
                    HighwaterMarkWatcher.displayKilobyteValue(highwaterMarkState.getFreeSpace());
            final String highwaterMarkDisplayed =
                    HighwaterMarkWatcher
                            .displayKilobyteValue(highwaterMarkState.getHighwaterMark());
            throw ConfigurationFailureException.fromTemplate(EXCEPTION_FORMAT, path,
                    highwaterMarkDisplayed, freeSpaceDisplayed);
        }
    }
}