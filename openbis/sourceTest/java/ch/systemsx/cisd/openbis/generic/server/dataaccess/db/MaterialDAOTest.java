/*
 * Copyright 2007 ETH Zuerich, CISD
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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.MaterialConfigurationProvider;

/**
 * Test cases for corresponding {@link MaterialDAO} class.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
{ "db", "material" })
@SuppressWarnings("deprecation")
public final class MaterialDAOTest extends AbstractDAOTest
{
    private static final String BACTERIUM = "BACTERIUM";

    private static final String BRAND_NEW_BACTERIUM = "BRAND_NEW_BACTERIUM";

    private final int NUMBER_OF_BACTERIA = 4;

    private IMaterialDAO materialDAO;

    private MaterialConfigurationProvider oldProvider;

    @BeforeMethod
    @Override
    public void setUp()
    {
        super.setUp();
        oldProvider = MaterialConfigurationProvider.initializeForTesting(false);
        materialDAO = daoFactory.getMaterialDAO();
    }

    @Override
    @AfterMethod
    public void tearDown()
    {
        super.tearDown();
        MaterialConfigurationProvider.restoreFromTesting(oldProvider);
    }

    @Test
    public void testCreateMaterials() throws Exception
    {
        MaterialTypePE type = getMaterialType();
        List<MaterialPE> bacteria_before = materialDAO.listMaterialsWithProperties(type);
        Assert.assertEquals(NUMBER_OF_BACTERIA, bacteria_before.size());
        List<MaterialPE> newMaterials = new ArrayList<MaterialPE>();
        newMaterials.add(createMaterial(type, "BRAND_NEW_BACTERIUM_1"));
        newMaterials.add(createMaterial(type, "BRAND_NEW_BACTERIUM_2"));
        Collections.sort(newMaterials);
        materialDAO.createOrUpdateMaterials(newMaterials);
        List<MaterialPE> bacteria_after = materialDAO.listMaterialsWithProperties(type);
        Assert.assertEquals(NUMBER_OF_BACTERIA + newMaterials.size(), bacteria_after.size());
        bacteria_after.removeAll(bacteria_before);
        Collections.sort(bacteria_after);
        for (int i = 0; i < newMaterials.size(); i++)
        {
            Assert.assertEquals(newMaterials.get(i), bacteria_after.get(i));
        }
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testFailCreateMaterialsWithTheSameCode() throws Exception
    {
        MaterialTypePE type = getMaterialType();
        List<MaterialPE> newMaterials = new ArrayList<MaterialPE>();
        newMaterials.add(createMaterial(type, BRAND_NEW_BACTERIUM));
        newMaterials.add(createMaterial(type, BRAND_NEW_BACTERIUM));
        materialDAO.createOrUpdateMaterials(newMaterials);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testFailCreateMaterialsWithExistingCode() throws Exception
    {
        MaterialTypePE type = getMaterialType();
        List<MaterialPE> bacteria_before = materialDAO.listMaterialsWithProperties(type);
        String existingBacteriumCode = bacteria_before.get(0).getCode();
        List<MaterialPE> newMaterials = new ArrayList<MaterialPE>();
        newMaterials.add(createMaterial(type, existingBacteriumCode));
        materialDAO.createOrUpdateMaterials(newMaterials);
    }

    @Test
    public final void testDeleteWithProperties()
    {
        // BACTERIUM2 has not been used as a property value
        MaterialIdentifier identifier = new MaterialIdentifier("BACTERIUM2", "BACTERIUM");
        final MaterialPE deletedMaterial = materialDAO.tryFindMaterial(identifier);
        assertNotNull(deletedMaterial);
        assertFalse(deletedMaterial.getProperties().isEmpty());

        materialDAO.delete(deletedMaterial);

        assertNull(materialDAO.tryFindMaterial(identifier));

        List<EntityTypePropertyTypePE> retrievedPropertyTypes =
                daoFactory.getEntityPropertyTypeDAO(EntityKind.MATERIAL).listEntityPropertyTypes(
                        deletedMaterial.getEntityType());
        for (MaterialPropertyPE property : deletedMaterial.getProperties())
        {
            int index = retrievedPropertyTypes.indexOf(property.getEntityTypePropertyType());
            EntityTypePropertyTypePE retrievedPropertyType = retrievedPropertyTypes.get(index);
            assertFalse(retrievedPropertyType.getPropertyValues().contains(property));
        }
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testFailDeleteMaterialUsedAsPropertyValue()
    {
        String bacteriumX = "BACTERIUM-X";
        MaterialIdentifier identifier = new MaterialIdentifier(bacteriumX, BACTERIUM);
        final MaterialPE usedMaterial = materialDAO.tryFindMaterial(identifier);
        assertNotNull(usedMaterial);

        // Assert that BACTERIUM-X has been used as a property value
        ProjectPE project = daoFactory.getProjectDAO().tryFindProject("CISD", "NEMO");
        SamplePE sample =
                daoFactory.getSampleDAO().tryfindByCodeAndProject("CP-TEST-1", project);
        assertNotNull(sample);
        boolean bacteriumFound = false;
        for (SamplePropertyPE property : sample.getProperties())
        {
            MaterialPE materialValue = property.getMaterialValue();
            if (materialValue != null && materialValue.getCode().equals(bacteriumX)
                    && materialValue.getMaterialType().getCode().equals(BACTERIUM))
            {
                bacteriumFound = true;
            }
        }
        assertTrue(bacteriumFound);

        materialDAO.delete(usedMaterial);
    }

    @Test
    public void testMaterialBulkDeletion() throws Exception
    {
        final int bulkSize = 10;
        MaterialTypePE type = getMaterialType();
        List<MaterialPE> preExistingBacterias = materialDAO.listMaterialsWithProperties(type);

        List<MaterialPE> newBacterias = new ArrayList<MaterialPE>();
        for (int i = 0; i < bulkSize; i++)
        {
            newBacterias.add(createMaterial(type, "BULK_BACTERIUM_" + i));
        }

        materialDAO.createOrUpdateMaterials(newBacterias);

        List<MaterialPE> allBacterias = materialDAO.listMaterialsWithProperties(type);
        Assert.assertEquals(NUMBER_OF_BACTERIA + bulkSize, allBacterias.size());

        allBacterias.removeAll(preExistingBacterias);
        materialDAO.delete(TechId.createList(allBacterias), getSystemPerson(), "test reason");
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testCreateMaterialsStrictCodeConstraints() throws Exception
    {
        MaterialTypePE type = getMaterialType();
        List<MaterialPE> newMaterials = new ArrayList<MaterialPE>();
        newMaterials.add(createMaterial(type, ":()ABC.12/D"));
        materialDAO.createOrUpdateMaterials(newMaterials);
    }

    @Test
    public void testCreateMaterialsRelaxedCodeConstraints() throws Exception
    {
        MaterialConfigurationProvider.initializeForTesting(true);
        MaterialTypePE type = getMaterialType();
        List<MaterialPE> newMaterials = new ArrayList<MaterialPE>();
        newMaterials.add(createMaterial(type, ":()ABC.12/D"));
        newMaterials.add(createMaterial(type, "id/14(head:tail)%division"));
        materialDAO.createOrUpdateMaterials(newMaterials);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testCreateMaterialsRelaxedCodeConstraintsAndWhiteSpace() throws Exception
    {
        MaterialConfigurationProvider.initializeForTesting(true);
        MaterialTypePE type = getMaterialType();
        List<MaterialPE> newMaterials = new ArrayList<MaterialPE>();
        newMaterials.add(createMaterial(type, "A B"));
        materialDAO.createOrUpdateMaterials(newMaterials);
    }

    private MaterialTypePE getMaterialType()
    {
        return (MaterialTypePE) daoFactory.getEntityTypeDAO(EntityKind.MATERIAL)
                .tryToFindEntityTypeByCode(BACTERIUM);
    }
}