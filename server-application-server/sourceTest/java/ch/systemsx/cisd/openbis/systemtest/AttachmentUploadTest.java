/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentVersions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class AttachmentUploadTest extends SystemTestCase
{
    private static final String FILE_CONTENT = "hello world";

    private static final String FILE_NAME = "hello.txt";

    @Test
    public void testUploadProjectAttachment()
    {
        SessionContext sessionContext = logIntoCommonClientService();
        TypedTableResultSet<Project> projects =
                commonClientService.listProjects(new DefaultResultSetConfig<String, TableModelRowWithObject<Project>>());
        Project project = projects.getResultSet().getList().extractOriginalObjects().get(0).getObjectOrNull();
        TechId projectID = TechId.create(project);

        uploadFile(sessionContext.getSessionID(), FILE_NAME, FILE_CONTENT);
        commonClientService.addAttachment(projectID, SESSION_KEY, AttachmentHolderKind.PROJECT,
                new NewAttachment(FILE_NAME, "my file", "example file"));

        AttachmentWithContent attachmentWithContent =
                genericServer.getProjectFileAttachment(sessionContext.getSessionID(), projectID,
                        FILE_NAME, 1);
        checkUploadedAttachment(projectID, AttachmentHolderKind.PROJECT, attachmentWithContent);
    }

    @Test
    public void testUploadExperimentAttachment()
    {
        SessionContext sessionContext = logIntoCommonClientService();
        TechId experimentID = new TechId(2);

        uploadFile(sessionContext.getSessionID(), FILE_NAME, FILE_CONTENT);
        commonClientService.addAttachment(experimentID, SESSION_KEY, AttachmentHolderKind.EXPERIMENT,
                new NewAttachment(FILE_NAME, "my file", "example file"));

        AttachmentWithContent attachmentWithContent =
                genericServer.getExperimentFileAttachment(sessionContext.getSessionID(), experimentID,
                        FILE_NAME, 1);
        checkUploadedAttachment(experimentID, AttachmentHolderKind.EXPERIMENT, attachmentWithContent);
    }

    @Test
    public void testUploadSampleAttachment()
    {
        SessionContext sessionContext = logIntoCommonClientService();
        TechId sampleID = new TechId(1);

        uploadFile(sessionContext.getSessionID(), FILE_NAME, FILE_CONTENT);
        commonClientService.addAttachment(sampleID, SESSION_KEY, AttachmentHolderKind.SAMPLE,
                new NewAttachment(FILE_NAME, "my file", "example file"));

        AttachmentWithContent attachmentWithContent =
                genericServer.getSampleFileAttachment(sessionContext.getSessionID(), sampleID,
                        FILE_NAME, 1);
        checkUploadedAttachment(sampleID, AttachmentHolderKind.SAMPLE, attachmentWithContent);
    }

    private void checkUploadedAttachment(TechId holderID, AttachmentHolderKind holderKind,
            AttachmentWithContent attachmentWithContent)
    {
        TypedTableResultSet<AttachmentVersions> attachmentVersions =
                commonClientService
                        .listAttachmentVersions(
                                holderID,
                                holderKind,
                                new DefaultResultSetConfig<String, TableModelRowWithObject<AttachmentVersions>>());

        GridRowModels<TableModelRowWithObject<AttachmentVersions>> list = attachmentVersions.getResultSet().getList();
        for (GridRowModel<TableModelRowWithObject<AttachmentVersions>> tmrl : list)
        {
            Attachment attachment = tmrl.getOriginalObject().getObjectOrNull().getVersions().get(0);
            if (attachment.getFileName().equals(FILE_NAME))
            {
                assertEquals("my file", attachment.getTitle());
                assertEquals("example file", attachment.getDescription());
                assertEquals(1, attachment.getVersion());
                assertEquals(FILE_CONTENT, new String(attachmentWithContent.getContent()));
                return;
            }
        }

        fail("Attachment with file name " + FILE_NAME + " was not found");
    }
}
