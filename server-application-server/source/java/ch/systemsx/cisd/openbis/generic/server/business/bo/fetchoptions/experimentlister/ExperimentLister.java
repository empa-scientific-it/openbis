/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.experimentlister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author pkupczyk
 */
public class ExperimentLister implements IExperimentLister
{

    private IDAOFactory daoFactory;

    private String baseIndexURL;

    private IExperimentListingQuery query;

    public ExperimentLister(IDAOFactory daoFactory, String baseIndexURL)
    {
        this(daoFactory, baseIndexURL, QueryTool.getManagedQuery(IExperimentListingQuery.class));
    }

    public ExperimentLister(IDAOFactory daoFactory, String baseIndexURL,
            IExperimentListingQuery query)
    {
        if (daoFactory == null)
        {
            throw new IllegalArgumentException("DaoFactory was null");
        }
        if (baseIndexURL == null)
        {
            throw new IllegalArgumentException("BaseIndexURL was null");
        }
        if (query == null)
        {
            throw new IllegalArgumentException("Query was null");
        }
        this.daoFactory = daoFactory;
        this.baseIndexURL = baseIndexURL;
        this.query = query;
    }

    @Override
    public List<Experiment> listExperiments(List<ExperimentIdentifier> experimentIdentifiers,
            ExperimentFetchOptions experimentFetchOptions)
    {
        if (experimentIdentifiers == null)
        {
            throw new IllegalArgumentException("ExperimentIdentifiers were null");
        }
        if (experimentFetchOptions == null)
        {
            throw new IllegalArgumentException("ExperimentFetchOptions were null");
        }
        if (!experimentFetchOptions.isSubsetOf(ExperimentFetchOption.BASIC))
        {
            throw new IllegalArgumentException("Currently only " + ExperimentFetchOption.BASIC
                    + " fetch option is supported by this method");
        }

        ExperimentIdentifiers identifiers = new ExperimentIdentifiers(experimentIdentifiers);

        DataIterator<ExperimentRecord> iterator =
                query.listExperiments(
                        identifiers.getSpaceCodes(), identifiers.getProjectCodes(),
                        identifiers.getExperimentCodes());

        return handleResults(iterator, identifiers);
    }

    @Override
    public List<Experiment> listExperimentsForProjects(List<ProjectIdentifier> projectIdentifiers,
            ExperimentFetchOptions experimentFetchOptions)
    {
        if (projectIdentifiers == null)
        {
            throw new IllegalArgumentException("ProjectIdentifiers were null");
        }
        if (experimentFetchOptions == null)
        {
            throw new IllegalArgumentException("ExperimentFetchOptions were null");
        }
        if (!experimentFetchOptions.isSubsetOf(ExperimentFetchOption.BASIC))
        {
            throw new IllegalArgumentException("Currently only " + ExperimentFetchOption.BASIC
                    + " fetch option is supported by this method");
        }

        ProjectIdentifiers identifiers = new ProjectIdentifiers(projectIdentifiers);

        DataIterator<ExperimentRecord> iterator =
                query.listExperimentsForProjects(
                        identifiers.getSpaceCodes(), identifiers.getProjectCodes());

        return handleResults(iterator, identifiers);
    }

    private List<Experiment> handleResults(DataIterator<ExperimentRecord> iterator,
            ProjectIdentifiers identifiers)
    {
        if (iterator != null)
        {
            List<ExperimentRecord> results = new LinkedList<ExperimentRecord>();
            while (iterator.hasNext())
            {
                results.add(iterator.next());
            }

            if (identifiers.size() > 1)
            {
                // We may need to filter out some of the records. When there are duplicated
                // codes among different spaces, projects or experiments the list methods
                // may return too many results. Therefore we have to verify the results really
                // match the identifiers list. Because it is a very rare case we
                // prefer to do it this way rather than make the query more complicated and
                // slower. It may only happen when there is more than one identifier
                // specified though.

                identifiers.filter(results);
            }

            return createExperiments(results);

        } else
        {
            return Collections.emptyList();
        }
    }

    private List<Experiment> createExperiments(List<ExperimentRecord> records)
    {
        List<Experiment> result = new ArrayList<Experiment>(records.size());
        for (ExperimentRecord record : records)
        {
            result.add(createExperiment(record));
        }
        return result;
    }

    private Experiment createExperiment(ExperimentRecord record)
    {
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifier(record.s_code, record.pr_code, record.e_code);

        Experiment experiment = new Experiment();
        experiment.setFetchOptions(new ExperimentFetchOptions());
        experiment.setId(record.e_id);
        experiment.setModificationDate(record.e_modification_timestamp);
        experiment.setCode(record.e_code);
        experiment.setPermId(record.e_perm_id);
        experiment.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL,
                EntityKind.EXPERIMENT, record.e_perm_id));
        experiment.setExperimentType(createExperimentType(record));
        experiment.setIdentifier(experimentIdentifier.toString());
        experiment.setProject(createProject(record));
        experiment.setRegistrator(createPerson(record));
        experiment.setModifier(createModifier(record));
        experiment.setRegistrationDate(record.e_registration_timestamp);
        experiment.setModificationDate(record.e_modification_timestamp);
        return experiment;
    }

    private ExperimentType createExperimentType(ExperimentRecord record)
    {
        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode(record.et_code);
        experimentType.setDescription(record.et_description);
        return experimentType;
    }

    private Space createSpace(ExperimentRecord record)
    {
        SpaceIdentifier spaceIdentifier =
                new SpaceIdentifier(record.s_code);

        Space space = new Space();
        space.setId(record.s_id);
        space.setCode(record.s_code);
        space.setDescription(record.s_description);
        space.setRegistrationDate(record.s_registration_timestamp);
        space.setIdentifier(spaceIdentifier.toString());
        return space;
    }

    private Person createPerson(ExperimentRecord record)
    {
        Person person = new Person();
        person.setFirstName(record.pe_first_name);
        person.setLastName(record.pe_last_name);
        person.setEmail(record.pe_email);
        person.setUserId(record.pe_user_id);
        person.setRegistrationDate(record.pe_registration_timestamp);
        return person;
    }

    private Person createModifier(ExperimentRecord record)
    {
        Person person = new Person();
        person.setFirstName(record.mod_first_name);
        person.setLastName(record.mod_last_name);
        person.setEmail(record.mod_email);
        person.setUserId(record.mod_user_id);
        person.setRegistrationDate(record.mod_registration_timestamp);
        return person;
    }

    private Project createProject(ExperimentRecord record)
    {
        ProjectIdentifier projectIdentifier =
                new ProjectIdentifier(record.s_code, record.pr_code);

        Project project = new Project();
        project.setId(record.pr_id);
        project.setPermId(record.pr_perm_id);
        project.setModificationDate(record.pr_modification_timestamp);
        project.setRegistrationDate(record.pr_registration_timestamp);
        project.setCode(record.pr_code);
        project.setDescription(record.pr_description);
        project.setSpace(createSpace(record));
        project.setRegistrationDate(record.pr_registration_timestamp);
        project.setIdentifier(projectIdentifier.toString());
        return project;
    }

    private class ProjectIdentifiers
    {
        private Set<ProjectIdentifier> identifiersSet;

        private Set<String> spaceCodes;

        private Set<String> projectCodes;

        public ProjectIdentifiers(List<? extends ProjectIdentifier> identifiers)
        {
            identifiersSet = new HashSet<ProjectIdentifier>(identifiers.size());
            spaceCodes = new HashSet<String>(identifiers.size());
            projectCodes = new HashSet<String>(identifiers.size());

            for (ProjectIdentifier identifier : identifiers)
            {
                identifiersSet.add(identifier);
                spaceCodes.add(identifier.getSpaceCode());
                projectCodes.add(identifier.getProjectCode());
            }
        }

        public String[] getSpaceCodes()
        {
            return spaceCodes.toArray(new String[spaceCodes.size()]);
        }

        public String[] getProjectCodes()
        {
            return projectCodes.toArray(new String[projectCodes.size()]);
        }

        public boolean contains(ProjectIdentifier identifier)
        {
            return identifiersSet.contains(identifier);
        }

        public int size()
        {
            return identifiersSet.size();
        }

        public void filter(List<ExperimentRecord> records)
        {
            Iterator<ExperimentRecord> iterator = records.iterator();
            while (iterator.hasNext())
            {
                ExperimentRecord record = iterator.next();
                ProjectIdentifier identifier =
                        new ProjectIdentifier(record.s_code, record.pr_code);
                if (!contains(identifier))
                {
                    iterator.remove();
                }
            }
        }
    }

    private class ExperimentIdentifiers extends ProjectIdentifiers
    {
        private Set<String> experimentCodes;

        public ExperimentIdentifiers(List<ExperimentIdentifier> identifiers)
        {
            super(identifiers);

            experimentCodes = new HashSet<String>(identifiers.size());

            for (ExperimentIdentifier identifier : identifiers)
            {
                experimentCodes.add(identifier.getExperimentCode());
            }
        }

        public String[] getExperimentCodes()
        {
            return experimentCodes.toArray(new String[experimentCodes.size()]);
        }

        @Override
        public void filter(List<ExperimentRecord> records)
        {
            Iterator<ExperimentRecord> iterator = records.iterator();
            while (iterator.hasNext())
            {
                ExperimentRecord record = iterator.next();
                ExperimentIdentifier identifier =
                        new ExperimentIdentifier(record.s_code, record.pr_code, record.e_code);
                if (!contains(identifier))
                {
                    iterator.remove();
                }
            }
        }

    }

}
