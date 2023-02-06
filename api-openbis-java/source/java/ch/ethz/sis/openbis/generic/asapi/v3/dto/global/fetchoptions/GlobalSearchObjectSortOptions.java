package ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOrder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.global.fetchoptions.GlobalSearchObjectSortOptions")
public class GlobalSearchObjectSortOptions extends SortOptions<GlobalSearchObject>
{

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    public static final String SCORE = "SCORE";

    @JsonIgnore
    public static final String OBJECT_KIND = "OBJECT_KIND";

    @JsonIgnore
    public static final String OBJECT_PERM_ID = "OBJECT_PERM_ID";

    @JsonIgnore
    public static final String OBJECT_IDENTIFIER = "OBJECT_IDENTIFIER";

    public SortOrder score()
    {
        return getOrCreateSorting(SCORE);
    }

    public SortOrder objectKind()
    {
        return getOrCreateSorting(OBJECT_KIND);
    }

    public SortOrder objectPermId()
    {
        return getOrCreateSorting(OBJECT_PERM_ID);
    }

    public SortOrder objectIdentifier()
    {
        return getOrCreateSorting(OBJECT_IDENTIFIER);
    }

}