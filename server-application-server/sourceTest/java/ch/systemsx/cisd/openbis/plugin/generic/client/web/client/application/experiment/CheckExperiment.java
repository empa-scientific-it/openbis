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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.attachment.AttachmentBrowser;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IPropertyChecker;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IValueAssertion;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.PropertyCheckingManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;

/**
 * @author Izabela Adamczyk
 */
public class CheckExperiment extends AbstractDefaultTestCommand implements
        IPropertyChecker<CheckExperiment>
{

    private final TechId experimentId;

    private final PropertyCheckingManager propertyCheckingManager;

    public CheckExperiment()
    {
        this(TechId.createWildcardTechId());
    }

    private CheckExperiment(final TechId experimentId)
    {
        this.experimentId = experimentId;
        propertyCheckingManager = new PropertyCheckingManager();
    }

    @Override
    public Property property(final String name)
    {
        return new Property(name, this);
    }

    @Override
    public CheckExperiment property(final String name, final IValueAssertion<?> valueAssertion)
    {
        propertyCheckingManager.addExcpectedProperty(name, valueAssertion);
        return this;
    }

    @Override
    public void execute()
    {
        propertyCheckingManager.assertPropertiesOf(ExperimentPropertiesPanel.PROPERTIES_ID_PREFIX
                + experimentId);
    }

    public CheckTableCommand createAttachmentsTableCheck()
    {
        return new CheckTableCommand(AttachmentBrowser.createGridId(experimentId,
                AttachmentHolderKind.EXPERIMENT));
    }

    public CheckTableCommand createSampleTableCheck()
    {
        return new CheckTableCommand(ExperimentSamplesSection.createGridId(experimentId));
    }

    public CheckTableCommand createDataSetTableCheck()
    {
        return new CheckTableCommand(ExperimentDataSetBrowser.createGridId(experimentId));
    }

}
