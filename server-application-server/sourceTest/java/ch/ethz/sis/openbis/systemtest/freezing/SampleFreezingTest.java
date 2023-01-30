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
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.AttachmentFileName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * @author Franz-Josef Elmer
 */
public class SampleFreezingTest extends FreezingTest
{
    private static final String PREFIX = "SFT-";

    private static final String SAMPLE_1 = PREFIX + "1";

    private static final String SAMPLE_COMP = PREFIX + "COMP";

    private static final String SAMPLE_CHILD = PREFIX + "CHILD";

    private static final String SAMPLE_PARENT_CONT = PREFIX + "PARENT_CONT";

    private SamplePermId sample1;

    private SamplePermId sampleComp;

    private SamplePermId sampleChild;

    private SamplePermId sampleParentCont;

    private TagPermId blueTag;

    @BeforeMethod
    public void createSampleExamples()
    {
        SampleCreation s1 = cellPlate(SAMPLE_1);
        s1.setProperty("SIZE", "1042");
        s1.setAttachments(Arrays.asList(attachment("f1.txt", "T1", "my t1", "abcdefgh")));
        SampleCreation s2 = cellPlate(SAMPLE_COMP);
        SampleCreation s3 = cellPlate(SAMPLE_CHILD);
        SampleCreation s4 = cellPlate(SAMPLE_PARENT_CONT);
        s4.setCreationId(new CreationId("s4"));
        s2.setContainerId(s4.getCreationId());
        s3.setParentIds(Arrays.asList(s4.getCreationId()));

        List<SamplePermId> sampleIds = v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2, s3, s4));
        sample1 = sampleIds.get(0);
        sampleComp = sampleIds.get(1);
        sampleChild = sampleIds.get(2);
        sampleParentCont = sampleIds.get(3);

        TagCreation tagCreation = new TagCreation();
        tagCreation.setCode("blue");
        tagCreation.setSampleIds(Arrays.asList(sampleComp));
        blueTag = v3api.createTags(systemSessionToken, Arrays.asList(tagCreation)).get(0);
    }

    @Test
    public void testTrash()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteSamples(systemSessionToken, Arrays.asList(sample1), deletionOptions),
                // Then
                "ERROR: Operation TRASH is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testDeletePermanently()
    {
        // Given
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(sample1), deletionOptions);
        setFrozenFlagForSamples(true, sample1);

        // When
        assertUserFailureException(Void -> v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId)),
                // Then
                "ERROR: Operation DELETE PROPERTY is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testDeletePermanentlySampleWithoutPropertiesAndAttachments()
    {
        // Given
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(sampleComp), deletionOptions);
        setFrozenFlagForSamples(true, sampleComp);

        // When
        assertUserFailureException(Void -> v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId)),
                // Then
                "ERROR: Operation DELETE is not allowed because sample " + SAMPLE_COMP + " is frozen.");
    }

    @Test
    public void testAddTag()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getSample(sample1).getTags().toString(), "[]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getTagIds().add(blueTag);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getTags().toString(), "[Tag blue]");
    }

    @Test
    public void testRemoveTag()
    {
        // Given
        setFrozenFlagForSamples(true, sampleComp);
        assertEquals(getSample(sampleComp).getTags().toString(), "[Tag blue]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.getTagIds().remove(blueTag);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleComp).getTags().toString(), "[]");
    }

    @Test
    public void testAddProperty()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("COMMENT", "my comment");

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation INSERT PROPERTY is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testAddPropertyForMoltenSample() throws Exception
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        setFrozenFlagForSamples(false, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("COMMENT", "my comment");

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getProperty("COMMENT"), "my comment");
    }

    @Test
    public void testChangeProperty()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("SIZE", "1043");

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation UPDATE PROPERTY is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testChangePropertyForMoltenSample()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        setFrozenFlagForSamples(false, sample1);
        assertEquals(getSample(sample1).getProperty("SIZE"), "1042");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("SIZE", "1043");

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getProperty("SIZE"), "1043");
    }

    @Test
    public void testDeleteProperty()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("SIZE", null);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation DELETE PROPERTY is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testDeletePropertyForMoltenSample()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        setFrozenFlagForSamples(false, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("SIZE", null);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getProperty("SIZE"), null);
    }

    @Test
    public void testAddAttachment()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getAttachments().add(attachment("f2.txt", "F2", "my f2", "3.14159"));

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation INSERT ATTACHMENT is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testAddAttachmentForMoltenSample()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        setFrozenFlagForSamples(false, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getAttachments().add(attachment("f2.txt", "F2", "my f2", "3.14159"));

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getAttachments().size(), 2);
    }

    @Test
    public void testDeleteAttachment()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getAttachments().remove(new AttachmentFileName("f1.txt"));

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation DELETE ATTACHMENT is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testDeleteAttachmentForMoltenSample()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        setFrozenFlagForSamples(false, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getAttachments().remove(new AttachmentFileName("f1.txt"));

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getAttachments().size(), 0);
    }

    @Test(dataProvider = "LiquidChildParentRelations")
    public void testValidAddChildToParent(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfParent, sample1);
        setFrozenFlagsForSamples(frozenFlagsOfChild, sampleChild);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getChildIds().add(sampleChild);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getChildren().get(0).getCode(), SAMPLE_CHILD);
    }

    @Test(dataProvider = "LiquidChildParentRelations")
    public void testValidRemoveChildFromParent(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getChildIds().add(sampleChild);
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));
        assertEquals(getSample(sampleParentCont).getChildren().get(0).getCode(), SAMPLE_CHILD);
        setFrozenFlagsForSamples(frozenFlagsOfParent, sampleParentCont);
        setFrozenFlagsForSamples(frozenFlagsOfChild, sampleChild);
        SampleUpdate sampleUpdate2 = new SampleUpdate();
        sampleUpdate2.setSampleId(sampleParentCont);
        sampleUpdate2.getChildIds().remove(sampleChild);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate2));

        // Then
        assertEquals(getSample(sampleParentCont).getChildren().toString(), "[]");
    }

    @DataProvider(name = "LiquidChildParentRelations")
    public static Object[][] liquidChildParentRelations()
    {
        List<FrozenFlags> combinationsForParent =
                new FrozenFlags(true).freezeForComponents().freezeForDataSet().freezeForParents().createAllCombinations();
        combinationsForParent.add(new FrozenFlags(false).freezeForChildren());
        List<FrozenFlags> combinationsForChild =
                new FrozenFlags(true).freezeForComponents().freezeForDataSet().freezeForChildren().createAllCombinations();
        combinationsForChild.add(new FrozenFlags(false).freezeForParents());
        return asCartesianProduct(combinationsForParent, combinationsForChild);
    }

    @Test(dataProvider = "FrozenChildParentRelations")
    public void testInvalidAddChildToParent(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfParent, sample1);
        setFrozenFlagsForSamples(frozenFlagsOfChild, sampleChild);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getChildIds().add(sampleChild);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation INSERT is not allowed because sample " + SAMPLE_1
                        + " or " + SAMPLE_CHILD + " is frozen.");
    }

    @Test(dataProvider = "FrozenChildParentRelations")
    public void testInvalidAddChildToParentAfterMelting(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfParent, sample1);
        setFrozenFlagsForSamples(frozenFlagsOfChild, sampleChild);
        setFrozenFlagsForSamples(frozenFlagsOfParent.clone().melt(), sample1);
        setFrozenFlagsForSamples(frozenFlagsOfChild.clone().melt(), sampleChild);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getChildIds().add(sampleChild);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getChildren().get(0).getCode(), SAMPLE_CHILD);
    }

    @Test(dataProvider = "FrozenChildParentRelations")
    public void testInvalidRemoveChildFromParent(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getChildIds().add(sampleChild);
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));
        assertEquals(getSample(sampleParentCont).getChildren().get(0).getCode(), SAMPLE_CHILD);
        setFrozenFlagsForSamples(frozenFlagsOfParent, sampleParentCont);
        setFrozenFlagsForSamples(frozenFlagsOfChild, sampleChild);
        SampleUpdate sampleUpdate2 = new SampleUpdate();
        sampleUpdate2.setSampleId(sampleParentCont);
        sampleUpdate2.getChildIds().remove(sampleChild);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate2)),
                // Then Operation % is not allowed because sample % or % is frozen.
                "ERROR: Operation DELETE is not allowed because sample " + SAMPLE_PARENT_CONT
                        + " or " + SAMPLE_CHILD + " is frozen.");
    }

    @Test(dataProvider = "FrozenChildParentRelations")
    public void testInvalidRemoveChildFromParentAfterMelting(FrozenFlags frozenFlagsOfParent, FrozenFlags frozenFlagsOfChild)
    {
        // Given
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getChildIds().add(sampleChild);
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));
        assertEquals(getSample(sampleParentCont).getChildren().get(0).getCode(), SAMPLE_CHILD);
        setFrozenFlagsForSamples(frozenFlagsOfParent, sampleParentCont);
        setFrozenFlagsForSamples(frozenFlagsOfChild, sampleChild);
        setFrozenFlagsForSamples(frozenFlagsOfParent.clone().melt(), sampleParentCont);
        setFrozenFlagsForSamples(frozenFlagsOfChild.clone().melt(), sampleChild);
        SampleUpdate sampleUpdate2 = new SampleUpdate();
        sampleUpdate2.setSampleId(sampleParentCont);
        sampleUpdate2.getChildIds().remove(sampleChild);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate2));

        // Then
        assertEquals(getSample(sampleParentCont).getChildren().toString(), "[]");
    }

    @DataProvider(name = "FrozenChildParentRelations")
    public static Object[][] frozenChildParentRelations()
    {
        List<FrozenFlags> combinationsForLiquidParent =
                new FrozenFlags(true).freezeForComponents().freezeForDataSet().freezeForParents().createAllCombinations();
        FrozenFlags childFrozenForParents = new FrozenFlags(true).freezeForParents();
        Object[][] combinationForInvalidChild =
                asCartesianProduct(combinationsForLiquidParent, Arrays.asList(childFrozenForParents));
        List<FrozenFlags> combinationsForLiquidChild =
                new FrozenFlags(true).freezeForComponents().freezeForDataSet().freezeForChildren().createAllCombinations();
        FrozenFlags parentFrozenForChildren = new FrozenFlags(true).freezeForChildren();
        Object[][] combinationForInvalidParent =
                asCartesianProduct(Arrays.asList(parentFrozenForChildren), combinationsForLiquidChild);
        return merge(asCartesianProduct(Arrays.asList(parentFrozenForChildren), Arrays.asList(childFrozenForParents)),
                combinationForInvalidParent, combinationForInvalidChild);
    }

    @Test(dataProvider = "LiquidComponentContainerRelations")
    public void testValidAddComponentToContainer(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfContainer, sampleComp);
        setFrozenFlagsForSamples(frozenFlagsOfComponent, sample1);
        assertEquals(getSample(sampleComp).getComponents().toString(), "[]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.getComponentIds().add(sample1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleComp).getComponents().get(0).getCode(), SAMPLE_1);
    }

    @Test(dataProvider = "LiquidComponentContainerRelations")
    public void testValidRemoveComponentFromContainer(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfContainer, sampleParentCont);
        setFrozenFlagsForSamples(frozenFlagsOfComponent, sampleComp);
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[Sample " + sampleComp + "]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getComponentIds().remove(sampleComp);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[]");
    }

    @DataProvider(name = "LiquidComponentContainerRelations")
    public static Object[][] liquidComponentContainerRelations()
    {
        List<FrozenFlags> combinationsForLiquidContainer =
                new FrozenFlags(true).freezeForChildren().freezeForDataSet().freezeForParents().createAllCombinations();
        combinationsForLiquidContainer.add(new FrozenFlags(false).freezeForComponents());
        List<FrozenFlags> combinationsForLiquidComponent =
                new FrozenFlags(true).freezeForComponents().freezeForDataSet().freezeForChildren().freezeForParents().createAllCombinations();
        return asCartesianProduct(combinationsForLiquidContainer, combinationsForLiquidComponent);
    }

    @Test(dataProvider = "frozenComponentContainerRelations")
    public void testInvalidAddComponentToContainer(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfContainer, sampleComp);
        setFrozenFlagsForSamples(frozenFlagsOfComponent, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.getComponentIds().add(sample1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET CONTAINER is not allowed because sample " + SAMPLE_COMP + " is frozen for sample " + SAMPLE_1 + ".");
    }

    @Test(dataProvider = "frozenComponentContainerRelations")
    public void testInvalidAddComponentToContainerAfterMelting(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfContainer, sampleComp);
        setFrozenFlagsForSamples(frozenFlagsOfComponent, sample1);
        setFrozenFlagsForSamples(frozenFlagsOfContainer.clone().melt(), sampleComp);
        setFrozenFlagsForSamples(frozenFlagsOfComponent.clone().melt(), sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.getComponentIds().add(sample1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleComp).getComponents().get(0).getCode(), SAMPLE_1);
    }

    @Test(dataProvider = "frozenComponentContainerRelations")
    public void testInvalidRemoveComponentFromContainer(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfContainer, sampleParentCont);
        setFrozenFlagsForSamples(frozenFlagsOfComponent, sampleComp);
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[Sample " + sampleComp + "]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getComponentIds().remove(sampleComp);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation REMOVE CONTAINER is not allowed because sample " + SAMPLE_PARENT_CONT + " is frozen for sample " + SAMPLE_COMP
                        + ".");
    }

    @Test(dataProvider = "frozenComponentContainerRelations")
    public void testInvalidRemoveComponentFromContainerAfterMelting(FrozenFlags frozenFlagsOfContainer, FrozenFlags frozenFlagsOfComponent)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfContainer, sampleParentCont);
        setFrozenFlagsForSamples(frozenFlagsOfComponent, sampleComp);
        setFrozenFlagsForSamples(frozenFlagsOfContainer.clone().melt(), sampleParentCont);
        setFrozenFlagsForSamples(frozenFlagsOfComponent.clone().melt(), sampleComp);
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[Sample " + sampleComp + "]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getComponentIds().remove(sampleComp);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[]");
    }

    @DataProvider(name = "frozenComponentContainerRelations")
    public static Object[][] frozenComponentContainerRelations()
    {
        List<FrozenFlags> frozenContainer = Arrays.asList(new FrozenFlags(true).freezeForComponents());
        List<FrozenFlags> combinationsForLiquidComponent =
                new FrozenFlags(true).freezeForComponents().freezeForDataSet().freezeForChildren().freezeForParents().createAllCombinations();
        return asCartesianProduct(frozenContainer, combinationsForLiquidComponent);
    }

    @Test(dataProvider = "liquidContainer")
    public void testValidAddSampleToContainer(FrozenFlags frozenFlagsForContainer)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsForContainer, sample1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setContainerId(sample1);

        // When
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).iterator().next();

        // Then
        assertEquals(getSample(id).getContainer().getCode(), SAMPLE_1);
    }

    @DataProvider(name = "liquidContainer")
    public static Object[][] liquidContainer()
    {
        List<FrozenFlags> combinationsForLiquidContainer = new FrozenFlags(true).freezeForDataSet()
                .freezeForChildren().freezeForParents().createAllCombinations();
        combinationsForLiquidContainer.add(new FrozenFlags(false).freezeForComponents());
        return asCartesianProduct(combinationsForLiquidContainer);
    }

    @Test
    public void testInvalidAddSampleToContainer()
    {
        // Given
        setFrozenFlagsForSamples(new FrozenFlags(true).freezeForComponents(), sample1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setContainerId(sample1);

        // When
        assertUserFailureException(Void -> v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)),
                // Then
                "ERROR: Operation SET CONTAINER is not allowed because sample " + SAMPLE_1 + " is frozen for sample "
                        + sampleCreation.getCode() + ".");
    }

    @Test
    public void testInvalidAddSampleToContainerAfterMelting()
    {
        // Given
        setFrozenFlagsForSamples(new FrozenFlags(true).freezeForComponents(), sample1);
        setFrozenFlagsForSamples(new FrozenFlags(true).freezeForComponents().melt(), sample1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setContainerId(sample1);

        // When
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).iterator().next();

        // Then
        assertEquals(getSample(id).getContainer().getCode(), SAMPLE_1);
    }

    @Test(dataProvider = "liquidParent")
    public void testValidAddSampleToParent(FrozenFlags frozenFlagsForParent)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsForParent, sample1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setParentIds(Arrays.asList(sample1));

        // When
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).iterator().next();

        // Then
        assertEquals(getSample(id).getParents().get(0).getCode(), SAMPLE_1);
    }

    @DataProvider(name = "liquidParent")
    public static Object[][] liquidParent()
    {
        List<FrozenFlags> combinationsForLiquidParent = new FrozenFlags(true).freezeForDataSet()
                .freezeForComponents().freezeForParents().createAllCombinations();
        combinationsForLiquidParent.add(new FrozenFlags(false).freezeForChildren());
        return asCartesianProduct(combinationsForLiquidParent);
    }

    @Test
    public void testInvalidAddSampleToParent()
    {
        // Given
        setFrozenFlagsForSamples(new FrozenFlags(true).freezeForChildren(), sample1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setParentIds(Arrays.asList(sample1));

        // When
        assertUserFailureException(Void -> v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)),
                // Then
                "ERROR: Operation INSERT is not allowed because sample " + SAMPLE_1
                        + " or " + sampleCreation.getCode() + " is frozen.");
    }

    @Test
    public void testInvalidAddSampleToParentAfterMelting()
    {
        // Given
        setFrozenFlagsForSamples(new FrozenFlags(true).freezeForChildren(), sample1);
        setFrozenFlagsForSamples(new FrozenFlags(true).freezeForChildren().melt(), sample1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setParentIds(Arrays.asList(sample1));

        // When
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).iterator().next();

        // Then
        assertEquals(getSample(id).getParents().get(0).getCode(), SAMPLE_1);
    }

    @Test(dataProvider = "liquidChild")
    public void testValidAddSampleToChild(FrozenFlags frozenFlagsForChild)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsForChild, sample1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setChildIds(Arrays.asList(sample1));

        // When
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).iterator().next();

        // Then
        assertEquals(getSample(id).getChildren().get(0).getCode(), SAMPLE_1);
    }

    @DataProvider(name = "liquidChild")
    public static Object[][] liquidChild()
    {
        List<FrozenFlags> combinationsForLiquidChild = new FrozenFlags(true).freezeForDataSet()
                .freezeForComponents().freezeForChildren().createAllCombinations();
        combinationsForLiquidChild.add(new FrozenFlags(false).freezeForChildren());
        return asCartesianProduct(combinationsForLiquidChild);
    }

    @Test
    public void testInvalidAddSampleToChild()
    {
        // Given
        setFrozenFlagsForSamples(new FrozenFlags(true).freezeForParents(), sample1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setChildIds(Arrays.asList(sample1));

        // When
        assertUserFailureException(Void -> v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)),
                // Then
                "ERROR: Operation INSERT is not allowed because sample " + sampleCreation.getCode()
                        + " or " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testInvalidAddSampleToChildAfterMelting()
    {
        // Given
        setFrozenFlagsForSamples(new FrozenFlags(true).freezeForParents(), sample1);
        setFrozenFlagsForSamples(new FrozenFlags(true).freezeForParents().melt(), sample1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setChildIds(Arrays.asList(sample1));

        // When
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).iterator().next();

        // Then
        assertEquals(getSample(id).getChildren().get(0).getCode(), SAMPLE_1);
    }

    @Test
    public void testFreezeChildAndParent()
    {
        // Given
        SampleUpdate update1 = new SampleUpdate();
        update1.setSampleId(sampleChild);
        update1.freeze();
        update1.freezeForParents();
        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(sampleParentCont);
        update2.freeze();
        update2.freezeForChildren();

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(update1, update2));

        // Then
        assertEquals(getSample(sampleChild).isFrozen(), true);
        assertEquals(getSample(sampleParentCont).isFrozen(), true);
    }

    @Test
    public void testFreezeComponentAndContainer()
    {
        // Given
        SampleUpdate update1 = new SampleUpdate();
        update1.setSampleId(sampleComp);
        update1.freeze();
        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(sampleParentCont);
        update2.freeze();
        update2.freezeForComponents();

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(update1, update2));

        // Then
        assertEquals(getSample(sampleComp).isFrozen(), true);
        assertEquals(getSample(sampleParentCont).isFrozen(), true);
    }

    @Test
    public void testAssertSampleHasNoDeletedChildren()
    {
        // Given
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionsId = v3api.deleteSamples(systemSessionToken, Arrays.asList(sampleChild), deletionOptions);
        DeletionSearchCriteria searchCriteria = new DeletionSearchCriteria();
        searchCriteria.withId().thatEquals(deletionsId);
        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        String deletionTimestamp = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(
                v3api.searchDeletions(systemSessionToken, searchCriteria, fetchOptions)
                        .getObjects().get(0).getDeletionDate());
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleParentCont);
        update.freezeForChildren();

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(update)),
                // Then
                "Can not freeze object " + sampleParentCont + " because it has 1 children in the trashcan (1 deletion sets):\n"
                        + "1 children (Deletion timestamp: " + deletionTimestamp + ", reason: test)\n"
                        + "These deletion sets must first be permanently deleted before object "
                        + sampleParentCont + " can be frozen.\n");
    }

    @Test
    public void testAssertSampleHasNoDeletedParents()
    {
        // Given
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionsId = v3api.deleteSamples(systemSessionToken, Arrays.asList(sampleParentCont), deletionOptions);
        DeletionSearchCriteria searchCriteria = new DeletionSearchCriteria();
        searchCriteria.withId().thatEquals(deletionsId);
        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        String deletionTimestamp = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(
                v3api.searchDeletions(systemSessionToken, searchCriteria, fetchOptions)
                        .getObjects().get(0).getDeletionDate());
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleChild);
        update.freezeForParents();

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(update)),
                // Then
                "Can not freeze object " + sampleChild + " because it has 1 parents in the trashcan (1 deletion sets):\n"
                        + "1 parents (Deletion timestamp: " + deletionTimestamp + ", reason: test)\n"
                        + "These deletion sets must first be permanently deleted before object "
                        + sampleChild + " can be frozen.\n");
    }

    @Test
    public void testAssertSampleHasNoDeletedComponents()
    {
        // Given
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionsId = v3api.deleteSamples(systemSessionToken, Arrays.asList(sampleComp), deletionOptions);
        DeletionSearchCriteria searchCriteria = new DeletionSearchCriteria();
        searchCriteria.withId().thatEquals(deletionsId);
        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        String deletionTimestamp = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(
                v3api.searchDeletions(systemSessionToken, searchCriteria, fetchOptions)
                        .getObjects().get(0).getDeletionDate());
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleParentCont);
        update.freezeForComponents();

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(update)),
                // Then
                "Can not freeze object " + sampleParentCont + " because it has 1 components in the trashcan (1 deletion sets):\n"
                        + "1 components (Deletion timestamp: " + deletionTimestamp + ", reason: test)\n"
                        + "These deletion sets must first be permanently deleted before object "
                        + sampleParentCont + " can be frozen.\n");
    }

    @Test
    public void testAssertSampleHasNoDeletedDataSets()
    {
        // Given
        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setCode("DS-" + System.currentTimeMillis());
        dataSetCreation.setSampleId(sample1);
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setTypeId(new EntityTypePermId("DELETION_TEST_CONTAINER", EntityKind.DATA_SET));
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        List<DataSetPermId> dataSetIds = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation));
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionsId = v3api.deleteDataSets(systemSessionToken, dataSetIds, deletionOptions);
        DeletionSearchCriteria searchCriteria = new DeletionSearchCriteria();
        searchCriteria.withId().thatEquals(deletionsId);
        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        String deletionTimestamp = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(
                v3api.searchDeletions(systemSessionToken, searchCriteria, fetchOptions)
                        .getObjects().get(0).getDeletionDate());
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample1);
        update.freezeForDataSets();

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(update)),
                // Then
                "Can not freeze object " + sample1 + " because it has 1 data sets in the trashcan (1 deletion sets):\n"
                        + "1 data sets (Deletion timestamp: " + deletionTimestamp + ", reason: test)\n"
                        + "These deletion sets must first be permanently deleted before object "
                        + sample1 + " can be frozen.\n");
    }
}
