/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentNodeFilter;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Cache;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpFileFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverConfig;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverRegistry;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.FtpFileEvaluationContext.EvaluatedElement;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;

/**
 * Resolves paths like
 * 
 * <pre>
 *  /&lt;space-code>/&lt;project-code>/&lt;experiment-code>/&lt;dataset-template>[/[PARENT-&lt;dataset-template>|CHILD-&lt;dataset-template>|&lt;sub-path>]]*
 * </pre>
 * 
 * to {@link FtpFile} objects.
 * <p>
 * Subpaths are resolved as relative paths starting from the root of a dataset.
 * <p>
 * 
 * @author Kaloyan Enimanev
 */
public class TemplateBasedDataSetResourceResolver implements IFtpPathResolver,
        IExperimentChildrenLister
{
    private static final String DATA_SET_CODE_VARNAME = "dataSetCode";

    private static final String DATA_SET_TYPE_VARNAME = "dataSetType";

    private static final String DATA_SET_DATE_VARNAME = "dataSetDate";

    private static final String FILE_NAME_VARNAME = "fileName";

    private static final String DISAMBIGUATION_VARNAME = "disambiguation";

    private static final String DATA_SET_DATE_FORMAT = "yyyy-MM-dd-HH-mm";

    private static final String PARENT_PREFIX = "PARENT-";

    private static final String CHILD_PREFIX = "CHILD-";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            TemplateBasedDataSetResourceResolver.class);

    private final class DataSetFtpFolder extends AbstractFtpFolder
    {
        private final AbstractExternalData dataSet;

        private final FtpPathResolverContext resolverContext;

        private DataSetFtpFolder(String absolutePath, AbstractExternalData dataSet,
                FtpPathResolverContext resolverContext)
        {
            super(absolutePath);
            this.dataSet = dataSet;
            this.resolverContext = resolverContext;
            setLastModified(dataSet.getModificationDate().getTime());
        }

        @Override
        public List<FtpFile> unsafeListFiles() throws RuntimeException
        {
            List<FtpFile> result = new ArrayList<FtpFile>();
            if (showParentsAndChildren)
            {
                DataSet dataSetWithMetaData = resolverContext.getDataSet(dataSet.getCode());
                addNodesOfType(PARENT_PREFIX, result, dataSetWithMetaData.getParentCodes());
                addNodesOfType(CHILD_PREFIX, result, dataSetWithMetaData.getChildrenCodes());
            }
            FtpFileEvaluationContext evalContext = createFtpFileEvaluationContext(resolverContext);
            try
            {
                IHierarchicalContent hierarchicalContent =
                        evalContext.getHierarchicalContent(dataSet, true);
                IHierarchicalContentNode rootNode =
                        getDataSetFileListRoot(dataSet, hierarchicalContent);
                List<IHierarchicalContentNode> childNodes = rootNode.getChildNodes();
                Cache cache = resolverContext.getCache();
                for (IHierarchicalContentNode childNode : childNodes)
                {
                    IHierarchicalContentNodeFilter fileFilter = getFileFilter(dataSet);
                    if (fileFilter.accept(childNode))
                    {
                        result.add(FtpFileFactory.createFtpFile(dataSet.getCode(), absolutePath
                                + FtpConstants.FILE_SEPARATOR + childNode.getName(), childNode,
                                hierarchicalContent, fileFilter, cache));
                    }
                }
            } finally
            {
                evalContext.close();
            }
            return result;
        }

        private void addNodesOfType(String prefix, List<FtpFile> result, List<String> dataSetCodes)
        {
            if (dataSetCodes.isEmpty())
            {
                return;
            }
            List<AbstractExternalData> dataSets = resolverContext.listDataSetsByCode(dataSetCodes);
            for (int i = 0; i < dataSets.size(); i++)
            {
                AbstractExternalData ds = dataSets.get(i);
                String dataSetUniqueSuffix = evaluateTemplate(ds, null, computeDisambiguation(i));
                if (false == isPresentInPath(dataSetUniqueSuffix))
                {
                    String folderName = prefix + dataSetUniqueSuffix;
                    result.add(new DataSetFtpFolder(absolutePath + FtpConstants.FILE_SEPARATOR
                            + folderName, ds, resolverContext));
                }
            }
        }

        private boolean isPresentInPath(String suffix)
        {
            String dataSetUniqueSuffix = suffix + FtpConstants.FILE_SEPARATOR;
            String pathWithSeparator = absolutePath + FtpConstants.FILE_SEPARATOR;
            return pathWithSeparator.contains(dataSetUniqueSuffix);
        }
    }

    /**
     * helper class holding the configuration properties for a data set type.
     */
    private static class DataSetTypeConfig
    {
        /**
         * a regex specifying the root of the listable hierarchy within a data set.
         */
        String fileListSubPath = StringUtils.EMPTY;

        /**
         * a filter to be applied to files within a certain data set type.
         */
        IHierarchicalContentNodeFilter fileFilter = IHierarchicalContentNodeFilter.MATCH_ALL;
    }

    /**
     * a template, that can contain special variables.
     * 
     * @see #evaluateTemplate(AbstractExternalData, String) to find out what variables are understood and interpreted.
     */
    private final Template template;

    private final Map<String /* dataset type */, DataSetTypeConfig> dataSetTypeConfigs;

    private final DataSetTypeConfig defaultDSTypeConfig;

    private boolean showParentsAndChildren;

    private boolean fileNamePresent;

    private IHierarchicalContentProvider contentProvider;

    public TemplateBasedDataSetResourceResolver(FtpPathResolverConfig resolverConfig)
    {
        this.template = new Template(resolverConfig.getDataSetDisplayTemplate());
        showParentsAndChildren = resolverConfig.isShowParentsAndChildren();
        fileNamePresent = template.getPlaceholderNames().contains(FILE_NAME_VARNAME);
        if (fileNamePresent && showParentsAndChildren)
        {
            throw new ConfigurationFailureException("Template contains file name variable and "
                    + "the flag to show parents/children data sets is set.");
        }
        this.dataSetTypeConfigs = initializeDataSetTypeConfigs(resolverConfig);
        this.defaultDSTypeConfig = new DataSetTypeConfig();
    }

    void setContentProvider(IHierarchicalContentProvider contentProvider)
    {
        this.contentProvider = contentProvider;
    }

    /**
     * @return <code>true</code> for paths containing at least 4 nested directory levels.
     */
    @Override
    public boolean canResolve(String path)
    {
        int nestedLevels = StringUtils.countMatches(path, FtpConstants.FILE_SEPARATOR);
        return nestedLevels >= 4;
    }

    @Override
    public FtpFile resolve(final String path, final FtpPathResolverContext resolverContext)
    {
        String experimentId = extractExperimentIdFromPath(path);

        Experiment experiment = tryGetExperiment(experimentId, resolverContext);
        if (experiment == null)
        {
            return FtpPathResolverRegistry.getNonExistingFile(path, "Unknown experiment '"
                    + experimentId + "'.");
        }
        List<AbstractExternalData> dataSets = resolverContext.getDataSets(experiment);
        if (fileNamePresent)
        {
            FtpFileEvaluationContext evalContext = evaluateDataSetPaths(resolverContext, dataSets);
            try
            {
                return extractMatchingFile(path, experimentId, resolverContext.getCache(), evalContext);
            } finally
            {
                evalContext.close();
            }
        }
        return resolve(path, resolverContext, dataSets);
    }

    private FtpFile resolve(final String path, final FtpPathResolverContext resolverContext,
            List<AbstractExternalData> dataSets)
    {
        String[] pathElements =
                StringUtils.splitByWholeSeparatorPreserveAllTokens(path,
                        FtpConstants.FILE_SEPARATOR);
        FtpFile result = null;
        for (int i = 4; i < pathElements.length; i++)
        {
            String dataSetPathElement = pathElements[i];
            if (result == null)
            {
                AbstractExternalData dataSet = tryToFindDataSet(dataSets, dataSetPathElement);
                if (dataSet == null)
                {
                    return FtpPathResolverRegistry.getNonExistingFile(path,
                            "No match found for path element '" + dataSetPathElement + "'.");
                }
                String subPath =
                        StringUtils.join(pathElements, FtpConstants.FILE_SEPARATOR, 0, i + 1);
                result = new DataSetFtpFolder(subPath, dataSet, resolverContext);
            } else
            {
                List<FtpFile> files = result.listFiles();
                FtpFile matchingFile = null;
                for (FtpFile file : files)
                {
                    if (dataSetPathElement.equals(file.getName()))
                    {
                        matchingFile = file;
                        break;
                    }
                }
                if (matchingFile == null)
                {
                    return FtpPathResolverRegistry.getNonExistingFile(path,
                            "No match found for path element '" + dataSetPathElement + "'.");
                }
                result = matchingFile;
            }
        }
        return result;
    }

    private AbstractExternalData tryToFindDataSet(List<AbstractExternalData> dataSets, String dataSetPathElement)
    {
        for (int disambiguationIdx = 0; disambiguationIdx < dataSets.size(); disambiguationIdx++)
        {
            String disambiguationVar = computeDisambiguation(disambiguationIdx);
            AbstractExternalData dataSet = dataSets.get(disambiguationIdx);
            String pathElement = evaluateTemplate(dataSet, null, disambiguationVar);
            if (dataSetPathElement.equals(pathElement))
            {
                return dataSet;
            }
        }
        return null;
    }

    private FtpFile extractMatchingFile(String path, String experimentId, Cache cache,
            FtpFileEvaluationContext evalContext)
    {
        final EvaluatedElement matchingElement =
                tryFindMatchingEvalElement(path, experimentId, evalContext);
        if (matchingElement == null)
        {
            return FtpPathResolverRegistry.getNonExistingFile(path, "Resource '"
                    + path + "' for experiment " + experimentId + " does not exist.");

        }

        final String pathInDataSet = extractPathInDataSet(path);
        final String hierarchicalNodePath =
                constructHierarchicalNodePath(matchingElement.pathInDataSet, pathInDataSet);

        final AbstractExternalData dataSet = matchingElement.dataSet;
        final IHierarchicalContentNodeFilter fileFilter = getFileFilter(dataSet);
        final IHierarchicalContent content = evalContext.getHierarchicalContent(dataSet, true);
        final IHierarchicalContentNode contentNodeOrNull = content.tryGetNode(hierarchicalNodePath);
        if (contentNodeOrNull != null && fileFilter.accept(contentNodeOrNull))
        {
            return FtpFileFactory.createFtpFile(dataSet.getCode(), path, contentNodeOrNull,
                    content, fileFilter, dataSet.getModificationDate().getTime(), cache);
        } else
        {
            return FtpPathResolverRegistry.getNonExistingFile(path, "Resource '"
                    + hierarchicalNodePath + "' does not exist.");
        }
    }

    private EvaluatedElement tryFindMatchingEvalElement(String path, String experimentId,
            FtpFileEvaluationContext evalContext)
    {
        String pathWithEndSlash = path + FtpConstants.FILE_SEPARATOR;
        for (EvaluatedElement evalElement : evalContext.getEvalElements())
        {
            String fullEvaluatedPath =
                    experimentId + FtpConstants.FILE_SEPARATOR + evalElement.evaluatedTemplate
                            + FtpConstants.FILE_SEPARATOR;
            if (pathWithEndSlash.startsWith(fullEvaluatedPath))
            {
                return evalElement;
            }
        }
        return null;
    }

    /**
     * @param path the path we are trying to resolve
     * @return the nested path within the data set (i.e. under the dataset directory level)
     */
    private String extractPathInDataSet(String path)
    {
        String[] levels = StringUtils.split(path, FtpConstants.FILE_SEPARATOR);
        if (levels.length > 4)
        {
            return StringUtils.join(levels, FtpConstants.FILE_SEPARATOR, 4, levels.length);
        } else
        {
            return StringUtils.EMPTY;
        }
    }

    private String constructHierarchicalNodePath(String relativePath, String pathInDataSet)
    {
        String result = relativePath;
        if (false == StringUtils.isBlank(pathInDataSet))
        {
            if (StringUtils.isBlank(relativePath))
            {
                result = pathInDataSet;
            } else
            {
                result += FtpConstants.FILE_SEPARATOR + pathInDataSet;
            }
        }
        return result;
    }

    private Experiment tryGetExperiment(String experimentId, FtpPathResolverContext context)
    {
        try
        {
            return context.getExperiment(experimentId);
        } catch (Throwable t)
        {
            operationLog.warn("Failed to get experiment with identifier :" + experimentId, t);
            return null;
        }
    }

    private String extractExperimentIdFromPath(String path)
    {
        String[] levels = StringUtils.split(path, FtpConstants.FILE_SEPARATOR);
        String experimentId =
                FtpConstants.ROOT_DIRECTORY
                        + StringUtils.join(levels, FtpConstants.FILE_SEPARATOR, 0, 3);
        return experimentId;
    }

    /**
     * @see IExperimentChildrenLister
     */
    @Override
    public List<FtpFile> listExperimentChildrenPaths(Experiment experiment,
            final String parentPath, FtpPathResolverContext context)
    {
        List<AbstractExternalData> dataSets = context.getDataSets(experiment);

        FtpFileEvaluationContext evalContext = evaluateDataSetPaths(context, dataSets);
        try
        {
            return createFtpFilesFromEvaluationResult(context, parentPath, evalContext);
        } finally
        {
            evalContext.close();
        }
    }

    private List<FtpFile> createFtpFilesFromEvaluationResult(FtpPathResolverContext context,
            final String parentPath, FtpFileEvaluationContext evalResult)
    {
        ArrayList<FtpFile> result = new ArrayList<FtpFile>();
        Cache cache = context.getCache();
        for (EvaluatedElement evalElement : evalResult.getEvalElements())
        {
            AbstractExternalData dataSet = evalElement.dataSet;
            IHierarchicalContentNodeFilter fileFilter = getFileFilter(dataSet);
            if (fileFilter.accept(evalElement.contentNode))
            {
                String childPath =
                        parentPath + FtpConstants.FILE_SEPARATOR + evalElement.evaluatedTemplate;
                String dataSetCode = dataSet.getCode();
                IHierarchicalContent content = evalResult.getHierarchicalContent(dataSet, false);
                FtpFile childFtpFile =
                        FtpFileFactory.createFtpFile(dataSetCode, childPath, evalElement.contentNode,
                                content, fileFilter, dataSet.getModificationDate().getTime(), cache);
                result.add(childFtpFile);
            }
        }

        return result;
    }

    private FtpFileEvaluationContext evaluateDataSetPaths(
            ISessionTokenProvider sessionTokenProvider,
            List<AbstractExternalData> dataSets)
    {
        FtpFileEvaluationContext evalContext = createFtpFileEvaluationContext(sessionTokenProvider);

        for (int disambiguationIdx = 0; disambiguationIdx < dataSets.size(); disambiguationIdx++)
        {
            AbstractExternalData dataSet = dataSets.get(disambiguationIdx);
            try
            {
                IHierarchicalContent hierarchicalContent =
                        evalContext.getHierarchicalContent(dataSet, false);
                IHierarchicalContentNode rootNode =
                        getDataSetFileListRoot(dataSet, hierarchicalContent);
                List<EvaluatedElement> paths =
                        evaluateDataSetPaths(dataSet, rootNode, disambiguationIdx);

                evalContext.addEvaluatedElements(paths);
            } catch (Throwable t)
            {
                operationLog.warn("Failed to evaluate data set paths for dataset "
                        + dataSet.getCode() + ": " + t.getMessage());
            }
        }

        return evalContext;
    }

    private List<EvaluatedElement> evaluateDataSetPaths(AbstractExternalData dataSet,
            IHierarchicalContentNode rootNode, int disambiguationIndex)
    {
        List<EvaluatedElement> result = new ArrayList<EvaluatedElement>();
        String disambiguationVar = computeDisambiguation(disambiguationIndex);

        for (IHierarchicalContentNode fileNode : getFileNamesRequiredByTemplate(rootNode))
        {
            EvaluatedElement evalElement = new EvaluatedElement();
            evalElement.dataSet = dataSet;
            evalElement.pathInDataSet = fileNode.getRelativePath();
            evalElement.evaluatedTemplate =
                    evaluateTemplate(dataSet, fileNode.getName(), disambiguationVar);
            evalElement.contentNode = fileNode;
            result.add(evalElement);
        }

        return result;
    }

    private IHierarchicalContentNode getDataSetFileListRoot(AbstractExternalData dataSet,
            IHierarchicalContent hierachicalContent)
    {
        String fileListSubPathOrNull = getFileListSubPath(dataSet);

        if (false == StringUtils.isBlank(fileListSubPathOrNull))
        {
            List<IHierarchicalContentNode> matchingNodes =
                    hierachicalContent.listMatchingNodes(fileListSubPathOrNull);
            if (false == matchingNodes.isEmpty())
            {
                if (matchingNodes.size() == 1)
                {
                    return matchingNodes.get(0);
                } else
                {
                    String message =
                            String.format("Multiple nodes in dataset '%s' match "
                                    + "pattern '%s'. Will use data set root for file listing.",
                                    dataSet.getCode(), fileListSubPathOrNull);
                    operationLog.warn(message);
                }
            } else
            {
                String message =
                        String.format("No nodes in dataset '%s' match "
                                + "pattern '%s'. Will use data set root for file listings.",
                                dataSet.getCode(), fileListSubPathOrNull);
                operationLog.warn(message);
            }
        }
        return hierachicalContent.getRootNode();
    }

    /**
     * Given a zero-based disambiguation index, produces the values for {@value #DISAMBIGUATION_VARNAME}.
     */
    private String computeDisambiguation(int disambiguationIdx)
    {
        int number = 10 + disambiguationIdx;
        return Integer.toString(number, 36).toUpperCase();
    }

    /**
     * @return all values to be used when evaluating the template "${fileName}" variable.
     */
    private List<IHierarchicalContentNode> getFileNamesRequiredByTemplate(
            IHierarchicalContentNode rootNode)
    {
        if (fileNamePresent)
        {
            if (rootNode.isDirectory())
            {
                return rootNode.getChildNodes();
            }
        }
        return Collections.singletonList(rootNode);
    }

    private String evaluateTemplate(AbstractExternalData dataSet, String fileName, String disambiguation)
    {
        Template eval = template.createFreshCopy();
        eval.attemptToBind(DATA_SET_CODE_VARNAME, dataSet.getCode());
        eval.attemptToBind(DATA_SET_TYPE_VARNAME, dataSet.getDataSetType().getCode());
        String dataSetDate = extractDateValue(dataSet.getRegistrationDate());
        eval.attemptToBind(DATA_SET_DATE_VARNAME, dataSetDate);
        if (fileName != null)
        {
            eval.attemptToBind(FILE_NAME_VARNAME, fileName);
        }
        eval.attemptToBind(DISAMBIGUATION_VARNAME, disambiguation);

        return eval.createText();
    }

    /**
     * Formats a date as it will appear after template evaluation.
     */
    @Private
    static String extractDateValue(Date dataSetDate)
    {
        return DateFormatUtils.format(dataSetDate, DATA_SET_DATE_FORMAT);
    }

    private Map<String, DataSetTypeConfig> initializeDataSetTypeConfigs(
            FtpPathResolverConfig resolverConfig)
    {
        Map<String, DataSetTypeConfig> result = new HashMap<String, DataSetTypeConfig>();
        Map<String, String> fileListSubPaths = resolverConfig.getFileListSubPaths();
        Map<String, String> fileListFilters = resolverConfig.getFileListFilters();

        for (Entry<String, String> subPathEntry : fileListSubPaths.entrySet())
        {
            DataSetTypeConfig dsConfig = new DataSetTypeConfig();
            dsConfig.fileListSubPath = subPathEntry.getValue();
            result.put(subPathEntry.getKey(), dsConfig);
        }

        for (Entry<String, String> filterEntry : fileListFilters.entrySet())
        {
            String dataSetType = filterEntry.getKey();
            String fileFilterPattern = filterEntry.getValue();
            DataSetTypeConfig dsConfig = result.get(dataSetType);
            if (dsConfig == null)
            {
                dsConfig = new DataSetTypeConfig();
            }
            dsConfig.fileFilter = createFilter(fileFilterPattern);
            result.put(dataSetType, dsConfig);
        }
        return result;
    }

    private IHierarchicalContentNodeFilter createFilter(final String fileFilterPattern)
    {
        return new IHierarchicalContentNodeFilter()
            {
                private final Pattern compiledPattern = Pattern.compile(fileFilterPattern);

                @Override
                public boolean accept(IHierarchicalContentNode node)
                {
                    if (node.isDirectory())
                    {
                        // no filtering for directories
                        return true;
                    }

                    return compiledPattern.matcher(node.getName()).matches();
                }
            };
    }

    private DataSetTypeConfig getDataSetTypeConfig(AbstractExternalData dataSet)
    {
        String dataSetType = dataSet.getDataSetType().getCode();
        DataSetTypeConfig dsConfig = dataSetTypeConfigs.get(dataSetType);
        if (dsConfig != null)
        {
            return dsConfig;
        } else
        {
            return defaultDSTypeConfig;
        }
    }

    private IHierarchicalContentNodeFilter getFileFilter(AbstractExternalData dataSet)
    {
        return getDataSetTypeConfig(dataSet).fileFilter;
    }

    private String getFileListSubPath(AbstractExternalData dataSet)
    {
        return getDataSetTypeConfig(dataSet).fileListSubPath;
    }

    private FtpFileEvaluationContext createFtpFileEvaluationContext(
            ISessionTokenProvider sessionTokenProvider)
    {
        return new FtpFileEvaluationContext(getContentProvider(sessionTokenProvider));
    }

    private IHierarchicalContentProvider getContentProvider(
            ISessionTokenProvider sessionTokenProvider)
    {
        if (contentProvider == null)
        {
            contentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return contentProvider.cloneFor(sessionTokenProvider);
    }

}
