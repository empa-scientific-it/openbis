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

package ch.systemsx.cisd.openbis.dss.client.api.gui.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import ch.systemsx.cisd.openbis.dss.client.api.gui.UiUtilities;

/**
 * @author Pawel Glyzewski
 */
public class FilterableMutableTreeNode extends DefaultMutableTreeNode
{
    private static final long serialVersionUID = 1L;

    private static class FilteredEnumerationWrapper implements Enumeration<TreeNode>
    {
        private final Enumeration<TreeNode> enumeration;

        private final Pattern pattern;

        private TreeNode next;

        private FilteredEnumerationWrapper(final Enumeration<TreeNode> enumeration,
                final Pattern pattern)
        {
            this.enumeration = enumeration;
            this.pattern = pattern;
        }

        @Override
        public boolean hasMoreElements()
        {
            return next != null;
        }

        @Override
        public TreeNode nextElement()
        {
            if (next == null)
            {
                return enumeration.nextElement();
            }
            final TreeNode ret = next;
            next = null;

            while (enumeration.hasMoreElements())
            {
                final TreeNode tmp = enumeration.nextElement();
                if (UiUtilities.isMatchingNode(tmp, pattern))
                {
                    next = tmp;
                    break;
                }
            }

            return ret;
        }
    }

    private Pattern pattern = Pattern.compile(".*");

    private ArrayList<Object> filtered = new ArrayList<Object>();

    public FilterableMutableTreeNode(Object nodeValue)
    {
        super(nodeValue);

        filter(".*");
    }

    private synchronized Pattern getPattern()
    {
        return this.pattern;
    }

    private synchronized void setPattern(Pattern pattern)
    {
        this.pattern = pattern;
        filter(pattern);
    }

    private synchronized ArrayList<Object> getFiltered()
    {
        return this.filtered;
    }

    private synchronized void setFiltered(ArrayList<Object> filtered)
    {
        this.filtered = filtered;
    }

    public void filter(String filter)
    {
        @SuppressWarnings("hiding")
        Pattern pattern;

        try
        {
            pattern = Pattern.compile("(?i)" + filter);
        } catch (RuntimeException e)
        {
            pattern = Pattern.compile(".*");
        }
        setPattern(pattern);
        for (int i = 0, n = getChildCount(); i < n; i++)
        {
            ((FilterableMutableTreeNode) getChildAt(i)).setPattern(pattern);
        }
    }

    private void filter(@SuppressWarnings("hiding") Pattern pattern)
    {
        @SuppressWarnings("hiding")
        ArrayList<Object> filtered = new ArrayList<Object>();
        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> enumeration = super.children();
        while (enumeration.hasMoreElements())
        {
            Object o = enumeration.nextElement();
            if (UiUtilities.isMatchingNode(o, pattern))
            {
                filtered.add(o);
            }
        }
        Collections.sort(filtered, new Comparator<Object>()
            {
                @Override
                public int compare(Object o1, Object o2)
                {
                    return String.valueOf(o1).compareTo(String.valueOf(o2));
                }
            });
        setFiltered(filtered);
    }
    
    @Override
    public TreeNode getChildAt(int childIndex)
    {
        @SuppressWarnings("hiding")
        final ArrayList<Object> filtered = getFiltered();

        if (filtered == null)
        {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }
        return (TreeNode) filtered.get(childIndex);
    }

    @Override
    public int getChildCount()
    {
        @SuppressWarnings("hiding")
        final ArrayList<Object> filtered = getFiltered();

        if (filtered == null)
        {
            return 0;
        } else
        {
            return filtered.size();
        }
    }

    @Override
    public int getIndex(TreeNode aChild)
    {
        if (aChild == null)
        {
            throw new IllegalArgumentException("argument is null");
        }

        if (!isNodeChild(aChild))
        {
            return -1;
        }
        return getFiltered().indexOf(aChild); // linear search
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<TreeNode> children()
    {
        return new FilteredEnumerationWrapper(super.children(), getPattern());
    }
    
    @Override
    public void add(MutableTreeNode o)
    {
        add(o, true);
    }
    
    public void add(MutableTreeNode o, boolean filter)
    {
        super.add(o);
        if (o instanceof FilterableMutableTreeNode)
        {
            FilterableMutableTreeNode filterableMutableTreeNode = (FilterableMutableTreeNode) o;
            filterableMutableTreeNode.setPattern(getPattern());
        }
        if (filter)
        {
            filter();
        }
    }

    public void filter()
    {
        filter(getPattern());
    }

    
    @Override
    public void remove(int childIndex)
    {
        Object removed = getFiltered().remove(childIndex);
        if (removed != null)
        {
            children.remove(removed);
        }
    }

    @Override
    public void removeAllChildren()
    {
        super.removeAllChildren();
        filter();
    }
    
    
}
