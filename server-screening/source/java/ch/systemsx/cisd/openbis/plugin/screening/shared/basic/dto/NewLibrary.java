/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;

/**
 * @author pkupczyk
 */
public class NewLibrary
{

    private List<NewMaterial> newGenesOrNull;

    private List<NewMaterial> newOligosOrNull;

    private List<NewSamplesWithTypes> newSamplesWithType;

    public List<NewMaterial> getNewGenesOrNull()
    {
        return newGenesOrNull;
    }

    public void setNewGenesOrNull(List<NewMaterial> newGenesOrNull)
    {
        this.newGenesOrNull = newGenesOrNull;
    }

    public int getNewGenesCount()
    {
        return getNewGenesOrNull() != null ? getNewGenesOrNull().size() : 0;
    }

    public List<NewMaterial> getNewOligosOrNull()
    {
        return newOligosOrNull;
    }

    public void setNewOligosOrNull(List<NewMaterial> newOligosOrNull)
    {
        this.newOligosOrNull = newOligosOrNull;
    }

    public int getNewOligosCount()
    {
        return getNewOligosOrNull() != null ? getNewOligosOrNull().size() : 0;
    }

    public List<NewSamplesWithTypes> getNewSamplesWithType()
    {
        return newSamplesWithType;
    }

    public void setNewSamplesWithType(List<NewSamplesWithTypes> newSamplesWithType)
    {
        this.newSamplesWithType = newSamplesWithType;
    }

    public int getNewSamplesWithTypeCount()
    {
        return getNewSamplesWithType() != null ? getNewSamplesWithType().size() : 0;
    }

}
