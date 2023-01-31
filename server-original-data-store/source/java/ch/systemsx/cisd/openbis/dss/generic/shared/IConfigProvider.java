/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;

/**
 * A provider for DSS configuration infos which can be used at runtime.
 * 
 * @author Kaloyan Enimanev
 */
public interface IConfigProvider
{

    /**
     * Return the DSS store root.
     */
    File getStoreRoot();

    /**
     * Return the data store code.
     */
    String getDataStoreCode();

    /**
     * Return the url of the openBIS server.
     */
    String getOpenBisServerUrl();

    /**
     * Return the minimum time (in seconds) that data streams are kept before expiring.
     */
    int getDataStreamTimeout();

    /**
     * Return the maximum time (in seconds) that data streams are kept before expiring.
     */
    int getDataStreamMaxTimeout();
}
