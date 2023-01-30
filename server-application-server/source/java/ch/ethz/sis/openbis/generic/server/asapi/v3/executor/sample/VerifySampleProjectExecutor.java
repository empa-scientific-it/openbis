/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.VerifyProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.utils.EntityUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class VerifySampleProjectExecutor implements IVerifySampleProjectExecutor
{

    @Override
    public void verify(final IOperationContext context, final CollectionBatch<SamplePE> batch)
    {
        new CollectionBatchProcessor<SamplePE>(context, batch)
            {
                @Override
                public void process(SamplePE sample)
                {
                    ProjectPE project = sample.getProject();
                    if (project != null && sample.getSpace() == null)
                    {
                        throw new UserFailureException("Shared samples cannot be attached to projects. Sample: "
                                + EntityUtils.render(sample) + ", Project: " + EntityUtils.render(project));
                    }
                    if (project != null && project.getSpace().equals(sample.getSpace()) == false)
                    {
                        throw new UserFailureException("Sample space must be the same as project space. Sample: "
                                + EntityUtils.render(sample) + ", Project: " + EntityUtils.render(project));
                    }
                }

                @Override
                public IProgress createProgress(SamplePE object, int objectIndex, int totalObjectCount)
                {
                    return new VerifyProgress(object, objectIndex, totalObjectCount);
                }
            };
    }

}
