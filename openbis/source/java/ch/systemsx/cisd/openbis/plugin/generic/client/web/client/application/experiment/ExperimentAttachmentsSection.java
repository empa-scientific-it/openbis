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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.CellSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.AttachmentModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;

/**
 * {@link SectionPanel} containing experiment attachments.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentAttachmentsSection extends SectionPanel
{
    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "experiment-attachment-section_";

    private final Experiment experiment;

    private final IMessageProvider messageProvider;

    public ExperimentAttachmentsSection(final Experiment experiment,
            final IViewContext<?> viewContext)
    {
        super("Experiment attachments");
        this.experiment = experiment;
        messageProvider = viewContext.getMessageProvider();
        if (experiment.getAttachments().size() > 0)
        {
            add(createAttachmentsGrid());
        } else
        {
            add(new Html(messageProvider.getMessage("no_attachments_found")), new RowData(-1, -1,
                    new Margins(3)));
        }
    }

    private Component createAttachmentsGrid()
    {
        final ListStore<AttachmentModel> attachmentStore = new ListStore<AttachmentModel>();
        attachmentStore.add(AttachmentModel.convert(experiment.getAttachments()));
        final Grid<AttachmentModel> attachmentGrid =
                new Grid<AttachmentModel>(attachmentStore, new ColumnModel(
                        defineAttachmentColumns()));
        final CellSelectionModel<AttachmentModel> selectionModel =
                new CellSelectionModel<AttachmentModel>();
        attachmentGrid.setSelectionModel(selectionModel);
        selectionModel.bindGrid(attachmentGrid);
        attachmentGrid.addListener(Events.CellClick, new Listener<GridEvent>()
            {
                public void handleEvent(final GridEvent be)
                {
                    String column = attachmentGrid.getColumnModel().getColumn(be.colIndex).getId();
                    if (ModelDataPropertyNames.FILE_NAME.equals(column))
                    {
                        final AttachmentModel selectedItem =
                                (AttachmentModel) be.grid.getStore().getAt(be.rowIndex);
                        Attachment selectedAttachment =
                                (Attachment) selectedItem.get(ModelDataPropertyNames.OBJECT);
                        int version = selectedAttachment.getVersion();
                        String fileName = selectedAttachment.getFileName();
                        Window.open(createURL(version, fileName, experiment), "Download file", "");
                    } else if (ModelDataPropertyNames.OLD_VERSIONS.equals(column))
                    {
                        MessageBox.alert("old versions", column, null);
                    }
                    attachmentGrid.getSelectionModel().deselectAll();
                }
            });
        return attachmentGrid;
    }

    final static String createURL(final int version, final String fileName, final Experiment exp)
    {
        final StringBuffer buffer = new StringBuffer();
        final String projectCode = exp.getProject().getCode();
        final String groupCode = exp.getProject().getGroup().getCode();
        final String experimentCode = exp.getCode();
        final String instanceCode = exp.getProject().getGroup().getInstance().getCode();
        buffer.append(GenericConstants.EXPERIMENT_ATTACHMENT_DOWNLOAD_SERVLET_NAME)

        .append("?")

        .append(GenericConstants.VERSION_PARAMETER).append("=").append(version)

        .append("&")

        .append(GenericConstants.FILE_NAME_PARAMETER).append("=").append(fileName)

        .append("&")

        .append(GenericConstants.PROJECT_PARAMETER).append("=").append(projectCode)

        .append("&")

        .append(GenericConstants.GROUP_PARAMETER).append("=").append(groupCode)

        .append("&")

        .append(GenericConstants.EXPERIMENT_PARAMETER).append("=").append(experimentCode)

        .append("&")

        .append(GenericConstants.DATABASE_PARAMETER).append("=").append(instanceCode);

        return buffer.toString();
    }

    private List<ColumnConfig> defineAttachmentColumns()
    {
        final ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(createFileNameColumnConfig());
        columns.add(ColumnConfigFactory.createRegistrationDateColumnConfig(messageProvider));
        columns.add(ColumnConfigFactory.createRegistratorColumnConfig(messageProvider));
        columns.add(createOlderVersionsColumnConfig());
        return columns;
    }

    private ColumnConfig createFileNameColumnConfig()
    {
        final ColumnConfig column =
                ColumnConfigFactory.createDefaultColumnConfig(messageProvider
                        .getMessage("file_name"), ModelDataPropertyNames.FILE_NAME);
        column.setRenderer(new GridCellRenderer<AttachmentModel>()
            {

                @SuppressWarnings("unchecked")
                public String render(final AttachmentModel model, final String property,
                        final ColumnData config, final int rowIndex, final int colIndex,
                        final ListStore<AttachmentModel> store)
                {
                    Object value = model.get(property);
                    if (value == null)
                    {
                        return "";
                    }
                    return createLink((String) value);
                }
            });
        return column;
    }

    private ColumnConfig createOlderVersionsColumnConfig()
    {
        final ColumnConfig column =
                ColumnConfigFactory.createDefaultColumnConfig(messageProvider
                        .getMessage("old_versions"), ModelDataPropertyNames.OLD_VERSIONS);

        column.setRenderer(new GridCellRenderer<AttachmentModel>()
            {

                @SuppressWarnings("unchecked")
                public String render(final AttachmentModel model, final String property,
                        final ColumnData config, final int rowIndex, final int colIndex,
                        final ListStore<AttachmentModel> store)
                {
                    Object value = model.get(property);
                    if (value == null)
                    {
                        return "";
                    }
                    List<Attachment> versions = (List<Attachment>) value;
                    if (versions.size() == 0)
                    {
                        return "-";
                    } else
                    {
                        final String message =
                                messageProvider
                                        .getMessage("old_versions_template", versions.size());
                        return createLink(message);
                    }
                }
            });
        return column;
    }

    private String createLink(final String message)
    {
        final Element div = DOM.createDiv();
        div.setInnerText(message);
        div.setClassName("link-style");
        return DOM.toString(div);
    }
}