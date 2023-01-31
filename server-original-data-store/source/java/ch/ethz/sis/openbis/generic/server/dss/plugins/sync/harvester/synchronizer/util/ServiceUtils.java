/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util;

import org.apache.commons.lang3.time.DateUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.openbis.dss.generic.server.EncapsulatedOpenBISService;

/**
 * @author Franz-Josef Elmer
 */
public class ServiceUtils
{
    public static final long TIMEOUT = 6 * DateUtils.MILLIS_PER_HOUR;

    public static IApplicationServerApi createAsV3Api(String url)
    {
        return EncapsulatedOpenBISService.createOpenBisV3Service(url, getTimeOutInMinutes());
    }

    public static IDataStoreServerApi createDssV3Api(String url)
    {
        return EncapsulatedOpenBISService.createDataStoreV3Service(url, getTimeOutInMinutes());
    }

    private static String getTimeOutInMinutes()
    {
        return Long.toString(TIMEOUT / DateUtils.MILLIS_PER_MINUTE);

    }
}
