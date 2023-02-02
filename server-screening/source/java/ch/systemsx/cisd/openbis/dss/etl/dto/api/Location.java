package ch.systemsx.cisd.openbis.dss.etl.dto.api;

/**
 * Auxiliary structure to store tile location on the well. The top left tile has coordinates (1,1).
 * 
 * @author Tomasz Pylak
 */
public class Location
{
    private final int row, column;

    /** Note: The top left tile has coordinates (1,1). */
    public Location(int row, int column)
    {
        this.row = row;
        this.column = column;
    }

    public int getRow()
    {
        return row;
    }

    public int getColumn()
    {
        return column;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Location other = (Location) obj;
        return column == other.column && row == other.row;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + row;
        return result;
    }

    @Override
    public String toString()
    {
        return "[" + row + ":" + column + "]";
    }
}