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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedBasicExperiment;

/**
 * A {@link AbstractParserObjectFactory} extension for creating {@link UpdatedBasicExperiment}.
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 * @author Chandrasekhar Ramakrishnan
 */
public final class UpdatedBasicExperimentParserObjectFactory extends
        AbstractBasicExperimentParserObjectFactory<UpdatedBasicExperiment> implements
        IParserObjectFactory<UpdatedBasicExperiment>
{
    public UpdatedBasicExperimentParserObjectFactory(final IPropertyMapper propertyMapper)
    {
        super(UpdatedBasicExperiment.class, propertyMapper);
    }

    @Override
    public UpdatedBasicExperiment createObject(final String[] lineTokens) throws ParserException
    {
        final UpdatedBasicExperiment newExperiment = super.createObject(lineTokens);
        final ExperimentBatchUpdateDetails updateDetails = createBatchUpdateDetails(newExperiment);
        newExperiment.setBatchUpdateDetails(updateDetails);
        return newExperiment;
    }

    private ExperimentBatchUpdateDetails createBatchUpdateDetails(
            UpdatedBasicExperiment newExperiment)
    {
        final Set<String> propertiesToUpdate = new HashSet<String>();
        for (IEntityProperty property : newExperiment.getProperties())
        {
            propertiesToUpdate.add(property.getPropertyType().getCode());
        }
        return new ExperimentBatchUpdateDetails(propertiesToUpdate);
    }
}
