/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IRegistratorHolder;

/**
 * An entity that matches the <i>Hibernate Search</i> query and which has been returned by the server.
 * 
 * @author Christian Ribeaud
 */
public final class MatchingEntity implements Serializable, IEntityInformationHolderWithPermId,
        IRegistratorHolder
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private String permId;

    private String identifier;

    private Person registrator;

    private BasicEntityType entityType;

    private EntityKind entityKind;

    private Space spaceOrNull;

    private List<PropertyMatch> matches;

	private double score;
    
    private String searchDomain;

    @Override
    public final EntityKind getEntityKind()
    {
        return entityKind;
    }

    public final void setEntityKind(final EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    @Override
    public final Person getRegistrator()
    {
        return registrator;
    }

    public final void setRegistrator(final Person registrator)
    {
        this.registrator = registrator;
    }

    @Override
    public final BasicEntityType getEntityType()
    {
        return entityType;
    }

    public final void setEntityType(final BasicEntityType entityType)
    {
        this.entityType = entityType;
    }

    public Space tryGetSpace()
    {
        return spaceOrNull;
    }

    public void setSpace(Space spaceOrNull)
    {
        this.spaceOrNull = spaceOrNull;
    }

    public final String getIdentifier()
    {
        return identifier;
    }

    public final void setIdentifier(final String identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @Override
    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    public IEntityInformationHolderWithPermId asIdentifiable()
    {
        final MatchingEntity entity = this;
        return new IEntityInformationHolderWithPermId()
            {
                private static final long serialVersionUID = ServiceVersionHolder.VERSION;

                @Override
                public Long getId()
                {
                    return entity.getId();
                }

                @Override
                public String getCode()
                {
                    return entity.getCode();
                }

                @Override
                public BasicEntityType getEntityType()
                {
                    return entity.getEntityType();
                }

                @Override
                public EntityKind getEntityKind()
                {
                    return entity.getEntityKind();
                }

                @Override
                public String getPermId()
                {
                    return entity.getPermId();
                }
            };
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getIdentifier();
    }

    public List<PropertyMatch> getMatches()
    {
        return matches;
    }

    public void setMatches(List<PropertyMatch> matches)
    {
        this.matches = matches;
    }

    public double getScore()
    {
        return score;
    }

    public void setScore(double score)
    {
        this.score = score;
    }
    
    public String getSearchDomain() {
		return searchDomain;
	}

	public void setSearchDomain(String searchDomain) {
		this.searchDomain = searchDomain;
	}
}
