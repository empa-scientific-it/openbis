/*
 * Copyright 2009 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.etlserver.plugins.TemplateBasedLinkNamingStrategy.DEFAULT_LINK_TEMPLATE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * Test cases for {@link TemplateBasedLinkNamingStrategy}.
 * 
 * @author Kaloyan Enimanev
 */
public class TemplateBasedLinkNamingStrategyTest extends AbstractFileSystemTestCase
{

    public static final String LONG_LINK_TEMPLATE =
            "/Space_${space}/Project_${project}/Experiment_${experiment}/DataSetType_${dataSetType}/Sample_${sample}/Dataset_${dataSet}";

    private static final String DATASET_PATH_LONG =
            "/Space_GROUP-G/Project_PROJECT-P/Experiment_EXP-E/DataSetType_TYPE-T/Sample_SAMPLE-S/Dataset_DATASET-D";

    private static final String DATASET_PATH_DEFAULT =
            "GROUP-G/PROJECT-P/EXP-E/TYPE-T+SAMPLE-S+DATASET-D";

    public static final String COMPONENT_LONG_LINK_TEMPLATE =
            "/Space_${space}/Project_${project}/Experiment_${experiment}/ContainerType_${containerDataSetType}/ContainerSample_${containerSample}/ContainerDataSet_${containerDataSet}/DataSetType_${dataSetType}/Sample_${sample}/Dataset_${dataSet}";

    private static final String COMPONENT_DATASET_PATH_LONG_1 =
            "/Space_GROUP-G/Project_PROJECT-P/Experiment_EXP-E/ContainerType_TYPE-C/ContainerSample_SAMPLE-C/ContainerDataSet_DATASET-C/DataSetType_TYPE-T/Sample_SAMPLE-S/Dataset_DATASET-D";

    private static final String COMPONENT_DATASET_PATH_LONG_2 =
            "/Space_GROUP-G/Project_PROJECT-P/Experiment_EXP-E/ContainerType_TYPE-C2/ContainerSample_SAMPLE-C2/ContainerDataSet_DATASET-C2/DataSetType_TYPE-T/Sample_SAMPLE-S/Dataset_DATASET-D";

    private static final String SAMPLE = "SAMPLE-S";

    private static final String PROJECT = "PROJECT-P";

    private static final String GROUP = "GROUP-G";

    private static final String EXPERIMENT = "EXP-E";

    private static final String TYPE = "TYPE-T";

    private static final String LOCATION = "location/L";

    private static final String DATASET = "DATASET-D";

    private static final String CONTAINER_TYPE_1 = "TYPE-C";

    private static final String CONTAINER_DATASET_1 = "DATASET-C";

    private static final String CONTAINER_SAMPLE_1 = "SAMPLE-C";

    private static final String CONTAINER_TYPE_2 = "TYPE-C2";

    private static final String CONTAINER_DATASET_2 = "DATASET-C2";

    private static final String CONTAINER_SAMPLE_2 = "SAMPLE-C2";

    @Test
    public void testCreateDataSetDefaultPath() throws Exception
    {
        Set<String> paths = createPathFromTemplate(DEFAULT_LINK_TEMPLATE, COMPONENT_LONG_LINK_TEMPLATE, createDataSetInfo());
        assertEquals(paths.size(), 1);
        assertEquals(DATASET_PATH_DEFAULT, paths.iterator().next());
    }

    @Test
    public void testCreateDataSetPathLongLink() throws Exception
    {
        Set<String> paths = createPathFromTemplate(LONG_LINK_TEMPLATE, COMPONENT_LONG_LINK_TEMPLATE, createDataSetInfo());
        assertEquals(1, paths.size());
        assertEquals(DATASET_PATH_LONG, paths.iterator().next());
    }

    @Test
    public void testCreateComponentDataSetPath() throws Exception
    {
        List<String> paths =
                new ArrayList<>(createPathFromTemplate(DEFAULT_LINK_TEMPLATE, COMPONENT_LONG_LINK_TEMPLATE, createComponentDataSetInfo()));
        assertEquals(2, paths.size());

        Collections.sort(paths);

        assertEquals(COMPONENT_DATASET_PATH_LONG_1, paths.get(0));
        assertEquals(COMPONENT_DATASET_PATH_LONG_2, paths.get(1));

    }

    private Set<String> createPathFromTemplate(String template, String componentTemplate, AbstractExternalData dsInfo)
    {
        Set<HierarchicalPath> hPaths = new TemplateBasedLinkNamingStrategy(template, componentTemplate)
                .createHierarchicalPaths(dsInfo);
        Set<String> paths = new HashSet<>();
        for (HierarchicalPath hPath : hPaths)
        {
            paths.add(hPath.getPath());
        }
        return paths;
    }

    private AbstractExternalData createDataSetInfo()
    {
        PhysicalDataSet dsInfo = new PhysicalDataSet();
        dsInfo.setCode(DATASET);
        dsInfo.setLocation(LOCATION);

        DataSetType type = new DataSetType();
        type.setCode(TYPE);
        dsInfo.setDataSetType(type);

        Space space = new Space();
        space.setCode(GROUP);
        Project project = new Project();
        project.setCode(PROJECT);
        project.setSpace(space);
        Experiment experiment = new Experiment();
        experiment.setCode(EXPERIMENT);
        experiment.setProject(project);
        dsInfo.setExperiment(experiment);

        Sample sample = new Sample();
        sample.setCode(SAMPLE);
        dsInfo.setSample(sample);

        return dsInfo;
    }

    private AbstractExternalData createComponentDataSetInfo()
    {
        AbstractExternalData dsInfo = createDataSetInfo();
        dsInfo.addContainer(createContainer(1L, CONTAINER_TYPE_1, CONTAINER_DATASET_1, CONTAINER_SAMPLE_1), 1);
        dsInfo.addContainer(createContainer(2L, CONTAINER_TYPE_2, CONTAINER_DATASET_2, CONTAINER_SAMPLE_2), 2);
        return dsInfo;
    }

    private ContainerDataSet createContainer(Long id, String type, String code, String sample)
    {
        ContainerDataSet c = new ContainerDataSet();
        c.setCode(code);

        DataSetType dsType = new DataSetType();
        dsType.setCode(type);
        c.setDataSetType(dsType);

        Sample s = new Sample();
        s.setCode(sample);
        c.setSample(s);

        c.setId(id);
        return c;
    }

}
