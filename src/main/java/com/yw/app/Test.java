package com.yw.app;

import java.util.Arrays;

public class Test {
	public static void main(String[] args) {
//		String regex="^[^,，\\s]{1,}[,，][^,，\\s]{1,}[,，][男|女][,，]\\d{1,}[,，][\u4E00-\u9FA5]{2,}$";
//		String test="黄宣景，monkey，女，26，江西";
//		System.out.println(test.matches(regex));
//		String a="1.12";
//		System.out.println(Arrays.toString(a.split(":")));
		
		String regex1="^[^,，\\s]{1,}[:：][\\s\\S]{1,}$";
		String test1="d你好sa:dsadaslgjwoqjeiwqjeq\n				你好么！   ";
		System.out.println(test1.matches(regex1));
		
	}
}
