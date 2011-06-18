/**************************************************************************
 * copyright file="GetUserSettingsRequest.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the GetUserSettingsRequest.java.
 **************************************************************************/
package microsoft.exchange.webservices.data;

import java.net.URI;
import java.util.List;

import javax.xml.stream.XMLStreamException;

/**
 *Represents a GetUserSettings request.
 * 
 */
class GetUserSettingsRequest extends AutodiscoverRequest {

	/**
	 * Action Uri of Autodiscover.GetUserSettings method.
	 */
	// / </summary>
	private static final String GetUserSettingsActionUri = EwsUtilities.
			AutodiscoverSoapNamespace +
			 "/Autodiscover/GetUserSettings";
	private List<String> smtpAddresses;
	private List<UserSettingName> settings;

	/**
	 * Initializes a new instance of the <see cref="GetUserSettingsRequest"/>
	 * class.
	 * 
	 * @param service
	 *            the service
	 * @param url
	 *            the url
	 */
	protected GetUserSettingsRequest(AutodiscoverService service, URI url) {
		super(service, url);
	}

	/**
	 * Validates the request.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	protected void validate() throws Exception {
		super.validate();

		EwsUtilities.validateParam(this.getSmtpAddresses(), "smtpAddresses");
		EwsUtilities.validateParam(this.getSettings(), "settings");

		if (this.getSettings().size() == 0) {
			throw new ServiceValidationException(
					Strings.InvalidAutodiscoverSettingsCount);
		}

		if (this.getSmtpAddresses().size() == 0) {
			throw new ServiceValidationException(
					Strings.InvalidAutodiscoverSmtpAddressesCount);
		}

		for (String smtpAddress : this.getSmtpAddresses()) {
			if (smtpAddress == null || smtpAddress.isEmpty()) {
				throw new ServiceValidationException(
						Strings.InvalidAutodiscoverSmtpAddress);
			}
		}
	}

	/**
	 * Executes this instance.
	 * 
	 * @return the gets the user settings response collection
	 * @throws ServiceLocalException
	 *             the service local exception
	 * @throws Exception
	 *             the exception
	 */
	protected GetUserSettingsResponseCollection execute()
			throws ServiceLocalException, Exception {
		GetUserSettingsResponseCollection responses =
			(GetUserSettingsResponseCollection) this
				.internalExecute();
		if (responses.getErrorCode() == AutodiscoverErrorCode.NoError) {
			this.postProcessResponses(responses);
		}
		return responses;
	}

	/**
	 * Post-process responses to GetUserSettings.
	 * 
	 * @param responses
	 *            The GetUserSettings responses.
	 */
	private void postProcessResponses(
			GetUserSettingsResponseCollection responses) {
		// Note:The response collection may not include all of the requested
		// users if the request has been throttled.
		for (int index = 0; index < responses.getCount(); index++) {
			responses.getResponses().get(index).setSmtpAddress(
					this.getSmtpAddresses().get(index));
		}
	}

	/**
	 * Gets the name of the request XML element.
	 * 
	 * @return Request XML element name.
	 */
	@Override
	protected String getRequestXmlElementName() {
		return XmlElementNames.GetUserSettingsRequestMessage;
	}

	/**
	 * Gets the name of the response XML element.
	 * 
	 * @return Response XML element name.
	 */
	@Override
	protected String getResponseXmlElementName() {
		return XmlElementNames.GetUserSettingsResponseMessage;
	}

	/**
	 * Gets the WS-Addressing action name.
	 * 
	 * @return WS-Addressing action name.
	 * 
	 */
	@Override
	protected String getWsAddressingActionName() {
		return GetUserSettingsActionUri;
	}

	/**
	 * Creates the service response.
	 * 
	 * @return AutodiscoverResponse
	 */
	@Override
	protected AutodiscoverResponse createServiceResponse() {
		return new GetUserSettingsResponseCollection();
	}

	/**
	 * Writes the attributes to XML.
	 * 
	 * @param writer
	 *            The writer.
	 * @throws ServiceXmlSerializationException
	 *             the service xml serialization exception
	 */
	@Override
	protected void writeAttributesToXml(EwsServiceXmlWriter writer)
			throws ServiceXmlSerializationException {
		writer.writeAttributeValue("xmlns",
				EwsUtilities.AutodiscoverSoapNamespacePrefix,
				EwsUtilities.AutodiscoverSoapNamespace);
	}

	/**
	 * Writes request to XML.
	 * 
	 * @param writer
	 *            The writer.
	 * @throws XMLStreamException
	 *             the xML stream exception
	 * @throws ServiceXmlSerializationException
	 *             the service xml serialization exception
	 */
	@Override
	protected void writeElementsToXml(EwsServiceXmlWriter writer)
			throws XMLStreamException, ServiceXmlSerializationException {
		writer.writeStartElement(XmlNamespace.Autodiscover,
				XmlElementNames.Request);

		writer.writeStartElement(XmlNamespace.Autodiscover,
				XmlElementNames.Users);

		for (String smtpAddress : this.getSmtpAddresses()) {
			writer.writeStartElement(XmlNamespace.Autodiscover,
					XmlElementNames.User);

			if (!(smtpAddress == null || smtpAddress.isEmpty())) {
				writer.writeElementValue(XmlNamespace.Autodiscover,
						XmlElementNames.Mailbox, smtpAddress);
			}
			writer.writeEndElement(); // User
		}
		writer.writeEndElement(); // Users

		writer.writeStartElement(XmlNamespace.Autodiscover,
				XmlElementNames.RequestedSettings);
		for (UserSettingName setting : this.getSettings()) {
			writer.writeElementValue(XmlNamespace.Autodiscover,
					XmlElementNames.Setting, setting);
		}

		writer.writeEndElement(); // RequestedSettings

		writer.writeEndElement(); // Request
	}

	/**
	 * Gets  the SMTP addresses.
	 * 
	 * @return the smtp addresses
	 */
	protected List<String> getSmtpAddresses() {
		return smtpAddresses;
	}

	/**
	 * Sets the smtp addresses.
	 * 
	 * @param value
	 *            the new smtp addresses
	 */
	protected void setSmtpAddresses(List<String> value) {
		this.smtpAddresses = value;
	}

	/**
	 * Gets the settings.
	 * 
	 * @return the settings
	 */
	protected List<UserSettingName> getSettings() {
		return settings;
	}

	/**
	 * Sets the settings.
	 * 
	 * @param value
	 *            the new settings
	 */
	protected void setSettings(List<UserSettingName> value) {
		this.settings=value;
		
	}

}
