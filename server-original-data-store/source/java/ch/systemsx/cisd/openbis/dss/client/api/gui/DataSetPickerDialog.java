/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.util.List;

import javax.swing.JFrame;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.Identifier;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.SamplesDataSets;
import ch.systemsx.cisd.openbis.dss.client.api.gui.tree.FilterableMutableTreeNode;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

/**
 * Dialog for browsing and picking a data set.
 * 
 * @author Pawel Glyzewski
 * @author Franz-Josef Elmer
 */
public class DataSetPickerDialog extends AbstractTreeEntityPickerDialog
{
    private static final long serialVersionUID = 1L;

    public DataSetPickerDialog(JFrame mainWindow, DataSetUploadClientModel clientModel)
    {
        super(mainWindow, "Pick a data set", DataSetOwnerType.DATA_SET, clientModel);
        setDialogData();
    }

    @Override
    protected void expandNode(final FilterableMutableTreeNode node, Identifier identifier)
    {
        if (identifier.getOwnerType() != DataSetOwnerType.DATA_SET)
        {
            clientModel.listSamplesDataSets(identifier,
                    new AsyncNodeAction<SamplesDataSets>(tree, node, scheduler)
                        {
                            @Override
                            public void handleData(SamplesDataSets samplesDataSets)
                            {
                                List<DataSet> dataSets = samplesDataSets.getDataSets();

                                node.removeAllChildren();
                                if (dataSets.size() > 0)
                                {
                                    FilterableMutableTreeNode dataSetsNode =
                                            new FilterableMutableTreeNode(UiUtilities.DATA_SETS);
                                    node.add(dataSetsNode, false);
                                    for (DataSet dataSet : dataSets)
                                    {
                                        dataSetsNode.add(new FilterableMutableTreeNode(Identifier
                                                .create(dataSet)), false);
                                    }
                                    dataSetsNode.filter();
                                }

                                for (Sample s : samplesDataSets.getSamples())
                                {
                                    FilterableMutableTreeNode sampleNode =
                                            new FilterableMutableTreeNode(Identifier.create(s));
                                    sampleNode.add(UiUtilities.createWaitingNode());
                                    node.add(sampleNode, false);
                                }
                                node.filter();
                            }
                        });
        }
    }

}
