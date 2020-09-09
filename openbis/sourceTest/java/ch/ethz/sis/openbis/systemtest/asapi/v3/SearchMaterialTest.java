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

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class SearchMaterialTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_USER, new MaterialSearchCriteria(), 3734);
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        MaterialPermId permId = new MaterialPermId("VIRUS1", "VIRUS");
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withId().thatEquals(permId);
        testSearch(TEST_USER, criteria, permId);
    }

    @Test
    public void testSearchWithIdSetToPermIdSortByPermId()
    {
        MaterialPermId permId = new MaterialPermId("VIRUS1", "VIRUS");
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withId().thatEquals(permId);
        MaterialFetchOptions options = new MaterialFetchOptions();
        options.sortBy().permId();
        testSearch(TEST_USER, criteria, options, permId);
    }

    @Test
    public void testSearchWithPermId()
    {
        final MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withPermId().thatEquals("NOT SUPPORTED YET");
        assertRuntimeException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testSearch(TEST_USER, criteria);
                }
            }, "Please use criteria.withId().thatEquals(new MaterialPermId('CODE','TYPE')) instead.");
    }

    @Test
    public void testSearchWithCode()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withCode().thatStartsWith("VIRUS");
        testSearch(TEST_USER, criteria, new MaterialPermId("VIRUS1", "VIRUS"), new MaterialPermId("VIRUS2", "VIRUS"));
    }

    @Test
    public void testSearchWithCodes()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("VIRUS2", "VIRUS1"));
        testSearch(TEST_USER, criteria, new MaterialPermId("VIRUS1", "VIRUS"), new MaterialPermId("VIRUS2", "VIRUS"));
    }

    @Test
    public void testSearchWithTypeWithIdSetToPermId()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withType().withId().thatEquals(new EntityTypePermId("BACTERIUM"));
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"),
                new MaterialPermId("BACTERIUM-X", "BACTERIUM"), new MaterialPermId("BACTERIUM-Y", "BACTERIUM"));
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withType().withPermId().thatEquals("BACTERIUM");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"),
                new MaterialPermId("BACTERIUM-X", "BACTERIUM"), new MaterialPermId("BACTERIUM-Y", "BACTERIUM"));
    }

    @Test
    public void testSearchWithPropertyThatEquals()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("adenovirus 5");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("adenovirus");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("adenoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("denovirus");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithPropertyThatStartsWith()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("adenovirus");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("adenoviru");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("denoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("denovirus");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithPropertyThatEndsWith()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("adenovirus 3");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("adenoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("denoviru");
        testSearch(TEST_USER, criteria, 0);

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("denovirus 5");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD5", "VIRUS"));
    }

    @Test
    public void testSearchWithPropertyThatContains()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("adenovirus");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));

        criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("denoviru");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));
    }

    @Test
    public void testSearchWithProperty()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withProperty("DESCRIPTION");
        testSearch(TEST_USER, criteria, 40);
    }

    @Test
    public void testSearchWithDateProperty()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatEquals("2007-07-17");
        testSearch(TEST_USER, criteria, new MaterialPermId("NEUTRAL", "CONTROL"), new MaterialPermId("C-NO-SEC", "CONTROL"), new MaterialPermId(
                "INHIBITOR", "CONTROL"));
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withAnyProperty().thatEquals("HUHU");
        testSearch(TEST_USER, criteria, new MaterialPermId("MYGENE1", "GENE"));

        criteria = new MaterialSearchCriteria();
        criteria.withAnyProperty().thatEquals("HUH");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithAnyField()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withAnyField().thatEquals("/VIRUS1");
        testSearch(TEST_USER, criteria, new MaterialPermId("VIRUS1", "VIRUS"));
    }

    @Test
    public void testSearchWithTagWithIdSetToPermId()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"));
    }

    @Test
    public void testSearchWithTagWithCode()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withTag().withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, new MaterialPermId("AD3", "VIRUS"));
    }

    @Test
    public void testSearchWithRegistratorWithUserIdThatEquals()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withRegistrator().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"));
    }

    @Test
    public void testSearchWithRegistratorWithFirstNameThatEquals()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withRegistrator().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"));
    }

    @Test
    public void testSearchWithRegistratorWithLastNameThatEquals()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withRegistrator().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"));
    }

    @Test
    public void testSearchWithRegistratorWithEmailThatEquals()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withRegistrator().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, new MaterialPermId("BACTERIUM1", "BACTERIUM"), new MaterialPermId("BACTERIUM2", "BACTERIUM"));
    }

    @Test
    public void testSearchWithModifierWithUserIdThatEquals()
    {
        // search by a modifier not supported yet
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withModifier().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithModifierWithFirstNameThatEquals()
    {
        // search by a modifier not supported yet
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withModifier().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithModifierWithLastNameThatEquals()
    {
        // search by a modifier not supported yet
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withModifier().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithModifierWithEmailThatEquals()
    {
        // search by a modifier not supported yet
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withModifier().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithRegistrationDate()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withRegistrationDate().thatEquals("2012-03-13");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithModificationDate()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withModificationDate().thatEquals("2012-03-13");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithSortingByPropertyWithFloatValues()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new MaterialPermId("GFP", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("SCRAM", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("XXXXX-ALL", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("X-NO-DESC", "CONTROL"));
        criteria.withId().thatEquals(new MaterialPermId("X-NO-SIZE", "CONTROL"));

        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withProperties();

        fo.sortBy().property("VOLUME").asc();
        List<Material> materials1 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();

        assertEquals(materials1.get(0).getProperty("VOLUME"), "2.2");
        assertEquals(materials1.get(1).getProperty("VOLUME"), "3.0");
        assertEquals(materials1.get(2).getProperty("VOLUME"), "22.22");
        assertEquals(materials1.get(3).getProperty("VOLUME"), "99.99");
        assertEquals(materials1.get(4).getProperty("VOLUME"), "123");

        fo.sortBy().property("VOLUME").desc();
        List<Material> materials2 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();

        assertEquals(materials2.get(0).getProperty("VOLUME"), "123");
        assertEquals(materials2.get(1).getProperty("VOLUME"), "99.99");
        assertEquals(materials2.get(2).getProperty("VOLUME"), "22.22");
        assertEquals(materials2.get(3).getProperty("VOLUME"), "3.0");
        assertEquals(materials2.get(4).getProperty("VOLUME"), "2.2");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("SRM");
        criteria.withCode().thatContains("1A");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithOrOperator()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withCode().thatEquals("SRM_1");
        criteria.withCode().thatEquals("SRM_1A");
        testSearch(TEST_USER, criteria, new MaterialPermId("SRM_1", "SELF_REF"), new MaterialPermId("SRM_1A", "SELF_REF"));
    }

    @Test
    public void testSearchWithSortingByCode()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new MaterialPermId("FLU", "VIRUS"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE1", "GENE"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE2", "GENE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fo = new MaterialFetchOptions();

        fo.sortBy().code().asc();
        List<Material> materials1 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials1, new MaterialPermId("FLU", "VIRUS"), new MaterialPermId("MYGENE1", "GENE"),
                new MaterialPermId("MYGENE2", "GENE"));

        fo.sortBy().code().desc();
        List<Material> materials2 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials2, new MaterialPermId("MYGENE2", "GENE"), new MaterialPermId("MYGENE1", "GENE"),
                new MaterialPermId("FLU", "VIRUS"));

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByType()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new MaterialPermId("FLU", "VIRUS"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE1", "GENE"));
        criteria.withId().thatEquals(new MaterialPermId("MYGENE2", "GENE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withType();

        fo.sortBy().type().asc();
        fo.sortBy().code().asc();
        List<Material> materials1 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials1, new MaterialPermId("MYGENE1", "GENE"), new MaterialPermId("MYGENE2", "GENE"),
                new MaterialPermId("FLU", "VIRUS"));

        fo.sortBy().type().desc();
        fo.sortBy().code().desc();
        List<Material> materials2 = v3api.searchMaterials(sessionToken, criteria, fo).getObjects();
        assertMaterialPermIds(materials2, new MaterialPermId("FLU", "VIRUS"), new MaterialPermId("MYGENE2", "GENE"),
                new MaterialPermId("MYGENE1", "GENE"));

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialSearchCriteria c = new MaterialSearchCriteria();
        c.withCode().thatEquals("VIRUS");

        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withRegistrator();
        fo.withProperties();

        v3api.searchMaterials(sessionToken, c, fo);

        assertAccessLog(
                "search-materials  SEARCH_CRITERIA:\n'MATERIAL\n    with attribute 'code' equal to 'VIRUS'\n'\nFETCH_OPTIONS:\n'Material\n    with Registrator\n    with Properties\n'");
    }

    @Test
    public void testSearchNumeric()
    {
        // VOLUME: 99.99 CODE: GFP
        // VOLUME: 123 CODE: SCRAM
        // VOLUME: 3.0 CODE: X-NO-DESC
        // VOLUME: 22.22 CODE: X-NO-SIZE
        // VOLUME: 2.2 CODE: XXXXX-ALL
        
        // OFFSET: 123 CODE: 913_A
        // OFFSET: 321 CODE: 913_B
        // OFFSET: 111111 CODE: 913_C
        // OFFSET: 4711 CODE: OLI_1
        // OFFSET: 3 CODE: XX333_B
        // OFFSET: 123 CODE: XX444_A

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final MaterialFetchOptions sortByCodeFO = new MaterialFetchOptions();
        sortByCodeFO.sortBy().code().asc();
        sortByCodeFO.withProperties();

        // Greater or Equals
        final MaterialSearchCriteria criteriaGOE = new MaterialSearchCriteria();
        criteriaGOE.withNumberProperty("VOLUME").thatIsGreaterThanOrEqualTo(99.99);
        final List<Material> materialsGOE = search(sessionToken, criteriaGOE, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsGOE, "GFP (CONTROL)", "SCRAM (CONTROL)");

        // Greater or Equals - Providing integer as real
        final MaterialSearchCriteria criteriaGOEIR = new MaterialSearchCriteria();
        criteriaGOEIR.withNumberProperty("OFFSET").thatIsGreaterThanOrEqualTo(321.0);
        final List<Material> materialsGOEIR = search(sessionToken, criteriaGOEIR, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsGOEIR, "913_B (SIRNA)", "913_C (SIRNA)", "OLI_1 (SIRNA)");

        // Greater or Equals - Providing integer
        final MaterialSearchCriteria criteriaGOEI = new MaterialSearchCriteria();
        criteriaGOEI.withNumberProperty("OFFSET").thatIsGreaterThanOrEqualTo(321);
        final List<Material> materialsGOEI = search(sessionToken, criteriaGOEI, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsGOEI, "913_B (SIRNA)", "913_C (SIRNA)", "OLI_1 (SIRNA)");

        // Greater
        final MaterialSearchCriteria criteriaG = new MaterialSearchCriteria();
        criteriaG.withNumberProperty("VOLUME").thatIsGreaterThan(99.99);
        final List<Material> materialsG = search(sessionToken, criteriaG, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsG, "SCRAM (CONTROL)");

        // Greater - Providing integer as real
        final MaterialSearchCriteria criteriaGIR = new MaterialSearchCriteria();
        criteriaGIR.withNumberProperty("OFFSET").thatIsGreaterThan(321.0);
        final List<Material> materialsGIR = search(sessionToken, criteriaGIR, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsGIR, "913_C (SIRNA)", "OLI_1 (SIRNA)");

        // Greater - Providing integer
        final MaterialSearchCriteria criteriaGI = new MaterialSearchCriteria();
        criteriaGI.withNumberProperty("OFFSET").thatIsGreaterThan(321);
        final List<Material> materialsGI = search(sessionToken, criteriaGI, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsGI, "913_C (SIRNA)", "OLI_1 (SIRNA)");

        // Equals As Text - Real
        final MaterialSearchCriteria criteriaETxt2 = new MaterialSearchCriteria();
        criteriaETxt2.withProperty("OFFSET").thatEquals("123.0");
        final List<Material> materialsETxt2 = search(sessionToken, criteriaETxt2, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsETxt2, "913_A (SIRNA)", "XX444_A (SIRNA)");

        // Equals As Text - Integer
        MaterialSearchCriteria criteriaETxt = new MaterialSearchCriteria();
        criteriaETxt.withProperty("OFFSET").thatEquals("123");
        List<Material> materialsETxt = search(sessionToken, criteriaETxt, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsETxt, "913_A (SIRNA)", "XX444_A (SIRNA)");

        // Equals
        MaterialSearchCriteria criteriaE = new MaterialSearchCriteria();
        criteriaE.withNumberProperty("OFFSET").thatEquals(123);
        List<Material> materialsE = search(sessionToken, criteriaE, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsE, "913_A (SIRNA)", "XX444_A (SIRNA)");

        // Less or Equals
        final MaterialSearchCriteria criteriaLOE = new MaterialSearchCriteria();
        criteriaLOE.withNumberProperty("VOLUME").thatIsLessThanOrEqualTo(99.99);
        final List<Material> materialsLOE = search(sessionToken, criteriaLOE, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsLOE, "GFP (CONTROL)", "X-NO-DESC (CONTROL)",  "X-NO-SIZE (CONTROL)",
                "XXXXX-ALL (CONTROL)");

        // Less or Equals - Providing integer as real
        final MaterialSearchCriteria criteriaLOEIR = new MaterialSearchCriteria().withAndOperator();
        criteriaLOEIR.withNumberProperty("OFFSET").thatIsLessThanOrEqualTo(321.0);
        criteriaLOEIR.withNumberProperty("OFFSET").thatIsGreaterThan(1.0);
        final List<Material> materialsLOEIR = search(sessionToken, criteriaLOEIR, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsLOEIR, "913_A (SIRNA)", "913_B (SIRNA)", "XX333_B (SIRNA)",
                "XX444_A (SIRNA)");

        // Less or Equals - Providing integer
        final MaterialSearchCriteria criteriaLOEI = new MaterialSearchCriteria().withAndOperator();
        criteriaLOEI.withNumberProperty("OFFSET").thatIsLessThanOrEqualTo(321);
        criteriaLOEI.withNumberProperty("OFFSET").thatIsGreaterThan(1);
        final List<Material> materialsLOEI = search(sessionToken, criteriaLOEI, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsLOEI, "913_A (SIRNA)", "913_B (SIRNA)", "XX333_B (SIRNA)",
                "XX444_A (SIRNA)");

        // Less
        final MaterialSearchCriteria criteriaL = new MaterialSearchCriteria();
        criteriaL.withNumberProperty("VOLUME").thatIsLessThan(99.99);
        final List<Material> materialsL = search(sessionToken, criteriaL, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsL, "X-NO-DESC (CONTROL)",  "X-NO-SIZE (CONTROL)",
                "XXXXX-ALL (CONTROL)");

        // Less - Providing integer as real
        final MaterialSearchCriteria criteriaLIR = new MaterialSearchCriteria().withAndOperator();
        criteriaLIR.withNumberProperty("OFFSET").thatIsLessThan(321.0);
        criteriaLIR.withNumberProperty("OFFSET").thatIsGreaterThan(1.0);
        final List<Material> materialsLIR = search(sessionToken, criteriaLIR, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsLIR, "913_A (SIRNA)", "XX333_B (SIRNA)", "XX444_A (SIRNA)");

        // Greater - Providing integer
        final MaterialSearchCriteria criteriaLI = new MaterialSearchCriteria();
        criteriaLI.withNumberProperty("OFFSET").thatIsLessThan(321);
        criteriaLI.withNumberProperty("OFFSET").thatIsGreaterThan(1);
        final List<Material> materialsLI = search(sessionToken, criteriaLI, sortByCodeFO);
        assertMaterialIdentifiersInOrder(materialsLI, "913_A (SIRNA)", "XX333_B (SIRNA)", "XX444_A (SIRNA)");

        v3api.logout(sessionToken);
    }

    private List<Material> search(final String sessionToken, final MaterialSearchCriteria criteria,
            final MaterialFetchOptions options)
    {
        return v3api.searchMaterials(sessionToken, criteria, options).getObjects();
    }

    private void testSearch(String user, MaterialSearchCriteria criteria, MaterialFetchOptions options, MaterialPermId... expectedPermIds)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Material> searchResult =
                v3api.searchMaterials(sessionToken, criteria, options);
        List<Material> materials = searchResult.getObjects();

        assertMaterialPermIds(materials, expectedPermIds);
        v3api.logout(sessionToken);
    }

    private void testSearch(String user, MaterialSearchCriteria criteria, MaterialPermId... expectedPermIds)
    {
        testSearch(user, criteria, new MaterialFetchOptions(), expectedPermIds);
    }

    private void testSearch(String user, MaterialSearchCriteria criteria, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Material> searchResult =
                v3api.searchMaterials(sessionToken, criteria, new MaterialFetchOptions());
        List<Material> materials = searchResult.getObjects();

        assertEquals(materials.size(), expectedCount);
        v3api.logout(sessionToken);
    }

}
