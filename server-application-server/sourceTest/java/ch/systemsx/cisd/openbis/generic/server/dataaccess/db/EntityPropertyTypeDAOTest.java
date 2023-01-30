/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for {@link EntityPropertyTypeDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
{ "db", "property" })
public class EntityPropertyTypeDAOTest extends AbstractDAOTest
{

    @Test(dataProvider = "entityKindsWithEntityTypeAndPropertyTypeMandatory")
    public final void testTryFindAssignment(EntityKind entityKind, String typeCode,
            String propertyCode)
    {
        EntityTypePropertyTypePE assignment =
                tryToGetAssignment(entityKind, typeCode, propertyCode);
        Assert.assertEquals(true, assignment.isMandatory());
    }

    @Test(dataProvider = "entityKindsWithEntityTypeAndPropertyTypeNotAssigned")
    public final void testTryFindNonexistentAssignment(EntityKind entityKind, String typeCode,
            String propertyCode)
    {
        EntityTypePropertyTypePE assignment =
                tryToGetAssignment(entityKind, typeCode, propertyCode);
        Assert.assertNull(assignment);
    }

    @Test(dataProvider = "entityKindsWithEntityTypeAndPropertyTypeNotAssigned")
    public void testCreateEntityPropertyTypeAssignment(EntityKind entityKind, String typeCode,
            String propertyCode)
    {
        // prepare data
        EntityTypePE entityType =
                daoFactory.getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(typeCode);
        PropertyTypePE propertyType =
                daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyCode);
        // check assignment does not exist
        Assert.assertNull(daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(
                entityType, propertyType));
        // create assignment
        EntityTypePropertyTypePE entityPropertyTypeAssignement =
                createAssignment(entityKind, entityType, propertyType);
        daoFactory.getEntityPropertyTypeDAO(entityKind).createEntityPropertyTypeAssignment(
                entityPropertyTypeAssignement);
        // check assignment exists
        Assert.assertNotNull(daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(
                entityType, propertyType));

    }

    public final void testCountTermUsageStatistics()
    {
        IEntityPropertyTypeDAO dao = daoFactory.getEntityPropertyTypeDAO(EntityKind.EXPERIMENT);
        VocabularyPE vocabulary = daoFactory.getVocabularyDAO().tryFindVocabularyByCode("GENDER");
        assert vocabulary != null : "gender vocabulary not found";

        VocabularyTermWithStats maleTermWithStats = createTermWithStats(vocabulary, "MALE");
        VocabularyTermWithStats femaleTermWithStats = createTermWithStats(vocabulary, "FEMALE");

        List<VocabularyTermWithStats> termsWithStats =
                Arrays.asList(maleTermWithStats, femaleTermWithStats);
        dao.fillTermUsageStatistics(termsWithStats, vocabulary);

        assertEquals(3, maleTermWithStats.getUsageCounter(EntityKind.EXPERIMENT));
        assertEquals(1, femaleTermWithStats.getUsageCounter(EntityKind.EXPERIMENT));
    }

    private VocabularyTermWithStats createTermWithStats(VocabularyPE vocabulary, String termCode)
    {
        final VocabularyTermPE term = tryFindTerm(vocabulary, termCode);
        assert term != null : "term " + termCode + " not found in " + vocabulary.getCode()
                + " vocabulary";
        return new VocabularyTermWithStats(term);
    }

    private static VocabularyTermPE tryFindTerm(VocabularyPE vocabulary, String termCode)
    {
        Set<VocabularyTermPE> terms = vocabulary.getTerms();
        for (VocabularyTermPE term : terms)
        {
            if (term.getCode().equalsIgnoreCase(termCode))
            {
                return term;
            }
        }
        return null;
    }

    @Test
    public void testListPropertiesByVocabularyTerm()
    {
        IEntityPropertyTypeDAO entityPropertyTypeDAO =
                daoFactory.getEntityPropertyTypeDAO(EntityKind.MATERIAL);
        List<EntityPropertyPE> properties =
                entityPropertyTypeDAO.listPropertiesByVocabularyTerm(10);

        assertEquals(1, properties.size());
        assertEquals("FLY", properties.get(0).getVocabularyTerm().getCode());
    }

    @Test
    public void testDelete()
    {
        EntityTypePropertyTypePE assignment =
                tryToGetAssignment(EntityKind.EXPERIMENT, "SIRNA_HCS", "DESCRIPTION");
        // Remember how many rows are in the properties table before we delete
        int beforeDeletionPropertiesRowCount =
                countRowsInTable(TableNames.EXPERIMENT_PROPERTIES_TABLE);
        assertEquals(true, beforeDeletionPropertiesRowCount > 0);
        daoFactory.getEntityPropertyTypeDAO(EntityKind.EXPERIMENT).delete(assignment);

        int afterDeletionPropertiesRowCount =
                countRowsInTable(TableNames.EXPERIMENT_PROPERTIES_TABLE);
        assertEquals(beforeDeletionPropertiesRowCount - 8, afterDeletionPropertiesRowCount);
    }

    private EntityTypePropertyTypePE tryToGetAssignment(EntityKind entityKind,
            String entityTypeCode, String propertyTypeCode)
    {
        EntityTypePE entityType =
                daoFactory.getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(entityTypeCode);
        PropertyTypePE propertyType =
                daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyTypeCode);
        EntityTypePropertyTypePE assignment =
                daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(entityType,
                        propertyType);
        return assignment;
    }

    @DataProvider
    private final static Object[][] entityKindsWithEntityTypeAndPropertyTypeMandatory()
    {
        return new Object[][]
        {
                { EntityKind.EXPERIMENT, "SIRNA_HCS", "DESCRIPTION" },
                { EntityKind.SAMPLE, "CONTROL_LAYOUT", "$PLATE_GEOMETRY" },
                { EntityKind.MATERIAL, "BACTERIUM", "DESCRIPTION" } };
    }

    @DataProvider
    private final static Object[][] entityKindsWithEntityTypeAndPropertyTypeNotAssigned()
    {
        return new Object[][]
        {
                { EntityKind.EXPERIMENT, "SIRNA_HCS", "IS_VALID" },
                { EntityKind.SAMPLE, "CONTROL_LAYOUT", "IS_VALID" },
                { EntityKind.MATERIAL, "BACTERIUM", "IS_VALID" }, };
    }
}
