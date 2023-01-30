/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeMetaprojectItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;

/**
 * @author pkupczyk
 */
public class MetaprojectTreeMetaprojectItemWidget extends MetaprojectTreeItemWidget
{

    private MetaprojectTreeMetaprojectItemData data;

    private Anchor link;

    public MetaprojectTreeMetaprojectItemWidget(IViewContext<?> viewContext,
            MetaprojectTreeMetaprojectItemData data)
    {
        super(viewContext);

        this.data = data;

        FlowPanel panel = new FlowPanel();
        panel.add(getLabel());
        panel.add(getLink());

        FocusPanel focusPanel = new FocusPanel();
        focusPanel.add(panel);
        focusPanel.addMouseOverHandler(new MouseOverHandler()
            {
                @Override
                public void onMouseOver(MouseOverEvent event)
                {
                    getLink().setVisible(true);
                }
            });
        focusPanel.addMouseOutHandler(new MouseOutHandler()
            {
                @Override
                public void onMouseOut(MouseOutEvent event)
                {
                    getLink().setVisible(isSelected());
                }
            });
        focusPanel.setTitle(getTooltip());

        initWidget(focusPanel);
    }

    private Widget getLabel()
    {
        return new InlineLabel(data.getMetaproject().getName() + " ");
    }

    private Widget getLink()
    {
        if (link == null)
        {
            ClickHandler clickListener = new ClickHandler()
                {
                    @Override
                    public void onClick(ClickEvent event)
                    {
                        OpenEntityDetailsTabHelper.openMetaproject(getViewContext(),
                                data.getMetaproject(),
                                WidgetUtils.ifSpecialKeyPressed(event.getNativeEvent()));

                        // just open the metaproject detail view when the link is clicked
                        // and prevent the metaproject tree selection to be changed
                        event.stopPropagation();
                    }
                };
            MouseDownHandler mouseDownHandler = new MouseDownHandler()
                {
                    @Override
                    public void onMouseDown(MouseDownEvent event)
                    {
                        // just open the metaproject detail view when the link is clicked
                        // and prevent the metaproject tree selection to be changed
                        event.stopPropagation();
                    }
                };

            link =
                    LinkRenderer.getLinkAnchor(
                            getViewContext().getMessage(Dict.METAPROJECT_TREE_INFO_LINK),
                            clickListener,
                            LinkExtractor.createMetaprojectLink(data.getMetaproject().getName()));
            link.addMouseDownHandler(mouseDownHandler);
            link.setVisible(isSelected());
        }
        return link;
    }

    private String getTooltip()
    {
        String name = StringEscapeUtils.unescapeHtml(data.getMetaproject().getName());
        String description = data.getMetaproject().getDescription();

        if (description == null)
        {
            description =
                    getViewContext().getMessage(Dict.METAPROJECT_TREE_DESCRIPTION_NOT_AVAILABLE);
        } else
        {
            description = StringEscapeUtils.unescapeHtml(description);
        }

        return getViewContext().getMessage(Dict.METAPROJECT_TREE_METAPROJECT_TOOLTIP, name,
                description);
    }

    @Override
    protected void onSelectedChange()
    {
        getLink().setVisible(isSelected());
    }

}
