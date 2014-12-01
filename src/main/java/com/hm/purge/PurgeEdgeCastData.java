package com.hm.purge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class PurgeEdgeCastData {
	
	private static final Logger logger = Logger.getLogger(PurgeEdgeCastData.class);
	private static final int EDGECAST_PURGE_CONNECTION_TIMEOUT;
	private static final String MEDIA_TYPE = "ADN";
	
	private Map<String, String> getValidPlformValues() {

		Map<String, String> validPlatformValues = new HashMap<String, String>();
		validPlatformValues.put("Flash Media Streaming", "2");
		validPlatformValues.put("HTTP Large", "3");
		validPlatformValues.put("HTTP Small", "8");
		validPlatformValues.put("ADN", "14");
		return validPlatformValues;
	}
	
	public Map<String, String> getPurgeIdDetails(String responsePurgeId) {
		Map<String, String> response = new HashMap<String, String>();
		if(responsePurgeId != null) {
			String responseJSON = purgeEdgeCastAPICall("GET","", responsePurgeId, false);
			if(responseJSON != null) {
				try {
					JSONParser parser = new JSONParser();
					JSONObject object = (JSONObject) parser.parse(responseJSON);
					response.put("CompleteDate",(String)object.get("CompleteDate"));
					response.put("InDate",(String)object.get("InDate"));
				} catch (ParseException e) {
					logger.error("purgeAssets Method :: Parser Exception Occured ::"+e);
				}
			} else {
				
			}
		}
		logger.info("purgeAssets Method :: Values after making a call with purgeId:"+responsePurgeId+" Response Map::"+response);
		return response;

	}
	
	public String getPurgeContentId(String mediaPath) {
		return getPurgeContentId(mediaPath, MEDIA_TYPE);
	}

	@SuppressWarnings("unchecked")
	public String getPurgeContentId(String mediaPath, String mediaType) {
		String respId = null;
		JSONObject obj = new JSONObject();
		obj.put("MediaPath", mediaPath);
		obj.put("MediaType", getValidPlformValues().get(mediaType));
		JSONObject object = null;
		String output = purgeEdgeCastAPICall("PUT",
				obj.toJSONString(), null, true);
		if(output != null) {
			try {
				JSONParser parser = new JSONParser();
				object = (JSONObject) parser.parse(output);
			} catch (ParseException e) {
				logger.error("getPurgeContentId Method :: Parser Exception Occured ::"+e);
			}
		} else {
			
		}
		if(object != null) {
			logger.info("getPurgeContentId Method :: Purge Id value:"+(String) object.get("Id"));
			respId = (String) object.get("Id");
		}
		return respId;
	}

	private String purgeEdgeCastAPICall(String action, String JSONInput, String purgeId, boolean purgeContent) {
		
		StringBuilder builder = new StringBuilder();
		BufferedReader in = null;
		StringBuilder currentUrl = new StringBuilder();
		try {
			if(purgeContent && purgeId == null) {
				currentUrl = currentUrl.append("https://api.edgecast.com/v2/mcc/customers/DF9D/edge/purge");
			}
			if(!purgeContent) {
				currentUrl = currentUrl.append("https://api.edgecast.com/v2/mcc/customers/DF9D/edge/purge/").append(purgeId);
			}
			logger.debug("purgeEdgeCastAPICall Method :: URL for Purge::"+currentUrl.toString());
			URL url = new URL(currentUrl.toString());
			HttpURLConnection httpurlconnection = (HttpURLConnection) url
					.openConnection();
			httpurlconnection.setRequestMethod(action);
			httpurlconnection.addRequestProperty("Authorization",
					"TOK:1ca17ed3-1e2d-47f2-8804-e539e2c4fa35");
			httpurlconnection.addRequestProperty("Accept", "application/json");
			httpurlconnection.addRequestProperty("Host", "api.edgecast.com");
			httpurlconnection.addRequestProperty("Content-Type",
					"application/json");
			httpurlconnection
					.setConnectTimeout(EDGECAST_PURGE_CONNECTION_TIMEOUT);
			httpurlconnection.setReadTimeout(EDGECAST_PURGE_CONNECTION_TIMEOUT);
			httpurlconnection.setDoOutput(true);
			if (!JSONInput.isEmpty() || purgeContent) {
				OutputStreamWriter outputstreamwriter = new OutputStreamWriter(
						httpurlconnection.getOutputStream());
				outputstreamwriter.write(JSONInput);
				outputstreamwriter.close();
			}
			int responseCode = httpurlconnection.getResponseCode();
			if(responseCode == 200) {
				logger.info(responseCode);
				InputStream input = httpurlconnection.getInputStream();
				in = new BufferedReader(new InputStreamReader(input));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					builder.append(inputLine);
				}
			} else {
				logger.error("getResponseStream Method :: Error Occured while getting the data::"+ responseCode);
				return null;
			}
		} catch (MalformedURLException e) {
			logger.error("purgeEdgeCastAPICall Method :: MalformedURL Exception Occured ::"+e.getMessage());
		} catch (IOException e) {
			logger.error("purgeEdgeCastAPICall Method :: IO Exception Occured ::"+e.getMessage());
		} catch (Exception e) {
			logger.error("purgeEdgeCastAPICall Method :: Exception Occured ::"+e.getMessage());
		}finally {
			try {
				if (in != null){
					in.close();
				}	
			} catch (Exception e) {
				logger.error("purgeEdgeCastAPICall Method :: while closing input stream Exception Occured ::"+e.getMessage());
			}
		}
		return builder.toString();
	}
	
	static {
		EDGECAST_PURGE_CONNECTION_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(20L);
	}
}
