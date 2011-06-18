/**************************************************************************
 * copyright file="StreamingSubscription.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the StreamingSubscription class.
 **************************************************************************/
package microsoft.exchange.webservices.data;

/**
 * Represents a streaming subscription.
 */
public final class StreamingSubscription extends SubscriptionBase{

	/**
	 * Initializes a new instance of the 
	 * <see cref="StreamingSubscription"/> class.
	 * @param service The service.
	 */
	private ExchangeService service;
	protected StreamingSubscription(ExchangeService service)
	throws Exception {  
		super(service);

	}

	/**
	 * Unsubscribes from the streaming subscription.
	 */
	public void unsubscribe() throws Exception {
		this.getService().unsubscribe(this.getId());
	}

	/**
	 * Gets the service used to create this subscription.
	 */
	public  ExchangeService getService() {
		return super.getService();
	}


	/**
	 * Gets a value indicating whether this subscription uses watermarks.
	 */
	@Override
	protected  boolean getUsesWatermark() {
		return false;
	}

}

