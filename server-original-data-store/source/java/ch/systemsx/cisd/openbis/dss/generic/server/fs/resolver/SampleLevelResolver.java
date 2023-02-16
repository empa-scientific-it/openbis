/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.fs.resolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;

/**
 * @author Franz-Josef Elmer
 *
 */
class SampleLevelResolver implements IResolver
{
    private ISampleId sampleId;

    public SampleLevelResolver(ISampleId sampleId)
    {
        this.sampleId = sampleId;
        
    }

    @Override
    public IFileSystemViewResponse resolve(String[] subPath, IResolverContext context)
    {
        if (subPath.length == 0)
        {
            SampleFetchOptions fetchOptions = new SampleFetchOptions();
            fetchOptions.withDataSets();

            Map<ISampleId, Sample> samples =
                    context.getApi().getSamples(context.getSessionToken(), Collections.singletonList(sampleId), fetchOptions);
            Sample sample = samples.get(sampleId);

            if (sample == null)
            {
                return context.createNonExistingFileResponse(null);
            }

            IDirectoryResponse response = context.createDirectoryResponse();
            for (DataSet dataSet : sample.getDataSets())
            {
                response.addDirectory(dataSet.getCode(), dataSet.getModificationDate());
            }
            return response;
        } else
        {
            String dataSetCode = subPath[0];
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            return new DataSetContentResolver(dataSetCode).resolve(remaining, context);
        }
    }
}
