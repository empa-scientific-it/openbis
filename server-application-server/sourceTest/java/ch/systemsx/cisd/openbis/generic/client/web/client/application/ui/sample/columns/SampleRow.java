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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.RowWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;

/**
 * @author Franz-Josef Elmer
 */
public class SampleRow extends RowWithProperties
{

    private final String code;

    private String groupIdentifier;

    public SampleRow(final String code)
    {
        super();
        this.code = code;
        withCell(SampleGridColumnIDs.CODE, code);
    }

    public SampleRow(final String code, final String typeCode)
    {
        this(code);
        withCell(SampleGridColumnIDs.SAMPLE_TYPE, typeCode);
    }

    public SampleRow identifier(final String instanceCode)
    {
        return identifier(instanceCode, null);
    }

    public SampleRow identifier(final String instanceCode, final String groupCodeOrNull)
    {
        if (groupCodeOrNull == null)
        {
            withCell(SampleGridColumnIDs.SPACE, "");
        } else
        {
            withCell(SampleGridColumnIDs.SPACE, groupCodeOrNull);
        }
        groupIdentifier = createGroupIdentifier(instanceCode, groupCodeOrNull);
        withCell(SampleGridColumnIDs.SAMPLE_IDENTIFIER, groupIdentifier + code);
        return this;
    }

    public SampleRow experiment(final String groupCode, final String projectCode,
            final String experimentCode)
    {
        withCell(SampleGridColumnIDs.PROJECT, projectCode);
        withCell(SampleGridColumnIDs.EXPERIMENT, createLinkString(experimentCode));
        final String experimentIdentifier =
                "/" + groupCode + "/" + projectCode + "/" + experimentCode;
        withCell(SampleGridColumnIDs.EXPERIMENT_IDENTIFIER, experimentIdentifier);
        return this;
    }

    private static final String LINK_PREFIX = "<div><a href=\"#\">";

    private static final String LINK_SUFFIX = "</a></div>";

    private String createLinkString(String string)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(LINK_PREFIX);
        sb.append(string);
        sb.append(LINK_SUFFIX);
        return sb.toString();
    }

    public SampleRow noExperiment()
    {
        withCell(SampleGridColumnIDs.PROJECT, null);
        withCell(SampleGridColumnIDs.EXPERIMENT, null);
        withCell(SampleGridColumnIDs.EXPERIMENT_IDENTIFIER, null);
        return this;
    }

    private String createGroupIdentifier(final String instanceCode, final String groupCodeOrNull)
    {
        String identifier = instanceCode + ":/";
        if (groupCodeOrNull != null)
        {
            identifier += groupCodeOrNull + "/";
        }
        return identifier;
    }

    public SampleRow deleted()
    {
        withDeletion(true);
        return this;
    }

    public SampleRow valid()
    {
        withDeletion(false);
        return this;
    }

    private void withDeletion(boolean isDeleted)
    {
        withCell(SampleGridColumnIDs.IS_DELETED, SimpleYesNoRenderer.render(isDeleted));
    }

    public SampleRow derivedFromAncestors(final String... ancestorCodes)
    {
        for (String ancestorCode : ancestorCodes)
        {
            derivedFromAncestor(ancestorCode);
        }
        return this;
    }

    private SampleRow derivedFromAncestor(final String ancestorCode)
    {
        withCell(SampleGridColumnIDs.PARENTS, ancestorCode);
        return this;
    }

    public SampleRow partOfContainer(final String containerCode)
    {
        withCell(SampleGridColumnIDs.CONTAINER_SAMPLE, containerCode);
        return this;
    }

}
