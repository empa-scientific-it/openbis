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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Pawel Glyzewski
 */
public class CustomImport implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static enum PropertyNames
    {
        CUSTOM_IMPORTS("custom-imports"), NAME("name"), DATASTORE_CODE("dss-code"), DROPBOX_NAME(
                "dropbox-name"), DESCRIPTION("description"), TEMPLATE_ENTITY_KIND(
                "template-entity-kind"), TEMPLATE_ENTITY_PERMID("template-entity-permid"),
        TEMPLATE_ATTACHMENT_NAME("template-attachment-name");

        private final String name;

        private PropertyNames(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }
    }

    private String code;

    private Map<String, String> properties;

    public CustomImport()
    {
    }

    public CustomImport(String code, Map<String, String> properties)
    {
        this.code = code;
        this.properties = properties;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String name)
    {
        this.code = name;
    }

    public String getProperty(String propertyName)
    {
        if (properties == null)
        {
            return null;
        } else
        {
            String propertyValue = properties.get(propertyName);

            if (propertyValue == null || propertyValue.trim().length() == 0)
            {
                return null;
            } else
            {
                return propertyValue.trim();
            }
        }
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }
}