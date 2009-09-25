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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.common.annotation.CollectionMapping;

/**
 * A {@link EntityType} extension for <i>Sample Type</i>.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleType extends EntityType implements IsSerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String DEFINED_IN_FILE = "(multiple)";

    private Long id;

    private int generatedFromHierarchyDepth;

    private boolean showContainer;

    private boolean listable;

    private List<SampleTypePropertyType> sampleTypePropertyTypes =
            new ArrayList<SampleTypePropertyType>(0);

    public final boolean isDefinedInFileSampleTypeCode()
    {
        return isDefinedInFileSampleTypeCode(getCode());
    }

    public static final boolean isDefinedInFileSampleTypeCode(String entityTypeCode)
    {
        return DEFINED_IN_FILE.equals(entityTypeCode);
    }

    public final int getGeneratedFromHierarchyDepth()
    {
        return generatedFromHierarchyDepth;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public final void setGeneratedFromHierarchyDepth(final int generatedFromHierarchyDepth)
    {
        this.generatedFromHierarchyDepth = generatedFromHierarchyDepth;
    }

    public final int getContainerHierarchyDepth()
    {
        return isShowContainer() ? 1 : 0;
    }

    public final void setContainerHierarchyDepth(final int partOfHierarchyDepth)
    {
        setShowContainer(partOfHierarchyDepth > 0);
    }

    public final void setShowContainer(final boolean showContainer)
    {
        this.showContainer = showContainer;
    }

    public final boolean isShowContainer()
    {
        return showContainer;
    }

    @Override
    public final List<SampleTypePropertyType> getAssignedPropertyTypes()
    {
        return sampleTypePropertyTypes;
    }

    @CollectionMapping(collectionClass = ArrayList.class, elementClass = SampleTypePropertyType.class)
    public final void setSampleTypePropertyTypes(
            final List<SampleTypePropertyType> sampleTypePropertyTypes)
    {
        this.sampleTypePropertyTypes = sampleTypePropertyTypes;
    }

    public final boolean isListable()
    {
        return listable;
    }

    public final void setListable(final boolean listable)
    {
        this.listable = listable;
    }

    //
    // Object
    // 

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj) == false)
        {
            return false;
        } else
        {
            if (obj instanceof SampleType == false)
            {
                return false;
            }
            final SampleType that = (SampleType) obj;
            return getGeneratedFromHierarchyDepth() == that.getGeneratedFromHierarchyDepth()
                    && getContainerHierarchyDepth() == that.getContainerHierarchyDepth();
        }
    }

    //
    // static methods to create 'all' SampleType
    //

    /**
     * Creates an artificial Sample Type that stores information merged from given sample types. It
     * will be listable, have {@link EntityType#ALL_TYPES_CODE} code and hierarchy depth equal to
     * max of hierarchy depths from specified types.
     * 
     * @param withDetails if 'true' property types will also be merged, and database instance will
     *            be set
     */
    public static final SampleType createAllSampleType(Collection<SampleType> sampleTypes,
            boolean withDetails)
    {
        assert sampleTypes != null;

        final SampleType allSampleType = new SampleType();
        allSampleType.setCode(EntityType.ALL_TYPES_CODE);
        allSampleType.setListable(true);
        setupMaxHierarchyDepth(allSampleType, sampleTypes);
        if (withDetails)
        {
            mergeDetails(allSampleType, sampleTypes);
        }
        return allSampleType;
    }

    private static void setupMaxHierarchyDepth(SampleType allSampleType,
            Collection<SampleType> sampleTypes)
    {
        int maxContainerHierarchyDepth = 0;
        int maxGeneratedFromHierarchyDepth = 0;
        for (SampleType sampleType : sampleTypes)
        {
            maxContainerHierarchyDepth =
                    max(maxContainerHierarchyDepth, sampleType.getContainerHierarchyDepth());
            maxGeneratedFromHierarchyDepth =
                    max(maxGeneratedFromHierarchyDepth, sampleType.getGeneratedFromHierarchyDepth());
        }
        allSampleType.setContainerHierarchyDepth(maxContainerHierarchyDepth);
        allSampleType.setGeneratedFromHierarchyDepth(maxGeneratedFromHierarchyDepth);
    }

    private static int max(int a, int b)
    {
        return (a >= b) ? a : b;
    }

    private static void mergeDetails(SampleType allSampleType, Collection<SampleType> sampleTypes)
    {
        Set<SampleTypePropertyType> allPropertyTypes = new HashSet<SampleTypePropertyType>();
        for (SampleType sampleType : sampleTypes)
        {
            allPropertyTypes.addAll(sampleType.getAssignedPropertyTypes());
            DatabaseInstance instance = sampleType.getDatabaseInstance();
            if (allSampleType.getDatabaseInstance() != null)
            {
                assert allSampleType.getDatabaseInstance().equals(instance) : "sample types from more than one database instance are not supported";
            } else
            {
                allSampleType.setDatabaseInstance(instance);
            }
        }
        allSampleType.setSampleTypePropertyTypes(new ArrayList<SampleTypePropertyType>(
                allPropertyTypes));
    }

}
