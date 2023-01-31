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

package ch.systemsx.cisd.openbis.dss.client.api.gui.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.table.AbstractTableModel;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel.NewDataSetInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTOBuilder;

/**
 * The DataSetUploadClientModel manages the list of data sets to register, initiates uploads (which run in a separate thread) and notifies the GUI of
 * updates. It also coordinates the Metadata panel to ensure the two are in sync.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetUploadTableModel extends AbstractTableModel
{

    private static final long serialVersionUID = 1L;

    // Constants for column order
    public static final int DATA_SET_OWNER_COLUMN = 0;

    public static final int DATA_SET_TYPE_COLUMN = 1;

    public static final int DATA_SET_METADATA_COLUMN = 2;

    public static final int DATA_SET_PATH_COLUMN = 3;

    public static final int UPLOAD_STATUS_COLUMN = 4;

    private final DataSetUploadClientModel clientModel;

    private final ArrayList<DataSetUploadClientModel.NewDataSetInfo> newDataSetInfos =
            new ArrayList<DataSetUploadClientModel.NewDataSetInfo>();

    private int sortColumnIndex = 0;

    private boolean sortAscending = true;

    // Initialize to no row selected
    private int selectedRow = -1;

    private ISynchronizer synchronizer;

    public static interface ISynchronizer
    {

        public void selectRow(int rowIndex);

        public void setNewDataSetInfo(NewDataSetInfo newDataSetInfo);

        public void tableChanged(DataSetUploadTableModel dataSetUploadTableModel);

    }

    /**
     * Internal class for sorting new data set info objects.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private class NewDataSetInfoComparator implements Comparator<NewDataSetInfo>
    {
        @Override
        public int compare(NewDataSetInfo info1, NewDataSetInfo info2)
        {
            int result = 0;
            switch (sortColumnIndex)
            {
                case DATA_SET_OWNER_COLUMN:
                    String identifier1 = info1.getNewDataSetBuilder().getDataSetOwnerIdentifier();
                    String identifier2 = info2.getNewDataSetBuilder().getDataSetOwnerIdentifier();
                    result = identifier1.compareTo(identifier2);
                    break;
                case DATA_SET_TYPE_COLUMN:
                    String type1 =
                            info1.getNewDataSetBuilder().getDataSetMetadata().tryDataSetType();
                    String type2 =
                            info2.getNewDataSetBuilder().getDataSetMetadata().tryDataSetType();
                    type1 = null == type1 ? "" : type1;
                    type2 = null == type2 ? "" : type2;
                    result = type1.compareTo(type2);
                    break;
                case DATA_SET_PATH_COLUMN:
                    File file1 = info1.getNewDataSetBuilder().getFile();
                    File file2 = info2.getNewDataSetBuilder().getFile();
                    String path1 = (null != file1) ? file1.getAbsolutePath() : "";
                    String path2 = (null != file2) ? file2.getAbsolutePath() : "";
                    result = path1.compareTo(path2);
                    break;
                case UPLOAD_STATUS_COLUMN:
                    result = info1.getStatus().compareTo(info2.getStatus());
                    break;
            }
            return sortAscending ? result : -result;
        }
    }

    public DataSetUploadTableModel(DataSetUploadClientModel clientModel)
    {
        this.clientModel = clientModel;
    }

    public void setSynchronizer(ISynchronizer synchronizer)
    {
        this.synchronizer = synchronizer;
    }

    /**
     * Notification that the status of the newDataSetInfo has changed.
     * 
     * @param newDataSetInfo The object whose status has changed.
     * @param newStatus The new status.
     */
    void fireChanged(NewDataSetInfo newDataSetInfo, NewDataSetInfo.Status newStatus)
    {
        int index = newDataSetInfos.indexOf(newDataSetInfo);
        fireTableRowsUpdated(index, index);
    }

    public void setSelectedIndices(ArrayList<Integer> selectedIndices)
    {
        if (selectedIndices.size() < 1)
        {
            selectNewDataSetInfo(null);
            return;
        }
        selectedRow = selectedIndices.get(0);
        NewDataSetInfo selectedDataSet = getSelectedNewDataSetOrNull();
        selectNewDataSetInfo(selectedDataSet);
    }

    public int getSortColumnIndex()
    {
        return sortColumnIndex;
    }

    public void setSortColumnIndex(int sortColumnIndex)
    {
        this.sortColumnIndex = sortColumnIndex;
    }

    public boolean isSortAscending()
    {
        return sortAscending;
    }

    public void setSortAscending(boolean sortAscending)
    {
        this.sortAscending = sortAscending;
    }

    @Override
    public int getColumnCount()
    {
        return 5;
    }

    @Override
    public int getRowCount()
    {
        return newDataSetInfos.size();
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        String name = "";
        if (columnIndex == sortColumnIndex)
        {
            name = sortAscending ? "\u25b2" : "\u25bc";
        }
        switch (columnIndex)
        {
            case DATA_SET_OWNER_COLUMN:
                name += " Owner";
                break;
            case DATA_SET_TYPE_COLUMN:
                name += " Type";
                break;
            case DATA_SET_METADATA_COLUMN:
                name += " Metadata";
                break;
            case DATA_SET_PATH_COLUMN:
                name += " Path";
                break;
            case UPLOAD_STATUS_COLUMN:
                break;
        }

        return name;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        NewDataSetInfo newDataSetInfo = newDataSetInfos.get(rowIndex);
        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        switch (columnIndex)
        {
            case DATA_SET_OWNER_COLUMN:
                DataSetOwner owner = builder.getDataSetOwner();
                return owner.getType() + ":" + untouchedStringOrEmpty(owner.getIdentifier());
            case DATA_SET_TYPE_COLUMN:
                return untouchedStringOrEmpty(builder.getDataSetMetadata().tryDataSetType());
            case DATA_SET_METADATA_COLUMN:
                return builder.getDataSetMetadata().getProperties();
            case DATA_SET_PATH_COLUMN:
                File file = builder.getFile();
                return (file == null) ? "" : file.getName();
            case UPLOAD_STATUS_COLUMN:
                return newDataSetInfo;
        }
        return null;
    }

    /**
     * If aString is null, make it a string
     */
    private String untouchedStringOrEmpty(String aString)
    {
        return (aString == null) ? "" : aString;
    }

    @Override
    public Class<?> getColumnClass(int c)
    {
        final Object columnValue = getValueAt(0, c);
        if (columnValue == null)
        {
            return super.getColumnClass(c);
        } else
        {
            return columnValue.getClass();
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        // This only makes sense for the button column
        assert (col == UPLOAD_STATUS_COLUMN);

        NewDataSetInfo newDataSetInfo = (NewDataSetInfo) value;
        if (null == newDataSetInfo)
        {
            return;
        }

        // Only start uploading if the file hasn't been upload yet
        NewDataSetInfo.Status status = newDataSetInfo.getStatus();
        if ((status != NewDataSetInfo.Status.TO_UPLOAD) && (status != NewDataSetInfo.Status.FAILED))
        {
            return;
        }

        // update the status of the info and start the upload
        queueUploadOfDataSet(newDataSetInfo);

        fireTableCellUpdated(row, col);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        // only the last column is editable
        if (columnIndex != UPLOAD_STATUS_COLUMN)
        {
            return false;
        }

        // and that, only when the file is not currently downloading or finished
        NewDataSetInfo newDataSetInfo = newDataSetInfos.get(rowIndex);
        if (null == newDataSetInfo)
        {
            return false;
        }
        NewDataSetInfo.Status status = newDataSetInfo.getStatus();
        return (status == NewDataSetInfo.Status.TO_UPLOAD)
                || (status == NewDataSetInfo.Status.FAILED)
                || (status == NewDataSetInfo.Status.COMPLETED_UPLOAD);
    }

    public void selectedRowDataChanged()
    {
        if (selectedRow < 0)
        {
            return;
        }
        fireTableRowsUpdated(selectedRow, selectedRow);
    }

    public void addNewDataSet()
    {
        NewDataSetInfo newlyCreated = clientModel.addNewDataSetInfo(getSelectedNewDataSetOrNull());
        syncNewDataSetInfos();
        ArrayList<Integer> selectedIndices = new ArrayList<Integer>();
        selectedIndices.add(clientModel.getNewDataSetInfos().size() - 1);
        setSelectedIndices(selectedIndices);
        selectNewDataSetInfo(newlyCreated);
        synchronizer.selectRow(selectedIndices.get(0));
    }

    public void removeSelectedDataSet()
    {
        if (selectedRow < 0)
        {
            return;
        }
        if (selectedRow >= newDataSetInfos.size())
        {
            return;
        }

        NewDataSetInfo dataSetInfoToRemove = getSelectedNewDataSetOrNull();

        int newSelectedRow = -1;
        ArrayList<Integer> selectedIndices = new ArrayList<Integer>();
        selectedIndices.add(newSelectedRow);
        setSelectedIndices(selectedIndices);
        clientModel.removeNewDataSetInfo(dataSetInfoToRemove);
        syncNewDataSetInfos();
    }

    /**
     * Synchronize the internal list of data set infos with the model's list.
     */
    private void syncNewDataSetInfos()
    {
        newDataSetInfos.clear();
        newDataSetInfos.addAll(clientModel.getNewDataSetInfos());
        syncNewDataSetInfoListView();
    }

    public void syncNewDataSetInfoListView()
    {
        Collections.sort(newDataSetInfos, new NewDataSetInfoComparator());

        synchronizer.tableChanged(this);
    }

    private void selectNewDataSetInfo(NewDataSetInfo newDataSetInfo)
    {
        synchronizer.setNewDataSetInfo(newDataSetInfo);
    }

    /**
     * Start a data set upload in a separate thread. Callers need to ensure that queuing makes sense.
     */
    private void queueUploadOfDataSet(NewDataSetInfo newDataSetInfo)
    {
        clientModel.queueUploadOfDataSet(newDataSetInfo);
    }

    public NewDataSetInfo getSelectedNewDataSetOrNull()
    {
        return (selectedRow < 0) ? null : newDataSetInfos.get(selectedRow);
    }

}
