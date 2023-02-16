/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.deletion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.DeletedObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DeletionValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class DeletionTranslator extends
        AbstractCachingTranslator<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion, Deletion, DeletionFetchOptions> implements
        IDeletionTranslator
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    protected boolean shouldTranslate(TranslationContext context, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion input,
            DeletionFetchOptions fetchOptions)
    {
        DeletionValidator validator = new DeletionValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));
        return validator.doValidation(context.getSession().tryGetPerson(), input);
    }

    @Override
    protected Deletion createObject(TranslationContext context, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion input,
            DeletionFetchOptions fetchOptions)
    {
        Deletion deletion = new Deletion();
        deletion.setId(new DeletionTechId(input.getId()));
        deletion.setReason(input.getReason());
        deletion.setDeletionDate(input.getRegistrationDate());
        deletion.setFetchOptions(new DeletionFetchOptions());
        deletion.setTotalExperimentsCount(input.getTotalExperimentsCount());
        deletion.setTotalSamplesCount(input.getTotalSamplesCount());
        deletion.setTotalDataSetsCount(input.getTotalDatasetsCount());
        return deletion;
    }

    @Override
    protected void updateObject(TranslationContext context, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion input, Deletion output,
            Object relations, DeletionFetchOptions fetchOptions)
    {
        if (fetchOptions.hasDeletedObjects())
        {
            output.getFetchOptions().withDeletedObjects();

            if (input.getDeletedEntities() != null)
            {
                List<DeletedObject> deletedObjects = new ArrayList<DeletedObject>(input.getDeletedEntities().size());

                for (IEntityInformationHolderWithIdentifier deletedEntity : input.getDeletedEntities())
                {
                    DeletedObject deletedObject = new DeletedObject();
                    EntityKind entityKind = deletedEntity.getEntityKind();
                    deletedObject.setEntityKind(EntityKindConverter.convert(entityKind));
                    deletedObject.setIdentifier(deletedEntity.getIdentifier());
                    deletedObject.setEntityTypeCode(deletedEntity.getEntityType().getCode());

                    switch (entityKind)
                    {
                        case EXPERIMENT:
                            deletedObject.setId(new ExperimentPermId(deletedEntity.getPermId()));
                            break;
                        case SAMPLE:
                            deletedObject.setId(new SamplePermId(deletedEntity.getPermId()));
                            break;
                        case DATA_SET:
                            deletedObject.setId(new DataSetPermId(deletedEntity.getPermId()));
                            break;
                        case MATERIAL:
                            deletedObject.setId(new MaterialPermId(deletedEntity.getCode(), deletedEntity.getEntityType().getCode()));
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown entity kind: " + entityKind);
                    }

                    deletedObjects.add(deletedObject);
                }

                output.setDeletedObjects(deletedObjects);
            }
        }
    }
}
