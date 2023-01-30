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
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectsProviderFromExperimentV1Collection;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromRolesWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.Select;

/**
 * A predicate for lists of entities of {@link Experiment}s.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Bernd Rinn
 */
@ShouldFlattenCollections(value = false)
public class ExperimentListPredicate extends AbstractSpacePredicate<List<Experiment>>
{
    public static interface IExperimentToSpaceQuery extends BaseQuery
    {
        @Select(sql = "select distinct s.code from spaces s left join projects p on p.space_id = s.id left join experiments e on e.proj_id = p.id "
                + "where e.id = any(?{1}) union "
                + "select distinct s.code from spaces s left join projects p on p.space_id = s.id left join experiments e on e.proj_id = p.id "
                + "where e.perm_id = any(?{2})", parameterBindings = { LongArrayMapper.class, StringArrayMapper.class })
        public List<String> getExperimentSpaceCodes(long[] experimentIds, String[] experimentPermIds);
    }

    private final static int ARRAY_SIZE_LIMIT = 999;

    private IExperimentToSpaceQuery experimentToSpaceQuery =
            QueryTool.getManagedQuery(IExperimentToSpaceQuery.class);

    //
    // AbstractPredicate
    //

    @Override
    public String getCandidateDescription()
    {
        return "experiment";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<Experiment> experiments)
    {
        IProjectAuthorization<Experiment> pa = new ProjectAuthorizationBuilder<Experiment>()
                .withData(authorizationDataProvider)
                .withUser(new UserProviderFromPersonPE(person))
                .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                .withObjects(new ProjectsProviderFromExperimentV1Collection(experiments))
                .build();

        if (pa.getObjectsWithoutAccess().isEmpty())
        {
            return Status.OK;
        }

        // All fields relevant for authorization are expected to be filled:
        // - technical id
        // - permanent id
        // - identifier

        Collection<Experiment> remainingExperiments = pa.getObjectsWithoutAccess();

        final List<Long> ids = new ArrayList<Long>(remainingExperiments.size());
        final List<String> permIds = new ArrayList<String>(remainingExperiments.size());
        for (Experiment experiment : remainingExperiments)
        {
            if (experiment.getId() == null)
            {
                throw new AuthorizationFailureException("id is undefined.");
            }
            ids.add(experiment.getId());
            if (experiment.getPermId() == null)
            {
                throw new AuthorizationFailureException("permId is undefined.");
            }
            permIds.add(experiment.getPermId());

            final SpaceIdentifier spaceIdentifier =
                    new ExperimentIdentifierFactory(experiment.getIdentifier()).createIdentifier();
            final String spaceCode = SpaceCodeHelper.getSpaceCode(person, spaceIdentifier);
            final Status status =
                    evaluate(allowedRoles, person, spaceCode);
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        for (String spaceCode : getExperimentSpaceCodes(ids, permIds))
        {
            final Status status = evaluate(allowedRoles, person, spaceCode);
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        return Status.OK;
    }

    private Collection<String> getExperimentSpaceCodes(final List<Long> ids, final List<String> permIds)
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
                spaceCodes.addAll(experimentToSpaceQuery.getExperimentSpaceCodes(toArray(idSubList),
                        permIdSubList.toArray(new String[permIdSubList.size()])));
            }
            return spaceCodes;
        } else
        {
            return experimentToSpaceQuery.getExperimentSpaceCodes(toArray(ids),
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

    void setExperimentToSpaceQuery(IExperimentToSpaceQuery experimentToSpaceQuery)
    {
        this.experimentToSpaceQuery = experimentToSpaceQuery;
    }

}
