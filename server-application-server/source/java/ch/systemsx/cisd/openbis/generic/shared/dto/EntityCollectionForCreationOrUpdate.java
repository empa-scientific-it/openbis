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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;

/**
 * Collections of {@link NewExternalData}, {@link NewExperiment}.
 *
 * @author Franz-Josef Elmer
 */
public class EntityCollectionForCreationOrUpdate implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private final List<NewExperiment> newExperiments = new ArrayList<NewExperiment>();

    private final List<NewExternalData> newDataSets = new ArrayList<NewExternalData>();

    public List<NewExperiment> getNewExperiments()
    {
        return newExperiments;
    }

    public void addExperiment(NewExperiment experiment)
    {
        newExperiments.add(experiment);
    }

    public List<NewExternalData> getNewDataSets()
    {
        return newDataSets;
    }

    public void addDataSet(NewExternalData dataSet)
    {
        newDataSets.add(dataSet);
    }
}
