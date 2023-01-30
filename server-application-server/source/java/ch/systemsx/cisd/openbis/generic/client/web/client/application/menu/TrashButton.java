package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

/** {@link Button} opening trash browser */
public class TrashButton extends Button
{
    public TrashButton(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ComponentProvider componentProvider)
    {
        setIcon(AbstractImagePrototype.create(viewContext.getImageBundle().getTrashIcon()));
        GWTUtils.setToolTip(this, viewContext.getMessage(Dict.TRASH_BUTTON_TOOLTIP));
        addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    DispatcherHelper.dispatchNaviEvent(componentProvider.getDeletionBrowser());
                }
            });
        setWidth(24);
    }
}