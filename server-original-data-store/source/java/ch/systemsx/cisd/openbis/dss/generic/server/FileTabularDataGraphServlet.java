/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CsvFileReaderHelper;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ITabularData;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class FileTabularDataGraphServlet extends AbstractTabularDataGraphServlet
{
    private static final long serialVersionUID = 1L;

    /**
     * Return the tabular data from a file as a DatasetFileLines.
     */
    @Override
    protected ITabularData getDatasetLines(HttpServletRequest request, String dataSetCode,
            String pathOrNull) throws IOException
    {
        if (pathOrNull == null)
        {
            throw new UserFailureException("No value for the parameter " + FILE_PATH_PARAM
                    + " found in the URL");
        }

        RequestParams requestParams = new RequestParams(request);

        File file;
        IHierarchicalContent content = null;
        try
        {
            if (dataSetCode == null)
            {
                file = new File(pathOrNull);
            } else
            {
                IHierarchicalContentProvider contentProvider =
                        applicationContext.getHierarchicalContentProvider(requestParams
                                .getSessionId());
                content = contentProvider.asContent(dataSetCode);
                IHierarchicalContentNode node = content.getNode(pathOrNull);
                file = node.getFile();
            }

            ITabularData data =
                    CsvFileReaderHelper.getDatasetFileLines(file, getConfiguration(request));
            return data;
        } finally
        {
            if (content != null)
            {
                content.close();
            }
        }
    }
}
