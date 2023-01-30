/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.utils.EntityUtils;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class VerifySampleDataSetsExecutor implements IVerifySampleDataSetsExecutor, InitializingBean
{

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    protected ExposablePropertyPlaceholderConfigurer configurer;

    private DataSetTypeWithoutExperimentChecker dataSetTypeChecker;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        dataSetTypeChecker = new DataSetTypeWithoutExperimentChecker(
                configurer == null ? new Properties() : configurer.getResolvedProps());
    }

    @Override
    public DataSetTypeWithoutExperimentChecker getDataSetTypeChecker()
    {
        return dataSetTypeChecker;
    }

    @Override
    public void checkDataSetsDoNotNeedAnExperiment(IOperationContext context, SamplePE sample)
    {
        List<String> dataSetsNeedingExperiment = new ArrayList<String>();
        for (DataPE dataSet : sample.getDatasets())
        {
            String dataSetTypeCode = dataSet.getDataSetType().getCode();
            if (getDataSetTypeChecker().isDataSetTypeWithoutExperiment(dataSetTypeCode) == false)
            {
                dataSetsNeedingExperiment.add(dataSet.getCode());
            }
        }
        if (dataSetsNeedingExperiment.isEmpty() == false)
        {
            throw new UserFailureException("Operation cannot be performed, because the sample "
                    + EntityUtils.render(sample) + " has the following datasets which need an experiment: "
                    + CollectionUtils.abbreviate(dataSetsNeedingExperiment, 10));
        }

    }

}
