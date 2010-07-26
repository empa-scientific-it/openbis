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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodeCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ExperimentIdentifierPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ScreeningPlateValidator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ScreenerReadonlyPlatePredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ScreeningExperimentValidator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;

/**
 * This interface is a part of the official public screening API. It is forbidden to change it in a
 * non-backward-compatible manner without discussing it with all screening customers.
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningApiServer extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "screening";

    /**
     * The major version of this service.
     */
    public static final int MAJOR_VERSION = 1;

    /**
     * The minor version of this service.
     */
    public static final int MINOR_VERSION = 1;

    /**
     * Service part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-api-v1";

    /**
     * Authenticates the user with a given password.
     * 
     *@return sessionToken if authentication succeeded, <code>null</code> otherwise.
     */
    @Transactional
    // this is not a readOnly transaction - it can create new users
    String tryLoginScreening(String userId, String userPassword) throws IllegalArgumentException;

    /**
     * Logout the session with the specified session token.
     */
    @Transactional(readOnly = true)
    void logoutScreening(final String sessionToken) throws IllegalArgumentException;

    /**
     * Return the list of all visible plates assigned to any experiment, along with their
     * hierarchical context (space, project, experiment).
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ScreeningPlateValidator.class)
    List<Plate> listPlates(String sessionToken) throws IllegalArgumentException;

    /**
     * Return the list of all visible experiments, along with their hierarchical context (space,
     * project).
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ScreeningExperimentValidator.class)
    List<ExperimentIdentifier> listExperiments(String sessionToken);

    /**
     * For a given set of plates (given by space / plate bar code), provide the list of all data
     * sets containing feature vectors for each of these plates.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            String sessionToken,
            @AuthorizationGuard(guardClass = ScreenerReadonlyPlatePredicate.class) List<? extends PlateIdentifier> plates)
            throws IllegalArgumentException;

    /**
     * For a given set of plates provide the list of all data sets containing images for each of
     * these plates.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    List<ImageDatasetReference> listImageDatasets(
            String sessionToken,
            @AuthorizationGuard(guardClass = ScreenerReadonlyPlatePredicate.class) List<? extends PlateIdentifier> plates)
            throws IllegalArgumentException;

    /**
     * Converts a given list of dataset codes to dataset identifiers.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    List<IDatasetIdentifier> getDatasetIdentifiers(
            String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes);

    /**
     * For the given <var>experimentIdentifier</var> find all plate locations that are connected to
     * the specified <var>materialIdentifier</var>. If <code>findDatasets == true</code>, find also
     * the connected image and image analysis data sets for the relevant plates.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    List<PlateWellReferenceWithDatasets> listPlateWells(
            String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentIdentifierPredicate.class) ExperimentIdentifier experimentIdentifer,
            MaterialIdentifier materialIdentifier, boolean findDatasets);

}
