package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

/** {@link Button} displaying a {@link MessageBox} with a short help message. */
public class InfoButton extends Button
{
    public InfoButton(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        setIcon(AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon()));
        GWTUtils.setToolTip(this, viewContext.getMessage(Dict.INFO_BUTTON_TOOLTIP));
        addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    final String title = viewContext.getMessage(Dict.INFO_BOX_TITLE);
                    final String msg = viewContext.getMessage(Dict.INFO_BOX_MSG);
                    MessageBox.info(title, msg, null);
                }
            });
        setWidth(24);
    }
}