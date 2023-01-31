/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * Default implementation which assumes that the information can be extracted from the file name. Following information can be extracted for each
 * dataset:
 * <ul>
 * <li>Sample code
 * <li>Sample group code
 * <li>Parent data set code
 * <li>Data producer code
 * <li>Data production date
 * </ul>
 * The name is split into entities separated by the property {@link #ENTITY_SEPARATOR_PROPERTY_NAME} . It is assumed that each of the above-mentioned
 * pieces of information is one of these entities. The extractor can be configured by the following optional properties:
 * <table border="1" * * cellspacing="0" cellpadding="5">
 * <tr>
 * <th>Property</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><code>strip-file-extension</code></td>
 * <td><code>false</code></td>
 * <td>If <code>true</code> the file extension will be removed before extracting informations from the file name.</td>
 * </tr>
 * <tr>
 * <td><code>entity-separator</code></td>
 * <td><code>.</code></td>
 * <td>Character which separates entities in the file name. Whitespace characters are not allowed.</td>
 * </tr>
 * <tr>
 * <td><code>sub-entity-separator</code></td>
 * <td><code>&amp;</code></td>
 * <td>Character which separates sub entities of an entity. Whitespace characters are not allowed.</td>
 * </tr>
 * <tr>
 * <td><code>index-of-space-code</code></td>
 * <td><code>null</code></td>
 * <td>This should be a space to which the sample connected with the dataset belongs. If not specified, the default space code will be used if given,
 * otherwise a sample will be assumed to be shared.</td>
 * </tr>
 * <tr>
 * <td><code>space-code</code></td>
 * <td><code>null</code></td>
 * <td>Default space code of the sample. If unspecified and space code for a specific dataset is also unspecified, a shared sample is assumed.</td>
 * </tr>
 * <tr>
 * <td><code>index-of-sample-code</code></td>
 * <td><code>-1</code></td>
 * <td>Index of the entity which is interpreted as the sample code. It will be ignored <code>index-of-experiment-identifier</code> has been specified.
 * </td>
 * </tr>
 * <tr>
 * <td><code>index-of-experiment-identifier</code></td>
 * <td><code>&nbsp;</code></td>
 * <td>Index of the entity which is interpreted as the experiment identifier. If not specified no experiment identifier will be extracted. In this
 * case <code>index-of-sample-code</code> will be used.</td>
 * </tr>
 * <tr>
 * <td><code>index-of-parent-data-set-codes</code></td>
 * <td>&nbsp;</td>
 * <td>Index of the entity which is interpreted as parent data set codes. The codes have to be separated by the sub entity separator. If not specified
 * no parent data set codes will be extracted.</td>
 * </tr>
 * <tr>
 * <td><code>index-of-data-producer-code</code></td>
 * <td>&nbsp;</td>
 * <td>Index of the entity which is interpreted as the data producer code. If not specified no data producer code will be extracted.</td>
 * </tr>
 * <tr>
 * <td><code>index-of-data-production-date</code></td>
 * <td>&nbsp;</td>
 * <td>Index of the entity which is interpreted as the data production date. If not specified no data production date will be extracted.</td>
 * </tr>
 * <tr>
 * <td><code>data-production-date-format</code></td>
 * <td><code>yyyyMMddHHmmss</code></td>
 * <td>Format of the data production date. For the correct syntax see <a
 * href="http://java.sun.com/j2se/1.5.0/docs/api/java/text/SimpleDateFormat.html" >SimpleDateFormat</a>.</td>
 * </tr>
 * <tr>
 * <td><code>data-set-properties-file-name</code></td>
 * <td><code>&nbsp;</code></td>
 * <td>Path to a file inside a data set directory which contains data set properties.</td>
 * </tr>
 * </table>
 * The first entity has index 0, the second 1, etc. Using negative numbers one can specify entities from the end. Thus, -1 means the last entity, -2
 * the second last entity, etc.
 * 
 * @author Franz-Josef Elmer
 */
public class DefaultDataSetInfoExtractor extends AbstractDataSetInfoExtractor
{
    /** The name of the property to get sub entity separator from. */
    @Private
    static final String SUB_ENTITY_SEPARATOR_PROPERTY_NAME = "sub-entity-separator";

    /** The default sub entity separator. */
    protected static final char DEFAULT_SUB_ENTITY_SEPARATOR = '&';

    /**
     * Name of the property specifying the index of the entity which should be interpreted as the sample code.
     * <p>
     * Use a negative number to count from the end, e.g. <code>-1</code> to use the last entity as the sample code.
     * </p>
     */
    @Private
    static final String INDEX_OF_SAMPLE_CODE = "index-of-sample-code";

    /**
     * Name of the property specifying the index of the entity which should be interpreted as the experiment identifier.
     * <p>
     * Use a negative number to count from the end, e.g. <code>-1</code> to use the last entity as the experiment identifer.
     * </p>
     */
    @Private
    static final String INDEX_OF_EXPERIMENT_IDENTIFIER = "index-of-experiment-identifier";

    /**
     * Name of the property specifying the index of the entity which should be interpreted as parent data set codes.
     * <p>
     * Use a negative number to count from the end, e.g. <code>-1</code> to use the last entity as the sample code.
     * </p>
     */
    @Private
    static final String INDEX_OF_PARENT_DATA_SET_CODES = "index-of-parent-data-set-codes";

    /** Default index of sample code. */
    private static final int DEFAULT_INDEX_OF_SAMPLE_CODE = -1;

    /**
     * Name of the property specifying the index of the entity which should be interpreted as the group code. This should be a group to which the
     * sample connected with the dataset belongs. If not specified, the default group code will be used if given, otherwise a sample will be assumed
     * to be shared.
     * <p>
     * Use a negative number to count from the end.
     * </p>
     */
    @Private
    static final String INDEX_OF_GROUP_CODE = "index-of-space-code";

    /**
     * Name of the property specifying the index of the entity which should be interpreted as the data producer code.
     * <p>
     * Use a negative number to count from the end, e.g. <code>-1</code> to use the last entity as the data producer code.
     * </p>
     */
    @Private
    static final String INDEX_OF_DATA_PRODUCER_CODE = "index-of-data-producer-code";

    /**
     * Name of the property specifying the index of the entity which should be interpreted as the data production date.
     * <p>
     * Use a negative number to count from the end, e.g. <code>-1</code> to use the last entity as the data production date.
     * </p>
     */
    @Private
    static final String INDEX_OF_DATA_PRODUCTION_DATE = "index-of-data-production-date";

    /**
     * Name of the property specifying the format of the data production date.
     */
    @Private
    static final String DATA_PRODUCTION_DATE_FORMAT = "data-production-date-format";

    /** Default data production date format. */
    private static final String DEFAULT_DATA_PRODUCTION_DATE_FORMAT = "yyyyMMddHHmmss";

    @Private
    static final String DATA_SET_PROPERTIES_FILE_NAME_KEY = "data-set-properties-file-name";

    protected static final class Index
    {
        private final boolean undefined;

        private final int index;

        Index(Properties properties, String key)
        {
            this(properties, key, 0);
        }

        Index(Properties properties, String key, int defaultValue)
        {
            undefined = StringUtils.isBlank(properties.getProperty(key));
            index = PropertyUtils.getInt(properties, key, defaultValue);
        }

        public final boolean isUndefined()
        {
            return undefined;
        }

        public final int getIndex()
        {
            return index;
        }
    }

    private final int indexOfSampleCode;

    private final Index indexOfExperimentIdentifier;

    private final Index indexOfGroupCode;

    protected final Index indexOfParentDataSetCodes;

    private final Index indexOfDataProducerCode;

    private final Index indexOfDataProductionDate;

    private final SimpleDateFormat dateFormat;

    private final String dataSetPropertiesFileNameOrNull;

    private final char subEntitySeparator;

    public DefaultDataSetInfoExtractor(final Properties properties)
    {
        super(properties);
        subEntitySeparator =
                PropertyUtils.getChar(properties, SUB_ENTITY_SEPARATOR_PROPERTY_NAME,
                        DEFAULT_SUB_ENTITY_SEPARATOR);
        if (Character.isWhitespace(subEntitySeparator))
        {
            throw new ConfigurationFailureException(
                    "Sub entity separator is a whitespace character.");
        }
        if (subEntitySeparator == entitySeparator)
        {
            throw new ConfigurationFailureException("Entity separator '" + entitySeparator
                    + "' and sub entity separator '" + subEntitySeparator
                    + "' have to be different.");
        }
        indexOfSampleCode =
                PropertyUtils
                        .getInt(properties, INDEX_OF_SAMPLE_CODE, DEFAULT_INDEX_OF_SAMPLE_CODE);

        indexOfExperimentIdentifier = new Index(properties, INDEX_OF_EXPERIMENT_IDENTIFIER);
        indexOfGroupCode = new Index(properties, INDEX_OF_GROUP_CODE);
        indexOfParentDataSetCodes = new Index(properties, INDEX_OF_PARENT_DATA_SET_CODES);
        indexOfDataProducerCode = new Index(properties, INDEX_OF_DATA_PRODUCER_CODE);
        indexOfDataProductionDate = new Index(properties, INDEX_OF_DATA_PRODUCTION_DATE);

        dateFormat =
                new SimpleDateFormat(properties.getProperty(DATA_PRODUCTION_DATE_FORMAT,
                        DEFAULT_DATA_PRODUCTION_DATE_FORMAT));
        dataSetPropertiesFileNameOrNull = properties.getProperty(DATA_SET_PROPERTIES_FILE_NAME_KEY);
    }

    //
    // ICodeExtractor
    //

    @Override
    public DataSetInformation getDataSetInformation(final File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws EnvironmentFailureException,
            UserFailureException
    {
        assert incomingDataSetPath != null : "Incoming data set path can not be null.";
        final DataSetNameEntitiesProvider entitiesProvider =
                new DataSetNameEntitiesProvider(incomingDataSetPath, entitySeparator,
                        stripExtension);
        final DataSetInformation dataSetInformation = new DataSetInformation();
        String groupCode = extractGroupCode(entitiesProvider);
        ExperimentIdentifier experimentIdentifier =
                tryToExtractExperimentIdentifier(entitiesProvider, groupCode);
        dataSetInformation.setExperimentIdentifier(experimentIdentifier);
        if (experimentIdentifier == null)
        {
            dataSetInformation.setSampleCode(extractSampleCode(entitiesProvider));
        }
        dataSetInformation.setParentDataSetCodes(getParentDataSetCodes(entitiesProvider));
        dataSetInformation.setProducerCode(tryGetDataProducerCode(entitiesProvider));
        dataSetInformation.setProductionDate(tryGetDataProductionDate(entitiesProvider));
        dataSetInformation.setDataSetProperties(extractDataSetProperties(incomingDataSetPath,
                dataSetPropertiesFileNameOrNull));
        dataSetInformation.setSpaceCode(groupCode);
        dataSetInformation.setProjectCode(getProjectCode());
        return dataSetInformation;
    }

    private ExperimentIdentifier tryToExtractExperimentIdentifier(
            final DataSetNameEntitiesProvider entitiesProvider, String groupCode)
    {
        if (indexOfExperimentIdentifier.isUndefined())
        {
            return null;
        }
        String experimentIdentifier =
                entitiesProvider.getEntity(indexOfExperimentIdentifier.getIndex());

        String[] codes = StringUtils.split(experimentIdentifier, subEntitySeparator);
        ExperimentIdentifier identifier = new ExperimentIdentifier();
        if (codes.length > 0)
        {
            identifier.setExperimentCode(codes[codes.length - 1]);
        }
        if (codes.length > 1)
        {
            identifier.setProjectCode(codes[codes.length - 2]);
        }
        identifier.setSpaceCode(codes.length > 2 ? codes[codes.length - 3] : groupCode);
        return identifier;
    }

    private String extractSampleCode(final DataSetNameEntitiesProvider entitiesProvider)
    {
        // subEntitySeparator can be used instead of ':' for contained sample code
        // (':' is not an allowed char some OS and it is not achievable in Mac OS Finder)
        String sampleCode = entitiesProvider.getEntity(indexOfSampleCode);
        return sampleCode.replace(subEntitySeparator, ':');
    }

    private List<String> getParentDataSetCodes(final DataSetNameEntitiesProvider entitiesProvider)
    {
        if (indexOfParentDataSetCodes.isUndefined())
        {
            return Collections.emptyList();
        }
        String parentDataSetCodes =
                entitiesProvider.getEntity(indexOfParentDataSetCodes.getIndex(), false);
        String[] codes = StringUtils.split(parentDataSetCodes, subEntitySeparator);
        return Arrays.asList(codes);
    }

    private String extractGroupCode(DataSetNameEntitiesProvider entitiesProvider)
    {
        if (indexOfGroupCode.isUndefined())
        {
            return super.getGroupCode();
        } else
        {
            return entitiesProvider.getEntity(indexOfGroupCode.getIndex());
        }
    }

    private String tryGetDataProducerCode(
            final DataSetNameEntitiesProvider dataSetNameEntitiesProvider)
    {
        if (indexOfDataProducerCode.isUndefined())
        {
            return null;
        }
        return dataSetNameEntitiesProvider.getEntity(indexOfDataProducerCode.getIndex());
    }

    private Date tryGetDataProductionDate(
            final DataSetNameEntitiesProvider dataSetNameEntitiesProvider)
    {
        if (indexOfDataProductionDate.isUndefined())
        {
            return null;
        }
        final String dateString =
                dataSetNameEntitiesProvider.getEntity(indexOfDataProductionDate.getIndex());
        try
        {
            return dateFormat.parse(dateString);
        } catch (final ParseException e)
        {
            throw new UserFailureException("Could not parse data production date '" + dateString
                    + "' because it violates the following format: " + dateFormat.toPattern());
        }
    }

}