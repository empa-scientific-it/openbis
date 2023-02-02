package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Identifier of a well on a screening plate, contains row and column number.
 * 
 * @author Tomasz Pylak
 */
@SuppressWarnings("unused")
@JsonObject("WellPosition")
public class WellPosition implements Serializable, Comparable<WellPosition>
{
    private static final long serialVersionUID = 1L;

    /**
     * Parses a white-space separated list of well position descriptions. A well position description is of the form
     * <code><it>&lt;row number&gt;</it>.<it>&lt;column number&gt;</it></code>.
     * 
     * @throws IllegalArgumentException in case of parsing error.
     * @since 1.4
     */
    public static List<WellPosition> parseWellPositions(String wellPositionDescriptions)
    {
        StringTokenizer tokenizer = new StringTokenizer(wellPositionDescriptions);
        List<WellPosition> positions = new ArrayList<WellPosition>();
        while (tokenizer.hasMoreTokens())
        {
            positions.add(parseWellPosition(tokenizer.nextToken()));
        }
        return positions;
    }

    /**
     * Parses a well position description of the form <code><it>&lt;row number&gt;</it>.<it>&lt;column number&gt;</it></code>.
     * 
     * @throws IllegalArgumentException in case of parsing error.
     * @since 1.4
     */
    public static WellPosition parseWellPosition(String wellDescription)
    {
        int indexOfDot = wellDescription.indexOf('.');
        if (indexOfDot < 1)
        {
            throw createException("Expecting a '.' in well description", wellDescription);
        }
        int row = getAndCheckRowNumber(indexOfDot, wellDescription);
        int col = getAndCheckColumnNumber(indexOfDot, wellDescription);
        return new WellPosition(row, col);
    }

    private static int getAndCheckColumnNumber(int indexOfDot, String well)
    {
        int col;
        try
        {
            col = Integer.parseInt(well.substring(indexOfDot + 1));
        } catch (NumberFormatException ex)
        {
            throw createException("String after '.' isn't a number", well);
        }
        if (col < 1)
        {
            throw createException("First column < 1", well);
        }
        return col;
    }

    private static int getAndCheckRowNumber(int indexOfDot, String well)
    {
        int row;
        try
        {
            row = Integer.parseInt(well.substring(0, indexOfDot));
        } catch (NumberFormatException ex)
        {
            throw createException("String before '.' isn't a number", well);
        }
        if (row < 1)
        {
            throw createException("First row < 1", well);
        }
        return row;
    }

    private static IllegalArgumentException createException(String description, String well)
    {
        return new IllegalArgumentException("Invalid well description: " + description + ": "
                + well);
    }

    private int wellRow, wellColumn;

    public WellPosition(int wellRow, int wellColumn)
    {
        this.wellRow = wellRow;
        this.wellColumn = wellColumn;
    }

    /** well row, starts from 1 */
    public int getWellRow()
    {
        return wellRow;
    }

    /** well column, starts from 1 */
    public int getWellColumn()
    {
        return wellColumn;
    }

    @Override
    public String toString()
    {
        return "[" + wellRow + ", " + wellColumn + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + wellColumn;
        result = prime * result + wellRow;
        return result;
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
        final WellPosition other = (WellPosition) obj;
        if (wellColumn != other.wellColumn)
        {
            return false;
        }
        if (wellRow != other.wellRow)
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(WellPosition other)
    {
        int diff = wellRow - other.wellRow;
        return diff != 0 ? diff : wellColumn - other.wellColumn;
    }

    //
    // JSON-RPC
    //

    private WellPosition()
    {
    }

    private void setWellRow(int wellRow)
    {
        this.wellRow = wellRow;
    }

    private void setWellColumn(int wellColumn)
    {
        this.wellColumn = wellColumn;
    }

}