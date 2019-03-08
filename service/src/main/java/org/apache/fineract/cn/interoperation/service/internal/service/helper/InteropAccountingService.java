/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
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
package org.apache.fineract.cn.interoperation.service.internal.service.helper;

import com.google.common.collect.Lists;
import org.apache.fineract.cn.accounting.api.v1.client.AccountNotFoundException;
import org.apache.fineract.cn.accounting.api.v1.client.LedgerManager;
import org.apache.fineract.cn.accounting.api.v1.client.LedgerNotFoundException;
import org.apache.fineract.cn.accounting.api.v1.domain.Account;
import org.apache.fineract.cn.accounting.api.v1.domain.AccountEntry;
import org.apache.fineract.cn.accounting.api.v1.domain.AccountPage;
import org.apache.fineract.cn.accounting.api.v1.domain.JournalEntry;
import org.apache.fineract.cn.accounting.api.v1.domain.Ledger;
import org.apache.fineract.cn.interoperation.service.ServiceConstants;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@Service
public class InteropAccountingService {

    private Logger logger;
    private LedgerManager ledgerManager;

    @Autowired
    public InteropAccountingService(@Qualifier(ServiceConstants.LOGGER_NAME) Logger logger,
                                    LedgerManager ledgerManager) {
        super();
        this.logger = logger;
        this.ledgerManager = ledgerManager;
    }

    public void createAccount(String equityLedger,
                              String productName,
                              String customer,
                              String accountNumber,
                              String alternativeAccountNumber,
                              Double balance) {
        try {
            Ledger ledger = ledgerManager.findLedger(equityLedger);
            Account account = new Account();
            account.setIdentifier(accountNumber);
            account.setType(ledger.getType());
            account.setLedger(equityLedger);
            account.setName(productName);
            account.setHolders(new HashSet<>(Lists.newArrayList(customer)));
            account.setBalance(balance != null ? balance : 0.00D);
            account.setAlternativeAccountNumber(alternativeAccountNumber);

            ledgerManager.createAccount(account);
        } catch (LedgerNotFoundException lnfex) {
            throw ServiceException.notFound("Ledger {0} not found.", equityLedger);
        }
    }

    public List<Account> fetchAccounts(boolean includeClosed, String term, String type, boolean includeCustomerAccounts,
                                     Integer pageIndex, Integer size, String sortColumn, String sortDirection) {
        return ledgerManager.fetchAccounts(includeClosed, term, type, includeCustomerAccounts, pageIndex, size, sortColumn, sortDirection).getAccounts();
    }

    public Account findAccount(final String accountNumber) {
        try {
            return this.ledgerManager.findAccount(accountNumber);
        } catch (final AccountNotFoundException anfex) {
            final AccountPage accountPage = this.ledgerManager.fetchAccounts(true, accountNumber, null, true,
                    0, 10, null, null);

            return accountPage.getAccounts()
                    .stream()
                    .filter(account -> account.getAlternativeAccountNumber().equals(accountNumber))
                    .findFirst()
                    .orElseThrow(() -> ServiceException.notFound("Account {0} not found.", accountNumber));
        }
    }

    public void modifyAccount(Account account) {
        ledgerManager.modifyAccount(account.getIdentifier(), account);
    }

    public List<AccountEntry> fetchAccountEntries(String identifier, String dateRange, String direction) {
        return ledgerManager
                .fetchAccountEntries(identifier, dateRange, null, 0, 1, "transactionDate", direction)
                .getAccountEntries();
    }

    public List<JournalEntry> fetchJournalEntries(final String dateRange, final String accountNumber, final BigDecimal amount) {
        return ledgerManager.fetchJournalEntries(dateRange, accountNumber, amount);
    }

    public JournalEntry findJournalEntry(@NotNull String transactionIdentifier) {
        return ledgerManager.findJournalEntry(transactionIdentifier);
    }

    public void createJournalEntry(@NotNull JournalEntry journalEntry) {
        ledgerManager.createJournalEntry(journalEntry);
    }
}
