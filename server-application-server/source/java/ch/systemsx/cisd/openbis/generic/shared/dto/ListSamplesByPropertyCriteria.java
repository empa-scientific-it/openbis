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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.LocalExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A DTO which describes the samples search criteria: the value of a chosen property. We look for samples in a specified space. If the experiment
 * identifier is specified, the search is restricted to samples in that experiment.
 * 
 * @author Tomasz Pylak
 */
public class ListSamplesByPropertyCriteria implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private final String samplePropertyCode;

    private final String samplePropertyValue;

    private final String spaceCode;

    private final LocalExperimentIdentifier experimentIdentifierOrNull;

    public ListSamplesByPropertyCriteria(String propertyCode, String propertyValue,
            String spaceCode, LocalExperimentIdentifier experimentIdentifierOrNull)
    {
        this.samplePropertyCode = propertyCode;
        this.samplePropertyValue = propertyValue;
        this.spaceCode = spaceCode;
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

    public String getPropertyCode()
    {
        return samplePropertyCode;
    }

    public String getPropertyValue()
    {
        return samplePropertyValue;
    }

    public ProjectIdentifier getProjectIdentifier()
    {
        ProjectIdentifier projectIdentifier =
                new ProjectIdentifier(getSpaceIdentifier(), experimentIdentifierOrNull
                        .getProjectCode());
        return projectIdentifier;
    }

    public SpaceIdentifier getSpaceIdentifier()
    {
        return new SpaceIdentifier(spaceCode);
    }

    public LocalExperimentIdentifier tryGetLocalExperimentIdentifier()
    {
        return experimentIdentifierOrNull;
    }

    @Override
    public String toString()
    {
        String expDesc =
                (experimentIdentifierOrNull == null ? "any experiment" : experimentIdentifierOrNull
                        .toString());
        return String.format("Samples from the space '%s' with property '%s' set to '%s' in %s",
                spaceCode, samplePropertyCode, samplePropertyValue, expDesc);
    }

    public String getSpaceCode()
    {
        return spaceCode;
    }
}
