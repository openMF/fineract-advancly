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
package org.apache.fineract.portfolio.savings.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SavingsAccountCreditsBlockedException extends AbstractPlatformDomainRuleException {

    public SavingsAccountCreditsBlockedException(final Long accountId) {
        super("error.msg.savings.account.credit.transaction.not.allowed",
                "Any Credit transactions to " + accountId + " is not allowed, since the account is blocked for credits", accountId);
    }

    public SavingsAccountCreditsBlockedException(final String accountNo) {
        super("error.msg.savings.account.credit.transaction.not.allowed",
                "Any Credit transactions to " + accountNo + " is not allowed, since the account is blocked for credits", accountNo);
    }
}
