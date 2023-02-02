package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.MaterialCodeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLListEncoder;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.WellSearchGrid;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCodesCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCriteria;

/**
 * Locator resolver for Well Search which displays wells containing specified materials optionally restricted to one experiment.
 * 
 * @author Tomasz Pylak
 */
public class WellSearchLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public WellSearchLocatorResolver(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(ScreeningLinkExtractor.WELL_SEARCH_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        String experimentPermId =
                getOptionalParameter(locator,
                        ScreeningLinkExtractor.WELL_SEARCH_EXPERIMENT_PERM_ID_PARAMETER_KEY);
        String projectCode = getOptionalParameter(locator, ScreeningLinkExtractor.PROJECT_CODE_KEY);
        String spaceCode = getOptionalParameter(locator, ScreeningLinkExtractor.SPACE_CODE_KEY);

        String materialCodesOrProperties =
                getMandatoryParameter(locator,
                        ScreeningLinkExtractor.WELL_SEARCH_MATERIAL_ITEMS_PARAMETER_KEY);
        String materialTypeCodes =
                getMandatoryParameter(locator,
                        ScreeningLinkExtractor.WELL_SEARCH_MATERIAL_TYPES_PARAMETER_KEY);
        boolean exactMatchOnly =
                getMandatoryBooleanParameter(locator,
                        ScreeningLinkExtractor.WELL_SEARCH_IS_EXACT_PARAMETER_KEY);

        boolean showCombinedResults =
                getOptionalBooleanParameter(locator,
                        ScreeningLinkExtractor.WELL_SEARCH_SHOW_COMBINED_RESULTS_PARAMETER_KEY,
                        ScreeningLinkExtractor.WELL_SEARCH_SHOW_COMBINED_RESULTS_DEFAULT);
        String nothingFoundRedirectionUrlOrNull =
                getOptionalParameter(locator,
                        ScreeningLinkExtractor.WELL_SEARCH_NOTHING_FOUND_REDIRECTION_URL_KEY);

        String[] materialCodes = MaterialCodeUtils.decodeList(materialCodesOrProperties);
        MaterialSearchCodesCriteria materialCodesCriteria =
                new MaterialSearchCodesCriteria(materialCodes, decodeList(materialTypeCodes),
                        exactMatchOnly);

        MaterialSearchCriteria materialSearchCriteria =
                MaterialSearchCriteria.create(materialCodesCriteria);

        AnalysisProcedureCriteria analysisProcedureCriteria =
                ScreeningResolverUtils.extractAnalysisProcedureCriteria(locator);

        if (StringUtils.isBlank(experimentPermId))
        {
            ExperimentSearchCriteria criteria;
            if (StringUtils.isBlank(projectCode) || StringUtils.isBlank(spaceCode))
            {
                criteria = ExperimentSearchCriteria.createAllExperiments();
            } else
            {
                criteria =
                        ExperimentSearchCriteria
                                .createAllExperimentsForProject(new BasicProjectIdentifier(
                                        spaceCode, projectCode));
            }
            WellSearchGrid.openTab(viewContext, criteria, materialSearchCriteria,
                    analysisProcedureCriteria, showCombinedResults,
                    nothingFoundRedirectionUrlOrNull);
        } else
        {
            WellSearchGrid.openTab(viewContext, experimentPermId, materialSearchCriteria,
                    analysisProcedureCriteria, showCombinedResults,
                    nothingFoundRedirectionUrlOrNull);
        }

    }

    private String[] decodeList(String itemsList)
    {
        return URLListEncoder.decodeItemList(itemsList);
    }
}