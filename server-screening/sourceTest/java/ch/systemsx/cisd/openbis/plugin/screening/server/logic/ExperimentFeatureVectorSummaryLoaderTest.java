/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.ScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentFeatureVectorSummaryLoaderTest extends AbstractServerTestCase
{
    private static final class ExperimentFeatureVectorSummaryLoaderWithNoCalculation extends ExperimentFeatureVectorSummaryLoader
    {
        public ExperimentFeatureVectorSummaryLoaderWithNoCalculation(Session session,
                IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
                IScreeningQuery screeningQuery, MaterialSummarySettings settings)
        {
            super(session, businessObjectFactory, daoFactory, screeningQuery, settings);
        }

        @Override
        ExperimentFeatureVectorSummary calculatedSummary(TechId experimentId,
                AnalysisProcedureCriteria analysisProcedureCriteria, ExperimentReference experiment)
        {
            return null;
        }
    }

    private static final String DATA_STORE_CODE = "DSS";

    private static final TechId EXPERIMENT_ID = new TechId(42);

    private IScreeningBusinessObjectFactory screeningBOFactory;

    private IScreeningQuery screeningQuery;

    private MaterialSummarySettings materialSummarySettings;

    @BeforeMethod
    public void beforeMethod()
    {
        screeningBOFactory = context.mock(IScreeningBusinessObjectFactory.class);
        screeningQuery = context.mock(IScreeningQuery.class);
        materialSummarySettings = ScreeningServer.createDefaultSettings();
    }

    @Test
    public void testLoadAnalysisSummaryFromReport()
    {
        Properties properties = new Properties();
        properties.setProperty(AnalysisSettings.KEY, "T1:viewer1");
        prepareLoadExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code("ds1").type("T1")
                        .property(ScreeningConstants.ANALYSIS_PROCEDURE, "extern")
                        .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).getDataSet();
        PhysicalDataSet ds2 = new DataSetBuilder().code("ds2").type("T2").getDataSet();
        prepareListDataSetsByExperiment(ds1, ds2);
        TableModel tabelModel =
                new TableModel(Collections.<TableModelColumnHeader> emptyList(),
                        Collections.<TableModelRow> emptyList());
        prepareCreateReport(tabelModel, "viewer1", ds1);

        ExperimentFeatureVectorSummary summary =
                createLoaderWithoutCalculation().loadExperimentFeatureVectors(EXPERIMENT_ID,
                        AnalysisProcedureCriteria.createFromCode("extern"),
                        new AnalysisSettings(properties));

        assertSame(tabelModel, summary.getTableModelOrNull());
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadAnalysisSummaryFromReportWithFileNotFound()
    {
        Properties properties = new Properties();
        properties.setProperty(AnalysisSettings.KEY, "T1:viewer1");
        prepareLoadExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code("ds1").type("T1")
                        .property(ScreeningConstants.ANALYSIS_PROCEDURE, "extern")
                        .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).getDataSet();
        prepareListDataSetsByExperiment(ds1);
        prepareCreateReportFails(new UserFailureException("Main TSV file could not be found."),
                "viewer1", ds1);

        try
        {
            createLoaderWithoutCalculation().loadExperimentFeatureVectors(EXPERIMENT_ID,
                    AnalysisProcedureCriteria.createFromCode("extern"),
                    new AnalysisSettings(properties));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Analysis summary for data set ds1 couldn't retrieved from "
                    + "Data Store Server. Reason: Main TSV file could not be found.\n" + "\n"
                    + "Hint: The file pattern for the data set type T1 might be wrong.",
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testLoadAnalysisSummaryFromReportWithOtherUserFailureException()
    {
        Properties properties = new Properties();
        properties.setProperty(AnalysisSettings.KEY, "T1:viewer1");
        prepareLoadExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code("ds1").type("T1")
                        .property(ScreeningConstants.ANALYSIS_PROCEDURE, "extern")
                        .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).getDataSet();
        prepareListDataSetsByExperiment(ds1);
        prepareCreateReportFails(new UserFailureException("Oohps!"), "viewer1", ds1);

        try
        {
            createLoaderWithoutCalculation().loadExperimentFeatureVectors(EXPERIMENT_ID,
                    AnalysisProcedureCriteria.createFromCode("extern"),
                    new AnalysisSettings(properties));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Analysis summary for data set ds1 couldn't retrieved from "
                    + "Data Store Server. Reason: Oohps!",
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testLoadAnalysisSummaryFromReportWithOtherException()
    {
        Properties properties = new Properties();
        properties.setProperty(AnalysisSettings.KEY, "T1:viewer1");
        prepareLoadExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code("ds1").type("T1")
                        .property(ScreeningConstants.ANALYSIS_PROCEDURE, "extern")
                        .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).getDataSet();
        prepareListDataSetsByExperiment(ds1);
        prepareCreateReportFails(new IllegalArgumentException("Oohps!"), "viewer1", ds1);

        try
        {
            createLoaderWithoutCalculation().loadExperimentFeatureVectors(EXPERIMENT_ID,
                    AnalysisProcedureCriteria.createFromCode("extern"),
                    new AnalysisSettings(properties));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Analysis summary for data set ds1 couldn't retrieved from "
                    + "Data Store Server. See server logs for the reason.",
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testLoadAnalysisForMoreThanOne()
    {
        Properties properties = new Properties();
        properties.setProperty(AnalysisSettings.KEY, "T1:viewer1");
        prepareLoadExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code("ds1").type("T1")
                        .property(ScreeningConstants.ANALYSIS_PROCEDURE, "extern").getDataSet();
        PhysicalDataSet ds2 =
                new DataSetBuilder().code("ds2").type("T1")
                        .property(ScreeningConstants.ANALYSIS_PROCEDURE, "extern").getDataSet();
        prepareListDataSetsByExperiment(ds1, ds2);

        ExperimentFeatureVectorSummary summary =
                createLoaderWithoutCalculation().loadExperimentFeatureVectors(EXPERIMENT_ID,
                        AnalysisProcedureCriteria.createFromCode("extern"),
                        new AnalysisSettings(properties));

        assertEquals(0, summary.getTableModelOrNull().getRows().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadAnalysisForWrongAnalysisProcedure()
    {
        Properties properties = new Properties();
        properties.setProperty(AnalysisSettings.KEY, "T1:viewer1");
        prepareLoadExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code("ds1").type("T1")
                        .property(ScreeningConstants.ANALYSIS_PROCEDURE, "extern")
                        .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).getDataSet();
        PhysicalDataSet ds2 = new DataSetBuilder().code("ds2").type("T2").getDataSet();
        prepareListDataSetsByExperiment(ds1, ds2);

        ExperimentFeatureVectorSummary summary =
                createLoaderWithoutCalculation().loadExperimentFeatureVectors(EXPERIMENT_ID,
                        AnalysisProcedureCriteria.createFromCode("intern"),
                        new AnalysisSettings(properties));

        assertEquals(0, summary.getTableModelOrNull().getRows().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadAnalysisForNoAnalysisSetting()
    {
        Properties properties = new Properties();
        prepareLoadExperiment();

        ExperimentFeatureVectorSummary summary = createLoaderWithoutCalculation().loadExperimentFeatureVectors(EXPERIMENT_ID,
                AnalysisProcedureCriteria.createFromCode("extern"), new AnalysisSettings(properties));

        assertEquals(null, summary);
        context.assertIsSatisfied();
    }

    private ExperimentFeatureVectorSummaryLoader createLoaderWithoutCalculation()
    {
        return new ExperimentFeatureVectorSummaryLoaderWithNoCalculation(session,
                screeningBOFactory, daoFactory, screeningQuery, materialSummarySettings);
    }

    private void prepareLoadExperiment()
    {
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).tryGetByTechId(EXPERIMENT_ID);
                    ExperimentPE experiment = new ExperimentPE();
                    experiment.setId(EXPERIMENT_ID.getId());
                    experiment.setPermId("123-" + EXPERIMENT_ID);
                    experiment.setExperimentType(new ExperimentTypePE());
                    ProjectPE project = new ProjectPE();
                    project.setSpace(new SpacePE());
                    experiment.setProject(project);
                    will(returnValue(experiment));
                }
            });
    }

    private void prepareListDataSetsByExperiment(final PhysicalDataSet... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));

                    one(datasetLister).listByExperimentTechId(EXPERIMENT_ID, true);
                    will(returnValue(Arrays.asList(dataSets)));
                }
            });
    }

    private void prepareCreateReport(final TableModel report, final String reportingPluginKey,
            final PhysicalDataSet dataSet)
    {
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createDataSetTable(session);
                    will(returnValue(dataSetTable));

                    one(dataSetTable).createReportFromDatasets(reportingPluginKey, DATA_STORE_CODE,
                            Arrays.asList(dataSet.getCode()));
                    will(returnValue(report));
                }
            });
    }

    private void prepareCreateReportFails(final Exception exception, final String reportingPluginKey,
            final PhysicalDataSet dataSet)
    {
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createDataSetTable(session);
                    will(returnValue(dataSetTable));

                    one(dataSetTable).createReportFromDatasets(reportingPluginKey, DATA_STORE_CODE,
                            Arrays.asList(dataSet.getCode()));
                    will(throwException(exception));
                }
            });
    }

}
