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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.Collection;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Console;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class CreateDataSetRmi implements Command<DataSet>
{
    @Inject
    private String session;

    @Inject
    private IDssServiceRpcGeneric dssInternal;

    @Inject("external")
    private IDssServiceRpcGeneric dssExternal;
    
    @Inject
    private IServiceForDataStoreServer etlService;

    @Inject
    private Console console;

    private final DataSet dataSet;

    private boolean external;

    public CreateDataSetRmi(DataSet dataSet, boolean external)
    {
        this.dataSet = dataSet;
        this.external = external;
    }

    @Override
    public DataSet execute()
    {
        DataSetCreator creator = new DataSetCreator("data set content");

        IDssServiceRpcGeneric dss;
        if (external)
        {
            dss = dssExternal;
        } else
        {
            console.startBuffering();
            dss = dssInternal;
        }

        final String code =
                dss.putDataSet(session, creator.getMetadata(dataSet), creator.getData());
        if (false == external)
        {
            AbstractExternalData ds = etlService.tryGetDataSet(session, code);
        }
        return new DataSet()
            {

                @Override
                public String getCode()
                {
                    return code;
                }

                @Override
                public DataSetType getType()
                {
                    return dataSet.getType();
                }

                @Override
                public Sample getSample()
                {
                    return dataSet.getSample();
                }

                @Override
                public Experiment getExperiment()
                {
                    return dataSet.getExperiment();
                }

                @Override
                public Collection<MetaProject> getMetaProjects()
                {
                    return dataSet.getMetaProjects();
                }

                @Override
                public Collection<DataSet> getParents()
                {
                    return dataSet.getParents();
                }

                @Override
                public String toString()
                {
                    return "DataSet " + getCode();
                }

            };
    }
}
