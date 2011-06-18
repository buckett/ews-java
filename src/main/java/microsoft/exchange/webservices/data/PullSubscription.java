/**************************************************************************
 * copyright file="PullSubscription.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the PullSubscription.java.
 **************************************************************************/
package microsoft.exchange.webservices.data;

/**
 * Represents a pull subscription.
 * 
 * 
 */
public final class PullSubscription extends SubscriptionBase {

	/** The more events available. */
	private boolean moreEventsAvailable;

	/**
	 * Initializes a new instance.
	 * 
	 * @param service
	 *            the service
	 * @throws Exception
	 *             the exception
	 */
	protected PullSubscription(ExchangeService service) throws Exception {
		super(service);
	}

	/**
	 * Obtains a collection of events that occurred on the subscribed folders
	 * since the point in time defined by the Watermark property. When GetEvents
	 * succeeds, Watermark is updated.
	 * 
	 * @return Returns a collection of events that occurred since the last
	 *         watermark
	 * @throws Exception
	 *             the exception
	 */
	public GetEventsResults getEvents() throws Exception {
		GetEventsResults results = getService().getEvents(this.getId(),
				this.getWaterMark());
		this.setWaterMark(results.getNewWatermark());
		this.moreEventsAvailable = results.isMoreEventsAvailable();
		return results;
	}

	/**
	 * Unsubscribes from the pull subscription.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void unsubscribe() throws Exception {
		getService().unsubscribe(getId());
	}

	/**
	 * Gets a value indicating whether more events are available on the server.
	 * MoreEventsAvailable is undefined (null) until GetEvents is called.
	 * 
	 * @return true, if is more events available
	 */
	public boolean isMoreEventsAvailable() {
		return moreEventsAvailable;
	}

}
