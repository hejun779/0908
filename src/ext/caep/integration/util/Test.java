package ext.caep.integration.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Test {

	public static void main(String[] args) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			System.out.println(messageDigest.getAlgorithm());
			System.out.println(messageDigest.digest("tom".getBytes()).toString());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

}
