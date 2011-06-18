/**************************************************************************
 * copyright file="ExchangeCredentials.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the ExchangeCredentials.java.
 **************************************************************************/
package microsoft.exchange.webservices.data;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Base class of Exchange credential types.
 * 
 */
public abstract class ExchangeCredentials {

	/***
	 * Performs an implicit conversion from <see
	 * cref="System.Net.NetworkCredential"/> to <see
	 * cref="Microsoft.Exchange.WebServices.Data.ExchangeCredentials"/>. This
	 * allows a NetworkCredential object to be implictly converted to an
	 * ExchangeCredential which is useful when setting credentials on an
	 * ExchangeService.
	 * 
	 * @param userName
	 *            Account user name.
	 * @param password
	 *            Account password.
	 * @param domain
	 *            Account domain.
	 * @return The result of the conversion.
	 */
	public static ExchangeCredentials 
			getExchangeCredentialsFromNetworkCredential(
			String userName, String password, String domain) {
		return new WebCredentials(userName, password, domain);
	}

	/***
	 * This method is called to pre-authenticate credentials before a service
	 * request is made.
	 */
	protected void preAuthenticate() {
		// do nothing by default.
	}

	/**
	 * * This method is called to apply credentials to a service request before
	 * the request is made.
	 * 
	 * @param client
	 *            The request.
	 * @throws URISyntaxException
	 *             the uRI syntax exception
	 */
	protected void prepareWebRequest(HttpWebRequest client)
			throws URISyntaxException {
		// do nothing by default.
	}

	/**
	 * * Emit any extra necessary namespace aliases for the SOAP:header block.
	 * 
	 * @param writer
	 *            The writer.
	 * @throws XMLStreamException
	 *             the xML stream exception
	 */
	protected void emitExtraSoapHeaderNamespaceAliases(XMLStreamWriter writer)
			throws XMLStreamException {
		// do nothing by default.
	}

	/**
	 * * Serialize any extra necessary SOAP headers. This is used for
	 * authentication schemes that rely on WS-Security, or for endpoints
	 * requiring WS-Addressing.
	 * 
	 * @param writer
	 *            The writer.
	 * @param webMethodName
	 *            The Web method being called.
	 * @throws XMLStreamException
	 *             the xML stream exception
	 */
	protected void serializeExtraSoapHeaders(XMLStreamWriter writer,
			String webMethodName) throws XMLStreamException {
		// do nothing by default.
	}

	/**
	 * * Serialize SOAP headers used for authentication schemes that rely on
	 * WS-Security.
	 * 
	 * @param writer
	 *            The writer.
	 * @throws XMLStreamException
	 *             the xML stream exception
	 */
	protected void serializeWSSecurityHeaders(XMLStreamWriter writer)
			throws XMLStreamException {
		// do nothing by default.
	}

	/**
	 * * Adjusts the URL endpoint based on the credentials.
	 * 
	 * @param url
	 *            The URL.
	 * @return Adjust URL.
	 * @throws URISyntaxException
	 *             the uRI syntax exception
	 */
	protected URI adjustUrl(URI url) throws URISyntaxException {
		return url;
	}
}
