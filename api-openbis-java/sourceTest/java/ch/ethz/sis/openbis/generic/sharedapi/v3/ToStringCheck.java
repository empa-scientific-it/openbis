/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.sharedapi.v3;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.property.PropertiesDeserializer;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ITableCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableColumn;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableLongCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableStringCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ServerTimeZone;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.TimeZone;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.LinkedDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ContentCopyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.lock.DataSetLockOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unlock.DataSetUnlockOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.LinkedDataUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.PhysicalDataUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.DeletedObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.CreateExperimentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionProgress;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdDeserializer;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainServiceExecutionResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermReplacement;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.create.WebAppSettingCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.FullDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.create.DataSetFileCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload.FastDownloadUtils;

/**
 * @author pkupczyk
 */
public class ToStringCheck
{
    private static final Set<Class<?>> IGNORED_CLASSES = new HashSet<>(
            Arrays.asList(FastDownloadUtils.class, SampleIdDeserializer.class,
                    PropertiesDeserializer.class));

    @Test
    public void testMissingToStringMethods() throws Exception
    {
        Collection<Class<?>> classes = ApiClassesProvider.getPublicClasses();
        Collection<Class<?>> classesWithoutToString = new ArrayList<Class<?>>();

        for (Class<?> clazz : classes)
        {
            if (filter(clazz))
            {
                Method method = clazz.getMethod("toString");

                if (method.getDeclaringClass().equals(Object.class))
                {
                    classesWithoutToString.add(clazz);
                }
            }
        }

        if (false == classesWithoutToString.isEmpty())
        {
            System.out.println("Classes without toString() method implemented:\n");
            for (Class<?> classWithoutToString : classesWithoutToString)
            {
                System.out.println(classWithoutToString);
            }
        }

        assertEquals(classesWithoutToString, Collections.emptyList());
    }

    private boolean filter(Class<?> clazz)
    {
        return Modifier.isPublic(clazz.getModifiers()) && false == Modifier.isAbstract(
                clazz.getModifiers())
                && IGNORED_CLASSES.contains(clazz) == false;
    }

    @Test
    public void testAttachmentCreation() throws Exception
    {
        AttachmentCreation o = new AttachmentCreation();
        o.setFileName("test-filename");
        o.setDescription("test-description");
        assertEquals(o.toString(), "AttachmentCreation[fileName=test-filename]");
    }

    @Test
    public void testAuthorizationGroup() throws Exception
    {
        AuthorizationGroup o = new AuthorizationGroup();
        o.setPermId(new AuthorizationGroupPermId("test-group"));
        assertEquals(o.toString(), "AuthorizationGroup[permId=TEST-GROUP]");
    }

    @Test
    public void testTableModel() throws Exception
    {
        List<TableColumn> columns = new ArrayList<TableColumn>();
        columns.add(new TableColumn("col1"));
        columns.add(new TableColumn("col2"));

        List<List<ITableCell>> rows = new ArrayList<List<ITableCell>>();
        rows.add(Arrays.asList(new TableStringCell("abc"), new TableLongCell(123)));

        TableModel o = new TableModel(columns, rows);

        assertEquals(o.toString(), "TableModel[columns={col1,col2},rowCount=1]");
    }

    @Test
    public void testFetchOptionsToStringBuilder() throws Exception
    {
        FetchOptionsToStringBuilder builder =
                new FetchOptionsToStringBuilder("Sample", new SampleFetchOptions());
        builder.addFetchOption("Type", new SampleTypeFetchOptions());
        builder.addFetchOption("Project", new ProjectFetchOptions());
        assertEquals(builder.toString(), "Sample\n    with Type\n    with Project\n");
    }

    @Test
    public void testSearchCriteriaToStringBuilder() throws Exception
    {
        SampleSearchCriteria c = new SampleSearchCriteria();
        c.withCode().thatEquals("abc");
        c.withType().withCode().thatStartsWith("prefix-");

        SearchCriteriaToStringBuilder builder = new SearchCriteriaToStringBuilder();
        builder.setCriteria(c.getCriteria());
        builder.setOperator(c.getOperator());
        builder.setName("sample");
        assertEquals(builder.toString(),
                "SAMPLE\n    with operator 'AND'\n    with attribute 'code' equal to 'abc'\n    with sample_type:\n        with attribute 'code' starts with 'prefix-'\n");
    }

    @Test
    public void testSearchResult() throws Exception
    {
        SearchResult<String> o = new SearchResult<String>(Arrays.asList("a", "b", "c"), 10);
        assertEquals(o.toString(), "SearchResult[objectCount=3,totalCount=10]");
    }

    @Test
    public void testServerTimeZone() throws Exception
    {
        ServerTimeZone o = new ServerTimeZone();
        assertEquals(o.toString(), "ServerTimeZone[]");
    }

    @Test
    public void testTimeZone() throws Exception
    {
        TimeZone o = new TimeZone(3);
        assertEquals(o.toString(), "TimeZone[hourOffset=3]");
    }

    @Test
    public void testIdListUpdateValue() throws Exception
    {
        IdListUpdateValue<ISampleId> o = new IdListUpdateValue<ISampleId>();
        o.add(new SamplePermId("p1"), new SampleIdentifier("i1"));
        o.remove(new SamplePermId("p2"), new SampleIdentifier("i2"));
        o.set(new SamplePermId("p3"), new SampleIdentifier("i3"));
        assertEquals(o.toString(), "IdListUpdateValue[added={P1,I1},removed={P2,I2},set={P3,I3}]");
    }

    @Test
    public void testFieldUpdateValue() throws Exception
    {
        FieldUpdateValue<String> o = new FieldUpdateValue<String>();
        o.setValue("abc");
        assertEquals(o.toString(), "FieldUpdateValue[value=abc]");

        FieldUpdateValue<String> o2 = new FieldUpdateValue<String>();
        assertEquals(o2.toString(), "FieldUpdateValue[]");
    }

    @Test
    public void testContentCopy() throws Exception
    {
        ContentCopy o = new ContentCopy();
        o.setId(new ContentCopyPermId("test-perm-id"));
        assertEquals(o.toString(), "ContentCopy[id=TEST-PERM-ID]");
    }

    @Test
    public void testDataSetArchiveOptions() throws Exception
    {
        DataSetArchiveOptions o = new DataSetArchiveOptions();
        o.setRemoveFromDataStore(true);
        assertEquals(o.toString(), "DataSetArchiveOptions[removeFromDataStore=true]");
    }

    @Test
    public void testContentCopyCreation() throws Exception
    {
        ContentCopyCreation o = new ContentCopyCreation();
        o.setExternalDmsId(new ExternalDmsPermId("test-dms-id"));
        o.setExternalId("test-external-id");
        assertEquals(o.toString(),
                "ContentCopyCreation[externalDmsId=TEST-DMS-ID,externalId=test-external-id]");
    }

    @Test
    public void testLinkedDataCreation() throws Exception
    {
        LinkedDataCreation o = new LinkedDataCreation();
        o.setExternalDmsId(new ExternalDmsPermId("test-dms-id"));
        o.setExternalCode("test-external-code");
        assertEquals(o.toString(),
                "LinkedDataCreation[externalDmsId=TEST-DMS-ID,externalCode=test-external-code]");
    }

    @Test
    public void testPhysicalDataCreation() throws Exception
    {
        PhysicalDataCreation o = new PhysicalDataCreation();
        o.setLocation("test-location");
        assertEquals(o.toString(), "PhysicalDataCreation[location=test-location]");
    }

    @Test
    public void testDataSetLockOptions() throws Exception
    {
        DataSetLockOptions o = new DataSetLockOptions();
        assertEquals(o.toString(), "DataSetLockOptions[]");
    }

    @Test
    public void testDataSetUnarchiveOptions() throws Exception
    {
        DataSetUnarchiveOptions o = new DataSetUnarchiveOptions();
        assertEquals(o.toString(), "DataSetUnarchiveOptions[]");
    }

    @Test
    public void testDataSetUnlockOptions() throws Exception
    {
        DataSetUnlockOptions o = new DataSetUnlockOptions();
        assertEquals(o.toString(), "DataSetUnlockOptions[]");
    }

    @Test
    public void testLinkedDataUpdate() throws Exception
    {
        LinkedDataUpdate o = new LinkedDataUpdate();
        o.setExternalDmsId(new ExternalDmsPermId("test-dms-id"));
        o.setExternalCode("test-external-code");
        assertEquals(o.toString(),
                "LinkedDataUpdate[externalDmsId=FieldUpdateValue[value=TEST-DMS-ID],externalCode=FieldUpdateValue[value=test-external-code]]");
    }

    @Test
    public void testPhysicalDataUpdate() throws Exception
    {
        PhysicalDataUpdate o = new PhysicalDataUpdate();
        assertEquals(o.toString(), "PhysicalDataUpdate[]");
    }

    @Test
    public void testDeletedObject() throws Exception
    {
        DeletedObject o = new DeletedObject();
        o.setId(new SamplePermId("test-perm-id"));
        assertEquals(o.toString(), "DeletedObject[id=TEST-PERM-ID]");
    }

    @Test
    public void testAsynchronousOperationExecutionResults() throws Exception
    {
        AsynchronousOperationExecutionResults o =
                new AsynchronousOperationExecutionResults(new OperationExecutionPermId());
        assertEquals(o.toString(),
                "AsynchronousOperationExecutionResults[executionId=" + o.getExecutionId()
                        .getPermId() + "]");
    }

    @Test
    public void testOperationExecutionProgress() throws Exception
    {
        OperationExecutionProgress o = new OperationExecutionProgress("test-message", 123, 234);
        assertEquals(o.toString(),
                "OperationExecutionProgress[message=test-message,numItemsProcessed=123,totalItemsToProcess=234]");
    }

    @Test
    public void testSynchronousOperationExecutionResults() throws Exception
    {
        CreateSamplesOperationResult result1 =
                new CreateSamplesOperationResult(Arrays.asList(new SamplePermId("test-perm-id-1")));
        CreateExperimentsOperationResult result2 = new CreateExperimentsOperationResult(
                Arrays.asList(new ExperimentPermId("test-perm-id-2")));
        SynchronousOperationExecutionResults o =
                new SynchronousOperationExecutionResults(Arrays.asList(result1, result2));
        assertEquals(o.toString(),
                "SynchronousOperationExecutionResults[results={CreateSamplesOperationResult[TEST-PERM-ID-1],CreateExperimentsOperationResult[TEST-PERM-ID-2]}]");
    }

    @Test
    public void testPlugin() throws Exception
    {
        Plugin o = new Plugin();
        o.setPermId(new PluginPermId("test-perm-id"));
        assertEquals(o.toString(), "Plugin[permId=test-perm-id]");
    }

    @Test
    public void testPropertyAssignmentCreation() throws Exception
    {
        PropertyAssignmentCreation o = new PropertyAssignmentCreation();
        o.setPropertyTypeId(new PropertyTypePermId("test-perm-id"));
        assertEquals(o.toString(), "PropertyAssignmentCreation[propertyTypeId=TEST-PERM-ID]");
    }

    @Test
    public void testRoleAssignment() throws Exception
    {
        RoleAssignment o = new RoleAssignment();
        o.setId(new RoleAssignmentTechId(1L));
        assertEquals(o.toString(), "RoleAssignment[id=1]");
    }

    @Test
    public void testSearchDomainServiceExecutionResult() throws Exception
    {
        SearchDomainServiceExecutionResult o = new SearchDomainServiceExecutionResult();
        o.setEntityKind(EntityKind.SAMPLE);
        o.setEntityPermId("test-perm-id");
        assertEquals(o.toString(),
                "SearchDomainServiceExecutionResult[entityKind=SAMPLE,entityPermId=test-perm-id]");
    }

    @Test
    public void testSessionInformation() throws Exception
    {
        SessionInformation o = new SessionInformation();
        o.setUserName("test-user-name");
        assertEquals(o.toString(), "SessionInformation[userName=test-user-name]");
    }

    @Test
    public void testVocabularyTermReplacement() throws Exception
    {
        VocabularyTermReplacement o = new VocabularyTermReplacement(
                new VocabularyTermPermId("test-code-1", "test-vocab-1"),
                new VocabularyTermPermId("test-code-2", "test-vocab-2"));
        assertEquals(o.toString(),
                "VocabularyTermReplacement[replacedId=TEST-CODE-1 (TEST-VOCAB-1),replacementId=TEST-CODE-2 (TEST-VOCAB-2)]");
    }

    @Test
    public void testWebAppSettingCreation() throws Exception
    {
        WebAppSettingCreation o = new WebAppSettingCreation("test-name", "test-value");
        assertEquals(o.toString(), "WebAppSettingCreation[name=test-name]");
    }

    @Test
    public void testFullDataSetCreation() throws Exception
    {
        DataSetCreation d = new DataSetCreation();
        d.setCode("test-code");
        d.setExperimentId(new ExperimentPermId("test-experiment-id"));

        DataSetFileCreation f = new DataSetFileCreation();
        f.setPath("test-path");

        FullDataSetCreation o = new FullDataSetCreation();
        o.setMetadataCreation(d);
        o.setFileMetadata(Arrays.asList(f));

        assertEquals(o.toString(),
                "FullDataSetCreation[metadataCreation=DataSetCreation[experimentId=TEST-EXPERIMENT-ID,sampleId=<null>,code=test-code],fileMetadata={DataSetFileCreation[path=test-path]}]");
    }

    @Test
    public void testUploadedDataSetCreation() throws Exception
    {
        UploadedDataSetCreation o = new UploadedDataSetCreation();
        o.setUploadId("test-upload-id");
        o.setExperimentId(new ExperimentPermId("test-experiment-id"));
        o.setSampleId(new SamplePermId("test-sample-id"));

        assertEquals(o.toString(),
                "UploadedDataSetCreation[uploadId=test-upload-id,experimentId=TEST-EXPERIMENT-ID,sampleId=TEST-SAMPLE-ID]");
    }

    @Test
    public void testDataSetFileDownload() throws Exception
    {
        DataSetFile f = new DataSetFile();
        f.setPermId(new DataSetFilePermId(new DataSetPermId("test-data-set-id"), "test-path"));
        DataSetFileDownload o = new DataSetFileDownload(f, null);

        assertEquals(o.toString(),
                "DataSetFileDownload[dataSetFile=DataSetFile[permId=DataSetFilePermId[dataSetId=TEST-DATA-SET-ID,filePath=test-path]]]");
    }

    @Test
    public void testDataSetFileDownloadOptions() throws Exception
    {
        DataSetFileDownloadOptions o = new DataSetFileDownloadOptions();
        o.setRecursive(true);

        assertEquals(o.toString(),
                "DataSetFileDownloadOptions[recursive=true]");
    }

}
