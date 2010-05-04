/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext.ClientStaticState;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;

/**
 * @author Franz-Josef Elmer
 * @author Piotr Buczek
 */
public class LinkRenderer
{
    private static final String LINK_STYLE = "link-style";

    public static GridCellRenderer<BaseEntityModel<?>> createLinkRenderer()
    {
        return new GridCellRenderer<BaseEntityModel<?>>()
            {// FIXME: almost the same as InternalLinkCellRenderer#createLinkRenderer()
                public Object render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
                        Grid<BaseEntityModel<?>> grid)
                {
                    String text = model.get(property).toString();
                    String tokenOrNull = model.tryGetLink(property);
                    String href = "#" + (tokenOrNull != null ? tokenOrNull : "");
                    return LinkRenderer.renderAsLinkWithAnchor(text, href, false);
                }
            };
    }

    public static GridCellRenderer<BaseEntityModel<?>> createLinkRenderer(final String text)
    {
        return new GridCellRenderer<BaseEntityModel<?>>()
            {
                public Object render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
                        Grid<BaseEntityModel<?>> grid)
                {
                    return LinkRenderer.renderAsLinkWithAnchor(text);
                }
            };
    }

    public static GridCellRenderer<BaseEntityModel<?>> createExternalLinkRenderer(
            final String overridenLinkTextOrNull)
    {
        return new GridCellRenderer<BaseEntityModel<?>>()
            {

                public Object render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
                        Grid<BaseEntityModel<?>> grid)
                {
                    String originalValue = String.valueOf(model.get(property));
                    String linkText =
                            overridenLinkTextOrNull != null ? overridenLinkTextOrNull
                                    : originalValue;
                    return LinkRenderer.renderAsLinkWithAnchor(linkText, originalValue, true);
                }
            };
    }

    public static GridCellRenderer<BaseEntityModel<?>> createExternalLinkRenderer()
    {
        return createExternalLinkRenderer(null);
    }

    /** renders a div witch looks like an anchor (hand cursor is on div - block) */
    public static String renderAsLink(final String message)
    {
        final Element div = DOM.createDiv();
        div.setInnerHTML(message);
        div.setClassName(LINK_STYLE);
        return DOM.toString(div);
    }

    /** renders a div with an inline anchor inside (hand cursor is on anchor - inline) */
    public static String renderAsLinkWithAnchor(final String text)
    {
        return renderAsLinkWithAnchor(text, "#", false);
    }

    /** renders a div with an inline anchor inside (hand cursor is on anchor - inline) */
    public static String renderAsLinkWithAnchor(final String text, final String href,
            final boolean openInNewWindow)
    {
        final Element anchor = DOM.createAnchor();
        DOM.setInnerText(anchor, text);

        DOM.setElementProperty(anchor, "href", href);
        if (openInNewWindow)
        {
            DOM.setElementProperty(anchor, "target", "blank");
        }
        return DOM.toString(anchor);
    }

    /**
     * @return {@link Anchor} GWT widget that is displayed as a link with given <var>text</var> and
     *         a <var>listener</var> registered on the click event.
     *         <p>
     *         The link display style is default (not invalidated).
     */
    public static Widget getLinkWidget(final String text, final ClickHandler listener)
    {
        return getLinkWidget(text, listener, null);
    }

    /**
     * @return {@link Hyperlink} GWT widget that is displayed as a link with given <var>text</var>.
     *         If <var>historyHref</var> is not null and simple view mode is active
     *         <var>historyHref</var> will be appended to the link after '#'. Otherwise if
     *         <var>listener</var> is not null it will be registered on the click event.
     *         <p>
     *         The link display style is default (not invalidated).
     */
    public static Widget getLinkWidget(final String text, final ClickHandler listener,
            final String historyHref)
    {
        return getLinkWidget(text, listener, historyHref, false);
    }

    /**
     * @return {@link Hyperlink} GWT widget that is displayed as a link with given <var>text</var>.
     *         If <var>historyHref</var> is not null and simple view mode is active
     *         <var>historyHref</var> will be appended to the link after '#'. Otherwise if
     *         <var>listener</var> is not null it will be registered on the click event.
     *         <p>
     *         The link display style is based on <var>invalidate</var> (default style is for
     *         false).
     */
    public static Widget getLinkWidget(final String text, final ClickHandler listener,
            final String historyHref, final boolean invalidate)
    {
        Anchor link = new Anchor();
        link.setText(text);
        link.setStyleName(LINK_STYLE);
        if (historyHref != null && ClientStaticState.isSimpleMode())
        {
            link.setHref("#" + historyHref);
        } else if (listener != null)
        {
            link.addClickHandler(listener);
        }
        if (invalidate)
        {
            link.addStyleName("invalid");
        }
        return link;
    }
}
