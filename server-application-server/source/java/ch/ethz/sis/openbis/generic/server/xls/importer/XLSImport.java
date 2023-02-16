/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.importer;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ScriptTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.handler.VersionInfoHandler;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.DatasetTypeImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.ExperimentImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.ExperimentTypeImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.ProjectImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.PropertyAssignmentImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.PropertyTypeImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.SampleImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.SampleTypeImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.ScriptImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.SemanticAnnotationImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.SpaceImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.VocabularyImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.VocabularyTermImportHelper;
import ch.ethz.sis.openbis.generic.server.xls.importer.handler.ExcelParser;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.DatabaseConsistencyChecker;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class XLSImport
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, XLSImport.class);

    private final String sessionToken;

    private final IApplicationServerApi api;

    private final DelayedExecutionDecorator delayedExecutor;

    private final ImportOptions options;

    private final Map<String, Integer> beforeVersions;

    private final Map<String, Integer> afterVersions;

    private final VocabularyImportHelper vocabularyHelper;

    private final VocabularyTermImportHelper vocabularyTermHelper;

    private final SampleTypeImportHelper sampleTypeHelper;

    private final ExperimentTypeImportHelper experimentTypeHelper;

    private final DatasetTypeImportHelper datasetTypeHelper;

    private final SpaceImportHelper spaceHelper;

    private final ProjectImportHelper projectHelper;

    private final ExperimentImportHelper experimentHelper;

    private final SampleImportHelper sampleHelper;

    private final PropertyTypeImportHelper propertyHelper;

    private final PropertyAssignmentImportHelper propertyAssignmentHelper;

    private final ScriptImportHelper scriptHelper;

    private final SemanticAnnotationImportHelper semanticAnnotationImportHelper;

    private final DatabaseConsistencyChecker dbChecker;

    public XLSImport(String sessionToken, IApplicationServerApi api, Map<String, String> scripts, ImportModes mode, ImportOptions options, String ignoredXLSName)
    {
        this.sessionToken = sessionToken;
        this.api = api;
        this.options = options;
        this.beforeVersions = Collections.unmodifiableMap(VersionInfoHandler.loadAllVersions(options));
        this.afterVersions = VersionInfoHandler.loadAllVersions(options);
        this.dbChecker = new DatabaseConsistencyChecker(this.sessionToken, this.api, this.afterVersions);
        this.delayedExecutor = new DelayedExecutionDecorator(this.sessionToken, this.api);

        this.vocabularyHelper = new VocabularyImportHelper(this.delayedExecutor, mode, options, afterVersions);
        this.vocabularyTermHelper = new VocabularyTermImportHelper(this.delayedExecutor, mode, options, afterVersions);
        this.sampleTypeHelper = new SampleTypeImportHelper(this.delayedExecutor, mode, options, afterVersions);
        this.experimentTypeHelper = new ExperimentTypeImportHelper(this.delayedExecutor, mode, options, afterVersions);
        this.datasetTypeHelper = new DatasetTypeImportHelper(this.delayedExecutor, mode, options, afterVersions);
        this.spaceHelper = new SpaceImportHelper(this.delayedExecutor, mode, options);
        this.projectHelper = new ProjectImportHelper(this.delayedExecutor, mode, options);
        this.experimentHelper = new ExperimentImportHelper(this.delayedExecutor, mode, options);
        this.sampleHelper = new SampleImportHelper(this.delayedExecutor, mode, options);
        this.propertyHelper = new PropertyTypeImportHelper(this.delayedExecutor, mode, options, afterVersions);
        this.propertyAssignmentHelper = new PropertyAssignmentImportHelper(this.delayedExecutor, mode, options, beforeVersions);
        this.scriptHelper = new ScriptImportHelper(this.delayedExecutor, mode, options, scripts);
        this.semanticAnnotationImportHelper = new SemanticAnnotationImportHelper(this.delayedExecutor, mode, options);
    }

    public List<IObjectId> importXLS(byte xls[])
    {
        this.dbChecker.checkVersionsOnDataBase();

        List<List<List<String>>> lines = ExcelParser.parseExcel(xls);
        int pageNumber = 0;
        int lineNumber;

        while (pageNumber < lines.size())
        {
            lineNumber = 0;
            List<List<String>> page = lines.get(pageNumber);
            int pageEnd = getPageEnd(page);
            while (lineNumber < pageEnd)
            {
                int blockEnd = getBlockEnd(page, lineNumber);
                ImportTypes blockType;
                try
                {
                    blockType = ImportTypes.valueOf(page.get(lineNumber).get(0));
                } catch (Exception e)
                {
                    throw new UserFailureException(
                            "Exception at page " + (pageNumber + 1) + " and line " + (lineNumber + 1) + " with message: " + e.getMessage());
                }
                lineNumber++;

                switch (blockType)
                {
                    case VOCABULARY_TYPE:
                        vocabularyHelper.importBlock(page, pageNumber, lineNumber, lineNumber + 2);
                        if (lineNumber + 2 != blockEnd)
                        {
                            vocabularyTermHelper.importBlock(page, pageNumber, lineNumber, blockEnd);
                        }
                        break;
                    case SAMPLE_TYPE:
                        // parse and create scripts
                        scriptHelper.importBlock(page, pageNumber, lineNumber, lineNumber + 2, ScriptTypes.VALIDATION_SCRIPT);
                        // parse and create sample type
                        sampleTypeHelper.importBlock(page, pageNumber, lineNumber, lineNumber + 2);
                        semanticAnnotationImportHelper.importBlockForEntityType(page, pageNumber, lineNumber, lineNumber + 2, ImportTypes.SAMPLE_TYPE);
                        // parse and assignment properties
                        if (lineNumber + 2 != blockEnd)
                        {
                            scriptHelper.importBlock(page, pageNumber, lineNumber + 2, blockEnd, ScriptTypes.DYNAMIC_SCRIPT);
                            propertyHelper.importBlock(page, pageNumber, lineNumber + 2, blockEnd);
                            propertyAssignmentHelper.importBlock(page, pageNumber, lineNumber, blockEnd, ImportTypes.SAMPLE_TYPE);
                            semanticAnnotationImportHelper.importBlockForEntityTypeProperty(page, pageNumber, lineNumber, blockEnd, ImportTypes.SAMPLE_TYPE);
                        }
                        break;
                    case EXPERIMENT_TYPE:
                        // parse and create scripts
                        scriptHelper.importBlock(page, pageNumber, lineNumber, lineNumber + 2, ScriptTypes.VALIDATION_SCRIPT);
                        // parse and create experiment type
                        experimentTypeHelper.importBlock(page, pageNumber, lineNumber, lineNumber + 2);
                        semanticAnnotationImportHelper.importBlockForEntityType(page, pageNumber, lineNumber, lineNumber + 2, ImportTypes.EXPERIMENT_TYPE);
                        // parse and assignment properties
                        if (lineNumber + 2 != blockEnd)
                        {
                            scriptHelper.importBlock(page, pageNumber, lineNumber + 2, blockEnd, ScriptTypes.DYNAMIC_SCRIPT);
                            propertyHelper.importBlock(page, pageNumber, lineNumber + 2, blockEnd);
                            propertyAssignmentHelper.importBlock(page, pageNumber, lineNumber, blockEnd, ImportTypes.EXPERIMENT_TYPE);
                            semanticAnnotationImportHelper.importBlockForEntityTypeProperty(page, pageNumber, lineNumber, blockEnd, ImportTypes.EXPERIMENT_TYPE);
                        }
                        break;
                    case DATASET_TYPE:
                        // parse and create scripts
                        scriptHelper.importBlock(page, pageNumber, lineNumber, lineNumber + 2, ScriptTypes.VALIDATION_SCRIPT);
                        // parse and create dataset type
                        datasetTypeHelper.importBlock(page, pageNumber, lineNumber, lineNumber + 2);
                        semanticAnnotationImportHelper.importBlockForEntityType(page, pageNumber, lineNumber, lineNumber + 2, ImportTypes.DATASET_TYPE);
                        // parse and assignment properties
                        if (lineNumber + 2 != blockEnd)
                        {
                            scriptHelper.importBlock(page, pageNumber, lineNumber + 2, blockEnd, ScriptTypes.DYNAMIC_SCRIPT);
                            propertyHelper.importBlock(page, pageNumber, lineNumber + 2, blockEnd);
                            propertyAssignmentHelper.importBlock(page, pageNumber, lineNumber, blockEnd, ImportTypes.DATASET_TYPE);
                            semanticAnnotationImportHelper.importBlockForEntityTypeProperty(page, pageNumber, lineNumber, blockEnd, ImportTypes.DATASET_TYPE);
                        }
                        break;
                    case SPACE:
                        spaceHelper.importBlock(page, pageNumber, lineNumber, blockEnd);
                        break;
                    case PROJECT:
                        projectHelper.importBlock(page, pageNumber, lineNumber, blockEnd);
                        break;
                    case EXPERIMENT:
                        experimentHelper.importBlock(page, pageNumber, lineNumber, blockEnd);
                        break;
                    case SAMPLE:
                        sampleHelper.importBlock(page, pageNumber, lineNumber, blockEnd);
                        break;
                    case PROPERTY_TYPE:
                        propertyHelper.importBlock(page, pageNumber, lineNumber, blockEnd);
                        semanticAnnotationImportHelper.importBlockForPropertyType(page, pageNumber, lineNumber, blockEnd);
                        break;
                    default:
                        throw new UserFailureException("Unknown type: " + blockType);
                }
                lineNumber = blockEnd + 1;
            }
            pageNumber++;
        }

        this.delayedExecutor.hasFinished();

        VersionInfoHandler.writeAllVersions(options, afterVersions);
        return new ArrayList<>(this.delayedExecutor.getIds());
    }

    private int getPageEnd(List<List<String>> page)
    {
        int pageEnd = page.size();

        boolean prevLineIsEmpty = false;

        for (int i = 0; i < page.size(); ++i)
        {
            boolean curLineIsEmpty = isLineEmpty(page.get(i));

            if (prevLineIsEmpty && curLineIsEmpty)
            {
                pageEnd = i - 2;

                for (int bankIndex = pageEnd + 1; bankIndex < page.size(); bankIndex++)
                {
                    if (!isLineEmpty(page.get(bankIndex)))
                    {
                        throw new UserFailureException("Content found after a double blank row that should mark the end of a page.");
                    }
                }

                break;
            }
            prevLineIsEmpty = curLineIsEmpty;
        }

        return pageEnd;
    }

    private int getBlockEnd(List<List<String>> page, int start)
    {
        for (int i = start; i < page.size(); ++i)
        {
            if (isLineEmpty(page.get(i)))
            {
                return i;
            }
        }
        return page.size();
    }

    private boolean isLineEmpty(List<String> line)
    {
        for (String cell : line)
        {
            if (cell != null && !cell.trim().isEmpty())
            {
                return false;
            }
        }
        return true;
    }
}