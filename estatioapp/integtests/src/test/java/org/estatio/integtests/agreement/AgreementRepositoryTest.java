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
package org.estatio.integtests.agreement;

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.agreement.Agreement;
import org.estatio.dom.agreement.AgreementRepository;
import org.estatio.dom.agreement.AgreementRoleTypeRepository;
import org.estatio.dom.agreement.AgreementType;
import org.estatio.dom.agreement.AgreementTypeRepository;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.LeaseRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.lease.LeaseForKalPoison001Nl;
import org.estatio.fixture.lease.LeaseForOxfMediaX002Gb;
import org.estatio.fixture.lease.LeaseForOxfMiracl005Gb;
import org.estatio.fixture.lease.LeaseForOxfPoison003Gb;
import org.estatio.fixture.lease.LeaseForOxfPret004Gb;
import org.estatio.fixture.lease.LeaseForOxfTopModel001Gb;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class AgreementRepositoryTest extends EstatioIntegrationTest {

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());

                // 5 oxford leases, 1 kal
                executionContext.executeChild(this, new LeaseForOxfTopModel001Gb());
                executionContext.executeChild(this, new LeaseForOxfMediaX002Gb());
                executionContext.executeChild(this, new LeaseForOxfPoison003Gb());
                executionContext.executeChild(this, new LeaseForOxfPret004Gb());
                executionContext.executeChild(this, new LeaseForOxfMiracl005Gb());
                executionContext.executeChild(this, new LeaseForKalPoison001Nl());
            }
        });

        lease = leaseRepository.findLeaseByReference(LeaseForOxfTopModel001Gb.REF);
    }

    @Inject
    AgreementRepository agreementRepository;

    @Inject
    AgreementTypeRepository agreementTypeRepository;

    @Inject
    AgreementRoleTypeRepository agreementRoleTypeRepository;

    @Inject
    LeaseRepository leaseRepository;

    Lease lease;

    public static class FindByTypeAndReferenceOrName extends AgreementRepositoryTest {

        @Test
        public void whenPresent() throws Exception {
            final AgreementType type = agreementTypeRepository.find("Lease");
            assertNotNull(type);
            final List<Agreement> results = agreementRepository.findByTypeAndReferenceOrName(type, ".*OXF.*");
            assertThat(results.size(), is(5));
        }
    }

    public static class FindAgreementByReference extends AgreementRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            Lease agreement = leaseRepository.findLeaseByReference(lease.getReference());
            assertThat(agreement.getName(), is(lease.getName()));
        }
    }

}
