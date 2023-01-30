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

package ch.systemsx.cisd.openbis.uitest.suite.headless;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.rmi.Identifiers;
import ch.systemsx.cisd.openbis.uitest.type.Material;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;

/**
 * @author anttil
 */
public class MaterialsContainMetaProjectInformation extends HeadlessSuite
{

    @Test
    public void materialSearchReturnsMetaProjectInformation() throws Exception
    {
        Material material = create(aMaterial());
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, material);

        List<Material> searchResult = searchFor(materials().withCode(material.getCode()));

        assertThat(searchResult, containsExactly(material));
        assertThat(metaProjectsOf(searchResult.get(0)), containExactly(metaProject));
    }

    @Test
    public void materialListingContainsMetaProjectInformation() throws Exception
    {
        Material material = create(aMaterial());
        MetaProject metaProject = create(aMetaProject());
        tagWith(metaProject, material);

        List<Material> listResult = listMaterials(Identifiers.get(material));

        assertThat(listResult, containsExactly(material));
        assertThat(metaProjectsOf(listResult.get(0)), containExactly(metaProject));
    }
}
