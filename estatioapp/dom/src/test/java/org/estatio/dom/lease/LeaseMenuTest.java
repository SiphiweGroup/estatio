package org.estatio.dom.lease;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.estatio.dom.party.Party;

import static org.assertj.core.api.Assertions.assertThat;

public class LeaseMenuTest {

    public static class NewLease extends LeaseMenuTest {

        ApplicationTenancy applicationTenancy;
        Party landLordFra;
        Party landLordIta;
        Party tenantFra;
        Party tenantIta;
        String error;
        LeaseMenu leaseMenu;

        @Before
        public void setUp() throws Exception {
            leaseMenu = new LeaseMenu();
        }

        @Test
        public void happyCase() {

            // given
            applicationTenancy = new ApplicationTenancy();
            applicationTenancy.setPath("/FRA/PDH");

            // when
            landLordFra = new Party() {
                @Override public ApplicationTenancy getApplicationTenancy() {
                    ApplicationTenancy applicationTenancyParty = new ApplicationTenancy();
                    applicationTenancyParty.setPath("/FRA");
                    return applicationTenancyParty;
                }
            };
            tenantFra = new Party() {
                @Override public ApplicationTenancy getApplicationTenancy() {
                    ApplicationTenancy applicationTenancyParty = new ApplicationTenancy();
                    applicationTenancyParty.setPath("/FRA");
                    return applicationTenancyParty;
                }
            };
            error = leaseMenu.validateNewLease(applicationTenancy, null, null, null, new LocalDate(2010, 01, 01), null, new LocalDate(2020, 01, 01), landLordFra, tenantFra);

            // then
            assertThat(error).isNull();

            // and when
            landLordIta = new Party() {
                @Override public ApplicationTenancy getApplicationTenancy() {
                    ApplicationTenancy applicationTenancyParty = new ApplicationTenancy();
                    applicationTenancyParty.setPath("/ITA");
                    return applicationTenancyParty;
                }
            };
            error = leaseMenu.validateNewLease(applicationTenancy, null, null, null, new LocalDate(2010, 01, 01), null, new LocalDate(2020, 01, 01), landLordIta, tenantFra);

            // then
            assertThat(error).isEqualTo("Landlord not valid. (wrong application tenancy)");

            // and when
            tenantIta = new Party() {
                @Override public ApplicationTenancy getApplicationTenancy() {
                    ApplicationTenancy applicationTenancyParty = new ApplicationTenancy();
                    applicationTenancyParty.setPath("/ITA");
                    return applicationTenancyParty;
                }
            };
            error = leaseMenu.validateNewLease(applicationTenancy, null, null, null, new LocalDate(2010, 01, 01), null, new LocalDate(2020, 01, 01), landLordFra, tenantIta);

            // then
            assertThat(error).isEqualTo("Tenant not valid. (wrong application tenancy)");

        }

    }

}