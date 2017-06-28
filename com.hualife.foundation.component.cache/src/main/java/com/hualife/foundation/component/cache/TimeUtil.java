package com.hualife.foundation.component.cache;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.CronTrigger;

public class TimeUtil {
	public static Date getDate(String cronExpression){
		Date date = null;
		try {
			CronTrigger conTrigger = new CronTrigger("triggerPreviewSchedule", "groupPreviewSchedule", cronExpression);
			date = conTrigger.getFireTimeAfter(new Date());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Date date2 = new Date();
			System.out.println("date1"+date);
			System.out.println("date2"+date2);
			long l = date.getTime()-date2.getTime();
			System.out.println(l);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	
	public static void main(String[] args){
		getDate("0 0 15 * * ?");
	}
}
