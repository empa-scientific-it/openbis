/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.db.mapper.LongArrayMapper;
import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectsProviderFromSampleV1Collection;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromRolesWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.Select;

/**
 * A predicate for lists of entities of {@link Sample}s. This predicate authorizes for read-only access, i.e. it will allow access to shared samples
 * for all users.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Bernd Rinn
 */
@ShouldFlattenCollections(value = false)
public class SampleListPredicate extends AbstractSpacePredicate<List<Sample>>
{
    private final static int ARRAY_SIZE_LIMIT = 999;

    public static interface ISampleToSpaceQuery extends BaseQuery
    {
        @Select(sql = "select distinct sp.code from samples sa inner join spaces sp on sa.space_id = sp.id where sa.id = any(?{1}) "
                + "union select distinct sp.code from samples sa inner join spaces sp on sa.space_id = sp.id where sa.perm_id = any(?{2})", parameterBindings = {
                        LongArrayMapper.class,
                        StringArrayMapper.class })
        public List<String> getSampleSpaceCodes(long[] sampleIds, String[] samplePermIds);
    }

    private final SampleIdentifierPredicate sampleIdentifierPredicate;

    private final ISampleToSpaceQuery sampleToSpaceQuery;

    public SampleListPredicate()
    {
        sampleIdentifierPredicate = new SampleIdentifierPredicate();
        sampleToSpaceQuery = QueryTool.getManagedQuery(ISampleToSpaceQuery.class);
    }

    //
    // AbstractPredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        sampleIdentifierPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "sample";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<Sample> samples)
    {
        IProjectAuthorization<Sample> pa = new ProjectAuthorizationBuilder<Sample>()
                .withData(authorizationDataProvider)
                .withUser(new UserProviderFromPersonPE(person))
                .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                .withObjects(new ProjectsProviderFromSampleV1Collection(samples))
                .build();

        if (pa.getObjectsWithoutAccess().isEmpty())
        {
            return Status.OK;
        }

        // All fields relevant for authorization are expected to be filled:
        // - technical id
        // - permanent id
        // - space code
        // - identifier

        Collection<Sample> remainingSamples = pa.getObjectsWithoutAccess();

        final List<Long> ids = new ArrayList<Long>(remainingSamples.size());
        final List<String> permIds = new ArrayList<String>(remainingSamples.size());
        for (Sample sample : remainingSamples)
        {
            if (sample.getId() == null)
            {
                throw new AuthorizationFailureException("id is undefined.");
            }
            ids.add(sample.getId());
            if (sample.getPermId() == null)
            {
                throw new AuthorizationFailureException("permId is undefined.");
            }
            permIds.add(sample.getPermId());

            if (sample.getSpaceCode() != null) // == null represents a shared sample
            {
                final Status status =
                        evaluate(allowedRoles, person, sample.getSpaceCode());
                if (Status.OK.equals(status) == false)
                {
                    return status;
                }
            }

            final SampleIdentifier sampleIdentifier =
                    SampleIdentifierFactory.parse(sample.getIdentifier());
            final Status status = sampleIdentifierPredicate.evaluate(person, allowedRoles, sampleIdentifier);
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        for (String spaceCode : getSampleSpaceCodes(ids, permIds))
        {
            if (spaceCode == null)
            {
                continue; // Shared samples will return a spaceCode of null.
            }
            final Status status =
                    evaluate(allowedRoles, person, spaceCode);
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        return Status.OK;
    }

    private Collection<String> getSampleSpaceCodes(final List<Long> ids, final List<String> permIds)
    {
        if (ids.size() != permIds.size())
        {
            throw new IllegalArgumentException("Expect to get the same number of ids and permIds.");
        }
        final int size = ids.size();
        if (size == 0)
        {
            return Collections.emptyList();
        }
        if (size > ARRAY_SIZE_LIMIT)
        {
            final Set<String> spaceCodes = new HashSet<String>(size);
            for (int startIdx = 0; startIdx < size; startIdx += ARRAY_SIZE_LIMIT)
            {
                final List<Long> idSubList = ids.subList(startIdx,
                        Math.min(size, startIdx + ARRAY_SIZE_LIMIT));
                final List<String> permIdSubList = permIds.subList(startIdx,
                        Math.min(size, startIdx + ARRAY_SIZE_LIMIT));
                spaceCodes.addAll(sampleToSpaceQuery.getSampleSpaceCodes(toArray(idSubList),
                        permIdSubList.toArray(new String[permIdSubList.size()])));
            }
            return spaceCodes;
        } else
        {
            return sampleToSpaceQuery.getSampleSpaceCodes(toArray(ids),
                    permIds.toArray(new String[size]));
        }
    }

    private long[] toArray(List<Long> list)
    {
        final long[] result = new long[list.size()];
        for (int i = 0; i < result.length; ++i)
        {
            result[i] = list.get(i);
        }
        return result;
    }

}
