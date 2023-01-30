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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Persistence entity representing vocabulary term.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.CONTROLLED_VOCABULARY_TERM_TABLE, uniqueConstraints = {
        @UniqueConstraint(columnNames = { ColumnNames.CODE_COLUMN, ColumnNames.CONTROLLED_VOCABULARY_COLUMN }) })
public class VocabularyTermPE extends HibernateAbstractRegistrationHolder implements
        IIdAndCodeHolder, Comparable<VocabularyTermPE>, Serializable
{

    private static final long serialVersionUID = IServer.VERSION;

    public static final VocabularyTermPE[] EMPTY_ARRAY = new VocabularyTermPE[0];

    private transient Long id;

    private String code;

    private String label;

    private String description;

    private Long ordinal;

    private Boolean isOfficial;

    private VocabularyPE vocabulary;

    private Date modificationDate;

    public VocabularyTermPE()
    {
    }

    @Override
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 50, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.TERM_CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.TERM_CODE_PATTERN_MESSAGE)
    public String getCode()
    {
        return code;
    }

    public void setCode(final String code)
    {
        this.code = CodeConverter.tryToDatabase(code);
    }

    @Version
    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
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

    @Column(name = ColumnNames.LABEL_COLUMN)
    @Length(max = GenericConstants.COLUMN_LABEL, message = ValidationMessages.LABEL_LENGTH_MESSAGE)
    public String getLabel()
    {
        return label;
    }

    public void setLabel(final String label)
    {
        this.label = label;
    }

    @Column(name = ColumnNames.ORDINAL_COLUMN)
    @NotNull(message = ValidationMessages.ORDINAL_NOT_NULL_MESSAGE)
    public Long getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(Long ordinal)
    {
        this.ordinal = ordinal;
    }

    @Column(name = ColumnNames.IS_OFFICIAL)
    public Boolean isOfficial()
    {
        return isOfficial;
    }

    public void setOfficial(Boolean isOfficial)
    {
        this.isOfficial = isOfficial;
    }

    public void setId(final long id)
    {
        this.id = id;
    }

    @Override
    @SequenceGenerator(name = SequenceNames.CONTROLLED_VOCABULARY_TERM_SEQUENCE, sequenceName = SequenceNames.CONTROLLED_VOCABULARY_TERM_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.CONTROLLED_VOCABULARY_TERM_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    @NotNull(message = ValidationMessages.VOCABULARY_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.CONTROLLED_VOCABULARY_COLUMN, updatable = false)
    private VocabularyPE getVocabularyInternal()
    {
        return vocabulary;
    }

    void setVocabularyInternal(final VocabularyPE vocabulary)
    {
        this.vocabulary = vocabulary;
    }

    public void setVocabulary(final VocabularyPE vocabulary)
    {
        if (vocabulary == null)
        {
            if (getVocabulary() != null)
            {
                getVocabulary().removeTerm(this);
            }
        } else
        {
            vocabulary.addTerm(this);
        }
    }

    @Transient
    public VocabularyPE getVocabulary()
    {
        return getVocabularyInternal();
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getCode(), "code");
        EqualsHashUtils.assertDefined(getVocabulary(), "vocabulary");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof VocabularyTermPE == false)
        {
            return false;
        }
        final VocabularyTermPE that = (VocabularyTermPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getVocabulary(), that.getVocabulary());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getVocabulary());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        builder.append("label", getLabel());
        return builder.toString();
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final VocabularyTermPE o)
    {
        return this.getOrdinal().compareTo(o.getOrdinal());
    }

    @Transient
    public String getUrl()
    {
        String template = getVocabulary().getURLTemplate();
        if (null == template)
        {
            return null;
        }
        String url =
                template.replaceAll(BasicConstant.DEPRECATED_VOCABULARY_URL_TEMPLATE_TERM_PATTERN,
                        getCode());
        url = url.replaceAll(BasicConstant.VOCABULARY_URL_TEMPLATE_TERM_PATTERN, getCode());
        return url;
    }

}
