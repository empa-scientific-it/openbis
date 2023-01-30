/**Copyright 2009 ETH Zuerich,CISD**Licensed under the Apache License,Version 2.0(the"License");*you may not use this file except in compliance with the License.*You may obtain a copy of the License at**http://www.apache.org/licenses/LICENSE-2.0
**Unless required by applicable law or agreed to in writing,software*distributed under the License is distributed on an"AS IS"BASIS,*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.*See the License for the specific language governing permissions and*limitations under the License.*/

package ch.systemsx.cisd.openbis.generic.shared.translator;

import static ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.TestJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.ContentCopyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocationType;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;

/***
 * @author Franz-Josef Elmer
 */
// TODO write test with translation of components
public class DataSetTranslatorTest extends AssertJUnit
{
    private static final String DOWNLOAD_URL = "url";

    private static final String BASE_INDEX_URL = "index.html";

    @Test
    public void testTranslationOfEmptyExternalDataPE()
    {
        ExternalDataPE externalDataPE = new ExternalDataPE();
        externalDataPE.setDataStore(createStore());
        AbstractExternalData data =
                DataSetTranslator.translate(externalDataPE, BASE_INDEX_URL, null,
                        new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()), null);

        PhysicalDataSet translated = data.tryGetAsDataSet();

        assertEquals(null, translated.getCode());
        assertEquals(null, translated.getExperiment());
        assertEquals(null, translated.getProductionDate());
        assertEquals(null, translated.getComplete());
        assertEquals(0, translated.getParents().size());
    }

    @Test
    public void testTranslationOfFullFleshedExternalDataPE()
    {
        ExternalDataPE externalDataPE = new ExternalDataPE();
        externalDataPE.setCode("code");
        externalDataPE.setDataStore(createStore());
        externalDataPE.setComplete(BooleanOrUnknown.F);
        externalDataPE.setDataProducerCode("dataProducerCode");
        DataSetTypePE dataSetTypePE = new DataSetTypePE();
        dataSetTypePE.setCode("dataSetTypeCode");
        dataSetTypePE.setDescription("dataSetTypeDescription");
        externalDataPE.setDataSetType(dataSetTypePE);
        externalDataPE.setDataSetKind(DataSetKind.PHYSICAL.name());
        FileFormatTypePE fileFormatTypePE = new FileFormatTypePE();
        fileFormatTypePE.setCode("fileFormatTypeCode");
        fileFormatTypePE.setDescription("fileFormatTypeDescription");
        externalDataPE.setFileFormatType(fileFormatTypePE);
        externalDataPE.setShareId("share id");
        externalDataPE.setLocation("location");
        externalDataPE.setSize(4711L);
        LocatorTypePE locatorTypePE = new LocatorTypePE();
        locatorTypePE.setCode("locatorTypeCode");
        locatorTypePE.setDescription("locatorTypeDescription");
        externalDataPE.setLocatorType(locatorTypePE);
        ExperimentPE experimentPE = new ExperimentPE();
        experimentPE.setCode("my-experiment");
        experimentPE.setExperimentType(new ExperimentTypePE());
        ProjectPE projectPE = new ProjectPE();
        projectPE.setCode("my-project");
        SpacePE groupPE = new SpacePE();
        groupPE.setCode("my-group");
        projectPE.setSpace(groupPE);
        experimentPE.setProject(projectPE);
        externalDataPE.setExperiment(experimentPE);
        externalDataPE.setProductionDate(new Date(1));
        externalDataPE.setRegistrationDate(new Date(2));
        SamplePE samplePE = new SamplePE();
        samplePE.setCode("sample");
        SampleTypePE sampleTypePE = new SampleTypePE();
        sampleTypePE.setCode("sampleTypeCode");
        sampleTypePE.setDescription("sampleTypeDescription");
        samplePE.setSampleType(sampleTypePE);
        DeletionPE deletionPE = new DeletionPE();
        deletionPE.setReason("reason");
        deletionPE.setRegistrationDate(new Date(3));
        PersonPE personPE = new PersonPE();
        personPE.setUserId("user");
        deletionPE.setRegistrator(personPE);
        externalDataPE.setDeletion(deletionPE);
        externalDataPE.setSampleAcquiredFrom(samplePE);
        AbstractExternalData data =
                DataSetTranslator.translate(externalDataPE, BASE_INDEX_URL, null,
                        new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()), null);

        PhysicalDataSet translated = data.tryGetAsDataSet();

        assertEquals(DOWNLOAD_URL, translated.getDataStore().getHostUrl());
        assertEquals(DOWNLOAD_URL + "/" + DATA_STORE_SERVER_WEB_APPLICATION_NAME, translated
                .getDataStore().getDownloadUrl());
        assertEquals("code", translated.getCode());
        assertEquals(Boolean.FALSE, translated.getComplete());
        assertEquals("dataProducerCode", translated.getDataProducerCode());
        assertEquals("dataSetTypeCode", translated.getDataSetType().getCode());
        assertEquals("dataSetTypeDescription", translated.getDataSetType().getDescription());
        assertEquals("fileFormatTypeCode", translated.getFileFormatType().getCode());
        assertEquals("fileFormatTypeDescription", translated.getFileFormatType().getDescription());
        assertEquals("location", translated.getLocation());
        assertEquals(4711L, translated.getSize().longValue());
        assertEquals("locatorTypeCode", translated.getLocatorType().getCode());
        assertEquals("locatorTypeDescription", translated.getLocatorType().getDescription());
        assertEquals(0, translated.getParents().size());
        assertEquals("my-experiment", translated.getExperiment().getCode());
        assertEquals(1, translated.getProductionDate().getTime());
        assertEquals(2, translated.getRegistrationDate().getTime());
        assertEquals("/sample", translated.getSampleIdentifier());
        assertEquals("sampleTypeCode", translated.getSampleType().getCode());
        assertEquals("sampleTypeDescription", translated.getSampleType().getDescription());
        assertEquals(false, translated.isDerived());
        assertEquals("reason", translated.getDeletion().getReason());
        assertEquals(3, translated.getDeletion().getRegistrationDate().getTime());
        assertEquals("user", translated.getDeletion().getRegistrator().getUserId());
    }

    @Test
    public void testTranslationADerivedExternalDataPEWithParents()
    {
        ExternalDataPE externalDataPE = new ExternalDataPE();
        externalDataPE.setCode("DS1");
        externalDataPE.setDataStore(createStore());
        externalDataPE.setDerived(true);

        ExperimentPE experimentPE = new ExperimentPE();
        experimentPE.setCode("my-experiment");
        experimentPE.setExperimentType(new ExperimentTypePE());
        ProjectPE projectPE = new ProjectPE();
        projectPE.setCode("my-project");
        SpacePE groupPE = new SpacePE();
        groupPE.setCode("my-group");
        projectPE.setSpace(groupPE);
        experimentPE.setProject(projectPE);
        externalDataPE.setExperiment(experimentPE);

        externalDataPE.addParentRelationship(createParentRelationship(externalDataPE, "parent-1"));
        externalDataPE.addParentRelationship(createParentRelationship(externalDataPE, "parent-2"));

        AbstractExternalData externalData =
                DataSetTranslator.translate(externalDataPE, BASE_INDEX_URL, null,
                        new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()), null);

        assertEquals("my-experiment", externalData.getExperiment().getCode());
        assertEquals(2, externalData.getParents().size());
        Set<String> parentCodes = extractParentCodes(externalData);
        assertEquals(true, parentCodes.contains("parent-1"));
        assertEquals(true, parentCodes.contains("parent-2"));
        assertEquals(true, externalData.isDerived());
        assertEquals(null, externalData.getDeletion());
    }

    @Test
    public void testTranslateLinkDataPE()
    {
        LinkDataPE linkDataPE = new LinkDataPE();
        linkDataPE.setCode("TEST_CODE");
        linkDataPE.setDataStore(createStore());

        ExperimentPE experimentPE = new ExperimentPE();
        experimentPE.setCode("my-experiment");
        experimentPE.setExperimentType(new ExperimentTypePE());
        ProjectPE projectPE = new ProjectPE();
        projectPE.setCode("my-project");
        SpacePE groupPE = new SpacePE();
        groupPE.setCode("my-group");
        projectPE.setSpace(groupPE);
        experimentPE.setProject(projectPE);
        linkDataPE.setExperiment(experimentPE);

        ExternalDataManagementSystemPE edms = new ExternalDataManagementSystemPE();
        edms.setCode("EDMS");
        edms.setLabel("Label");
        edms.setAddress("http://ethz.ch");
        edms.setAddressType(ExternalDataManagementSystemType.URL);

        ContentCopyPE copy = new ContentCopyPE();
        copy.setExternalCode("TEST EXTERNAL CODE");
        copy.setExternalDataManagementSystem(edms);
        copy.setLocationType(LocationType.OPENBIS);
        copy.setDataSet(linkDataPE);
        linkDataPE.setContentCopies(Collections.singleton(copy));

        linkDataPE.addParentRelationship(createParentRelationship(linkDataPE, "parent-1"));
        linkDataPE.addParentRelationship(createParentRelationship(linkDataPE, "parent-2"));

        AbstractExternalData externalData =
                DataSetTranslator.translate(linkDataPE, BASE_INDEX_URL, null,
                        new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()), null);

        assertEquals("my-experiment", externalData.getExperiment().getCode());
        assertEquals(2, externalData.getParents().size());
        Set<String> parentCodes = extractParentCodes(externalData);
        assertEquals(true, parentCodes.contains("parent-1"));
        assertEquals(true, parentCodes.contains("parent-2"));
        assertEquals(false, externalData.isDerived());
        assertNull(externalData.getDeletion());

        assertTrue(externalData.isLinkData());
        assertNotNull(externalData.tryGetAsLinkDataSet());
        LinkDataSet linkData = externalData.tryGetAsLinkDataSet();
        assertEquals(linkDataPE.getCode(), linkData.getCode());
        assertEquals(linkDataPE.getContentCopies().iterator().next().getExternalCode(), linkData.getExternalCode());
        assertNotNull(linkData.getExternalDataManagementSystem());
        assertEquals(linkDataPE.getContentCopies().iterator().next().getExternalDataManagementSystem().getCode(), linkData
                .getExternalDataManagementSystem().getCode());
        assertEquals(linkDataPE.getContentCopies().iterator().next().getExternalDataManagementSystem().getLabel(), linkData
                .getExternalDataManagementSystem().getLabel());
    }

    private Set<String> extractParentCodes(AbstractExternalData externalData)
    {
        final Set<String> result = new HashSet<String>();
        for (AbstractExternalData parent : externalData.getParents())
        {
            result.add(parent.getCode());
        }
        return result;
    }

    private DataSetRelationshipPE createParentRelationship(DataPE child, String parentCode)
    {
        DataPE parent = new DataPE();
        parent.setCode(parentCode);
        parent.setDataStore(createStore());
        RelationshipTypePE relationshipType = new RelationshipTypePE();
        relationshipType.setCode(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        return new DataSetRelationshipPE(parent, child, relationshipType, null, new PersonPE());
    }

    private DataStorePE createStore()
    {
        DataStorePE store = new DataStorePE();
        store.setDownloadUrl(DOWNLOAD_URL);
        return store;
    }

}
