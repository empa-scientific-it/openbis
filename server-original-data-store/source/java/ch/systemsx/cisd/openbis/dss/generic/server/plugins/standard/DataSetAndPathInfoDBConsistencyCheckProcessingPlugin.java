/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.MailService;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IEmailSender;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetAndPathInfoDBConsistencyChecker;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Processing plugin which checks consistency between data set files in the store and the information stored in pathinfo database.
 *
 * @author pkupczyk
 */
public class DataSetAndPathInfoDBConsistencyCheckProcessingPlugin implements IProcessingPluginTask
{
    private static final long serialVersionUID = 1L;

    private transient IHierarchicalContentProvider fileProvider;

    private transient IHierarchicalContentProvider pathInfoProvider;

    public DataSetAndPathInfoDBConsistencyCheckProcessingPlugin(Properties properties,
            File storeRoot)
    {
    }

    /**
     * @param fileProvider The hierarchical content provider that references the file system.
     * @param pathInfoProvider The hierarchical content provider that references the path-info db.
     */
    public DataSetAndPathInfoDBConsistencyCheckProcessingPlugin(
            IHierarchicalContentProvider fileProvider, IHierarchicalContentProvider pathInfoProvider)
    {
        this.fileProvider = fileProvider;
        this.pathInfoProvider = pathInfoProvider;
    }

    @Override
    public ProcessingStatus process(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        DataSetAndPathInfoDBConsistencyChecker checker = new DataSetAndPathInfoDBConsistencyChecker(fileProvider, pathInfoProvider);
        checker.check(datasets);
        String report = checker.createReport();

        IEmailSender mailSender =
                new MailService(context.getMailClient(), context.getUserEmailOrNull())
                        .createEmailSender();
        mailSender.withSubject("File system and path info DB consistency check report");
        mailSender.withBody(report);
        mailSender.send();

        return checker.getStatus();
    }
}
