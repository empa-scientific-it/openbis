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

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext.ClientStaticState;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 * @author Piotr Buczek
 */
// TODO 2011-06-17, Piotr Buczek: reduce amount of methods & their parameters (use link builders)
public class LinkRenderer
{
    private static final String INVALID_STYLE_CLASS = "invalid";

    private static final String LINK_STYLE = "link-style";

    /**
     * @deprecated doesn't support deletion; use {@link #createLinkRenderer(boolean, int)}
     */
    @Deprecated
    public static GridCellRenderer<BaseEntityModel<?>> createLinkRenderer()
    {
        return new GridCellRenderer<BaseEntityModel<?>>()
            {
                @Override
                public Object render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
                        Grid<BaseEntityModel<?>> grid)
                {
                    if (model.get(property) == null)
                    {
                        return "";
                    } else
                    {
                        String originalValue = model.get(property).toString();
                        String tokenOrNull = model.tryGetLink(property);
                        if (tokenOrNull == null && ClientStaticState.isSimpleMode())
                        {
                            return new MultilineHTML(originalValue).toString();
                        } else
                        {
                            if (ClientStaticState.isSimpleMode())
                            {
                                String href = "#" + tokenOrNull;
                                return LinkRenderer.renderAsLinkWithAnchor(originalValue, href,
                                        false);
                            } else
                            {
                                return LinkRenderer.renderAsLinkWithAnchor(originalValue);
                            }
                        }
                    }
                }
            };
    }

    public static GridCellRenderer<BaseEntityModel<?>> createLinkRenderer(
            final boolean renderOriginalValueForEmptyToken, final int columnIndex)
    {
        return new GridCellRenderer<BaseEntityModel<?>>()
            {
                @Override
                @SuppressWarnings("deprecation")
                public Object render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
                        Grid<BaseEntityModel<?>> grid)
                {
                    if (model.get(property) == null)
                    {
                        return "";
                    } else
                    {
                        boolean invalidate = false;
                        if (model.getBaseObject() instanceof TableModelRowWithObject)
                        {
                            List<ISerializableComparable> values =
                                    ((TableModelRowWithObject<?>) model.getBaseObject())
                                            .getValues();
                            if (columnIndex < values.size()) // sanity check
                            {
                                ISerializableComparable cell = values.get(columnIndex);
                                if (cell instanceof EntityTableCell)
                                {
                                    invalidate = ((EntityTableCell) cell).isInvalid();
                                }
                            }
                        }
                        String originalValue = model.get(property).toString();
                        String tokenOrNull = model.tryGetLink(property);
                        if (tokenOrNull == null
                                && (renderOriginalValueForEmptyToken || ClientStaticState
                                        .isSimpleMode()))
                        {
                            return new MultilineHTML(originalValue).toString();
                        } else
                        {
                            if (ClientStaticState.isSimpleMode())
                            {
                                String href = "#" + tokenOrNull;
                                return LinkRenderer.renderAsLinkWithAnchor(originalValue, href,
                                        false, invalidate);
                            } else
                            {
                                return LinkRenderer.renderAsLinkWithAnchor(originalValue,
                                        invalidate);
                            }
                        }
                    }
                }
            };
    }

    public static GridCellRenderer<BaseEntityModel<?>> createExternalLinkRenderer(
            final String overridenLinkTextOrNull)
    {
        return new GridCellRenderer<BaseEntityModel<?>>()
            {

                @Override
                public Object render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
                        Grid<BaseEntityModel<?>> grid)
                {
                    String originalValue =
                            StringEscapeUtils.unescapeHtml(StringUtils.toStringEmptyIfNull(model
                                    .get(property)));
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
        return renderAsLinkWithAnchor(text, "#", false, false);
    }

    /** renders a div with an inline anchor inside (hand cursor is on anchor - inline) */
    public static String renderAsLinkWithAnchor(final String text, final boolean invalidate)
    {
        return renderAsLinkWithAnchor(text, "#", false, invalidate);
    }

    /** renders a div with an inline anchor inside (hand cursor is on anchor - inline) */
    public static String renderAsLinkWithAnchor(final String text, final String href,
            final boolean openInNewWindow)
    {
        return renderAsLinkWithAnchor(text, href, openInNewWindow, false);
    }

    /** renders a div with an inline anchor inside (hand cursor is on anchor - inline) */
    public static String renderAsLinkWithAnchor(final String text, final String href,
            final boolean openInNewWindow, final boolean invalidate)
    {
        final Element anchor = DOM.createAnchor();
        DOM.setInnerHTML(anchor, text);

        DOM.setElementProperty(anchor, "href", href);
        if (openInNewWindow)
        {
            DOM.setElementProperty(anchor, "target", "blank");
        }
        if (invalidate)
        {
            anchor.addClassName(INVALID_STYLE_CLASS);
        }
        return DOM.toString(anchor);
    }

    /**
     * @return {@link Anchor} GWT widget that is displayed as a link with given <var>text</var> and a <var>listener</var> registered on the click
     *         event.
     *         <p>
     *         The link display style is default (not invalidated).
     */
    public static Widget getLinkWidget(final String text, final ClickHandler listener)
    {
        return getLinkWidget(text, listener, null);
    }

    /**
     * See {@link #getLinkAnchor}. Use this method to hide the type of returned widget.
     * <p>
     * The link display style is default (not invalidated).
     */
    public static Widget getLinkWidget(final String text, final ClickHandler listener,
            final String historyHref)
    {
        return getLinkAnchor(text, listener, historyHref);
    }

    /**
     * It is suggested to use {@link #getLinkWidget} method instead of this one.
     * 
     * @return {@link Hyperlink} GWT widget that is displayed as a link with given <var>text</var>. If <var>historyHref</var> is not null and simple
     *         view mode is active <var>historyHref</var> will be appended to the link after '#'. Otherwise if <var>listener</var> is not null it will
     *         be registered on the click event.
     *         <p>
     *         The link display style is default (not invalidated).
     */
    public static Anchor getLinkAnchor(final String text, final ClickHandler listener,
            final String historyHref)
    {
        return getLinkAnchor(text, listener, historyHref, false);
    }

    public static Widget getLinkWidget(final String text, final ClickHandler listener,
            final String historyHref, final boolean invalidate)
    {
        return getLinkAnchor(text, listener, historyHref, invalidate);
    }

    private static Anchor getLinkAnchor(final String text, final ClickHandler listener,
            final String historyHref, final boolean invalidate)
    {
        Anchor link = new Anchor();
        link.setHTML(text);
        link.setStyleName(LINK_STYLE);
        setHrefOrListener(listener, historyHref, link);
        if (invalidate)
        {
            invalidate(link);
        }
        return link;
    }

    /** adds style for invalidated links to given widget */
    public static void invalidate(Widget linkWidget)
    {
        linkWidget.addStyleName(INVALID_STYLE_CLASS);
    }

    @SuppressWarnings("deprecation")
    private static void setHrefOrListener(final ClickHandler listener, final String historyHref,
            Anchor link)
    {
        if (historyHref != null && ClientStaticState.isSimpleMode())
        {
            link.setHref("#" + historyHref);
        } else if (listener != null)
        {
            link.addClickHandler(listener);
        }
    }

    public static interface IURLProvider
    {
        /** @return URL to which redicection should be made or null if no redirection should occur */
        String tryGetURL();
    }

    /**
     * Sets the click listener which executes the specified action when the click occurs if we are in normal view mode and redirects to the provided
     * URL in simple view mode.
     */
    @SuppressWarnings("deprecation")
    public static Widget createButtonLink(Button button,
            final IDelegatedAction normalViewModeAction,
            final IURLProvider simpleViewModeUrlProvider)
    {
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    if (ClientStaticState.isSimpleMode())
                    {
                        String url = simpleViewModeUrlProvider.tryGetURL();
                        if (url != null)
                        {
                            History.newItem(url); // redirects
                        }
                    } else
                    {
                        normalViewModeAction.execute();
                    }
                }
            });
        return button;
    }
}
