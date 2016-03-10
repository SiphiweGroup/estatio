package org.estatio.dom.alias;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;

import com.google.common.eventbus.Subscribe;

import org.apache.isis.applib.AbstractSubscriber;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

import org.incode.module.alias.dom.api.aliasable.Aliasable;
import org.incode.module.alias.dom.impl.aliaslink.AliasableLink;

import org.estatio.dom.charge.Charge;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType= IdentityType.DATASTORE)
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@DomainObject()
public class AliasableLinkForCharge extends AliasableLink {

    //region > instantiationSubscriber, setPolymorphicReference
    @DomainService(nature = NatureOfService.DOMAIN)
    public static class InstantiationSubscriber extends AbstractSubscriber {
        @Programmatic
        @Subscribe
        public void on(final InstantiateEvent ev) {
            if(ev.getPolymorphicReference() instanceof Charge) {
                ev.setSubtype(AliasableLinkForCharge.class);
            }
        }
    }

    @Override
    public void setPolymorphicReference(final Aliasable polymorphicReference) {
        super.setPolymorphicReference(polymorphicReference);
        setCharge((Charge) polymorphicReference);
    }

    @Column(
            allowsNull = "false",
            name = "taxId"
    )
    @Getter @Setter
    private Charge charge;

}