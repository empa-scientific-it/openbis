/*
 * Copyright 2007 - 2018 ETH Zuerich, CISD and SIS.
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

package ch.systemsx.cisd.base.unix;

import java.io.File;
import java.io.IOException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.utilities.NativeLibraryUtilities;

/**
 * A utility class that provides access to common Unix system calls. Obviously, this will only work on Unix platforms and it requires a native library
 * to be loaded.
 * <p>
 * <i>Check with {@link #isOperational()} if this class is operational and only call the other methods if
 * <code>Unix.isOperational() == true</code>.</i>
 * 
 * @author Bernd Rinn
 */
public final class Unix
{

    /**
     * Method how processes are detected on this host.
     *
     */
    private enum ProcessDetection
    {
        /**
         * Process detection via <code>procfs</code> (Linux only).
         */
        PROCFS,
        
        /**
         * Process detection via the command line tool <code>ps</code>.
         */
        PS,
        
        /**
         * No working process detection found. 
         */
        NONE
    }

    private final static boolean operational;
    
    private final static ProcessDetection processDetection;

    private static volatile boolean useUnixRealtimeTimer = false;

    static
    {
        operational = NativeLibraryUtilities.loadNativeLibrary("unix");
        if (operational)
        {
            init();
            final int myPid = getPid();
            if (isProcessRunningProcFS(myPid))
            {
                processDetection = ProcessDetection.PROCFS;
            } else if (isProcessRunningPS(myPid))
            {
                processDetection = ProcessDetection.PS;
            } else
            {
                processDetection = ProcessDetection.NONE;
            }
        } else
        {
            processDetection = ProcessDetection.NONE;
        }
        useUnixRealtimeTimer = Boolean.getBoolean("unix.realtime.timer");
    }

    /** set user ID on execution */
    public static final short S_ISUID = 04000;

    /** set group ID on execution */
    public static final short S_ISGID = 02000;

    /** sticky bit */
    public static final short S_ISVTX = 01000;

    /** read by owner */
    public static final short S_IRUSR = 00400;

    /** write by owner */
    public static final short S_IWUSR = 00200;

    /** execute/search by owner */
    public static final short S_IXUSR = 00100;

    /** read by group */
    public static final short S_IRGRP = 00040;

    /** write by group */
    public static final short S_IWGRP = 00020;

    /** execute/search by group */
    public static final short S_IXGRP = 00010;

    /** read by others */
    public static final short S_IROTH = 00004;

    /** write by others */
    public static final short S_IWOTH = 00002;

    /** execute/search by others */
    public static final short S_IXOTH = 00001;

    /**
     * A class to represent a Unix <code>struct timespec</code> that holds a system time in nano-second resolution. 
     */
    public static final class Time
    {
        private final long secs;
        
        private final long nanos;

        private Time(long secs, long nanos)
        {
            this.secs = secs;
            this.nanos = nanos;
        }

        public long getSecs()
        {
            return secs;
        }

        public long getNanoSecPart()
        {
            return nanos;
        }

        public long getMicroSecPart()
        {
            if (nanos % 1000 >= 500)
            {
                return nanos / 1_000 + 1;
            } else
            {
                return nanos / 1_000;
            }
        }

        public long getMilliSecPart()
        {
            if (nanos % 1000000 >= 500000)
            {
                return nanos / 1_000_000 + 1;
            } else
            {
                return nanos / 1_000_000;
            }
        }
        
        public long getMillis()
        {
            return secs * 1_000 + getMilliSecPart();
        }

        @Override
        public String toString()
        {
            return "Time [secs=" + secs + ", nanos=" + nanos + "]";
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (nanos ^ (nanos >>> 32));
            result = prime * result + (int) (secs ^ (secs >>> 32));
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
            final Time other = (Time) obj;
            if (nanos != other.nanos)
            {
                return false;
            }
            if (secs != other.secs)
            {
                return false;
            }
            return true;
        }

    }
    
    /**
     * A class representing the Unix <code>stat</code> structure.
     */
    public static final class Stat
    {
        private final long deviceId;

        private final long inode;

        private final short permissions;

        private final FileLinkType linkType;

        private String symbolicLinkOrNull;

        private final int numberOfHardLinks;

        private final int uid;

        private final int gid;

        private final Time lastAccess;

        private final Time lastModified;

        private final Time lastStatusChange;

        private final long size;

        private final long numberOfBlocks;

        private final int blockSize;

        Stat(long deviceId, long inode, short permissions, byte linkType, int numberOfHardLinks,
                int uid, int gid, long lastAccess, long lastModified, long lastStatusChange,
                long size, long numberOfBlocks, int blockSize)
        {
            this(deviceId, inode, permissions, linkType, numberOfHardLinks, uid, gid,
                    lastAccess, 0, lastModified, 0, lastStatusChange, 0, size, numberOfBlocks, blockSize);
        }

        Stat(long deviceId, long inode, short permissions, byte linkType, int numberOfHardLinks,
                int uid, int gid, long lastAccess, long lastAccessNanos,
                long lastModified, long lastModifiedNanos,
                long lastStatusChange, long lastStatusChangeNanos,
                long size, long numberOfBlocks, int blockSize)
        {
            this.deviceId = deviceId;
            this.inode = inode;
            this.permissions = permissions;
            this.linkType = FileLinkType.values()[linkType];
            this.numberOfHardLinks = numberOfHardLinks;
            this.uid = uid;
            this.gid = gid;
            this.lastAccess = new Time(lastAccess, lastAccessNanos);
            this.lastModified = new Time(lastModified, lastModifiedNanos);
            this.lastStatusChange = new Time(lastStatusChange, lastStatusChangeNanos);
            this.size = size;
            this.numberOfBlocks = numberOfBlocks;
            this.blockSize = blockSize;
        }

        void setSymbolicLinkOrNull(String symbolicLinkOrNull)
        {
            this.symbolicLinkOrNull = symbolicLinkOrNull;
        }

        /**
         * Get link target of the symbolic link or <code>null</code>, if this is not a link or the link target has not been read.
         */
        public String tryGetSymbolicLink()
        {
            return symbolicLinkOrNull;
        }

        public long getDeviceId()
        {
            return deviceId;
        }

        /**
         * Returns the inode. 
         */
        public long getInode()
        {
            return inode;
        }

        public short getPermissions()
        {
            return permissions;
        }

        public FileLinkType getLinkType()
        {
            return linkType;
        }

        /**
         * Returns <code>true</code>, if this link is a symbolic link.
         */
        public final boolean isSymbolicLink()
        {
            return FileLinkType.SYMLINK == linkType;
        }

        /**
         * Returns the number of hard links for the <var>linkName</var>. Does not dereference a symbolic link.
         * 
         * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the link does not exist.
         */ 
        public int getNumberOfHardLinks()
        {
            return numberOfHardLinks;
        }

        public int getUid()
        {
            return uid;
        }

        public int getGid()
        {
            return gid;
        }

        /**
         * Time when file data last accessed.
         * <p>
         * Changed by the mknod(2), utimes(2) and read(2) system calls.
         * 
         * @return {@link Time} object containing seconds (to nano-second resolution) since the epoch.
         */
        public Time getLastAccessTime()
        {
            return lastAccess;
        }

        /**
         * Time when file data last accessed.
         * <p>
         * Changed by the mknod(2), utimes(2) and read(2) system calls.
         * 
         * @return Seconds since the epoch.
         */
        public long getLastAccess()
        {
            return lastAccess.getSecs();
        }

        /**
         * Time when file data last modified.
         * <p>
         * Changed by the mknod(2), utimes(2) and write(2) system calls.
         * 
         * @return {@link Time} object containing seconds (to nano-second resolution) since the epoch.
         */
        public Time getLastModifiedTime()
        {
            return lastModified;
        }

        /**
         * Time when file data last modified.
         * <p>
         * Changed by the mknod(2), utimes(2) and write(2) system calls.
         * 
         * @return Seconds since the epoch.
         */
        public long getLastModified()
        {
            return lastModified.getSecs();
        }

        /**
         * Time when file status was last changed (inode data modification).
         * <p>
         * Changed by the chmod(2), chown(2), link(2), mknod(2), rename(2), unlink(2), utimes(2) and write(2) system calls.
         * 
         * @return {@link Time} object containing seconds (to nano-second resolution) since the epoch.
         */
        public Time getLastStatusChangeTime()
        {
            return lastStatusChange;
        }

        /**
         * Time when file status was last changed (inode data modification).
         * <p>
         * Changed by the chmod(2), chown(2), link(2), mknod(2), rename(2), unlink(2), utimes(2) and write(2) system calls.
         * 
         * @return Seconds since the epoch.
         */
        public long getLastStatusChange()
        {
            return lastStatusChange.getSecs();
        }

        public long getSize()
        {
            return size;
        }

        public long getNumberOfBlocks()
        {
            return numberOfBlocks;
        }

        public int getBlockSize()
        {
            return blockSize;
        }

    }

    /**
     * A class representing the Unix <code>group</code> struct.
     */
    public static final class Group
    {
        private final String groupName;

        private final String groupPasswordHash;

        private final int gid;

        private final String[] groupMembers;

        Group(String groupName, String groupPasswordHash, int gid, String[] groupMembers)
        {
            this.groupName = groupName;
            this.groupPasswordHash = groupPasswordHash;
            this.gid = gid;
            this.groupMembers = groupMembers;
        }

        public String getGroupName()
        {
            return groupName;
        }

        public String getGroupPasswordHash()
        {
            return groupPasswordHash;
        }

        public int getGid()
        {
            return gid;
        }

        public String[] getGroupMembers()
        {
            return groupMembers;
        }
    }

    /**
     * A class representing the Unix <code>passwd</code> struct.
     */
    public static final class Password
    {
        private final String userName;

        private final String passwordHash;

        private final int uid;

        private final int gid;

        private final String userFullName;

        private final String homeDirectory;

        private final String shell;

        Password(String userName, String passwordHash, int uid, int gid, String userFullName,
                String homeDirectory, String shell)
        {
            this.userName = userName;
            this.passwordHash = passwordHash;
            this.uid = uid;
            this.gid = gid;
            this.userFullName = userFullName;
            this.homeDirectory = homeDirectory;
            this.shell = shell;
        }

        public String getUserName()
        {
            return userName;
        }

        public String getPasswordHash()
        {
            return passwordHash;
        }

        public int getUid()
        {
            return uid;
        }

        public int getGid()
        {
            return gid;
        }

        public String getUserFullName()
        {
            return userFullName;
        }

        public String getHomeDirectory()
        {
            return homeDirectory;
        }

        public String getShell()
        {
            return shell;
        }
    }

    private static void throwLinkCreationException(String type, String source, String target,
            String errorMessage)
    {
        throw new IOExceptionUnchecked(new IOException(String.format(
                "Creating %s link '%s' -> '%s': %s", type, target, source, errorMessage)));
    }

    private static void throwStatException(String filename, String errorMessage)
    {
        throw new IOExceptionUnchecked(new IOException(String.format(
                "Cannot obtain inode info for file '%s': %s", filename, errorMessage)));
    }

    private static void throwFileException(String operation, String filename, String errorMessage)
    {
        throw new IOExceptionUnchecked(new IOException(String.format("Cannot %s of file '%s': %s",
                operation, filename, errorMessage)));
    }

    private static void throwRuntimeException(String operation, String errorMessage)
    {
        throw new RuntimeException(String.format("Error on %s: %s", operation, errorMessage));
    }

    private static native int init();

    public static boolean isUseUnixRealtimeTimer()
    {
        return useUnixRealtimeTimer;
    }

    /**
     * Sets whether to use the Unix realttime timer.
     * <p>
     * <i>Note that old versions of Linux and MacOSX do not yet support this and will terminate the Java program when 
     * this flag is set to <code>true</code> and {@link #getSystemTime()} is called!</i>
     * @param useUnixRealTimeTimer if <code>true</code>, the realtime timer (nano-second resolution) will be used, 
     * otherwise the regular timer (micro-second resolution) will be used.
     */
    public static void setUseUnixRealtimeTimer(boolean useUnixRealTimeTimer)
    {
        Unix.useUnixRealtimeTimer = useUnixRealTimeTimer;
    }

    private static native int getpid();

    private static native int getuid();

    private static native int geteuid();

    private static native int getgid();

    private static native int getegid();

    private static native int link(String filename, String linktarget);

    private static native int symlink(String filename, String linktarget);

    private static native Stat stat(String filename);

    private static native Stat lstat(String filename);

    private static native String readlink(String filename, int linkvallen);

    private static native int chmod(String filename, short mode);

    private static native int chown(String filename, int uid, int gid);

    private static native int lchown(String filename, int uid, int gid);

    private static native int clock_gettime(final long[] time);
    
    private static native int clock_gettime2(final long[] time);
    
    private static native int lutimes(String filename,
            long accessTimeSecs, long accessTimeMicroSecs,
            long modificationTimeSecs, long modificationTimeMicroSecs);

    private static native int utimes(String filename,
            long accessTimeSecs, long accessTimeMicroSecs,
            long modificationTimeSecs, long modificationTimeMicroSecs);

    private static native String getuser(int uid);

    private static native String getgroup(int gid);

    private static native int getuid(String user);

    private static native Password getpwnam(String user);

    private static native Password getpwuid(int uid);

    private static native int getgid(String group);

    private static native Group getgrnam(String group);

    private static native Group getgrgid(int gid);

    private static native String strerror(int errnum);

    private static native String strerror();
    
    @Private
    static boolean isProcessRunningProcFS(int pid)
    {
        return new File("/proc/" + pid).isDirectory();
    }

    @Private
    static boolean isProcessRunningPS(int pid)
    {
        try
        {
            return Runtime.getRuntime().exec(new String[] { "ps", "-p", Integer.toString(pid) }).waitFor() == 0;
        } catch (IOException ex)
        {
            return false;
        } catch (InterruptedException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    //
    // Public
    //

    /**
     * Returns <code>true</code>, if the native library has been loaded successfully and the link utilities are operational, <code>false</code>
     * otherwise.
     */
    public static final boolean isOperational()
    {
        return operational;
    }

    /**
     * Returns <code>true</code>, if process detection is available on this system.
     */
    public static boolean canDetectProcesses()
    {
        return processDetection != ProcessDetection.NONE;
    }

    /**
     * Returns the last error that occurred in this class. Use this to find out what went wrong after {@link #tryGetLinkInfo(String)} or
     * {@link #tryGetFileInfo(String)} returned <code>null</code>.
     */
    public static String getLastError()
    {
        return strerror();
    }

    //
    // Process functions
    //

    /**
     * Returns the process identifier of the current process.
     */
    public static int getPid()
    {
        return getpid();
    }

    /**
     * Returns <code>true</code>, if the process with <var>pid</var> is currently running and <code>false</code>, if it is not running or if process
     * detection is not available ( {@link #canDetectProcesses()} <code>== false</code>).
     */
    public static boolean isProcessRunning(int pid)
    {
        switch (processDetection)
        {
            case PROCFS:
                return isProcessRunningProcFS(pid);
            case PS:
                return isProcessRunningPS(pid);
            default:
                return false;
        }
    }

    /**
     * Returns the uid of the user that started this process.
     */
    public static final int getUid()
    {
        return getuid();
    }

    /**
     * Returns the effective uid that determines the permissions of this process.
     */
    public static final int getEuid()
    {
        return geteuid();
    }

    /**
     * Returns the gid of the user that started this process.
     */
    public static final int getGid()
    {
        return getgid();
    }

    /**
     * Returns the effective gid that determines the permissions of this process.
     */
    public static final int getEgid()
    {
        return getegid();
    }

    //
    // Time functions
    //

    /**
     * Gets the current system time.
     * 
     * @return the system time as <i>seconds since the epoch</i> and, in addition, 
     *         nano-seconds since the current second.
     */
    public static final Time getSystemTime()
    {
        final long[] time = new long[2];
        final int result = useUnixRealtimeTimer ? clock_gettime(time) : clock_gettime2(time);
        if (result < 0)
        {
            throwRuntimeException("get system time", strerror(result));
        }
        return new Time(time[0], time[1]);
    }
    
    /**
     * Gets the current system time.
     * 
     * @return the system time as <i>milli-seconds since the epoch</i>.
     */
    public static final long getSystemTimeMillis()
    {
        return getSystemTime().getMillis();
    }
    
    //
    // File functions
    //

    /**
     * Creates a hard link <var>linkName</var> that points to <var>fileName</var>.
     * 
     * @throws IOExceptionUnchecked If the underlying system call fails, e.g. because <var>linkName</var> already exists or <var>fileName</var> does
     *             not exist.
     */
    public static final void createHardLink(String fileName, String linkName)
            throws IOExceptionUnchecked
    {
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }
        if (linkName == null)
        {
            throw new NullPointerException("linkName");
        }
        final int result = link(fileName, linkName);
        if (result < 0)
        {
            throwLinkCreationException("hard", fileName, linkName, strerror(result));
        }
    }

    /**
     * Creates a symbolic link <var>linkName</var> that points to <var>fileName</var>.
     * 
     * @throws IOExceptionUnchecked If the underlying system call fails, e.g. because <var>linkName</var> already exists.
     */
    public static final void createSymbolicLink(String fileName, String linkName)
            throws IOExceptionUnchecked
    {
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }
        if (linkName == null)
        {
            throw new NullPointerException("linkName");
        }
        final int result = symlink(fileName, linkName);
        if (result < 0)
        {
            throwLinkCreationException("symbolic", fileName, linkName, strerror(result));
        }
    }

    private static Stat tryGetStat(String fileName) throws IOExceptionUnchecked
    {
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }
        return stat(fileName);
    }

    private static Stat getStat(String fileName) throws IOExceptionUnchecked
    {
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }
        final Stat result = stat(fileName);
        if (result == null)
        {
            throwStatException(fileName, strerror());
        }
        return result;
    }

    private static Stat tryGetLStat(String linkName) throws IOExceptionUnchecked
    {
        if (linkName == null)
        {
            throw new NullPointerException("linkName");
        }
        return lstat(linkName);
    }

    private static Stat getLStat(String linkName) throws IOExceptionUnchecked
    {
        if (linkName == null)
        {
            throw new NullPointerException("linkName");
        }
        final Stat result = lstat(linkName);
        if (result == null)
        {
            throwStatException(linkName, strerror());
        }
        return result;
    }

    /**
     * Returns the inode for the <var>fileName</var>. Does not dereference a symbolic link. 
     * 
     * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the link does not exist.
     * 
     * @deprecated Use method {@link Stat#getInode(String)} from {@link #getLinkInfo(String)} instead.
     */
    @Deprecated
    public static final long getInode(String linkName) throws IOExceptionUnchecked
    {
        return getLStat(linkName).getInode();
    }

    /**
     * Returns the number of hard links for the <var>linkName</var>. Does not dereference a symbolic link.
     * 
     * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the link does not exist.
     * 
     * @deprecated Use method {@link Stat#getNumberOfHardLinks(String)} from {@link #getLinkInfo(String)} instead.
     */
    @Deprecated
    public static final int getNumberOfHardLinks(String linkName) throws IOExceptionUnchecked
    {
        return getLStat(linkName).getNumberOfHardLinks();
    }

    /**
     * Returns <code>true</code> if <var>linkName</var> is a symbolic link and <code>false</code> otherwise.
     * 
     * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the link does not exist.
     */
    public static final boolean isSymbolicLink(String linkName) throws IOExceptionUnchecked
    {
        return getLStat(linkName).isSymbolicLink();
    }

    /**
     * Returns the value of the symbolik link <var>linkName</var>, or <code>null</code>, if <var>linkName</var> is not a symbolic link.
     * 
     * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the link does not exist.
     */
    public static final String tryReadSymbolicLink(String linkName) throws IOExceptionUnchecked
    {
        final Stat stat = getLStat(linkName);
        return stat.isSymbolicLink() ? readlink(linkName, (int) stat.getSize()) : null;
    }

    /**
     * Returns the information about <var>fileName</var>.
     * 
     * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the file does not exist.
     */
    public static final Stat getFileInfo(String fileName) throws IOExceptionUnchecked
    {
        return getStat(fileName);
    }

    /**
     * Returns the information about <var>fileName</var>, or {@link NullPointerException}, if the information could not be obtained, e.g. because the
     * file does not exist (call {@link #getLastError()} to find out what went wrong).
     */
    public static final Stat tryGetFileInfo(String fileName) throws IOExceptionUnchecked
    {
        return tryGetStat(fileName);
    }

    /**
     * Returns the information about <var>linkName</var>.
     * 
     * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the link does not exist.
     */
    public static final Stat getLinkInfo(String linkName) throws IOExceptionUnchecked
    {
        return getLinkInfo(linkName, true);
    }

    /**
     * Returns the information about <var>linkName</var>. If <code>readSymbolicLinkTarget == true</code>, then the symbolic link target is read when
     * <var>linkName</var> is a symbolic link.
     * 
     * @throws IOExceptionUnchecked If the information could not be obtained, e.g. because the link does not exist.
     */
    public static final Stat getLinkInfo(String linkName, boolean readSymbolicLinkTarget)
            throws IOExceptionUnchecked
    {
        final Stat stat = getLStat(linkName);
        final String symbolicLinkOrNull =
                (readSymbolicLinkTarget && stat.isSymbolicLink()) ? readlink(linkName,
                        (int) stat.getSize()) : null;
        stat.setSymbolicLinkOrNull(symbolicLinkOrNull);
        return stat;
    }

    /**
     * Returns the information about <var>linkName</var>, or {@link NullPointerException}, if the information could not be obtained, e.g. because the
     * link does not exist (call {@link #getLastError()} to find out what went wrong).
     */
    public static final Stat tryGetLinkInfo(String linkName) throws IOExceptionUnchecked
    {
        return tryGetLinkInfo(linkName, true);
    }

    /**
     * Returns the information about <var>linkName</var>, or <code>null</code> if the information can not be obtained, e.g. because the link does not
     * exist (call {@link #getLastError()} to find out what went wrong). If <code>readSymbolicLinkTarget == true</code>, then the symbolic link target
     * is read when <var>linkName</var> is a symbolic link.
     */
    public static final Stat tryGetLinkInfo(String linkName, boolean readSymbolicLinkTarget)
            throws IOExceptionUnchecked
    {
        final Stat stat = tryGetLStat(linkName);
        if (stat == null)
        {
            return null;
        }
        final String symbolicLinkOrNull =
                (readSymbolicLinkTarget && stat.isSymbolicLink()) ? readlink(linkName,
                        (int) stat.getSize()) : null;
        stat.setSymbolicLinkOrNull(symbolicLinkOrNull);
        return stat;
    }

    /**
     * Sets the access mode of <var>filename</var> to the specified <var>mode</var> value.
     * Dereferences a symbolic link.
     */
    public static final void setAccessMode(String fileName, short mode) throws IOExceptionUnchecked
    {
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }
        final int result = chmod(fileName, mode);
        if (result < 0)
        {
            throwFileException("set mode", fileName, strerror(result));
        }
    }

    /**
     * Sets the owner of <var>fileName</var> to the specified <var>uid</var> and <var>gid</var> values. 
     * Dereferences a symbolic link.
     */
    public static final void setOwner(String fileName, int uid, int gid)
            throws IOExceptionUnchecked
    {
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }
        final int result = chown(fileName, uid, gid);
        if (result < 0)
        {
            throwFileException("set owner", fileName, strerror(result));
        }
    }

    /**
     * Sets the owner of <var>fileName</var> to the <var>uid</var> and <var>gid</var> of the specified <code>user</code>. 
     * Dereferences a symbolic link.
     */
    public static final void setOwner(String fileName, Password user)
            throws IOExceptionUnchecked
    {
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }
        final int result = chown(fileName, user.getUid(), user.getGid());
        if (result < 0)
        {
            throwFileException("set owner", fileName, strerror(result));
        }
    }

    /**
     * Sets the owner of <var>linkName</var> to the specified <var>uid</var> and <var>gid</var> values. 
     * Does not dereference a symbolic link.
     */
    public static final void setLinkOwner(String linkName, int uid, int gid)
            throws IOExceptionUnchecked
    {
        if (linkName == null)
        {
            throw new NullPointerException("linkName");
        }
        final int result = lchown(linkName, uid, gid);
        if (result < 0)
        {
            throwFileException("set link owner", linkName, strerror(result));
        }
    }

    /**
     * Sets the owner of <var>linkName</var> to the <var>uid</var> and <var>gid</var> of the specified <code>user</code>. 
     * Does not dereference a symbolic link.
     */
    public static final void setLinkOwner(String linkName, Password user)
            throws IOExceptionUnchecked
    {
        if (linkName == null)
        {
            throw new NullPointerException("linkName");
        }
        final int result = lchown(linkName, user.getUid(), user.getGid());
        if (result < 0)
        {
            throwFileException("set owner", linkName, strerror(result));
        }
    }

    /**
     * Change link timestamps of a file, directory or link. Does not dereference a symbolic link.
     * 
     * @param fileName The name of the file or link to change the timestamp of.
     * @param accessTimeSecs The new access time in seconds since start of the epoch.
     * @param accessTimeMicroSecs The micro-second part of the new access time.
     * @param modificationTimeSecs The new modification time in seconds since start of the epoch.
     * @param modificationTimeMicroSecs The micro-second part of the new modification time.
     */
    public static void setLinkTimestamps(String fileName,
            long accessTimeSecs, long accessTimeMicroSecs,
            long modificationTimeSecs, long modificationTimeMicroSecs) throws IOExceptionUnchecked
    {
        final int result = lutimes(fileName, accessTimeSecs, accessTimeMicroSecs,
                            modificationTimeSecs, modificationTimeMicroSecs);
        if (result < 0)
        {
            throwFileException("set file timestamps", fileName, strerror(result));
        }
    }

    /**
     * Change file timestamps of a file, directory or link. Does not dereference a symbolic link.
     * 
     * @param fileName The name of the file or link to change the timestamp of.
     * @param accessTimeSecs The new access time in seconds since start of the epoch.
     * @param modificationTimeSecs The new modification time in seconds since start of the epoch.
     */
    public static void setLinkTimestamps(String fileName,
            long accessTimeSecs, long modificationTimeSecs) throws IOExceptionUnchecked
    {
        setLinkTimestamps(fileName, accessTimeSecs, 0, modificationTimeSecs, 0);
    }

    /**
     * Change file timestamps of a file, directory or link. Does not dereference a symbolic link.
     * 
     * @param fileName The name of the file or link to change the timestamp of.
     * @param accessTime The new access time as {@link Time} object.
     * @param modificationTime The new modification time as {@link Time} object.
     */
    public static void setLinkTimestamps(String fileName,
            Time accessTime, Time modificationTime) throws IOExceptionUnchecked
    {
        setLinkTimestamps(fileName, accessTime.getSecs(), accessTime.getMicroSecPart(), 
                modificationTime.getSecs(), modificationTime.getMicroSecPart());
    }

    /**
     * Change file timestamps of a file, directory or link to the current time. Does not dereference a symbolic link.
     * 
     * @param fileName The name of the file or link to change the timestamp of.
     */
    public static void setLinkTimestamps(String fileName) throws IOExceptionUnchecked
    {
        final Time now = getSystemTime();
        final int result = lutimes(fileName, now.getSecs(), now.getMicroSecPart(), now.getSecs(), now.getMicroSecPart());
        if (result < 0)
        {
            throwFileException("set file timestamps", fileName, strerror(result));
        }
    }

    /**
     * Change file timestamps of a file, directory or link. Dereferences a symbolic link.
     * 
     * @param fileName The name of the file or link to change the timestamp of.
     * @param accessTimeSecs The new access time in seconds since start of the epoch.
     * @param accessTimeMicroSecs The micro-second part of the new access time.
     * @param modificationTimeSecs The new modification time in seconds since start of the epoch.
     * @param modificationTimeMicroSecs The micro-second part of the new modification time.
     */
    public static void setFileTimestamps(String fileName,
            long accessTimeSecs, long accessTimeMicroSecs,
            long modificationTimeSecs, long modificationTimeMicroSecs) throws IOExceptionUnchecked
    {
        final int result = utimes(fileName, accessTimeSecs, accessTimeMicroSecs,
                            modificationTimeSecs, modificationTimeMicroSecs);
        if (result < 0)
        {
            throwFileException("set file timestamps", fileName, strerror(result));
        }
    }

    /**
     * Change file timestamps of a file, directory or link. Does not dereference a symbolic link.
     * 
     * @param fileName The name of the file or link to change the timestamp of.
     * @param accessTimeSecs The new access time in seconds since start of the epoch.
     * @param modificationTimeSecs The new modification time in seconds since start of the epoch.
     */
    public static void setFileTimestamps(String fileName,
            long accessTimeSecs, long modificationTimeSecs) throws IOExceptionUnchecked
    {
        setFileTimestamps(fileName, accessTimeSecs, 0, modificationTimeSecs, 0);
    }

    /**
     * Change file timestamps of a file, directory or link. Does not dereference a symbolic link.
     * 
     * @param fileName The name of the file or link to change the timestamp of.
     * @param accessTime The new access time as {@link Time} object.
     * @param modificationTime The new modification time as {@link Time} object.
     */
    public static void setFileTimestamps(String fileName,
            Time accessTime, Time modificationTime) throws IOExceptionUnchecked
    {
        setFileTimestamps(fileName, accessTime.getSecs(), accessTime.getMicroSecPart(), 
                modificationTime.getSecs(), modificationTime.getMicroSecPart());
    }

    /**
     * Change file timestamps of a file, directory or link to the current time. Does not dereference a symbolic link.
     * 
     * @param fileName The name of the file or link to change the timestamp of.
     */
    public static void setFileTimestamps(String fileName) throws IOExceptionUnchecked
    {
        final Time now = getSystemTime();
        final int result = utimes(fileName, now.getSecs(), now.getMicroSecPart(), now.getSecs(), now.getMicroSecPart());
        if (result < 0)
        {
            throwFileException("set file timestamps", fileName, strerror(result));
        }
    }

    //
    // User functions
    //

    /**
     * Returns the name of the user identified by <var>uid</var>.
     */
    public static final String tryGetUserNameForUid(int uid)
    {
        return getuser(uid);
    }

    /**
     * Returns the uid of the <var>userName</var>, or <code>-1</code>, if no user with this name exists.
     */
    public static final int getUidForUserName(String userName)
    {
        if (userName == null)
        {
            throw new NullPointerException("userName");
        }
        return getuid(userName);
    }

    /**
     * Returns the {@link Password} for the given <var>userName</var>, or <code>null</code>, if no user with that name exists.
     */
    public static final Password tryGetUserByName(String userName)
    {
        if (userName == null)
        {
            throw new NullPointerException("userName");
        }
        return getpwnam(userName);
    }

    /**
     * Returns the {@link Password} for the given <var>userName</var>, or <code>null</code>, if no user with that name exists.
     */
    public static final Password tryGetUserByUid(int uid)
    {
        return getpwuid(uid);
    }

    //
    // Group functions
    //

    /**
     * Returns the name of the group identified by <var>gid</var>, or <code>null</code>, if no group with that <var>gid</var> exists.
     */
    public static final String tryGetGroupNameForGid(int gid)
    {
        return getgroup(gid);
    }

    /**
     * Returns the gid of the <var>groupName</var>, or <code>-1</code>, if no group with this name exists.
     */
    public static final int getGidForGroupName(String groupName)
    {
        if (groupName == null)
        {
            throw new NullPointerException("groupName");
        }
        return getgid(groupName);
    }

    /**
     * Returns the {@link Group} for the given <var>groupName</var>, or <code>null</code>, if no group with that name exists.
     */
    public static final Group tryGetGroupByName(String groupName)
    {
        if (groupName == null)
        {
            throw new NullPointerException("groupName");
        }
        return getgrnam(groupName);
    }

    /**
     * Returns the {@link Group} for the given <var>gid</var>, or <code>null</code>, if no group with that gid exists.
     */
    public static final Group tryGetGroupByGid(int gid)
    {
        return getgrgid(gid);
    }

    //
    // Error
    //

    /**
     * Returns the error string for the given <var>errnum</var>.
     */
    public static final String getErrorString(int errnum)
    {
        return strerror(errnum);
    }

}
