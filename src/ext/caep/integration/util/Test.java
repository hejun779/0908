package ext.caep.integration.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public static void main(String[] args) {
		String ID = "P0000001";
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(ID.substring(1));
		System.out.println(isNum.matches());

	}

}
