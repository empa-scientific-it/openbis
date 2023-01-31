/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * @author Pawel Glyzewski
 */
public class BevelArrowIcon implements Icon
{

    /** Constant indicating that the arrow is pointing up. */
    public static final int UP = 0;

    /** Constant indicating that the arrow is pointing down. */
    public static final int DOWN = 1;

    /** The default arrow size. */
    private static final int DEFAULT_SIZE = 11;

    /** Edge color 1. */
    private Color edge1;

    /** Edge color 2. */
    private Color edge2;

    /** The fill color for the arrow icon. */
    private Color fill;

    /** The size of the icon. */
    private int size;

    /** The direction that the arrow is pointing (UP or DOWN). */
    private int direction;

    /**
     * Standard constructor - builds an icon with the specified attributes.
     * 
     * @param direction .
     * @param isRaisedView .
     * @param isPressedView .
     */
    public BevelArrowIcon(final int direction, final boolean isRaisedView,
            final boolean isPressedView)
    {
        if (isRaisedView)
        {
            if (isPressedView)
            {
                init(UIManager.getColor("controlLtHighlight"),
                        UIManager.getColor("controlDkShadow"), UIManager.getColor("controlShadow"),
                        DEFAULT_SIZE, direction);
            } else
            {
                init(UIManager.getColor("controlHighlight"), UIManager.getColor("controlShadow"),
                        UIManager.getColor("control"), DEFAULT_SIZE, direction);
            }
        } else
        {
            if (isPressedView)
            {
                init(UIManager.getColor("controlDkShadow"),
                        UIManager.getColor("controlLtHighlight"),
                        UIManager.getColor("controlShadow"), DEFAULT_SIZE, direction);
            } else
            {
                init(UIManager.getColor("controlShadow"), UIManager.getColor("controlHighlight"),
                        UIManager.getColor("control"), DEFAULT_SIZE, direction);
            }
        }
    }

    /**
     * Standard constructor - builds an icon with the specified attributes.
     * 
     * @param edge1 the color of edge1.
     * @param edge2 the color of edge2.
     * @param fill the fill color.
     * @param size the size of the arrow icon.
     * @param direction the direction that the arrow points.
     */
    public BevelArrowIcon(final Color edge1, final Color edge2, final Color fill, final int size,
            final int direction)
    {
        init(edge1, edge2, fill, size, direction);
    }

    /**
     * Paints the icon at the specified position. Supports the Icon interface.
     * 
     * @param c .
     * @param g .
     * @param x .
     * @param y .
     */
    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y)
    {
        switch (this.direction)
        {
            case DOWN:
                drawDownArrow(g, x, y);
                break;
            case UP:
                drawUpArrow(g, x, y);
                break;
        }
    }

    /**
     * Returns the width of the icon. Supports the Icon interface.
     * 
     * @return the icon width.
     */
    @Override
    public int getIconWidth()
    {
        return this.size;
    }

    /**
     * Returns the height of the icon. Supports the Icon interface.
     * 
     * @return the icon height.
     */
    @Override
    public int getIconHeight()
    {
        return this.size;
    }

    /**
     * Initialises the attributes of the arrow icon.
     * 
     * @param edge1 the color of edge1.
     * @param edge2 the color of edge2.
     * @param fill the fill color.
     * @param size the size of the arrow icon.
     * @param direction the direction that the arrow points.
     */
    @SuppressWarnings("hiding")
    private void init(final Color edge1, final Color edge2, final Color fill, final int size,
            final int direction)
    {
        this.edge1 = edge1;
        this.edge2 = edge2;
        this.fill = fill;
        this.size = size;
        this.direction = direction;
    }

    /**
     * Draws the arrow pointing down.
     * 
     * @param g the graphics device.
     * @param xo ??
     * @param yo ??
     */
    private void drawDownArrow(final Graphics g, final int xo, final int yo)
    {
        g.setColor(this.edge1);
        g.drawLine(xo, yo, xo + this.size - 1, yo);
        g.drawLine(xo, yo + 1, xo + this.size - 3, yo + 1);
        g.setColor(this.edge2);
        g.drawLine(xo + this.size - 2, yo + 1, xo + this.size - 1, yo + 1);
        int x = xo + 1;
        int y = yo + 2;
        int dx = this.size - 6;
        while (y + 1 < yo + this.size)
        {
            g.setColor(this.edge1);
            g.drawLine(x, y, x + 1, y);
            g.drawLine(x, y + 1, x + 1, y + 1);
            if (0 < dx)
            {
                g.setColor(this.fill);
                g.drawLine(x + 2, y, x + 1 + dx, y);
                g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1);
            }
            g.setColor(this.edge2);
            g.drawLine(x + dx + 2, y, x + dx + 3, y);
            g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1);
            x += 1;
            y += 2;
            dx -= 2;
        }
        g.setColor(this.edge1);
        g.drawLine(xo + (this.size / 2), yo + this.size - 1, xo + (this.size / 2), yo + this.size
                - 1);
    }

    /**
     * Draws the arrow pointing up.
     * 
     * @param g the graphics device.
     * @param xo ??
     * @param yo ??
     */
    private void drawUpArrow(final Graphics g, final int xo, final int yo)
    {
        g.setColor(this.edge1);
        int x = xo + (this.size / 2);
        g.drawLine(x, yo, x, yo);
        x--;
        int y = yo + 1;
        int dx = 0;
        while (y + 3 < yo + this.size)
        {
            g.setColor(this.edge1);
            g.drawLine(x, y, x + 1, y);
            g.drawLine(x, y + 1, x + 1, y + 1);
            if (0 < dx)
            {
                g.setColor(this.fill);
                g.drawLine(x + 2, y, x + 1 + dx, y);
                g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1);
            }
            g.setColor(this.edge2);
            g.drawLine(x + dx + 2, y, x + dx + 3, y);
            g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1);
            x -= 1;
            y += 2;
            dx += 2;
        }
        g.setColor(this.edge1);
        g.drawLine(xo, yo + this.size - 3, xo + 1, yo + this.size - 3);
        g.setColor(this.edge2);
        g.drawLine(xo + 2, yo + this.size - 2, xo + this.size - 1, yo + this.size - 2);
        g.drawLine(xo, yo + this.size - 1, xo + this.size, yo + this.size - 1);
    }

}
