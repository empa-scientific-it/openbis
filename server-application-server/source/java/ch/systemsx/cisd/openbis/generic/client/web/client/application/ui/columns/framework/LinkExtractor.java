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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.SearchlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * Defines the ways simple view mode links are created for objects of selected types.
 * 
 * @author Izabela Adamczyk
 */
public class LinkExtractor
{

    // browser links

    public static final String createSampleBrowserLink(String spaceOrNull, String sampleTypeOrNull)
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER, PermlinkUtilities.BROWSE_ACTION);
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY, EntityKind.SAMPLE.name());
        if (spaceOrNull != null)
        {
            url.addParameter(PermlinkUtilities.SPACE_PARAMETER_KEY, spaceOrNull);
        }
        if (sampleTypeOrNull != null)
        {
            url.addParameter(PermlinkUtilities.TYPE_PARAMETER_KEY, sampleTypeOrNull);
        }
        return tryPrint(url);
    }

    public static final String createMaterialBrowserLink(String materialTypeOrNull)
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER, PermlinkUtilities.BROWSE_ACTION);
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY, EntityKind.MATERIAL.name());
        if (materialTypeOrNull != null)
        {
            url.addParameter(PermlinkUtilities.TYPE_PARAMETER_KEY, materialTypeOrNull);
        }
        return tryPrint(url);
    }

    public static final String createExperimentBrowserLink(String spaceOrNull,
            String projectOrNull, String experimentTypeOrNull)
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER, PermlinkUtilities.BROWSE_ACTION);
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY, EntityKind.EXPERIMENT.name());
        if (spaceOrNull != null)
        {
            url.addParameter(PermlinkUtilities.SPACE_PARAMETER_KEY, spaceOrNull);
        }
        if (projectOrNull != null)
        {
            url.addParameter("project", projectOrNull);
        }
        if (experimentTypeOrNull != null)
        {
            url.addParameter(PermlinkUtilities.TYPE_PARAMETER_KEY, experimentTypeOrNull);
        }
        return tryPrint(url);
    }

    public static final String createMetaprojectLink(String metaprojectName)
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER, PermlinkUtilities.PERMLINK_ACTION);
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY, PermlinkUtilities.METAPROJECT);
        url.addParameter(PermlinkUtilities.NAME_PARAMETER_KEY, metaprojectName);
        return tryPrint(url);
    }

    public static final String createMetaprojectBrowserLink()
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER,
                PermlinkUtilities.BROWSE_ACTION);
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY,
                PermlinkUtilities.METAPROJECT);
        return tryPrint(url);
    }

    public static final String createSearchLink(EntityKind entityKind)
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER, SearchlinkUtilities.SEARCH_ACTION);
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY, entityKind.name());
        return tryPrint(url);
    }

    // detail view links

    public static String tryExtract(IEntityInformationHolderWithPermId entityOrNull)
    {
        if (entityOrNull == null)
        {
            return null;
        }
        if (entityOrNull.getEntityKind() == EntityKind.MATERIAL)
        {
            return tryExtractMaterial(entityOrNull);
        } else
        {
            return createPermlink(entityOrNull.getEntityKind(), entityOrNull.getPermId());
        }
    }

    /** creates permid for entities other than material */
    public static String createPermlink(EntityKind entityKind, String permId)
    {
        assert entityKind != EntityKind.MATERIAL;
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY, entityKind.name());
        url.addParameterWithoutEncoding(PermlinkUtilities.PERM_ID_PARAMETER_KEY, permId);
        return tryPrint(url);
    }

    public static final String tryExtract(Project p)
    {
        if (p == null)
        {
            return null;
        }
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY,
                PermlinkUtilities.PROJECT);
        url.addParameter(PermlinkUtilities.CODE_PARAMETER_KEY, p.getCode());
        url.addParameter(PermlinkUtilities.SPACE_PARAMETER_KEY, p.getSpace().getCode());
        return tryPrint(url);
    }

    public static final String tryExtractMaterial(IEntityInformationHolder material)
    {
        if (material == null)
        {
            return null;
        }
        return tryPrint(tryCreateMaterialLink(material.getCode(), material.getEntityType()
                .getCode()));
    }

    public static final String tryExtract(MaterialIdentifier identifier)
    {
        if (identifier == null)
        {
            return null;
        }
        return tryPrint(tryCreateMaterialLink(identifier.getCode(), identifier.getTypeCode()));
    }

    protected static final URLMethodWithParameters tryCreateMaterialLink(String materialCode,
            String materialTypeCode)
    {
        if (materialCode == null || materialTypeCode == null)
        {
            return null;
        }
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY, EntityKind.MATERIAL.name());
        url.addParameter(PermlinkUtilities.CODE_PARAMETER_KEY, materialCode);
        url.addParameter(PermlinkUtilities.TYPE_PARAMETER_KEY, materialTypeCode);
        return url;
    }

    protected static String tryPrint(URLMethodWithParameters urlOrNull)
    {
        return urlOrNull == null ? null : urlOrNull.toString().substring(1);
    }
}
