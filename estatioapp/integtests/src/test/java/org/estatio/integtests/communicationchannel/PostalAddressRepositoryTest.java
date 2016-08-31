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
package org.estatio.integtests.communicationchannel;

import java.util.Iterator;
import java.util.SortedSet;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.communicationchannel.CommunicationChannel;
import org.estatio.dom.communicationchannel.CommunicationChannelRepository;
import org.estatio.dom.communicationchannel.CommunicationChannelType;
import org.estatio.dom.communicationchannel.PostalAddress;
import org.estatio.dom.communicationchannel.PostalAddressRepository;
import org.estatio.dom.party.Party;
import org.estatio.dom.party.PartyRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.party.OrganisationForTopModelGb;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PostalAddressRepositoryTest extends EstatioIntegrationTest {

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new OrganisationForTopModelGb());
            }
        });
    }

    @Inject
    PostalAddressRepository postalAddressRepository;

    @Inject
    CommunicationChannelRepository communicationChannelRepository;

    @Inject
    PartyRepository partyRepository;

    Party party;

    CommunicationChannel communicationChannel;

    PostalAddress postalAddress;

    @Before
    public void setUp() throws Exception {
        party = partyRepository.findPartyByReference(OrganisationForTopModelGb.REF);
        SortedSet<CommunicationChannel> results = communicationChannelRepository.findByOwner(party);
        Iterator<CommunicationChannel> it = results.iterator();
        while (it.hasNext()) {
            CommunicationChannel next = it.next();
            if (next.getType() == CommunicationChannelType.POSTAL_ADDRESS) {
                postalAddress = (PostalAddress) next;
            }
        }

        assertThat(postalAddress.getAddress1(), is("1 Circle Square"));
        assertThat(postalAddress.getPostalCode(), is("W2AXXX"));
    }

    public static class FindByAddress extends PostalAddressRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            // when
            PostalAddress result = postalAddressRepository.findByAddress(party,
                    postalAddress.getAddress1(),
                    postalAddress.getPostalCode(),
                    postalAddress.getCity(),
                    postalAddress.getCountry());

            // then
            assertThat(result, is(postalAddress));
        }
    }
}