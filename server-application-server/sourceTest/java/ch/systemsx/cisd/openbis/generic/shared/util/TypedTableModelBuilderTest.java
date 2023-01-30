/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class TypedTableModelBuilderTest extends AssertJUnit
{
    private static final class MockSerializable implements Serializable
    {
        private static final long serialVersionUID = 1L;
    }

    @Test
    public void testSimpleBuilding()
    {
        TypedTableModelBuilder<Serializable> builder = new TypedTableModelBuilder<Serializable>();
        builder.addColumn("A").withTitle("Alpha").hideByDefault();
        builder.addColumn("B").withDataType(DataTypeCode.REAL).withDefaultWidth(500);
        MockSerializable object = new MockSerializable();
        builder.addRow(object);
        builder.column("A").addString("hello");
        builder.column("B").addDouble(42.5);

        TypedTableModel<Serializable> model = builder.getModel();
        List<TableModelColumnHeader> headers = model.getHeader();
        assertHeadersOrder(headers, "A", "B");
        assertEquals("Alpha", headers.get(0).getTitle());
        assertEquals(true, headers.get(0).isHidden());
        assertEquals(150, headers.get(0).getDefaultColumnWidth());
        assertEquals(DataTypeCode.VARCHAR, headers.get(0).getDataType());
        assertEquals(null, headers.get(1).getTitle());
        assertEquals(false, headers.get(1).isHidden());
        assertEquals(500, headers.get(1).getDefaultColumnWidth());
        assertEquals(DataTypeCode.REAL, headers.get(1).getDataType());
        List<TableModelRowWithObject<Serializable>> rows = model.getRows();
        assertSame(object, rows.get(0).getObjectOrNull());
        assertEquals(new StringTableCell("hello"), rows.get(0).getValues().get(0));
        assertEquals(new DoubleTableCell(42.5), rows.get(0).getValues().get(1));
        assertEquals(2, rows.get(0).getValues().size());
        assertEquals(1, rows.size());
    }

    @Test
    public void testBuildingWithGroups()
    {
        TypedTableModelBuilder<Serializable> builder = new TypedTableModelBuilder<Serializable>();
        MockSerializable object = new MockSerializable();
        builder.addRow(object);
        builder.columnGroup("g1").column("a1").addDate(new Date(4711));
        builder.columnGroup("g2").column("a2").addDouble(2.125);
        builder.columnGroup("g1").column("b1").addString("hello");
        builder.columnGroup("g2").column("b2").addInteger(42L);

        TypedTableModel<Serializable> model = builder.getModel();
        List<TableModelColumnHeader> headers = model.getHeader();
        assertHeadersOrder(headers, "a1", "b1", "a2", "b2");
        assertEquals(DataTypeCode.TIMESTAMP, headers.get(0).getDataType());
        assertEquals(DataTypeCode.VARCHAR, headers.get(1).getDataType());
        assertEquals(DataTypeCode.REAL, headers.get(2).getDataType());
        assertEquals(DataTypeCode.INTEGER, headers.get(3).getDataType());
        List<TableModelRowWithObject<Serializable>> rows = model.getRows();
        assertEquals(new DateTableCell(4711), rows.get(0).getValues().get(0));
        assertEquals(new StringTableCell("hello"), rows.get(0).getValues().get(1));
        assertEquals(new DoubleTableCell(2.125), rows.get(0).getValues().get(2));
        assertEquals(new IntegerTableCell(42), rows.get(0).getValues().get(3));
        assertEquals(1, rows.size());
    }

    @Test
    public void testAddProperties()
    {
        TypedTableModelBuilder<Serializable> builder = new TypedTableModelBuilder<Serializable>();
        builder.addRow(new MockSerializable());
        IEntityProperty p1 = property("beta", "3.25", true, DataTypeCode.REAL);
        IEntityProperty p2 =
                property("alpha", "hello\nworld", false, DataTypeCode.MULTILINE_VARCHAR);
        IEntityProperty p3 = property("gamma", "hello", false, DataTypeCode.VARCHAR);
        builder.columnGroup("g").addProperties("MY-", Arrays.asList(p1, p2, p3));
        builder.addRow(new MockSerializable());
        IEntityProperty p4 = property("gamma", "hi", false, DataTypeCode.VARCHAR);
        IEntityProperty p5 = property("kappa", "42", false, DataTypeCode.INTEGER);
        builder.columnGroup("g").addProperties("MY-", Arrays.asList(p4, p5));

        TypedTableModel<Serializable> model = builder.getModel();
        List<TableModelColumnHeader> headers = model.getHeader();
        assertHeadersOrder(headers, "MY-USER-ALPHA", "MY-INTERN-BETA", "MY-USER-GAMMA",
                "MY-USER-KAPPA");
        assertEquals("alpha", headers.get(0).getTitle());
        assertEquals(DataTypeCode.MULTILINE_VARCHAR, headers.get(0).getDataType());
        assertEquals("beta", headers.get(1).getTitle());
        assertEquals(DataTypeCode.REAL, headers.get(1).getDataType());
        assertEquals("gamma", headers.get(2).getTitle());
        assertEquals(DataTypeCode.VARCHAR, headers.get(2).getDataType());
        assertEquals("kappa", headers.get(3).getTitle());
        assertEquals(DataTypeCode.INTEGER, headers.get(3).getDataType());
        List<TableModelRowWithObject<Serializable>> rows = model.getRows();
        assertEquals(new StringTableCell("hello\nworld"), rows.get(0).getValues().get(0));
        assertEquals(new DoubleTableCell(3.25), rows.get(0).getValues().get(1));
        assertEquals(new StringTableCell("hello"), rows.get(0).getValues().get(2));
        assertEquals(new StringTableCell(""), rows.get(0).getValues().get(3));
        assertEquals(new StringTableCell(""), rows.get(1).getValues().get(0));
        assertEquals(new StringTableCell(""), rows.get(1).getValues().get(1));
        assertEquals(new StringTableCell("hi"), rows.get(1).getValues().get(2));
        assertEquals(new IntegerTableCell(42), rows.get(1).getValues().get(3));

        assertEquals(2, rows.size());
    }

    @Test
    public void testAddPropertiesForUpdate()
    {
        TypedTableModelBuilder<Serializable> builder = new TypedTableModelBuilder<Serializable>();
        builder.addRow(new MockSerializable());
        IEntityProperty p1 = property("beta", "3.25", true, DataTypeCode.REAL);
        IEntityProperty p2 = property("alpha", "hello", false, DataTypeCode.MATERIAL);
        IEntityProperty p3 = property("gamma", "hello world", false, DataTypeCode.VARCHAR);
        IEntityProperty p4 = property("delta", "hi", false, DataTypeCode.CONTROLLEDVOCABULARY);
        IEntityProperty p5 =
                property("kappa", "2012-08-14 15:11:12", false, DataTypeCode.TIMESTAMP);
        builder.columnGroup("g").addPropertiesForUpdate(Arrays.asList(p1, p2, p3, p4, p5));

        TypedTableModel<Serializable> model = builder.getModel();
        List<TableModelColumnHeader> headers = model.getHeader();
        assertHeadersOrder(headers, "ALPHA", "$BETA", "DELTA", "GAMMA", "KAPPA");
        List<TableModelRowWithObject<Serializable>> rows = model.getRows();
        assertEquals(new StringTableCell("HELLO (TEST)"), rows.get(0).getValues().get(0));
        assertEquals(new StringTableCell("3.25"), rows.get(0).getValues().get(1));
        assertEquals(new StringTableCell("HI"), rows.get(0).getValues().get(2));
        assertEquals(new StringTableCell("hello world"), rows.get(0).getValues().get(3));
        assertEquals(new StringTableCell("2012-08-14 15:11:12"), rows.get(0).getValues().get(4));
        assertEquals(1, rows.size());
    }

    private IEntityProperty property(String key, String value, boolean managedInternally,
            DataTypeCode type)
    {
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        String normalizedKey = key.toUpperCase();
        propertyType.setSimpleCode(normalizedKey);
        propertyType.setManagedInternally(managedInternally);
        propertyType.setCode((managedInternally ? "$" : "") + normalizedKey);
        propertyType.setLabel(key);
        propertyType.setDataType(new DataType(type));
        property.setPropertyType(propertyType);
        if (type == DataTypeCode.CONTROLLEDVOCABULARY)
        {
            VocabularyTerm vocabularyTerm = new VocabularyTerm();
            vocabularyTerm.setCode(value.toUpperCase());
            vocabularyTerm.setLabel(value);
            property.setVocabularyTerm(vocabularyTerm);
        } else if (type == DataTypeCode.MATERIAL)
        {
            property.setMaterial(new MaterialBuilder().code(value.toUpperCase()).type("TEST")
                    .getMaterial());
        } else
        {
            property.setValue(value);
        }
        return property;
    }

    @Test
    public void testAddIntegerValueToColumn()
    {
        TypedTableModelBuilder<Serializable> builder = new TypedTableModelBuilder<Serializable>();
        MockSerializable object0 = new MockSerializable();
        builder.addRow(object0);
        builder.column("A").withTitle("Alpha").addInteger(42L);
        MockSerializable object1 = new MockSerializable();
        builder.addRow(object1);
        builder.column("A").addInteger(null);
        builder.addRow(null);
        builder.column("A").withTitle("a").addInteger(4711L);

        TypedTableModel<Serializable> model = builder.getModel();
        List<TableModelColumnHeader> headers = model.getHeader();
        assertHeadersOrder(headers, "A");
        assertEquals("a", headers.get(0).getTitle());
        assertEquals(DataTypeCode.INTEGER, headers.get(0).getDataType());
        List<TableModelRowWithObject<Serializable>> rows = model.getRows();
        assertSame(object0, rows.get(0).getObjectOrNull());
        assertEquals(new IntegerTableCell(42), rows.get(0).getValues().get(0));
        assertSame(object1, rows.get(1).getObjectOrNull());
        assertEquals(new StringTableCell(""), rows.get(1).getValues().get(0));
        assertSame(null, rows.get(2).getObjectOrNull());
        assertEquals(new IntegerTableCell(4711), rows.get(2).getValues().get(0));
        assertEquals(3, rows.size());
    }

    @Test
    public void testAddDoubleValueToColumn()
    {
        TypedTableModelBuilder<Serializable> builder = new TypedTableModelBuilder<Serializable>();
        MockSerializable object0 = new MockSerializable();
        builder.addRow(object0);
        builder.column("A").withTitle("Alpha").addDouble(4.25);
        MockSerializable object1 = new MockSerializable();
        builder.addRow(object1);
        builder.column("A").addDouble(null);
        builder.addRow(null);
        builder.column("A").withTitle("a").addDouble(4711.5);

        TypedTableModel<Serializable> model = builder.getModel();
        List<TableModelColumnHeader> headers = model.getHeader();
        assertHeadersOrder(headers, "A");
        assertEquals("a", headers.get(0).getTitle());
        assertEquals(DataTypeCode.REAL, headers.get(0).getDataType());
        List<TableModelRowWithObject<Serializable>> rows = model.getRows();
        assertSame(object0, rows.get(0).getObjectOrNull());
        assertEquals(new DoubleTableCell(4.25), rows.get(0).getValues().get(0));
        assertSame(object1, rows.get(1).getObjectOrNull());
        assertEquals(new StringTableCell(""), rows.get(1).getValues().get(0));
        assertSame(null, rows.get(2).getObjectOrNull());
        assertEquals(new DoubleTableCell(4711.5), rows.get(2).getValues().get(0));
        assertEquals(3, rows.size());
    }

    @Test
    public void testRowsWithEmptyCells()
    {
        TypedTableModelBuilder<Serializable> builder = new TypedTableModelBuilder<Serializable>();
        builder.addRow(null);
        builder.column("A").addString("a");
        builder.addRow(null);
        builder.column("B").addString("b");
        TypedTableModel<Serializable> model = builder.getModel();

        assertHeadersOrder(model.getHeader(), "A", "B");
        List<TableModelRowWithObject<Serializable>> rows = model.getRows();
        assertEquals("[a, ]", rows.get(0).getValues().toString());
        assertEquals("[, b]", rows.get(1).getValues().toString());
        assertEquals(2, rows.size());
    }

    private void assertHeadersOrder(List<TableModelColumnHeader> headers, String... headerIds)
    {
        List<String> actualHeaderIds = new ArrayList<String>();
        for (int i = 0; i < headerIds.length; i++)
        {
            TableModelColumnHeader header = headers.get(i);
            assertEquals(i, header.getIndex());
            actualHeaderIds.add(header.getId());
        }
        assertEquals(Arrays.asList(headerIds).toString(), actualHeaderIds.toString());
    }
}
