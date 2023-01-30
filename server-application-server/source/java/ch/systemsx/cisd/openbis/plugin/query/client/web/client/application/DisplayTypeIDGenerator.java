/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;

/**
 * @author Franz-Josef Elmer
 */
public enum DisplayTypeIDGenerator implements IDisplayTypeIDGenerator
{
    QUERY_EDITOR("query-editor"),

    QUERY_SECTION("query-section"),

    ;

    private final String genericNameOrPrefix;

    private DisplayTypeIDGenerator(String genericNameOrPrefix)
    {
        this.genericNameOrPrefix = genericNameOrPrefix;
    }

    @Override
    public String createID()
    {
        return genericNameOrPrefix;
    }

    @Override
    public String createID(String suffix)
    {
        return genericNameOrPrefix + suffix;
    }

}
