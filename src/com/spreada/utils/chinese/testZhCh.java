package com.spreada.utils.chinese;

public class testZhCh
{

	static ZHConverter simp_converter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED); // 繁体转简体
	static ZHConverter trad_converter = ZHConverter.getInstance(ZHConverter.TRADITIONAL); // 简体转繁体

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		String name = "帅气的鹏鹏";

		String trad_name = simp_converter.convert(name);
		System.out.println(trad_name);

		String simple_name = trad_converter.convert(trad_name);
		System.out.println(simple_name);
	}

}
