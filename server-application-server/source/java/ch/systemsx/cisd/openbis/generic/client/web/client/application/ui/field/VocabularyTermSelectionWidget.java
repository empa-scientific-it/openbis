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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * @author Izabela Adamczyk
 */
public class VocabularyTermSelectionWidget extends
        DropDownList<VocabularyTermModel, VocabularyTerm>
{

    private class AddVocabularyTermDialog extends AbstractRegistrationDialog
    {
        private static final int LABEL_WIDTH = 100;

        private static final int FIELD_WIDTH = 300;

        private final IViewContext<?> viewContext;

        private final LabelField codeField;

        private final DescriptionField descriptionField;

        private final TextField<String> labelField;

        private final VocabularyTermSelectionWidget termSelectionWidget;

        private RefreshAction refreshAction;

        public AddVocabularyTermDialog(IViewContext<?> viewContext, RefreshAction refreshAction)
        {
            super(viewContext, viewContext
                    .getMessage(Dict.ADD_UNOFFICIAL_VOCABULARY_TERM_DIALOG_TITLE), refreshAction);

            this.viewContext = viewContext;
            this.refreshAction = refreshAction;

            form.setLabelWidth(LABEL_WIDTH);
            form.setFieldWidth(FIELD_WIDTH);
            this.setWidth(LABEL_WIDTH + FIELD_WIDTH + 50);

            codeField = new LabelField();
            codeField.setLabelSeparator(":");
            codeField.setFieldLabel(viewContext.getMessage(Dict.CODE));
            codeField.setReadOnly(true);

            addField(codeField);

            boolean mandatory = false;
            labelField = createTextField(viewContext.getMessage(Dict.LABEL), mandatory);
            labelField.setEmptyText("enter label");
            FieldUtil.setValueWithUnescaping(labelField, refreshAction.code);
            FieldUtil.setValueWithUnescaping(codeField, labelField.getValue() == null ? ""
                    : CodeNormalizer.normalize(labelField.getValue()));
            labelField.setMaxLength(GenericConstants.COLUMN_LABEL);
            labelField.addKeyListener(new KeyListener()
                {
                    @Override
                    public void handleEvent(ComponentEvent e)
                    {
                        EventType type = e.getType();
                        if (type == Events.KeyPress || type == Events.KeyUp
                                || type == Events.KeyDown)
                        {
                            FieldUtil.setValueWithUnescaping(
                                    codeField,
                                    labelField.getValue() == null ? "" : CodeNormalizer
                                            .normalize(labelField.getValue()));
                        }
                    }
                });
            addField(labelField);

            descriptionField = createDescriptionField(viewContext, mandatory);
            FieldUtil.setValueWithUnescaping(descriptionField, "");
            addField(descriptionField);

            termSelectionWidget = createTermSelectionWidget();
            addField(termSelectionWidget);

            setFocusWidget(labelField);
        }

        @Override
        protected void register(AsyncCallback<Void> registrationCallback)
        {
            refreshAction.code = (String) codeField.getValue();
            viewContext.getCommonService().addUnofficialVocabularyTerm(
                    TechId.create(vocabularyOrNull), refreshAction.code,
                    labelField.getValue().trim(), descriptionField.getValue(),
                    extractPreviousTermOrdinal(), registrationCallback);
        }

        private VocabularyTermSelectionWidget createTermSelectionWidget()
        {
            boolean mandatory = false;
            VocabularyTermSelectionWidget result =
                    new VocabularyTermSelectionWidget(getId() + "_edit_pos", "Position after",
                            mandatory, vocabularyOrNull, viewContext, null, null, false);
            result.setEmptyText(result.emptyText = result.chooseMsg = "empty value == beginning");
            return result;
        }

        /**
         * extracts ordinal of a term after which edited terms should be put
         */
        private Long extractPreviousTermOrdinal()
        {
            // - 0 if nothing is selected (move to the beginning),
            // - (otherwise) selected term's ordinal
            VocabularyTermModel selectedItem = termSelectionWidget.getValue();
            return selectedItem != null ? selectedItem.getTerm().getOrdinal() : 0;
        }
    }

    private class RefreshAction implements IDelegatedAction
    {
        private String code;

        private RefreshAction(String code)
        {
            this.code = code;
        }

        @Override
        public void execute()
        {
            VocabularyTermSelectionWidget.this.typedValueOrNull = code;
            refreshStore();
            clearInvalid();
            focus();
        }
    }

    private abstract class AddNewTermListener implements Listener<BaseEvent>
    {
        @Override
        public void handleEvent(BaseEvent be)
        {
            if (VocabularyTermSelectionWidget.this.vocabularyOrNull != null && condition())
            {
                AddVocabularyTermDialog d =
                        new AddVocabularyTermDialog(viewContextOrNull, new RefreshAction(
                                getRawValue()));
                d.show();
            }
        }

        public abstract boolean condition();
    }

    private static final String CHOOSE_MSG = "Choose...";

    private static final String CHOOSE_OR_ADD_MSG = "Choose or add new...";

    private static final String VALUE_NOT_IN_LIST_MSG = "Value not in the list";

    private static final String EMPTY_MSG = "- No terms found -";

    private final IViewContext<?> viewContextOrNull;

    private Vocabulary vocabularyOrNull;

    private String initialTermCodeOrNull;

    private String typedValueOrNull = null;

    /**
     * Allows to choose one of the specified vocabulary's terms, is able to refresh the available terms by calling the server.
     */
    public static DatabaseModificationAwareField<VocabularyTermModel> create(String idSuffix,
            String label, Vocabulary vocabulary, final boolean mandatory,
            IViewContext<?> viewContext, String initialTermCodeOrNull)
    {
        return new VocabularyTermSelectionWidget(idSuffix, label, mandatory, vocabulary,
                viewContext, null, initialTermCodeOrNull).asDatabaseModificationAware();
    }

    /**
     * Allows to choose one of the specified vocabulary's terms, is able to refresh the available terms by calling the server.
     */
    public static DatabaseModificationAwareField<VocabularyTermModel> create(String idSuffix,
            String label, Vocabulary vocabulary, final boolean mandatory,
            IViewContext<?> viewContext, String initialTermCodeOrNull,
            boolean allowAddingUnofiicialTerms)
    {
        return new VocabularyTermSelectionWidget(idSuffix, label, mandatory, vocabulary,
                viewContext, null, initialTermCodeOrNull, allowAddingUnofiicialTerms)
                .asDatabaseModificationAware();
    }

    /**
     * Allows to choose one of the specified vocabulary terms.
     */
    public VocabularyTermSelectionWidget(String idSuffix, String label, final boolean mandatory,
            List<VocabularyTerm> initialTermsOrNull, String initialTermOrNull)
    {
        this(idSuffix, label, mandatory, null, null, initialTermsOrNull, initialTermOrNull);
    }

    protected VocabularyTermSelectionWidget(String idSuffix, String label, boolean mandatory,
            Vocabulary vocabularyOrNull, final IViewContext<?> viewContextOrNull,
            List<VocabularyTerm> termsOrNull, String initialTermCodeOrNull)
    {
        this(idSuffix, label, mandatory, vocabularyOrNull, viewContextOrNull, termsOrNull,
                initialTermCodeOrNull, allowAddingUnofficialTerms(viewContextOrNull));
    }

    protected VocabularyTermSelectionWidget(String idSuffix, String label, boolean mandatory,
            Vocabulary vocabularyOrNull, final IViewContext<?> viewContextOrNull,
            List<VocabularyTerm> termsOrNull, String initialTermCodeOrNull,
            boolean allowAddigUnofficialTerms)
    {
        super(idSuffix, ModelDataPropertyNames.CODE_WITH_LABEL, label,
                allowAddigUnofficialTerms ? CHOOSE_OR_ADD_MSG : CHOOSE_MSG, EMPTY_MSG,
                VALUE_NOT_IN_LIST_MSG, mandatory, viewContextOrNull, termsOrNull == null,
                allowAddigUnofficialTerms);
        this.viewContextOrNull = viewContextOrNull;
        this.vocabularyOrNull = vocabularyOrNull;
        this.initialTermCodeOrNull = initialTermCodeOrNull;
        FieldUtil.setMandatoryFlag(this, mandatory);
        setAllowBlank(mandatory == false);
        if (termsOrNull != null)
        {
            setTerms(termsOrNull);
        }

        setLazyRender(false);
        setTemplate(GWTUtils.getTooltipTemplate(VocabularyTermModel.DISPLAY_FIELD,
                ModelDataPropertyNames.TOOLTIP));

        if (allowAddingUnofficialTerms(viewContextOrNull))
        {
            this.addListener(Events.Blur, new AddNewTermListener()
                {
                    @Override
                    public boolean condition()
                    {
                        return getSelection().size() != 1
                                && (false == StringUtils.isBlank(VocabularyTermSelectionWidget.this
                                        .getRawValue()));
                    }
                });
            this.addListener(Events.TwinTriggerClick, new AddNewTermListener()
                {
                    @Override
                    public boolean condition()
                    {
                        return true;
                    }
                });
        }
    }

    private static boolean allowAddingUnofficialTerms(IViewContext<?> viewContextOrNull)
    {
        return viewContextOrNull != null
                && viewContextOrNull.getModel().getApplicationInfo().getWebClientConfiguration()
                        .getAllowAddingUnofficialTerms();
    }

    public void setVocabulary(Vocabulary vocabulary)
    {
        vocabularyOrNull = vocabulary;
        refreshStore();
    }

    private void setTerms(List<VocabularyTerm> terms)
    {
        final List<VocabularyTermModel> models = new ArrayList<VocabularyTermModel>();
        models.addAll(convertItems(terms));
        updateStore(models);
        getPropertyEditor().setList(store.getModels()); // see workaround description above
        selectInitialValue();
    }

    @Override
    protected List<VocabularyTermModel> convertItems(List<VocabularyTerm> result)
    {
        return VocabularyTermModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<VocabularyTerm>> callback)
    {
        if (viewContextOrNull != null && vocabularyOrNull != null)
        {
            viewContextOrNull.getCommonService().listVocabularyTerms(vocabularyOrNull,
                    new ListTermsCallback(viewContextOrNull));
        }
        callback.ignore();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.VOCABULARY_TERM);
    }

    public void selectInitialValue()
    {
        if (typedValueOrNull != null)
        {
            try
            {
                trySelectByCode(typedValueOrNull);
                updateOriginalValue();
            } finally
            {
                typedValueOrNull = null;
            }
        } else if (initialTermCodeOrNull != null)
        {
            trySelectByCode(initialTermCodeOrNull);
            updateOriginalValue();
        }
    }

    public void trySelectByCode(String termCode)
    {
        GWTUtils.setSelectedItem(this, ModelDataPropertyNames.CODE, termCode);
    }

    /**
     * Returns the model for the given value.
     * 
     * @param code the value
     * @return the corresponding model for the value
     */
    public VocabularyTermModel findModel(String code)
    {
        VocabularyTermModel val = null;
        for (VocabularyTermModel c : store.getModels())
        {
            if (c.getTerm().getCode().equals(code))
            {
                val = c;
                break;
            }
        }

        if (val == null)
        {
            initialTermCodeOrNull = code;
        }
        return val;
    }

    private class ListTermsCallback extends VocabularyTermSelectionWidget.ListItemsCallback
    {

        protected ListTermsCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(List<VocabularyTerm> result)
        {
            super.process(result);
            selectInitialValue();
        }
    }
}
