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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import java.io.Serializable;

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Identifies a group of samples on the same level (in certain group or on database level)
 * 
 * @author Tomasz Pylak
 */
public class SampleIdentifierPattern extends AbstractHashable implements Serializable
{
    public final static SampleIdentifierPattern[] EMPTY_ARRAY = new SampleIdentifierPattern[0];

    private static final long serialVersionUID = IServer.VERSION;

    private final SampleOwnerIdentifier owner;

    private final String sampleCodePattern;

    /**
     * Matches samples from the specified group and the database instance to which this group belongs. Only samples with the code matching
     * sampleCodePattern will be selected.
     */
    public static SampleIdentifierPattern[] createGroupVisible(SpaceIdentifier groupIdentifier,
            String sampleCodePattern)
    {
        SampleOwnerIdentifier groupOwner = new SampleOwnerIdentifier(groupIdentifier);
        SampleIdentifierPattern groupPattern =
                new SampleIdentifierPattern(sampleCodePattern, groupOwner);

        SampleOwnerIdentifier dbOwner =
                new SampleOwnerIdentifier(groupIdentifier);
        SampleIdentifierPattern dbPattern = new SampleIdentifierPattern(sampleCodePattern, dbOwner);

        return new SampleIdentifierPattern[]
        { groupPattern, dbPattern };
    }

    /**
     * Matches samples owned by the specified owner. Only samples with the code matching sampleCodePattern will be selected.
     */
    public static SampleIdentifierPattern createOwnedBy(SampleOwnerIdentifier owner,
            String sampleCodePattern)
    {
        return new SampleIdentifierPattern(sampleCodePattern, owner);
    }

    private SampleIdentifierPattern(String sampleCodePattern, SampleOwnerIdentifier owner)
    {
        this.owner = owner;
        this.sampleCodePattern = sampleCodePattern;
    }

    public SampleOwnerIdentifier getSampleOwner()
    {
        return owner;
    }

    public String getSampleCodePattern()
    {
        return sampleCodePattern;
    }

    @Override
    public String toString()
    {
        return owner.toString() + sampleCodePattern;
    }
}
