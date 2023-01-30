/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class UpdateMaterialTest extends AbstractSampleTest
{

    @Test
    public void testUpdateWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialUpdate update = new MaterialUpdate();
        update.setMaterialId(new MaterialPermId("VIRUS2", "VIRUS"));
        update.setProperty("DESCRIPTION", "an updated description");

        v3api.updateMaterials(sessionToken, Arrays.asList(update));

        assertMaterialsExists(new MaterialPermId("VIRUS2", "VIRUS"));
    }

    @Test
    public void testSimpleMaterialUpdate()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialUpdate update = new MaterialUpdate();
        update.setMaterialId(new MaterialPermId("1", "GENE"));
        String description = "This is amodified gene description";
        update.setProperty("DESCRIPTION", description);

        v3api.updateMaterials(sessionToken, Arrays.asList(update));

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withProperties();

        Map<IMaterialId, Material> map = v3api.getMaterials(sessionToken, Arrays.asList(update.getMaterialId()), fetchOptions);

        Material material = map.get(update.getMaterialId());
        assertEquals(material.getProperties().get("DESCRIPTION"), description);
    }

    @Test
    public void testWithMaterialProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withMaterialPropertiesUsing(fetchOptions);

        MaterialPermId selfParentId = new MaterialPermId("SRM_1", "SELF_REF");
        MaterialPermId selfChildId = new MaterialPermId("SRM_1A", "SELF_REF");

        Map<IMaterialId, Material> map = v3api.getMaterials(sessionToken, Arrays.asList(selfChildId, selfParentId), fetchOptions);

        Material parent = map.get(selfParentId);
        Material child = map.get(selfChildId);

        Map<String, Material> materialProperties = parent.getMaterialProperties();

        Material childFromProperties = materialProperties.get("ANY_MATERIAL");

        assertEquals(selfChildId, childFromProperties.getPermId());
        assertEquals(true, child == childFromProperties);
    }

    @Test
    public void testUpdateMaterialProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialUpdate update = new MaterialUpdate();
        update.setMaterialId(new MaterialPermId("SRM_1", "SELF_REF"));
        update.setProperty("ANY_MATERIAL", "1 (GENE)");

        v3api.updateMaterials(sessionToken, Arrays.asList(update));

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withMaterialProperties().withType();

        Map<IMaterialId, Material> map = v3api.getMaterials(sessionToken, Arrays.asList(update.getMaterialId()), fetchOptions);

        Material material = map.get(update.getMaterialId());
        assertEquals(material.getProperties().get("ANY_MATERIAL"), "1 (GENE)");
        Material expectedGene = material.getMaterialProperties().get("ANY_MATERIAL");
        assertEquals(expectedGene.getCode(), "1");
        assertEquals(expectedGene.getType().getCode(), "GENE");
    }

    @Test
    public void testUpdateTag()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialUpdate update = new MaterialUpdate();
        update.setMaterialId(new MaterialPermId("SRM_1", "SELF_REF"));

        TagCode test123 = new TagCode("TEST_123");
        TagCode testMetaprojects = new TagCode("TEST_METAPROJECTS");
        ListUpdateAction<ITagId> addAction = new ListUpdateActionAdd<ITagId>();
        addAction.setItems(Arrays.asList(test123, testMetaprojects));
        update.setTagActions(Collections.singletonList(addAction));

        v3api.updateMaterials(sessionToken, Arrays.asList(update));

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withTags();

        Map<IMaterialId, Material> map = v3api.getMaterials(sessionToken, Arrays.asList(update.getMaterialId()), fetchOptions);

        Material material = map.get(update.getMaterialId());
        Set<Tag> tags = material.getTags();
        AssertionUtil.assertSize(tags, 2);
        ArrayList<String> tagCodes = new ArrayList<String>();
        for (Tag tag : tags)
        {
            tagCodes.add(tag.getCode());
        }
        AssertionUtil.assertCollectionContainsOnly(tagCodes, "TEST_123", "TEST_METAPROJECTS");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialUpdate update = new MaterialUpdate();
        update.setMaterialId(new MaterialPermId("VIRUS2", "VIRUS"));

        MaterialUpdate update2 = new MaterialUpdate();
        update2.setMaterialId(new MaterialPermId("BACTERIUM1", "BACTERIUM"));

        v3api.updateMaterials(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-materials  MATERIAL_UPDATES('[MaterialUpdate[materialId=VIRUS2 (VIRUS)], MaterialUpdate[materialId=BACTERIUM1 (BACTERIUM)]]')");
    }

}
