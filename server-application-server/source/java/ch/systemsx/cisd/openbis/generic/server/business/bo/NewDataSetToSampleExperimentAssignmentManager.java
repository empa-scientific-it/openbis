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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Franz-Josef Elmer
 */
public class NewDataSetToSampleExperimentAssignmentManager
{
    private final DataSetTypeWithoutExperimentChecker dataSetTypeChecker;

    private final List<DataSetSampleExperiment> assignments = new ArrayList<DataSetSampleExperiment>();

    public NewDataSetToSampleExperimentAssignmentManager(DataSetTypeWithoutExperimentChecker dataSetTypeChecker)
    {
        this.dataSetTypeChecker = dataSetTypeChecker;
    }

    public void performAssignment(IRelationshipService relationshipService, Session session)
    {
        for (DataSetSampleExperiment assignment : assignments)
        {
            assignment.assignDataSet(relationshipService, session);
        }
    }

    public void assignDataSetAndRelatedComponents(DataPE dataSet, SamplePE sample, ExperimentPE experiment)
    {
        assignDataSetAndRelatedComponents(dataSet, dataSet, sample, experiment);
    }

    private void assignDataSetAndRelatedComponents(DataPE dataSet, DataPE rootDataSet, SamplePE sample, ExperimentPE experiment)
    {
        String dataSetTypeCode = dataSet.getDataSetType().getCode();
        if (experiment == null && dataSetTypeChecker.isDataSetTypeWithoutExperiment(dataSetTypeCode) == false)
        {
            throw SampleUtils.createWrongSampleException(dataSet, sample,
                    "the new sample is not connected to any experiment and the data set type ("
                            + dataSetTypeCode + ") doesn't match one of the following regular expressions: "
                            + dataSetTypeChecker.getRegularExpressions());
        }
        if (rootDataSet == dataSet)
        {
            assignments.add(new DataSetSampleExperiment(dataSet, sample, experiment));
        }
        SamplePE rootSample = rootDataSet.tryGetSample();
        ExperimentPE rootExperiment = getExperimentOf(rootDataSet);
        List<DataPE> components = dataSet.getContainedDataSets();
        for (DataPE component : components)
        {
            SamplePE componentSample = component.tryGetSample();
            ExperimentPE componentExperiment = getExperimentOf(component);
            if ((EntityHelper.equalEntities(rootSample, componentSample) || componentSample == null)
                    && EntityHelper.equalEntities(rootExperiment, componentExperiment))
            {
                SamplePE newSample = componentSample == null && experiment != null ? null : sample;
                assignments.add(new DataSetSampleExperiment(component, newSample, experiment));
                assignDataSetAndRelatedComponents(component, rootDataSet, sample, experiment);
            }
        }
    }

    private ExperimentPE getExperimentOf(DataPE data)
    {
        SamplePE sample = data.tryGetSample();
        return sample == null ? data.getExperiment() : sample.getExperiment();
    }

    private static class DataSetSampleExperiment
    {
        private DataPE dataSet;

        private SamplePE sample;

        private ExperimentPE experiment;

        DataSetSampleExperiment(DataPE dataSet, SamplePE sample, ExperimentPE experiment)
        {
            this.dataSet = dataSet;
            this.sample = sample;
            this.experiment = experiment;
        }

        void assignDataSet(IRelationshipService relationshipService, Session session)
        {
            if (EntityHelper.equalEntities(dataSet.tryGetSample(), sample) == false)
            {
                relationshipService.assignDataSetToSample(session, dataSet, sample);
            }
            if (EntityHelper.equalEntities(dataSet.getExperiment(), experiment) == false)
            {
                relationshipService.assignDataSetToExperiment(session, dataSet, experiment);
            }
        }
    }
}
