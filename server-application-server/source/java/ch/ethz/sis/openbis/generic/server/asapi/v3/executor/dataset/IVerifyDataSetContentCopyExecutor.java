package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

public interface IVerifyDataSetContentCopyExecutor {

    public void verify(IOperationContext context, CollectionBatch<? extends DataPE> batch);

}
