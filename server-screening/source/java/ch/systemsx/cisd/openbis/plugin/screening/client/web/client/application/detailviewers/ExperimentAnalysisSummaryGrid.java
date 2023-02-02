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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs.RANK_PREFIX;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ColumnIDUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Constants;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.AnalysisProcedureChooser.IAnalysisProcedureSelectionListener;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs;

/**
 * A grid showing feature vector summary for an experiment.
 * 
 * @author Kaloyan Enimanev
 */
public class ExperimentAnalysisSummaryGrid extends TypedTableGrid<MaterialFeatureVectorSummary>
        implements IAnalysisProcedureSelectionListener
{
    private static final String PREFIX = GenericConstants.ID_PREFIX
            + "experiment-feature-vector-summary";

    private static final String BROWSER_ID = PREFIX + "_main";

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final IEntityInformationHolderWithIdentifier experiment;

    private final boolean restrictGlobalScopeLinkToProject;

    private AnalysisProcedureCriteria analysisProcedureCriteria;

    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithIdentifier experiment,
            boolean restrictGlobalScopeLinkToProject,
            AnalysisProcedureCriteria initialAnalysisProcedureOrNull)
    {
        return new ExperimentAnalysisSummaryGrid(viewContext, experiment,
                restrictGlobalScopeLinkToProject, initialAnalysisProcedureOrNull)
                .asDisposableWithoutToolbar();
    }

    private ICellListenerAndLinkGenerator<MaterialFeatureVectorSummary> createMaterialReplicaSummaryLinkGenerator()
    {
        return new ICellListenerAndLinkGenerator<MaterialFeatureVectorSummary>()
            {

                @Override
                public void handle(TableModelRowWithObject<MaterialFeatureVectorSummary> rowItem,
                        boolean specialKeyPressed)
                {
                    IEntityInformationHolderWithPermId material =
                            rowItem.getObjectOrNull().getMaterial();
                    openMaterialDetailViewer(material);
                }

                @Override
                public String tryGetLink(MaterialFeatureVectorSummary entity,
                        ISerializableComparable comparableValue)
                {
                    IEntityInformationHolder material = entity.getMaterial();
                    return ScreeningLinkExtractor.createMaterialDetailsLink(material,
                            getExperimentAsSearchCriteria());
                }
            };
    }

    private void openMaterialDetailViewer(IEntityInformationHolderWithPermId material)
    {
        assert analysisProcedureCriteria != null : "analysisProcedureCriteria is not set yet, "
                + "it should not happen because this field is set before the grid refreshes";
        ClientPluginFactory.openImagingMaterialViewer(material, getExperimentAsSearchCriteria(),
                analysisProcedureCriteria, false, screeningViewContext);
    }

    private ExperimentSearchCriteria getExperimentAsSearchCriteria()
    {
        return ExperimentSearchCriteria.createExperiment(experiment,
                restrictGlobalScopeLinkToProject);
    }

    ExperimentAnalysisSummaryGrid(IViewContext<IScreeningClientServiceAsync> viewContext,
            final IEntityInformationHolderWithIdentifier experiment,
            boolean restrictGlobalScopeLinkToProject,
            AnalysisProcedureCriteria initialAnalysisProcedureOrNull)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID,
                initialAnalysisProcedureOrNull != null,
                DisplayTypeIDGenerator.EXPERIMENT_FEATURE_VECTOR_SUMMARY_SECTION);
        this.screeningViewContext = viewContext;
        this.experiment = experiment;
        this.restrictGlobalScopeLinkToProject = restrictGlobalScopeLinkToProject;
        this.analysisProcedureCriteria = initialAnalysisProcedureOrNull;

        ICellListenerAndLinkGenerator<MaterialFeatureVectorSummary> linkGenerator =
                createMaterialReplicaSummaryLinkGenerator();
        registerListenerAndLinkGenerator(FeatureVectorSummaryGridColumnIDs.MATERIAL_ID,
                linkGenerator);
        String detailsLinkPropertyTypeName =
                screeningViewContext
                        .getPropertyOrNull(Constants.MATERIAL_DETAILS_PROPERTY_TYPE_KEY);
        if (detailsLinkPropertyTypeName != null)
        {
            String detailsLinkPropertyColumnId =
                    ColumnIDUtils.getColumnIdForProperty(
                            FeatureVectorSummaryGridColumnIDs.MATERIAL_PROPS_GROUP,
                            detailsLinkPropertyTypeName);
            registerListenerAndLinkGenerator(detailsLinkPropertyColumnId, linkGenerator);
        }

        setBorders(true);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<MaterialFeatureVectorSummary>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<MaterialFeatureVectorSummary>> callback)
    {
        screeningViewContext.getService().listExperimentFeatureVectorSummary(resultSetConfig,
                new TechId(experiment), analysisProcedureCriteria, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<MaterialFeatureVectorSummary>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        screeningViewContext.getService().prepareExportFeatureVectorSummary(exportCriteria,
                callback);
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        String id = columnID;
        if (columnID.startsWith(RANK_PREFIX))
        {
            id = RANK_PREFIX;
        }
        return Dict.EXPERIMENT_FEATURE_VECTOR_SUMMARY_SECTION.toLowerCase() + "_"
                + id.toUpperCase();
    }

    public void dispose()
    {
        asDisposableWithoutToolbar().dispose();
    }

    //
    // IAnalysisProcedureSelectionListener
    //
    @Override
    public void analysisProcedureSelected(AnalysisProcedureCriteria selectedProcedureCriteria)
    {
        this.analysisProcedureCriteria = selectedProcedureCriteria;
        refresh(true);
    }
}
