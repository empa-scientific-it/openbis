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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2;

import java.util.List;

/**
 * An interface for samples from the database that should not be altered.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface ISampleImmutable extends IMetaprojectContent
{
    /**
     * Return the identifier for this sample.
     */
    String getSampleIdentifier();

    /**
     * Return the space code for this sample.
     */
    String getSpace();

    /**
     * Return the code for this sample.
     */
    String getCode();

    /**
     * Return the experiment for this sample. May be null.
     */
    IExperimentImmutable getExperiment();
    
    /**
     * Returns the project for this sample or null.
     */
    IProjectImmutable getProject();

    /**
     * Return the type for this sample. May be null.
     */
    String getSampleType();

    /**
     * Return true if the sample exists in the database.
     */
    boolean isExistingSample();

    /**
     * Return the value of a property specified by a code. May return null of no such property with code <code>propertyCode</code> is found.
     */
    String getPropertyValue(String propertyCode);

    /**
     * Return the contained sample objects. Only available for samples existing prior the transaction start that were obtained from the server with
     * their contained samples included. Call {@link #canGetContainedSamples()} to see if these can be provided.
     * 
     * @throws IllegalStateException If the contained samples cannot be returned.
     */
    List<ISampleImmutable> getContainedSamples();

    /**
     * Return true if this sample object has contained samples available. This is independent of whether or not the sample has contained samples. This
     * method only says if the contained samples were provided to this object by the application server.
     */
    public boolean canGetContainedSamples();

    /**
     * Returns the permId of this sample.
     */
    String getPermId();

    /**
     * Gets the parents of this sample
     */
    public List<String> getParentSampleIdentifiers();
}
