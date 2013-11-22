/*
 * Copyright (C) 2013 guang.com
 */

package net.river.socket.pool;

import java.util.Calendar;

/**
 *
 * @author ferdi email ferdi@blueferdi.com
 * @version 0.0.1
 */
public class App 
{
    public static void main(String[] args)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(1375427310155l);
        
        int hour = c.get(Calendar.HOUR_OF_DAY);
        
        System.out.println(hour);
        
        System.out.println((256 >>> 8) + " " + (256 & 0xff));
        
        System.out.println(1 << 8 | 0);
        
        
        
    }
}
