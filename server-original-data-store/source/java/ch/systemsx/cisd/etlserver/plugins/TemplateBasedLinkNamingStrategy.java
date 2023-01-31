/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * A naming strategy based on a configurable string template.
 * 
 * @author Kaloyan Enimanev
 */
public class TemplateBasedLinkNamingStrategy implements IHierarchicalStorageLinkNamingStrategy
{
    public static final String DEFAULT_LINK_TEMPLATE =
            "${space}/${project}/${experiment}/${dataSetType}+${sample}+${dataSet}";

    private static final String LINKS_TEMPLATE_PROP_NAME = "template";

    public static final String COMPONENT_LINKS_TEMPLATE_PROP_NAME = "component-template";

    private static final String NOT_DIRECTLY_CONNECTED = "NOT_DIRECTLY_CONNECTED";

    private final String linkTemplate;

    private final String componentLinkTemplate;

    public TemplateBasedLinkNamingStrategy(String template, String componentTemplate)
    {
        if (template == null)
        {
            this.linkTemplate = DEFAULT_LINK_TEMPLATE;
        } else
        {
            this.linkTemplate = template;
        }

        this.componentLinkTemplate = componentTemplate;
    }

    public TemplateBasedLinkNamingStrategy(Properties configurationProperties)
    {
        this(configurationProperties.getProperty(LINKS_TEMPLATE_PROP_NAME), configurationProperties.getProperty(COMPONENT_LINKS_TEMPLATE_PROP_NAME));
    }

    /**
     * For given {@link SimpleDataSetInformationDTO} creates relevant path part.
     * 
     * @return Instance_AAA/Group_BBB/Project_CCC...</code>
     */
    @Override
    public Set<HierarchicalPath> createHierarchicalPaths(AbstractExternalData data)
    {
        if (data.getContainerDataSets().size() == 0 || componentLinkTemplate == null)
        {
            ExtendedProperties props = new ExtendedProperties();
            for (PathVariable pathElement : PathVariable.values())
            {
                String pathElementName = pathElement.name();
                String pathElementValue = pathElement.extractValueFromData(data);
                if (pathElementValue == null)
                {
                    pathElementValue = StringUtils.EMPTY;
                }
                props.put(pathElementName, pathElementValue);
            }

            return Collections.singleton(new HierarchicalPath(evaluateTemplate(props, linkTemplate), null));
        } else
        {
            Set<HierarchicalPath> results = new HashSet<>();
            for (ContainerDataSet container : data.getContainerDataSets())
            {
                ExtendedProperties props = new ExtendedProperties();
                for (PathVariable pathElement : PathVariable.values())
                {
                    String pathElementName = pathElement.name();
                    String pathElementValue = pathElement.extractValueFromData(data, container);
                    if (pathElementValue == null)
                    {
                        pathElementValue = StringUtils.EMPTY;
                    }
                    props.put(pathElementName, pathElementValue);
                }
                results.add(new HierarchicalPath(evaluateTemplate(props, componentLinkTemplate), container));

            }
            return results;
        }
    }

    /**
     * Creates a {@link Set} of data set paths located inside <code>root</code>.
     */
    @Override
    public Set<String> extractPaths(File root)
    {
        HashSet<String> set = new HashSet<String>();
        accumulatePaths(set, root);
        return set;
    }

    private String evaluateTemplate(ExtendedProperties props, String template)
    {
        props.put("template", template);
        // this will evaluate and replace all variables in the value of the property
        return props.getProperty("template");
    }

    @Private
    static void accumulatePaths(HashSet<String> paths, File dir)
    {
        File[] children = dir.listFiles();
        if (children != null)
        {
            for (File child : children)
            {
                if (child.isDirectory() && (FileUtilities.isSymbolicLink(child) == false))
                {
                    accumulatePaths(paths, child);
                } else
                {
                    String absolutePath = child.getAbsolutePath();
                    paths.add(absolutePath);
                }
            }
        }
    }

    enum PathVariable
    {
        dataSet
        {
            @Override
            String extractValueFromData(AbstractExternalData data, ContainerDataSet container)
            {
                return data.getCode();
            }
        },

        dataSetType
        {
            @Override
            String extractValueFromData(AbstractExternalData data, ContainerDataSet container)
            {
                return data.getDataSetType().getCode();
            }
        },

        sample
        {
            @Override
            String extractValueFromData(AbstractExternalData data, ContainerDataSet container)
            {
                return getPathElement(data.getSampleCode());
            }
        },

        experiment
        {
            @Override
            String extractValueFromData(AbstractExternalData data, ContainerDataSet container)
            {
                Experiment experiment = data.getExperiment();
                return getPathElement(experiment == null ? null : experiment.getCode());
            }
        },

        project
        {
            @Override
            String extractValueFromData(AbstractExternalData data, ContainerDataSet container)
            {
                Experiment experiment = data.getExperiment();
                return getPathElement(experiment == null ? null : experiment.getProject().getCode());
            }
        },

        space
        {
            @Override
            String extractValueFromData(AbstractExternalData data, ContainerDataSet container)
            {
                return data.getSpace().getCode();
            }
        },

        containerDataSet
        {
            @Override
            String extractValueFromData(AbstractExternalData data, ContainerDataSet container)
            {
                if (container != null)
                {
                    return container.getCode();
                } else
                {
                    return null;
                }
            }
        },

        containerDataSetType
        {
            @Override
            String extractValueFromData(AbstractExternalData data, ContainerDataSet container)
            {
                if (container != null)
                {
                    return container.getDataSetType().getCode();
                } else
                {
                    return null;
                }
            }
        },

        containerSample
        {
            @Override
            String extractValueFromData(AbstractExternalData data, ContainerDataSet container)
            {
                if (container != null)
                {
                    return getPathElement(container.getSampleCode());
                } else
                {
                    return null;
                }
            }
        };

        String extractValueFromData(AbstractExternalData data)
        {
            return extractValueFromData(data, null);
        }

        abstract String extractValueFromData(AbstractExternalData data, ContainerDataSet container);

        private static String getPathElement(String pathElement)
        {
            return pathElement == null ? NOT_DIRECTLY_CONNECTED : pathElement;
        }
    }
}