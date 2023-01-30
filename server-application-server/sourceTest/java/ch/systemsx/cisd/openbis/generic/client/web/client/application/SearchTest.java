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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.RelatedDataSetGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;

/**
 * A {@link AbstractGWTTestCase} extension to test searching.
 * 
 * @author Christian Ribeaud
 * @author Piotr Buczek
 */
public class SearchTest extends AbstractGWTTestCase
{
    public final void testAllSearch()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new SearchCommand("EXP-REUSE"));

        final CheckTableCommand checkDatasetsTableCommand =
                createCheckMatchingEntitiesTableCommand(1, "/CISD/DEFAULT/EXP-REUSE");
        remoteConsole.prepare(checkDatasetsTableCommand);

        launchTest();
    }

    public final void testContainedSampleSearch()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new SearchCommand("MP:A10"));

        final CheckTableCommand checkDatasetsTableCommand =
                createCheckMatchingEntitiesTableCommand(1, "CISD:/MP:A10");
        remoteConsole.prepare(checkDatasetsTableCommand);

        launchTest();
    }

    public final void testExperimentSearch()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new SearchCommand("Experiment", "John"));

        final CheckTableCommand checkDatasetsTableCommand =
                createCheckMatchingEntitiesTableCommand(8, "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP11",
                        "/CISD/NEMO/EXP1");
        remoteConsole.prepare(checkDatasetsTableCommand);

        launchTest();
    }

    public final void testDataSetSearch()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new SearchCommand("Data Set", "TIFF"));

        final CheckTableCommand checkDatasetsTableCommand =
                createCheckMatchingEntitiesTableCommand(2, "20081105092158673-1",
                        "20081105092159111-1");
        remoteConsole.prepare(checkDatasetsTableCommand);

        launchTest();
    }

    private final static String SAMPLE_T1 = "CISD:/CISD/CP-TEST-1";

    private final static String SAMPLE_T2 = "CISD:/CISD/CP-TEST-2";

    private final static String SAMPLE_T3 = "CISD:/CISD/CP-TEST-3";

    private final static String EXP_T1 = "/CISD/NEMO/EXP-TEST-1";

    private final static String EXP_T2 = "/CISD/NEMO/EXP-TEST-2";

    private final static String EXP_T3 = "/CISD/NOE/EXP-TEST-2";

    private final static String DS_LOC1 = "a/1";

    private final static String DS_LOC2 = "a/2";

    private final static String DS_LOC3 = "a/3";

    public final void testShowDatasetsRelatedToSamples()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new SearchCommand("Sample", "cp-test-*"));

        final CheckTableCommand checkDatasetsTableCommand =
                createCheckMatchingEntitiesTableCommand(SAMPLE_T1, SAMPLE_T2, SAMPLE_T3);
        remoteConsole.prepare(checkDatasetsTableCommand);
        remoteConsole.prepare(showDatasetsRelatedToAllDisplayedEntities());
        remoteConsole.prepare(createCheckRelatedDatasetsTableCommand(DS_LOC1, DS_LOC2, DS_LOC3));

        launchTest();
    }

    public final void testShowDatasetsRelatedToExperiments()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new SearchCommand("Experiment", "exp-test-*"));

        final CheckTableCommand checkDatasetsTableCommand =
                createCheckMatchingEntitiesTableCommand(EXP_T1, EXP_T2, EXP_T3);
        remoteConsole.prepare(checkDatasetsTableCommand);
        remoteConsole.prepare(showDatasetsRelatedToAllDisplayedEntities());
        remoteConsole.prepare(createCheckRelatedDatasetsTableCommand(DS_LOC1, DS_LOC2, DS_LOC3));

        launchTest();
    }

    public final void testShowDatasetsRelatedToAll()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new SearchCommand("*test*")); // could also "*-test-*" gives 6 results

        final CheckTableCommand checkDatasetsTableCommand =
                createCheckMatchingEntitiesTableCommand(15, SAMPLE_T1, SAMPLE_T2, SAMPLE_T3,
                        EXP_T1, EXP_T2, EXP_T3);
        remoteConsole.prepare(checkDatasetsTableCommand);
        remoteConsole.prepare(showDatasetsRelatedToAllDisplayedEntities());
        // all datasets from EXP-REUSED are also in results
        remoteConsole.prepare(createCheckRelatedDatasetsTableCommand(DS_LOC1, DS_LOC2, DS_LOC3,
                "xml/result-8", "xml/result-9", "xml/result-10", "xml/result-11", "xml/result-12"));

        launchTest();
    }

    private CheckTableCommand createCheckMatchingEntitiesTableCommand(int size,
            String... identifiers)
    {
        final CheckTableCommand checkTableCommand =
                new CheckTableCommand(MatchingEntitiesPanel.GRID_ID);

        checkTableCommand.expectedSize(size);
        for (String identifier : identifiers)
        {
            checkTableCommand.expectedRow(createMatchingEntityRowWithIdentifier(identifier));
        }
        return checkTableCommand;
    }

    private CheckTableCommand createCheckMatchingEntitiesTableCommand(String... identifiers)
    {
        return createCheckMatchingEntitiesTableCommand(identifiers.length, identifiers);
    }

    private CheckTableCommand createCheckRelatedDatasetsTableCommand(String... locations)
    {
        final CheckTableCommand checkTableCommand =
                new CheckTableCommand(RelatedDataSetGrid.GRID_ID);

        checkTableCommand.expectedSize(locations.length);
        for (String location : locations)
        {
            checkTableCommand.expectedRow(createRelatedDatasetRowWithLocation(location));
        }
        return checkTableCommand;
    }

    private static Row createMatchingEntityRowWithIdentifier(String identifier)
    {
        return new Row().withCell(MatchingEntitiesPanelColumnIDs.IDENTIFIER, identifier);
    }

    private static Row createRelatedDatasetRowWithLocation(String location)
    {
        return new Row().withCell(ExternalDataGridColumnIDs.LOCATION, location);
    }

    private AbstractDefaultTestCommand showDatasetsRelatedToAllDisplayedEntities()
    {
        return new AbstractDefaultTestCommand()
            {
                @Override
                public void execute()
                {
                    GWTTestUtil
                            .clickButtonWithID(MatchingEntitiesPanel.SHOW_RELATED_DATASETS_BUTTON_ID);
                }
            };
    }

}
