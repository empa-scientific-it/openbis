/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.dssapi.v3.download;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.Utils;
import ch.systemsx.cisd.openbis.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * @author pkupczyk
 */
public class DataSetFileDownloadInputStream extends ConcatenatedContentInputStream
{

    private Map<IHierarchicalContentNode, String> contentNodes;

    public DataSetFileDownloadInputStream(Map<IHierarchicalContentNode, String> contentNodes)
    {
        super(true, new ArrayList<IHierarchicalContentNode>(contentNodes.keySet()));
        this.contentNodes = contentNodes;
    }

    @Override
    protected InputStream createHeaderSizeStream(IHierarchicalContentNode contentOrNull) throws IOException
    {
        if (contentOrNull == null)
        {
            return new ByteArrayInputStream(longToBytes(0));
        } else
        {
            byte[] bytes = objectToBytes(createDto(contentOrNull));
            return new ByteArrayInputStream(longToBytes(bytes.length));
        }
    }

    @Override
    protected InputStream createHeaderStream(IHierarchicalContentNode contentOrNull) throws IOException
    {
        if (contentOrNull == null)
        {
            return createEmptyStream();
        } else
        {
            byte[] bytes = objectToBytes(createDto(contentOrNull));
            return new ByteArrayInputStream(bytes);
        }
    }

    private DataSetFile createDto(IHierarchicalContentNode content)
    {
        return Utils.createDataSetFile(contentNodes.get(content), content, null);
    }

}
