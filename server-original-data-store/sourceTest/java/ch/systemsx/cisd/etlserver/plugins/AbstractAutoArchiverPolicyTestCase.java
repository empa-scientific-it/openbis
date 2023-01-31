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

package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * @author Franz-Josef Elmer
 */
public class AbstractAutoArchiverPolicyTestCase extends AssertJUnit
{

    protected static final class ExecutionContext
    {
        private AtomicInteger counter = new AtomicInteger();

        public AbstractExternalData createDataset(String projectCode, String experimentCode,
                String datasetType, String dsCode, Long size)
        {
            return createDataset("___space", projectCode, experimentCode, datasetType, dsCode, size);
        }

        /**
         * If datasetCode is null then it gets assigned unique code containing the word "generated"
         */
        public AbstractExternalData createDataset(String spaceCode, String projectCode, String experimentCode,
                String datasetType, String dsCode, Long accessTimestamp, Long size)
        {
            return createDataset(spaceCode, projectCode, experimentCode, datasetType, null, dsCode, accessTimestamp, size);
        }

        /**
         * If datasetCode is null then it gets assigned unique code containing the word "generated"
         */
        public AbstractExternalData createDataset(String spaceCode, String projectCode, String experimentCode,
                String datasetType, String dsCode, Long size)
        {
            return createDataset(spaceCode, projectCode, experimentCode, datasetType, null, dsCode, size);
        }

        /**
         * If datasetCode is null then it gets assigned unique code containing the word "generated"
         */
        public AbstractExternalData createDataset(String spaceCode, String projectCode, String experimentCode,
                String datasetType, String sampleCode, String dsCode, Long size)
        {
            return createDataset(spaceCode, projectCode, experimentCode, datasetType, sampleCode, dsCode, null, size);
        }

        /**
         * If datasetCode is null then it gets assigned unique code containing the word "generated"
         */
        public AbstractExternalData createDataset(String spaceCode, String projectCode, String experimentCode,
                String datasetType, String sampleCode, String dsCode, Long accessTimestamp, Long size)
        {
            Space space = new Space();
            space.setCode(spaceCode);
            space.setIdentifier("/" + space.getCode());

            Project project = new Project();
            project.setCode(projectCode);
            project.setIdentifier("/" + spaceCode + "/" + projectCode);
            project.setSpace(space);

            Experiment exp = new Experiment();
            exp.setProject(project);
            exp.setCode(experimentCode);
            exp.setIdentifier(project.getIdentifier() + "/" + experimentCode);

            Sample sample = null;
            if (sampleCode != null)
            {
                sample = new Sample();
                sample.setCode(sampleCode);
                sample.setIdentifier(space.getIdentifier() + "/" + sample.getCode());
                sample.setExperiment(exp);
                sample.setSpace(space);
            }

            DataSetType dataSetType = new DataSetType();
            dataSetType.setCode(datasetType);

            PhysicalDataSet ds = new PhysicalDataSet();
            if (dsCode != null)
            {
                ds.setCode(dsCode);
            }
            else
            {
                ds.setCode("generated-" + counter.incrementAndGet());
            }
            ds.setExperiment(exp);
            ds.setSample(sample);
            ds.setSize(size);
            ds.setDataSetType(dataSetType);
            ds.setAccessTimestamp(accessTimestamp == null ? new Date(0) : new Date(accessTimestamp));

            return ds;
        }
    }

    protected ExecutionContext ctx;

    @BeforeMethod
    public void setUp()
    {
        ctx = new ExecutionContext();
    }

    protected ExtendedProperties createPolicyProperties(long min, long max)
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, Long.toString(min));
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, Long.toString(max));
        return props;
    }

}
