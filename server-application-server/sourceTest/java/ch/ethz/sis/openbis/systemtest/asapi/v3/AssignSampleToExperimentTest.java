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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.openbis.systemtest.AbstractAssignmentSampleToExperimentTestCase;

/**
 * Sample to experiment assignment tests for API V3.
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups = { "system-cleandb" })
public class AssignSampleToExperimentTest extends AbstractAssignmentSampleToExperimentTestCase
{

    @Autowired
    protected IApplicationServerApi v3api;

    @Override
    protected void updateExperimentChangeSamples(String experimentIdentifier, List<String> samplePermIds,
            String userSessionToken)
    {
        IExperimentId eId = new ExperimentIdentifier(experimentIdentifier);
        ExperimentFetchOptions efo = new ExperimentFetchOptions();
        efo.withSamples();

        Map<IExperimentId, Experiment> eMap = v3api.getExperiments(userSessionToken, Arrays.asList(eId), efo);
        Experiment e = eMap.get(eId);

        List<SampleUpdate> suList = new LinkedList<SampleUpdate>();
        Set<SamplePermId> sIdSet = new HashSet<SamplePermId>();
        for (Sample s : e.getSamples())
        {
            if (false == samplePermIds.contains(s.getPermId()))
            {
                SampleUpdate su = new SampleUpdate();
                su.setSampleId(s.getPermId());
                su.setExperimentId(null);
                suList.add(su);
            }

            sIdSet.add(s.getPermId());
        }

        for (String samplePermId : samplePermIds)
        {
            if (false == sIdSet.contains(new SamplePermId(samplePermId)))
            {
                SampleUpdate su = new SampleUpdate();
                su.setSampleId(new SamplePermId(samplePermId));
                su.setExperimentId(eId);
                suList.add(su);
            }
        }

        v3api.updateSamples(userSessionToken, suList);
    }

    @Override
    protected void updateSampleChangeExperiment(String samplePermId, String experimentIdentifierOrNull,
            String userSessionToken)
    {
        ExperimentIdentifier experimentId = experimentIdentifierOrNull == null ? null : new ExperimentIdentifier(experimentIdentifierOrNull);

        List<SampleUpdate> sampleUpdates = new ArrayList<SampleUpdate>();
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(new SamplePermId(samplePermId));
        sampleUpdate.setExperimentId(experimentId);

        if (experimentIdentifierOrNull != null)
        {
            String[] tokens = experimentIdentifierOrNull.split("/");
            sampleUpdate.setSpaceId(new SpacePermId(tokens[1]));
        }

        sampleUpdates.add(sampleUpdate);

        v3api.updateSamples(userSessionToken, sampleUpdates);
    }

    @Override
    public void addSampleToAnExperimentFailingBecauseSampleHasAlreadyAnExperiment()
    {
        // changing a list of samples via experiment update is not supported in v3
    }

    @Override
    public void sampleWithExperimentCanNotBeAssignedToAnotherExperimentThroughExperimentUpdate()
    {
        // changing a list of samples via experiment update is not supported in v3
    }

    @Override
    protected String getErrorMessage(Exception e)
    {
        String msg = e.getMessage();
        int index = msg.indexOf(" (Context: [");

        if (index != -1)
        {
            return msg.substring(0, index);
        } else
        {
            return msg;
        }
    }
}
