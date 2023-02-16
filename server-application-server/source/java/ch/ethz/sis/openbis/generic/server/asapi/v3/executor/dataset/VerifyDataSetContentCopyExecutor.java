/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.VerifyProgress;
import ch.systemsx.cisd.openbis.generic.shared.dto.ContentCopyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;

/**
 * {@link LinkDataPE}s are not allowed to have {@link ContentCopyPE}s which have different git repository ids.
 */
@Component
public class VerifyDataSetContentCopyExecutor implements IVerifyDataSetContentCopyExecutor {

	@Override
	public void verify(IOperationContext context,
			CollectionBatch<? extends DataPE> batch) {

        new CollectionBatchProcessor<DataPE>(context, batch)
        {
            @Override
            public void process(DataPE dataSet)
            {
                verify(dataSet);
            }

            @Override
            public IProgress createProgress(DataPE object, int objectIndex, int totalObjectCount)
            {
                return new VerifyProgress(object, objectIndex, totalObjectCount);
            }
        };
	}

	protected void verify(DataPE dataSet)
	{
		if (dataSet instanceof LinkDataPE)
		{
			LinkDataPE linkDataPE = (LinkDataPE) dataSet;
			Map<String, List<ContentCopyPE>> byRepositoryId = linkDataPE.getContentCopies().stream()
					.filter(cc -> cc.getGitRepositoryId() != null)
					.collect(Collectors.groupingBy(cc -> cc.getGitRepositoryId()));

			if (byRepositoryId.keySet().size() > 1)
			{
				throw new IllegalArgumentException("Within one data set, all git repository ids must be the same.");
			}			
		}
	}

}
