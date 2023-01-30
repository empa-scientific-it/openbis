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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.common;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnSettingsDataModelProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExpression;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;

/**
 * A {@link Window} extension for registering and editing grid custom filters or columns.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
abstract public class AbstractGridCustomExpressionEditOrRegisterDialog extends
        AbstractRegistrationDialog
{

    public static int FIELD_WIDTH = 400;

    public static int LABEL_WIDTH = 100;

    public static final String PUBLIC_FIELD = "public-field";

    public static final String EXPRESSION_FIELD = "expression-field";

    public static final String DESCRIPTION_FIELD = "description-field";

    public static final String NAME_FIELD = "name-field";

    public static final String INSERT_COLUMNS_LINK = "insert-columns-link";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final TextField<String> nameField;

    private final DescriptionField descriptionField;

    private final MultilineVarcharField expressionField;

    private final CheckBoxField publicField;

    private final LabelField insertColumnsLink;

    protected final String gridId;

    public AbstractGridCustomExpressionEditOrRegisterDialog(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String title,
            final IDelegatedAction postRegistrationCallback, final String gridId,
            final AbstractColumnSettingsDataModelProvider columnDataModelProvider)
    {
        super(viewContext, title, postRegistrationCallback);
        this.viewContext = viewContext;
        this.gridId = gridId;
        addField(nameField = createTextField(viewContext.getMessage(Dict.NAME), true));
        nameField.setId(createId(gridId, NAME_FIELD));
        addField(descriptionField = createDescriptionField(viewContext, false));
        descriptionField.setId(createId(gridId, DESCRIPTION_FIELD));
        addField(expressionField = createExpressionField());
        expressionField.setId(createId(gridId, EXPRESSION_FIELD));
        addField(insertColumnsLink =
                createInsertColumnsLink(viewContext.getMessage(Dict.INSERT_COLUMNS),
                        columnDataModelProvider));
        insertColumnsLink.setId(createId(gridId, INSERT_COLUMNS_LINK));
        addField(publicField = new CheckBoxField(viewContext.getMessage(Dict.IS_PUBLIC), false));
        publicField.setId(createId(gridId, PUBLIC_FIELD));

        form.setLabelWidth(LABEL_WIDTH);
        form.setFieldWidth(FIELD_WIDTH);
        setWidth(form.getLabelWidth() + form.getFieldWidth() + 50);

        DialogWithOnlineHelpUtils.addHelpButton(viewContext, this, createHelpPageIdentifier());
    }

    protected abstract HelpPageIdentifier createHelpPageIdentifier();

    protected void initializeValues(AbstractExpression gridExpression)
    {
        FieldUtil.setValueWithUnescaping(descriptionField, gridExpression.getDescription());
        FieldUtil.setValueWithUnescaping(expressionField, gridExpression.getExpression());
        FieldUtil.setValueWithUnescaping(nameField, gridExpression.getName());
        publicField.setValue(gridExpression.isPublic());
    }

    private LabelField createInsertColumnsLink(final String label,
            final AbstractColumnSettingsDataModelProvider columnDataModelProvider)
    {
        LabelField result = new LabelField(LinkRenderer.renderAsLink(label));
        result.sinkEvents(Event.ONCLICK);
        result.addListener(Events.OnClick, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    GridColumnChooserDialog.show(viewContext, columnDataModelProvider, gridId,
                            asExpressionHolder(expressionField));
                }
            });
        return result;
    }

    public static String createId(String gridId, String suffix)
    {
        String escapedGridId = gridId;
        escapedGridId = escapedGridId.replace("(", "_");
        escapedGridId = escapedGridId.replace(")", "_");
        return GenericConstants.ID_PREFIX + "grid-expression-edit-register-" + escapedGridId
                + suffix;
    }

    private MultilineVarcharField createExpressionField()
    {
        MultilineVarcharField field =
                new MultilineVarcharField(viewContext.getMessage(Dict.EXPRESSION), true, 10);
        return field;
    }

    // constructs an item from the information provided by the user
    protected NewColumnOrFilter getNewItemInfo()
    {
        NewColumnOrFilter newItem = new NewColumnOrFilter();
        newItem.setGridId(gridId);
        newItem.setDescription(descriptionField.getValue());
        newItem.setExpression(expressionField.getValue());
        newItem.setName(nameField.getValue());
        newItem.setPublic(publicField.getValue());
        return newItem;
    }

    protected void update(final AbstractExpression gridExpression)
    {
        gridExpression.setDescription(descriptionField.getValue());
        gridExpression.setExpression(expressionField.getValue());
        gridExpression.setName(nameField.getValue());
        gridExpression.setPublic(publicField.getValue());
    }

    private static final IExpressionHolder asExpressionHolder(
            final MultilineVarcharField expressionField)
    {
        return new IExpressionHolder()
            {

                @Override
                public int getCursorPos()
                {
                    return expressionField.getCursorPos();
                }

                @Override
                public String getValue()
                {
                    return expressionField.getValue();
                }

                @Override
                public void setCursorPos(int position)
                {
                    expressionField.setCursorPos(position);
                }

                @Override
                public void setValue(String value)
                {
                    expressionField.setValue(value);
                }
            };
    }
}
