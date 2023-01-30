/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

public final class PropertyField
{
    private final String propertyFieldId;

    private final String propertyFieldValue;

    public PropertyField(final String propertyFieldId, final String propertyFieldValue)
    {
        this.propertyFieldId = propertyFieldId;
        this.propertyFieldValue = propertyFieldValue;
    }

    public final String getPropertyFieldId()
    {
        return propertyFieldId;
    }

    public final String getPropertyFieldValue()
    {
        return propertyFieldValue;
    }

}