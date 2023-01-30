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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of file format types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class FileFormatTypeSelectionWidget extends
        DropDownList<FileFormatTypeSelectionWidget.FileFormatTypeModel, FileFormatType>
{
    private static final String EMPTY_RESULT_SUFFIX = "file format types";

    private static final String CHOOSE_SUFFIX = "file format type";

    public static final String SUFFIX = "file-format-type";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private static final String DISPLAY_COLUMN_ID = "code";

    public static class FileFormatTypeModel extends SimplifiedBaseModelData
    {
        private static final long serialVersionUID = 1L;

        public FileFormatTypeModel(FileFormatType type)
        {
            set(DISPLAY_COLUMN_ID, type.getCode());
            set(ModelDataPropertyNames.OBJECT, type);
        }

    }

    public FileFormatTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String idSuffix)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.FILE_FORMAT_TYPE, ModelDataPropertyNames.CODE,
                CHOOSE_SUFFIX, EMPTY_RESULT_SUFFIX);
        this.viewContext = viewContext;
    }

    /**
     * Returns the {@link FileFormatType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final FileFormatType tryGetSelectedFileFormatType()
    {
        return super.tryGetSelected();
    }

    @Override
    protected List<FileFormatTypeModel> convertItems(List<FileFormatType> types)
    {
        final List<FileFormatTypeModel> result = new ArrayList<FileFormatTypeModel>();
        for (final FileFormatType type : types)
        {
            result.add(new FileFormatTypeModel(type));
        }
        return result;
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<FileFormatType>> callback)
    {
        viewContext.getService().listFileTypes(callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.FILE_FORMAT_TYPE);
    }

}
