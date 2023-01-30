/*
 * Copyright 2007 ETH Zuerich, CISD
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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DOMUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IRegistratorAndModifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IRegistratorHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimplePersonRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * A <i>static</i> class to render {@link Person}.
 * 
 * @author Christian Ribeaud
 */
public final class PersonRenderer
{

    private static final char EMAIL_END = '>';

    private static final char EMAIL_START = '<';

    private static final char LOGIN_END = ']';

    private static final char LOGIN_START = '[';

    /**
     * Registrator renderer. Works only with {@link TableModelRowWithObject} wrapping a DTO implementing {@link IRegistratorHolder}.
     */
    public static final GridCellRenderer<BaseEntityModel<?>> REGISTRATOR_RENDERER =
            new GridCellRenderer<BaseEntityModel<?>>()
                {
                    @Override
                    @SuppressWarnings("unchecked")
                    public Object render(BaseEntityModel<?> model, String property,
                            ColumnData config, int rowIndex, int colIndex,
                            ListStore<BaseEntityModel<?>> store, Grid<BaseEntityModel<?>> grid)
                    {
                        Person registrator =
                                ((TableModelRowWithObject<IRegistratorHolder>) model
                                        .getBaseObject()).getObjectOrNull().getRegistrator();
                        return PersonRenderer.createPersonAnchor(registrator);
                    }
                };

    /**
     * Modifier renderer. Works only with {@link TableModelRowWithObject} wrapping a DTO implementing {@link IRegistratorAndModifierHolder}.
     */
    public static final GridCellRenderer<BaseEntityModel<?>> MODIFIER_RENDERER =
            new GridCellRenderer<BaseEntityModel<?>>()
                {
                    @Override
                    @SuppressWarnings("unchecked")
                    public Object render(BaseEntityModel<?> model, String property,
                            ColumnData config, int rowIndex, int colIndex,
                            ListStore<BaseEntityModel<?>> store, Grid<BaseEntityModel<?>> grid)
                    {
                        Person modifier = null;
                        if (model.get(property) != null && model.get(property) instanceof Person)
                        {
                            modifier = model.get(property);
                        } else
                        {
                            modifier =
                                    ((TableModelRowWithObject<IRegistratorAndModifierHolder>) model
                                            .getBaseObject()).getObjectOrNull().getModifier();
                        }
                        return PersonRenderer.createPersonAnchor(modifier);
                    }
                };

    private PersonRenderer()
    {
        // This class can not be instantiated
    }

    /**
     * Creates an <i>HTML</i> A element for given <var>person</var>.
     */
    public final static String createPersonAnchor(final Person person)
    {
        String personName = createPersonName(person).toString();
        return createPersonAnchor(person, personName);
    }

    private static StringBuilder createPersonName(Person person)
    {
        return SimplePersonRenderer.createPersonName(person);
    }

    /**
     * Creates an <i>HTML</i> A element for given <var>person</var> with a specified name.
     */
    public final static String createPersonAnchor(final Person personOrNull, String personName)
    {
        if (personOrNull == null)
        {
            return personName;
        }
        final String email = personOrNull.getEmail();
        if (StringUtils.isBlank(email) == false)
        {
            final Element anchor = DOMUtils.createAnchorElement(null, "mailto:" + email, email);
            DOM.setInnerText(anchor, StringEscapeUtils.unescapeHtml(personName));
            return DOM.toString(anchor);
        } else
        {
            return personName;
        }
    }

    /**
     * Returns a pretty and short description of this object.
     * <p>
     * If the fields are not blank, the returned string will have the following format:
     * 
     * <pre>
     * &lt;lastName&gt;, &lt;firstName&gt; &lt;&lt;email&gt;&gt; [userID]
     * </pre>
     * 
     * </p>
     */
    public final static String toString(final Person person)
    {
        assert person != null : "Given person can not be null.";
        final StringBuilder builder = new StringBuilder();
        builder.append(createPersonName(person));
        // UserId
        final String userId = person.getUserId();
        if (StringUtils.isBlank(userId) == false)
        {
            if (builder.length() != 0)
            {
                builder.append(' ');
            }
            builder.append(LOGIN_START);
            builder.append(userId);
            builder.append(LOGIN_END);
        }
        // Email
        final String email = person.getEmail();
        if (StringUtils.isBlank(email) == false)
        {
            if (builder.length() > 0)
            {
                builder.append(" ");
            }
            builder.append(EMAIL_START);
            builder.append(email);
            builder.append(EMAIL_END);
        }
        return builder.toString();
    }

}
