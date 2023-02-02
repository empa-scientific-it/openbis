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

package ch.systemsx.cisd.common.filesystem.highwatermark;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.NonHangingFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.CallableExecutor;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * A high water mark watcher.
 * <p>
 * This class is thread-safe.
 * </p>
 * 
 * @see FileSystemUtils
 * @author Christian Ribeaud
 */
public final class HighwaterMarkWatcher implements Runnable
{

    private static final String UNSPECIFIED = "unspecified";

    private final static IFreeSpaceProvider DEFAULT_FREE_SPACE_PROVIDER =
            new NonHangingFreeSpaceProvider(new SimpleFreeSpaceProvider());

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HighwaterMarkWatcher.class);

    /**
     * The high water mark value in <i>kilobytes</i>.
     */
    private final long highwaterMarkInKb;

    private final EventListenerList listenerList = new EventListenerList();

    /** The path to get free space for. Is not <code>null</code>, not empty on Unix. */
    private HostAwareFile path;

    /** The last {@link HighwaterMarkState} computed. */
    private HighwaterMarkState highwaterMarkState;

    private final IFreeSpaceProvider freeSpaceProvider;

    /**
     * @param highwaterMarkInKb the high water mark value in kilobytes. If negative, then {@link #run()} always returns without doing anything.
     */
    public HighwaterMarkWatcher(final long highwaterMarkInKb)
    {
        this(highwaterMarkInKb, DEFAULT_FREE_SPACE_PROVIDER);
    }

    public HighwaterMarkWatcher(final long highwaterMarkInKb,
            final IFreeSpaceProvider freeSpaceProvider)
    {
        assert freeSpaceProvider != null : "Unspecified IFreeSpaceProvider";
        this.highwaterMarkInKb = highwaterMarkInKb;
        this.freeSpaceProvider = freeSpaceProvider;
        addChangeListener(new NotificationLogChangeListener());
    }

    private final void fireChangeListeners(final HighwaterMarkEvent highwaterMarkEvent)
    {
        final ChangeListener[] listeners = listenerList.getListeners(ChangeListener.class);
        for (final ChangeListener changeListener : listeners)
        {
            changeListener.stateChanged(highwaterMarkEvent);
        }
    }

    public final static String displayKilobyteValue(final long value)
    {
        if (value < 0)
        {
            return UNSPECIFIED;
        }
        return FileUtilities.byteCountToDisplaySize(value * FileUtils.ONE_KB);
    }

    public final static boolean isBelow(final HighwaterMarkState highwaterMarkState)
    {
        assert highwaterMarkState != null : "Unspecified WatermarkState";
        return highwaterMarkState.freeSpace < highwaterMarkState.getHighwaterMark();
    }

    /**
     * Adds a <code>ChangeListener</code> to this high water mark watcher.
     */
    public final synchronized void addChangeListener(final ChangeListener changeListener)
    {
        assert changeListener != null : "Unspecified ChangeListener";
        listenerList.add(ChangeListener.class, changeListener);
    }

    /**
     * Removes given <code>ChangeListener</code> from this high water mark watcher.
     */
    public final synchronized void removeChangeListener(final ChangeListener changeListener)
    {
        assert changeListener != null : "Unspecified ChangeListener";
        listenerList.remove(ChangeListener.class, changeListener);
    }

    /**
     * Whether the free space is below the high water mark or not.
     */
    public final synchronized boolean isBelow()
    {
        return highwaterMarkState == null ? false : isBelow(highwaterMarkState);
    }

    /**
     * Sets the path to get free space for.
     * <p>
     * The path must be set before calling {@link #run()}.
     * </p>
     */
    public final synchronized void setPath(final HostAwareFile path)
    {
        this.path = path;
    }

    /**
     * Returns the path to get free space for.
     */
    public final synchronized HostAwareFile getPath()
    {
        return path;
    }

    /**
     * Sets the path and calls {@link #run()} at the same time.
     */
    public final synchronized void setPathAndRun(final HostAwareFile path)
    {
        setPath(path);
        run();
    }

    /**
     * Analyzes given <var>path</var> and returns a {@link HighwaterMarkState}.
     */
    public final HighwaterMarkState getHighwaterMarkState(final HostAwareFile file)
            throws EnvironmentFailureException
    {
        assert file != null : "Unspecified file";
        final String errorMsg =
                String.format("Could not compute available free space for '%s'.", file);
        final Long freeSpaceInKb =
                new CallableExecutor(5, TimingParameters.DEFAULT_MILLIS_TO_SLEEP_BEFORE_RETRYING)
                        .executeCallable(new Callable<Long>()
                            {

                                //
                                // Callable
                                //

                                @Override
                                public final Long call() throws Exception
                                {
                                    try
                                    {
                                        return freeSpaceProvider.freeSpaceKb(file);
                                    } catch (final IOException ex)
                                    {
                                        operationLog.error(errorMsg, ex);
                                        return null;
                                    }
                                }
                            });
        if (freeSpaceInKb == null)
        {
            throw new EnvironmentFailureException(errorMsg);
        }
        return new HighwaterMarkState(new HostAwareFileWithHighwaterMark(file.tryGetHost(), file
                .getPath(), file.tryGetRsyncModule(), getLocalHighWaterMark(file)),
                freeSpaceInKb);
    }

    /**
     * Returns the high water mark (in <i>kilobytes</i>) specified in the constructor.
     */
    public final long getHighwaterMark()
    {
        return highwaterMarkInKb;
    }

    //
    // Runnable
    //

    @Override
    public final synchronized void run()
    {
        assert path != null : "Unspecified path";
        if (getLocalHighWaterMark(path) < 0)
        {
            return;
        }
        try
        {
            final HighwaterMarkState state = getHighwaterMarkState(path);
            final boolean newBelowValue = isBelow(state);
            final boolean stateChanged = isBelow() != newBelowValue;
            highwaterMarkState = state;
            if (stateChanged)
            {
                fireChangeListeners(new HighwaterMarkEvent(this, state));
            }
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Free space on '%s': %s, highwater mark: %s.",
                        state.hostAwareFileWithHighwaterMark.getPathDescription(),
                        displayKilobyteValue(state.freeSpace),
                        displayKilobyteValue(getLocalHighWaterMark(path))));
            }
        } catch (final EnvironmentFailureException ex)
        {
            operationLog.error("The highwater mark watcher can not work properly "
                    + "due to an environment exception.", ex);
        }
    }

    /**
     * @return the highwater mark from the file if it is aware of it, otherwise the default highwater mark specified in the constructor
     */
    private long getLocalHighWaterMark(HostAwareFile file)
    {
        return (file instanceof HostAwareFileWithHighwaterMark) ? ((HostAwareFileWithHighwaterMark) file)
                .getHighwaterMark() : highwaterMarkInKb;
    }

    //
    // Helper classes
    //

    public final static class HighwaterMarkState implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark;

        private final long freeSpace;

        HighwaterMarkState(final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark,
                final Long freeSpace)
        {
            this.hostAwareFileWithHighwaterMark = hostAwareFileWithHighwaterMark;
            this.freeSpace = freeSpace;
        }

        public final String getPath()
        {
            return hostAwareFileWithHighwaterMark.getPath();
        }

        /** Returns the free space (in <i>kilobytes</i>). */
        public final long getFreeSpace()
        {
            return freeSpace;
        }

        /** Returns the high water mark (in <i>kilobytes</i>). */
        public final long getHighwaterMark()
        {
            return hostAwareFileWithHighwaterMark.getHighwaterMark();
        }

    }

    public final static class HighwaterMarkEvent extends ChangeEvent
    {

        private static final long serialVersionUID = 1L;

        private final HighwaterMarkState highwaterMarkState;

        HighwaterMarkEvent(final Object source, final HighwaterMarkState highwaterMarkState)
        {
            super(source);
            this.highwaterMarkState = highwaterMarkState;
        }

        /**
         * Whether the free space is below the high water mark.
         */
        public final boolean isBelow()
        {
            return HighwaterMarkWatcher.isBelow(highwaterMarkState);
        }

        /** Returns the canonical path. */
        public final String getPath()
        {
            return highwaterMarkState.hostAwareFileWithHighwaterMark.getPath();
        }

        public final long getFreeSpace()
        {
            return highwaterMarkState.freeSpace;
        }

        public final long getHighwaterMark()
        {
            return highwaterMarkState.hostAwareFileWithHighwaterMark.getHighwaterMark();
        }
    }

    /**
     * A <code>ChangeListener</code> implementation which informs the administrator when free space becomes tight or when free space is again "green".
     * 
     * @author Christian Ribeaud
     */
    final static class NotificationLogChangeListener implements ChangeListener
    {
        static final String INFO_LOG_FORMAT =
                "Low space condition resolved on '%s', required: %s, found: %s, resuming operation.";

        static final String WARNING_LOG_FORMAT =
                "Highwater mark reached on '%s', required: %s, found: %s, missing: %s, suspending operation.";

        private static final Logger notificationLog =
                LogFactory.getLogger(LogCategory.NOTIFY, NotificationLogChangeListener.class);

        NotificationLogChangeListener()
        {
        }

        //
        // ChangeListener
        //

        @Override
        public final void stateChanged(final ChangeEvent e)
        {
            final HighwaterMarkEvent event = (HighwaterMarkEvent) e;
            final String path = event.getPath();
            final String highwaterMarkDisplayed = displayKilobyteValue(event.getHighwaterMark());
            final String freeSpaceDisplayed = displayKilobyteValue(event.getFreeSpace());
            if (event.isBelow())
            {
                final String missingSpace =
                        displayKilobyteValue(event.getHighwaterMark() - event.getFreeSpace());
                notificationLog.warn(String.format(WARNING_LOG_FORMAT, path,
                        highwaterMarkDisplayed, freeSpaceDisplayed, missingSpace));
            } else
            {
                notificationLog.info(String.format(INFO_LOG_FORMAT, path, highwaterMarkDisplayed,
                        freeSpaceDisplayed));
            }
        }
    }

}