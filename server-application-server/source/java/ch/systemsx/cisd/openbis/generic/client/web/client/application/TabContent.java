/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * {@link ContentPanel} for sections with deferred request handling.
 * <p>
 * If this panel is used outside of {@link SectionsPanel} one needs call {@link #setContentVisible(boolean)} to process requests coming from this
 * section.
 * 
 * @author Izabela Adamczyk
 */
abstract public class TabContent extends ContentPanel
{
    /** creates a section content, called when the section is shown for the first time */
    abstract protected void showContent();

    protected final IViewContext<?> viewContext;

    private String displayId;

    private boolean isContentVisible = false;

    /**
     * Whether additional components created for this section (e.g. browsers) should be automatically disposed when the section is detached from its
     * container. For sections that can be temporarily removed from container as in {@link SectionsPanel} it should be turned off and the container
     * should dispose section components manually.
     */
    private boolean autoDisposeComponents = true;

    private String parentDisplayID;

    private final String ownerId;

    public TabContent(final String header, IViewContext<?> viewContext, IIdHolder ownerOrNull)
    {
        this(header, viewContext, (ownerOrNull != null) ? ownerOrNull.getId().toString() : "");
    }

    private TabContent(final String header, IViewContext<?> viewContext, String ownerId)
    {
        this.viewContext = viewContext;
        this.ownerId = ownerId;
        setHeading(header);
        setHeaderVisible(true);
        setCollapsible(false);
        setAnimCollapse(false);
        setBodyBorder(true);
        setLayout(new FitLayout());
    }

    public String getParentDisplayID()
    {
        return parentDisplayID;
    }

    public void setParentDisplayID(String parentDisplayID)
    {
        this.parentDisplayID = parentDisplayID;
    }

    public void setIds(IDisplayTypeIDGenerator generator)
    {
        setId(createId(ownerId, generator));
        this.displayId = generator.createID();
    }

    public static String createId(String ownerId, IDisplayTypeIDGenerator generator)
    {
        return GenericConstants.ID_PREFIX + ownerId + "-" + generator.createID();
    }

    public String getDisplayID()
    {
        if (displayId == null)
        {
            throw new IllegalStateException("Undefined display ID");
        } else
        {
            return displayId;
        }
    }

    public void setContentVisible(boolean visible)
    {
        if (visible && isContentVisible == false)
        {
            showContent();
            Header h = getHeader();
            if (h.getToolCount() > 0)
            {
                h.show();
            } else
            {
                h.hide();
            }
            syncSize();
            isContentVisible = true;
        }
    }

    public boolean isContentVisible()
    {
        return isContentVisible;
    }

    public void disableAutoDisposeComponents()
    {
        this.autoDisposeComponents = false;
    }

    protected boolean isAutoDisposeComponents()
    {
        return autoDisposeComponents;
    }

    @Override
    protected void onDetach()
    {
        if (isAutoDisposeComponents())
        {
            disposeComponents();
        }
        super.onDetach();
    }

    /** disposes components created for the section (by default does nothing) */
    public void disposeComponents()
    {
    }

}
