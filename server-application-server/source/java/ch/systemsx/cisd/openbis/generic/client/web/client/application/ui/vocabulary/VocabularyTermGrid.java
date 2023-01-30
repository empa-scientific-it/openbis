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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VocabularyTermSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.BasicFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.InfoBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CommonGridIDs;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermGridIDs;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermWithStats;

/**
 * Grid displaying vocabularies.
 * 
 * @author Tomasz Pylak
 */
public class VocabularyTermGrid extends TypedTableGrid<VocabularyTermWithStats>
{

    private static final int LABEL_WIDTH = 100;

    private static final int FIELD_WIDTH = 350;

    private static final int FIELD_WIDTH_IN_REPLACEMENT_DIALOG = 200;

    private static final int LABEL_WIDTH_IN_REPLACEMENT_DIALOG = 200;

    // browser consists of the grid and the paging toolbar
    private static final String BROWSER_ID = GenericConstants.ID_PREFIX
            + "vocabulary-term-browser";

    private final class RefreshCallback extends AbstractAsyncCallback<Void>
    {
        private RefreshCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Void result)
        {
            refresh();
        }
    }

    private final IDelegatedAction postRegistrationCallback;

    private final Vocabulary vocabulary;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            Vocabulary vocabulary)
    {
        return new VocabularyTermGrid(viewContext, vocabulary)
                .asDisposableWithoutToolbar();
    }

    private VocabularyTermGrid(
            IViewContext<ICommonClientServiceAsync> viewContext,
            Vocabulary vocabulary)
    {
        super(viewContext, createBrowserId(vocabulary), true,
                DisplayTypeIDGenerator.VOCABULARY_TERMS_GRID);
        this.vocabulary = vocabulary;
        this.postRegistrationCallback = createRefreshGridAction();
        extendBottomToolbar();

    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<VocabularyTermWithStats>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<VocabularyTermWithStats>> definitions = super.createColumnsDefinition();
        definitions.setGridCellRendererFor(VocabularyTermGridIDs.URL,
                LinkRenderer.createExternalLinkRenderer());
        definitions.setGridCellRendererFor(CommonGridIDs.DESCRIPTION,
                createMultilineStringCellRenderer());
        return definitions;
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        Button addButton = new Button(
                viewContext.getMessage(Dict.ADD_VOCABULARY_TERMS_BUTTON));
        addButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    createAddNewTermsDialog().show();
                }
            });
        addButton(addButton);

        Button editButton = createSelectedItemButton(
                viewContext.getMessage(Dict.EDIT_VOCABULARY_TERM_BUTTON),
                new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<VocabularyTermWithStats>>>()
                    {

                        @Override
                        public void invoke(
                                BaseEntityModel<TableModelRowWithObject<VocabularyTermWithStats>> selectedItem,
                                boolean keyPressed)
                        {
                            final VocabularyTermWithStats term = selectedItem
                                    .getBaseObject().getObjectOrNull();
                            createEditDialog(term.getTerm()).show();
                        }
                    });
        addButton(editButton);

        Button deleteButton = createSelectedItemsButton(
                viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_BUTTON),
                new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            deleteTerms();
                        }
                    });
        addButton(deleteButton);

        if (getWebClientConfiguration().getAllowAddingUnofficialTerms())
        {
            Button makeOfficialButton = new Button(
                    viewContext.getMessage(Dict.MAKE_OFFICIAL_VOCABULARY_TERM_BUTTON),
                    new SelectionListener<ButtonEvent>()
                        {
                            @Override
                            public void componentSelected(ButtonEvent ce)
                            {
                                makeOfficial();
                            }
                        });
            addButton(makeOfficialButton);
        }

        Button batchUpdateButton = new Button(
                viewContext.getMessage(Dict.UPDATE_VOCABULARY_TERMS_BUTTON));
        batchUpdateButton
                .addSelectionListener(new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            createUpdateTermsDialog().show();
                        }
                    });
        addButton(batchUpdateButton);

        if (vocabulary.isManagedInternally())
        {
            String tooltip = viewContext
                    .getMessage(Dict.TOOLTIP_VOCABULARY_MANAGED_INTERNALLY);
            disableButton(batchUpdateButton, tooltip);
            disableButton(editButton, tooltip);
            disableButton(deleteButton, tooltip);
        } else
        {
            allowMultipleSelection();
        }

        addEntityOperationsSeparator();
    }

    private Window createEditDialog(final VocabularyTerm term)
    {
        final String code = term.getCode();
        final String description = term.getDescription();
        final String label = term.getLabel();
        final String title = viewContext.getMessage(Dict.EDIT_TITLE,
                "Vocabulary Term", code);

        return new AbstractRegistrationDialog(viewContext, title,
                postRegistrationCallback)
            {
                private final DescriptionField descriptionField;

                private final TextField<String> labelField;

                private final VocabularyTermSelectionWidget termSelectionWidget;

                {
                    form.setLabelWidth(LABEL_WIDTH);
                    form.setFieldWidth(FIELD_WIDTH);
                    this.setWidth(LABEL_WIDTH + FIELD_WIDTH + 50);

                    boolean mandatory = false;

                    labelField = createTextField(
                            viewContext.getMessage(Dict.LABEL), mandatory);
                    FieldUtil.setValueWithUnescaping(labelField, label);
                    labelField.setMaxLength(GenericConstants.COLUMN_LABEL);
                    addField(labelField);

                    descriptionField = createDescriptionField(viewContext,
                            mandatory);
                    FieldUtil.setValueWithUnescaping(descriptionField, description);
                    addField(descriptionField);
                    // if vocabulary term cannot be choosen from list there is no
                    // need to edit order
                    if (vocabulary.isChosenFromList())
                    {
                        termSelectionWidget = createTermSelectionWidget();
                        addField(termSelectionWidget);
                    } else
                    {
                        termSelectionWidget = null;
                    }
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    term.setDescription(descriptionField.getValue());
                    term.setLabel(labelField.getValue());
                    if (termSelectionWidget != null)
                    {
                        term.setOrdinal(extractPreviousTermOrdinal() + 1);
                    }

                    viewContext.getService().updateVocabularyTerm(term,
                            registrationCallback);
                }

                private VocabularyTermSelectionWidget createTermSelectionWidget()
                {
                    List<VocabularyTerm> allTerms = getTerms();
                    String previousTermCodeOrNull = null;
                    for (int i = 0; i < allTerms.size(); i++)
                    {
                        final String currentTermCode = allTerms.get(i).getCode();
                        if (term.getCode().equals(currentTermCode))
                        {
                            allTerms.remove(i);
                            break;
                        }
                        previousTermCodeOrNull = currentTermCode;
                    }
                    boolean mandatory = false;
                    VocabularyTermSelectionWidget result = new VocabularyTermSelectionWidget(
                            getId() + "_edit_pos", "Position after", mandatory,
                            allTerms, previousTermCodeOrNull);
                    result.setEmptyText("empty value == beginning");
                    return result;
                }

                /**
                 * extracts ordinal of a term after which edited terms should be put
                 */
                private Long extractPreviousTermOrdinal()
                {
                    // - 0 if nothing is selected (move to the beginning),
                    // - (otherwise) selected term's ordinal
                    VocabularyTermModel selectedItem = termSelectionWidget
                            .getValue();
                    return selectedItem != null ? selectedItem.getTerm()
                            .getOrdinal() : 0;
                }
            };
    }

    private void disableButton(Button button, String tooltip)
    {
        button.setEnabled(false);
        GWTUtils.setToolTip(button, tooltip);
    }

    public static String createBrowserId(Vocabulary vocabulary)
    {
        return createBrowserId(TechId.create(vocabulary));
    }

    public static String createBrowserId(TechId vocabularyId)
    {
        return BROWSER_ID + "-" + vocabularyId;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<VocabularyTermWithStats>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<VocabularyTermWithStats>> callback)
    {
        viewContext.getService().listVocabularyTerms(vocabulary,
                resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<VocabularyTermWithStats>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportVocabularyTerms(exportCriteria,
                callback);
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.startsWith("TERM") ? columnID : columnID.toLowerCase();
    }

    private Window createUpdateTermsDialog()
    {
        final String title = viewContext.getMessage(
                Dict.UPDATE_VOCABULARY_TERMS_TITLE, vocabulary.getCode());

        return new AbstractRegistrationDialog(viewContext, title,
                postRegistrationCallback)
            {

                public static final String ID = GenericConstants.ID_PREFIX
                        + "vocabulary-content-edit_" + "form";

                protected final String termsSessionKey;

                {
                    termsSessionKey = ID + vocabulary.getId();

                    form.setLabelWidth(LABEL_WIDTH - 50);
                    form.setFieldWidth(FIELD_WIDTH + 50);
                    this.setWidth(LABEL_WIDTH + FIELD_WIDTH + 50);
                    form.setAction(GenericConstants.createServicePath("upload"));
                    form.setEncoding(Encoding.MULTIPART);
                    form.setMethod(Method.POST);
                    form.add(AbstractRegistrationForm.createHiddenField(
                            AbstractRegistrationForm.SESSION_KEYS_NUMBER, "1"));
                    form.add(AbstractRegistrationForm.createHiddenSessionField(
                            termsSessionKey, 0));

                    insert(createMessageField(), 0);
                    addField(createImportFileField());

                    form.addListener(Events.Submit, new FormPanelListener(
                            new InfoBox(viewContext))
                        {
                            @Override
                            protected void onSuccessfullUpload()
                            {

                                viewContext.getService().updateVocabularyTerms(
                                        termsSessionKey, TechId.create(vocabulary),
                                        new AbstractAsyncCallback<Void>(viewContext)
                                            {

                                                @Override
                                                protected void process(Void result)
                                                {
                                                    postRegistrationCallback.execute();
                                                    hide();
                                                }
                                            });
                            }

                            @Override
                            protected void setUploadEnabled()
                            {
                            }
                        });

                    saveButton.removeAllListeners();
                    saveButton
                            .addSelectionListener(new SelectionListener<ButtonEvent>()
                                {
                                    @Override
                                    public final void componentSelected(
                                            final ButtonEvent ce)
                                    {
                                        if (form.isValid())
                                        {
                                            form.submit();
                                        }
                                    }
                                });

                    DialogWithOnlineHelpUtils.addHelpButton(viewContext, this,
                            createHelpPageIdentifier());
                }

                private FileUploadField createImportFileField()
                {
                    BasicFileFieldManager fileManager = new BasicFileFieldManager(
                            termsSessionKey, 1, "File");
                    fileManager.setMandatory();
                    return fileManager.getFields().get(0);
                }

                private Widget createMessageField()
                {
                    final LabelField messageField = new LabelField();
                    messageField.setStyleAttribute("margin-left", "5px");
                    final String fileFormat = viewContext
                            .getMessage(Dict.VOCABULARY_TERMS_FILE_FORMAT);
                    final String exportMsg = viewContext
                            .getMessage(Dict.UPDATE_VOCABULARY_TERMS_MESSAGE_2);
                    final String msgText = viewContext.getMessage(
                            Dict.UPDATE_VOCABULARY_TERMS_MESSAGE, fileFormat,
                            exportMsg);
                    messageField.setText(msgText);
                    return messageField;
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {

                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(
                            HelpPageIdentifier.HelpPageDomain.TERM,
                            HelpPageIdentifier.HelpPageAction.BATCH_UPDATE);
                }

            };
    }

    private Window createAddNewTermsDialog()
    {
        final String title = viewContext
                .getMessage(Dict.ADD_VOCABULARY_TERM_TITLE);

        return new AbstractRegistrationDialog(viewContext, title,
                postRegistrationCallback)
            {
                private final VocabularyTermSelectionWidget termSelectionWidget;

                private final TextField<String> newTermCodesArea;

                private final TextField<String> newTermsLabelsArea;

                private final TextArea newTermDescriptionsArea;

                {
                    form.setLabelWidth(LABEL_WIDTH);
                    form.setFieldWidth(FIELD_WIDTH);
                    this.setWidth(LABEL_WIDTH + FIELD_WIDTH + 50);

                    newTermCodesArea = createNewTermCodesArea();
                    addField(newTermCodesArea);
                    newTermsLabelsArea = createNewTermLabelArea();
                    addField(newTermsLabelsArea);
                    newTermDescriptionsArea = createNewTermDescriptionArea();
                    addField(newTermDescriptionsArea);

                    // if vocabulary term cannot be choosen from list there is no
                    // need to edit order
                    if (vocabulary.isChosenFromList())
                    {
                        termSelectionWidget = createTermSelectionWidget();
                        addField(termSelectionWidget);
                    } else
                    {
                        termSelectionWidget = null;
                    }

                    DialogWithOnlineHelpUtils.addHelpButton(viewContext, this,
                            createHelpPageIdentifier());
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    VocabularyTerm newVocabularyTerm = VocabularyTermSingleCodeValidator.getTerm(newTermCodesArea.getValue());
                    newVocabularyTerm.setLabel(newTermsLabelsArea.getValue());
                    newVocabularyTerm.setDescription(newTermDescriptionsArea.getValue());
                    Long previousTermOrdinal = extractPreviousTermOrdinal();

                    List<VocabularyTerm> newTerms = new ArrayList<VocabularyTerm>();
                    newTerms.add(newVocabularyTerm);
                    viewContext.getService().addVocabularyTerms(
                            TechId.create(vocabulary), newTerms,
                            previousTermOrdinal, registrationCallback);
                }

                private TextField<String> createNewTermCodesArea()
                {
                    final TextField<String> result = new TextField<String>();
                    result.setFieldLabel(viewContext.getMessage(Dict.TERM_CODE));
                    result.setEmptyText(viewContext.getMessage(Dict.VOCABULARY_TERM_CODE_EMPTY));
                    result.setValidator(new VocabularyTermSingleCodeValidator(viewContext));
                    FieldUtil.setMandatoryFlag(result, true);
                    return result;
                }

                private TextArea createNewTermDescriptionArea()
                {
                    final TextArea result = new TextArea();
                    result.setFieldLabel(viewContext.getMessage(Dict.VOCABULARY_TERM_DESCRIPTION));
                    result.setEmptyText(viewContext.getMessage(Dict.VOCABULARY_TERM_DESCRIPTION_EMPTY));
                    return result;
                }

                private TextField<String> createNewTermLabelArea()
                {
                    final TextField<String> result = new TextField<String>();
                    result.setFieldLabel(viewContext.getMessage(Dict.VOCABULARY_TERM_LABEL));
                    result.setEmptyText(viewContext.getMessage(Dict.VOCABULARY_TERM_LABEL_EMPTY));
                    return result;
                }

                private VocabularyTermSelectionWidget createTermSelectionWidget()
                {
                    // by default - append
                    final List<VocabularyTerm> allTerms = getTerms();
                    final String lastTermCodeOrNull = (allTerms.size() > 0) ? allTerms.get(allTerms.size() - 1).getCode() : null;
                    boolean mandatory = false;
                    VocabularyTermSelectionWidget result =
                            new VocabularyTermSelectionWidget(getId() + "_add_pos", "Position after", mandatory, allTerms, lastTermCodeOrNull);
                    result.setEmptyText("empty value == beginning");
                    return result;
                }

                /**
                 * extracts ordinal of a term after which new terms will be added
                 */
                private Long extractPreviousTermOrdinal()
                {
                    if (termSelectionWidget == null)
                    {
                        // last terms position (append) if vocabulary will not be
                        // chosen from list
                        final List<VocabularyTerm> allTerms = getTerms();
                        return (allTerms.size() > 0) ? allTerms.get(
                                allTerms.size() - 1).getOrdinal() : 0L;
                    } else
                    {
                        // 0 if nothing was selected to enable prepend
                        VocabularyTermModel selectedItem = termSelectionWidget
                                .getValue();
                        return selectedItem != null ? selectedItem.getTerm()
                                .getOrdinal() : 0L;
                    }
                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(
                            HelpPageIdentifier.HelpPageDomain.TERM,
                            HelpPageIdentifier.HelpPageAction.REGISTER);
                }
            };
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(CommonGridIDs.CODE, CommonGridIDs.LABEL);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        // refresh when any high level entity or property assignment is
        // modified/created/deleted
        return new DatabaseModificationKind[] {
                DatabaseModificationKind.createOrDelete(ObjectKind.DATA_SET),
                DatabaseModificationKind.edit(ObjectKind.DATA_SET),
                DatabaseModificationKind.createOrDelete(ObjectKind.EXPERIMENT),
                DatabaseModificationKind.edit(ObjectKind.EXPERIMENT),
                DatabaseModificationKind.createOrDelete(ObjectKind.MATERIAL),
                DatabaseModificationKind.edit(ObjectKind.MATERIAL),
                DatabaseModificationKind.createOrDelete(ObjectKind.SAMPLE),
                DatabaseModificationKind.edit(ObjectKind.SAMPLE),
                DatabaseModificationKind.createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                // (unofficial) terms may also be created outside of the grid
                DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM),
                DatabaseModificationKind.edit(ObjectKind.VOCABULARY_TERM) };
    }

    private void deleteTerms()
    {
        List<BaseEntityModel<TableModelRowWithObject<VocabularyTermWithStats>>> terms = getSelectedItems();
        if (terms.isEmpty())
        {
            return;
        }
        Set<String> selectedTerms = new HashSet<String>();
        List<VocabularyTerm> termsToBeDeleted = new ArrayList<VocabularyTerm>();
        List<VocabularyTermReplacement> termsToBeReplaced = new ArrayList<VocabularyTermReplacement>();
        for (BaseEntityModel<TableModelRowWithObject<VocabularyTermWithStats>> model : terms)
        {
            VocabularyTerm term = model.getBaseObject().getObjectOrNull()
                    .getTerm();
            selectedTerms.add(term.getCode());
            if (model.getBaseObject().getObjectOrNull().getTotalUsageCounter() > 0)
            {
                VocabularyTermReplacement termToBeReplaced = new VocabularyTermReplacement();
                termToBeReplaced.setTerm(term);
                termsToBeReplaced.add(termToBeReplaced);
            } else
            {
                termsToBeDeleted.add(term);
            }
        }
        deleteAndReplace(selectedTerms, termsToBeDeleted, termsToBeReplaced);
    }

    private void deleteAndReplace(Set<String> selectedTerms,
            final List<VocabularyTerm> termsToBeDeleted,
            final List<VocabularyTermReplacement> termsToBeReplaced)
    {
        if (termsToBeReplaced.isEmpty())
        {
            String title = viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_CONFIRMATION_TITLE);
            int size = termsToBeDeleted.size();
            String message;
            if (size == 1)
            {
                message = viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_CONFIRMATION_MESSAGE_NO_REPLACEMENTS_SINGULAR);
            } else
            {
                message = viewContext.getMessage(Dict.DELETE_VOCABULARY_TERMS_CONFIRMATION_MESSAGE_NO_REPLACEMENTS, size);
            }
            ConfirmationDialog confirmationDialog = new ConfirmationDialog(
                    title, message)
                {
                    @Override
                    protected void onYes()
                    {
                        deleteAndReplace(termsToBeDeleted, termsToBeReplaced);
                    }
                };
            confirmationDialog.show();
        } else
        {
            List<VocabularyTerm> termsForReplacement = new ArrayList<VocabularyTerm>();
            for (VocabularyTerm term : getTerms())
            {
                if (selectedTerms.contains(term.getCode()) == false)
                {
                    termsForReplacement.add(term);
                }
            }
            askForReplacements(termsToBeDeleted, termsToBeReplaced,
                    termsForReplacement);
        }
    }

    private void makeOfficial()
    {
        List<BaseEntityModel<TableModelRowWithObject<VocabularyTermWithStats>>> terms = getSelectedItems();
        if (terms.isEmpty())
        {
            return;
        }

        Set<String> selectedTerms = new HashSet<String>();
        final List<VocabularyTerm> termsToBeOfficial = new ArrayList<VocabularyTerm>();
        for (BaseEntityModel<TableModelRowWithObject<VocabularyTermWithStats>> model : terms)
        {
            VocabularyTerm term = model.getBaseObject().getObjectOrNull().getTerm();
            selectedTerms.add(term.getCode());
            termsToBeOfficial.add(term);
        }

        String title = viewContext.getMessage(Dict.MAKE_OFFICIAL_VOCABULARY_TERMS_CONFIRMATION_TITLE);
        int size = termsToBeOfficial.size();
        String message;
        if (size == 1)
        {
            message = viewContext.getMessage(Dict.MAKE_OFFICIAL_VOCABULARY_TERMS_CONFIRMATION_MESSAGE_SINGULAR);
        } else
        {
            message = viewContext.getMessage(Dict.MAKE_OFFICIAL_VOCABULARY_TERMS_CONFIRMATION_MESSAGE, size);
        }
        ConfirmationDialog confirmationDialog = new ConfirmationDialog(title,
                message)
            {
                @Override
                protected void onYes()
                {
                    RefreshCallback callback = new RefreshCallback(viewContext);
                    viewContext.getService().makeVocabularyTermsOfficial(
                            TechId.create(vocabulary), termsToBeOfficial, callback);

                }
            };
        confirmationDialog.show();

    }

    private List<VocabularyTerm> getTerms()
    {
        List<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();
        for (VocabularyTermWithStats v : getContainedGridElements())
        {
            terms.add(v.getTerm());
        }
        return terms;
    }

    private void askForReplacements(
            final List<VocabularyTerm> termsToBeDeleted,
            final List<VocabularyTermReplacement> termsToBeReplaced,
            List<VocabularyTerm> termsForReplacement)
    {
        VerticalPanel panel = new VerticalPanel();
        int totalNumber = termsToBeDeleted.size() + termsToBeReplaced.size();
        panel.add(new Text(
                viewContext
                        .getMessage(
                                Dict.DELETE_VOCABULARY_TERMS_CONFIRMATION_MESSAGE_FOR_REPLACEMENTS,
                                totalNumber)));
        final FormPanel formPanel = new FormPanel();
        formPanel.setLabelWidth(LABEL_WIDTH_IN_REPLACEMENT_DIALOG);
        formPanel.setFieldWidth(FIELD_WIDTH_IN_REPLACEMENT_DIALOG);
        formPanel.setBorders(false);
        formPanel.setHeaderVisible(false);
        formPanel.setBodyBorder(false);
        panel.add(formPanel);
        String title = viewContext
                .getMessage(Dict.DELETE_VOCABULARY_TERMS_CONFIRMATION_TITLE);
        String okButtonLable = viewContext
                .getMessage(Dict.ADD_VOCABULARY_TERMS_OK_BUTTON);
        final SimpleDialog dialog = new SimpleDialog(panel, title,
                okButtonLable, viewContext);
        dialog.setScrollMode(Scroll.AUTOY);
        dialog.setWidth(LABEL_WIDTH_IN_REPLACEMENT_DIALOG
                + FIELD_WIDTH_IN_REPLACEMENT_DIALOG + 50);
        dialog.setEnableOfAcceptButton(false);
        for (final VocabularyTermReplacement termToBeReplaced : termsToBeReplaced)
        {
            String term = termToBeReplaced.getTerm().getCode();
            // TODO 2009-06-26, IA: do we really want to load almost all the
            // terms from large
            // vocabulary to the drop down list?
            final VocabularyTermSelectionWidget s = new VocabularyTermSelectionWidget(
                    getId() + term, term, true, termsForReplacement, null);
            s.addSelectionChangedListener(new SelectionChangedListener<VocabularyTermModel>()
                {
                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<VocabularyTermModel> se)
                    {
                        VocabularyTermModel selectedItem = se.getSelectedItem();
                        termToBeReplaced
                                .setReplacementCode(selectedItem == null ? null
                                        : selectedItem.getTerm().getCode());
                        dialog.setEnableOfAcceptButton(formPanel.isValid());
                    }
                });
            formPanel.add(s);
        }
        dialog.setAcceptAction(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    deleteAndReplace(termsToBeDeleted, termsToBeReplaced);
                }

            });
        DialogWithOnlineHelpUtils.addHelpButton(viewContext, dialog,
                new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.TERM,
                        HelpPageIdentifier.HelpPageAction.DELETE));
        dialog.show();
    }

    private void deleteAndReplace(List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced)
    {
        RefreshCallback callback = new RefreshCallback(viewContext);
        viewContext.getService().deleteVocabularyTerms(
                TechId.create(vocabulary), termsToBeDeleted, termsToBeReplaced,
                callback);

    }
}
