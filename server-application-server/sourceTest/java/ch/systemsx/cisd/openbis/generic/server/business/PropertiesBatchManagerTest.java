/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.TestJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertiesBean;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.ExperimentTypePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SampleTypePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;

/**
 * @author felmer
 */
public class PropertiesBatchManagerTest extends AssertJUnit
{
    private static final String MANAGED_SUBCOLUMNS = "MANAGED_SUBCOLUMNS";

    private static final String MANAGED_NO_SUBCOLUMNS_BUT_UPDATE =
            "MANAGED-NO-SUBCOLUMNS-BUT-UPDATE";

    private static final String MANAGED_NO_SUBCOLUMNS_NO_UPDATE = "MANAGED-NO-SUBCOLUMNS";

    private static final String MANAGED_ACCESS_OTHER_COLUMNS = "MANAGED_ACCESS_OTHER_COLUMNS";

    private static final String UN_MANAGED = "UN-MANAGED";

    @Test
    public void testHappyCases()
    {
        ExperimentTypePEBuilder builder = new ExperimentTypePEBuilder();
        builder.assign(UN_MANAGED);
        builder.assign(MANAGED_NO_SUBCOLUMNS_NO_UPDATE).script(ScriptType.MANAGED_PROPERTY, "");
        builder.assign(MANAGED_NO_SUBCOLUMNS_BUT_UPDATE).script(
                ScriptType.MANAGED_PROPERTY,
                "def updateFromBatchInput(columnValues):\n"
                        + "  if columnValues.get('') is None:\n" + "    return\n"
                        + "  property.setValue(columnValues.get('') + ' alpha')");
        builder.assign(MANAGED_SUBCOLUMNS).script(
                ScriptType.MANAGED_PROPERTY,
                "def batchColumnNames():\n  return ['1', '2']\n"
                        + "def updateFromBatchInput(columnValues):\n"
                        + "  if columnValues.get('1') is None:\n" + "    return\n"
                        + "  if columnValues.get('2') is None:\n" + "    return\n"
                        + "  property.setValue(columnValues.get('1') + columnValues.get('2'))");
        NewBasicExperiment e1 = new NewBasicExperiment();
        PropertyBuilder p1 = new PropertyBuilder(UN_MANAGED).value("hello");
        PropertyBuilder p2 = new PropertyBuilder(MANAGED_NO_SUBCOLUMNS_NO_UPDATE).value("hi");
        addProperties(e1, p1, p2);
        NewBasicExperiment e2 = new NewBasicExperiment();
        PropertyBuilder p3 = new PropertyBuilder(MANAGED_NO_SUBCOLUMNS_BUT_UPDATE).value("hi");
        PropertyBuilder p4 = new PropertyBuilder(MANAGED_SUBCOLUMNS + ":1").value("ab");
        PropertyBuilder p5 = new PropertyBuilder(MANAGED_SUBCOLUMNS + ":2").value("12");
        addProperties(e2, p3, p4, p5);

        new PropertiesBatchManager(new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool())).manageProperties(
                builder.getExperimentTypePE(), Arrays.asList(e1, e2), null);

        assertProperties("UN-MANAGED:hello, MANAGED-NO-SUBCOLUMNS:hi", e1);
        assertProperties("MANAGED-NO-SUBCOLUMNS-BUT-UPDATE:hi alpha, MANAGED_SUBCOLUMNS:ab12", e2);
    }

    @Test
    public void testAccessToNonManagedColumns()
    {
        ExperimentTypePEBuilder builder = new ExperimentTypePEBuilder();
        builder.assign(UN_MANAGED);
        builder.assign(MANAGED_ACCESS_OTHER_COLUMNS).script(
                ScriptType.MANAGED_PROPERTY,
                "def updateFromBatchInput(columnValues):\n"
                        + "  property.setValue(columnValues.get(originalColumnNameBindingKey('"
                        + UN_MANAGED + "')) + ' ' + columnValues.get(''))");
        NewBasicExperiment e1 = new NewBasicExperiment();
        PropertyBuilder p1 = new PropertyBuilder(UN_MANAGED).value("hello");
        PropertyBuilder p2 = new PropertyBuilder(MANAGED_ACCESS_OTHER_COLUMNS).value("ptr");
        addProperties(e1, p1, p2);

        new PropertiesBatchManager(new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool())).manageProperties(
                builder.getExperimentTypePE(), Arrays.asList(e1), null);

        assertProperties("UN-MANAGED:hello, MANAGED_ACCESS_OTHER_COLUMNS:hello ptr", e1);
    }

    @Test
    public void testScriptErrorWhenExecutingUpdateFromBatchInput()
    {
        SampleTypePEBuilder builder = new SampleTypePEBuilder();
        builder.assign(MANAGED_NO_SUBCOLUMNS_BUT_UPDATE).script(
                ScriptType.MANAGED_PROPERTY,
                "def updateFromBatchInput(columnValues):\n"
                        + "  property.setValue(str(int(columnValues.get('')) + 42))");
        NewSample s1 = new NewSample();
        PropertyBuilder p1 = new PropertyBuilder(MANAGED_NO_SUBCOLUMNS_BUT_UPDATE).value("1");
        addProperties(s1, p1);
        NewSample s2 = new NewSample();
        PropertyBuilder p2 = new PropertyBuilder(MANAGED_NO_SUBCOLUMNS_BUT_UPDATE).value("two");
        addProperties(s2, p2);

        try
        {
            new PropertiesBatchManager(new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool())).manageProperties(
                    builder.getSampleType(), Arrays.asList(s1, s2), null);
        } catch (UserFailureException ufe)
        {
            assertEquals(
                    "Script malfunction in 1 out of 2 rows.\n"
                            + "Row 2 has failed due to the property 'MANAGED-NO-SUBCOLUMNS-BUT-UPDATE' causing a malfuction "
                            + "in the script (name = 'null', registrator = '')\n"
                            + "A detailed error report has been sent to your system administrator.",
                    ufe.getMessage());
        }

        assertProperties("MANAGED-NO-SUBCOLUMNS-BUT-UPDATE:43", s1);
    }

    @Test
    public void testScriptThrowingValidationException()
    {
        ExperimentTypePEBuilder builder = new ExperimentTypePEBuilder();
        builder.assign(MANAGED_NO_SUBCOLUMNS_BUT_UPDATE).script(
                ScriptType.MANAGED_PROPERTY,
                "def updateFromBatchInput(columnValues):\n"
                        + "  raise ValidationException('Oops!')");
        NewBasicExperiment e1 = new NewBasicExperiment();
        PropertyBuilder p1 = new PropertyBuilder(MANAGED_NO_SUBCOLUMNS_BUT_UPDATE).value("hello");
        addProperties(e1, p1);

        try
        {
            new PropertiesBatchManager(new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool())).manageProperties(
                    builder.getExperimentTypePE(), Arrays.asList(e1), null);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Error in row 1: Oops!", ex.getMessage());
        }

    }

    private void assertProperties(String expectedProperties, IPropertiesBean propertiesBean)
    {
        Set<String> expected = new HashSet<String>();
        for (String prop : expectedProperties.split(","))
        {
            expected.add(prop.trim());
        }

        Set<String> actual = new HashSet<String>();
        IEntityProperty[] properties = propertiesBean.getProperties();
        for (IEntityProperty property : properties)
        {
            String propString = property.getPropertyType().getCode() + ":" + property.getValue();
            actual.add(propString);
        }
        assertEquals(expected, actual);
    }

    private void addProperties(IPropertiesBean propertiesBean, PropertyBuilder... builders)
    {
        IEntityProperty[] properties = new IEntityProperty[builders.length];
        for (int i = 0; i < properties.length; i++)
        {
            properties[i] = builders[i].getProperty();
        }
        propertiesBean.setProperties(properties);
    }
}
