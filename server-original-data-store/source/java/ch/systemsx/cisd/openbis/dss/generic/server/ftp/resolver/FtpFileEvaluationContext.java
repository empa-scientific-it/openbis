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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * An object holding templates evaluation result data.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpFileEvaluationContext
{

    static class EvaluatedElement
    {
        AbstractExternalData dataSet;

        // will only be filled when the ${fileName} variable
        // is used in the template
        String pathInDataSet = StringUtils.EMPTY;

        String evaluatedTemplate;

        IHierarchicalContentNode contentNode;
    }

    private Map<String /* dataset code # access */, IHierarchicalContent> contents =
            new HashMap<String, IHierarchicalContent>();

    private IHierarchicalContentProvider contentProvider;

    private List<EvaluatedElement> evaluatedPaths = new ArrayList<EvaluatedElement>();

    FtpFileEvaluationContext(IHierarchicalContentProvider contentProvider)
    {
        this.contentProvider = contentProvider;
    }

    /**
     * @return the evaluation result.
     */
    public List<EvaluatedElement> getEvalElements()
    {
        return Collections.unmodifiableList(evaluatedPaths);

    }

    /**
     * Adds a collection of {@link EvaluatedElement} to the results.
     */
    public void addEvaluatedElements(Collection<EvaluatedElement> evaluatedPath)
    {
        evaluatedPaths.addAll(evaluatedPath);
    }

    public IHierarchicalContent getHierarchicalContent(AbstractExternalData dataSet, boolean withModifyingAccessTimestamp)
    {
        String dataSetCode = dataSet.getCode() + "#" + withModifyingAccessTimestamp;
        IHierarchicalContent result = contents.get(dataSetCode);
        if (result == null)
        {
            result = createHierarchicalContent(dataSet, withModifyingAccessTimestamp);
            contents.put(dataSetCode, result);
        }
        return result;
    }

    /**
     * closes the evaluation context and frees all associated resources.
     */
    public void close()
    {
        for (IHierarchicalContent content : contents.values())
        {
            content.close();
        }
        contents.clear();
    }

    private IHierarchicalContent createHierarchicalContent(AbstractExternalData dataSet, boolean withModifyingAccessTimestamp)
    {
        if (withModifyingAccessTimestamp)
        {
            return contentProvider.asContent(dataSet);
        } else
        {
            return contentProvider.asContentWithoutModifyingAccessTimestamp(dataSet);
        }
    }

}
