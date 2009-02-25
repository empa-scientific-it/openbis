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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link MaterialTable} class.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = MaterialTable.class)
public final class MaterialTableTest extends AbstractBOTest
{
    private final MaterialTable createMaterialTable(List<MaterialPE> materials, boolean dataChanged)
    {
        return new MaterialTable(daoFactory, ManagerTestTool.EXAMPLE_SESSION, propertiesConverter,
                materials, dataChanged);
    }

    private final MaterialTable createMaterialTable()
    {
        return createMaterialTable(null, false);
    }

    @Test
    public void testLoad() throws Exception
    {
        final MaterialTypePE materialType = CommonTestUtils.createMaterialType();
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    allowing(daoFactory).getMaterialDAO();
                    will(returnValue(materialDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(materialType.getCode());
                    will(returnValue(materialType));

                    one(materialDAO).listMaterialsWithPropertiesAndInhibitor(materialType);
                    will(returnValue(new ArrayList<MaterialPE>()));
                }
            });
        createMaterialTable().load(materialType.getCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testAddMaterials() throws Exception
    {
        final MaterialTypePE materialType = CommonTestUtils.createMaterialType();
        List<NewMaterial> newMaterials = new ArrayList<NewMaterial>();
        newMaterials.add(createNewMaterial("BRAND_NEW_MATERIAL"));
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(CommonTestUtils
                            .createDatabaseInstance(CommonTestUtils.HOME_DATABASE_INSTANCE_CODE)));
                }
            });
        createMaterialTable().add(newMaterials, materialType);
        context.assertIsSatisfied();
    }

    @Test
    public void testSave() throws Exception
    {
        final MaterialTypePE materialType = CommonTestUtils.createMaterialType();
        final ArrayList<MaterialPE> materials = new ArrayList<MaterialPE>();
        materials.add(createMaterial(materialType, "BRAND_NEW_MATERIAL"));
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getMaterialDAO();
                    will(returnValue(materialDAO));

                    one(materialDAO).createMaterials(materials);
                }
            });
        createMaterialTable(materials, true).save();
        context.assertIsSatisfied();
    }

    private MaterialPE createMaterial(MaterialTypePE materialType, String code)
    {
        final MaterialPE material = new MaterialPE();
        material.setCode(code);
        material.setMaterialType(materialType);
        material.setDatabaseInstance(CommonTestUtils
                .createDatabaseInstance(CommonTestUtils.HOME_DATABASE_INSTANCE_CODE));
        material.setRegistrator(EXAMPLE_SESSION.tryGetPerson());
        return material;
    }

    private NewMaterial createNewMaterial(String code)
    {
        final NewMaterial material = new NewMaterial();
        material.setCode(code);
        return material;
    }
}
