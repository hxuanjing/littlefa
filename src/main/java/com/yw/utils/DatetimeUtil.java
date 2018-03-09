package com.yw.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatetimeUtil {
	
	/**
	 * 按指定格式,格式化日期
	 * 
	 * @param date
	 *            日期
	 * @param pattern
	 *            日期格式
	 * @return 格式化日期
	 */
	public static String getDateByPattern(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		String s = sdf.format(date);
		return s;
	}

	/**
	 * 按指定格式,格式化日期（含异常捕捉）
	 * 
	 * @param date
	 *            日期
	 * @param pattern
	 *            日期格式
	 * @return 格式化日期
	 * @throws ParseException 
	 */
	public static Date getDateByPattern(String date, String pattern) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		Date d = sdf.parse(date);
		return d;
	}
	/**
	 * 获取指定日期的上月最后一天
	 * 
	 * @param da
	 *            指定日期
	 * @return 指定日期的当月的第一天   2015-01-31
	 * @throws ParseException 
	 */
	public static String getNextMFirstDay(Date da) throws ParseException {
		da = new Date(da.getTime());
		Calendar time = Calendar.getInstance();
		time.setTime(da);
		int year = time.get(Calendar.YEAR);
		int month = time.get(Calendar.MONTH) + 3;
//		int day = time.get(Calendar.DATE);
		String monthstr = month >= 10 ? String.valueOf(month) : ("0" + month);
//		String daystr = day >= 10 ? String.valueOf(day) : ("0" + day);
		Date d = DatetimeUtil.getDateByPattern(year + "-" + monthstr + "-" + "01", "yyyy-MM-dd");
		Date lastd = new Date(d.getTime()-24*60*60*1000);
		return DatetimeUtil.getDateByPattern(lastd, "yyyy-MM-dd");
	}
	/**
	 * 获取指定日期的上月最后一天
	 * 
	 * @param da
	 *            指定日期
	 * @return 指定日期的当月的第一天   2015-01-31
	 * @throws ParseException 
	 */
	public static String getMFirstDay(Date da) throws ParseException {
		da = new Date(da.getTime());
		Calendar time = Calendar.getInstance();
		time.setTime(da);
		int year = time.get(Calendar.YEAR);
		int month = time.get(Calendar.MONTH) + 1;
//		int day = time.get(Calendar.DATE);
		String monthstr = month >= 10 ? String.valueOf(month) : ("0" + month);
//		String daystr = day >= 10 ? String.valueOf(day) : ("0" + day);
		Date d = DatetimeUtil.getDateByPattern(year + "-" + monthstr + "-" + "01", "yyyy-MM-dd");
		Date lastd = new Date(d.getTime()-24*60*60*1000);
		return DatetimeUtil.getDateByPattern(lastd, "yyyy-MM-dd");
	}
	/**
	 * 获取指定日期的当季度的第一天
	 * 
	 * @param da
	 *            指定日期
	 * @return 指定日期的当季度的第一天
	 * @throws ParseException 
	 */
	public static String getQFirstDay(Date da) throws ParseException {
		
		String date = "";
		Calendar time = Calendar.getInstance();
		time.setTime(da);
		int year = time.get(Calendar.YEAR);
		int month = time.get(Calendar.MONTH) + 1;
//		int day = time.get(Calendar.DATE);
//		String monthstr = month >= 10 ? String.valueOf(month) : ("0" + month);
//		String daystr = day >= 10 ? String.valueOf(day) : ("0" + day);
		if(month >= 1 && month<=3) 
			date = year + "-" + "01" + "-" + "01";
		if(month >= 4 && month<=6) 
			date = year + "-" + "04" + "-" + "01";
		if(month >= 7 && month<=9) 
			date = year + "-" + "07" + "-" + "01";
		if(month >= 10 && month<=12) 
			date = year + "-" + "10" + "-" + "01";
		
		Date d = DatetimeUtil.getDateByPattern(date, "yyyy-MM-dd");
		Date lastd = new Date(d.getTime()-24*60*60*1000);
		return DatetimeUtil.getDateByPattern(lastd, "yyyy-MM-dd");
	}
	/**
	 * 获取指定日期的当年的第一天
	 * 
	 * @param da
	 *            指定日期
	 * @return 指定日期的当年的第一天
	 * @throws ParseException 
	 */
	public static String getYFirstDay(Date da) throws ParseException {
		String date = "";
		Calendar time = Calendar.getInstance();
		time.setTime(da);
		int year = time.get(Calendar.YEAR);
//		int month = time.get(Calendar.MONTH) + 1;
//		int day = time.get(Calendar.DATE);
//		String monthstr = month >= 10 ? String.valueOf(month) : ("0" + month);
//		String daystr = day >= 10 ? String.valueOf(day) : ("0" + day);
		date = year + "-" + "01" + "-" + "01";
		Date d = DatetimeUtil.getDateByPattern(date, "yyyy-MM-dd");
		Date lastd = new Date(d.getTime()-24*60*60*1000);
		return DatetimeUtil.getDateByPattern(lastd, "yyyy-MM-dd");
	}
	/**
	 * 获取指定日期的上年的同期
	 * 
	 * @param da
	 *            指定日期
	 * @return 指定日期的上年的同期
	 */
	public static String getCPFirstDay(Date da) {
		Calendar time = Calendar.getInstance();
		time.setTime(da);
		int year = time.get(Calendar.YEAR);
		int month = time.get(Calendar.MONTH) + 1;
		int day = time.get(Calendar.DATE);
		String monthstr = month >= 10 ? String.valueOf(month) : ("0" + month);
		String daystr = day >= 10 ? String.valueOf(day) : ("0" + day);
		if(leapYear(year) && month == 2 && day == 29){
			return year-1 + "-" + monthstr + "-28";
		}
		return year-1 + "-" + monthstr + "-" + daystr;
	}
	
	/**
	 * 计算日期之间差值
	 * @param beginDate 
	 * @param endDate 
	 * @return
	 */
	public static int calculateDayDiff(Date beginDate,Date endDate){
		try {
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
			beginDate=sdf.parse(sdf.format(beginDate));
			endDate=sdf.parse(sdf.format(endDate));
			Calendar cal = Calendar.getInstance();
			cal.setTime(beginDate);
			long time1=cal.getTimeInMillis();
			cal.setTime(endDate);
			long time2=cal.getTimeInMillis();
			long betweenDays=(time2-time1)/(1000*60*60*24);
			return Integer.parseInt(String.valueOf(betweenDays));
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * 获取指定日期的上上年的同期
	 * 
	 * @param da
	 *            指定日期
	 * @return 指定日期的上上年的同期
	 */
	public static String getCPLPFirstDay(Date da) {
		Calendar time = Calendar.getInstance();
		time.setTime(da);
		int year = time.get(Calendar.YEAR);
		int month = time.get(Calendar.MONTH) + 1;
		int day = time.get(Calendar.DATE);
		String monthstr = month >= 10 ? String.valueOf(month) : ("0" + month);
		String daystr = day >= 10 ? String.valueOf(day) : ("0" + day);
		if(leapYear(year) && month == 2 && day == 29){
			return year-1 + "-" + monthstr + "-28";
		}
		return year-2 + "-" + monthstr + "-" + daystr;
	}

	public static String getLastMin(Date day, int i) {
		return DatetimeUtil.getDateByPattern(new Date(day.getTime() - i*60*1000), "yyyy-MM-dd HH:mm:ss");
	}
	public static String getLastday(Date day) {
		return DatetimeUtil.getDateByPattern(new Date(day.getTime() - 24*60*60*1000), "yyyy-MM-dd");
	}
	public static String getLastday(Date day, int i, String pattern) {
		Calendar c = Calendar.getInstance();
		c.setTime(day);
		c.set(Calendar.DATE, c.get(Calendar.DATE) - i);
		return DatetimeUtil.getDateByPattern(c.getTime(), pattern);
	}
	public static boolean leapYear(int year){
		boolean leap = false;
		if(year % 4 == 0){
			if(year % 100 == 0){
				if(year % 400 == 0) leap = true;
				else leap = false;
			}else leap = true;
		}
		return leap;
	}
	/**
	 * 获取当前日期的后一天
	 * @param predays
	 * @return
	 */
	public static String getdate(int predays){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, predays);
		return sdf.format(c.getTime());
	}
	/**
	 * 获取指定日期的后一天
	 * @param predays
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static String getdate(int predays,String date) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(sdf.parse(date));
		c.add(Calendar.DATE, predays);
		return sdf.format(c.getTime());
	}
	
	/*public static void main(String[] args) throws ParseException {
		System.out.println(calculateDayDiff(DatetimeUtils.getDateByPattern("2012-10-11", "yyyy-MM-dd"), DatetimeUtils.getDateByPattern("2012-10-15", "yyyy-MM-dd")));
		System.out.println(leapYear(2012));
		Date date = getDateByPattern("20111228", "yyyyMMdd");
		System.out.println("月初"+getMFirstDay(date));
		System.out.println("季初"+getQFirstDay(date));
		System.out.println("年初"+getYFirstDay(date));
		System.out.println("上年同期"+getCPFirstDay(date));
		System.out.println("上上年同期"+getCPLPFirstDay(date));
		System.out.println(DatetimeUtils.getMFirstDay(new Date()));
		System.out.println(DatetimeUtils.getDateByPattern(new Date(), "yyyy-MM-dd HH:mm:00"));
		String d = DatetimeUtils.getDateByPattern(new Date(), "yyyy-MM-dd HH:mm:00");
		System.out.println(getLastMin(DatetimeUtils.getDateByPattern(d,"yyyy-MM-dd HH:mm:ss"),1));
		System.out.println(d.substring(0, 10));
		System.out.println(d.substring(11, 13));
		System.out.println(d.substring(14, 16));
	}*/
}