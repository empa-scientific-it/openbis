/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.screening.systemtests;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractImageDropboxTestCase extends AbstractScreeningSystemTestCase
{
    private static final FileFilter SVN_FILTER = new FileFilter()
    {
        @Override
        public boolean accept(File pathname)
        {
            return pathname.getName().equals(".svn") == false;
        }
    };

    protected ImageChecker imageChecker;

    protected void dropAnExampleDataSet() throws Exception
    {
        registerAdditionalOpenbisMetaData();
        File exampleDataSet = createTestDataContents();
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported(FINISHED_POST_REGISTRATION_CONDITION);
    }

    @BeforeMethod
    public void setUpImageChecker()
    {
        imageChecker = new ImageChecker(new File("tmp/wrong_images/" + getClass().getSimpleName()));
    }

    @AfterMethod
    public void assertImageChecker()
    {
        imageChecker.assertNoFailures();
    }

    protected void registerAdditionalOpenbisMetaData()
    {
    }

    private File createTestDataContents() throws IOException
    {
        File destination = new File(workingDirectory, "test-data");
        destination.mkdirs();
        FileUtils.copyDirectory(new File(getTestDataFolder(), getDataFolderToDrop()), destination,
                SVN_FILTER);
        return destination;
    }

    protected abstract String getDataFolderToDrop();

    protected String getTestDataFolder()
    {
        return "../server-screening/resource/test-data/" + getClass().getSimpleName();
    }

    protected AbstractExternalData getRegisteredContainerDataSet()
    {
        Class<? extends AbstractImageDropboxTestCase> testClass = getClass();
        AbstractExternalData containerDataSet = getRegisteredContainerDataSet(testClass);
        assertNotNull("No container data set found for test " + testClass.getSimpleName(),
                containerDataSet);
        return containerDataSet;
    }

    protected AbstractExternalData getRegisteredContainerDataSet(
            Class<? extends AbstractImageDropboxTestCase> testClass)
    {
        List<AbstractExternalData> dataSets = getRegisteredContainerDataSets(testClass);
        return dataSets.isEmpty() ? null : dataSets.get(0);
    }

    protected List<AbstractExternalData> getRegisteredContainerDataSets(
            Class<? extends AbstractImageDropboxTestCase> testClass)
    {
        List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
        String experimentCode = translateIntoCamelCase(testClass.getSimpleName()).toUpperCase();
        List<AbstractExternalData> dataSets = getRegisteredDataSets(experimentCode);
        for (AbstractExternalData dataSet : dataSets)
        {
            if (dataSet.getDataSetKind().equals(DataSetKind.CONTAINER))
            {
                result.add(dataSet);
            }
        }
        return result;
    }

    private List<AbstractExternalData> getRegisteredDataSets(String experimentCode)
    {
        ExperimentIdentifier identifier =
                ExperimentIdentifierFactory.parse("/TEST/TEST-PROJECT/" + experimentCode);
        List<Experiment> experiments =
                commonServer.listExperiments(sessionToken, Arrays.asList(identifier));
        if (experiments.isEmpty())
        {
            return Collections.emptyList();
        }
        Experiment experiment = experiments.get(0);
        return commonServer.listRelatedDataSets(sessionToken,
                new DataSetRelatedEntities(Arrays.asList(experiment)), false);
    }

    private String translateIntoCamelCase(String string)
    {
        StringBuilder builder = new StringBuilder();
        for (char c : string.toCharArray())
        {
            if (Character.isUpperCase(c))
            {
                if (builder.length() > 0)
                {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(c));
            } else
            {
                builder.append(c);
            }
        }
        return builder.toString();
    }

}
