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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author pkupczyk
 */
public abstract class MutableTest extends SystemTestCase
{

    private IGeneralInformationService generalInformationService;

    private String sessionToken;

    private int beforeProjectAttachmentCount;

    private int beforeExperimentAttachmentCount;

    private int beforeSampleAttachmentCount;

    @BeforeMethod
    public void beforeMethod()
    {
        generalInformationService =
                HttpInvokerUtils.createServiceStub(IGeneralInformationService.class,
                        TestInstanceHostUtils.getOpenBISUrl()
                                + IGeneralInformationService.SERVICE_URL, 10000);
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "password");
    }

    @AfterMethod
    public void afterMethod()
    {
        generalInformationService.logout(sessionToken);
    }

    @BeforeClass
    public void beforeClass()
    {
        Logger.getLogger("OPERATION.Resources").setLevel(Level.DEBUG);
    }

    @AfterClass
    public void afterClass()
    {
        Logger.getLogger("OPERATION.Resources").setLevel(Level.INFO);
    }

    @Override
    protected Level getLogLevel()
    {
        return Level.DEBUG;
    }

    @Test
    public void testMutable() throws Exception
    {
        assertBefore();
        createData();
        waitUntilDataSetImported();
        assertAfter();
    }

    private void createData() throws Exception
    {
        File dataDirectory = new File(workingDirectory, "mutable-test-directory");
        dataDirectory.mkdirs();
        FileUtilities.writeToFile(new File(dataDirectory, "mutable-test-file"), "");
        moveFileToIncoming(dataDirectory);
    }

    protected void assertBefore()
    {
        beforeProjectAttachmentCount = getProjectAttachments().size();
        beforeExperimentAttachmentCount = getExperimentAttachments().size();
        beforeSampleAttachmentCount = getSampleAttachments().size();
    }

    protected void assertAfter()
    {
        Assert.assertEquals(getProjectAttachments().size(), beforeProjectAttachmentCount + 3, "Number of project attachments");
        Assert.assertEquals(getExperimentAttachments().size(), beforeExperimentAttachmentCount + 3, "Number of experiment attachments");
        Assert.assertEquals(getSampleAttachments().size(), beforeSampleAttachmentCount + 3, "Number of sample attachments");
        Assert.assertEquals(getMaterialProperty(), "mutable material description 2");
        Assert.assertEquals(getDataSetProperty(), "mutable data set comment 2");
    }

    private List<Attachment> getProjectAttachments()
    {
        return generalInformationService.listAttachmentsForProject(sessionToken, new ProjectIdentifierId("/CISD/NOE"), true);
    }

    private List<Attachment> getExperimentAttachments()
    {
        return generalInformationService.listAttachmentsForExperiment(sessionToken, new ExperimentIdentifierId("/CISD/DEFAULT/EXP-REUSE"), true);
    }

    private List<Attachment> getSampleAttachments()
    {
        return generalInformationService.listAttachmentsForSample(sessionToken, new SampleIdentifierId("/TEST-SPACE/TEST-PROJECT/FV-TEST"), true);
    }

    private String getMaterialProperty()
    {
        MaterialIdentifier identifier = new MaterialIdentifier(new MaterialTypeIdentifier("BACTERIUM"), "BACTERIUM1");
        List<Material> materials = generalInformationService.getMaterialByCodes(sessionToken, Collections.singletonList(identifier));
        return materials.get(0).getProperties().get("DESCRIPTION");
    }

    private String getDataSetProperty()
    {
        List<DataSet> dataSets = generalInformationService.getDataSetMetaData(sessionToken, Collections.singletonList("20081105092159188-3"));
        return dataSets.get(0).getProperties().get("COMMENT");
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 120;
    }

}
