package edu.wm.flat3.model;

import java.util.ArrayList;
import java.util.List;

public class ConcernListenerManager
{
	private int areNotificationsDisabled = 0;
	private List<IConcernListener> listeners = new ArrayList<IConcernListener>();

	ConcernEvent queuedEvents = null;
	
	public void disableNotifications()
	{
		++areNotificationsDisabled;
	}

	public void clearQueuedEvents()
	{
		queuedEvents = null;
	}
	
	/**
	 */
	public void enableNotifications()
	{
		assert areNotificationsDisabled > 0;
		
		--areNotificationsDisabled;
		
		if (areNotificationsDisabled == 0 && queuedEvents != null)
		{
			modelChanged(queuedEvents);
			clearQueuedEvents();
		}
	}
	
	/**
	 * Notifies all observers of a change in the model.
	 * 
	 * @param pChange
	 *            The type of change. See the constants in
	 *            ConcernModelChangeListener.
	 */
	public void modelChanged(ConcernEvent event)
	{
		if (areNotificationsDisabled > 0)
		{
			if (queuedEvents == null)
				queuedEvents = new ConcernEvent();
			
			queuedEvents.addEvent(event);
			return;
		}
	
		// Must copy array because modelChanged() may caused
		// add/removeListener to be invoked re-entrantly
		for (IConcernListener lListener : 
			listeners.toArray(new IConcernListener[] {} ))
		{
			lListener.modelChanged(event);
		}
	}

	/**
	 * Adds a listener to the list.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addListener(IConcernListener listener)
	{
		if (!listeners.contains(listener))
		{
			listeners.add(listener);
		}
	}

	/**
	 * Removes a Listener from the list.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeListener(IConcernListener listener)
	{
		listeners.remove(listener);
	}
}
