package com.hm.util;

public class PasswordTest {

	public static void main(String ar[]){
		try {
			System.out.println("Password:"+PasswordUtil.encrypt("admin", "Nutch"));
		} catch (Exception e) {
			System.out.println("Error while encrypt password: "+e.getMessage());
		}
	}
}
