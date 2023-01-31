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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.common.parser.BeanProperty;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * An object that represents a sample/data set pair defined in a file
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleDataSetPair
{
    /**
     * How was the SampleDataSetPair processed?
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static enum SampleDataSetPairProcessing
    {
        PENDING, /* Has not been processed yet, the initial state */
        REGISTERED_SAMPLE_AND_DATA_SET, /* Sample was created and a new data set was created */
        UPDATED_SAMPLE_REGISTERED_DATA_SET, /* A sample was updated and a data set was created */
        SKIPPED, /* This sample/data set pair was skipped */
        FAILED
        /* This sample/data set was not successfully processed */
    }

    private final static String SAMPLE_IDENTIFIER = "S_identifier";

    private final static String SAMPLE_CONTAINER = "S_container";

    private final static String SAMPLE_PARENT = "S_parent";

    private final static String SAMPLE_EXPERIMENT = "S_experiment";

    private final static String DATA_SET_CODE = "D_code";

    private final static String DATA_SET_FILE_TYPE = "D_file_type";

    private final static String FOLDER = "FOLDER";

    private final NewSample newSample;

    private final DataSetInformation dataSetInformation;

    private String[] tokens;

    private String folderName;

    private String fileFormatTypeCode;

    private SampleDataSetPairProcessing processingState = SampleDataSetPairProcessing.PENDING;

    public SampleDataSetPair()
    {
        newSample = new NewSample();
        dataSetInformation = new DataSetInformation();
    }

    public String[] getTokens()
    {
        return tokens;
    }

    public void setTokens(String[] tokens)
    {
        this.tokens = tokens;
    }

    public NewSample getNewSample()
    {
        return newSample;
    }

    public DataSetInformation getDataSetInformation()
    {
        return dataSetInformation;
    }

    @BeanProperty(label = SAMPLE_IDENTIFIER, optional = true)
    public void setSampleIdentifier(String sampleIdentifier)
    {
        this.newSample.setIdentifier(sampleIdentifier);
    }

    @BeanProperty(label = SAMPLE_CONTAINER, optional = true)
    public void setSampleContainerIdentifier(String container)
    {
        this.newSample.setContainerIdentifier(container);
    }

    @BeanProperty(label = SAMPLE_PARENT, optional = true)
    public void setSampleParent(String parent)
    {
        this.newSample.setParents(parent);
    }

    @BeanProperty(label = SAMPLE_EXPERIMENT, optional = true)
    public void setSampleExperimentIdentifier(String experimentIdentifier)
    {
        this.newSample.setExperimentIdentifier(experimentIdentifier);
        this.dataSetInformation.setExperimentIdentifier(new ExperimentIdentifierFactory(
                experimentIdentifier).createIdentifier());
    }

    @BeanProperty(label = DATA_SET_CODE, optional = true)
    public void setDataSetCode(String code)
    {
        dataSetInformation.setDataSetCode(code);
    }

    public String getFileFormatTypeCode()
    {
        return fileFormatTypeCode;
    }

    @BeanProperty(label = DATA_SET_FILE_TYPE, optional = true)
    public void setFileFormatTypeCode(String fileFormatType)
    {
        this.fileFormatTypeCode = fileFormatType;
    }

    public String getFolderName()
    {
        return folderName;
    }

    @BeanProperty(label = FOLDER, optional = false)
    public void setFolderName(String folderName)
    {
        this.folderName = folderName;
    }

    public void setSampleProperties(IEntityProperty[] properties)
    {
        newSample.setProperties(properties);
    }

    public void setDataSetProperties(List<NewProperty> properties)
    {
        dataSetInformation.setDataSetProperties(properties);
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("sampleIdentifier", newSample.getIdentifier());
        builder.append("sampleProperties", newSample.getProperties());
        builder.append("dataSetInformation", getDataSetInformation());
        return builder.toString();
    }

    public SampleUpdatesDTO getSampleUpdates(Sample sample)
    {
        List<NewAttachment> attachments = Collections.emptyList();
        SampleUpdatesDTO sampleUpdates = new SampleUpdatesDTO(TechId.create(sample), // db id
                Arrays.asList(newSample.getProperties()), // List<IEntityProperty>
                getExperimentIdentifier(), // ExperimentIdentifier
                null, // TODO: ProjectIdentifier
                attachments, // Collection<NewAttachment>
                sample.getVersion(), // Sample version
                getSampleIdentifier(), // Sample Identifier
                newSample.getContainerIdentifier(), // Container Identifier
                newSample.getParentsOrNull() // Parent Identifiers
                );
        return sampleUpdates;
    }

    private SampleIdentifier getSampleIdentifier()
    {
        return new SampleIdentifierFactory(newSample.getIdentifier()).createIdentifier();
    }

    private ExperimentIdentifier getExperimentIdentifier()
    {
        return dataSetInformation.getExperimentIdentifier();
    }

    /**
     * How was the this SampleDataSetPair processed? This is set by the SampleAndDataSetRegistrator.
     */
    public SampleDataSetPairProcessing getProcessingApplied()
    {
        return processingState;
    }

    public void setProcessingApplied(SampleDataSetPairProcessing processingApplied)
    {
        this.processingState = processingApplied;
    }

}
