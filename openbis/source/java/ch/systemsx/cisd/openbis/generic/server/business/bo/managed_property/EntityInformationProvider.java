/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.managed_property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.EntityLinkElementTranslator;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IEntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityLinkElement;

/**
 * @author Piotr Buczek
 */
@Component(value = ResourceNames.ENTITY_INFORMATION_PROVIDER)
public class EntityInformationProvider implements IEntityInformationProvider
{
    @Autowired
    private IDAOFactory daoFactory;

    public String getIdentifier(EntityKind entityKind, String permId)
    {
        IIdentifierHolder identifierHolderOrNull = null;
        switch (entityKind)
        {
            case EXPERIMENT:
                identifierHolderOrNull = daoFactory.getExperimentDAO().tryGetByPermID(permId);
                break;
            case SAMPLE:
                identifierHolderOrNull = daoFactory.getSampleDAO().tryToFindByPermID(permId);
                break;
            case DATA_SET:
                identifierHolderOrNull =
                        daoFactory.getExternalDataDAO().tryToFindDataSetByCode(permId);
                break;
            case MATERIAL:
                MaterialIdentifier idOrNull = MaterialIdentifier.tryParseIdentifier(permId);
                if (idOrNull == null)
                {
                    return null;
                } else
                {
                    final MaterialPE materialOrNull =
                            daoFactory.getMaterialDAO().tryFindMaterial(idOrNull);
                    if (materialOrNull == null)
                    {
                        return null;
                    } else
                    {
                        identifierHolderOrNull = new IIdentifierHolder()
                            {

                                public String getIdentifier()
                                {
                                    return new MaterialIdentifier(materialOrNull.getCode(),
                                            materialOrNull.getEntityType().getCode()).print();
                                }
                            };
                    }
                }
        }
        return identifierHolderOrNull == null ? null : identifierHolderOrNull.getIdentifier();
    }

    public String getIdentifier(IEntityLinkElement entityLink)
    {
        final EntityKind entityKind =
                EntityLinkElementTranslator.translate(entityLink.getEntityLinkKind());
        final String permId = entityLink.getPermId();
        return getIdentifier(entityKind, permId);
    }

}
