package org.estatio.dom.lease;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.common.collect.Ordering;

import org.estatio.dom.invoice.InvoiceItem;
import org.estatio.dom.utils.CalenderUtils;
import org.estatio.dom.utils.DateRange;
import org.estatio.dom.utils.Orderings;
import org.joda.time.LocalDate;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;

@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "LEASETERM_ID")
public class LeaseTerm extends AbstractDomainObject implements Comparable<LeaseTerm> {

    // {{ Lease (property)
    private LeaseItem leaseItem;

    @Hidden(where = Where.PARENTED_TABLES)
    @MemberOrder(sequence = "1")
    @Persistent
    public LeaseItem getLeaseItem() {
        return leaseItem;
    }

    public void setLeaseItem(final LeaseItem leaseItem) {
        this.leaseItem = leaseItem;
    }

    // }}

    // {{ Sequence (property)
    private BigInteger sequence;

    @Hidden
    @Optional
    public BigInteger getSequence() {
        return sequence;
    }

    public void setSequence(final BigInteger sequence) {
        this.sequence = sequence;
    }

    // }}

    // {{ StartDate (property)
    private LocalDate startDate;

    @Persistent
    @MemberOrder(sequence = "2")
    @Title(sequence = "1")
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    // }}

    // {{ EndDate (property)
    private LocalDate endDate;

    @Persistent
    @MemberOrder(sequence = "3")
    @Title(sequence = "2", prepend = "-")
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    // }}

    // {{ Value (property)
    private BigDecimal value;

    @MemberOrder(sequence = "4")
    @Column(scale = 4)
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(final BigDecimal value) {
        this.value = value;
    }

    // }}

    // {{ NextTerm (property)
    private LeaseTerm nextTerm;

    @Hidden
    @Optional
    @MemberOrder(sequence = "1")
    public LeaseTerm getNextTerm() {
        return nextTerm;
    }

    public void setNextTerm(final LeaseTerm nextTerm) {
        this.nextTerm = nextTerm;
    }

    // }}

    // {{ Status (property)
    private LeaseTermStatus status;

    @MemberOrder(sequence = "1")
    public LeaseTermStatus getStatus() {
        return status;
    }

    public void setStatus(final LeaseTermStatus status) {
        this.status = status;
    }

    // }}

    // {{ InvoiceItems (Collection)
    private Set<InvoiceItem> invoiceItems = new LinkedHashSet<InvoiceItem>();

    @MemberOrder(sequence = "1")
    public Set<InvoiceItem> getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(final Set<InvoiceItem> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    // }}

    public void addToInvoiceItems(final InvoiceItem invoiceItem) {
        // check for no-op
        if (invoiceItem == null || getInvoiceItems().contains(invoiceItem)) {
            return;
        }
        // dissociate arg from its current parent (if any).
        invoiceItem.clearLeaseTerm();
        // associate arg
        invoiceItem.setLeaseTerm(this);
        getInvoiceItems().add(invoiceItem);
        // additional business logic
        onAddToInvoiceItems(invoiceItem);
    }

    private void onAddToInvoiceItems(InvoiceItem invoiceItem) {
        // TODO Auto-generated method stub

    }

    public void removeFromInvoiceItems(final InvoiceItem invoiceItem) {
        // check for no-op
        if (invoiceItem == null || !getInvoiceItems().contains(invoiceItem)) {
            return;
        }
        // dissociate arg
        invoiceItem.setLeaseTerm(null);
        getInvoiceItems().remove(invoiceItem);
        // additional business logic
        onRemoveFromInvoiceItems(invoiceItem);
    }

    private void onRemoveFromInvoiceItems(InvoiceItem invoiceItem) {
        // TODO Auto-generated method stub

    }

    public void removeUnapprovedInvoiceItems() {
        for (InvoiceItem item : getInvoiceItems()) {
            if (item.getInvoice() == null) {
                // TODO remove the invoice item
                // Select items within this period
            }
        }
    }

    public void createInvoiceItems(LocalDate date) {
        BigDecimal newValue = this.calculate(date);
        BigDecimal totalValue = BigDecimal.ZERO;
        for (InvoiceItem item : getInvoiceItems()) {
            // retrieve current value
            totalValue.add(item.getNetAmount());
        }
    }

    public void verify() {
        return;
    }

    public BigDecimal calculate(LocalDate startDate) {
        InvoicingFrequency freq = this.getLeaseItem().getInvoicingFrequency();
        DateRange parentRange = new DateRange(CalenderUtils.currentInterval(startDate, freq.rrule));
        DateRange range = new DateRange(this.getStartDate(), this.getEndDate(), true);
        range.setParentRange(parentRange);
        BigDecimal parentRangeDays = new BigDecimal(parentRange.getDays());
        BigDecimal rangeDays = new BigDecimal(range.getActualDays());
        BigDecimal rangeFactor = rangeDays.divide(parentRangeDays, MathContext.DECIMAL64);
        BigDecimal freqFactor = freq.numerator.divide(freq.denominator, MathContext.DECIMAL64);
        return getValue().multiply(freqFactor).multiply(rangeFactor).setScale(2, RoundingMode.HALF_UP);
    }

    // {{ CompareTo

    @Override
    public int compareTo(LeaseTerm o) {
        return ORDERING_BY_CLASS.compound(ORDERING_BY_START_DATE).compare(this, o);
    }

    public static Ordering<LeaseTerm> ORDERING_BY_CLASS = new Ordering<LeaseTerm>() {
        public int compare(LeaseTerm p, LeaseTerm q) {
            return Ordering.<String> natural().compare(p.getClass().toString(), q.getClass().toString());
        }
    };

    public final static Ordering<LeaseTerm> ORDERING_BY_START_DATE = new Ordering<LeaseTerm>() {
        public int compare(LeaseTerm p, LeaseTerm q) {
            return Orderings.lOCAL_DATE_NATURAL_NULLS_FIRST.compare(p.getStartDate(), q.getStartDate());
        }
    };

    // }}

}