package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToggleToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

/**
 * Content panel which allows to choose which contained panels should be visible and uses the whole
 * space available to show them.
 * 
 * @author Izabela Adamczyk
 */
public class SectionsPanel extends ContentPanel
{
    List<SectionElement> elements = new ArrayList<SectionElement>();

    private ToolBar toolbar;

    private final boolean withShowHide;

    public SectionsPanel()
    {
        this(true);
    }

    public SectionsPanel(boolean withShowHide)
    {
        this.withShowHide = withShowHide;
        setLayout(new FillLayout());
        toolbar = new ToolBar();
        setHeaderVisible(false);
        setTopComponent(toolbar);
    }

    public void addPanel(final ContentPanel panel)
    {
        final SectionElement element = new SectionElement(panel, withShowHide);
        element.getButton().addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    removeAll();
                    for (SectionElement el : elements)
                    {
                        if (el.getButton().isPressed())
                        {
                            internalAdd(el);
                        }
                    }
                    layout();
                }
            });
        elements.add(element);
        addToToolbar(element.getButton());
        internalAdd(element);
    }

    private void addToToolbar(ToggleToolItem bb)
    {
        toolbar.add(bb);
    }

    public void removePanel(final ContentPanel panel)
    {
        int index = elements.indexOf(panel);
        if (index > -1)
        {
            internalRemove(panel);
            elements.remove(index);
            toolbar.remove(toolbar.getItem(index));
        }
    }

    private void internalAdd(final SectionElement element)
    {
        super.add(element.getPanel());
    }

    private void internalRemove(final ContentPanel panel)
    {
        super.remove(panel);
    }

    /**
     * Use {@link #removePanel(ContentPanel)}
     */
    @Deprecated
    @Override
    protected boolean remove(Component item)
    {
        return super.remove(item);
    }

    /**
     * Use {@link #addPanel(ContentPanel)}
     */
    @Deprecated
    @Override
    protected boolean add(Component item)
    {
        return super.add(item);
    }

    private static class SectionElement
    {

        private ToggleToolItem button;

        private ContentPanel panel;

        public SectionElement(ContentPanel panel, boolean withShowHide)
        {
            panel.setCollapsible(false);
            this.setPanel(panel);
            String heading = panel.getHeading();
            setButton(createButton(heading, withShowHide));
        }

        public void setButton(ToggleToolItem button)
        {
            this.button = button;
        }

        public ToggleToolItem getButton()
        {
            return button;
        }

        void setPanel(ContentPanel panel)
        {
            this.panel = panel;
        }

        ContentPanel getPanel()
        {
            return panel;
        }

        private static ToggleToolItem createButton(String heading, boolean withShowHide)
        {
            final String showHeading = withShowHide ? ("Show " + heading) : heading;
            final String hideHeading = withShowHide ? ("Hide " + heading) : heading;
            final ToggleToolItem result = new ToggleToolItem(hideHeading);
            result.pressed = true;
            result.addSelectionListener(new SelectionListener<ComponentEvent>()
                {
                    @Override
                    public void componentSelected(ComponentEvent ce)
                    {
                        if (result.isPressed())
                        {
                            result.setText(hideHeading);
                        } else
                        {
                            result.setText(showHeading);
                        }
                    }
                });
            return result;
        }
    }
}