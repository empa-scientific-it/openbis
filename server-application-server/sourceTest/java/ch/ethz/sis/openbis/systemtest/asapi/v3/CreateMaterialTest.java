/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;

/**
 * @author pkupczyk
 */
public class CreateMaterialTest extends AbstractSampleTest
{

    @Test
    public void testCreateWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialCreation material = geneCreation("1982");

        List<MaterialPermId> permIds = v3api.createMaterials(sessionToken, Arrays.asList(material));

        assertMaterialsExists(permIds.get(0));
    }

    @Test
    public void testSimpleMaterialCreation()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialCreation m1 = geneCreation("1982");
        MaterialCreation m2 = geneCreation("1984");

        List<MaterialPermId> materialIds = v3api.createMaterials(sessionToken, Arrays.asList(m1, m2));

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        Map<IMaterialId, Material> map = v3api.getMaterials(sessionToken, materialIds, fetchOptions);
        AssertionUtil.assertCollectionSize(map.values(), 2);

        Material material = map.get(new MaterialPermId("1982", "GENE"));
        assertEquals(material.getCode(), "1982");
        assertEquals(material.getPermId().getTypeCode(), "GENE");

        material = map.get(new MaterialPermId("1984", "GENE"));
        assertEquals(material.getCode(), "1984");
        assertEquals(material.getPermId().getTypeCode(), "GENE");
    }

    @Test
    public void testMaterialCreationWithSystemProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setPropertyTypeCode("$PLATE_GEOMETRY");
        assignment.setEntityTypeCode("GENE");
        assignment.setEntityKind(EntityKind.MATERIAL);
        assignment.setOrdinal(1000L);
        commonServer.assignPropertyType(sessionToken, assignment);

        MaterialCreation m1 = geneCreation("1982");
        m1.setProperty("$PLATE_GEOMETRY", "384_WELLS_16X24");

        List<MaterialPermId> materialIds = v3api.createMaterials(sessionToken, Arrays.asList(m1));

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withProperties();
        Map<IMaterialId, Material> map = v3api.getMaterials(sessionToken, materialIds, fetchOptions);
        AssertionUtil.assertCollectionSize(map.values(), 1);

        Material material = map.get(new MaterialPermId("1982", "GENE"));
        assertEquals(material.getCode(), "1982");
        assertEquals(material.getPermId().getTypeCode(), "GENE");

        assertEquals(material.getProperty("$PLATE_GEOMETRY"), "384_WELLS_16X24");
    }

    @Test
    public void testMaterialCreationWithTags()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialCreation m1 = geneCreation("NEW_GENE_WITH_TAGS");

        TagCode test123 = new TagCode("TEST_123");
        TagCode testMetaprojects = new TagCode("TEST_METAPROJECTS");
        m1.setTagIds(Arrays.asList(test123, testMetaprojects));

        List<MaterialPermId> materialIds = v3api.createMaterials(sessionToken, Arrays.asList(m1));

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withTags();
        Map<IMaterialId, Material> map = v3api.getMaterials(sessionToken, materialIds, fetchOptions);
        AssertionUtil.assertCollectionSize(map.values(), 1);

        Material material = map.get(new MaterialPermId("NEW_GENE_WITH_TAGS", "GENE"));

        Set<Tag> tags = material.getTags();
        AssertionUtil.assertSize(tags, 2);
        ArrayList<String> tagCodes = new ArrayList<String>();
        for (Tag tag : tags)
        {
            tagCodes.add(tag.getCode());
        }
        AssertionUtil.assertCollectionContainsOnly(tagCodes, "TEST_123", "TEST_METAPROJECTS");

    }

    // @Test broken
    public void testCreateTwoMaterialsWithMaterialLinks()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialPermId m1id = new MaterialPermId("FIRST", "SELF_REF");
        MaterialCreation m1 = materialCreation(m1id);
        HashMap<String, Serializable> properties1 = new HashMap<String, Serializable>();
        properties1.put("ANY_MATERIAL", "SECOND (SELF_REF)");
        properties1.put("DESCRIPTION", "mandatory material decsription");
        m1.setProperties(properties1);

        MaterialPermId m2id = new MaterialPermId("SECOND", "SELF_REF");
        MaterialCreation m2 = materialCreation(m2id);
        HashMap<String, Serializable> properties2 = new HashMap<String, Serializable>();
        properties2.put("ANY_MATERIAL", "FIRST (SELF_REF)");
        properties2.put("DESCRIPTION", "mandatory material decsription");
        m2.setProperties(properties2);

        List<MaterialPermId> materialIds = v3api.createMaterials(sessionToken, Arrays.asList(m1, m2));

        AssertionUtil.assertCollectionContainsOnly(materialIds, m1id, m2id);

        // circular materialProperties
        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withMaterialPropertiesUsing(fetchOptions);

        Map<IMaterialId, Material> map = v3api.getMaterials(sessionToken, Arrays.asList(m1id), fetchOptions);

        AssertionUtil.assertCollectionSize(map.values(), 1);

        Material resultm1 = map.get(m1id);

        assertEquals(resultm1.getPermId(), m1id);
        Material resultm2 = resultm1.getMaterialProperties().get("ANY_MATERIAL");
        assertEquals(resultm2.getPermId(), m2id);
        Material resultm3 = resultm2.getMaterialProperties().get("ANY_MATERIAL");
        assertEquals(resultm1, resultm3);
    }

    @Test
    public void testCreateWithCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final MaterialCreation materialCreation = geneCreation(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createMaterials(sessionToken, Arrays.asList(materialCreation));
                }
            }, "Code cannot be empty");
    }

    @Test
    public void testCreateWithTypeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final MaterialCreation materialCreation = geneCreation("GENE_NOT_OK");
        materialCreation.setTypeId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createMaterials(sessionToken, Arrays.asList(materialCreation));
                }
            }, "Type id cannot be null");
    }

    @Test
    public void testCreateWithNoMandatoryProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final MaterialCreation materialCreation = geneCreation("GENE_NOT_OK");
        materialCreation.getProperties().clear();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createMaterials(sessionToken, Arrays.asList(materialCreation));
                }
            }, "Value of mandatory property 'GENE_SYMBOL' not specified");
    }

    @Test
    public void testCreateWithNonExistingProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final MaterialCreation materialCreation = geneCreation("GENE_NOT_OK");
        materialCreation.getProperties().put("CODE_THAT_DOESNT_EXIST", "value");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createMaterials(sessionToken, Arrays.asList(materialCreation));
                }
            }, "Property type with code 'CODE_THAT_DOESNT_EXIST' does not exist");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialCreation creation = new MaterialCreation();
        creation.setCode("LOG_TEST_1");
        creation.setTypeId(new EntityTypePermId("GENE"));
        creation.setDescription("test description 1");
        creation.setProperty("GENE_SYMBOL", "s1");

        MaterialCreation creation2 = new MaterialCreation();
        creation2.setCode("LOG_TEST_2");
        creation2.setTypeId(new EntityTypePermId("GENE"));
        creation2.setDescription("test description 2");
        creation2.setProperty("GENE_SYMBOL", "s2");

        v3api.createMaterials(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-materials  NEW_MATERIALS('[MaterialCreation[code=LOG_TEST_1], MaterialCreation[code=LOG_TEST_2]]')");
    }

    private MaterialCreation materialCreation(MaterialPermId permId)
    {
        String code = permId.getCode();
        String type = permId.getTypeCode();
        MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode(code);
        materialCreation.setTypeId(new EntityTypePermId(type));
        materialCreation.setCreationId(new CreationId("creation " + code));
        materialCreation.setDescription("Material with code " + code);
        return materialCreation;
    }

    private MaterialCreation geneCreation(String code)
    {
        MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode(code);
        materialCreation.setTypeId(new EntityTypePermId("GENE"));
        materialCreation.setCreationId(new CreationId("creation " + code));
        materialCreation.setDescription("Material with code " + code);

        HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put("GENE_SYMBOL", "SYMBOL " + code);

        materialCreation.setProperties(properties);

        return materialCreation;
    }
}
