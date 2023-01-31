/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.api.v2.IExperimentUpdatable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * Implementation of {@link IExperimentUpdatable}.
 * 
 * @author Jakub Straszewski
 */
class ExperimentUpdatable extends ExperimentImmutable implements IExperimentUpdatable
{
    private final List<NewAttachment> newAttachments;

    public ExperimentUpdatable(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment)
    {
        super(experiment);
        newAttachments = new ArrayList<NewAttachment>();
    }

    @Override
    public boolean isExistingExperiment()
    {
        return true;
    }

    @Override
    public void setPropertyValue(String propertyCode, String propertyValue)
    {
        EntityHelper.createOrUpdateProperty(getExperiment(), propertyCode, propertyValue);
    }

    @Override
    public void addAttachment(String filePath, String title, String description, byte[] content)
    {
        newAttachments.add(ConversionUtils.createAttachment(filePath, title, description, content));
    }

    /**
     * For conversion to updates DTO.
     */
    List<NewAttachment> getNewAttachments()
    {
        return newAttachments;
    }
}
