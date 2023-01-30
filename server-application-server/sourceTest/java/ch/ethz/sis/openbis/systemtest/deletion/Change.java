package ch.ethz.sis.openbis.systemtest.deletion;

import java.util.Date;

class Change implements Comparable<Change>
{
    public final Date time;

    public final String userId;

    public final String value;

    public final boolean isRemoval;

    public final String key;

    public final String attachmentContent;

    public Change(Date time, String key, String userId, String value, String attachmentContent, boolean isRemoval)
    {
        this.time = time;
        this.key = key;
        this.userId = userId;
        this.value = value;
        this.attachmentContent = attachmentContent;
        this.isRemoval = isRemoval;
    }

    @Override
    public int compareTo(Change other)
    {
        if (this.time == null)
        {
            return other.time == null ? 0 : Long.compare(Long.MIN_VALUE, other.time.getTime());
        }
        int cmp = this.time.compareTo(other.time);
        if (cmp == 0)
        {
            if (this.isRemoval == other.isRemoval)
            {
                return 0;
            } else if (this.isRemoval)
            {
                return -1;
            } else
            {
                return 1;
            }
        } else
        {
            return cmp;
        }
    }

    @Override
    public String toString()
    {
        return key + " = " + value + " (" + time + ", removal " + isRemoval + ", userId: " + userId + ")";
    }
}