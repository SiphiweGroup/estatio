/*
 * Copyright 2015 Yodo Int. Projects and Consultancy
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.estatio.integtests.budget;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.asset.Unit;
import org.estatio.dom.asset.UnitRepository;
import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budget.BudgetRepository;
import org.estatio.dom.budgeting.keyitem.KeyItemRepository;
import org.estatio.dom.budgeting.keytable.FoundationValueType;
import org.estatio.dom.budgeting.keytable.KeyTable;
import org.estatio.dom.budgeting.keytable.KeyTableRepository;
import org.estatio.dom.budgeting.keytable.KeyValueMethod;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertyForOxfGb;
import org.estatio.fixture.budget.BudgetsForOxf;
import org.estatio.fixture.budget.KeyTablesForOxf;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyTableIntegrationTest extends EstatioIntegrationTest {

    @Inject
    KeyTableRepository keyTableRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    BudgetRepository budgetRepository;

    @Inject
    KeyItemRepository keyItemRepository;

    @Inject
    UnitRepository unitRepository;

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new KeyTablesForOxf());

            }
        });
    }

    public static class ChangeKeytableTest extends KeyTableIntegrationTest {

        KeyTable keyTable;

        @Test
        public void changeKeyTableTest() throws Exception {

            //given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            keyTable = keyTableRepository.findByBudgetAndName(budget, KeyTablesForOxf.NAME_BY_AREA);
            assertThat(keyTable.getName()).isEqualTo(KeyTablesForOxf.NAME_BY_AREA);
            assertThat(keyTable.getFoundationValueType()).isEqualTo(KeyTablesForOxf.BUDGET_FOUNDATION_VALUE_TYPE);
            assertThat(keyTable.getKeyValueMethod()).isEqualTo(KeyTablesForOxf.BUDGET_KEY_VALUE_METHOD);
            assertThat(keyTable.isValidForKeyValues()).isEqualTo(true);

            //when
            keyTable.changeName("something else");
            keyTable.changeFoundationValueType(FoundationValueType.COUNT);
            keyTable.changeKeyValueMethod(KeyValueMethod.PERCENT);

            //then
            assertThat(keyTable.getName()).isEqualTo("something else");
            assertThat(keyTable.getFoundationValueType()).isEqualTo(FoundationValueType.COUNT);
            assertThat(keyTable.getKeyValueMethod()).isEqualTo(KeyValueMethod.PERCENT);
            //due to changing foundation value and key value method
            assertThat(keyTable.isValidForKeyValues()).isEqualTo(false);
        }

    }

    public static class GenerateKeyItemsTest extends KeyTableIntegrationTest {

        KeyTable keyTableByArea;

        @Test
        public void whenSetUp() throws Exception {

            //given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            keyTableByArea = keyTableRepository.findByBudgetAndName(budget, KeyTablesForOxf.NAME_BY_AREA);

            //when
            wrap(keyTableByArea).generateItems();

            //then
            assertThat(keyItemRepository.findByKeyTableAndUnit(keyTableByArea, unitRepository.findUnitByReference("OXF-001")).getValue()).isEqualTo(new BigDecimal("3.077"));
            assertThat(keyItemRepository.findByKeyTableAndUnit(keyTableByArea, unitRepository.findUnitByReference("OXF-002")).getValue()).isEqualTo(new BigDecimal("6.154"));
        }

        Unit unitWithAreaNull;

        @Test
        public void whenSetUpWithNullValues() throws Exception {

            //given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            keyTableByArea = keyTableRepository.findByBudgetAndName(budget, KeyTablesForOxf.NAME_BY_AREA);
            unitWithAreaNull = unitRepository.findUnitByReference("OXF-001");
            unitWithAreaNull.setArea(null);

            //when
            wrap(keyTableByArea).generateItems();

            //then
            assertThat(keyItemRepository.findByKeyTableAndUnit(keyTableByArea, unitWithAreaNull).getValue()).isEqualTo(new BigDecimal("0.000"));
            assertThat(keyItemRepository.findByKeyTableAndUnit(keyTableByArea, unitRepository.findUnitByReference("OXF-002")).getValue()).isEqualTo(new BigDecimal("6.173"));
        }

        Unit unitNotIncluded;
        Unit unitNotIncludedWithEndDateOnly;
        Unit unitNotIncludedWithStartDateOnly;
        Unit unitIncluded;
        Unit unitIncludedWithEndDateOnly;
        Unit unitIncludedWithStartDateOnly;
        Unit unitIncludedWithoutStartAndEndDate;

        @Test
        public void whenSetUpWithUnitsNotInKeyTablePeriod() throws Exception {

            //given
            Property property = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);
            Budget budget = budgetRepository.findByPropertyAndStartDate(property, BudgetsForOxf.BUDGET_2015_START_DATE);
            keyTableByArea = keyTableRepository.findByBudgetAndName(budget, KeyTablesForOxf.NAME_BY_AREA);

            //when
            unitNotIncludedWithEndDateOnly = unitRepository.findUnitByReference("OXF-001");
            unitNotIncludedWithEndDateOnly.setStartDate(null);
            unitNotIncludedWithEndDateOnly.setEndDate(new LocalDate(2015, 12, 30));
            unitNotIncluded = unitRepository.findUnitByReference("OXF-002");
            unitNotIncluded.setStartDate(new LocalDate(2015, 01, 01));
            unitNotIncluded.setEndDate(new LocalDate(2015, 12, 30));
            unitNotIncludedWithStartDateOnly = unitRepository.findUnitByReference("OXF-003");
            unitNotIncludedWithStartDateOnly.setStartDate(new LocalDate(2015, 01, 02));
            unitNotIncludedWithStartDateOnly.setEndDate(null);

            unitIncluded = unitRepository.findUnitByReference("OXF-004");
            unitIncluded.setStartDate(new LocalDate(2015, 01, 01));
            unitIncluded.setEndDate(new LocalDate(2015, 12, 31));
            unitIncludedWithEndDateOnly = unitRepository.findUnitByReference("OXF-005");
            unitIncludedWithEndDateOnly.setStartDate(null);
            unitIncludedWithEndDateOnly.setEndDate(new LocalDate(2015, 12, 31));
            unitIncludedWithStartDateOnly = unitRepository.findUnitByReference("OXF-006");
            unitIncludedWithStartDateOnly.setStartDate(new LocalDate(2015, 01, 01));
            unitIncludedWithStartDateOnly.setEndDate(null);
            unitIncludedWithoutStartAndEndDate = unitRepository.findUnitByReference("OXF-007");
            unitIncludedWithoutStartAndEndDate.setStartDate(null);
            unitIncludedWithoutStartAndEndDate.setEndDate(null);

            wrap(keyTableByArea).generateItems();

            //then
            assertThat(keyItemRepository.findByKeyTableAndUnit(keyTableByArea, unitNotIncludedWithEndDateOnly)).isEqualTo(null);
            assertThat(keyItemRepository.findByKeyTableAndUnit(keyTableByArea, unitNotIncluded)).isEqualTo(null);
            assertThat(keyItemRepository.findByKeyTableAndUnit(keyTableByArea, unitNotIncludedWithStartDateOnly)).isEqualTo(null);
            assertThat(keyItemRepository.findByKeyTableAndUnit(keyTableByArea, unitIncluded).getValue()).isEqualTo("12.539");
            assertThat(keyItemRepository.findByKeyTableAndUnit(keyTableByArea, unitIncludedWithEndDateOnly).getValue()).isEqualTo("15.674");
            assertThat(keyItemRepository.findByKeyTableAndUnit(keyTableByArea, unitIncludedWithStartDateOnly).getValue()).isEqualTo("18.808");
            assertThat(keyItemRepository.findByKeyTableAndUnit(keyTableByArea, unitIncludedWithoutStartAndEndDate).getValue()).isEqualTo("21.944");

        }

    }

}