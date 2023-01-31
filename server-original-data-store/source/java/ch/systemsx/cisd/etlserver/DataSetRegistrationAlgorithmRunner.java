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

package ch.systemsx.cisd.etlserver;

import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Runs DataSetRegistrationAlgorithm objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationAlgorithmRunner
{
    public static interface IDataSetRegistrationAlgorithmRunnerDelegate
    {
        public void didNotIdentifyDataSet();
    }

    public static final class NoOpDelegate implements IDataSetRegistrationAlgorithmRunnerDelegate
    {

        @Override
        public void didNotIdentifyDataSet()
        {
        }

    }

    private final DataSetRegistrationAlgorithm registrationAlgorithm;

    private final IDataSetRegistrationAlgorithmRunnerDelegate delegate;

    public DataSetRegistrationAlgorithmRunner(DataSetRegistrationHelper registrationHelper)
    {
        this(registrationHelper, new NoOpDelegate());
    }

    public DataSetRegistrationAlgorithmRunner(DataSetRegistrationHelper registrationHelper,
            IDataSetRegistrationAlgorithmRunnerDelegate delegate)
    {
        this(registrationHelper.getRegistrationAlgorithm(), delegate);
    }

    public DataSetRegistrationAlgorithmRunner(DataSetRegistrationAlgorithm registrationAlgorithm)
    {
        this(registrationAlgorithm, new NoOpDelegate());
    }

    public DataSetRegistrationAlgorithmRunner(DataSetRegistrationAlgorithm registrationAlgorithm,
            IDataSetRegistrationAlgorithmRunnerDelegate delegate)
    {
        this.registrationAlgorithm = registrationAlgorithm;
        this.delegate = delegate;
    }

    public List<DataSetInformation> runAlgorithm()
    {
        registrationAlgorithm.prepare();
        if (registrationAlgorithm.hasDataSetBeenIdentified())
        {
            return registrationAlgorithm.registerDataSet();
        } else
        {
            delegate.didNotIdentifyDataSet();
            registrationAlgorithm.dealWithUnidentifiedDataSet();
            return Collections.emptyList();
        }
    }
}
