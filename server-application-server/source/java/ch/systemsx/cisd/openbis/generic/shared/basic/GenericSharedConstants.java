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

package ch.systemsx.cisd.openbis.generic.shared.basic;

/**
 * Some public shared constants.
 * 
 * @author Franz-Josef Elmer
 */
public final class GenericSharedConstants
{

    private GenericSharedConstants()
    {
        // Can not be instantiated.
    }

    /** Part of the URL of the DSS server. */
    public static final String DATA_STORE_SERVER_WEB_APPLICATION_NAME = "datastore_server";

    public static final String DATA_STORE_SERVER_APPLICATION_PATH = "data_store";

    /** Part of the URL of the DSS service. */
    public static final String DATA_STORE_SERVER_SERVICE_NAME =
            DATA_STORE_SERVER_WEB_APPLICATION_NAME + "/dss";

    public static final String SESSION_ID_COOKIE = "openbis";

    public static final String SESSION_ID_PARAMETER = "sessionID";

}
