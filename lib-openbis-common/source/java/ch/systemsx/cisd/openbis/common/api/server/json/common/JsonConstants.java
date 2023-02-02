/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.api.server.json.common;

/**
 * @author pkupczyk
 */
public class JsonConstants
{

    private static final String ID_FIELD = "@id";

    private static final String TYPE_FIELD = "@type";

    private static final String LEGACY_CLASS_FIELD = "@class";

    public static final String getIdField()
    {
        return ID_FIELD;
    }

    public static final String getTypeField()
    {
        return TYPE_FIELD;
    }

    public static final String getLegacyClassField()
    {
        return LEGACY_CLASS_FIELD;
    }

}
