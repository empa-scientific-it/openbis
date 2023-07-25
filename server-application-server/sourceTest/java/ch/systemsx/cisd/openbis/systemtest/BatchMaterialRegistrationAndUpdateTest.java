/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class BatchMaterialRegistrationAndUpdateTest extends SystemTestCase
{
    private static final String MATERIAL_TYPE = "CONTROL";

    private static final Set<String> CODES = new HashSet<String>(Arrays.asList("C1", "C2"));

    @AfterMethod
    public void tearDown()
    {
        deleteTestMaterials();
        commonClientService.unassignPropertyType(EntityKind.MATERIAL, "COMMENT", MATERIAL_TYPE);
    }

    @Test(groups = "slow")
    public void testBatchRegistrationWithManagedProperty()
    {
        SessionContext session = logIntoCommonClientService();
        deleteTestMaterials();
        Script script = new Script();
        script.setScriptType(ScriptType.MANAGED_PROPERTY);
        script.setPluginType(PluginType.JYTHON);
        script.setName("batch script");
        script.setScript("def batchColumnNames():\n  return ['A', 'B']\n"
                + "def updateFromBatchInput(bindings):\n"
                + "  property.setValue(bindings.get('A') + ' & ' + bindings.get('B'))\n"
                + "def configureUI():\n  None");
        commonClientService.registerScript(script);
        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setEntityKind(EntityKind.MATERIAL);
        assignment.setEntityTypeCode(MATERIAL_TYPE);
        assignment.setPropertyTypeCode("COMMENT");
        assignment.setManaged(true);
        assignment.setScriptName("batch script");
        assignment.setOrdinal(0L);
        commonClientService.assignPropertyType(assignment);
        String materialBatchData =
                "code\tdescription\tsize\tcomment:a\tcomment:b\n" + "c1\tcompound 1\t42\tx\ty\n"
                        + "c2\tcompound 2\t43\ta\tb";

        List<BatchRegistrationResult> result = registerMaterials(session.getSessionID(), materialBatchData, MATERIAL_TYPE);

        assertEquals("Registration/update of 2 material(s) is complete.", result.get(0)
                .getMessage());
        assertEquals(1, result.size());

        assertProperties("[COMMENT: x & y, DESCRIPTION: compound 1, SIZE: 42]", "C1");
        assertProperties("[COMMENT: a & b, DESCRIPTION: compound 2, SIZE: 43]", "C2");
    }

    @Test(groups = "slow")
    public void testUpdateOfPropertiesOfVariousTypes()
    {
        SessionContext session = logIntoCommonClientService();
        deleteTestMaterials();

        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setEntityKind(EntityKind.MATERIAL);
        assignment.setEntityTypeCode(MATERIAL_TYPE);
        assignment.setPropertyTypeCode("GENDER");
        commonClientService.assignPropertyType(assignment);
        assignment.setPropertyTypeCode("BACTERIUM");
        commonClientService.assignPropertyType(assignment);

        String materialBatchData =
                "code\tdescription\tsize\tgender\tbacterium\n"
                        + "c1\tcompound 1\t42\tfemale\tbacterium1\n"
                        + "c2\tcompound 2\t43\tmale\tbacterium-x";
        registerMaterials(session.getSessionID(), materialBatchData, MATERIAL_TYPE);

        long timeBeforeUpdate = System.currentTimeMillis();

        List<BatchRegistrationResult> result =
                updateMaterials(session.getSessionID(), "code\tdescription\tgender\tbacterium\n"
                        + "c1\tnew description\tmale\tbacterium2\n" + "c2\t\tmale\tbacterium-y",
                        MATERIAL_TYPE, false);

        long timeAfterUpdate = System.currentTimeMillis();

        assertEquals("2 material(s) updated.", result.get(0).getMessage());
        assertEquals(1, result.size());
        assertProperties(
                "[BACTERIUM: BACTERIUM2 (BACTERIUM), DESCRIPTION: new description, GENDER: MALE, SIZE: 42]",
                "C1");
        assertProperties(
                "[BACTERIUM: BACTERIUM-Y (BACTERIUM), DESCRIPTION: compound 2, GENDER: MALE, SIZE: 43]",
                "C2");
        List<PropertyHistory> history =
                getMaterialPropertiesHistory(getMaterialOrNull("C1").getId());
        assertEquals(
                "[BACTERIUM: material:BACTERIUM1 [BACTERIUM]<a:2>, DESCRIPTION: compound 1<a:2>, GENDER: term:FEMALE [GENDER]<a:2>]",
                history.toString());

        PropertyHistory bacteriumHistory = history.get(0);

        assertTrue(bacteriumHistory.getValidFromTimeStamp().getTime() < timeBeforeUpdate);
        assertTrue(bacteriumHistory.getValidUntilTimeStamp().getTime() < timeAfterUpdate);

        assertEquals("[BACTERIUM: material:BACTERIUM-X [BACTERIUM]<a:2>]",
                getMaterialPropertiesHistory(getMaterialOrNull("C2").getId()).toString());

        updateMaterials(session.getSessionID(), "code\tdescription\tgender\tbacterium\n"
                + "c2\t--DELETE--\tfemale\tbacterium2\n", MATERIAL_TYPE, false);

        assertEquals(
                "[BACTERIUM: material:BACTERIUM-X [BACTERIUM]<a:2>, BACTERIUM: material:BACTERIUM-Y [BACTERIUM]<a:2>, DESCRIPTION: compound 2<a:2>, GENDER: term:MALE [GENDER]<a:2>]",
                getMaterialPropertiesHistory(getMaterialOrNull("C2").getId()).toString());
        deleteTestMaterials();
    }

    @Test
    public void testUpdateIgnoreUnregistered()
    {
        SessionContext session = logIntoCommonClientService();
        deleteTestMaterials();
        String materialBatchData =
                "code\tdescription\tsize\n" + "c1\tcompound 1\t42\n" + "c2\tcompound 2\t43";
        registerMaterials(session.getSessionID(), materialBatchData, MATERIAL_TYPE);

        List<BatchRegistrationResult> result =
                updateMaterials(session.getSessionID(), "code\tdescription\tsize\n" + "c1\tcompound one\t\n"
                        + "c2\tcompound two\t4711\n" + "c3\t3\t\n", MATERIAL_TYPE, true);

        assertEquals("2 material(s) updated, 1 ignored.", result.get(0).getMessage());
        assertEquals(1, result.size());

        assertProperties("[DESCRIPTION: compound one, SIZE: 42]", "C1");
        assertProperties("[DESCRIPTION: compound two, SIZE: 4711]", "C2");
    }

    private void deleteTestMaterials()
    {
        MaterialType materialType = getMaterialType(MATERIAL_TYPE);
        TypedTableResultSet<Material> materials =
                commonClientService.listMaterials(ListMaterialDisplayCriteria
                        .createForMaterialType(materialType));
        GridRowModels<TableModelRowWithObject<Material>> list = materials.getResultSet().getList();
        List<TableModelRowWithObject<Material>> materialsToBeDeleted =
                new ArrayList<TableModelRowWithObject<Material>>();
        for (GridRowModel<TableModelRowWithObject<Material>> gridRowModel : list)
        {
            TableModelRowWithObject<Material> row = gridRowModel.getOriginalObject();
            if (CODES.contains(row.getObjectOrNull().getCode()))
            {
                materialsToBeDeleted.add(row);
            }
        }
        commonClientService
                .deleteMaterials(
                        DisplayedOrSelectedIdHolderCriteria
                                .<TableModelRowWithObject<Material>> createSelectedItems(materialsToBeDeleted),
                        "?");
        for (TableModelRowWithObject<Material> deletedMaterial : materialsToBeDeleted)
        {
            assertEquals("Deleted material: " + deletedMaterial, 0,
                    getMaterialPropertiesHistory(deletedMaterial.getId()).size());
        }
    }

    private MaterialType getMaterialType(String code)
    {
        List<MaterialType> materialTypes = commonClientService.listMaterialTypes();
        for (MaterialType materialType : materialTypes)
        {
            if (materialType.getCode().equalsIgnoreCase(code))
            {
                return materialType;
            }
        }
        throw new IllegalArgumentException("Unknown material type: " + code);
    }

    private void assertProperties(String expectedProperties, String materialCode)
    {
        assertEquals(expectedProperties, StringEscapeUtils.unescapeHtml(getMaterialOrNull(
                materialCode).getProperties().toString()));
    }

    private Material getMaterialOrNull(String code)
    {
        try
        {
            IEntityInformationHolderWithPermId m =
                    commonClientService.getMaterialInformationHolder(new MaterialIdentifier(code,
                            MATERIAL_TYPE));
            Material materialInfo = commonClientService.getMaterialInfo(new TechId(m.getId()));
            assertEquals(code, materialInfo.getCode());
            Collections.sort(materialInfo.getProperties(), new Comparator<IEntityProperty>()
                {
                    @Override
                    public int compare(IEntityProperty p1, IEntityProperty p2)
                    {
                        return p1.getPropertyType().getCode()
                                .compareTo(p2.getPropertyType().getCode());
                    }
                });
            return materialInfo;
        } catch (UserFailureException ex)
        {
            return null;
        }
    }

    private List<BatchRegistrationResult> registerMaterials(String sessionToken, String materialBatchData,
            String materialTypeCode)
    {
        uploadFile(sessionToken, "my-file", materialBatchData);
        MaterialType materialType = new MaterialType();
        materialType.setCode(materialTypeCode);
        return genericClientService
                .registerMaterials(materialType, false, SESSION_KEY, false, null);
    }

    private List<BatchRegistrationResult> updateMaterials(String sessionToken, String materialBatchData,
            String materialTypeCode, boolean ignoreUnregistered)
    {
        uploadFile(sessionToken, "my-file", materialBatchData);
        MaterialType materialType = new MaterialType();
        materialType.setCode(materialTypeCode);
        return genericClientService.updateMaterials(materialType, SESSION_KEY, ignoreUnregistered,
                false, null);
    }

}
