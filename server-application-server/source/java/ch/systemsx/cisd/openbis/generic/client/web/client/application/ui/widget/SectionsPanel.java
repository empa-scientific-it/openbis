package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailViewConfiguration;

/**
 * {@link LayoutContainer} which allows to choose which contained panels should be visible and uses the whole space available to show them.
 * 
 * @author Izabela Adamczyk
 */
public class SectionsPanel extends LayoutContainer
{
    public static final String SECTIONS_TAB_PANEL_ID_SUFFIX = "_section_tabs";

    public static final String SECTION_ID_SUFFIX = "_element";

    private final List<SectionElement> elements = new ArrayList<SectionElement>();

    private final TabPanel tabPanel;

    private String displayId;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public SectionsPanel(IViewContext<ICommonClientServiceAsync> viewContext, String idPrefix)
    {
        this.viewContext = viewContext;
        setLayout(new FillLayout());
        tabPanel = new TabPanel();
        tabPanel.setAutoSelect(false);
        tabPanel.setId(idPrefix + SECTIONS_TAB_PANEL_ID_SUFFIX);
        super.add(tabPanel);
    }

    // apply display settings after first render
    @Override
    protected void onRender(Element parent, int index)
    {
        super.onRender(parent, index);
        tryApplyDisplaySettings();
    }

    public void tryApplyDisplaySettings()
    {
        if (getDisplayID() != null)
        {
            final String tabToActivateID =
                    viewContext.getDisplaySettingsManager().getActiveTabSettings(getDisplayID());
            if (tabToActivateID != null)
            {
                for (SectionElement sectionElement : elements)
                {
                    final String thisTabID = sectionElement.getTabContent().getDisplayID();
                    if (tabToActivateID.equals(thisTabID))
                    {
                        tabPanel.setSelection(sectionElement);
                        return;
                    }
                }
            }
            if (elements.size() > 0)
            {
                tabPanel.setSelection(elements.get(0));
            }
        }
    }

    //

    public void addSection(final TabContent tabContent)
    {
        DetailViewConfiguration viewSettingsOrNull =
                getDisplayID() != null ? viewContext.getDisplaySettingsManager()
                        .tryGetDetailViewSettings(getDisplayID()) : null;
        String panelDisplayId = tabContent.getDisplayID().toUpperCase();
        if (viewSettingsOrNull != null
                && viewSettingsOrNull.getDisabledTabs().contains(panelDisplayId))
        {
            return;
        }
        final SectionElement element = new SectionElement(tabContent, viewContext);
        // sections will be disposed when section panel is removed, not when they are hidden
        // (see onDetach())
        tabContent.disableAutoDisposeComponents();
        elements.add(element);
        addToTabPanel(element);
        tabContent.setParentDisplayID(getDisplayID());
    }

    public void selectSection(final TabContent tabContent)
    {
        for (SectionElement element : elements)
        {
            if (element.getTabContent().equals(tabContent))
            {
                tabPanel.setSelection(element);
                break;
            }
        }
    }

    public void selectFirstSection()
    {
        tabPanel.setSelection(tabPanel.getItem(0));
    }

    @Override
    protected void onDetach()
    {
        for (SectionElement el : elements)
        {
            el.getTabContent().disposeComponents();
        }
        super.onDetach();
    }

    private void addToTabPanel(SectionElement element)
    {
        tabPanel.add(element);
    }

    /**
     * Use {@link #addSection(TabContent)}
     */
    @Deprecated
    @Override
    protected boolean add(Component item)
    {
        return super.add(item);
    }

    private class SectionElement extends TabItem
    {

        private TabContent tabContent;

        public SectionElement(final TabContent tabContent,
                final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            setClosable(false);
            setLayout(new FitLayout());
            this.setTabContent(tabContent);
            setText(tabContent.getHeading());
            add(tabContent);
            setId(tabContent.getId() + SECTION_ID_SUFFIX);

            addListener(Events.Select, new Listener<TabPanelEvent>()
                {
                    @Override
                    public void handleEvent(TabPanelEvent be)
                    {
                        tabContent.setContentVisible(true);
                        if (getDisplayID() != null)
                        {
                            viewContext.getDisplaySettingsManager().storeActiveTabSettings(
                                    getDisplayID(), tabContent.getDisplayID(), SectionsPanel.this);
                        }
                    }
                });
            // WORKAROUND to fix problems when paging toolbar's layout is performed in a hidden tab
            setHideMode(HideMode.OFFSETS);
        }

        void setTabContent(TabContent tabContent)
        {
            this.tabContent = tabContent;
        }

        TabContent getTabContent()
        {
            return tabContent;
        }
    }

    public String getDisplayID()
    {
        return displayId;
    }

    public void setDisplayID(IDisplayTypeIDGenerator generator, String suffix)
    {
        if (suffix != null)
        {
            this.displayId = generator.createID(suffix);
        } else
        {
            this.displayId = generator.createID();
        }
    }

}
