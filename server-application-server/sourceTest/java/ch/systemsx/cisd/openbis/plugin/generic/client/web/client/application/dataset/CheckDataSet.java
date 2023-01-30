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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IPropertyChecker;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IValueAssertion;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.PropertyCheckingManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;

/**
 * * {@link AbstractDefaultTestCommand} extension checking e.g. properties displayed in data set detail view.
 * 
 * @author Piotr Buczek
 */
public class CheckDataSet extends AbstractDefaultTestCommand implements
        IPropertyChecker<CheckDataSet>
{

    private final TechId datasetId;

    private final String childrenGridId;

    private final String parentsGridId;

    private final PropertyCheckingManager propertyCheckingManager;

    public CheckDataSet()
    {
        this(TechId.createWildcardTechId());
    }

    private CheckDataSet(final TechId datasetId)
    {
        this.datasetId = datasetId;
        this.propertyCheckingManager = new PropertyCheckingManager();
        this.childrenGridId =
                DataSetRelationshipBrowser.createGridId(datasetId, DataSetRelationshipRole.PARENT);
        this.parentsGridId =
                DataSetRelationshipBrowser.createGridId(datasetId, DataSetRelationshipRole.CHILD);
    }

    @Override
    public Property property(final String name)
    {
        return new Property(name, this);
    }

    @Override
    public CheckDataSet property(final String name, final IValueAssertion<?> valueAssertion)
    {
        propertyCheckingManager.addExcpectedProperty(name, valueAssertion);
        return this;
    }

    @Override
    public void execute()
    {
        propertyCheckingManager.assertPropertiesOf(DataSetPropertiesPanel.PROPERTIES_ID_PREFIX
                + datasetId);
    }

    public CheckTableCommand createChildrenTableCheck()
    {
        return new CheckTableCommand(childrenGridId);
    }

    public CheckTableCommand createParentsTableCheck()
    {
        return new CheckTableCommand(parentsGridId);
    }

}
