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

import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A PE for retrieving only the information necessary to determine if a user/person can access a data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Entity
@SqlResultSetMapping(name = DataSetAccessPE.RESULT_SET_MAPPING, entities = @EntityResult(entityClass = DataSetAccessPE.class))
@NamedNativeQueries({
        @NamedNativeQuery(name = DataSetAccessPE.DATASET_ACCESS_BY_TECH_IDS_QUERY_NAME, query = DataSetAccessPE.DATASET_ACCESS_BY_TECH_IDS_QUERY, resultSetMapping = DataSetAccessPE.RESULT_SET_MAPPING),
        @NamedNativeQuery(name = DataSetAccessPE.DATASET_ACCESS_BY_CODES_QUERY_NAME, query = DataSetAccessPE.DATASET_ACCESS_BY_CODES_QUERY, resultSetMapping = DataSetAccessPE.RESULT_SET_MAPPING),
        @NamedNativeQuery(name = DataSetAccessPE.DELETED_DATASET_ACCESS_QUERY_NAME, query = DataSetAccessPE.DELETED_DATASET_ACCESS_QUERY, resultSetMapping = DataSetAccessPE.RESULT_SET_MAPPING) })
public class DataSetAccessPE
{

    private final static String DATASET_ACCESS_QUERY_PART_1 =
            "SELECT d.id as dataSetId, d.code as dataSetCode, ep.code as experimentProjectCode, es.code as experimentSpaceCode, ss.code as sampleSpaceCode, "
                    + "sep.code as sampleExperimentProjectCode, ses.code as sampleExperimentSpaceCode, "
                    + "sp.code as sampleProjectCode, sps.code as sampleProjectSpaceCode "
                    + "FROM ";

    private final static String DATASET_ACCESS_QUERY_PART_2 = " d left outer join "
            + TableNames.EXPERIMENTS_ALL_TABLE
            + " e on d.expe_id = e.id left outer join "
            + TableNames.PROJECTS_TABLE
            + " ep on e.proj_id = ep.id left outer join "
            + TableNames.SPACES_TABLE
            + " es on ep.space_id = es.id left outer join "
            + TableNames.SAMPLES_ALL_TABLE
            + " s on d.samp_id = s.id left outer join "
            + TableNames.SPACES_TABLE
            + " ss on s.space_id = ss.id left outer join "
            + TableNames.EXPERIMENTS_ALL_TABLE
            + " se on s.expe_id = se.id left outer join "
            + TableNames.PROJECTS_TABLE
            + " sep on se.proj_id = sep.id left outer join "
            + TableNames.SPACES_TABLE
            + " ses on sep.space_id = ses.id left outer join "
            + TableNames.PROJECTS_TABLE
            + " sp on s.proj_id = sp.id left outer join "
            + TableNames.SPACES_TABLE
            + " sps on sp.space_id = sps.id ";

    public final static String DATASET_ACCESS_BY_TECH_IDS_QUERY =
            DATASET_ACCESS_QUERY_PART_1 + TableNames.DATA_VIEW + DATASET_ACCESS_QUERY_PART_2 + "WHERE d.id in (:ids)";

    public final static String DATASET_ACCESS_BY_CODES_QUERY =
            DATASET_ACCESS_QUERY_PART_1 + TableNames.DATA_VIEW + DATASET_ACCESS_QUERY_PART_2 + "WHERE d.code in (:codes)";

    public final static String DELETED_DATASET_ACCESS_QUERY =
            DATASET_ACCESS_QUERY_PART_1 + TableNames.DELETED_DATA_VIEW + DATASET_ACCESS_QUERY_PART_2 + "WHERE d.del_id in (:del_ids)";

    public final static String DATASET_ACCESS_BY_TECH_IDS_QUERY_NAME = "dataset_access_by_tech_ids";

    public final static String DATASET_ACCESS_BY_CODES_QUERY_NAME = "dataset_access_by_codes";

    public final static String DELETED_DATASET_ACCESS_QUERY_NAME = "deleted_dataset_access";

    public final static String DATA_SET_IDS_PARAMETER_NAME = "ids";

    public final static String DATA_SET_CODES_PARAMETER_NAME = "codes";

    public final static String DELETION_IDS_PARAMETER_NAME = "del_ids";

    public final static String RESULT_SET_MAPPING = "dataset_access_implicit";

    private Long dataSetId;

    private String dataSetCode;

    private String experimentProjectCode;

    private String experimentSpaceCode;

    private String sampleSpaceCode;

    private String sampleExperimentProjectCode;

    private String sampleExperimentSpaceCode;

    private String sampleProjectCode;

    private String sampleProjectSpaceCode;

    private boolean group;

    /**
     * A factory method that should only be used for testing.
     */
    public static DataSetAccessPE createDataSetAccessPEForTest(String dataSetId,
            String dataSetCode, String groupCode)
    {
        DataSetAccessPE newMe = new DataSetAccessPE();
        newMe.setExperimentSpaceCode(groupCode);
        return newMe;
    }

    @Transient
    public SpaceIdentifier getSpaceIdentifier()
    {
        if (getExperimentSpaceCode() != null)
        {
            return new SpaceIdentifier(getExperimentSpaceCode());
        } else if (getSampleSpaceCode() != null)
        {
            return new SpaceIdentifier(getSampleSpaceCode());
        } else if (getSampleProjectSpaceCode() != null)
        {
            return new SpaceIdentifier(getSampleProjectSpaceCode());
        } else if (getSampleExperimentSpaceCode() != null)
        {
            return new SpaceIdentifier(getSampleExperimentSpaceCode());
        } else
        {
            return null;
        }
    }

    @Transient
    public ProjectIdentifier getProjectIdentifier()
    {
        if (getExperimentSpaceCode() != null && getExperimentProjectCode() != null)
        {
            return new ProjectIdentifier(getExperimentSpaceCode(), getExperimentProjectCode());
        } else if (getSampleExperimentSpaceCode() != null && getSampleExperimentProjectCode() != null)
        {
            return new ProjectIdentifier(getSampleExperimentSpaceCode(), getSampleExperimentProjectCode());
        } else if (getSampleProjectSpaceCode() != null && getSampleProjectCode() != null)
        {
            return new ProjectIdentifier(getSampleProjectSpaceCode(), getSampleProjectCode());
        } else
        {
            return null;
        }
    }

    @Id
    public Long getDataSetId()
    {
        return dataSetId;
    }

    public void setDataSetId(Long dataSetId)
    {
        this.dataSetId = dataSetId;
    }

    public String getDataSetCode()
    {
        return dataSetCode;
    }

    public void setDataSetCode(String dataSetCode)
    {
        this.dataSetCode = dataSetCode;
    }

    public String getExperimentProjectCode()
    {
        return experimentProjectCode;
    }

    public void setExperimentProjectCode(String experimentProjectCode)
    {
        this.experimentProjectCode = experimentProjectCode;
    }

    public String getExperimentSpaceCode()
    {
        return experimentSpaceCode;
    }

    public void setExperimentSpaceCode(String experimentSpaceCode)
    {
        this.experimentSpaceCode = experimentSpaceCode;
    }

    public String getSampleSpaceCode()
    {
        return sampleSpaceCode;
    }

    public void setSampleSpaceCode(String sampleSpaceCode)
    {
        this.sampleSpaceCode = sampleSpaceCode;
    }

    public String getSampleExperimentProjectCode()
    {
        return sampleExperimentProjectCode;
    }

    public void setSampleExperimentProjectCode(String sampleExperimentProjectCode)
    {
        this.sampleExperimentProjectCode = sampleExperimentProjectCode;
    }

    public String getSampleExperimentSpaceCode()
    {
        return sampleExperimentSpaceCode;
    }

    public void setSampleExperimentSpaceCode(String sampleExperimentSpaceCode)
    {
        this.sampleExperimentSpaceCode = sampleExperimentSpaceCode;
    }

    public String getSampleProjectCode()
    {
        return sampleProjectCode;
    }

    public void setSampleProjectCode(String sampleProjectCode)
    {
        this.sampleProjectCode = sampleProjectCode;
    }

    public String getSampleProjectSpaceCode()
    {
        return sampleProjectSpaceCode;
    }

    public void setSampleProjectSpaceCode(String sampleProjectSpaceCode)
    {
        this.sampleProjectSpaceCode = sampleProjectSpaceCode;
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
        if (obj instanceof DataSetAccessPE == false)
        {
            return false;
        }

        final DataSetAccessPE that = (DataSetAccessPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();

        if (isGroup())
        {
            builder.append(getDataSetCode(), that.getDataSetCode());
            builder.append(getExperimentProjectCode(), that.getExperimentProjectCode());
            builder.append(getExperimentSpaceCode(), that.getExperimentSpaceCode());
            builder.append(getSampleSpaceCode(), that.getSampleSpaceCode());
            builder.append(getSampleExperimentProjectCode(), that.getSampleExperimentProjectCode());
            builder.append(getSampleExperimentSpaceCode(), that.getSampleExperimentSpaceCode());
            builder.append(getSampleProjectCode(), that.getSampleProjectCode());
            builder.append(getSampleProjectSpaceCode(), that.getSampleProjectSpaceCode());
        } else
        {
            builder.append(getDataSetId(), that.getDataSetId());
        }

        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();

        if (isGroup())
        {
            builder.append(getDataSetCode());
            builder.append(getExperimentProjectCode());
            builder.append(getExperimentSpaceCode());
            builder.append(getSampleSpaceCode());
            builder.append(getSampleExperimentProjectCode());
            builder.append(getSampleExperimentSpaceCode());
            builder.append(getSampleProjectCode());
            builder.append(getSampleProjectSpaceCode());
        } else
        {
            builder.append(getDataSetId());
        }

        return builder.toHashCode();
    }

}
