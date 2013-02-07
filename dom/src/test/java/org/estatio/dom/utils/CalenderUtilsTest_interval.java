package org.estatio.dom.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import org.estatio.dom.utils.CalenderUtils;
import org.estatio.dom.utils.MathUtils;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.junit.Test;


public class CalenderUtilsTest_interval {

    @Test
    public void currentIntervalTest() throws Exception {
        assertEquals(CalenderUtils.currentInterval(new LocalDate(2012,2,1), "RRULE:FREQ=MONTHLY;INTERVAL=3" ), new Interval(new LocalDate(2012,1,1).toInterval().getStartMillis(), new LocalDate(2012,4,1).toInterval().getStartMillis()));
    }

    @Test
    public void zaraIntervalTest() throws Exception {
        assertEquals(CalenderUtils.currentInterval(new LocalDate(2012,3,1), "RRULE:FREQ=MONTHLY;INTERVAL=3", new LocalDate(2010,2,1) ), new Interval(new LocalDate(2012,2,1).toInterval().getStartMillis(), new LocalDate(2012,5,1).toInterval().getStartMillis()));
    }

    @Test
    public void zaraIntervalTest2() throws Exception {
        assertEquals(CalenderUtils.currentInterval(new LocalDate(2012,3,1), "RRULE:FREQ=MONTHLY;INTERVAL=3;BYMONTH=2,5,8,11", new LocalDate(2010,1,1) ), new Interval(new LocalDate(2012,2,1).toInterval().getStartMillis(), new LocalDate(2012,5,1).toInterval().getStartMillis()));
    }

    @Test
    public void roundDown() throws Exception {
        assertThat(new BigDecimal("4.5"), is(MathUtils.round(new BigDecimal("4.54"), 1)));
    }

    @Test
    public void noRounding() throws Exception {
        assertThat(new BigDecimal("4.54"), is(MathUtils.round(new BigDecimal("4.54"), 2)));
    }

}