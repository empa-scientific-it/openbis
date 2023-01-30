/*
 * Copyright 2015 ETH Zuerich, SIS
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

/**
 * An implementation of {@link IAttributeSearchFieldKind} which provides code and description.
 *
 * @author Franz-Josef Elmer
 */
public class SimpleAttributeSearchFieldKind implements IAttributeSearchFieldKind
{
    private String code;

    private String description;

    public SimpleAttributeSearchFieldKind(String code, String description)
    {
        this.code = code;
        this.description = description;
    }

    @Override
    public ISearchFieldAvailability getAvailability()
    {
        return null;
    }

    @Override
    public ISearchFieldCriterionFactory getCriterionFactory()
    {
        return null;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

}
