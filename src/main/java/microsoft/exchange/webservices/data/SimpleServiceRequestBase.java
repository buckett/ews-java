/**************************************************************************
 * copyright file="SimpleServiceRequestBase.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the SimpleServiceRequestBase.java.
 **************************************************************************/
package microsoft.exchange.webservices.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/***
 * Defines the SimpleServiceRequestBase class. 
 */
abstract class SimpleServiceRequestBase extends ServiceRequestBase {
	
	/**
	 * Initializes a new instance of the SimpleServiceRequestBase class.
	 */
	protected SimpleServiceRequestBase(ExchangeService service) 
	throws Exception {
		super(service);
	}

	/** 
	 * Execute this request. 
	 * @throws Exception 
	 * @throws ServiceLocalException 
	 */
	protected Object internalExecute() 
	throws ServiceLocalException, Exception {		
		OutParam<HttpWebRequest> outParam = 
			new OutParam<HttpWebRequest>();
		HttpWebRequest response = this.validateAndEmitRequest(outParam);
        try {        	
			return this.readResponse(response);
        }
        catch (IOException ex) {
            // Wrap exception.
            throw new ServiceRequestException(String.
            		format(Strings.ServiceRequestFailed, ex.getMessage(), ex));
        }
        catch (Exception e) {
            if (response != null) {
                this.getService().traceHttpResponseHeaders(TraceFlags.
                		EwsResponseHttpHeaders, response);
            }

            throw new ServiceRequestException(String.format(Strings.
            		ServiceRequestFailed, e.getMessage()), e);
        }
        finally
        {
        	try {
        		response.close(); 
			} catch (Exception e2) {
				response = null;
			}       	
        }
        
    }
		
	
	/**
	 * Reads the response.
	 * @return serviceResponse	
	 * @throws Exception 
	 */
	private Object readResponse(HttpWebRequest response) throws Exception {
		Object serviceResponse;
		
		/** If tracing is enabled, we read the entire 
		 * response into a MemoryStream so that we
          * can pass it along to the ITraceListener. 
          * Then we parse the response from the 
          * MemoryStream.
          */
		if (this.getService().isTraceEnabledFor(TraceFlags.EwsResponse)) {
			ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
			InputStream serviceResponseStream = ServiceRequestBase.
			getResponseStream(response);
			while (true) {
				int data = serviceResponseStream.read();
				if (-1 == data) {
					break;
				} else {
					memoryStream.write(data);
				}
			}
			

                this.traceResponse(response, memoryStream);
                ByteArrayInputStream memoryStreamIn = 
					new ByteArrayInputStream(
							memoryStream.toByteArray());
                EwsServiceXmlReader ewsXmlReader = 
                	new EwsServiceXmlReader(memoryStreamIn, 
					this.getService());                
                serviceResponse = this.readResponse(ewsXmlReader);
                serviceResponseStream.close();
                memoryStream.flush();		
        }
        else {
        	InputStream responseStream = ServiceRequestBase.
        	getResponseStream(response);
                EwsServiceXmlReader ewsXmlReader =
                	new EwsServiceXmlReader(responseStream, this.getService());
                serviceResponse = this.readResponse(ewsXmlReader);
            
        }

        return serviceResponse;
    }
	
}
