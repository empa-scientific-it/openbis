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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import org.apache.commons.lang.ArrayUtils;

/**
 * An entity that is searchable by <i>Hibernate Search</i>.
 * 
 * @author Christian Ribeaud
 */
public enum SearchableEntity
{
    SAMPLE("Sample")
    {

        //
        // SearchableEntity
        //

        @Override
        public final <T extends IMatchingEntity> Class<T> getMatchingEntityClass()
        {
            return cast(SamplePE.class);
        }

        @Override
        public final String[] getFields()
        {
            return (String[]) ArrayUtils.addAll(getStandardFields(), getPropertyFields(name()
                    .toLowerCase()));
        }
    },
    EXPERIMENT("Experiment")
    {
        //
        // SearchableEntity
        //

        @Override
        public final <T extends IMatchingEntity> Class<T> getMatchingEntityClass()
        {
            return cast(ExperimentPE.class);
        }

        @Override
        public final String[] getFields()
        {
            return (String[]) ArrayUtils.addAll(getStandardFields(), getPropertyFields(name()
                    .toLowerCase()));
        }
    },
    MATERIAL("Material")
    {
        //
        // SearchableEntity
        //

        @Override
        public final <T extends IMatchingEntity> Class<T> getMatchingEntityClass()
        {
            return cast(MaterialPE.class);
        }

        @Override
        public final String[] getFields()
        {
            return (String[]) ArrayUtils.addAll(getStandardFields(), getPropertyFields(name()
                    .toLowerCase()));
        }
    };

    private final String description;

    SearchableEntity(final String description)
    {
        this.description = description;
    }

    static final String[] getStandardFields()
    {
        return new String[]
            { "code", "registrator.firstName", "registrator.lastName" };
    }

    static final String[] getPropertyFields(final String entityName)
    {
        return new String[]

            { String.format("%sProperties.value", entityName),
                    String.format("%sProperties.vocabularyTerm.code", entityName) };
    }

    @SuppressWarnings("unchecked")
    final static <T> Class<T> cast(final Class<?> clazz)
    {
        return (Class<T>) clazz;
    }

    /**
     * Returns a description for this searchable entity.
     */
    public final String getDescription()
    {
        return description;
    }

    /**
     * For <i>bean</i> conversion.
     */
    public final String getName()
    {
        return name();
    }

    /**
     * Returns the searchable fields for this entity.
     */
    public abstract String[] getFields();

    /**
     * Returns the <code>class</code> of this searchable entity.
     */
    public abstract <T extends IMatchingEntity> Class<T> getMatchingEntityClass();

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getDescription();
    }
}
