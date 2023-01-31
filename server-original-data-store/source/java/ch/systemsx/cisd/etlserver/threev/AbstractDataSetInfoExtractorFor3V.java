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

package ch.systemsx.cisd.etlserver.threev;

import java.io.File;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.AbstractDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.DataSetNameEntitiesProvider;
import ch.systemsx.cisd.etlserver.DefaultDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractDataSetInfoExtractorFor3V extends AbstractDataSetInfoExtractor
{
    /**
     * Name of the property specifying the character which will be used to concatenate the entities specifying the data set code.
     */
    @Private
    static final String DATA_SET_CODE_ENTITIES_GLUE = "data-set-code-entities-glue";

    private static final String DEFAULT_DATA_SET_CODE_ENTITIES_GLUE = ".";

    private final DefaultDataSetInfoExtractor codeExtractor;

    private final int[] dataSetCodeIndices;

    private final String dataSetCodeEntitiesGlue;

    public AbstractDataSetInfoExtractorFor3V(final Properties properties,
            final String indicesPropertyName)
    {
        super(properties);
        codeExtractor = new DefaultDataSetInfoExtractor(properties);
        final String indicesAsString =
                PropertyUtils.getMandatoryProperty(properties, indicesPropertyName);
        final String[] indicesAsStringArray = StringUtils.split(indicesAsString, ", ");
        dataSetCodeIndices = new int[indicesAsStringArray.length];
        for (int i = 0; i < indicesAsStringArray.length; i++)
        {
            final String index = indicesAsStringArray[i];
            try
            {
                dataSetCodeIndices[i] = Integer.parseInt(index);
            } catch (final NumberFormatException ex)
            {
                throw new ConfigurationFailureException(i + 1 + ". index in property '"
                        + indicesPropertyName + "' isn't a number: " + indicesAsString);
            }
        }
        dataSetCodeEntitiesGlue =
                properties.getProperty(DATA_SET_CODE_ENTITIES_GLUE,
                        DEFAULT_DATA_SET_CODE_ENTITIES_GLUE);
    }

    //
    // AbstractCodeExtractor
    //

    @Override
    public DataSetInformation getDataSetInformation(final File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        final DataSetInformation dataSetInfo =
                codeExtractor.getDataSetInformation(incomingDataSetPath, openbisService);
        final DataSetNameEntitiesProvider entitiesProvider =
                new DataSetNameEntitiesProvider(incomingDataSetPath, entitySeparator,
                        stripExtension);
        final StringBuilder builder = new StringBuilder();
        for (final int index : dataSetCodeIndices)
        {
            if (builder.length() > 0)
            {
                builder.append(dataSetCodeEntitiesGlue);
            }
            builder.append(entitiesProvider.getEntity(index));
        }
        final String code = builder.toString();
        setCodeFor(dataSetInfo, code);
        return dataSetInfo;
    }

    protected abstract void setCodeFor(DataSetInformation dataSetInfo, String code);

}
