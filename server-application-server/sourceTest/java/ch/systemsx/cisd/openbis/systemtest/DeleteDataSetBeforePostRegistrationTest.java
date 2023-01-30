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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * A test the moves a data set to the trash before post registration runs.
 * 
 * @author cramakri
 */
public class DeleteDataSetBeforePostRegistrationTest extends BaseTest
{

    Sample sample;

    @Test
    public void emptyTrashWithDataSetInPostRegistrationQueue() throws Exception
    {
        assertEquals(0, getDataSetsForPostRegistration().size());
        AbstractExternalData dataSet = create(aDataSet().inSample(sample));

        daoFactory.getPostRegistrationDAO().addDataSet(dataSet.getCode());
        assertEquals(1, getDataSetsForPostRegistration().size());
        perform(trash(dataSet));

        perform(emptyTrash());
        assertEquals(0, getDataSetsForPostRegistration().size());
    }

    private Collection<Long> getDataSetsForPostRegistration()
    {
        return daoFactory.getPostRegistrationDAO().listDataSetsForPostRegistration();
    }

    @BeforeMethod
    void createFixture() throws Exception
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        sample = create(aSample().inExperiment(experiment));
    }
}
