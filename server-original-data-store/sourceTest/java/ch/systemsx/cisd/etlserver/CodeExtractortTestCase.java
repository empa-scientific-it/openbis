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

package ch.systemsx.cisd.etlserver;

/**
 * @author Franz-Josef Elmer
 */
public abstract class CodeExtractortTestCase
{

    protected static final String ENTITY_SEPARATOR =
            DefaultDataSetInfoExtractor.ENTITY_SEPARATOR_PROPERTY_NAME;

    protected static final String SUB_ENTITY_SEPARATOR =
            DefaultDataSetInfoExtractor.SUB_ENTITY_SEPARATOR_PROPERTY_NAME;

    protected static final String INDEX_OF_SAMPLE_CODE =
            DefaultDataSetInfoExtractor.INDEX_OF_SAMPLE_CODE;

    protected static final String INDEX_OF_EXPERIMENT_IDENTIFIER =
            DefaultDataSetInfoExtractor.INDEX_OF_EXPERIMENT_IDENTIFIER;

    protected static final String INDEX_OF_PARENT_DATA_SET_CODE =
            DefaultDataSetInfoExtractor.INDEX_OF_PARENT_DATA_SET_CODES;

    protected static final String INDEX_OF_DATA_PRODUCER_CODE =
            DefaultDataSetInfoExtractor.INDEX_OF_DATA_PRODUCER_CODE;

    protected static final String INDEX_OF_DATA_PRODUCTION_DATE =
            DefaultDataSetInfoExtractor.INDEX_OF_DATA_PRODUCTION_DATE;

    protected static final String DATA_PRODUCTION_DATE_FORMAT =
            DefaultDataSetInfoExtractor.DATA_PRODUCTION_DATE_FORMAT;

    protected static final String DATA_SET_PROPERTIES_FILE_NAME_KEY =
            DefaultDataSetInfoExtractor.DATA_SET_PROPERTIES_FILE_NAME_KEY;

}
