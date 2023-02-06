package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;

/**
 * A specification of criteria for a subquery about a connected entity.
 * 
 * @author Piotr Buczek
 */
@SuppressWarnings("unused")
@JsonObject("SearchSubCriteria")
public class SearchSubCriteria implements Serializable
{
    private static final long serialVersionUID = 1L;

    private SearchCriteria criteria;

    private SearchableEntityKind targetEntityKind;

    public static SearchSubCriteria createSampleParentCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.SAMPLE_PARENT, criteria);
    }

    public static SearchSubCriteria createSampleChildCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.SAMPLE_CHILD, criteria);
    }

    public static SearchSubCriteria createSampleContainerCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.SAMPLE_CONTAINER, criteria);
    }

    public static SearchSubCriteria createSampleCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.SAMPLE, criteria);
    }

    public static SearchSubCriteria createExperimentCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.EXPERIMENT, criteria);
    }

    public static SearchSubCriteria createDataSetContainerCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.DATA_SET_CONTAINER, criteria);
    }

    public static SearchSubCriteria createDataSetParentCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.DATA_SET_PARENT, criteria);
    }

    public static SearchSubCriteria createDataSetChildCriteria(SearchCriteria criteria)
    {
        return new SearchSubCriteria(SearchableEntityKind.DATA_SET_CHILD, criteria);
    }

    /**
     * Protected constructor. Use one of the factory methods to instantiate a {@link SearchSubCriteria}.
     * 
     * @param targetEntityKind
     * @param criteria
     */
    protected SearchSubCriteria(SearchableEntityKind targetEntityKind, SearchCriteria criteria)
    {
        this.targetEntityKind = targetEntityKind;
        this.criteria = criteria;
    }

    public SearchCriteria getCriteria()
    {
        return criteria;
    }

    public SearchableEntityKind getTargetEntityKind()
    {
        return targetEntityKind;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof MatchClause == false)
        {
            return false;
        }

        SearchSubCriteria other = (SearchSubCriteria) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(getTargetEntityKind(), other.getTargetEntityKind());
        builder.append(getCriteria(), other.getCriteria());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getTargetEntityKind());
        builder.append(getCriteria());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getTargetEntityKind());
        builder.append(getCriteria());
        return builder.toString();
    }

    //
    // JSON-RPC
    //
    private SearchSubCriteria()
    {

    }

    private void setCriteria(SearchCriteria criteria)
    {
        this.criteria = criteria;
    }

    private void setTargetEntityKind(SearchableEntityKind targetEntityKind)
    {
        this.targetEntityKind = targetEntityKind;
    }

}