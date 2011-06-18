/**************************************************************************
 * copyright file="HttpClientWebRequest.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the HttpClientWebRequest.java.
 **************************************************************************/
package microsoft.exchange.webservices.data;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;


/**
 * HttpClientWebRequest is used for making request to the server through 
 * NTLM Authentication by using Apache HttpClient 3.1 and JCIFS Library.
 */
class HttpClientWebRequest extends HttpWebRequest {

	/** The Http Client. */
	private HttpClient client = null;
	
	/** The Http Method. */
	private HttpMethodBase httpMethod = null;
	
	/** The TrustManager. */
	private TrustManager trustManger = null;
	
	private HttpConnectionManager simpleHttpConnMng = null;
	
	/**
	 * Instantiates a new http native web request.
	 */
	public HttpClientWebRequest(HttpConnectionManager simpleHttpConnMng) {
		this.simpleHttpConnMng = simpleHttpConnMng;
	}

	/**
	 * Releases the connection by Closing.
	 */
	@Override
	public void close() {
		if (null != httpMethod) {
			httpMethod.releaseConnection();
			//postMethod.abort();
		}
		httpMethod = null;
	}

	/**
	 * Prepare connection 
	 * 
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	@Override
	public void prepareConnection() throws EWSHttpException {
		if(trustManger != null) {
			EwsSSLProtocolSocketFactory.trustManager = trustManger;
		}
				
		Protocol.registerProtocol("https", 
				new Protocol("https", new EwsSSLProtocolSocketFactory(), 443));
		AuthPolicy.registerAuthScheme(AuthPolicy.NTLM, EwsJCIFSNTLMScheme.class);
		client = new HttpClient(this.simpleHttpConnMng); 
		List authPrefs = new ArrayList();
		authPrefs.add(AuthPolicy.NTLM);
		authPrefs.add(AuthPolicy.BASIC);
		authPrefs.add(AuthPolicy.DIGEST);
		client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
		
		if(getProxy() != null) {
			client.getHostConfiguration().setProxy(getProxy().getHost(),getProxy().getPort());
			if (HttpProxyCredentials.isProxySet()) {
				AuthScope authScope = new AuthScope(getProxy().getHost(), getProxy().getPort()); 
				client.getState().setProxyCredentials(authScope, new NTCredentials(HttpProxyCredentials.getUserName(),
	            		HttpProxyCredentials.getPassword(),
	                    "",HttpProxyCredentials.getDomain())); 
	                //new AuthScope(AuthScope.ANY_HOST, 80, AuthScope.ANY_REALM)
			}
		}
		if(getUserName() != null) {
		client.getState().setCredentials(AuthScope.ANY, new NTCredentials(getUserName(),getPassword(),"",getDomain()));
		}
		client.getHttpConnectionManager().getParams().setSoTimeout(getTimeout());
		client.getHttpConnectionManager().getParams().setConnectionTimeout(getTimeout());
		httpMethod = new PostMethod(getUrl().toString()); 
		httpMethod.setRequestHeader("Content-type", getContentType());
		httpMethod.setDoAuthentication(true);
		httpMethod.setRequestHeader("User-Agent", getUserAgent());		
		httpMethod.setRequestHeader("Accept", getAccept());		
		httpMethod.setRequestHeader("Keep-Alive", "300");		
		httpMethod.setRequestHeader("Connection", "Keep-Alive");	
		//httpMethod.setFollowRedirects(isAllowAutoRedirect());

		if (isAcceptGzipEncoding()) {
			httpMethod.setRequestHeader("Accept-Encoding", "gzip,deflate");
		}

		if (getHeaders().size() > 0){
			for (Map.Entry httpHeader : getHeaders().entrySet()) {
				httpMethod.setRequestHeader((String)httpHeader.getKey(),
						(String)httpHeader.getValue());						
			}

		}
	}

	/**
	 * Prepare asynchronous connection.
	 * 
	 * @throws EWSHttpException
	 *             throws EWSHttpException
	 */
	public void prepareAsyncConnection() throws EWSHttpException {
		try {
			if(trustManger != null) {
				EwsSSLProtocolSocketFactory.trustManager = trustManger;
			}
			
			Protocol.registerProtocol("https", 
					new Protocol("https", new EwsSSLProtocolSocketFactory(), 443));
			AuthPolicy.registerAuthScheme(AuthPolicy.NTLM, EwsJCIFSNTLMScheme.class);
			client = new HttpClient(this.simpleHttpConnMng); 
			List authPrefs = new ArrayList();
			authPrefs.add(AuthPolicy.NTLM);
			authPrefs.add(AuthPolicy.BASIC);
			authPrefs.add(AuthPolicy.DIGEST);
			client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);

			client.getState().setCredentials(AuthScope.ANY, new NTCredentials(getUserName(),getPassword(),"",getDomain()));
			client.getHttpConnectionManager().getParams().setSoTimeout(getTimeout());
			client.getHttpConnectionManager().getParams().setConnectionTimeout(20000);
			httpMethod = new GetMethod(getUrl().toString()); 
			httpMethod.setFollowRedirects(isAllowAutoRedirect());
			
			int status = client.executeMethod(httpMethod); 
		} catch (IOException e) {
			client = null;
			httpMethod = null;
			throw new EWSHttpException("Unable to open connection to "
					+ this.getUrl());
		}
	}

	/**
	 * Gets the input stream.
	 * 
	 * @return the input stream
	 * @throws EWSHttpException
	 *             the eWS http exception
	 * @throws IOException 
	 */
	@Override
	public InputStream getInputStream() throws EWSHttpException, IOException {
		throwIfConnIsNull();
		BufferedInputStream bufferedInputStream = null;
		try {
			bufferedInputStream = new 
			BufferedInputStream(httpMethod.getResponseBodyAsStream());
		} catch (IOException e) {
			throw new EWSHttpException("Connection Error " + e);
		}
		return bufferedInputStream;
	}

	/**
	 * Gets the error stream.
	 * 
	 * @return the error stream
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	@Override
	public InputStream getErrorStream() throws EWSHttpException {
		throwIfConnIsNull();
		BufferedInputStream bufferedInputStream = null;
		try {
			bufferedInputStream = new BufferedInputStream(
					httpMethod.getResponseBodyAsStream());
		} catch (Exception e) {
			throw new EWSHttpException("Connection Error " + e);
		}
		return bufferedInputStream;
	}

	/**
	 * Gets the output stream.
	 * 
	 * @return the output stream
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	@Override
	public OutputStream getOutputStream() throws EWSHttpException {
		OutputStream os = null;
		throwIfConnIsNull();
		os = new ByteArrayOutputStream();
		((EntityEnclosingMethod) httpMethod).setRequestEntity(new ByteArrayOSRequestEntity(os)); 
		return os;
	}

	/**
	 * Gets the response headers.
	 * 
	 * @return the response headers
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	@Override
	public Map<String, String> getResponseHeaders()
	throws EWSHttpException {
		throwIfConnIsNull();
		Map<String, String> map = new HashMap<String, String>(); 

		Header[] hM = httpMethod.getResponseHeaders();
		for (Header header : hM) {
			map.put(header.getName(),header.getValue());
		}
		return map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * microsoft.exchange.webservices.HttpWebRequest#getResponseHeaderField(
	 * java.lang.String)
	 */
	@Override
	public String getResponseHeaderField(String headerName)
	throws EWSHttpException {
		throwIfConnIsNull();
		Header hM = httpMethod.getResponseHeader(headerName);
		return hM != null ? hM.getValue() : null;
	}

	/**
	 * Gets the content encoding.
	 * 
	 * @return the content encoding
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	@Override
	public String getContentEncoding() throws EWSHttpException {
		throwIfConnIsNull();
		return httpMethod.getResponseHeader("content-encoding") != null ? httpMethod.getResponseHeader("content-encoding").getValue() : null;
	}

	/**
	 * Gets the response content type.
	 * 
	 * @return the response content type
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	@Override
	public String getResponseContentType() throws EWSHttpException {
		throwIfConnIsNull();
		return httpMethod.getResponseHeader("Content-type") != null ? httpMethod.getResponseHeader("Content-type").getValue() : null;
	}
	
	/**
	 * Executes Request by sending request xml data to server.
	 * 
	 * @throws EWSHttpException
	 *             the eWS http exception
	 * @throws HttpException
	 *             the http exception
	 * @throws IOException
	 *             the IO Exception
	 */
	@Override
	public int executeRequest() throws EWSHttpException, HttpException, IOException {
		throwIfConnIsNull();
		return client.executeMethod(httpMethod);
	}

	/**
	 * Gets the response code.
	 * 
	 * @return the response code
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	@Override
	public int getResponseCode() throws EWSHttpException {
		throwIfConnIsNull();
		return httpMethod.getStatusCode();
	}
	
	/**
	 * Gets the response message.
	 * 
	 * @return the response message
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	public String getResponseText() throws EWSHttpException {
		throwIfConnIsNull();
		return httpMethod.getStatusText();
	}

	/**
	 * Throw if conn is null.
	 * 
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	private void throwIfConnIsNull() throws EWSHttpException {
		if (null == httpMethod) {
			throw new EWSHttpException("Connection not established");
		}
	}

	/**
	 * Gets the request properties.
	 * 
	 * @return the request properties
	 * @throws EWSHttpException
	 *             the eWS http exception
	 */
	public Map<String,String> getRequestProperty() throws EWSHttpException
	{
		throwIfConnIsNull();
		Map<String, String> map = new HashMap<String, String>(); 

		Header[] hM = httpMethod.getRequestHeaders();
		for (Header header : hM) {
			map.put(header.getName(),header.getValue());
		}
		return map;
	}

	/**
	 * Sets the Client Certificates.
	 * 
	 * @param certs
	 * 			the Trust Manager
	 * @throws EWSHttpException
	 *             the eWS http exception
	 * @throws KeyManagementException
	 *             the KeyManagementException
	 * @throws NoSuchAlgorithmException
	 *             the NoSuchAlgorithmException
	 */
	public void setClientCertificates(TrustManager certs) throws EWSHttpException {
		trustManger = certs;
	}
}
