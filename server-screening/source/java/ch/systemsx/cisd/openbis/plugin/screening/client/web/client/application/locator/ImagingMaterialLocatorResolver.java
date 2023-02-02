package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.MaterialLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.dto.ExperimentIdentifierSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;

/**
 * Material detail view for screening materials. Overrides {@link MaterialLocatorResolver}.
 * 
 * @author Piotr Buczek
 */
public class ImagingMaterialLocatorResolver extends MaterialLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private static final String EMPTY_EXPERIMENT_IDENTIFIER = "";

    public ImagingMaterialLocatorResolver(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext());
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        // If there is exactly one material matching given parameters open its detail view,
        // otherwise show an error message.
        assert (EntityKind.MATERIAL.name().equals(locator.tryGetEntity()));

        ExperimentIdentifierSearchCriteria experimentCriteriaOrNull =
                tryGetExperimentIdentifierSearchCriteria(locator);

        AnalysisProcedureCriteria analysisProcedureCriteria =
                ScreeningResolverUtils.extractAnalysisProcedureCriteria(locator);

        boolean computeRanks =
                getOptionalBooleanParameter(locator, ScreeningLinkExtractor.COMPUTE_RANKS_KEY,
                        false);

        openInitialMaterialViewer(extractMaterialIdentifier(locator), experimentCriteriaOrNull,
                analysisProcedureCriteria, computeRanks);
    }

    private static ExperimentIdentifierSearchCriteria tryGetExperimentIdentifierSearchCriteria(
            ViewLocator locator)
    {
        // one experiment
        String experimentIdentifierOrNull =
                getOptionalParameter(locator,
                        ScreeningLinkExtractor.MATERIAL_DETAIL_EXPERIMENT_IDENT_PARAMETER_KEY);
        boolean experimentIdentifierSpecified =
                locator.getParameters().containsKey(
                        ScreeningLinkExtractor.MATERIAL_DETAIL_EXPERIMENT_IDENT_PARAMETER_KEY);

        if (experimentIdentifierSpecified)
        {
            if (experimentIdentifierOrNull == null
                    || experimentIdentifierOrNull.trim().length() == 0)
            {
                experimentIdentifierOrNull = EMPTY_EXPERIMENT_IDENTIFIER;
            }
            boolean restrictGlobalSearchToProject =
                    getOptionalBooleanParameter(locator,
                            ScreeningLinkExtractor.RESTRICT_GLOBAL_SEARCH_TO_PROJECT, false);
            return ExperimentIdentifierSearchCriteria.createExperimentScope(
                    experimentIdentifierOrNull, restrictGlobalSearchToProject);
        }

        // project
        String space = getOptionalParameter(locator, ScreeningLinkExtractor.SPACE_CODE_KEY);
        String project = getOptionalParameter(locator, ScreeningLinkExtractor.PROJECT_CODE_KEY);
        if (StringUtils.isBlank(space) == false && StringUtils.isBlank(project) == false)
        {
            return ExperimentIdentifierSearchCriteria
                    .createProjectScope(new BasicProjectIdentifier(space, project));
        }
        // all experiments
        boolean searchAllExp =
                getOptionalBooleanParameter(
                        locator,
                        ScreeningLinkExtractor.MATERIAL_DETAIL_SEARCH_ALL_EXPERIMENTS_PARAMETER_KEY,
                        false);
        if (searchAllExp)
        {
            return ExperimentIdentifierSearchCriteria.createSearchAll();
        }
        return null;
    }

    /**
     * Open the gene material details tab for the specified identifier. Optionally select experiment in the viewer.
     * 
     * @param analysisProcedureCriteria
     * @param computeRanks
     */
    protected void openInitialMaterialViewer(MaterialIdentifier identifier,
            ExperimentIdentifierSearchCriteria experimentCriteriaOrNull,
            AnalysisProcedureCriteria analysisProcedureCriteria, boolean computeRanks)
            throws UserFailureException
    {
        viewContext.getCommonService().getMaterialInformationHolder(
                identifier,
                new OpenEntityDetailsTabCallback(viewContext, experimentCriteriaOrNull,
                        analysisProcedureCriteria, computeRanks));
    }

    private static class OpenEntityDetailsTabCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {
        private final ExperimentIdentifierSearchCriteria scopeOrNull;

        private final AnalysisProcedureCriteria analysisProcedureCriteria;

        private boolean computeRanks;

        private OpenEntityDetailsTabCallback(
                final IViewContext<IScreeningClientServiceAsync> viewContext,
                ExperimentIdentifierSearchCriteria scopeOrNull,
                AnalysisProcedureCriteria analysisProcedureCriteria, boolean computeRanks)
        {
            super(viewContext);
            this.scopeOrNull = scopeOrNull;
            this.analysisProcedureCriteria = analysisProcedureCriteria;
            this.computeRanks = computeRanks;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Opens the tab with <var>result</var> entity details.
         */
        @Override
        protected final void process(final IEntityInformationHolderWithPermId material)
        {
            if (scopeOrNull == null)
            {
                openImagingMaterialViewer(material, null);
            } else
            {
                String experimentIdentifier = scopeOrNull.tryGetExperimentIdentifier();
                BasicProjectIdentifier project = scopeOrNull.tryGetProject();
                if (experimentIdentifier != null)
                {
                    if (EMPTY_EXPERIMENT_IDENTIFIER.equals(experimentIdentifier))
                    {
                        openImagingMaterialViewer(material,
                                ExperimentSearchCriteria.createExperiment(
                                        SingleExperimentSearchCriteria.EMPTY_CRITERIA,
                                        scopeOrNull.getRestrictGlobalSearchLinkToProject()));
                    } else
                    {
                        fetchExperimentAndShowLocations(material, experimentIdentifier);
                    }

                } else if (project != null)
                {
                    openImagingMaterialViewer(material,
                            ExperimentSearchCriteria.createAllExperimentsForProject(project));
                } else
                {
                    openImagingMaterialViewer(material,
                            ExperimentSearchCriteria.createAllExperiments());
                }
            }
        }

        private void fetchExperimentAndShowLocations(
                final IEntityInformationHolderWithPermId material, String experimentIdentifier)
        {
            final IViewContext<IScreeningClientServiceAsync> context = getViewContext();
            context.getCommonService().getExperimentInfo(experimentIdentifier,
                    new AbstractAsyncCallback<Experiment>(context)
                        {
                            @Override
                            protected void process(Experiment experiment)
                            {
                                ExperimentSearchCriteria experimentCriteria =
                                        ExperimentSearchCriteria.createExperiment(experiment,
                                                scopeOrNull.getRestrictGlobalSearchLinkToProject());
                                openImagingMaterialViewer(material, experimentCriteria);
                            }

                        });
        }

        private final void openImagingMaterialViewer(
                final IEntityInformationHolderWithPermId material,
                final ExperimentSearchCriteria experimentSearchCriteriaOrNull)
        {
            ClientPluginFactory.openImagingMaterialViewer(material, experimentSearchCriteriaOrNull,
                    analysisProcedureCriteria, computeRanks, getViewContext());
        }

        @SuppressWarnings("unchecked")
        private IViewContext<IScreeningClientServiceAsync> getViewContext()
        {
            return (IViewContext<IScreeningClientServiceAsync>) viewContext;
        }
    }

}