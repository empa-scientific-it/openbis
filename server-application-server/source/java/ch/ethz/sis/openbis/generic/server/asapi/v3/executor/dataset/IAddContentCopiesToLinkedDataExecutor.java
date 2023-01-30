package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Collection;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;

public interface IAddContentCopiesToLinkedDataExecutor
{

    void add(IOperationContext context, LinkDataPE entity, Collection<ContentCopyCreation> added);

}
