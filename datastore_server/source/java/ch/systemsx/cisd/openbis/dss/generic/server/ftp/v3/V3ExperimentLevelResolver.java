/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;

class V3ExperimentLevelResolver extends V3Resolver
{
    private IExperimentId experimentId;

    public V3ExperimentLevelResolver(IExperimentId experimentId, FtpPathResolverContext resolverContext)
    {
        super(resolverContext);
        this.experimentId = experimentId;
    }

    @Override
    public FtpFile resolve(String fullPath, String[] subPath)
    {
        if (subPath.length == 0)
        {
            ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
            fetchOptions.withDataSets();

            Map<IExperimentId, Experiment> experiments = api.getExperiments(sessionToken, Collections.singletonList(experimentId), fetchOptions);
            Experiment exp = experiments.get(experimentId);

            List<FtpFile> files = new LinkedList<>();
            for (DataSet dataSet : exp.getDataSets())
            {
                files.add(createDirectoryScaffolding(fullPath, dataSet.getCode()));
            }
            return createDirectoryWithContent(fullPath, files);
        } else
        {
            String dataSetCode = subPath[0];
            IHierarchicalContent content = resolverContext.getContentProvider().asContent(dataSetCode);
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            V3HierarchicalContentResolver resolver = new V3HierarchicalContentResolver(content, resolverContext);
            return resolver.resolve(fullPath, remaining);
        }
    }

}