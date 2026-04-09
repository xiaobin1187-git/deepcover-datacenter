package io.deepcover.datacenter.service.utils;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DateTimeUtilTest {

    @Test
    public void testGetYMDByDateString() {
        Date result = DateTimeUtil.getYMDByDate("2024-06-15");
        assertNotNull(result);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testGetYMDByDateInvalidFormat() {
        assertThrows(RuntimeException.class, () -> DateTimeUtil.getYMDByDate("not-a-date"));
    }

    @Test
    public void testGetYMDByDateIntToday() {
        String result = DateTimeUtil.getYMDByDate(0);
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testGetYMDByDateIntYesterday() {
        String today = DateTimeUtil.getYMDByDate(0);
        String yesterday = DateTimeUtil.getYMDByDate(-1);
        assertNotNull(yesterday);
        assertNotEquals(today, yesterday);
    }

    @Test
    public void testGetThisDayStartToday() {
        Date result = DateTimeUtil.getThisDayStart(0);
        assertNotNull(result);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testGetThisDayStartTomorrow() {
        Date today = DateTimeUtil.getThisDayStart(0);
        Date tomorrow = DateTimeUtil.getThisDayStart(1);
        assertTrue(tomorrow.after(today));

        long diff = tomorrow.getTime() - today.getTime();
        assertEquals(24 * 60 * 60 * 1000, diff);
    }

    @Test
    public void testGetDaysSingleDay() {
        Date start = DateTimeUtil.getThisDayStart(0);
        Date end = DateTimeUtil.getThisDayStart(0);
        List<String> days = DateTimeUtil.getDays(start, end);
        assertEquals(1, days.size());
    }

    @Test
    public void testGetDaysRange() {
        Date start = DateTimeUtil.getYMDByDate("2024-01-01");
        Date end = DateTimeUtil.getYMDByDate("2024-01-05");
        List<String> days = DateTimeUtil.getDays(start, end);
        assertEquals(5, days.size());
        assertEquals("2024-01-01", days.get(0));
        assertEquals("2024-01-05", days.get(4));
    }

    @Test
    public void testGetDaysSameDayReturnsOne() {
        Date date = DateTimeUtil.getYMDByDate("2024-06-15");
        List<String> days = DateTimeUtil.getDays(date, date);
        assertEquals(1, days.size());
        assertEquals("2024-06-15", days.get(0));
    }

    @Test
    public void testGetDaysArrayString() {
        Date start = DateTimeUtil.getYMDByDate("2024-01-01");
        Date end = DateTimeUtil.getYMDByDate("2024-01-03");
        String result = DateTimeUtil.getDaysArrayString(start, end);
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("'2024-01-01'"));
        assertTrue(result.contains("'2024-01-03'"));
    }

    @Test
    public void testGetDaysArrayStringSingleDay() {
        Date date = DateTimeUtil.getYMDByDate("2024-06-15");
        String result = DateTimeUtil.getDaysArrayString(date, date);
        assertEquals("['2024-06-15']", result);
    }
}
