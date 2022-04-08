/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.freezing;

import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.LinkedDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.LinkedDataUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetFreezingTest extends FreezingTest
{
    private static final String PREFIX = "DSFT-";

    private static final String DATA_SET_1 = PREFIX + "1";

    private static final String DATA_SET_2 = PREFIX + "2";

    private static final String DATA_SET_3 = PREFIX + "3";

    private static final String DATA_SET_4 = PREFIX + "4";

    private TagPermId blueTag;

    private DataSetPermId dataSet1;

    private DataSetPermId dataSet2;

    private SamplePermId sampleId;

    private DataSetPermId dataSet3;

    private DataSetPermId dataSet4;

    @BeforeMethod
    public void createDataSetExamples()
    {
        DataSetCreation ds1 = dataSet(DATA_SET_1);
        sampleId = new SamplePermId("200811050929940-1018");
        ds1.setSampleId(sampleId);
        ds1.setDataSetKind(DataSetKind.CONTAINER);
        ds1.setProperty("DESCRIPTION", "testing");
        DataSetCreation ds2 = physicalDataSet(DATA_SET_2);
        ds2.setSampleId(sampleId);
        DataSetCreation ds3 = dataSet(DATA_SET_3);
        ds3.setSampleId(sampleId);
        ds3.setDataSetKind(DataSetKind.LINK);
        LinkedDataCreation linkedData = new LinkedDataCreation();
        ContentCopyCreation ccCreation = new ContentCopyCreation();
        ccCreation.setExternalDmsId(new ExternalDmsPermId("DMS_3"));
        ccCreation.setPath("a/b/c");
        linkedData.setContentCopies(Arrays.asList(ccCreation));
        ds3.setLinkedData(linkedData);
        DataSetCreation ds4 = dataSet(DATA_SET_4);
        ds4.setSampleId(sampleId);
        ds4.setDataSetKind(DataSetKind.LINK);
        LinkedDataCreation linkedData2 = new LinkedDataCreation();
        linkedData2.setContentCopies(Arrays.asList());
        ds4.setLinkedData(linkedData2);

        List<DataSetPermId> dataSetIds = v3api.createDataSets(systemSessionToken, Arrays.asList(ds1, ds2, ds3, ds4));
        dataSet1 = dataSetIds.get(0);
        dataSet2 = dataSetIds.get(1);
        dataSet3 = dataSetIds.get(2);
        dataSet4 = dataSetIds.get(3);

        TagCreation tagCreation = new TagCreation();
        tagCreation.setCode("blue");
        tagCreation.setDataSetIds(Arrays.asList(dataSet2));
        blueTag = v3api.createTags(systemSessionToken, Arrays.asList(tagCreation)).get(0);
    }

    @Test
    public void testTrash()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteDataSets(systemSessionToken, Arrays.asList(dataSet1), deletionOptions),
                // Then
                "ERROR: Operation TRASH is not allowed because data set " + DATA_SET_1 + " is frozen.");
    }

    @Test
    public void testDeletePermanently()
    {
        // Given
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(dataSet1), deletionOptions);
        setFrozenFlagForDataSets(true, dataSet1);

        // When
        assertUserFailureException(Void -> v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId)),
                // Then
                "ERROR: Operation DELETE PROPERTY is not allowed because data set " + DATA_SET_1 + " is frozen.");
    }

    @Test
    public void testDeletePermanentlyDataSetWithoutProperties()
    {
        // Given
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(dataSet2), deletionOptions);
        setFrozenFlagForDataSets(true, dataSet2);

        // When
        assertUserFailureException(Void -> v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId)),
                // Then
                "ERROR: Operation DELETE is not allowed because data set " + DATA_SET_2 + " is frozen.");
    }

    @Test
    public void testAddTag()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        assertEquals(getDataSet(dataSet1).getTags().toString(), "[]");
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getTagIds().add(blueTag);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getTags().toString(), "[Tag blue]");
    }

    @Test
    public void testRemoveTag()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet2);
        assertEquals(getDataSet(dataSet2).getTags().toString(), "[Tag blue]");
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet2);
        dataSetUpdate.getTagIds().remove(blueTag);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet2).getTags().toString(), "[]");
    }

    @Test
    public void testAddProperty()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet2);
        dataSetUpdate.setProperty("DESCRIPTION", "my description");

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation INSERT PROPERTY is not allowed because data set " + DATA_SET_2 + " is frozen.");
    }

    @Test
    public void testAddPropertyToMoltenDataSet()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet2);
        setFrozenFlagForDataSets(false, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet2);
        dataSetUpdate.setProperty("DESCRIPTION", "my description");

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet2).getProperty("DESCRIPTION"), "my description");
    }

    @Test
    public void testChangeProperty()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.setProperty("DESCRIPTION", "my description");

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation UPDATE PROPERTY is not allowed because data set " + DATA_SET_1 + " is frozen.");
    }

    @Test
    public void testChangePropertyForMoltenDataSet()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        setFrozenFlagForDataSets(false, dataSet1);
        assertEquals(getDataSet(dataSet1).getProperty("DESCRIPTION"), "testing");
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.setProperty("DESCRIPTION", "my description");

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getProperty("DESCRIPTION"), "my description");
    }

    @Test
    public void testDeleteProperty()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.setProperty("DESCRIPTION", null);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation DELETE PROPERTY is not allowed because data set " + DATA_SET_1 + " is frozen.");
    }

    @Test
    public void testDeletePropertyForMoltenDataSet()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        setFrozenFlagForDataSets(false, dataSet1);
        assertEquals(getDataSet(dataSet1).getProperty("DESCRIPTION"), "testing");
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.setProperty("DESCRIPTION", null);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getProperty("DESCRIPTION"), null);
    }

    @Test
    public void testAddContentCopyForFrozenLinkDataSet()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet4);
        assertEquals(getDataSet(dataSet4).getLinkedData().getContentCopies().toString(), "[]");
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet4);
        LinkedDataUpdate linkedDataUpdate = new LinkedDataUpdate();
        ContentCopyCreation contentCopyCreation = new ContentCopyCreation();
        contentCopyCreation.setExternalDmsId(new ExternalDmsPermId("DMS_3"));
        contentCopyCreation.setPath("a/b/c/d");
        linkedDataUpdate.getContentCopies().add(contentCopyCreation);
        dataSetUpdate.getLinkedData().setValue(linkedDataUpdate);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet4).getLinkedData().getContentCopies().get(0).getPath(), "/a/b/c/d");
    }

    @Test
    public void testDeleteContentCopyForFrozenLinkDataSet()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet3);
        ContentCopy contentCopy = getDataSet(dataSet3).getLinkedData().getContentCopies().get(0);
        assertEquals(contentCopy.getPath(), "/a/b/c");
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet3);
        LinkedDataUpdate linkedDataUpdate = new LinkedDataUpdate();
        linkedDataUpdate.getContentCopies().remove(contentCopy.getId());
        dataSetUpdate.getLinkedData().setValue(linkedDataUpdate);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet3).getLinkedData().getContentCopies().toString(), "[]");
    }

    @Test(dataProvider = "LiquidComponentContainerRelations")
    public void testValidAddComponentToContainer(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsOfContainer, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfComponent, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
    }

    @Test(dataProvider = "LiquidComponentContainerRelations")
    public void testValidRemoveComponentFromContainer(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
        setFrozenFlagsForDataSets(frozenFlagsOfContainer, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfComponent, dataSet2);

        dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().remove(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getComponents().toString(), "[]");
    }

    @DataProvider(name = "LiquidComponentContainerRelations")
    public static Object[][] liquidComponentContainerRelations()
    {
        List<FrozenFlags> combinationsForContainer =
                new FrozenFlags(true).freezeForChildren().freezeForParents().freezeForContainers().createAllCombinations();
        combinationsForContainer.add(new FrozenFlags(false).freezeForComponents());
        List<FrozenFlags> combinationsForComponent =
                new FrozenFlags(true).freezeForChildren().freezeForParents().freezeForComponents().createAllCombinations();
        combinationsForComponent.add(new FrozenFlags(false).freezeForContainers());
        return asCartesianProduct(combinationsForContainer, combinationsForComponent);
    }

    @Test(dataProvider = "FrozenComponentContainerRelations")
    public void testInvalidAddComponentToContainer(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsOfContainer, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfComponent, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation INSERT CONTAINER_COMPONENT is not allowed because data set " + DATA_SET_1
                        + " or " + DATA_SET_2 + " is frozen.");
    }

    @Test(dataProvider = "FrozenComponentContainerRelations")
    public void testInvalidAddComponentToContainerAfterMelting(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsOfContainer, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfComponent, dataSet2);
        setFrozenFlagsForDataSets(frozenFlagsOfContainer.clone().melt(), dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfComponent.clone().melt(), dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
    }

    @Test(dataProvider = "FrozenComponentContainerRelations")
    public void testInvalidRemoveComponentFromContainer(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
        setFrozenFlagsForDataSets(frozenFlagsOfContainer, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfComponent, dataSet2);

        DataSetUpdate dataSetUpdate2 = new DataSetUpdate();
        dataSetUpdate2.setDataSetId(dataSet1);
        dataSetUpdate2.getComponentIds().remove(dataSet2);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate2)),
                // Then
                "ERROR: Operation DELETE CONTAINER_COMPONENT is not allowed because data set " + DATA_SET_1
                        + " or " + DATA_SET_2 + " is frozen.");
    }

    @Test(dataProvider = "FrozenComponentContainerRelations")
    public void testInvalidRemoveComponentFromContainerAfterMelting(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
        setFrozenFlagsForDataSets(frozenFlagsOfContainer, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfComponent, dataSet2);
        setFrozenFlagsForDataSets(frozenFlagsOfContainer.clone().melt(), dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfComponent.clone().melt(), dataSet2);

        DataSetUpdate dataSetUpdate2 = new DataSetUpdate();
        dataSetUpdate2.setDataSetId(dataSet1);
        dataSetUpdate2.getComponentIds().remove(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate2));

        // Then
        assertEquals(getDataSet(dataSet1).getComponents().toString(), "[]");
    }

    @DataProvider(name = "FrozenComponentContainerRelations")
    public static Object[][] frozenComponentContainerRelations()
    {
        List<FrozenFlags> combinationsForContainer =
                new FrozenFlags(true).freezeForChildren().freezeForParents().freezeForContainers().createAllCombinations();
        FrozenFlags componentFrozenForContainers = new FrozenFlags(true).freezeForContainers();
        Object[][] combinationForInvalidComponent =
                asCartesianProduct(combinationsForContainer, Arrays.asList(componentFrozenForContainers));
        List<FrozenFlags> combinationsForComponent =
                new FrozenFlags(true).freezeForChildren().freezeForParents().freezeForComponents().createAllCombinations();
        FrozenFlags containerFrozenForComponents = new FrozenFlags(true).freezeForComponents();
        Object[][] combinationForInvalidContainer =
                asCartesianProduct(Arrays.asList(containerFrozenForComponents), combinationsForComponent);
        return merge(asCartesianProduct(Arrays.asList(containerFrozenForComponents), Arrays.asList(componentFrozenForContainers)),
                combinationForInvalidContainer, combinationForInvalidComponent);
    }

    @Test(dataProvider = "LiquidChildParentRelations")
    public void testValidAddChildToParent(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsOfParent, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfChild, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
    }

    @Test(dataProvider = "LiquidChildParentRelations")
    public void testValidRemoveChildFromParent(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
        setFrozenFlagsForDataSets(frozenFlagsOfParent, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfChild, dataSet2);

        dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().remove(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getChildren().toString(), "[]");
    }

    @DataProvider(name = "LiquidChildParentRelations")
    public static Object[][] liquidChildParentRelations()
    {
        List<FrozenFlags> combinationsForParent =
                new FrozenFlags(true).freezeForComponents().freezeForContainers().freezeForParents().createAllCombinations();
        combinationsForParent.add(new FrozenFlags(false).freezeForChildren());
        List<FrozenFlags> combinationsForChild =
                new FrozenFlags(true).freezeForComponents().freezeForContainers().freezeForChildren().createAllCombinations();
        combinationsForChild.add(new FrozenFlags(false).freezeForParents());
        return asCartesianProduct(combinationsForParent, combinationsForChild);
    }

    @Test(dataProvider = "FrozenChildParentRelations")
    public void testInvalidAddChildToParent(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsOfParent, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfChild, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation INSERT PARENT_CHILD is not allowed because data set " + DATA_SET_1
                        + " or " + DATA_SET_2 + " is frozen.");
    }

    @Test(dataProvider = "FrozenChildParentRelations")
    public void testInvalidAddChildToParentAfterMelting(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsOfParent, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfChild, dataSet2);
        setFrozenFlagsForDataSets(frozenFlagsOfParent.clone().melt(), dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfChild.clone().melt(), dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
    }

    @Test(dataProvider = "FrozenChildParentRelations")
    public void testInvalidRemoveChildFromParent(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
        setFrozenFlagsForDataSets(frozenFlagsOfParent, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfChild, dataSet2);

        DataSetUpdate dataSetUpdate2 = new DataSetUpdate();
        dataSetUpdate2.setDataSetId(dataSet1);
        dataSetUpdate2.getChildIds().remove(dataSet2);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate2)),
                // Then
                "ERROR: Operation DELETE PARENT_CHILD is not allowed because data set " + DATA_SET_1
                        + " or " + DATA_SET_2 + " is frozen.");
    }

    @Test(dataProvider = "FrozenChildParentRelations")
    public void testInvalidRemoveChildFromParentAfterMelting(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
        setFrozenFlagsForDataSets(frozenFlagsOfParent, dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfChild, dataSet2);
        setFrozenFlagsForDataSets(frozenFlagsOfParent.clone().melt(), dataSet1);
        setFrozenFlagsForDataSets(frozenFlagsOfChild.clone().melt(), dataSet2);

        DataSetUpdate dataSetUpdate2 = new DataSetUpdate();
        dataSetUpdate2.setDataSetId(dataSet1);
        dataSetUpdate2.getChildIds().remove(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate2));

        // Then
        assertEquals(getDataSet(dataSet1).getChildren().toString(), "[]");
    }

    @DataProvider(name = "FrozenChildParentRelations")
    public static Object[][] frozenChildParentRelations()
    {
        List<FrozenFlags> combinationsForParent =
                new FrozenFlags(true).freezeForComponents().freezeForContainers().freezeForParents().createAllCombinations();
        FrozenFlags childFrozenForParents = new FrozenFlags(true).freezeForParents();
        Object[][] combinationForInvalidChild =
                asCartesianProduct(combinationsForParent, Arrays.asList(childFrozenForParents));
        List<FrozenFlags> combinationsForChild =
                new FrozenFlags(true).freezeForComponents().freezeForContainers().freezeForChildren().createAllCombinations();
        FrozenFlags parentFrozenForChilds = new FrozenFlags(true).freezeForChildren();
        Object[][] combinationForInvalidParent =
                asCartesianProduct(Arrays.asList(parentFrozenForChilds), combinationsForChild);
        return merge(asCartesianProduct(Arrays.asList(parentFrozenForChilds), Arrays.asList(childFrozenForParents)),
                combinationForInvalidParent, combinationForInvalidChild);
    }

    @Test(dataProvider = "liquidParent")
    public void testValidCreateChildDataSet(FrozenFlags frozenFlagsForParent)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForParent, dataSet1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setParentIds(Arrays.asList(dataSet1));

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getParents().get(0).getCode(), DATA_SET_1);
    }

    @DataProvider(name = "liquidParent")
    public static Object[][] liquidParent()
    {
        List<FrozenFlags> combinationsForLiquidParent = new FrozenFlags(true).freezeForComponents()
                .freezeForContainers().freezeForParents().createAllCombinations();
        combinationsForLiquidParent.add(new FrozenFlags(false).freezeForChildren());
        return asCartesianProduct(combinationsForLiquidParent);
    }

    @Test
    public void testInvalidCreateChildDataSet()
    {
        // Given
        setFrozenFlagsForDataSets(new FrozenFlags(true).freezeForChildren(), dataSet1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setParentIds(Arrays.asList(dataSet1));

        // When
        assertUserFailureException(Void -> v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)),
                // Then
                "ERROR: Operation INSERT PARENT_CHILD is not allowed because data set " + DATA_SET_1
                        + " or " + dataSetCreation.getCode() + " is frozen.");

    }

    @Test
    public void testInvalidCreateChildDataSetAfterMelting()
    {
        // Given
        FrozenFlags frozenFlags = new FrozenFlags(true).freezeForChildren();
        setFrozenFlagsForDataSets(frozenFlags, dataSet1);
        setFrozenFlagsForDataSets(frozenFlags.clone().melt(), dataSet1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setParentIds(Arrays.asList(dataSet1));

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getParents().get(0).getCode(), DATA_SET_1);
    }

    @Test(dataProvider = "liquidChild")
    public void testValidCreateParentDataSet(FrozenFlags frozenFlagsForChild)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForChild, dataSet1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setChildIds(Arrays.asList(dataSet1));

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getChildren().get(0).getCode(), DATA_SET_1);
    }

    @DataProvider(name = "liquidChild")
    public static Object[][] liquidChild()
    {
        List<FrozenFlags> combinationsForLiquidChild = new FrozenFlags(true).freezeForComponents()
                .freezeForContainers().freezeForChildren().createAllCombinations();
        combinationsForLiquidChild.add(new FrozenFlags(false).freezeForParents());
        return asCartesianProduct(combinationsForLiquidChild);
    }

    @Test
    public void testInvalidCreateParentDataSet()
    {
        // Given
        setFrozenFlagsForDataSets(new FrozenFlags(true).freezeForParents(), dataSet1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setChildIds(Arrays.asList(dataSet1));

        // When
        assertUserFailureException(Void -> v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)),
                // Then
                "ERROR: Operation INSERT PARENT_CHILD is not allowed because data set " + dataSetCreation.getCode()
                        + " or " + DATA_SET_1 + " is frozen.");

    }

    @Test
    public void testInvalidCreateParentDataSetAfterMelting()
    {
        // Given
        FrozenFlags frozenFlags = new FrozenFlags(true).freezeForParents();
        setFrozenFlagsForDataSets(frozenFlags, dataSet1);
        setFrozenFlagsForDataSets(frozenFlags.clone().melt(), dataSet1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setChildIds(Arrays.asList(dataSet1));

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getChildren().get(0).getCode(), DATA_SET_1);
    }

    @Test(dataProvider = "liquidContainer")
    public void testValidCreateComponentDataSet(FrozenFlags frozenFlagsForContainer)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForContainer, dataSet1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setContainerIds(Arrays.asList(dataSet1));

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getContainers().get(0).getCode(), DATA_SET_1);
    }

    @DataProvider(name = "liquidContainer")
    public static Object[][] liquidContainer()
    {
        List<FrozenFlags> combinationsForLiquidContainer = new FrozenFlags(true).freezeForChildren()
                .freezeForContainers().freezeForParents().createAllCombinations();
        combinationsForLiquidContainer.add(new FrozenFlags(false).freezeForComponents());
        return asCartesianProduct(combinationsForLiquidContainer);
    }

    @Test
    public void testInvalidCreateComponentDataSet()
    {
        // Given
        setFrozenFlagsForDataSets(new FrozenFlags(true).freezeForComponents(), dataSet1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setContainerIds(Arrays.asList(dataSet1));

        // When
        assertUserFailureException(Void -> v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)),
                // Then
                "ERROR: Operation INSERT CONTAINER_COMPONENT is not allowed because data set " + DATA_SET_1
                        + " or " + dataSetCreation.getCode() + " is frozen.");

    }

    @Test
    public void testInvalidCreateComponentDataSetAfterMelting()
    {
        // Given
        FrozenFlags frozenFlags = new FrozenFlags(true).freezeForComponents();
        setFrozenFlagsForDataSets(frozenFlags, dataSet1);
        setFrozenFlagsForDataSets(frozenFlags.clone().melt(), dataSet1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setContainerIds(Arrays.asList(dataSet1));

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getContainers().get(0).getCode(), DATA_SET_1);
    }

    @Test(dataProvider = "liquidComponent")
    public void testValidCreateContainerDataSet(FrozenFlags frozenFlagsForComponent)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForComponent, dataSet2);
        DataSetCreation dataSetCreation = dataSet(PREFIX + "D2");
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setComponentIds(Arrays.asList(dataSet2));

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getComponents().get(0).getCode(), DATA_SET_2);
    }

    @DataProvider(name = "liquidComponent")
    public static Object[][] liquidComponent()
    {
        List<FrozenFlags> combinationsForLiquidComponent = new FrozenFlags(true).freezeForComponents()
                .freezeForParents().freezeForChildren().createAllCombinations();
        combinationsForLiquidComponent.add(new FrozenFlags(false).freezeForContainers());
        return asCartesianProduct(combinationsForLiquidComponent);
    }

    @Test
    public void testInvalidCreateContainerDataSet()
    {
        // Given
        setFrozenFlagsForDataSets(new FrozenFlags(true).freezeForContainers(), dataSet2);
        DataSetCreation dataSetCreation = dataSet(PREFIX + "D2");
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setComponentIds(Arrays.asList(dataSet2));

        // When
        assertUserFailureException(Void -> v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)),
                // Then
                "ERROR: Operation INSERT CONTAINER_COMPONENT is not allowed because data set " + dataSetCreation.getCode()
                        + " or " + DATA_SET_2 + " is frozen.");

    }

    @Test
    public void testInvalidCreateContainerDataSetAfterMelting()
    {
        // Given
        FrozenFlags frozenFlags = new FrozenFlags(true).freezeForContainers();
        setFrozenFlagsForDataSets(frozenFlags, dataSet2);
        setFrozenFlagsForDataSets(frozenFlags.clone().melt(), dataSet2);
        DataSetCreation dataSetCreation = dataSet(PREFIX + "D2");
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setSampleId(sampleId);
        dataSetCreation.setComponentIds(Arrays.asList(dataSet2));

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getComponents().get(0).getCode(), DATA_SET_2);
    }

    @Test
    public void testFreezeContainerAndComponent()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getPermId(), dataSet2);
        DataSetUpdate dataSetUpdate1 = new DataSetUpdate();
        dataSetUpdate1.setDataSetId(dataSet1);
        dataSetUpdate1.freeze();
        dataSetUpdate1.freezeForComponents();
        DataSetUpdate dataSetUpdate2 = new DataSetUpdate();
        dataSetUpdate2.setDataSetId(dataSet2);
        dataSetUpdate2.freeze();
        dataSetUpdate2.freezeForContainers();

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate1, dataSetUpdate2));

        // Then
        assertEquals(getDataSet(dataSet1).isFrozen(), true);
        assertEquals(getDataSet(dataSet2).isFrozen(), true);
    }

    @Test
    public void testFreezeParentAndChild()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getPermId(), dataSet2);
        DataSetUpdate dataSetUpdate1 = new DataSetUpdate();
        dataSetUpdate1.setDataSetId(dataSet1);
        dataSetUpdate1.freeze();
        dataSetUpdate1.freezeForChildren();
        DataSetUpdate dataSetUpdate2 = new DataSetUpdate();
        dataSetUpdate2.setDataSetId(dataSet2);
        dataSetUpdate2.freeze();
        dataSetUpdate2.freezeForParents();

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate1, dataSetUpdate2));

        // Then
        assertEquals(getDataSet(dataSet1).isFrozen(), true);
        assertEquals(getDataSet(dataSet2).isFrozen(), true);
    }

    @Test
    public void testAssertDataSetHasNoDeletedChildren()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionsId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(dataSet2), deletionOptions);
        DeletionSearchCriteria searchCriteria = new DeletionSearchCriteria();
        searchCriteria.withId().thatEquals(deletionsId);
        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        String deletionTimestamp = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(
                v3api.searchDeletions(systemSessionToken, searchCriteria, fetchOptions)
                        .getObjects().get(0).getDeletionDate());
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet1);
        update.freezeForChildren();

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(update)),
                // Then
                "Can not freeze data set " + dataSet1 + " because it has 1 children in the trashcan (1 deletion sets):\n"
                        + "1 children (Deletion timestamp: " + deletionTimestamp + ", reason: test)\n"
                        + "These deletion sets must first be permanently deleted before data set "
                        + dataSet1 + " can be frozen.\n");
    }

    @Test
    public void testAssertDataSetHasNoDeletedParents()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getParentIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionsId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(dataSet2), deletionOptions);
        DeletionSearchCriteria searchCriteria = new DeletionSearchCriteria();
        searchCriteria.withId().thatEquals(deletionsId);
        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        String deletionTimestamp = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(
                v3api.searchDeletions(systemSessionToken, searchCriteria, fetchOptions)
                        .getObjects().get(0).getDeletionDate());
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet1);
        update.freezeForParents();

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(update)),
                // Then
                "Can not freeze data set " + dataSet1 + " because it has 1 parents in the trashcan (1 deletion sets):\n"
                        + "1 parents (Deletion timestamp: " + deletionTimestamp + ", reason: test)\n"
                        + "These deletion sets must first be permanently deleted before data set "
                        + dataSet1 + " can be frozen.\n");
    }

    @Test
    public void testAssertDataSetHasNoDeletedComponents()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionsId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(dataSet2), deletionOptions);
        DeletionSearchCriteria searchCriteria = new DeletionSearchCriteria();
        searchCriteria.withId().thatEquals(deletionsId);
        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        String deletionTimestamp = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(
                v3api.searchDeletions(systemSessionToken, searchCriteria, fetchOptions)
                        .getObjects().get(0).getDeletionDate());
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet1);
        update.freezeForComponents();

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(update)),
                // Then
                "Can not freeze data set " + dataSet1 + " because it has 1 components in the trashcan (1 deletion sets):\n"
                        + "1 components (Deletion timestamp: " + deletionTimestamp + ", reason: test)\n"
                        + "These deletion sets must first be permanently deleted before data set "
                        + dataSet1 + " can be frozen.\n");
    }
}
