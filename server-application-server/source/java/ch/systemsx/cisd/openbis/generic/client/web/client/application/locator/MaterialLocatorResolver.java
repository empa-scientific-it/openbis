package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;

/**
 * ViewLocatorHandler for Material locators. We don't have permIds for materials so we need a different way of handling permlinks for them. We use
 * material code and type to identify material.
 * 
 * @author Piotr Buczek
 */
public class MaterialLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public MaterialLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(PermlinkUtilities.PERMLINK_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public boolean canHandleLocator(ViewLocator locator)
    {
        String entityKindValueOrNull = locator.tryGetEntity();
        return super.canHandleLocator(locator)
                && EntityKind.MATERIAL.name().equals(entityKindValueOrNull);
    }

    @Override
    public void locatorExists(ViewLocator locator, AsyncCallback<Void> callback)
    {
        try
        {
            MaterialIdentifier identifier = extractMaterialIdentifier(locator);
            viewContext.getCommonService().getMaterialInformationHolder(identifier,
                    new LocatorExistsCallback<IEntityInformationHolderWithPermId>(callback));
        } catch (UserFailureException e)
        {
            callback.onFailure(null);
        }
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        // If there is exactly one material matching given parameters open its detail view,
        // otherwise show an error message.
        assert (EntityKind.MATERIAL.name().equals(locator.tryGetEntity()));

        openInitialMaterialViewer(extractMaterialIdentifier(locator));
    }

    protected MaterialIdentifier extractMaterialIdentifier(ViewLocator locator)
    {
        String codeValueOrNull = locator.getParameters().get(PermlinkUtilities.CODE_PARAMETER_KEY);
        String materialTypeValueOrNull = locator.getParameters().get(PermlinkUtilities.TYPE_PARAMETER_KEY);
        checkRequiredParameter(codeValueOrNull, PermlinkUtilities.CODE_PARAMETER_KEY);
        checkRequiredParameter(materialTypeValueOrNull, PermlinkUtilities.TYPE_PARAMETER_KEY);

        String decodedMaterialCode = MaterialCodeUtils.decode(codeValueOrNull);
        return new MaterialIdentifier(decodedMaterialCode, materialTypeValueOrNull);
    }

    /**
     * Open the material details tab for the specified identifier.
     */
    protected void openInitialMaterialViewer(MaterialIdentifier identifier)
            throws UserFailureException
    {
        OpenEntityDetailsTabHelper.open(viewContext, identifier, false);
    }

}