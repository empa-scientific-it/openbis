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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PersonBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentProviderTest extends AbstractProviderTest
{
    @Test
    public void test()
    {
        ListExperimentsCriteria criteria = new ListExperimentsCriteria();
        final ExperimentType experimentType = new ExperimentType();
        criteria.setExperimentType(experimentType);
        Project project = new Project();
        project.setCode("project");
        Space space = new Space();
        space.setCode("space");
        project.setSpace(space);
        criteria.setProject(project);
        final ExperimentBuilder e1 =
                new ExperimentBuilder()
                        .code("E1")
                        .identifier("/A/B/E1")
                        .date(new Date(1000123456))
                        .modificationDate(new Date(1000124456))
                        .markDeleted()
                        .permID("123-45")
                        .type("1")
                        .registrator(
                                new PersonBuilder().name("Albert", "Einstein").userID("ae")
                                        .getPerson()).property("NUMBER", "42");
        context.checking(new Expectations()
            {
                {
                    one(server).listExperimentTypes(SESSION_TOKEN);
                    ExperimentType type1 =
                            new ExperimentTypeBuilder().code("1")
                                    .propertyType("TEXT", "text", DataTypeCode.MULTILINE_VARCHAR)
                                    .propertyType("NUMBER", "number", DataTypeCode.REAL)
                                    .getExperimentType();
                    will(returnValue(Arrays.asList(type1)));

                    one(server).listExperiments(SESSION_TOKEN, experimentType,
                            new ProjectIdentifier("space", "project"));
                    will(returnValue(Arrays.asList(e1.getExperiment())));
                }
            });

        TypedTableModel<Experiment> tableModel =
                new ExperimentProvider(server, SESSION_TOKEN, criteria).getTableModel(100);
        assertEquals(
                "[CODE, EXPERIMENT_TYPE, EXPERIMENT_IDENTIFIER, SPACE, PROJECT, PROJECT_IDENTIFIER, "
                        + "REGISTRATOR, MODIFIER, REGISTRATION_DATE, MODIFICATION_DATE, IS_DELETED, PERM_ID, "
                        + "SHOW_DETAILS_LINK, METAPROJECTS, property-USER-NUMBER, property-USER-TEXT]",
                getHeaderIDs(tableModel).toString());
        assertEquals(
                "[null, VARCHAR, null, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, TIMESTAMP, "
                        + "TIMESTAMP, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, MULTILINE_VARCHAR]",
                getHeaderDataTypes(tableModel).toString());
        assertEquals(
                "[EXPERIMENT, null, EXPERIMENT, null, null, null, null, null, null, null, "
                        + "null, null, null, null, null, null]", getHeaderEntityKinds(tableModel)
                        .toString());
        List<TableModelRowWithObject<Experiment>> rows = tableModel.getRows();
        assertSame(e1.getExperiment(), rows.get(0).getObjectOrNull());
        assertEquals(
                "[E1, 1, /A/B/E1, A, B, , Einstein, Albert, , Mon Jan 12 14:48:43 CET 1970, "
                        + "Mon Jan 12 14:48:44 CET 1970, yes, 123-45, , , 42, ]", rows.get(0)
                        .getValues().toString());
        context.assertIsSatisfied();
    }
}
