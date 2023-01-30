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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Length;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;

/**
 * Persistence entity representing controlled vocabulary.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.CONTROLLED_VOCABULARY_TABLE, uniqueConstraints =
{ @UniqueConstraint(columnNames =
{ ColumnNames.CODE_COLUMN, ColumnNames.IS_MANAGED_INTERNALLY }) })
public class VocabularyPE extends HibernateAbstractRegistrationHolder implements IIdAndCodeHolder, IIdentityHolder,
        Comparable<VocabularyPE>, Serializable
{

    public final static VocabularyPE[] EMPTY_ARRAY = new VocabularyPE[0];

    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private String simpleCode;

    private String description;

    private Set<VocabularyTermPE> terms = new LinkedHashSet<VocabularyTermPE>();

    private boolean managedInternally;

    private boolean chosenFromList;

    private String urlTemplate;

    private Date modificationDate;

    public VocabularyPE()
    {
    }

    @Column(name = ColumnNames.CODE_COLUMN)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    private String getSimpleCode()
    {
        return simpleCode;
    }

    /**
     * Sets code in 'database format' - without 'user prefix'. To set full code (with user prefix use {@link #setCode(String)}).
     */
    private void setSimpleCode(final String simpleCode)
    {
        this.simpleCode = simpleCode;
    }

    @Version
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    @Column(name = ColumnNames.IS_CHOSEN_FROM_LIST)
    public boolean isChosenFromList()
    {
        return chosenFromList;
    }

    public void setChosenFromList(boolean chosenFromList)
    {
        this.chosenFromList = chosenFromList;
    }

    @Column(name = ColumnNames.URL_TEMPLATE, updatable = true)
    public String getURLTemplate()
    {
        return urlTemplate;
    }

    public void setURLTemplate(String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String descriptionOrNull)
    {
        this.description = descriptionOrNull;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "vocabularyInternal", orphanRemoval = true)
    private Set<VocabularyTermPE> getVocabularyTerms()
    {
        return terms;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setVocabularyTerms(final Set<VocabularyTermPE> terms)
    {
        this.terms = terms;
    }

    @Transient
    public Set<VocabularyTermPE> getTerms()
    {
        return new UnmodifiableSetDecorator<VocabularyTermPE>(getVocabularyTerms());
    }

    public final void setTerms(final Iterable<VocabularyTermPE> terms)
    {
        getVocabularyTerms().clear();
        for (final VocabularyTermPE child : terms)
        {
            addTerm(child);
        }
    }

    public void addTerm(final VocabularyTermPE child)
    {
        final VocabularyPE parent = child.getVocabulary();
        if (parent != null)
        {
            parent.getVocabularyTerms().remove(child);
        }
        child.setVocabularyInternal(this);
        getVocabularyTerms().add(child);
    }

    void removeTerm(VocabularyTermPE child)
    {
        if (this.equals(child.getVocabulary()) == false)
        {
            throw new IllegalArgumentException("Not a term of this vocabulary: " + child);
        }
        getVocabularyTerms().remove(child);
    }

    @Override
    @SequenceGenerator(name = SequenceNames.CONTROLLED_VOCABULARY_SEQUENCE, sequenceName = SequenceNames.CONTROLLED_VOCABULARY_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.CONTROLLED_VOCABULARY_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Column(name = ColumnNames.IS_MANAGED_INTERNALLY, nullable = false)
    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    public void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    public void setId(final long id)
    {
        this.id = id;

    }

    public void setCode(final String fullCode)
    {
        setManagedInternally(CodeConverter.isInternalNamespace(fullCode));
        setSimpleCode(CodeConverter.tryToDatabase(fullCode));
    }

    @Override
    @Transient
    public String getCode()
    {
        return CodeConverter.tryToBusinessLayer(getSimpleCode(), isManagedInternally());
    }

    @Override
    @Transient
    public String getPermId()
    {
        return getCode();
    }

    @Override
    @Transient
    public String getIdentifier()
    {
        return getCode();
    }

    /**
     * Returns the {@link VocabularyTermPE} of this vocabulary having given code.
     * 
     * @return <code>null</code> if no term with given code could be found.
     */
    public final VocabularyTermPE tryGetVocabularyTerm(final String code)
    {
        assert code != null : "Unspecified code";
        for (final VocabularyTermPE vocabularyTermPE : getTerms())
        {
            if (vocabularyTermPE.getCode().equals(code))
            {
                return vocabularyTermPE;
            }
        }
        return null;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof VocabularyPE == false)
        {
            return false;
        }
        final VocabularyPE that = (VocabularyPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(isManagedInternally(), that.isManagedInternally());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(isManagedInternally());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("simpleCode", getSimpleCode());
        builder.append("managedInternally", isManagedInternally());
        return builder.toString();
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final VocabularyPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

    //
    // connected property types for use only in tests (no bidirectional support for connection)
    //

    /** children of container hierarchy - added only to simplify testing */
    private List<PropertyTypePE> propertyTypes = new ArrayList<PropertyTypePE>();

    @Private
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vocabulary")
    public List<PropertyTypePE> getPropertyTypes()
    {
        return propertyTypes;
    }

    @SuppressWarnings("unused")
    private void setPropertyTypes(List<PropertyTypePE> propertyTypes)
    {
        this.propertyTypes = propertyTypes;
    }

}
