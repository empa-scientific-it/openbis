/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static junit.framework.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.delete.DeleteObjectsWithTrashOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DeleteDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.DeleteMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.OperationExecutionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.DeleteSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.UpdateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.CreateVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.DeleteVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;

/**
 * System tests for
 * {@link IApplicationServerApi#executeOperations(String, List, ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions)} for
 * various combinations.
 *
 * @author Franz-Josef Elmer
 */
public class MixedExecuteOperationsTest extends AbstractOperationExecutionTest
{
    private static final String PROPERTY_TYPE = "ORGANISM";

    private static final String MATERIAL_TYPE = "BACTERIUM";

    private static final EntityTypePermId MATERIAL_TYPE_ID = new EntityTypePermId(MATERIAL_TYPE);

    private static final VocabularyPermId VOCABULARY_ID = new VocabularyPermId(PROPERTY_TYPE);

    @Test
    public void testReplaceVocabularyTermForMaterialProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        long time = System.currentTimeMillis();
        List<IOperation> operations = new ArrayList<IOperation>();

        // create new material and two new vocabulary terms, one is a property value of the created material
        VocabularyTermCreation vt1 = new VocabularyTermCreation();
        vt1.setCode("ORG-" + time);
        vt1.setVocabularyId(VOCABULARY_ID);
        IVocabularyTermId id1 = getId(vt1);
        VocabularyTermCreation vt2 = new VocabularyTermCreation();
        vt2.setCode("ORG-" + time + "_2");
        vt2.setVocabularyId(VOCABULARY_ID);
        IVocabularyTermId id2 = getId(vt2);
        operations.add(new CreateVocabularyTermsOperation(vt1, vt2));
        MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode("BAC-" + time);
        materialCreation.setTypeId(MATERIAL_TYPE_ID);
        materialCreation.setProperty(PROPERTY_TYPE, vt1.getCode());
        materialCreation.setProperty("description", "test");
        IMaterialId materialId = getId(materialCreation);
        operations.add(new CreateMaterialsOperation(materialCreation));
        execute(sessionToken, operations);
        assertEquals(getMaterial(sessionToken, materialId).getProperty(PROPERTY_TYPE), vt1.getCode());

        // Delete first vocabulary term and replace it by the second one
        operations.clear();
        VocabularyTermDeletionOptions termDeletionOptions = createVocabularyTermDeletionOptions();
        termDeletionOptions.replace(id1, id2);
        operations.add(new DeleteVocabularyTermsOperation(Arrays.asList(id1), termDeletionOptions));
        execute(sessionToken, operations);
        assertEquals(getMaterial(sessionToken, materialId).getProperty(PROPERTY_TYPE), vt2.getCode());

        // Delete both vocabulary terms and the material
        operations.clear();
        operations.add(new DeleteVocabularyTermsOperation(Arrays.asList(id1, id2), createVocabularyTermDeletionOptions()));
        MaterialDeletionOptions materialDeletionOptions = new MaterialDeletionOptions();
        materialDeletionOptions.setReason("test material deletion");
        operations.add(new DeleteMaterialsOperation(Arrays.asList(materialId), materialDeletionOptions));
        execute(sessionToken, operations);
        assertEquals(v3api.getVocabularyTerms(sessionToken, Arrays.asList(id1, id2), new VocabularyTermFetchOptions()).size(), 0);
        assertEquals(v3api.getMaterials(sessionToken, Arrays.asList(materialId), new MaterialFetchOptions()).size(), 0);

        v3api.logout(sessionToken);
    }

    @Test
    public void testDeleteSampleWithDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        long time = System.currentTimeMillis();
        List<IOperation> operations = new ArrayList<IOperation>();

        // Create a sample with a data set
        SampleCreation sampleCreation = createSample("S-" + time);
        ISampleId sampleId = getId(sampleCreation);
        operations.add(new CreateSamplesOperation(sampleCreation));
        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        dataSetCreation.setTypeId(new EntityTypePermId("CONTAINER_TYPE"));
        dataSetCreation.setCode("DS-" + time);
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setSampleId(sampleId);
        DataSetPermId dataSetPermId = new DataSetPermId(dataSetCreation.getCode());
        operations.add(new CreateDataSetsOperation(dataSetCreation));
        execute(sessionToken, operations);

        // Delete sample and data set
        operations.clear();
        DataSetDeletionOptions dataSetDeletionOptions = new DataSetDeletionOptions();
        dataSetDeletionOptions.setReason("test data set deletion");
        operations.add(new DeleteDataSetsOperation(Arrays.asList(dataSetPermId), dataSetDeletionOptions));
        SampleDeletionOptions sampleDeletionOptions = createSampleDeletionOptions();
        operations.add(new DeleteSamplesOperation(Arrays.asList(sampleId), sampleDeletionOptions));
        SynchronousOperationExecutionResults executionResults = execute(sessionToken, operations);
        IDeletionId delId1 = getDeletionId(executionResults.getResults().get(0));
        IDeletionId delId2 = getDeletionId(executionResults.getResults().get(1));
        assertEquals(v3api.getSamples(sessionToken, Arrays.asList(sampleId), new SampleFetchOptions()).size(), 0);
        assertEquals(v3api.getDataSets(sessionToken, Arrays.asList(dataSetPermId), new DataSetFetchOptions()).size(), 0);

        // Clear trash
        assertEquals(delId1, null);
        assertNotNull(delId2);
        v3api.confirmDeletions(sessionToken, Arrays.asList(delId1, delId2));

        v3api.logout(sessionToken);
    }

    @Test
    public void testDeleteAndReplaceParentSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        long time = System.currentTimeMillis();
        List<IOperation> operations = new ArrayList<IOperation>();

        // Create parentA and parentB and a child
        SampleCreation pa = createSample("P-" + time + "_A");
        ISampleId paId = getId(pa);
        SampleCreation pb = createSample("P-" + time + "_B");
        ISampleId pbId = getId(pb);
        SampleCreation child = createSample("C-" + time);
        ISampleId childId = getId(child);
        child.setParentIds(Arrays.asList(paId, pbId));
        operations.add(new CreateSamplesOperation(pa, pb, child));
        execute(sessionToken, operations);
        Sample parentA = getSample(sessionToken, paId);
        assertEquals(parentA.getCode(), pa.getCode());
        assertEquals(parentA.getChildren().get(0).getCode(), child.getCode());
        Sample parentB = getSample(sessionToken, pbId);
        assertEquals(parentB.getCode(), pb.getCode());
        assertEquals(parentB.getChildren().get(0).getCode(), child.getCode());
        Sample childSample = getSample(sessionToken, childId);
        assertEquals(childSample.getCode(), child.getCode());
        List<String> parentsCode = extractCodes(childSample.getParents());
        Collections.sort(parentsCode);
        assertEquals(parentsCode.toString(), "[" + pa.getCode() + ", " + pb.getCode() + "]");

        // Create parentC, delete parentB, and replace parentB by parentC
        operations.clear();
        SampleCreation pc = createSample("P-" + time + "_C");
        ISampleId pcId = getId(pc);
        operations.add(new CreateSamplesOperation(pc));
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(childId);
        sampleUpdate.getParentIds().add(paId, pcId);
        operations.add(new UpdateSamplesOperation(sampleUpdate));
        operations.add(new DeleteSamplesOperation(Arrays.asList(pbId), createSampleDeletionOptions()));
        SynchronousOperationExecutionResults executionResults = execute(sessionToken, operations);
        IDeletionId deletionId = getDeletionId(executionResults.getResults().get(2));
        assertNotNull(deletionId);
        v3api.confirmDeletions(sessionToken, Arrays.asList(deletionId));
        assertEquals(getSample(sessionToken, paId).getChildren().get(0).getCode(), child.getCode());
        assertEquals(getSample(sessionToken, pbId), null);
        assertEquals(getSample(sessionToken, pcId).getChildren().get(0).getCode(), child.getCode());
        parentsCode = extractCodes(getSample(sessionToken, childId).getParents());
        Collections.sort(parentsCode);
        assertEquals(parentsCode.toString(), "[" + pa.getCode() + ", " + pc.getCode() + "]");

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        OperationExecutionSearchCriteria c = new OperationExecutionSearchCriteria();

        OperationExecutionFetchOptions fo = new OperationExecutionFetchOptions();
        fo.withDetails();
        fo.withSummary();

        v3api.searchOperationExecutions(sessionToken, c, fo);

        assertAccessLog(
                "search-operation-executions  SEARCH_CRITERIA:\n'OPERATION_EXECUTION\n'\nFETCH_OPTIONS:\n'OperationExecution\n    with Summary\n    with Details\n'");
    }

    private SampleCreation createSample(String code)
    {
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("NORMAL"));
        sampleCreation.setCode(code);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        return sampleCreation;
    }

    private ISampleId getId(SampleCreation sampleCreation)
    {
        ISpaceId spaceId = sampleCreation.getSpaceId();
        return new SampleIdentifier("/" + ((SpacePermId) spaceId).getPermId() + "/" + sampleCreation.getCode());
    }

    private IMaterialId getId(MaterialCreation materialCreation)
    {
        IEntityTypeId typeId = materialCreation.getTypeId();
        return new MaterialPermId(materialCreation.getCode(), ((EntityTypePermId) typeId).getPermId());
    }

    private IVocabularyTermId getId(VocabularyTermCreation vt)
    {
        IVocabularyId vocabularyId = vt.getVocabularyId();
        return new VocabularyTermPermId(vt.getCode(), ((VocabularyPermId) vocabularyId).getPermId());
    }

    private IDeletionId getDeletionId(IOperationResult operationResult)
    {
        if (operationResult instanceof DeleteObjectsWithTrashOperationResult)
        {
            return ((DeleteObjectsWithTrashOperationResult) operationResult).getDeletionId();
        }
        return null;
    }

    private SampleDeletionOptions createSampleDeletionOptions()
    {
        SampleDeletionOptions sampleDeletionOptions = new SampleDeletionOptions();
        sampleDeletionOptions.setReason("Test sample deletion");
        return sampleDeletionOptions;
    }

    private VocabularyTermDeletionOptions createVocabularyTermDeletionOptions()
    {
        VocabularyTermDeletionOptions termDeletionOptions = new VocabularyTermDeletionOptions();
        termDeletionOptions.setReason("test vocabulary term deletion");
        return termDeletionOptions;
    }

    private Material getMaterial(String sessionToken, IMaterialId materialId)
    {
        MaterialFetchOptions materialFetchOptions = new MaterialFetchOptions();
        materialFetchOptions.withProperties();
        return v3api.getMaterials(sessionToken, Arrays.asList(materialId), materialFetchOptions).get(materialId);
    }

    private Sample getSample(String sessionToken, ISampleId sampleId)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withChildren();
        fetchOptions.withParents();
        return v3api.getSamples(sessionToken, Arrays.asList(sampleId), fetchOptions).get(sampleId);
    }

    private SynchronousOperationExecutionResults execute(String sessionToken, List<IOperation> operations)
    {
        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        return (SynchronousOperationExecutionResults) v3api.executeOperations(sessionToken, operations, options);
    }
}
