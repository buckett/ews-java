/**************************************************************************
 * copyright file="PropertyDefinitionFlags.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the PropertyDefinitionFlags.java.
 **************************************************************************/
package microsoft.exchange.webservices.data;

/***
 * Defines how a complex property behaves.
 * 
 * 
 */
enum PropertyDefinitionFlags {
	/**
	 * No specific behavior.
	 */
	None,

	/**
	 * The property is automatically instantiated when it is read.
	 */
	AutoInstantiateOnRead,

	/**
	 * The existing instance of the property is reusable.
	 */
	ReuseInstance,

	/**
	 * The property can be set.
	 */
	CanSet,

	/**
	 * The property can be updated.
	 */
	CanUpdate,

	/**
	 * The property can be deleted.
	 */
	CanDelete,

	/**
	 * The property can be searched.
	 */
	CanFind,

	/** The property must be loaded explicitly. */
	MustBeExplicitlyLoaded
}
