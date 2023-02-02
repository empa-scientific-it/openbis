package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Contains data which uniquely define a plate.
 * 
 * @author Tomasz Pylak
 */
@SuppressWarnings("unused")
@JsonObject("PlateIdentifier")
public class PlateIdentifier extends PermanentIdentifier
{
    private static final long serialVersionUID = 1L;

    private String plateCode;

    @JsonProperty
    private String spaceCodeOrNull;

    private String projectCodeOrNull;

    /**
     * Creates a {@link PlateIdentifier} from the given <var>augmentedCode</code>.
     * 
     * @param augmentedCode The <var>augmentedCode</code> in the form <code>/SPACE/PROJECT/EXPERIMENT</code>
     * @return A plate identifier corresponding to <var>augmentedCode</code>. Note that this plate identifier has no perm id set.
     * @throws IllegalArgumentException If the <var>augmentedCode</code> is not in one of the forms <code>/SPACE/PLATE', /PLATE or PLATE</code>.
     */
    public static PlateIdentifier createFromAugmentedCode(String augmentedCode)
            throws IllegalArgumentException
    {
        final String[] splitted = augmentedCode.split("/");
        if (splitted.length == 1) // Sample in home space
        {
            return new PlateIdentifier(splitted[0], null);
        }
        if (splitted.length == 2 && splitted[0].length() == 0) // Shared sample
        {
            return new PlateIdentifier(splitted[1], "");
        }
        if (splitted.length == 4)
        {
            return new PlateIdentifier(splitted[3], splitted[1], splitted[2], null);
        }
        if (splitted.length != 3 || splitted[0].length() != 0)
        {
            throw new IllegalArgumentException("Augmented code '" + augmentedCode
                    + "' needs to be of the form '/SPACE/PROJECT/PLATE', '/SPACE/PLATE', '/PLATE' or 'PLATE'.");
        }
        return new PlateIdentifier(splitted[2], splitted[1]);
    }

    /**
     * Creates a {@link PlateIdentifier} from the given <var>permId</code>.
     * 
     * @param permId The <var>permId</code>
     * @return A plate identifier corresponding to <var>permId</code>. Note that this plate identifier has no code or space set.
     * @throws IllegalArgumentException If the <var>augmentedCode</code> is not in one of the forms <code>/SPACE/PLATE', /PLATE or PLATE</code>.
     */
    public static PlateIdentifier createFromPermId(String permId) throws IllegalArgumentException
    {
        return new PlateIdentifier(null, null, permId);
    }

    /**
     * An empty <var>spaceCode</var> is interpreted as the home space, a <code>null</code> <var>spaceCode</code> is interpreted as 'no space', i.e.
     * identifies a shared sample.
     */
    protected PlateIdentifier(String plateCode, String spaceCodeOrNull)
    {
        this(plateCode, spaceCodeOrNull, null);
    }

    public PlateIdentifier(String plateCode, String spaceCodeOrNull, String permId)
    {
        this(plateCode, spaceCodeOrNull, null, permId);
    }

    public PlateIdentifier(String plateCode, String spaceCodeOrNull, String projectCodeOrNull, String permId)
    {
        super(permId);
        this.plateCode = plateCode;
        this.spaceCodeOrNull = spaceCodeOrNull;
        this.projectCodeOrNull = projectCodeOrNull;
    }

    /**
     * A code of the plate.
     */
    public String getPlateCode()
    {
        return plateCode;
    }

    /**
     * A code of the space to which the plate belongs or <code>null</code> if it is a shared plate.
     */
    public String tryGetSpaceCode()
    {
        return spaceCodeOrNull;
    }

    public String tryGetProjectCode()
    {
        return projectCodeOrNull;
    }

    /**
     * Returns the augmented (full) code of this plate.
     */
    @JsonIgnore
    public String getAugmentedCode()
    {
        if (projectCodeOrNull != null)
        {
            return "/" + spaceCodeOrNull + "/" + projectCodeOrNull + "/" + plateCode;
        }
        if (spaceCodeOrNull != null)
        {
            if (isSharedPlate())
            {
                return "/" + plateCode;
            } else
            {
                return "/" + spaceCodeOrNull + "/" + plateCode;
            }
        } else
        {
            return plateCode;
        }
    }

    @JsonIgnore
    public boolean isSharedPlate()
    {
        return "".equals(spaceCodeOrNull);
    }

    @Override
    public int hashCode()
    {
        if (getPermId() != null)
        {
            return getPermId().hashCode();
        }
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((plateCode == null) ? 0 : plateCode.hashCode());
        result = prime * result + ((spaceCodeOrNull == null) ? 0 : spaceCodeOrNull.hashCode());
        result = prime * result + ((projectCodeOrNull == null) ? 0 : projectCodeOrNull.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (getPermId() != null)
        {
            return super.equals(obj);
        }
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (!(obj instanceof PlateIdentifier))
        {
            return false;
        }
        PlateIdentifier other = (PlateIdentifier) obj;
        return equals(plateCode, other.plateCode) && equals(spaceCodeOrNull, other.spaceCodeOrNull)
                && equals(projectCodeOrNull, other.projectCodeOrNull);
    }

    private boolean equals(Object obj1, Object obj2)
    {
        return obj1 == null ? obj1 == obj2 : obj1.equals(obj2);
    }

    @Override
    public String toString()
    {
        if (getPermId() == null)
        {
            return getAugmentedCode();
        } else
        {
            return getAugmentedCode() + " [" + getPermId() + "]";
        }
    }

    //
    // JSON-RPC
    //

    private PlateIdentifier()
    {
        super(null);
    }

    private void setPlateCode(String plateCode)
    {
        this.plateCode = plateCode;
    }

    private String getSpaceCodeOrNull()
    {
        return spaceCodeOrNull;
    }

    private void setSpaceCodeOrNull(String spaceCodeOrNull)
    {
        this.spaceCodeOrNull = spaceCodeOrNull;
    }

    private String getProjectCodeOrNull()
    {
        return projectCodeOrNull;
    }

    private void setProjectCodeOrNull(String projectCodeOrNull)
    {
        this.projectCodeOrNull = projectCodeOrNull;
    }

}