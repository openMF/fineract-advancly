/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.accounting.glaccount.jobs.updatetrialbalancedetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.apache.fineract.accounting.glaccount.domain.TrialBalance;
import org.apache.fineract.accounting.glaccount.domain.TrialBalanceRepository;
import org.apache.fineract.accounting.glaccount.domain.TrialBalanceRepositoryWrapper;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSourceService;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSourceServiceFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
class UpdateTrialBalanceDetailsTaskletTest {

    @Mock
    private RoutingDataSourceServiceFactory dataSourceServiceFactory;

    @Mock
    private RoutingDataSourceService routingDataSourceService;

    @Mock
    private DataSource dataSource;

    @Mock
    private TrialBalanceRepositoryWrapper trialBalanceRepositoryWrapper;

    @Mock
    private TrialBalanceRepository trialBalanceRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @InjectMocks
    private UpdateTrialBalanceDetailsTasklet tasklet;

    private MockedStatic<DateUtils> mockedDateUtils;

    @BeforeEach
    void setUp() {
        mockedDateUtils = Mockito.mockStatic(DateUtils.class);
        FineractPlatformTenant tenant = new FineractPlatformTenant(1L, "default", "Default Tenant", "UTC", null);
        ThreadLocalContextUtil.setTenant(tenant);
    }

    @AfterEach
    void tearDown() {
        if (mockedDateUtils != null) {
            mockedDateUtils.close();
        }
        ThreadLocalContextUtil.clearTenant();
    }

    @Test
    void testExecuteHandlesOffsetDateTimeSuccessfully() throws Exception {
        LocalDate businessDate = LocalDate.of(2026, 11, 15);
        LocalDate entryDate = LocalDate.of(2026, 11, 10);
        LocalDate baselineDate = LocalDate.of(2026, 11, 1);
        OffsetDateTime createdDateTime = OffsetDateTime.of(2026, 11, 10, 14, 30, 0, 0, ZoneOffset.UTC);

        mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(businessDate);
        mockedDateUtils.when(() -> DateUtils.getExactDifferenceInDays(entryDate, businessDate)).thenReturn(5);

        when(dataSourceServiceFactory.determineDataSourceService()).thenReturn(routingDataSourceService);
        when(routingDataSourceService.retrieveDataSource()).thenReturn(dataSource);

        when(trialBalanceRepository.findMaxCreatedDate()).thenReturn(baselineDate);
        when(journalEntryRepository.findTransactionDatesAfter(baselineDate)).thenReturn(List.of(entryDate));

        Object[] row = new Object[] { 1L, 100L, BigDecimal.valueOf(500.00), entryDate, createdDateTime, BigDecimal.valueOf(1500.00) };
        when(journalEntryRepository.findTrialBalanceLinesForDate(entryDate)).thenReturn(Collections.singletonList(row));
        when(trialBalanceRepository.findDistinctOfficeIdsWithNullClosingBalance()).thenReturn(List.of());

        StepContribution stepContribution = mock(StepContribution.class);
        ChunkContext chunkContext = mock(ChunkContext.class);

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TrialBalance>> captor = ArgumentCaptor.forClass(List.class);
        verify(trialBalanceRepositoryWrapper).save(captor.capture());

        List<TrialBalance> savedBalances = captor.getValue();
        assertEquals(1, savedBalances.size());
        TrialBalance tb = savedBalances.get(0);
        assertEquals(1L, tb.getOfficeId());
        assertEquals(100L, tb.getGlAccountId());
        assertEquals(BigDecimal.valueOf(500.00), tb.getAmount());
        assertEquals(entryDate, tb.getEntryDate());
        assertEquals(LocalDate.of(2026, 11, 10), tb.getTransactionDate());
        assertEquals(BigDecimal.valueOf(1500.00), tb.getClosingBalance());
    }

    @Test
    void testExecuteHandlesLocalDateSuccessfully() throws Exception {
        LocalDate businessDate = LocalDate.of(2026, 11, 15);
        LocalDate entryDate = LocalDate.of(2026, 11, 10);
        LocalDate baselineDate = LocalDate.of(2026, 11, 1);
        LocalDate createdDate = LocalDate.of(2026, 11, 10);

        mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(businessDate);
        mockedDateUtils.when(() -> DateUtils.getExactDifferenceInDays(entryDate, businessDate)).thenReturn(5);

        when(dataSourceServiceFactory.determineDataSourceService()).thenReturn(routingDataSourceService);
        when(routingDataSourceService.retrieveDataSource()).thenReturn(dataSource);

        when(trialBalanceRepository.findMaxCreatedDate()).thenReturn(baselineDate);
        when(journalEntryRepository.findTransactionDatesAfter(baselineDate)).thenReturn(List.of(entryDate));

        Object[] row = new Object[] { 2L, 200L, BigDecimal.valueOf(250.00), entryDate, createdDate, BigDecimal.valueOf(750.00) };
        when(journalEntryRepository.findTrialBalanceLinesForDate(entryDate)).thenReturn(Collections.singletonList(row));
        when(trialBalanceRepository.findDistinctOfficeIdsWithNullClosingBalance()).thenReturn(List.of());

        StepContribution stepContribution = mock(StepContribution.class);
        ChunkContext chunkContext = mock(ChunkContext.class);

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TrialBalance>> captor = ArgumentCaptor.forClass(List.class);
        verify(trialBalanceRepositoryWrapper).save(captor.capture());

        List<TrialBalance> savedBalances = captor.getValue();
        assertEquals(1, savedBalances.size());
        assertEquals(createdDate, savedBalances.get(0).getTransactionDate());
    }
}
