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

package ch.systemsx.cisd.common.utilities;

import java.io.File;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A watermark watcher.
 * <p>
 * This class is thread-safe.
 * </p>
 * 
 * @see FileSystemUtils
 * @author Christian Ribeaud
 */
public final class WatermarkWatcher implements Runnable
{

    private final static IFreeSpaceProvider DEFAULT_FREE_SPACE_PROVIDER =
            new DefaultFreeSpaceProvider();

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, WatermarkWatcher.class);

    /**
     * The watermark value in <i>kilobytes</i>.
     */
    private final long watermarkInKb;

    private final EventListenerList listenerList = new EventListenerList();

    /** The path to get free space for. Is not <code>null</code>, not empty on Unix. */
    private File path;

    /** The last {@link WatermarkState} computed. */
    private WatermarkState watermarkState;

    private final IFreeSpaceProvider freeSpaceProvider;

    /**
     * @param watermark the watermark value in kilobytes. If negative, then {@link #run()} always
     *            returns without doing anything.
     */
    public WatermarkWatcher(final long watermark)
    {
        this(watermark, DEFAULT_FREE_SPACE_PROVIDER);
    }

    WatermarkWatcher(final long watermarkInKb, final IFreeSpaceProvider freeSpaceProvider)
    {
        assert freeSpaceProvider != null : "Unspecified IFreeSpaceProvider";
        this.watermarkInKb = watermarkInKb;
        this.freeSpaceProvider = freeSpaceProvider;
        addChangeListener(new NotificationLogChangeListener());
    }

    private final void fireChangeListeners(final WatermarkEvent watermarkEvent)
    {
        final ChangeListener[] listeners = listenerList.getListeners(ChangeListener.class);
        for (final ChangeListener changeListener : listeners)
        {
            changeListener.stateChanged(watermarkEvent);
        }
    }

    public final static String displayKilobyteValue(final long value)
    {
        return FileUtils.byteCountToDisplaySize(value * FileUtils.ONE_KB);
    }

    public final static boolean isBelow(final WatermarkState watermarkState)
    {
        assert watermarkState != null : "Unspecified WatermarkState";
        return watermarkState.freeSpace < watermarkState.watermark;
    }

    /**
     * Adds a <code>ChangeListener</code> to this watermark watcher.
     */
    public final synchronized void addChangeListener(final ChangeListener changeListener)
    {
        assert changeListener != null : "Unspecified ChangeListener";
        listenerList.add(ChangeListener.class, changeListener);
    }

    /**
     * Removes given <code>ChangeListener</code> from this watermark watcher.
     */
    public final synchronized void removeChangeListener(final ChangeListener changeListener)
    {
        assert changeListener != null : "Unspecified ChangeListener";
        listenerList.remove(ChangeListener.class, changeListener);
    }

    /**
     * Whether the free space is below the watermark or not.
     */
    public final synchronized boolean isBelow()
    {
        return watermarkState == null ? false : isBelow(watermarkState);
    }

    /**
     * Sets the path to get free space for.
     * <p>
     * The path must be set before calling {@link #run()}.
     * </p>
     */
    public final synchronized void setPath(final File path)
    {
        this.path = path;
    }

    /**
     * Sets the path and calls {@link #run()} at the same time.
     */
    public final synchronized void setPathAndRun(final File path)
    {
        setPath(path);
        run();
    }

    /**
     * Analyzes given <var>path</var> and returns a {@link WatermarkState}.
     */
    public final WatermarkState getWatermarkState(final File file) throws IOException
    {
        assert file != null : "Unspecified file";
        final long freeSpaceInKb = freeSpaceProvider.freeSpaceKb(file);
        return new WatermarkState(file, watermarkInKb, freeSpaceInKb);
    }

    /**
     * Returns the watermark (in <i>kilobytes</i>) specified in the constructor.
     */
    public final long getWatermark()
    {
        return watermarkInKb;
    }

    //
    // Runnable
    //

    public final synchronized void run()
    {
        assert path != null : "Unspecified path";
        if (watermarkInKb < 0)
        {
            return;
        }
        try
        {
            final WatermarkState state = getWatermarkState(path);
            final boolean newBelowValue = isBelow(state);
            final boolean stateChanged = isBelow() != newBelowValue;
            watermarkState = state;
            if (stateChanged)
            {
                fireChangeListeners(new WatermarkEvent(this, state));
            }
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Amount of available space on '%s' is: %s.",
                        FileUtilities.getCanonicalPath(state.path),
                        displayKilobyteValue(state.freeSpace)));
            }
        } catch (final IOException ex)
        {
            operationLog.error(
                    "The watermark watcher can not work properly due to an I/O exception.", ex);
        }
    }

    //
    // Helper classes
    //

    private final static class DefaultFreeSpaceProvider implements IFreeSpaceProvider
    {

        //
        // IFreeSpaceProvider
        //

        public final long freeSpaceKb(final File path) throws IOException
        {
            final String canonicalPath = FileUtilities.getCanonicalPath(path);
            return FileSystemUtils.freeSpaceKb(canonicalPath);
        }
    }

    public final static class WatermarkState
    {
        private final File path;

        private final long freeSpace;

        private final long watermark;

        WatermarkState(final File path, final long watermark, final long freeSpace)
        {
            this.path = path;
            this.watermark = watermark;
            this.freeSpace = freeSpace;
        }

        public final File getPath()
        {
            return path;
        }

        /** Returns the free space (in <i>kilobytes</i>). */
        public final long getFreeSpace()
        {
            return freeSpace;
        }

        /** Returns the watermark (in <i>kilobytes</i>). */
        public final long getWatermark()
        {
            return watermark;
        }

    }

    public final static class WatermarkEvent extends ChangeEvent
    {

        private static final long serialVersionUID = 1L;

        private final WatermarkState watermarkState;

        WatermarkEvent(final Object source, final WatermarkState watermarkState)
        {
            super(source);
            this.watermarkState = watermarkState;
        }

        /**
         * Whether the free space is below or reaches the watermark.
         */
        public final boolean isBelow()
        {
            return WatermarkWatcher.isBelow(watermarkState);
        }

        /** Returns the canonical path. */
        public final String getPath()
        {
            return FileUtilities.getCanonicalPath(watermarkState.path);
        }

        public final long getFreeSpace()
        {
            return watermarkState.freeSpace;
        }

        public final long getWatermark()
        {
            return watermarkState.watermark;
        }
    }

    /**
     * A <code>ChangeListener</code> implementation which informs the administrator when free
     * space becomes tight or when free space is again "green".
     * 
     * @author Christian Ribeaud
     */
    final static class NotificationLogChangeListener implements ChangeListener
    {
        static final String INFO_LOG_FORMAT =
                "The amount of available space (%s) on '%s' "
                        + "is again sufficient (greater than the specified watermark: %s).";

        static final String WARNING_LOG_FORMAT =
                "The amount of available space (%s) on '%s' "
                        + "is lower than the specified watermark (%s).";

        private static final Logger notificationLog =
                LogFactory.getLogger(LogCategory.NOTIFY, NotificationLogChangeListener.class);

        NotificationLogChangeListener()
        {
        }

        //
        // ChangeListener
        //

        public final void stateChanged(final ChangeEvent e)
        {
            final WatermarkEvent event = (WatermarkEvent) e;
            final String path = event.getPath();
            final String watermarkDisplayed = displayKilobyteValue(event.getWatermark());
            final String freeSpaceDisplayed = displayKilobyteValue(event.getFreeSpace());
            if (event.isBelow())
            {
                notificationLog.warn(String.format(WARNING_LOG_FORMAT, freeSpaceDisplayed, path,
                        watermarkDisplayed));
            } else
            {
                notificationLog.info(String.format(INFO_LOG_FORMAT, freeSpaceDisplayed, path,
                        watermarkDisplayed));
            }
        }
    }

    /**
     * Each implementation is able to return the free space on a drive or volume.
     * 
     * @author Christian Ribeaud
     */
    static interface IFreeSpaceProvider
    {

        /**
         * Returns the free space on a drive or volume in kilobytes by invoking the command line.
         */
        long freeSpaceKb(final File path) throws IOException;
    }
}