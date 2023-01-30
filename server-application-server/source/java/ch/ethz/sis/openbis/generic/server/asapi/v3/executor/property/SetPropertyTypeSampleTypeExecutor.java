package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

@Component
public class SetPropertyTypeSampleTypeExecutor
        extends AbstractSetEntityToOneRelationExecutor<PropertyTypeCreation, PropertyTypePE, IEntityTypeId, EntityTypePE>
        implements ISetPropertyTypeSampleTypeExecutor
{
    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "property type-sample type";
    }

    @Override
    protected IEntityTypeId getRelatedId(PropertyTypeCreation creation)
    {
        return creation.getSampleTypeId();
    }

    @Override
    protected Map<IEntityTypeId, EntityTypePE> map(IOperationContext context, List<IEntityTypeId> relatedIds)
    {
        return mapEntityTypeByIdExecutor.map(context, EntityKind.SAMPLE, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, PropertyTypePE entity, IEntityTypeId relatedId, EntityTypePE related)
    {
    }

    @Override
    protected void set(IOperationContext context, PropertyTypePE entity, EntityTypePE related)
    {
        entity.setSampleType((SampleTypePE) related);
    }

}
