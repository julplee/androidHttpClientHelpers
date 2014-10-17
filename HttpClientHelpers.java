package com.playnet.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class HttpClientHelpers {

	private final static String TAG = "HttpHelper";
	private final static String HOST_URL = "http://10.0.2.2:49824";

	private static CookieStore sCookieStore = null;
	private static String sOAuthBearerTokenHeader = null;
	
	public static void setOAuthBearerToken(String oAuthBearerTokenHeader) {
		sOAuthBearerTokenHeader = oAuthBearerTokenHeader;
	}
	
	public static String invokePost(String action, List<NameValuePair> params) {
		try {
			String url = HOST_URL + action;
			Log.d(TAG, "url is " + url);

			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type",
					"application/x-www-form-urlencoded");

			if (sOAuthBearerTokenHeader != null) {
				httpPost.setHeader("Authorization", sOAuthBearerTokenHeader);
			}

			if (params != null && params.size() > 0) {
				HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
				httpPost.setEntity(entity);
			}

			return invoke(httpPost);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

		return null;
	}

	public static String invokePost(String action) {
		return invokePost(action, null);
	}

	public static String invokeGet(String action, List<NameValuePair> params) {
		try {
			StringBuilder sb = new StringBuilder(HOST_URL);
			sb.append(action);
			if (params != null) {
				for (NameValuePair param : params) {
					sb.append("?");
					sb.append(param.getName());
					sb.append("=");
					sb.append(param.getValue());
				}
			}

			Log.d(TAG, "url is" + sb.toString());
			HttpGet httpGet = new HttpGet(sb.toString());
			httpGet.setHeader("Accept", "application/json");
			httpGet.setHeader("Accept-Encoding", "gzip");

			if (sOAuthBearerTokenHeader != null) {
				httpGet.setHeader("Authorization", sOAuthBearerTokenHeader);
			}

			return invoke(httpGet);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

		return null;
	}

	public static String invokeGet(String action) {
		return invokeGet(action, null);
	}

	private static String invoke(HttpUriRequest request)
			throws ClientProtocolException, IOException {
		String resultString = null;
		DefaultHttpClient httpClient = new DefaultHttpClient();

		// restore cookie
		if (sCookieStore != null) {
			httpClient.setCookieStore(sCookieStore);
		}

		HttpResponse httpResponse = httpClient.execute(request);

		int statusCode = httpResponse.getStatusLine().getStatusCode(); 
		HttpEntity httpEntity = httpResponse.getEntity();
		
		if (statusCode == HttpStatus.SC_OK) {
			if (httpEntity != null) {
				InputStream inputStream = httpEntity.getContent();
				Header contentEncoding = httpResponse
						.getFirstHeader("Content-Encoding");

				if (contentEncoding != null
						&& contentEncoding.getValue().equalsIgnoreCase("gzip")) {
					inputStream = new GZIPInputStream(inputStream);
				}

				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream));

				for (String s = reader.readLine(); s != null; s = reader
						.readLine()) {
					builder.append(s);
				}

				resultString = builder.toString();
				Log.d(TAG, "result is ( " + resultString + " )");
			}

			// store cookie
			sCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
		} else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			
		} else if (statusCode == HttpStatus.SC_BAD_REQUEST) {
			
		}

		return resultString;
	}
}