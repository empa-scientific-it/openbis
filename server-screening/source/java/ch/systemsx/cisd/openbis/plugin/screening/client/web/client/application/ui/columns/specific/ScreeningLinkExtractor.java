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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLListEncoder;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.dto.ExperimentIdentifierSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCodesCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;

/**
 * Defines the ways simple view mode links for screening specific views are created.
 * 
 * @author Piotr Buczek
 */
public class ScreeningLinkExtractor extends LinkExtractor
{
    public final static String PLATE_METADATA_BROWSER_ACTION = "PLATE_METADATA_REPORT";

    /** If used then {@link #MATERIAL_DETAIL_SEARCH_ALL_EXPERIMENTS_PARAMETER_KEY} is ignored. */
    public final static String MATERIAL_DETAIL_EXPERIMENT_IDENT_PARAMETER_KEY = "experiment";

    /** Cannot be used together with {@link #WELL_SEARCH_EXPERIMENT_PERM_ID_PARAMETER_KEY}. */
    public final static String MATERIAL_DETAIL_SEARCH_ALL_EXPERIMENTS_PARAMETER_KEY =
            "searchAllExperiments";

    public final static String MATERIAL_DETAIL_SEARCH_ALL_EXPERIMENTS_PARAMETER_VALUE = "true";

    public final static String GLOBAL_WELL_SEARCH_ACTION = "GLOBAL_WELL_SEARCH";

    public final static String WELL_SEARCH_ACTION = "WELL_SEARCH";

    /** If not given then all experiments or experiments in the specified project are searched. */
    public final static String WELL_SEARCH_EXPERIMENT_PERM_ID_PARAMETER_KEY = "experimentPermId";

    public final static String WELL_SEARCH_IS_EXACT_PARAMETER_KEY = "isExactSearch";

    public final static String WELL_SEARCH_MATERIAL_TYPES_PARAMETER_KEY = "types";

    public final static String WELL_SEARCH_MATERIAL_ITEMS_PARAMETER_KEY = "items";

    /** meaning: should we show disambiguation page if more than one material matchs the query? */
    public final static String WELL_SEARCH_SHOW_COMBINED_RESULTS_PARAMETER_KEY =
            "showCombinedResults";

    public final static boolean WELL_SEARCH_SHOW_COMBINED_RESULTS_DEFAULT = true;

    public final static String WELL_SEARCH_NOTHING_FOUND_REDIRECTION_URL_KEY =
            "noResultsRedirectionUrl";

    public final static String EXPERIMENT_ANALYSIS_SUMMARY_ACTION = "FEATURE_VECTOR_SUMMARY";

    public final static String EXPERIMENT_ANALYSIS_SUMMARY_EXPERIMENT_PERMID_PARAMETER_KEY =
            "experimentPermId";

    public final static String MATERIAL_REPLICA_SUMMARY_EXPERIMENT_PERM_ID_KEY = "experimentPermId";

    public final static String MATERIAL_CODE_KEY = "materialCode";

    public final static String MATERIAL_TYPE_CODE_KEY = "materialTypeCode";

    public final static String PROJECT_CODE_KEY = "projectCode";

    public final static String SPACE_CODE_KEY = "spaceCode";

    // false by default
    public static final String RESTRICT_GLOBAL_SEARCH_TO_PROJECT = "restrictGlobalSearchToProject";

    public static final String ANALYSIS_PROCEDURE_KEY = "analysisProcedure";

    public static final String COMPUTE_RANKS_KEY = "computeRanks";

    public static final String createPlateMetadataBrowserLink(String platePermId)
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER, PLATE_METADATA_BROWSER_ACTION);
        url.addParameter(PermlinkUtilities.PERM_ID_PARAMETER_KEY, platePermId);
        return tryPrint(url);
    }

    public static final String createExperimentAnalysisSummaryBrowserLink(String experimentPermId,
            boolean restrictGlobalScopeLinkToProject, String analysisProcedureOrNull)
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER, EXPERIMENT_ANALYSIS_SUMMARY_ACTION);
        url.addParameter(EXPERIMENT_ANALYSIS_SUMMARY_EXPERIMENT_PERMID_PARAMETER_KEY,
                experimentPermId);
        if (restrictGlobalScopeLinkToProject)
        {
            url.addParameterWithoutEncoding(RESTRICT_GLOBAL_SEARCH_TO_PROJECT,
                    restrictGlobalScopeLinkToProject);
        }
        if (StringUtils.isNotBlank(analysisProcedureOrNull))
        {
            url.addParameter(ANALYSIS_PROCEDURE_KEY, analysisProcedureOrNull);
        }
        return tryPrint(url);
    }

    // action=WELL_SEARCH&experimentPermId=8127361723172863&isExactSearch=true&types=typeCode1,typeCode2&items=code1,property2&showCombinedResults=false"
    // One can change "experimentPermId=8127361723172863" to "searchAllExperiments=true".
    public static String tryCreateWellsSearchLink(WellSearchCriteria searchCriteria,
            boolean showCombinedResults)
    {
        MaterialSearchCodesCriteria materialCodesOrProperties =
                searchCriteria.getMaterialSearchCriteria().tryGetMaterialCodesOrProperties();
        if (materialCodesOrProperties == null)
        {
            return null;
        }
        return createWellsSearchLink(searchCriteria.getExperimentCriteria(),
                materialCodesOrProperties, showCombinedResults);
    }

    public static String createWellsSearchLink(ExperimentSearchCriteria experimentCriteria,
            MaterialSearchCodesCriteria materialCodesCriteria, Boolean showCombinedResults)
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER, WELL_SEARCH_ACTION);
        SingleExperimentSearchCriteria experiment = experimentCriteria.tryGetExperiment();
        BasicProjectIdentifier project = experimentCriteria.tryGetProjectIdentifier();
        if (experiment != null)
        {
            url.addParameter(WELL_SEARCH_EXPERIMENT_PERM_ID_PARAMETER_KEY,
                    experiment.getExperimentPermId());
        }
        if (project != null)
        {
            url.addParameterWithoutEncoding(SPACE_CODE_KEY, project.getSpaceCode());
            url.addParameterWithoutEncoding(PROJECT_CODE_KEY, project.getProjectCode());
        }
        url.addParameter(WELL_SEARCH_IS_EXACT_PARAMETER_KEY,
                materialCodesCriteria.isExactMatchOnly());
        url.addParameterWithoutEncoding(WELL_SEARCH_MATERIAL_TYPES_PARAMETER_KEY,
                URLListEncoder.encodeItemList(materialCodesCriteria.getMaterialTypeCodes()));
        url.addParameterWithoutEncoding(WELL_SEARCH_MATERIAL_ITEMS_PARAMETER_KEY,
                URLListEncoder.encodeItemList(materialCodesCriteria.getMaterialCodesOrProperties()));
        url.addParameterWithoutEncoding(WELL_SEARCH_SHOW_COMBINED_RESULTS_PARAMETER_KEY,
                showCombinedResults);

        return tryPrint(url);
    }

    private static ExperimentIdentifierSearchCriteria convertToIdentifierExperimentCriteria(
            ExperimentSearchCriteria experimentSearchCriteria)
    {
        SingleExperimentSearchCriteria experiment = experimentSearchCriteria.tryGetExperiment();
        BasicProjectIdentifier project = experimentSearchCriteria.tryGetProjectIdentifier();
        if (experiment != null)
        {
            return ExperimentIdentifierSearchCriteria.createExperimentScope(
                    experiment.getExperimentIdentifier(),
                    experimentSearchCriteria.getRestrictGlobalSearchLinkToProject());
        } else if (project != null)
        {
            return ExperimentIdentifierSearchCriteria.createProjectScope(project);
        } else
        {
            return ExperimentIdentifierSearchCriteria.createSearchAll();
        }
    }

    /**
     * Creates a link for material detail view.
     * 
     * @param experimentCriteriaOrNull if null, no search for data about locations of the material takes place. Otherwise the search is performed in
     *            all or a single experiment.
     */
    public static final String createMaterialDetailsLink(IEntityInformationHolder material,
            ExperimentIdentifierSearchCriteria experimentCriteriaOrNull)
    {
        URLMethodWithParameters url =
                createMaterialDetailsLink(material.getCode(), material.getEntityType().getCode(),
                        experimentCriteriaOrNull);
        return tryPrint(url);
    }

    private static final URLMethodWithParameters createMaterialDetailsLink(String materialCode,
            String materialTypeCode, ExperimentIdentifierSearchCriteria experimentCriteriaOrNull)
    {
        assert materialCode != null;
        assert materialTypeCode != null;

        URLMethodWithParameters url = tryCreateMaterialLink(materialCode, materialTypeCode);
        assert url != null : "url is null";

        if (experimentCriteriaOrNull != null)
        {
            if (experimentCriteriaOrNull.searchAllExperiments())
            {
                url.addParameter(MATERIAL_DETAIL_SEARCH_ALL_EXPERIMENTS_PARAMETER_KEY,
                        MATERIAL_DETAIL_SEARCH_ALL_EXPERIMENTS_PARAMETER_VALUE);
            } else
            {
                BasicProjectIdentifier project = experimentCriteriaOrNull.tryGetProject();
                if (project != null)
                {
                    url.addParameterWithoutEncoding(SPACE_CODE_KEY, project.getSpaceCode());
                    url.addParameterWithoutEncoding(PROJECT_CODE_KEY, project.getProjectCode());
                } else
                {
                    // We know that experiment identifier cannot contain characters that should be
                    // encoded
                    // apart from '/'. Encoding '/' makes the URL less readable and on the other
                    // hand
                    // leaving it as it is doesn't cause us any problems.
                    url.addParameterWithoutEncoding(MATERIAL_DETAIL_EXPERIMENT_IDENT_PARAMETER_KEY,
                            StringEscapeUtils.unescapeHtml(experimentCriteriaOrNull
                                    .tryGetExperimentIdentifier()));
                    if (experimentCriteriaOrNull.getRestrictGlobalSearchLinkToProject())
                    {
                        url.addParameterWithoutEncoding(RESTRICT_GLOBAL_SEARCH_TO_PROJECT, "true");
                    }
                }
            }
        }
        return url;
    }

    public static String createMaterialDetailsLink(IEntityInformationHolder material,
            ExperimentSearchCriteria experimentSearchCriteria)
    {
        ExperimentIdentifierSearchCriteria experimentCriteria =
                convertToIdentifierExperimentCriteria(experimentSearchCriteria);
        return createMaterialDetailsLink(material, experimentCriteria);
    }

}
