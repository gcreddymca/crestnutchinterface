package com.hm.util;

public class StatusCodeMessages {

	public String getStatusMessage(int code){
		String message = null;
		
		switch (code) {

						
						case 301:
								message = "Moved Permanently";
								break;
						case 302:
								message = "Moved Temporarily";
								break;
						case 404:
								message = "Page Not Found";
								break;
						case 500:
								message = "Internal Server Error";
								break;
						default:
							message = String.valueOf(code);
								break;
					}
		return message;
	}
}
