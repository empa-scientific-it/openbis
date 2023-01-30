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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedSamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Some useful methods around identifiers.
 * 
 * @author Christian Ribeaud
 */
public final class IdentifierHelper
{

    private IdentifierHelper()
    {
        // Can not be instantiated.
    }

    /**
     * Creates a {@link SpaceIdentifier} from given <var>groupPE</var>.
     */
    public final static SpaceIdentifier createGroupIdentifier(final SpacePE groupPE)
    {
        assert groupPE != null : "Unspecified space";
        return new SpaceIdentifier(groupPE.getCode());
    }

    /**
     * Creates a {@link SampleIdentifier} from given <var>samplePE</var>.
     */
    public final static SampleIdentifier createSampleIdentifier(final SamplePE samplePE)
    {
        assert samplePE != null : "Unspecified sample";
        ProjectPE project = samplePE.getProject();
        final SpacePE space = samplePE.getSpace();
        final String sampleCode = extractCode(samplePE);
        return createSampleIdentifier(project, space, sampleCode);
    }

    public static final SampleIdentifier sample(final SamplePE sample)
    {
        SampleIdentifier sampleId;
        if (sample.getSpace() == null)
        {
            sampleId =
                    new SampleIdentifier(sample.getCode());
        } else
        {
            sampleId =
                    new SampleIdentifier(new SpaceIdentifier(sample.getSpace()
                            .getCode()), sample.getCode());
        }

        if (sample.getContainer() != null)
        {
            sampleId.addContainerCode(sample.getContainer().getCode());
        }

        return sampleId;
    }

    public static final SpaceIdentifier space(final SpacePE space)
    {
        return new SpaceIdentifier(space.getCode());
    }

    /**
     * Creates a {@link SampleIdentifier} from given <var>samplePE</var>.
     */
    public final static SampleIdentifier createSampleIdentifier(
            final DeletedSamplePE deletedSamplePE)
    {
        assert deletedSamplePE != null : "Unspecified sample";
        ProjectPE project = deletedSamplePE.getProject();
        final SpacePE space = deletedSamplePE.getSpace();
        final String sampleCode = deletedSamplePE.getCode();
        return createSampleIdentifier(project, space, sampleCode);
    }

    public final static SampleIdentifier createSampleIdentifier(SpaceIdentifier spaceIdentifier, String sampleCode, String sampleContainerCode)
    {
        String fullSampleCode = convertCode(sampleCode, sampleContainerCode);

        if (spaceIdentifier == null)
        {
            return new SampleIdentifier(fullSampleCode);
        } else
        {
            return new SampleIdentifier(spaceIdentifier, fullSampleCode);
        }
    }

    private static SampleIdentifier createSampleIdentifier(ProjectPE project, final SpacePE space, final String sampleCode)
    {
        if (space == null)
        {
            return new SampleIdentifier(sampleCode);
        }
        if (project != null)
        {
            SpaceIdentifier spaceIdentifier = createGroupIdentifier(project.getSpace());
            ProjectIdentifier projectIdentifier = new ProjectIdentifier(spaceIdentifier, project.getCode());
            return new SampleIdentifier(projectIdentifier, sampleCode);
        }
        return new SampleIdentifier(createGroupIdentifier(space), sampleCode);
    }

    /**
     * Creates a {@link SampleIdentifier} from given <var>sample</var>.
     * <p>
     * NOTE: Specified sample should have full code set (with container code part).
     */
    public static SampleIdentifier createSampleIdentifier(Sample sample)
    {
        assert sample != null : "Unspecified sample";
        Project project = sample.getProject();
        String sampleCode = sample.getCode();
        if (project != null)
        {
            Space space = project.getSpace();
            if (space == null)
            {
                throw new IllegalArgumentException("Missing space code of project sample " + sample);
            }
            return new SampleIdentifier(new ProjectIdentifier(space.getCode(), project.getCode()), sampleCode);
        }
        final Space space = sample.getSpace();
        if (space != null)
        {
            return new SampleIdentifier(new SpaceIdentifier(space.getCode()), sampleCode);
        } else
        {
            return new SampleIdentifier(sampleCode);
        }
    }

    /**
     * Extracts "sub" code from {@link SamplePE} that is in exactly the same as the one kept in DB (so the same one that is mapped with
     * {@link SamplePE#getCode()}).
     */
    public final static String extractSubCode(SamplePE samplePE)
    {
        return samplePE.getCode();
    }

    /**
     * Converts "sub" code from <var>sampleCode</var> that is in exactly the same as the one kept in DB.
     */
    public final static String convertSubCode(String sampleCode)
    {
        return sampleCode;
    }

    /**
     * Extracts "full" sample code from {@link SamplePE}. For contained samples has a prefix consisting of container sample DB code and a colon,
     * otherwise it is just sample DB code, where by "sample DB code" is the code kept in the DB.
     */
    public final static String extractCode(SamplePE samplePE)
    {
        final String subCode = extractSubCode(samplePE);

        final String code;
        if (samplePE.getContainer() != null
                && HibernateUtils.isInitialized(samplePE.getContainer()))
        {
            final String containerCode = samplePE.getContainer().getCode();
            code = containerCode + ":" + subCode;
        } else
        {
            code = subCode;
        }
        return code;
    }

    /**
     * Converts "full" sample code from the <var>sampleCode</var> and <var>containerCodeOrNull</var>. For contained samples (i.e.
     * <code>containerCodeOrNull != null</code>) has a prefix consisting of container sample DB code and a colon, otherwise it is just sample DB code,
     * where by "sample DB code" is the code kept in the DB.
     */
    public final static String convertCode(String sampleCode, String containerCodeOrNull)
    {
        final String subCode = convertSubCode(sampleCode);

        final String code;
        if (containerCodeOrNull != null)
        {
            final String containerCode = containerCodeOrNull;
            code = containerCode + ":" + subCode;
        } else
        {
            code = subCode;
        }
        return code;
    }

    /**
     * Creates a {@link ProjectIdentifier} from given <var>project</var>.
     */
    public final static ProjectIdentifier createProjectIdentifier(final ProjectPE project)
    {
        assert project != null : "Unspecified project";
        final SpacePE group = project.getSpace();
        final ProjectIdentifier identifier =
                new ProjectIdentifier(group.getCode(), project.getCode());
        return identifier;
    }

    /**
     * Creates a full {@link ProjectIdentifier} (i.e. identifier with database instance, space and project codes specified) from given
     * <var>project</var>.
     */
    public final static ProjectIdentifier createFullProjectIdentifier(final ProjectPE project)
    {
        assert project != null : "Unspecified project";
        return new ProjectIdentifier(project.getSpace().getCode(), project.getCode());
    }

    /**
     * Creates a full {@link ProjectIdentifier} (i.e. identifier with database instance, space and project codes specified) from given
     * <var>projectIdentifier</var> and <var>homeDatabaseInstance</var>.
     */
    public final static ProjectIdentifier createFullProjectIdentifier(final ProjectIdentifier projectIdentifier)
    {
        assert projectIdentifier != null : "Unspecified project identifier";

        return new ProjectIdentifier(projectIdentifier.getSpaceCode(), projectIdentifier.getProjectCode());
    }

    /**
     * Creates a {@link ExperimentIdentifier} from given <var>experiment</var>.
     */
    public final static ExperimentIdentifier createExperimentIdentifier(
            final ExperimentPE experiment)
    {
        assert experiment != null : "Unspecified experiment";
        final ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifier(createProjectIdentifier(experiment.getProject()),
                        experiment.getCode());
        return experimentIdentifier;
    }

    public static final List<SampleIdentifier> extractSampleIdentifiers(NewExperiment newExperiment)
    {
        final List<SampleIdentifier> result = new ArrayList<SampleIdentifier>();
        for (NewSamplesWithTypes samplesWithTypes : newExperiment.getNewSamples())
        {
            for (NewSample sample : samplesWithTypes.getNewEntities())
            {
                SampleIdentifier identifier = SampleIdentifierFactory.parse(sample);
                // if default container is not specified, use the "new container" if it's specified
                if (identifier.tryGetContainerCode() == null
                        && StringUtils.isEmpty(sample.getContainerIdentifier()) == false)
                {
                    final SampleIdentifier containerIdentifier =
                            SampleIdentifierFactory.parse(sample.getContainerIdentifier());
                    identifier.addContainerCode(containerIdentifier.getSampleCode());
                }
                result.add(identifier);
            }
        }
        return result;
    }

    public static final List<SampleIdentifier> extractSampleIdentifiers(String[] samples,
            String defaultSpace)
    {
        List<SampleIdentifier> sampleIdentifiers = new ArrayList<SampleIdentifier>();
        for (String s : samples)
        {
            sampleIdentifiers.add(SampleIdentifierFactory.parse(s, defaultSpace));
        }
        return sampleIdentifiers;
    }

    static public void fillAndCheckGroup(SampleIdentifier sample, String expectedGroupCode)
    {
        if (sample.isDatabaseInstanceLevel())
        {
            return;
        } else if (sample.isInsideHomeSpace())
        {
            sample.getSpaceLevel().setSpaceCode(expectedGroupCode);
        } else if (sample.getSpaceLevel().getSpaceCode().equalsIgnoreCase(expectedGroupCode))
        {
            return;
        } else
        {
            throw new UserFailureException(String.format(
                    "Sample '%s' does not belong to the space '%s'", sample, expectedGroupCode));
        }
    }

    static public void fillSpaceIfNotSpecified(SampleIdentifier sample, String defaultSpaceCode)
    {
        if (sample.isInsideHomeSpace())
        {
            sample.getSpaceLevel().setSpaceCode(defaultSpaceCode);
        }
    }

}
