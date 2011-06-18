/**************************************************************************
 * copyright file="ServiceRequestBase.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the ServiceRequestBase.java.
 **************************************************************************/
package microsoft.exchange.webservices.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.xml.stream.XMLStreamException;

/**
 * Represents an abstract service request.
 * 
 */
abstract class ServiceRequestBase {

	// Private Constants
	// private final String XMLSchemaNamespace =
	// "http://www.w3.org/2001/XMLSchema";
	// private final String XMLSchemaInstanceNamespace =
	// "http://www.w3.org/2001/XMLSchema-instance";

	/** The service. */
	private ExchangeService service;

	// Methods for subclasses to override

	/***
	 * Gets the name of the XML element.
	 * 
	 * @return XML element name
	 */
	protected abstract String getXmlElementName();

	/***
	 * Gets the name of the response XML element.
	 * 
	 * @return XML element name
	 */
	protected abstract String getResponseXmlElementName();

	/***
	 * Gets the minimum server version required to process this request.
	 * 
	 * @return Exchange server version.
	 */
	protected abstract ExchangeVersion getMinimumRequiredServerVersion();

	/**
	 * * Writes XML elements.
	 * 
	 * @param writer
	 *            The writer.
	 * @throws XMLStreamException
	 *             the xML stream exception
	 * @throws ServiceXmlSerializationException
	 *             the service xml serialization exception
	 * @throws ServiceLocalException
	 *             the service local exception
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws ServiceValidationException
	 *             the service validation exception
	 * @throws Exception
	 *             the exception
	 */
	protected abstract void writeElementsToXml(EwsServiceXmlWriter writer) 
	throws XMLStreamException, ServiceXmlSerializationException,
	ServiceLocalException, InstantiationException,
	IllegalAccessException, ServiceValidationException, Exception;

	/**
	 * * Parses the response.
	 * 
	 * @param reader
	 *            The reader.
	 * @return Response object.
	 * @throws ServiceXmlDeserializationException
	 *             the service xml deserialization exception
	 * @throws XMLStreamException
	 *             the xML stream exception
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws ServiceLocalException
	 *             the service local exception
	 * @throws ServiceResponseException
	 *             the service response exception
	 * @throws IndexOutOfBoundsException
	 *             the index out of bounds exception
	 * @throws Exception
	 *             the exception
	 */
	protected abstract Object parseResponse(EwsServiceXmlReader reader)
	throws ServiceXmlDeserializationException, XMLStreamException,
	InstantiationException, IllegalAccessException,
	ServiceLocalException, ServiceResponseException,
	IndexOutOfBoundsException, Exception;

	/**
	 * * Validate request.
	 * 
	 * @throws ServiceLocalException
	 *             the service local exception
	 * @throws Exception
	 *             the exception
	 */
	protected void validate() throws ServiceLocalException, Exception {
		this.service.validate();
	}

	/**
	 * * Writes XML body.
	 * 
	 * @param writer
	 *            The writer.
	 * @throws Exception
	 *             the exception
	 */
	protected void writeBodyToXml(EwsServiceXmlWriter writer) throws Exception {
		writer.writeStartElement(XmlNamespace.Messages, this
				.getXmlElementName());

		this.writeAttributesToXml(writer);
		this.writeElementsToXml(writer);

		writer.writeEndElement(); // m:this.GetXmlElementName()
	}

	/**
	 * * Writes XML attributes. Subclass will override if it has XML attributes.
	 * 
	 * @param writer
	 *            The writer.
	 * @throws ServiceXmlSerializationException
	 *             the service xml serialization exception
	 */
	protected void writeAttributesToXml(EwsServiceXmlWriter writer)
	throws ServiceXmlSerializationException {
	}

	/**
	 * * Initializes a new instance.
	 * 
	 * @param service
	 *            The service.
	 * @throws ServiceVersionException
	 *             the service version exception
	 */
	protected ServiceRequestBase(ExchangeService service)
	throws ServiceVersionException {
		this.service = service;
		this.throwIfNotSupportedByRequestedServerVersion();
	}

	/***
	 * Gets the service.
	 * 
	 * @return The service.
	 */
	protected ExchangeService getService() {
		return service;
	}

	/**
	 * * Throw exception if request is not supported in requested server
	 * version.
	 * 
	 * @throws ServiceVersionException
	 *             the service version exception
	 */
	protected void throwIfNotSupportedByRequestedServerVersion()
	throws ServiceVersionException {
		if (this.service.getRequestedServerVersion().ordinal() < this
				.getMinimumRequiredServerVersion().ordinal()) {
			throw new ServiceVersionException(String.format(
					Strings.RequestIncompatibleWithRequestVersion, this
					.getXmlElementName(), this
					.getMinimumRequiredServerVersion()));
		}
	}

	// HttpWebRequest-based implementation

	/**
	 * * Writes XML.
	 * 
	 * @param writer
	 *            The writer.
	 * @throws Exception
	 *             the exception
	 */
	protected void writeToXml(EwsServiceXmlWriter writer) throws Exception {
		writer.writeStartDocument();
		writer.writeStartElement(XmlNamespace.Soap,
				XmlElementNames.SOAPEnvelopeElementName);
		writer.writeAttributeValue("xmlns", EwsUtilities
				.getNamespacePrefix(XmlNamespace.Soap), EwsUtilities
				.getNamespaceUri(XmlNamespace.Soap));
		writer.writeAttributeValue("xmlns",
				EwsUtilities.EwsXmlSchemaInstanceNamespacePrefix,
				EwsUtilities.EwsXmlSchemaInstanceNamespace);
		writer.writeAttributeValue("xmlns",
				EwsUtilities.EwsMessagesNamespacePrefix,
				EwsUtilities.EwsMessagesNamespace);
		writer.writeAttributeValue("xmlns",
				EwsUtilities.EwsTypesNamespacePrefix,
				EwsUtilities.EwsTypesNamespace);
		writer.writeStartElement(XmlNamespace.Soap,
				XmlElementNames.SOAPHeaderElementName);

		if (this.service.getCredentials() != null) {
			this.service.getCredentials().emitExtraSoapHeaderNamespaceAliases(
					writer.getInternalWriter());
		}

		// Emit the RequestServerVersion header
		writer.writeStartElement(XmlNamespace.Types,
				XmlElementNames.RequestServerVersion);
		writer.writeAttributeValue(XmlAttributeNames.Version, this
				.getRequestedServiceVersionString());
		writer.writeEndElement(); // RequestServerVersion



		if (this.service.getPreferredCulture() != null) {
			writer.writeElementValue(XmlNamespace.Types,
					XmlElementNames.MailboxCulture, this.service
					.getPreferredCulture().getDisplayName());
		}
		if (this.service.getImpersonatedUserId() != null) {
			this.service.getImpersonatedUserId().writeToXml(writer);
		}

		if (this.service.getCredentials() != null) {
			this.service.getCredentials().serializeExtraSoapHeaders(
					writer.getInternalWriter(), this.getXmlElementName());
		}
		this.service.doOnSerializeCustomSoapHeaders(writer.getInternalWriter());

		writer.writeEndElement(); // soap:Header

		writer.writeStartElement(XmlNamespace.Soap,
				XmlElementNames.SOAPBodyElementName);

		this.writeBodyToXml(writer);

		writer.writeEndElement(); // soap:Body
		writer.writeEndElement(); // soap:Envelope
		writer.flush();
	}

	/***
	 * Gets st ring representation of requested server version. In order to
	 * support E12 RTM servers, ExchangeService has another flag indicating that
	 * we should use "Exchange2007" as the server version string rather than
	 * Exchange2007_SP1.
	 * 
	 * @return String representation of requested server version.
	 */
	private String getRequestedServiceVersionString() {
		/*if (this.service.getRequestedServerVersion() == 
			ExchangeVersion.Exchange2007_SP1) {
			return "Exchange2007";
		} else {*/
		return this.service.getRequestedServerVersion().toString();
		//}
	}

	/**
	 * * Send request and get response.
	 * 
	 * @return HttpWebRequest object from which response stream can be read.
	 * @throws Exception
	 *             the exception
	 */
	protected HttpWebRequest emit(OutParam<HttpWebRequest> request) 
	throws Exception {
		request.setParam(this.getService().prepareHttpWebRequest());
		this.getService().traceHttpRequestHeaders(TraceFlags.
				EwsRequestHttpHeaders, request.getParam());

		// If tracing is enabled, we generate the request in-memory so that we
		// can pass it along to the ITraceListener. Then we copy the stream to
		// the request stream.
		if (this.service.isTraceEnabledFor(TraceFlags.EwsRequest)) {
			ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
			EwsServiceXmlWriter writer = new EwsServiceXmlWriter(this.service,
					memoryStream);
			this.writeToXml(writer);
			this.service.traceXml(TraceFlags.EwsRequest, memoryStream);
			OutputStream urlOutStream = request.getParam().getOutputStream();
			//request.getParam().write(memoryStream);
			//System.out.println("Actual XML : "+new String(memoryStream.toByteArray())+": end of XML");

			memoryStream.writeTo(urlOutStream);
			urlOutStream.flush();
			urlOutStream.close();
			writer.dispose();
			memoryStream.close();
		} else {
			//ByteArrayOutputStream bos = new ByteArrayOutputStream();  
			//ObjectOutputStream urlOutStream = new ObjectOutputStream(bos); 
			OutputStream urlOutStream = request.getParam().getOutputStream();
			EwsServiceXmlWriter writer = new EwsServiceXmlWriter(this.service,
					urlOutStream);
			this.writeToXml(writer);
			//request.getParam().write(bos);
			urlOutStream.flush();
			urlOutStream.close();
			writer.dispose();
		}
		// Closing and flushing stream does not ensure xml data is posted. Hence
		// try to get response code. This will force the xml data to be posted.
		request.getParam().executeRequest();
		if(request.getParam().getResponseCode() >= 400)
		{
			throw new Exception("The remote server returned an error: ("+request.getParam().getResponseCode()+")"+request.getParam().getResponseText());
		}
		return request.getParam();
	}

	/**
	 * * Gets the response stream (may be wrapped with GZip/Deflate stream to
	 * decompress content).
	 * 
	 * @param request
	 *            HttpWebRequest object from which response stream can be read.
	 * @return ResponseStream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	protected static InputStream getResponseStream(HttpWebRequest request)
	throws IOException, EWSHttpException {
		String contentEncoding = "";

		if (null != request.getContentEncoding()) {
			contentEncoding = request.getContentEncoding().toLowerCase();
		}

		InputStream responseStream;

		if (contentEncoding.contains("gzip")) {
			responseStream = new GZIPInputStream(request.getInputStream());
		} else if (contentEncoding.contains("deflate")) {
			responseStream = new InflaterInputStream(request.getInputStream());
		} else {
			responseStream = request.getInputStream();
		}
		return responseStream;
	}

	/**
	 * * Traces the response.
	 * 
	 * @param request
	 *            The response.
	 * @param memoryStream
	 *            The response content in a MemoryStream.
	 * @throws XMLStreamException
	 *             the xML stream exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	protected void traceResponse(HttpWebRequest request,
			ByteArrayOutputStream memoryStream) throws XMLStreamException,
			IOException, EWSHttpException {

		this.service.traceHttpResponseHeaders(
				TraceFlags.EwsResponseHttpHeaders, request);
		String contentType = request.getResponseContentType();

		if (!isNullOrEmpty(contentType) &&
				(contentType.startsWith("text/") ||
						contentType.startsWith("application/soap")))
		{
			this.service.traceXml(TraceFlags.EwsResponse, memoryStream);
		}
		else
		{
			this.service.traceMessage(TraceFlags.EwsResponse, 
			"Non-textual response");
		}

	}

	/**
	 * Gets the response error stream.
	 * 
	 * @param request
	 *            the request
	 * @return the response error stream
	 * @throws EWSHttpException
	 *             the eWS http exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static InputStream getResponseErrorStream(HttpWebRequest request)
	throws EWSHttpException, IOException {
		String contentEncoding = "";

		if (null != request.getContentEncoding()) {
			contentEncoding = request.getContentEncoding().toLowerCase();
		}

		InputStream responseStream;

		if (contentEncoding.contains("gzip")) {
			responseStream = new GZIPInputStream(request.getErrorStream());
		} else if (contentEncoding.contains("deflate")) {
			responseStream = new InflaterInputStream(request.getErrorStream());
		} else {
			responseStream = request.getErrorStream();
		}
		return responseStream;
	}

	/**
	 * * Reads the response.
	 * 
	 * @param ewsXmlReader
	 *            The XML reader.
	 * @return Service response.
	 * @throws Exception
	 *             the exception
	 */
	protected Object readResponse(EwsServiceXmlReader ewsXmlReader)
	throws Exception {
		Object serviceResponse;
		this.readPreamble(ewsXmlReader);
		ewsXmlReader.readStartElement(XmlNamespace.Soap,
				XmlElementNames.SOAPEnvelopeElementName);
		this.readSoapHeader(ewsXmlReader);
		ewsXmlReader.readStartElement(XmlNamespace.Soap,
				XmlElementNames.SOAPBodyElementName);

		ewsXmlReader.readStartElement(XmlNamespace.Messages, this
				.getResponseXmlElementName());

		serviceResponse = this.parseResponse(ewsXmlReader);

		ewsXmlReader.readEndElementIfNecessary(XmlNamespace.Messages, this
				.getResponseXmlElementName());

		ewsXmlReader.readEndElement(XmlNamespace.Soap,
				XmlElementNames.SOAPBodyElementName);
		ewsXmlReader.readEndElement(XmlNamespace.Soap,
				XmlElementNames.SOAPEnvelopeElementName);
		return serviceResponse;
	}

	/**
	 * * Reads any preamble data not part of the core response.
	 * 
	 * @param ewsXmlReader
	 *            The EwsServiceXmlReader.
	 * @throws Exception 
	 */
	protected void readPreamble(EwsServiceXmlReader ewsXmlReader) 
	throws Exception
	{            
		this.readXmlDeclaration(ewsXmlReader);
	}

	/**
	 * * Read SOAP header and extract server version.
	 * 
	 * @param reader
	 *            EwsServiceXmlReader
	 * @throws Exception
	 *             the exception
	 */
	private void readSoapHeader(EwsServiceXmlReader reader) throws Exception {
		reader.readStartElement(XmlNamespace.Soap,
				XmlElementNames.SOAPHeaderElementName);
		do {
			reader.read();

			// Is this the ServerVersionInfo?
			if (reader.isStartElement(XmlNamespace.Types,
					XmlElementNames.ServerVersionInfo)) {
				this.service.setServerInfo(ExchangeServerInfo.parse(reader));
			}

			// Ignore anything else inside the SOAP header
		} while (!reader.isEndElement(XmlNamespace.Soap,
				XmlElementNames.SOAPHeaderElementName));
	}




	/**
	 * * Processes the web exception.
	 * 
	 * @param webException
	 *            The web exception.
	 * @param req
	 *            http Request object used to send the http request.
	 * @throws Exception 
	 * @throws Exception 
	 */
	private void processWebException(Exception webException, HttpWebRequest req)
	throws Exception {
		SoapFaultDetails soapFaultDetails = null;
		if (null != req) {
			if (500 == req.getResponseCode()) {
				if (this.service.isTraceEnabledFor(TraceFlags.EwsResponse)) {
					ByteArrayOutputStream memoryStream = 
						new ByteArrayOutputStream();
					InputStream serviceResponseStream = ServiceRequestBase
					.getResponseErrorStream(req);
					while (true) {
						int data = serviceResponseStream.read();
						if (-1 == data) {
							break;
						} else {
							memoryStream.write(data);
						}
					}
					memoryStream.flush();
					serviceResponseStream.close();
					this.traceResponse(req, memoryStream);
					ByteArrayInputStream memoryStreamIn = 
						new ByteArrayInputStream(
								memoryStream.toByteArray());
					EwsServiceXmlReader reader = new EwsServiceXmlReader(
							memoryStreamIn, this.service);
					soapFaultDetails = this.readSoapFault(reader);
					memoryStream.close();
				} else {
					InputStream serviceResponseStream = ServiceRequestBase
					.getResponseStream(req);
					EwsServiceXmlReader reader = new EwsServiceXmlReader(
							serviceResponseStream, this.service);
					soapFaultDetails = this.readSoapFault(reader);
					serviceResponseStream.close();

				}


				if (soapFaultDetails != null) {
					switch (soapFaultDetails.getResponseCode()) {
					case ErrorInvalidServerVersion:
						throw new ServiceVersionException(
								Strings.ServerVersionNotSupported);

					case ErrorSchemaValidation:
						// If we're talking to an E12 server 
						//(8.00.xxxx.xxx), a schema
						// validation error is the same as
						//a version mismatch error.
						// (Which only will happen if we 
						//send a request that's not valid
						// for E12).
						if ((this.service.getServerInfo() != null) &&
								(this.service.getServerInfo().
										getMajorVersion() == 8) &&
										(this.service.getServerInfo().
												getMinorVersion() == 0)) {
							throw new ServiceVersionException(
									Strings.ServerVersionNotSupported);
						}

						break;

					case ErrorIncorrectSchemaVersion:
						// This shouldn't happen. It 
						//indicates that a request wasn't
						// valid for the version that was specified.
						EwsUtilities
						.EwsAssert(
								false,
								"ServiceRequestBase.ProcessWebException",
								"Exchange server supports " +
								"requested version " +
						"but request was invalid for that version");
						break;

					default:
						// Other error codes will 
						//be reported as remote error
						break;
					}

					// General fall-through case: 
					//throw a ServiceResponseException
					throw new ServiceResponseException(new ServiceResponse(
							soapFaultDetails));
				}
			}
			else
			{
				this.service.processHttpErrorResponse(req, webException);
			}
		}

	}

	/***
	 * Reads the SOAP fault.
	 * 
	 * @param reader
	 *            The reader.
	 * @return SOAP fault details.
	 */
	protected SoapFaultDetails readSoapFault(EwsServiceXmlReader reader) {
		SoapFaultDetails soapFaultDetails = null;

		try {
			this.readXmlDeclaration(reader);

			reader.read();
			if (!reader.isStartElement()
					|| (!reader.getLocalName().equals(
							XmlElementNames.SOAPEnvelopeElementName))) {
				return soapFaultDetails;
			}

			// EWS can sometimes return SOAP faults using the SOAP 1.2
			// namespace. Get the
			// namespace URI from the envelope element and use it for the rest
			// of the parsing.
			// If it's not 1.1 or 1.2, we can't continue.
			XmlNamespace soapNamespace = EwsUtilities
			.getNamespaceFromUri(reader.getNamespaceUri());
			if (soapNamespace == XmlNamespace.NotSpecified) {
				return soapFaultDetails;
			}

			reader.read();

			// EWS doesn't always return a SOAP header. If this response
			// contains a header element,
			// read the server version information contained in the header.
			if (reader.isStartElement(soapNamespace,
					XmlElementNames.SOAPHeaderElementName)) {
				do {
					reader.read();

					if (reader.isStartElement(XmlNamespace.Types,
							XmlElementNames.ServerVersionInfo)) {
						this.service.setServerInfo(ExchangeServerInfo
								.parse(reader));
					}
				} while (!reader.isEndElement(soapNamespace,
						XmlElementNames.SOAPHeaderElementName));

				// Queue up the next read
				reader.read();
			}

			// Parse the fault element contained within the SOAP body.
			if (reader.isStartElement(soapNamespace,
					XmlElementNames.SOAPBodyElementName)) {
				do {
					reader.read();

					// Parse Fault element
					if (reader.isStartElement(soapNamespace,
							XmlElementNames.SOAPFaultElementName)) {
						soapFaultDetails = SoapFaultDetails.parse(reader,
								soapNamespace);
					}
				} while (!reader.isEndElement(soapNamespace,
						XmlElementNames.SOAPBodyElementName));
			}

			reader.readEndElement(soapNamespace,
					XmlElementNames.SOAPEnvelopeElementName);
		} catch (Exception e) {
			// If response doesn't contain a valid SOAP fault, just ignore
			// exception and
			// return null for SOAP fault details.
			e.printStackTrace();
		}

		return soapFaultDetails;
	}

	/**
	 * * Validates request parameters, and emits the request to the server.
	 * 
	 * @param request
	 *            The request.
	 * @return The response returned by the server.
	 */
	protected HttpWebRequest validateAndEmitRequest(OutParam<HttpWebRequest> request) 
	throws ServiceLocalException, Exception
	{
		this.validate();

		try
		{
			return this.emit(request);
		}
		catch (IOException e)
		{
			// Wrap exception.
			throw new ServiceRequestException(String.format(Strings.
					ServiceRequestFailed, e.getMessage()), e);
		}
		catch (Exception e)
		{
			if (null != request.getParam() && -1 != request.getParam().getResponseCode()) {
				// this.service.traceHttpResponseHeaders(
				// TraceFlags.EwsResponseHttpHeaders, req);
				// response code -1 indicate exception is not HTTP exception
				this.processWebException(e, request.getParam());
			}

			// Wrap exception if the above code block didn't throw
			throw new ServiceRequestException(String.format(Strings.
					ServiceRequestFailed, e.getMessage()), e);
		}
	}

	/**
	 * * Checks whether input string is null or empty.
	 * 
	 * @param str
	 *            The input string.
	 * @return true if input string is null or empty, otherwise false
	 */
	private boolean isNullOrEmpty(String str) {
		return null == str || str.isEmpty();
	}

	/**
	 * * Try to read the XML declaration. If it's
	 *  not there, the server didn't return XML.
	 * 
	 * @param reader
	 *            The reader.
	 * @throws Exception 
	 */
	private void readXmlDeclaration(EwsServiceXmlReader reader) throws Exception
	{
		try
		{
			reader.read(new XMLNodeType(XMLNodeType.START_DOCUMENT));
		}
		catch (XmlException ex)
		{
			throw new ServiceRequestException(Strings.
					ServiceResponseDoesNotContainXml, ex);
		}
		catch (ServiceXmlDeserializationException ex)
		{
			throw new ServiceRequestException(Strings.
					ServiceResponseDoesNotContainXml, ex);
		}
	}

}
