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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DssComponentFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractSwingGUI
{
    private static final int MESSAGE_WRAP_MAX_CHAR = 100;

    /**
     * The interface for communicating with DSS
     */
    protected final IDssComponent dssComponent;

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
        dssComponent = communicationState.getDssComponent();

        // create the window frame
        windowFrame = new JFrame(getTitle());
        windowFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

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
                            dssComponent.logout();
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

    public String getSessionId()
    {
        return dssComponent.getSessionToken();
    }

    public IDssComponent getDssComponent()
    {
        return dssComponent;
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
                dssComponent.logout();
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
                        dssComponent.checkSession();
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
     * Notifies the user of the given <var>throwable</var>, if the error message is different from
     * <var>lastExceptionMessageOrNull</var>.
     */
    static String notifyUserOfThrowable(final Frame parentFrame, final String fileName,
            final String operationName, final Throwable throwable,
            final String lastExceptionMessageOrNull)
    {
        final Throwable th =
                (throwable instanceof Error) ? throwable : CheckedExceptionTunnel
                        .unwrapIfNecessary((Exception) throwable);
        final String message;
        if (th instanceof UserFailureException)
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
     * Notifies the user of the given <var>throwable</var>, if the error message is different from
     * <var>lastExceptionMessageOrNull</var>.
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
                    public void run()
                    {
                        JOptionPane.showMessageDialog(parentFrame,
                                WordUtils.wrap(message, MESSAGE_WRAP_MAX_CHAR), title,
                                JOptionPane.ERROR_MESSAGE);
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

    // -------- errors reporting -----------------

    protected static void showErrorsAndWarningsIfAny(JFrame frame, String firstMessageOrNull,
            List<String> warningMessages, List<Throwable> exceptions)
    {
        String message = (firstMessageOrNull == null ? "" : firstMessageOrNull + "\n");
        message += joinMessages(warningMessages, exceptions);
        if (exceptions.size() > 0)
        {
            showErrorMessage(frame, message);
        } else if (warningMessages.size() > 0)
        {
            showWarningMessage(frame, message);
        }
    }

    private static void showErrorMessage(JFrame frame, String message)
    {
        showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void showWarningMessage(JFrame frame, String message)
    {
        showMessageDialog(frame, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private static void showMessageDialog(JFrame frame, String message, String title,
            int messageType)
    {
        JOptionPane.showMessageDialog(frame, message, title, messageType);
    }

    private static String joinMessages(List<String> warningMessages, List<Throwable> exceptions)
    {
        StringBuffer sb = new StringBuffer();
        addErrorMessages(exceptions, sb);
        addWarningMessages(warningMessages, sb);
        return sb.toString();
    }

    private static void addErrorMessages(List<Throwable> exceptions, StringBuffer sb)
    {
        if (exceptions.size() > 0)
        {
            if (exceptions.size() > 1)
            {
                sb.append("Following errors occured: \n");
            }
            for (Throwable exception : exceptions)
            {
                sb.append(getErrorMessage(exception));
                sb.append("\n");
            }
        }
    }

    private static void addWarningMessages(List<String> warningMessages, StringBuffer sb)
    {
        if (warningMessages.size() > 0)
        {
            sb.append("Following warnings occured (you can most probably ignore them): \n");
            String lastWarningMessage = "";
            for (String warningMessage : warningMessages)
            {
                if (lastWarningMessage.equals(warningMessage) == false)
                {
                    sb.append(warningMessage);
                    sb.append("\n");
                    lastWarningMessage = warningMessage;
                }
            }
        }
    }

    private static String getErrorMessage(Throwable throwable)
    {
        final String message;
        if (throwable instanceof UserFailureException)
        {
            message = throwable.getMessage();
        } else
        {
            message = "ERROR: " + throwable;
        }
        return message;
    }
}

class DssCommunicationState
{
    private final IDssComponent dssComponent;

    private final IGeneralInformationService generalInformationService;

    private final IGeneralInformationChangingService generalInformationChangingService;

    private final boolean logoutOnClose;

    private static final long CONNECTION_TIMEOUT_MILLIS = 15 * DateUtils.MILLIS_PER_SECOND;

    private static IGeneralInformationService createGeneralInformationService(String openBISURL)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        IGeneralInformationService service =
                generalInformationServiceFinder.createService(IGeneralInformationService.class,
                        openBISURL);
        return service;
    }

    private static IGeneralInformationChangingService createGeneralInformationChangingService(
            String openBISURL)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationChangingService.SERVICE_URL);
        IGeneralInformationChangingService service =
                generalInformationServiceFinder.createService(
                        IGeneralInformationChangingService.class, openBISURL);
        return service;

    }

    /**
     * Create a new instance of the DssCommunicationState based info in the arguments. Throws an
     * exception if it could not be created.
     */
    protected DssCommunicationState(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        if (args.length < 2)
            throw new ConfigurationFailureException(
                    "The openBIS File Upload Client was improperly configured -- the arguments it requires were not supplied. Please talk to the openBIS administrator.");

        String openBisUrl = args[0];

        switch (args.length)
        {
            case 2:
                String sessionToken = args[1];
                dssComponent =
                        DssComponentFactory.tryCreate(sessionToken, openBisUrl,
                                CONNECTION_TIMEOUT_MILLIS);
                if (null == dssComponent)
                {
                    throw new ConfigurationFailureException(
                            "The openBIS File Upload Client was improperly configured -- the session token is not valid. Please talk to the openBIS administrator.");
                }
                // Don't logout -- the user wants to keep his/her session token alive.
                logoutOnClose = false;
                break;
            default:
                String userName = args[1];
                String passwd = args[2];
                dssComponent =
                        DssComponentFactory.tryCreate(userName, passwd, openBisUrl,
                                CONNECTION_TIMEOUT_MILLIS);
                if (null == dssComponent)
                {
                    throw new ConfigurationFailureException(
                            "The user name / password combination is incorrect.");
                }
                // Do logout on close
                logoutOnClose = true;
        }

        generalInformationService = createGeneralInformationService(openBisUrl);
        generalInformationChangingService = createGeneralInformationChangingService(openBisUrl);
    }

    IDssComponent getDssComponent()
    {
        return dssComponent;
    }

    public IGeneralInformationService getGeneralInformationService()
    {
        return generalInformationService;
    }

    public IGeneralInformationChangingService getGeneralInformationChangingService()
    {
        return generalInformationChangingService;
    }

    public boolean isLogoutOnClose()
    {
        return logoutOnClose;
    }
}