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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.CheckExperimentTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ListExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ShowExperiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ShowExperimentEditor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.columns.ExperimentRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericExperimentEditForm}.
 * 
 * @author Izabela Adamczyk
 */
public class GenericExperimentEditorTest extends AbstractGWTTestCase
{
    private static final String SIRNA_HCS = "SIRNA_HCS";

    private static final String CISD = "CISD";

    private static final String NEMO = "NEMO";

    private static final String DEFAULT = "DEFAULT";

    private static final String EXP1 = "EXP1";

    private static final String EXP11 = "EXP11";

    private static final String withGroup(String group, String project)
    {
        return project + " (" + group + ")";
    }

    private static final String identifier(String... elements)
    {
        StringBuilder sb = new StringBuilder();
        for (String s : elements)
        {
            sb.append("/");
            sb.append(s);
        }
        return sb.toString();
    }

    public final void testEditExperimentDescription()
    {
        prepareShowExperimentEditor(CISD, NEMO, SIRNA_HCS, EXP1);
        String description = "description from " + new Date();
        remoteConsole.prepare(new FillExperimentEditForm().addProperty(new PropertyField(
                "description", description)));
        remoteConsole.prepare(new ListExperiments(withGroup(CISD, NEMO), SIRNA_HCS));
        CheckExperimentTable table = new CheckExperimentTable();
        table.expectedRow(new ExperimentRow(EXP1).withUserPropertyCell("description", description));
        remoteConsole.prepare(table);
        launchTest();
    }

    public final void testEditExperimentProject()
    {
        String oldProject = NEMO;
        String newProject = DEFAULT;
        String experiment = EXP11;
        prepareShowExperimentEditor(CISD, oldProject, SIRNA_HCS, experiment);
        remoteConsole.prepare(new FillExperimentEditForm().changeProject(identifier(CISD,
                newProject)));
        remoteConsole.prepare(new ListExperiments(withGroup(CISD, newProject), SIRNA_HCS));
        CheckExperimentTable table = new CheckExperimentTable();
        table.expectedRow(new ExperimentRow(experiment));
        remoteConsole.prepare(table);
        launchTest();
    }

    private void prepareShowExperimentEditor(String group, final String project,
            final String experimentTypeName, final String code)
    {
        loginAndInvokeAction(ActionMenuKind.EXPERIMENT_MENU_BROWSE);
        remoteConsole.prepare(new ListExperiments(withGroup(group, project), experimentTypeName));
        remoteConsole.prepare(new ShowExperiment(code));
        remoteConsole.prepare(new ShowExperimentEditor());
    }

}
