package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor.MATERIAL_REPLICA_SUMMARY_EXPERIMENT_PERM_ID_KEY;
import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor.MATERIAL_CODE_KEY;
import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor.MATERIAL_TYPE_CODE_KEY;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.MaterialReplicaSummaryViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;

/**
 * Locator resolver for material replica summary view.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialReplicaSummaryResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public MaterialReplicaSummaryResolver(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(ScreeningLinkExtractor.MATERIAL_REPLICA_SUMMARY_ACTION);
        this.viewContext = viewContext;
    }

    public void resolve(final ViewLocator locator) throws UserFailureException
    {
        String experimentPermId =
                getMandatoryParameter(locator, MATERIAL_REPLICA_SUMMARY_EXPERIMENT_PERM_ID_KEY);

        String materialCode =
                getMandatoryParameter(locator, MATERIAL_CODE_KEY);

        String materialTypeCode =
                getMandatoryParameter(locator, MATERIAL_TYPE_CODE_KEY);

        MaterialIdentifier materialIdentifier =
                new MaterialIdentifier(materialCode, materialTypeCode);

        MaterialReplicaSummaryViewer.openTab(viewContext, experimentPermId, materialIdentifier);

    }
}