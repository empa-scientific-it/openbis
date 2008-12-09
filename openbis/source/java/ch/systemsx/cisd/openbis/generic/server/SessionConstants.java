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

package ch.systemsx.cisd.openbis.generic.server;

/**
 * Some session constants.
 * 
 * @author Christian Ribeaud
 */
public final class SessionConstants
{

    private SessionConstants()
    {
    }

    public static final String OPENBIS_SESSION_ATTRIBUTE_KEY = "openbis-session";

    public static final String OPENBIS_SERVER_ATTRIBUTE_KEY = "openbis-server";

    public static final String OPENBIS_RESULT_SET_MANAGER = "openbis-result-set-manager";

    public static final String OPENBIS_EXPORT_MANAGER = "openbis-export-manager";

    public static final String OPENBIS_UPLOADED_FILES = "openbis-uploaded-files";
}
