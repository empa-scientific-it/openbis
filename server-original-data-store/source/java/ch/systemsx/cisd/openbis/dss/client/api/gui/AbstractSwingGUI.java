/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.text.WordUtils;
import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DssCommunicationState;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractSwingGUI
{
    private static final int MESSAGE_WRAP_MAX_CHAR = 100;

    /**
     * The interface for communicating with DSS
     */
    protected final IOpenbisServiceFacade openBISService;

    protected final Thread shutdownHook;

    private final JFrame windowFrame;

    private final boolean logoutOnClose;

    private static final long KEEP_ALIVE_PERIOD_MILLIS = 60 * 1000; // Every minute.

    /**
     * Instantiates the Swing GUI with the necessary information to communicate with CIFEX.
     * 
     * @param communicationState
     */
    protected AbstractSwingGUI(DssCommunicationState communicationState)
    {
        openBISService = communicationState.getOpenBISService();

        // create the window frame
        windowFrame = new JFrame(getTitle());
        windowFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // add callbacks to close the app properly
        shutdownHook = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if (logoutOnClose)
                        {
                            openBISService.logout();
                        }
                    } catch (InvalidSessionException ex)
                    {
                        // Silence this exception.
                    }
                }
            };
        addShutdownHook();
        startSessionKeepAliveTimer(KEEP_ALIVE_PERIOD_MILLIS);
        addWindowCloseHook();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
            {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable)
                {
                    final String message =
                            throwable.getClass().getSimpleName() + "[Thread: " + thread.getName()
                                    + "]: " + throwable.getMessage();
                    notifyUserOfThrowable(windowFrame, message, "Unexpected Error", throwable);
                }
            });

        logoutOnClose = communicationState.isLogoutOnClose();
    }

    public IOpenbisServiceFacade getOpenBISService()
    {
        return openBISService;
    }

    /**
     * The main window
     */
    protected JFrame getWindowFrame()
    {
        return windowFrame;
    }

    /**
     * Checks if it is safe to quit, if not, asks the user before doing so.
     */
    protected void logout()
    {
        if (cancel())
        {
            if (logoutOnClose)
            {
                openBISService.logout();
            }
            System.exit(0);
        }
    }

    private void startSessionKeepAliveTimer(final long checkTimeIntervalMillis)
    {
        final Timer timer = new Timer("Session Keep Alive", true);
        timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    try
                    {
                        openBISService.checkSession();
                    } catch (RemoteAccessException ex)
                    {
                        System.err.println("Error connecting to the server");
                        ex.printStackTrace();
                    } catch (InvalidSessionException ex)
                    {
                        JOptionPane.showMessageDialog(windowFrame,
                                "Your session has expired on the server. Please log in again",
                                "Error connecting to server", JOptionPane.ERROR_MESSAGE);
                        Runtime.getRuntime().removeShutdownHook(shutdownHook);
                        System.exit(1);
                    }
                }
            }, 0L, checkTimeIntervalMillis);
    }

    /**
     * Log the user out automatically if the window is closed.
     */
    private void addWindowCloseHook()
    {
        windowFrame.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    logout();
                }
            });
    }

    /**
     * Log the user out automatically if the app is shutdown.
     */
    private void addShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    protected abstract String getTitle();

    protected abstract boolean cancel();

    /**
     * Notifies the user of the given <var>throwable</var>, if the error message is different from <var>lastExceptionMessageOrNull</var>.
     */
    public static String notifyUserOfThrowable(final Frame parentFrame, final String fileName,
            final String operationName, final Throwable throwable,
            final String lastExceptionMessageOrNull)
    {
        final Throwable th =
                (throwable instanceof Error) ? throwable : CheckedExceptionTunnel
                        .unwrapIfNecessary((Exception) throwable);
        final String message;
        if (th instanceof HighLevelException)
        {
            message = th.getMessage();
        } else
        {
            message =
                    operationName + " file '" + fileName + "' failed:\n"
                            + th.getClass().getSimpleName() + ": " + th.getMessage();
        }
        final String title = "Error " + operationName + " File";
        if (message.equals(lastExceptionMessageOrNull) == false)
        {
            notifyUserOfThrowable(parentFrame, message, title, throwable);
        }
        return message;
    }

    /**
     * Notifies the user of the given <var>throwable</var>, if the error message is different from <var>lastExceptionMessageOrNull</var>.
     */
    static void notifyUserOfThrowable(final Frame parentFrame, final String message,
            final String title, final Throwable throwable)
    {
        final Throwable th =
                (throwable instanceof Error) ? throwable : CheckedExceptionTunnel
                        .unwrapIfNecessary((Exception) throwable);
        if (throwable instanceof ClassCastException)
        {
            System.err.println("Encountered ClassCastException problem.");
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        UiUtilities.showMessageAndException(parentFrame, throwable, 
                                WordUtils.wrap(message, MESSAGE_WRAP_MAX_CHAR), title);
                    }
                });
        }
        th.printStackTrace();
    }

    protected static void setLookAndFeelToNative()
    {
        // Set the look and feel to the native system look and feel, if possible
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex)
        {
            // just ignore -- no big deal
        }
    }

    protected static void setLookAndFeelToMetal()
    {
        // Set the look and feel to the native system look and feel, if possible
        try
        {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex)
        {
            // just ignore -- no big deal
        }
    }

}