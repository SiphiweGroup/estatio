package org.estatio.dom.lease;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.factory.FactoryService;

import org.estatio.dom.UdoDomainRepositoryAndFactory;

@DomainService(nature = NatureOfService.DOMAIN, repositoryFor = LeaseItemSource.class)
public class LeaseItemSourceRepository extends UdoDomainRepositoryAndFactory<LeaseItemSource> {

    public LeaseItemSourceRepository() {
        super(LeaseItemSourceRepository.class, LeaseItemSource.class);
    }

    public LeaseItemSource newSource(final LeaseItem item, final LeaseItem sourceItem){
        final LeaseItemSource leaseItemSource = newTransientInstance(LeaseItemSource.class);
        leaseItemSource.setItem(item);
        leaseItemSource.setSourceItem(sourceItem);
        persist(leaseItemSource);
        return leaseItemSource;
    }

    public List<LeaseItemSource> findByItem(final LeaseItem item) {
        return allMatches("findByItem", "item", item);
    }

    @Inject
    FactoryService factoryService;


}
