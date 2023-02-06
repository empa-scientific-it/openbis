/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Result location for a search in a sequence in a file in data set.
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("DataSetFileSearchResultLocation")
public class DataSetFileSearchResultLocation extends AbstractEntitySearchResultLocation
{
    private static final long serialVersionUID = 1L;

    private String pathInDataSet;

    private String identifier;

    public String getPathInDataSet()
    {
        return pathInDataSet;
    }

    public void setPathInDataSet(String pathInDataSet)
    {
        this.pathInDataSet = pathInDataSet;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String sequenceIdentifier)
    {
        this.identifier = sequenceIdentifier;
    }

    @Override
    public String toString()
    {
        return "Data set type: " + getEntityType() + ", code: " + getCode() + ", path: " + pathInDataSet
                + ", identifier: [" + identifier + "], " + appendToString();
    }

}
