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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp;

/**
 * Parameters that are passed to a web application in an URL query string.
 * 
 * @author pkupczyk
 */
public enum WebAppUrlParameter
{

    WEBAPP_CODE("webapp-code"), SESSION_ID("session-id"), ENTITY_KIND("entity-kind"), ENTITY_TYPE(
            "entity-type"), ENTITY_IDENTIFIER("entity-identifier"),
    ENTITY_PERM_ID("entity-perm-id");

    private final String name;

    private WebAppUrlParameter(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

}
