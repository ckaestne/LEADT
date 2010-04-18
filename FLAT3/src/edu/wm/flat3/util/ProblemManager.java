/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3.util;

import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.wm.flat3.FLATTT;

/**
 * A general utility class to report exceptions to the UI.
 */
public final class ProblemManager
{
	private static final int MAX_ERRORS_TO_REPORT_FOR_SAME_ERROR = 3;
	private static final int MAX_ERRORS_TO_REPORT = 20;
	private static int totalErrorsReported = 0;

	private static HashMap<String, Integer> msgCount = new HashMap<String, Integer>();

	private ProblemManager()
	{}

	/**
	 * Reports an exception in a dialog and logs the exception. The exception
	 * dialog contains the stack trace details.
	 * 
	 * @param e
	 *            The exception to report.
	 */
	public static void reportException(final Exception e, final boolean limitPopups)
	{
		reportException(e, e.getMessage(), limitPopups);
	}

	public static void reportException(final Exception e)
	{
		reportException(e, e.getMessage(), false);
	}
	
	/**
	 * Reports an exception in a dialog and logs the exception. The exception
	 * dialog contains the stack trace details.
	 * 
	 * @param e
	 *            The exception to report.
	 */
	public static void reportException(final Exception e, 
	                                   final String msg, 
	                                   final boolean limitPopups)
	{
		StackTraceElement[] stackElements = e.getStackTrace();
		IStatus[] statuses = new IStatus[stackElements.length + 1];
		statuses[0] = new Status(IStatus.ERROR, FLATTT.ID_PLUGIN,
				IStatus.OK, msg, e);

		for (int i = 0; i < stackElements.length; i++)
		{
			statuses[i + 1] = new Status(IStatus.ERROR,
					FLATTT.ID_PLUGIN, IStatus.OK, "     "
							+ stackElements[i].toString(), null);
		}

		String title = "Error: " + e.getClass().getSimpleName();
		assert title != null;

		MultiStatus multiStatus = new MultiStatus(FLATTT.ID_PLUGIN,
				IStatus.OK, statuses, msg, e);

		doMessage(title, msg, multiStatus, limitPopups);
	}

	public static void reportException(final Exception e, final String msg)
	{
		reportException(e, msg, false);
	}
	
	public static void reportInfo(final String msg, final String details)
	{
		// Info messages never cause popups so just pass false
		reportMsg(null, msg, details, IStatus.INFO, false);
	}
	
	public static void reportError(final String title, 
	                               final String msg, 
	                               final String details, 
	                               final boolean limitPopups)
	{
		reportMsg(title, msg, details, IStatus.ERROR, limitPopups);
	}

	public static void reportError(final String title, 
	                               final String msg, 
	                               final String details)
	{
		reportMsg(title, msg, details, IStatus.ERROR, false);
	}
	
	public static void reportMsg(final String title, 
	                             final String msg, 
	                             final String details, 
	                             final int severity, 
	                             final boolean limitPopups)
	{
		IStatus status;
		
		if (details != null)
		{
			IStatus[] statuses = new IStatus[] {
				new Status(severity, FLATTT.ID_PLUGIN, details)
			};

			status = new MultiStatus(FLATTT.ID_PLUGIN,
					IStatus.OK, statuses, msg, null);
		}
		else
		{
			status = new Status(severity, FLATTT.ID_PLUGIN, msg);
		}

		doMessage(title, msg, status, limitPopups);
	}

	private static void doMessage(String title, 
	                              final String msg, 
	                              final IStatus status,
	                              final boolean limitPopups)
	{
		// Always log the error
		FLATTT.singleton().getLog().log(status);

		if (title == null)
			title = "Error";
		
		Integer reportedErrors = msgCount.get(title);

		if (reportedErrors == null)
			reportedErrors = new Integer(0);

		Shell shell = null;
		if (PlatformUI.getWorkbench() != null &&
			PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null)
		{
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		}
		
		// But don't show too many error dialogs
		if (limitPopups &&
			status.matches(IStatus.ERROR) &&
			reportedErrors.intValue() < MAX_ERRORS_TO_REPORT_FOR_SAME_ERROR &&
			totalErrorsReported < MAX_ERRORS_TO_REPORT &&
			shell != null)
		{
			ErrorDialog.openError(shell, title, msg, status);
		}

		msgCount.put(title, new Integer(reportedErrors.intValue() + 1));
		++totalErrorsReported;
	}
}
