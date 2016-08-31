/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.integtests.lease;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.applib.services.wrapper.InvalidException;

import org.estatio.dom.asset.Unit;
import org.estatio.dom.asset.UnitRepository;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.LeaseRepository;
import org.estatio.dom.lease.Occupancy;
import org.estatio.dom.lease.OccupancyRepository;
import org.estatio.dom.lease.tags.Brand;
import org.estatio.dom.lease.tags.Brand.RemoveEvent;
import org.estatio.dom.lease.tags.BrandRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertyForOxfGb;
import org.estatio.fixture.lease.LeaseForOxfMediaX002Gb;
import org.estatio.fixture.lease.LeaseForOxfTopModel001Gb;
import org.estatio.fixture.lease.LeaseItemAndTermsForOxfTopModel001;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class OccupancyRepositoryTest extends EstatioIntegrationTest {

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new LeaseItemAndTermsForOxfTopModel001());
            }
        });
        lease = leaseRepository.findLeaseByReference(LeaseForOxfTopModel001Gb.REF);
        unit = unitRepository.findUnitByReference(PropertyForOxfGb.unitReference("001"));
    }

    @Inject
    LeaseRepository leaseRepository;

    @Inject
    UnitRepository unitRepository;

    @Inject
    OccupancyRepository occupancyRepository;

    @Inject
    BrandRepository brandRepository;

    Lease lease;

    Unit unit;

    public static class FindByLease extends OccupancyRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            assertThat(occupancyRepository.findByLease(lease).size(), is(1));
        }

    }

    public static class FindByUnit extends OccupancyRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            assertThat(occupancyRepository.findByUnit(unit).size(), is(1));
        }

    }

    public static class FindByLeaseAndUnitAndStartDate extends OccupancyRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            assertNotNull(occupancyRepository.findByLeaseAndUnitAndStartDate(lease, unit, new LocalDate(2010, 7, 15)));
        }

    }

    public static class FindByBrand extends OccupancyRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            Brand brand = brandRepository.findByName(LeaseForOxfTopModel001Gb.BRAND);
            assertNotNull(brand);

            assertThat(occupancyRepository.findByBrand(brand, false).size(), is(1));
            assertThat(occupancyRepository.findByBrand(brand, true).size(), is(1));
        }

    }

    public static class FindByLeaseAndDate extends OccupancyRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            List<Occupancy> results = occupancyRepository.findByLeaseAndDate(lease, lease.getStartDate());
            assertThat(results.size(), is(1));
        }
    }

    public static class OnBrandRemoveEvent extends OccupancyRepositoryTest {

        Brand oldBrand;
        Brand newBrand;

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Before
        public void setupData() {
            runFixtureScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());
                    executionContext.executeChild(this, new LeaseForOxfTopModel001Gb());
                    executionContext.executeChild(this, new LeaseForOxfMediaX002Gb());
                }
            });

        }

        @Before
        public void setUp() throws Exception {
            oldBrand = brandRepository.findByName(LeaseForOxfTopModel001Gb.BRAND);
            newBrand = brandRepository.findByName(LeaseForOxfMediaX002Gb.BRAND);
        }

        @Test
        public void invalidBecauseNoReplacement() throws Exception {
            // when
            Brand.RemoveEvent event = new RemoveEvent();
            event.setSource(oldBrand);
            event.setArguments(Lists.newArrayList());
            event.setEventPhase(AbstractDomainEvent.Phase.VALIDATE);
            occupancyRepository.on(event);

            // then
            assertTrue(event.isInvalid());
        }

        @Test
        public void executingReplacesBrand() throws Exception {
            // when
            Brand.RemoveEvent event = new RemoveEvent();
            event.setSource(oldBrand);
            event.setArguments(Lists.newArrayList(newBrand));
            event.setEventPhase(AbstractDomainEvent.Phase.VALIDATE);
            occupancyRepository.on(event);
            event.setEventPhase(AbstractDomainEvent.Phase.EXECUTING);
            occupancyRepository.on(event);

            /*
             * then Topmodel brand should be adopted by the MEDIAX brand. So,
             * there should be 2
             * MEDIAX occupancies and 0 TOPMODEL occupancies.
             */
            assertThat(occupancyRepository.findByBrand(oldBrand, false).size(), is(0));
            assertThat(occupancyRepository.findByBrand(newBrand, false).size(), is(2));
        }

        @Test
        public void whenVetoingSubscriber() {
            // then
            expectedException.expect(InvalidException.class);

            // when
            wrap(oldBrand).remove();
        }

    }

    public static class OnChangeDateEvent extends OccupancyRepositoryTest {

        @Before
        public void setupData() {
            runFixtureScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());

                    executionContext.executeChild(this, new LeaseForOxfTopModel001Gb());
                }
            });
        }

        private Lease leaseTopModel;
        private Occupancy occupancy;

        @Before
        public void setup() {
            leaseTopModel = leaseRepository.findLeaseByReference(LeaseForOxfTopModel001Gb.REF);
            occupancy = leaseTopModel.primaryOccupancy().get();
        }

        @Test
        public void onDateChange() throws Exception {
            // Given that occupancy dates are equal to lease dates
            assertEquals(occupancy.getStartDate(), leaseTopModel.getTenancyStartDate());

            // When lease tenancy start date changes
            LocalDate newTenancyStartDate = new LocalDate(2015, 12, 31);
            wrap(leaseTopModel).changeTenancyDates(newTenancyStartDate, leaseTopModel.getTenancyEndDate());

            // Then assert that occupancy date has changed too
            assertThat(occupancy.getStartDate(), is(newTenancyStartDate));
            assertEquals(occupancy.getStartDate(), leaseTopModel.getTenancyStartDate());
        }
    }

    public static class OnTerminateEvent extends OccupancyRepositoryTest {

        @Before
        public void setupData() {
            runFixtureScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());

                    executionContext.executeChild(this, new LeaseForOxfTopModel001Gb());
                }
            });
        }

        private Lease leaseTopModel;
        private Occupancy occupancy;

        @Before
        public void setup() {
            leaseTopModel = leaseRepository.findLeaseByReference(LeaseForOxfTopModel001Gb.REF);
            occupancy = leaseTopModel.primaryOccupancy().get();
        }

        @Test
        public void onTerminate() throws Exception {
            // Given that occupancy has no end date
            assertNull(occupancy.getEndDate());

            // When lease is terminated
            LocalDate terminationDate = new LocalDate(2015, 12, 31);
            wrap(leaseTopModel).terminate(terminationDate);

            // Then assert that occupancy end date is set too
            assertThat(occupancy.getEndDate(), is(terminationDate));
            assertEquals(occupancy.getEndDate(), leaseTopModel.getTenancyEndDate());
        }
    }
}