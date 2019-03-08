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

import org.apache.fineract.cn.deposit.api.v1.client.DepositAccountManager;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.Action;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.Charge;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.DividendDistribution;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.ProductDefinition;
import org.apache.fineract.cn.deposit.api.v1.instance.domain.AvailableTransactionType;
import org.apache.fineract.cn.deposit.api.v1.instance.domain.ProductInstance;
import org.apache.fineract.cn.interoperation.api.v1.domain.TransactionType;
import org.apache.fineract.cn.interoperation.service.ServiceConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InteropDepositService {

    private Logger logger;
    private DepositAccountManager depositAccountManager;

    @Autowired
    public InteropDepositService(@Qualifier(ServiceConstants.LOGGER_NAME) Logger logger,
                                 DepositAccountManager depositAccountManager) {
        super();
        this.logger = logger;
        this.depositAccountManager = depositAccountManager;
    }

    public List<Charge> getWithdrawCharges(String accountIdentifier) {
        return getCharges(accountIdentifier, TransactionType.CURRENCY_WITHDRAWAL);
    }

    public List<Charge> getDepositCharges(String accountIdentifier) {
        return getCharges(accountIdentifier, TransactionType.CURRENCY_DEPOSIT);
    }

    public List<Charge> getCharges(String accountIdentifier, TransactionType transactionType) {
        List<Action> actions = depositAccountManager.fetchActions();

        List<String> actionIds = actions
                .stream()
                .filter(action -> action.getTransactionType().equals(transactionType.getCode()))
                .map(Action::getIdentifier)
                .collect(Collectors.toList());

        ProductInstance productInstance = depositAccountManager.findProductInstance(accountIdentifier);
        ProductDefinition productDefinition = depositAccountManager.findProductDefinition(productInstance.getProductIdentifier());

        return productDefinition.getCharges()
                .stream()
                .filter(charge -> actionIds.contains(charge.getActionIdentifier()))
                .collect(Collectors.toList());
    }

    public void createAction(@NotNull Action action) {
        depositAccountManager.create(action);
    }

    public List<Action> fetchActions() {
        return depositAccountManager.fetchActions();
    }

    public void createProductDefinition(@NotNull ProductDefinition productDefinition) {
        depositAccountManager.create(productDefinition);
    }

    public List<ProductDefinition> fetchProductDefinitions() {
        return depositAccountManager.fetchProductDefinitions();
    }

    public ProductDefinition findProductDefinition(@NotNull String identifier) {
        return depositAccountManager.findProductDefinition(identifier);
    }

    public List<ProductInstance> findProductInstances(@NotNull String identifier) {
        return depositAccountManager.findProductInstances(identifier);
    }

    public void createProductInstance(@NotNull ProductInstance productInstance) {
        depositAccountManager.create(productInstance);
    }

    public List<ProductInstance> fetchProductInstances(@NotNull String customer) {
        return depositAccountManager.fetchProductInstances(customer);
    }

    public Set<AvailableTransactionType> fetchPossibleTransactionTypes(String customer) {
        return depositAccountManager.fetchPossibleTransactionTypes(customer);
    }

    public ProductInstance findProductInstance(@NotNull String accountIdentifier) {
        return depositAccountManager.findProductInstance(accountIdentifier);
    }

    public void dividendDistribution(@NotNull String identifier, @NotNull DividendDistribution distribution) {
        depositAccountManager.dividendDistribution(identifier, distribution);
    }

    public List<DividendDistribution> fetchDividendDistributions(@NotNull String identifier) {
        return depositAccountManager.fetchDividendDistributions(identifier);
    }
}

