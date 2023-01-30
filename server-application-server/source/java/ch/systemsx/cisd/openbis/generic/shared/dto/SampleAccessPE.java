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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A PE for retrieving only the information necessary to determine if a user/person can access a sample.
 * 
 * @author Piotr Buczek
 */
@Entity
@SqlResultSetMapping(name = SampleAccessPE.RESULT_SET_MAPPING, entities = @EntityResult(entityClass = SampleAccessPE.class))
@NamedNativeQueries(value = {
        @NamedNativeQuery(name = SampleAccessPE.SAMPLE_ACCESS_BY_TECH_IDS_QUERY_NAME, query = SampleAccessPE.SAMPLE_ACCESS_BY_TECH_IDS_QUERY, resultSetMapping = SampleAccessPE.RESULT_SET_MAPPING),
        @NamedNativeQuery(name = SampleAccessPE.SAMPLE_ACCESS_BY_PERM_IDS_QUERY_NAME, query = SampleAccessPE.SAMPLE_ACCESS_BY_PERM_IDS_QUERY, resultSetMapping = SampleAccessPE.RESULT_SET_MAPPING),
        @NamedNativeQuery(name = SampleAccessPE.DELETED_SAMPLE_ACCESS_QUERY_NAME, query = SampleAccessPE.DELETED_SAMPLE_ACCESS_QUERY, resultSetMapping = SampleAccessPE.RESULT_SET_MAPPING) })
public class SampleAccessPE
{

    private final static String SAMPLE_ACCESS_QUERY_PART_1 =
            "SELECT g.code as spaceCode, p.code as projectCode, ep.code as experimentProjectCode, e.code as experimentCode, s.code as sampleCode, s.id as sampleId, c.code as containerCode FROM ";

    private final static String SAMPLE_ACCESS_QUERY_PART_2 = " s left outer join "
            + TableNames.SPACES_TABLE
            + " g on s.space_id = g.id left outer join "
            + TableNames.PROJECTS_TABLE
            + " p on s.proj_id = p.id left outer join "
            + TableNames.SAMPLES_ALL_TABLE
            + " c on s.samp_id_part_of = c.id left outer join "
            + TableNames.EXPERIMENTS_ALL_TABLE
            + " e on s.expe_id = e.id left outer join "
            + TableNames.PROJECTS_TABLE
            + " ep on e.proj_id = ep.id ";

    public final static String SAMPLE_ACCESS_BY_TECH_IDS_QUERY =
            SAMPLE_ACCESS_QUERY_PART_1 + TableNames.SAMPLES_VIEW + SAMPLE_ACCESS_QUERY_PART_2 + "WHERE s.id in (:ids)";

    public final static String SAMPLE_ACCESS_BY_PERM_IDS_QUERY =
            SAMPLE_ACCESS_QUERY_PART_1 + TableNames.SAMPLES_VIEW + SAMPLE_ACCESS_QUERY_PART_2 + "WHERE s.perm_id in (:ids)";

    public final static String DELETED_SAMPLE_ACCESS_QUERY =
            SAMPLE_ACCESS_QUERY_PART_1 + TableNames.DELETED_SAMPLES_VIEW + SAMPLE_ACCESS_QUERY_PART_2 + "WHERE s.del_id in (:del_ids)";

    public final static String SAMPLE_ACCESS_BY_TECH_IDS_QUERY_NAME = "sample_access_by_tech_ids";

    public final static String SAMPLE_ACCESS_BY_PERM_IDS_QUERY_NAME = "sample_access_by_perm_ids";

    public final static String DELETED_SAMPLE_ACCESS_QUERY_NAME = "deleted_sample_access";

    public final static String SAMPLE_IDS_PARAMETER_NAME = "ids";

    public final static String DELETION_IDS_PARAMETER_NAME = "del_ids";

    public final static String RESULT_SET_MAPPING = "sample_access_implicit";

    private String spaceCode;

    private String projectCode;

    private String experimentProjectCode;

    private String experimentCode;

    private Long sampleId;

    private String sampleCode;

    private String containerCode;

    private boolean group;

    public String getSpaceCode()
    {
        return spaceCode;
    }

    public void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    @Transient
    public SpaceIdentifier getSpaceIdentifier()
    {
        if (getSpaceCode() != null)
        {
            return new SpaceIdentifier(getSpaceCode());
        } else
        {
            return null;
        }
    }

    @Transient
    public ProjectIdentifier getProjectIdentifier()
    {
        if (getSpaceCode() != null)
        {
            if (getProjectCode() != null)
            {
                return new ProjectIdentifier(getSpaceCode(), getProjectCode());
            } else if (getExperimentProjectCode() != null)
            {
                return new ProjectIdentifier(getSpaceCode(), getExperimentProjectCode());
            }
        }

        return null;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    public String getExperimentProjectCode()
    {
        return experimentProjectCode;
    }

    public void setExperimentProjectCode(String experimentProjectCode)
    {
        this.experimentProjectCode = experimentProjectCode;
    }

    public String getExperimentCode()
    {
        return experimentCode;
    }

    public void setExperimentCode(String experimentCode)
    {
        this.experimentCode = experimentCode;
    }

    public void setSampleId(Long sampleId)
    {
        this.sampleId = sampleId;
    }

    @Id
    public Long getSampleId()
    {
        return sampleId;
    }

    public String getSampleCode()
    {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public String getContainerCode()
    {
        return containerCode;
    }

    public void setContainerCode(String containerCode)
    {
        this.containerCode = containerCode;
    }

    public void setGroup(boolean group)
    {
        this.group = group;
    }

    @Transient
    public boolean isGroup()
    {
        return group;
    }

    //
    // Object
    //

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SampleAccessPE == false)
        {
            return false;
        }

        final SampleAccessPE that = (SampleAccessPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();

        if (isGroup())
        {
            builder.append(getSpaceCode(), that.getSpaceCode());
            builder.append(getProjectCode(), that.getProjectCode());
            builder.append(getExperimentProjectCode(), that.getExperimentProjectCode());
            builder.append(getExperimentCode(), that.getExperimentCode());
            builder.append(getSampleCode(), that.getSampleCode());
            builder.append(getContainerCode(), that.getContainerCode());
        } else
        {
            builder.append(getSampleId(), that.getSampleId());
        }

        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();

        if (isGroup())
        {
            builder.append(getSpaceCode());
            builder.append(getProjectCode());
            builder.append(getExperimentProjectCode());
            builder.append(getExperimentCode());
            builder.append(getSampleCode());
            builder.append(getContainerCode());
        } else
        {
            builder.append(getSampleId());
        }

        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

}
