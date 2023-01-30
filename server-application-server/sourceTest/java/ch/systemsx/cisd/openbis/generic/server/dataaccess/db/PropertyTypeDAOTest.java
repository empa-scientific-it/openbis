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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * Test cases for corresponding {@link PropertyTypeDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups = { "db", "propertyType" })
public final class PropertyTypeDAOTest extends AbstractDAOTest
{

    static final void checkDataType(final DataTypePE dataTypeDTO, final boolean checkDescription)
    {
        assertNotNull(dataTypeDTO.getId());
        assertNotNull(dataTypeDTO.getCode());
        if (checkDescription)
        {
            assertNotNull(dataTypeDTO.getDescription());
        }
    }

    final static void checkPropertyType(final PropertyTypePE propertyTypeDTO)
    {
        assertNotNull(propertyTypeDTO.getId());
        assertNotNull(propertyTypeDTO.getCode());
        assertNotNull(propertyTypeDTO.getDescription());
        assertNotNull(propertyTypeDTO.getLabel());
        checkDataType(propertyTypeDTO.getType(), false);
    }

    @Test
    public final void testListPropertyTypes()
    {
        final IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
        final List<PropertyTypePE> list = propertyTypeDAO.listPropertyTypes();
        for (final PropertyTypePE propertyTypeDTO : list)
        {
            checkPropertyType(propertyTypeDTO);
            assertFalse(CodeConverter.isInternalNamespace(propertyTypeDTO.getCode()));
        }
        assertTrue(list.size() > 0);
    }

    @Test
    public final void testListDataTypesAndEnumeration()
    {
        final List<DataTypePE> list = daoFactory.getPropertyTypeDAO().listDataTypes();
        assertEquals(DataTypeCode.values().length, list.size());
        assertEquals(DataTypeCode.values().length, list.size());
        for (final DataTypePE dataType : list)
        {
            DataTypeCode.valueOf(dataType.getCode().name()); // check enums identity
            checkDataType(dataType, true);
        }
    }

    @Test
    public final void testTryFindDataTypeByCode()
    {
        final IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
        boolean fail = true;
        try
        {
            propertyTypeDAO.getDataTypeByCode(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertNotNull(propertyTypeDAO.getDataTypeByCode(DataTypeCode.INTEGER));
    }

    @DataProvider
    private final Object[][] getEntityDataType()
    {
        return new Object[][] {
                { DataTypeCode.BOOLEAN },
                { DataTypeCode.INTEGER },
                { DataTypeCode.REAL },
                { DataTypeCode.TIMESTAMP },
                { DataTypeCode.VARCHAR }, };
    }

    @Test(dataProvider = "getEntityDataType")
    public final void testCreatePropertyType(final DataTypeCode entityDataType)
    {
        final IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
        boolean fail = true;
        try
        {
            propertyTypeDAO.createPropertyType(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        // TODO KE: decide what to do with internal property types. Create a separate create methods
        // for them ?
        // try
        // {
        // createPropertyType(entityDataType, "$code");
        // fail(String.format("'%s' expected.", DataIntegrityViolationException.class
        // .getSimpleName()));
        // } catch (final DataIntegrityViolationException ex)
        // {
        // // Nothing to do here.
        // }
        assertNull(propertyTypeDAO.tryFindPropertyTypeByCode("$code"));
        createPropertyType(entityDataType, "code");
        assertNotNull(propertyTypeDAO.tryFindPropertyTypeByCode("code"));
    }

    private final void createPropertyType(final DataTypeCode entityDataType, final String code)
    {
        final IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
        propertyTypeDAO.createPropertyType(createPropertyType(
                propertyTypeDAO.getDataTypeByCode(entityDataType), code, null, null));
    }

    @Test
    public void testCreateUpdatePropertyTypeMetaData()
    {
        // Given 1: Creation
        IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
        String code = "PT-" + System.currentTimeMillis();
        PropertyTypePE propertyType = createPropertyType(propertyTypeDAO.getDataTypeByCode(DataTypeCode.VARCHAR), code, null, null);
        HashMap<String, String> metaData = new HashMap<String, String>();
        metaData.put("greetings", "hello");
        metaData.put("pi", Double.toString(Math.PI));
        propertyType.setMetaData(metaData);

        // When 1
        propertyTypeDAO.createPropertyType(propertyType);

        // Then 1
        Map<String, String> metaData2 = propertyTypeDAO.tryFindPropertyTypeByCode(code).getMetaData();
        assertEquals("[greetings=hello, pi=3.141592653589793]", getSortedEntries(metaData2).toString());

        // Given 2: Update
        metaData2.remove("pi");
        metaData2.put("greetings", "hello test");
        metaData2.put("name", "Test");

        // When 2
        propertyTypeDAO.flush();

        // Then 2
        Map<String, String> metaData3 = propertyTypeDAO.tryFindPropertyTypeByCode(code).getMetaData();
        assertEquals("[greetings=hello test, name=Test]", getSortedEntries(metaData3).toString());
    }

    private List<Entry<String, String>> getSortedEntries(Map<String, String> map)
    {
        List<Entry<String, String>> result = new ArrayList<>(map.entrySet());
        Collections.sort(result, new SimpleComparator<Entry<String, String>, String>()
            {

                @Override
                public String evaluate(Entry<String, String> item)
                {
                    return item.getKey();
                }
            });
        return result;
    }

    @Test
    public final void testDelete()
    {
        // create new property type with no connections
        final DataTypeCode entityDataType = DataTypeCode.BOOLEAN;
        final String propertyTypeCode = "user.bool";
        createPropertyType(entityDataType, propertyTypeCode);

        final IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
        final PropertyTypePE deletedPropertyType = findPropertyType(propertyTypeCode);

        // Deleted property type should have no connections which prevent it from deletion.
        assertTrue(getConnectionsPreventingDeletion(deletedPropertyType).isEmpty());

        // delete
        propertyTypeDAO.delete(deletedPropertyType);

        // test successful deletion of vocabulary
        assertNull(propertyTypeDAO.tryGetByTechId(TechId.create(deletedPropertyType)));
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFail()
    {
        final IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
        final PropertyTypePE deletedPropertyType = findPropertyType("COMMENT");

        // Deleted property type should have at least one connection which prevent it from deletion.
        assertFalse(getConnectionsPreventingDeletion(deletedPropertyType).isEmpty());

        // delete
        propertyTypeDAO.delete(deletedPropertyType);
    }

    private final PropertyTypePE findPropertyType(String code)
    {
        final IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
        final PropertyTypePE propertyType = propertyTypeDAO.tryFindPropertyTypeByCode(code);
        assertNotNull(propertyType);

        return propertyType;
    }

    private final List<Object> getConnectionsPreventingDeletion(final PropertyTypePE propertyType)
    {
        List<Object> result = new ArrayList<Object>();
        result.addAll(propertyType.getDataSetTypePropertyTypes());
        result.addAll(propertyType.getExperimentTypePropertyTypes());
        result.addAll(propertyType.getMaterialTypePropertyTypes());
        result.addAll(propertyType.getSampleTypePropertyTypes());
        return result;
    }
}