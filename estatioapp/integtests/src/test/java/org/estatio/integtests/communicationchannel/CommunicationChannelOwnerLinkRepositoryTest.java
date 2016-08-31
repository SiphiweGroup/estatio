/*
 *
 *  Copyright 2012-2015 Eurocommercial Properties NV
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

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.communicationchannel.CommunicationChannelOwnerLink;
import org.estatio.dom.communicationchannel.CommunicationChannelOwnerLinkRepository;
import org.estatio.dom.communicationchannel.CommunicationChannelType;
import org.estatio.dom.communicationchannel.PostalAddress;
import org.estatio.dom.communicationchannel.PostalAddressRepository;
import org.estatio.dom.geography.Country;
import org.estatio.dom.geography.CountryRepository;
import org.estatio.dom.party.Organisation;
import org.estatio.dom.party.Party;
import org.estatio.dom.party.PartyRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.party.OrganisationForTopModelGb;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class CommunicationChannelOwnerLinkRepositoryTest extends EstatioIntegrationTest {

    @Before
    public void setupData() throws Exception {
        runFixtureScript(new FixtureScript() {
            @Override protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new OrganisationForTopModelGb());
            }
        });
    }

    public static class FindByCommunicationChannel extends CommunicationChannelOwnerLinkRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            // given
            final Party party = partyRepository.findPartyByReference(OrganisationForTopModelGb.REF);
            final Country country = countryRepository.findCountry("GBR");
            final PostalAddress postalAddress = postalAddressRepository.findByAddress(party, "1 Circle Square", "W2AXXX", "London", country);

            // when
            final CommunicationChannelOwnerLink communicationChannelOwnerLink = communicationChannelOwnerLinkRepository.findByCommunicationChannel(postalAddress);

            // then
            assertThat(communicationChannelOwnerLink.getOwnerObjectType()).isEqualToIgnoringCase(Organisation.class.getCanonicalName());
        }
    }

    public static class FindByOwner extends CommunicationChannelOwnerLinkRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            // given
            final Party party = partyRepository.findPartyByReference(OrganisationForTopModelGb.REF);

            // when
            final List<CommunicationChannelOwnerLink> communicationChannelOwnerLinks = communicationChannelOwnerLinkRepository.findByOwner(party);

            // then
            assertThat(communicationChannelOwnerLinks.size()).isEqualTo(5);
        }
    }

    public static class FindByOwnerAndCommunicationChannelType extends CommunicationChannelOwnerLinkRepositoryTest {

        @Test
        public void happyCase() throws Exception {
            // given
            final Party party = partyRepository.findPartyByReference(OrganisationForTopModelGb.REF);

            // when
            final List<CommunicationChannelOwnerLink> communicationChannelOwnerLinks = communicationChannelOwnerLinkRepository.findByOwnerAndCommunicationChannelType(party, CommunicationChannelType.PHONE_NUMBER);

            // then
            assertThat(communicationChannelOwnerLinks.size()).isEqualTo(1);
            assertThat(communicationChannelOwnerLinks.get(0).getCommunicationChannel().getType()).isEqualTo(CommunicationChannelType.PHONE_NUMBER);
        }
    }

    @Inject
    CommunicationChannelOwnerLinkRepository communicationChannelOwnerLinkRepository;

    @Inject
    PartyRepository partyRepository;

    @Inject
    CountryRepository countryRepository;

    @Inject
    PostalAddressRepository postalAddressRepository;
}
