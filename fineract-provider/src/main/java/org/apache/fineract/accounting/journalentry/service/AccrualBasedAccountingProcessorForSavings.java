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
package org.apache.fineract.accounting.journalentry.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.journalentry.data.ChargePaymentDTO;
import org.apache.fineract.accounting.journalentry.data.SavingsDTO;
import org.apache.fineract.accounting.journalentry.data.SavingsTransactionDTO;
import org.apache.fineract.organisation.office.domain.Office;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccrualBasedAccountingProcessorForSavings implements AccountingProcessorForSavings {

    private final AccountingProcessorHelper helper;

    @Override
    public void createJournalEntriesForSavings(final SavingsDTO savingsDTO) {
        final GLClosure latestGLClosure = this.helper.getLatestClosureByBranch(savingsDTO.getOfficeId());
        final Long savingsProductId = savingsDTO.getSavingsProductId();
        final Long savingsId = savingsDTO.getSavingsId();
        final String currencyCode = savingsDTO.getCurrencyCode();
        for (final SavingsTransactionDTO savingsTransactionDTO : savingsDTO.getNewSavingsTransactions()) {
            final LocalDate transactionDate = savingsTransactionDTO.getTransactionDate();
            final String transactionId = savingsTransactionDTO.getTransactionId();
            final Office office = this.helper.getOfficeById(savingsTransactionDTO.getOfficeId());
            final Long paymentTypeId = savingsTransactionDTO.getPaymentTypeId();
            final boolean isReversal = savingsTransactionDTO.isReversed();
            final BigDecimal amount = savingsTransactionDTO.getAmount();
            final BigDecimal overdraftAmount = savingsTransactionDTO.getOverdraftAmount();
            final List<ChargePaymentDTO> feePayments = savingsTransactionDTO.getFeePayments();
            final List<ChargePaymentDTO> penaltyPayments = savingsTransactionDTO.getPenaltyPayments();

            this.helper.checkForBranchClosures(latestGLClosure, transactionDate);

            if (savingsTransactionDTO.getTransactionType().isWithdrawal() && savingsTransactionDTO.isOverdraftTransaction()) {
                boolean isPositive = amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) > 0;
                if (savingsTransactionDTO.isAccountTransfer()) {
                    log.error("debug");
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                            FinancialActivity.LIABILITY_TRANSFER.getValue(), savingsProductId, paymentTypeId, savingsId, transactionId,
                            transactionDate, overdraftAmount, isReversal);
                    if (isPositive) {
                        log.error("debug");
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(), FinancialActivity.LIABILITY_TRANSFER.getValue(),
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate,
                                amount.subtract(overdraftAmount), isReversal);
                    }
                } else {
                    log.error("debug");
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                            AccrualAccountsForSavings.SAVINGS_REFERENCE.getValue(), savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, overdraftAmount, isReversal);
                    if (isPositive) {
                        log.error("debug");
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(),
                                AccrualAccountsForSavings.SAVINGS_REFERENCE.getValue(), savingsProductId, paymentTypeId, savingsId,
                                transactionId, transactionDate, amount.subtract(overdraftAmount), isReversal);
                    }
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isDeposit() && savingsTransactionDTO.isOverdraftTransaction()) {
                log.error("debug");
                boolean isPositive = amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) > 0;
                if (savingsTransactionDTO.isAccountTransfer()) {
                    log.error("debug");
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            FinancialActivity.LIABILITY_TRANSFER.getValue(),
                            AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, overdraftAmount, isReversal);
                    if (isPositive) {
                        log.error("debug");
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                FinancialActivity.LIABILITY_TRANSFER.getValue(), AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(),
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate,
                                amount.subtract(overdraftAmount), isReversal);
                    }
                } else {
                    log.error("debug");
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            AccrualAccountsForSavings.SAVINGS_REFERENCE.getValue(),
                            AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, overdraftAmount, isReversal);
                    if (isPositive) {
                        log.error("debug");
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                AccrualAccountsForSavings.SAVINGS_REFERENCE.getValue(),
                                AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                                transactionId, transactionDate, amount.subtract(overdraftAmount), isReversal);
                    }
                }
            }
            else if (savingsTransactionDTO.getTransactionType().isDeposit()) {
                log.error("Handle Deposits and reversals of deposits");
                if (savingsTransactionDTO.isAccountTransfer()) {
                    log.error("debug");
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            FinancialActivity.LIABILITY_TRANSFER.getValue(), AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                } else {
                    log.error("debug");
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            AccrualAccountsForSavings.SAVINGS_REFERENCE.getValue(), AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                }
            }
            else if (savingsTransactionDTO.getTransactionType().isDividendPayout()) {
                log.error("Handle Deposits and reversals of Dividend pay outs");
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        FinancialActivity.PAYABLE_DIVIDENDS.getValue(), AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }
            else if (savingsTransactionDTO.getTransactionType().isWithdrawal()) {
                log.error("Handle withdrawals and reversals of withdrawals ");
                if (savingsTransactionDTO.isAccountTransfer()) {
                    log.error("debug");
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(), FinancialActivity.LIABILITY_TRANSFER.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                } else {
                    log.error("debug");
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(), AccrualAccountsForSavings.SAVINGS_REFERENCE.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isEscheat()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(), AccrualAccountsForSavings.ESCHEAT_LIABILITY.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }
            else if (savingsTransactionDTO.getTransactionType().isInterestPosting() && savingsTransactionDTO.isOverdraftTransaction()) {
                log.error("Handle Interest Applications and reversals of Interest Applications");
                boolean isPositive = amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) > 0;
                log.error("Post journal entry if earned interest amount is greater than zero");
                if (savingsTransactionDTO.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    log.error("debug");
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            AccrualAccountsForSavings.INTEREST_ON_SAVINGS.getValue(),
                            AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, overdraftAmount, isReversal);
                    if (isPositive) {
                        log.error("debug");
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                AccrualAccountsForSavings.INTEREST_PAYABLE.getValue(), AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(),
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate,
                                amount.subtract(overdraftAmount), isReversal);
                    }
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isInterestPosting()) {
                log.error("Post journal entry if earned interest amount is greater than zero");
                if (savingsTransactionDTO.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    log.error("debug");
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            AccrualAccountsForSavings.INTEREST_PAYABLE.getValue(), AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isAccrual()) {
                log.error("Post journal entry for Accrual Recognition"); 
                if (savingsTransactionDTO.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    if (feePayments.size() > 0 || penaltyPayments.size() > 0) {
                        log.error("if (feePayments.size() > 0 || penaltyPayments.size() > 0)" );
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                AccrualAccountsForSavings.FEES_RECEIVABLE.getValue(), AccrualAccountsForSavings.INCOME_FROM_FEES.getValue(),
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                    } 
                    else {
                        log.error("debug");
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                AccrualAccountsForSavings.INTEREST_ON_SAVINGS.getValue(),
                                AccrualAccountsForSavings.INTEREST_PAYABLE.getValue(), savingsProductId, paymentTypeId, savingsId,
                                transactionId, transactionDate, amount, isReversal);
                    }
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isWithholdTax()) {
                log.error("debug");
                log.error("else if (savingsTransactionDTO.getTransactionType().isWithholdTax()) " );
                this.helper.createAccrualBasedJournalEntriesAndReversalsForSavingsTax(office, currencyCode,
                        AccrualAccountsForSavings.SAVINGS_CONTROL, AccrualAccountsForSavings.SAVINGS_REFERENCE, savingsProductId,
                        paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal,
                        savingsTransactionDTO.getTaxPayments());
            }
            else if (savingsTransactionDTO.getTransactionType().isFeeDeduction() && savingsTransactionDTO.isOverdraftTransaction()) {
                log.error("Handle Fees Deductions and reversals of Fees Deductions");
                boolean isPositive = amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) > 0;
                log.error("Is the Charge a penalty?");
                if (penaltyPayments.size() > 0) {
                    log.error("debug");
                    this.helper.createAccrualBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL, AccrualAccountsForSavings.INCOME_FROM_PENALTIES,
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, overdraftAmount, isReversal,
                            penaltyPayments);
                    if (isPositive) {
                        log.error("debug");
                        final ChargePaymentDTO chargePaymentDTO = penaltyPayments.get(0);
                        AccrualAccountsForSavings accountTypeToBeDebited = AccrualAccountsForSavings.SAVINGS_CONTROL;
                        if (chargePaymentDTO.isAccrualRecognized()) {
                            log.error("debug");
                            accountTypeToBeDebited = AccrualAccountsForSavings.FEES_RECEIVABLE;
                        }
                        log.error("debug");
                        this.helper.createAccrualBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                                accountTypeToBeDebited, AccrualAccountsForSavings.INCOME_FROM_PENALTIES, savingsProductId, paymentTypeId,
                                savingsId, transactionId, transactionDate, amount.subtract(overdraftAmount), isReversal, penaltyPayments);
                    }
                } else {
                    log.error("debug");
                    this.helper.createAccrualBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL, AccrualAccountsForSavings.INCOME_FROM_FEES,
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, overdraftAmount, isReversal,
                            feePayments);
                    if (isPositive) {
                        log.error("debug");
                        final ChargePaymentDTO chargePaymentDTO = feePayments.get(0);
                        AccrualAccountsForSavings accountTypeToBeDebited = AccrualAccountsForSavings.SAVINGS_CONTROL;
                        if (chargePaymentDTO.isAccrualRecognized()) {
                            log.error("debug");
                            accountTypeToBeDebited = AccrualAccountsForSavings.FEES_RECEIVABLE;
                        }
                        log.error("debug");
                        this.helper.createAccrualBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                                accountTypeToBeDebited, AccrualAccountsForSavings.INCOME_FROM_FEES, savingsProductId, paymentTypeId,
                                savingsId, transactionId, transactionDate, amount.subtract(overdraftAmount), isReversal, feePayments);
                    }
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isFeeDeduction()) {
                log.error("Is the Charge a penalty?");
                if (penaltyPayments.size() > 0) {
                    log.error("debug");
                    final ChargePaymentDTO chargePaymentDTO = penaltyPayments.get(0);
                    AccrualAccountsForSavings accountTypeToBeCredited = AccrualAccountsForSavings.INCOME_FROM_PENALTIES;
                    if (chargePaymentDTO.isAccrualRecognized()) {
                        log.error("debug");
                        accountTypeToBeCredited = AccrualAccountsForSavings.FEES_RECEIVABLE;
                    }
                    log.error("debug");
                    this.helper.createAccrualBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            AccrualAccountsForSavings.SAVINGS_CONTROL, accountTypeToBeCredited, savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, amount, isReversal, penaltyPayments);
                } else {
                    log.error("debug");
                    final ChargePaymentDTO chargePaymentDTO = feePayments.get(0);
                    AccrualAccountsForSavings accountTypeToBeCredited = AccrualAccountsForSavings.INCOME_FROM_PENALTIES;
                    if (chargePaymentDTO.isAccrualRecognized()) {
                        log.error("debug");
                        accountTypeToBeCredited = AccrualAccountsForSavings.FEES_RECEIVABLE;
                    }
                    log.error("debug");
                    this.helper.createAccrualBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            AccrualAccountsForSavings.SAVINGS_CONTROL, accountTypeToBeCredited, savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, amount, isReversal, feePayments);
                }
            }
            else if (savingsTransactionDTO.getTransactionType().isInitiateTransfer()) {
                log.error("Handle Transfers proposal");
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(), AccrualAccountsForSavings.TRANSFERS_SUSPENSE.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }
            else if (savingsTransactionDTO.getTransactionType().isWithdrawTransfer()
                    || savingsTransactionDTO.getTransactionType().isApproveTransfer()) {
                log.error("Handle Transfer Withdrawal or Acceptance");
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        AccrualAccountsForSavings.TRANSFERS_SUSPENSE.getValue(), AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }
            else if (savingsTransactionDTO.getTransactionType().isOverdraftInterest()) {
                log.error("overdraft");
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        AccrualAccountsForSavings.SAVINGS_REFERENCE.getValue(), AccrualAccountsForSavings.INCOME_FROM_INTEREST.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            } else if (savingsTransactionDTO.getTransactionType().isWrittenoff()) {
                log.error("debug");
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        AccrualAccountsForSavings.LOSSES_WRITTEN_OFF.getValue(),
                        AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                        transactionId, transactionDate, amount, isReversal);
            } else if (savingsTransactionDTO.getTransactionType().isOverdraftFee()) {
                log.error("debug");
                this.helper.createAccrualBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                        AccrualAccountsForSavings.SAVINGS_REFERENCE, AccrualAccountsForSavings.INCOME_FROM_FEES, savingsProductId,
                        paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal, feePayments);
            }
        }
    }
}
