package com.hm.file.monitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;


public class Test {

	public static void main(String args[]){
		Pattern PROTOCOLS = Pattern.compile("ftp");
		Matcher match = PROTOCOLS.matcher("ftp://localhost:8080/nrs.jsp");
		System.out.println(match.find());
		
		String str ="Testing string replacement error error";
		str.replaceAll("\r\n", "");
		str.replaceAll("\0", "");
		str.replaceAll("\n", "");
		System.out.println(str);
		
		Matcher matcher = Pattern.compile("G:/NVIZ/PlantronicsEnv/jboss-eap-4.2/jboss-as/server/plantronics/deploy/Plantronics.ear/plt_estore.war/"+
".*\\.css|.*\\.gif|.*\\.png|.*\\.jpg").matcher("G:/NVIZ/PlantronicsEnv/jboss-eap-4.2/jboss-as/server/plantronics/deploy/Plantronics.ear/plt_estore.war/middle-east/catalog/category.jsp");
    	if(matcher.find()) {
    		System.out.println("matched");
    	}else {
    		System.out.println("not matched");
    	}
    	System.out.println("/middle-east/catalog/category.jsp".matches("E:/NVIZ/workspaces/HM/Test/middle-east/catalog/category.jsp"));
    	System.out.println("*.css".matches("E:/NVIZ/workspaces/HM/Test/middle-east/catalog/category.jsp"));
    	
    	URL url;
		try {
			url = new URL("http://usscdev111:9080/hm/processForm?domainName=Plantrocnics&event=refreshSelectedURLSConfirm&isApi=true&isThreadWait=true&apiselectedURLS=/html/middle-east/catalog/index6.jsp");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			/*urlConnection.setReadTimeout(10000);
			urlConnection.setConnectTimeout(15000);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("domainName", "Plantrocnics"));
			params.add(new BasicNameValuePair("event", "refreshSelectedURLSConfirm"));
			params.add(new BasicNameValuePair("isApi", "true"));
			params.add(new BasicNameValuePair("apiselectedURLS", "/middle-east/index.jsp"));
			
			OutputStream os = urlConnection.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
			        new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(params));
			writer.flush();
			writer.close();
			os.close();*/
			urlConnection.connect();
			System.out.println(urlConnection.getResponseCode());
			
			try {
				URI uri = new URI("http", "localhost:9080","/processForm", "domainName=Plant QA", null);
				System.out.println(uri.toURL());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (NameValuePair pair : params)
	    {
	        if (first)
	            first = false;
	        else
	            result.append("&");

	        result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	    }

	    return result.toString();
	}
}
