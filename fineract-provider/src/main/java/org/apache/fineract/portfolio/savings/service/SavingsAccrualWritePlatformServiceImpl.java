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
package org.apache.fineract.portfolio.savings.service;

import java.time.LocalDate;
import java.util.Collection;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargePaidBy;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavingsAccrualWritePlatformServiceImpl implements SavingsAccrualWritePlatformService {

    @Transactional
    @Override
    public void addAccrualAccounting(Long savingsId) {

    }

    @Override
    public boolean isChargeToBeRecognizedAsAccrual(final Collection<Long> chargeIds, final SavingsAccountCharge savingsAccountCharge) {
        if (chargeIds.isEmpty()) {
            return false;
        }
        return chargeIds.contains(savingsAccountCharge.getCharge().getId());
    }

    @Transactional
    @Override
    public SavingsAccountTransaction addSavingsChargeAccrualTransaction(SavingsAccount savingsAccount,
            SavingsAccountCharge savingsAccountCharge, LocalDate transactionDate) {
        final MonetaryCurrency currency = savingsAccount.getCurrency();
        final Money chargeAmount = savingsAccountCharge.getAmount(currency);
        SavingsAccountTransaction savingsAccountTransaction = SavingsAccountTransaction.accrual(savingsAccount, savingsAccount.office(),
                transactionDate, chargeAmount, false);
        final SavingsAccountChargePaidBy chargePaidBy = SavingsAccountChargePaidBy.instance(savingsAccountTransaction, savingsAccountCharge,
                savingsAccountTransaction.getAmount(currency).getAmount());
        savingsAccountTransaction.getSavingsAccountChargesPaid().add(chargePaidBy);

        savingsAccount.addTransaction(savingsAccountTransaction);
        return savingsAccountTransaction;
    }

}
