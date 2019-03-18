package com.fractalmc.commons.common.core.utils;

import java.util.Calendar;

import static java.util.Calendar.*;

public class FractalCalendar
{
    private static boolean isNewYear; //1/1
    private static boolean isValentinesDay; //14/2
    private static boolean isStDavidsDay; //1/3
    private static boolean isAprilFoolsDay; //1/4
    private static boolean isHalloween; //31/10
    private static boolean isChristmas; //25/12

    public static int day;

    public static void checkDate()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        day = calendar.get(Calendar.DAY_OF_MONTH);

        switch (calendar.get(Calendar.MONTH))
        {
            case JANUARY:
            {
                if (day == 1)
                {
                    isNewYear = true;
                }

                break;
            }
            case FEBRUARY:
            {
                if (day == 14)
                {
                    isValentinesDay = true;
                }

                break;
            }
            case MARCH:
            {
                if (day == 1)
                {
                    isStDavidsDay = true;
                }

                break;
            }
            case APRIL:
            {
                if (day == 1)
                {
                    isAprilFoolsDay = true;
                }

                break;
            }
            case OCTOBER:
            {
                if (day == 31)
                {
                    isHalloween = true;
                }

                break;
            }
            case DECEMBER:
            {
                if (day == 25 || day == 24)
                {
                    isChristmas = true;
                }

                break;
            }
        }
    }

    public static boolean isNewYear()
    {
        return isNewYear;
    }

    public static boolean isValentinesDay()
    {
        return isValentinesDay;
    }

    public static boolean isStDavidsDay()
    {
        return isStDavidsDay;
    }

    public static boolean isAprilFoolsDay()
    {
        return isAprilFoolsDay;
    }

    public static boolean isHalloween()
    {
        return isHalloween;
    }

    public static boolean isChristmas()
    {
        return isChristmas;
    }
}