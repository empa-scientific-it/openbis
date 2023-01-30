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
