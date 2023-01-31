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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractTableModelReportingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.HierarchicalContentTraverseUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentNodeVisitor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * Reporting plugin which can be used for demonstration purposes. Shows size of the datasets.
 * 
 * @author Tomasz Pylak
 */
public class DemoReportingPlugin extends AbstractTableModelReportingPlugin
{
    private static final long serialVersionUID = 1L;

    public DemoReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    public TableModel createReport(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader("Dataset Code");
        builder.addHeader("Thumbnail");
        builder.addHeader("Name");
        builder.addHeader("Relative Path");
        builder.addHeader("Last Modified");
        builder.addHeader("Size");
        builder.addHeader("Checksum");
        for (DatasetDescription dataset : datasets)
        {
            describe(builder, dataset);
        }
        return builder.getTableModel();
    }

    private void describe(SimpleTableModelBuilder builder, DatasetDescription dataset)
    {
        IHierarchicalContentNodeVisitor visitor = createFileDescribingVisitor(builder, dataset);
        IHierarchicalContentProvider provider = ServiceProvider.getHierarchicalContentProvider();
        HierarchicalContentTraverseUtil.traverse(provider, dataset.getDataSetCode(), visitor);
    }

    private IHierarchicalContentNodeVisitor createFileDescribingVisitor(
            final SimpleTableModelBuilder builder, final DatasetDescription dataset)
    {
        return new IHierarchicalContentNodeVisitor()
            {

                @Override
                public void visit(IHierarchicalContentNode node)
                {
                    if (false == node.isDirectory())
                    {
                        describeFileNode(builder, dataset, node);
                    }
                }
            };
    }

    private void describeFileNode(SimpleTableModelBuilder builder, DatasetDescription dataset,
            IHierarchicalContentNode fileNode)
    {
        ISerializableComparable image = createPathOrImageCell(dataset, fileNode);
        List<ISerializableComparable> row =
                Arrays.<ISerializableComparable> asList(
                        new StringTableCell(dataset.getDataSetCode()), image, new StringTableCell(
                                fileNode.getName()),
                        new StringTableCell(fileNode.getRelativePath()), new DateTableCell(
                                new Date(fileNode.getLastModified())),
                        new DoubleTableCell(fileNode.getFileLength()),
                        new StringTableCell(Long.toString(fileNode.getChecksumCRC32())));
        builder.addRow(row);
    }

    private ISerializableComparable createPathOrImageCell(DatasetDescription dataset,
            IHierarchicalContentNode fileNode)
    {
        File fileOnDisk = null;
        try
        {
            fileOnDisk = fileNode.getFile();
        } catch (UnsupportedOperationException uoe)
        {
            // do not break
        }

        if (fileOnDisk != null && ImageUtil.isImageFile(fileOnDisk))
        {
            return new ImageTableCell(dataset.getDataSetCode(), fileNode.getRelativePath(), 100, 60);
        }
        return new StringTableCell(fileNode.getName());
    }

}
