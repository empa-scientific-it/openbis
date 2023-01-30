package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import com.extjs.gxt.ui.client.widget.TabItem;

/**
 * Allows to update the tab title. The update feature is not yet used anywhere!
 * 
 * @author Izabela Adamczyk
 */
public class TabTitleUpdater
{
    private TabItem tab;

    private String title;

    public TabTitleUpdater(String initialTitle)
    {
        this.title = initialTitle;
    }

    /**
     * Returns current title of the tab.
     */
    String getCurrentTitle()
    {
        return title;
    }

    /**
     * Updates the tab title if binded, stores the title value otherwise.
     */
    private void update(String newTitle)
    {
        this.title = newTitle;
        if (tab != null)
        {
            tab.setText(newTitle);
        }
    }

    /**
     * Binds the tab and updates the title with already stored value.
     */
    void bind(TabItem tabItem)
    {
        this.tab = tabItem;
        update(title);
    }
}
