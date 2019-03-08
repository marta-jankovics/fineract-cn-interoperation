/*
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
package org.apache.fineract.cn.interoperation.service.internal.service;

import org.apache.fineract.cn.accounting.api.v1.domain.Account;
import org.apache.fineract.cn.accounting.api.v1.domain.AccountType;
import org.apache.fineract.cn.accounting.api.v1.domain.Creditor;
import org.apache.fineract.cn.accounting.api.v1.domain.Debtor;
import org.apache.fineract.cn.accounting.api.v1.domain.JournalEntry;
import org.apache.fineract.cn.api.util.UserContextHolder;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.Charge;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.Currency;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.ProductDefinition;
import org.apache.fineract.cn.deposit.api.v1.instance.domain.ProductInstance;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropActionState;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropActionType;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropIdentifierType;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropState;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropStateMachine;
import org.apache.fineract.cn.interoperation.api.v1.domain.TransactionType;
import org.apache.fineract.cn.interoperation.api.v1.domain.data.*;
import org.apache.fineract.cn.interoperation.api.v1.util.MathUtil;
import org.apache.fineract.cn.interoperation.service.ServiceConstants;
import org.apache.fineract.cn.interoperation.service.internal.repository.InteropActionEntity;
import org.apache.fineract.cn.interoperation.service.internal.repository.InteropActionRepository;
import org.apache.fineract.cn.interoperation.service.internal.repository.InteropIdentifierEntity;
import org.apache.fineract.cn.interoperation.service.internal.repository.InteropIdentifierRepository;
import org.apache.fineract.cn.interoperation.service.internal.repository.InteropTransactionEntity;
import org.apache.fineract.cn.interoperation.service.internal.repository.InteropTransactionRepository;
import org.apache.fineract.cn.interoperation.service.internal.service.helper.InteropAccountingService;
import org.apache.fineract.cn.interoperation.service.internal.service.helper.InteropDepositService;
import org.apache.fineract.cn.lang.DateConverter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

//import static org.apache.fineract.cn.interoperation.api.v1.util.InteroperationUtil.DEFAULT_ROUTING_CODE;

@Service
public class InteropService {

    public static final String ACCOUNT_NAME_NOSTRO = "Interoperation NOSTRO";

    private final Logger logger;

    private final InteropIdentifierRepository identifierRepository;
    private final InteropTransactionRepository transactionRepository;
    private final InteropActionRepository actionRepository;

    private final InteropDepositService depositService;
    private final InteropAccountingService accountingService;


    @Autowired
    public InteropService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                          InteropIdentifierRepository interopIdentifierRepository,
                          InteropTransactionRepository interopTransactionRepository,
                          InteropActionRepository interopActionRepository,
                          InteropDepositService interopDepositService,
                          InteropAccountingService interopAccountingService) {
        this.logger = logger;
        this.identifierRepository = interopIdentifierRepository;
        this.transactionRepository = interopTransactionRepository;
        this.actionRepository = interopActionRepository;
        this.depositService = interopDepositService;
        this.accountingService = interopAccountingService;
    }

    @NotNull
    public InteropIdentifierData getAccountByIdentifier(@NotNull InteropIdentifierType idType, @NotNull String idValue, String subIdOrType) {
        InteropIdentifierEntity identifier = findIdentifier(idType, idValue, subIdOrType);
        if (identifier == null)
            throw new UnsupportedOperationException("Account not found for identifier " + idType + "/" + idValue + (subIdOrType == null ? "" : ("/" + subIdOrType)));

        return new InteropIdentifierData(identifier.getCustomerAccountIdentifier());
    }

    @NotNull
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropIdentifierData registerAccountIdentifier(@NotNull InteropIdentifierCommand request) {
        //TODO: error handling
        String accountId = request.getAccountId();
        validateAndGetAccount(accountId);

        String createdBy = getLoginUser();
        LocalDateTime createdOn = getNow();

        InteropIdentifierEntity identifier = new InteropIdentifierEntity(accountId, request.getIdType(), request.getIdValue(),
                request.getSubIdOrType(), createdBy, createdOn);

        identifierRepository.save(identifier);

        return new InteropIdentifierData(accountId);
    }

    @NotNull
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropIdentifierData deleteAccountIdentifier(@NotNull InteropIdentifierDeleteCommand request) {
        InteropIdentifierType idType = request.getIdType();
        String idValue = request.getIdValue();
        String subIdOrType = request.getSubIdOrType();

        InteropIdentifierEntity identifier = findIdentifier(idType, idValue, subIdOrType);
        if (identifier == null)
            throw new UnsupportedOperationException("Account not found for identifier " + idType + "/" + idValue + (subIdOrType == null ? "" : ("/" + subIdOrType)));

        String customerAccountIdentifier = identifier.getCustomerAccountIdentifier();

        identifierRepository.delete(identifier);

        return new InteropIdentifierData(customerAccountIdentifier);
    }

    public InteropTransactionRequestResponseData getTransactionRequest(@NotNull String transactionCode, @NotNull String requestCode) {
        InteropActionEntity action = validateAndGetAction(transactionCode, calcActionIdentifier(requestCode, InteropActionType.REQUEST),
                InteropActionType.REQUEST);
        return InteropTransactionRequestResponseData.build(transactionCode, action.getState(), action.getExpirationDate(), requestCode);
    }

    @NotNull
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropTransactionRequestResponseData createTransactionRequest(@NotNull InteropTransactionRequestData request) {
        // only when Payee request transaction from Payer, so here role must be always Payer
        //TODO: error handling
        AccountWrapper accountWrapper = validateAndGetAccount(request);
        //TODO: transaction expiration separated from action expiration
        InteropTransactionEntity transaction = validateAndGetTransaction(request, accountWrapper);
        InteropActionEntity action = addAction(transaction, request);

        transactionRepository.save(transaction);

        return InteropTransactionRequestResponseData.build(request.getTransactionCode(), action.getState(), action.getExpirationDate(),
                request.getExtensionList(), request.getRequestCode());
    }

    public InteropQuoteResponseData getQuote(@NotNull String transactionCode, @NotNull String quoteCode) {
        InteropActionEntity action = validateAndGetAction(transactionCode, calcActionIdentifier(quoteCode, InteropActionType.QUOTE),
                InteropActionType.QUOTE);

        Currency currency = getCurrency(action);

        return InteropQuoteResponseData.build(transactionCode, action.getState(), action.getExpirationDate(), quoteCode,
                MoneyData.build(action.getFee(), currency), MoneyData.build(action.getCommission(), currency));
    }

    @NotNull
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropQuoteResponseData createQuote(@NotNull InteropQuoteRequestData request) {
        //TODO: error handling
        AccountWrapper accountWrapper = validateAndGetAccount(request);
        //TODO: transaction expiration separated from action expiration
        InteropTransactionEntity transaction = validateAndGetTransaction(request, accountWrapper);

        TransactionType transactionType = request.getTransactionRole().getTransactionType();
        String accountId = accountWrapper.account.getIdentifier();
        List<Charge> charges = depositService.getCharges(accountId, transactionType);

        BigDecimal amount = request.getAmount().getAmount();
        BigDecimal fee = MathUtil.normalize(calcTotalCharges(charges, amount), MathUtil.DEFAULT_MATH_CONTEXT);

        Double withdrawableBalance = getWithdrawableBalance(accountWrapper.account, accountWrapper.productDefinition);
        boolean withdraw = request.getTransactionRole().isWithdraw();

        BigDecimal total = MathUtil.nullToZero(withdraw ? MathUtil.add(amount, fee) : fee);
        if (withdraw && withdrawableBalance < total.doubleValue())
            throw new UnsupportedOperationException("Account balance is not enough to pay the fee " + accountId);

        // TODO add action and set the status to failed in separated transaction
        InteropActionEntity action = addAction(transaction, request);
        action.setFee(fee);
        // TODO: extend Charge with a property that could be stored in charges

        transactionRepository.save(transaction);

        InteropQuoteResponseData build = InteropQuoteResponseData.build(request.getTransactionCode(), action.getState(),
                action.getExpirationDate(), request.getExtensionList(), request.getQuoteCode(),
                MoneyData.build(fee, accountWrapper.productDefinition.getCurrency()), null);
        return build;
    }

    public InteropTransferResponseData getTransfer(@NotNull String transactionCode, @NotNull String transferCode) {
        InteropActionEntity action = validateAndGetAction(transactionCode, calcActionIdentifier(transferCode, InteropActionType.PREPARE),
                InteropActionType.PREPARE, false);
        if (action == null)
            action = validateAndGetAction(transactionCode, calcActionIdentifier(transferCode, InteropActionType.COMMIT),
                    InteropActionType.COMMIT);

        return InteropTransferResponseData.build(transactionCode, action.getState(), action.getExpirationDate(), transferCode, action.getCreatedOn());
    }

    @NotNull
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropTransferResponseData prepareTransfer(@NotNull InteropTransferCommand request) {
        //TODO: error handling
        //TODO: ABORT
        AccountWrapper accountWrapper = validateAndGetAccount(request);

        LocalDateTime transactionDate = getNow();
        //TODO: transaction expiration separated from action expiration
        InteropTransactionEntity transaction = validateAndGetTransaction(request, accountWrapper, transactionDate, true);

        validateTransfer(request, accountWrapper);

        TransactionType transactionType = request.getTransactionRole().getTransactionType();
        List<Charge> charges = depositService.getCharges(accountWrapper.account.getIdentifier(), transactionType);

        // TODO add action and set the status to failed in separated transaction
        InteropActionEntity action = addAction(transaction, request, transactionDate);
        MoneyData fee = request.getFspFee();
        action.setFee(fee == null ? null : fee.getAmount());
        // TODO: extend Charge with a property that could be stored in charges

        prepareTransfer(request, accountWrapper, action, charges, transactionDate);

        transactionRepository.save(transaction);

        return InteropTransferResponseData.build(request.getTransferCode(), action.getState(), action.getExpirationDate(),
                request.getExtensionList(), request.getTransferCode(), transactionDate);
    }

    @NotNull
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropTransferResponseData commitTransfer(@NotNull InteropTransferCommand request) {
        //TODO: error handling
        //TODO: ABORT
        AccountWrapper accountWrapper = validateAndGetAccount(request);

        LocalDateTime transactionDate = getNow();
        //TODO: transaction expiration separated from action expiration
        InteropTransactionEntity transaction = validateAndGetTransaction(request, accountWrapper, transactionDate, true);
        transaction.setTransactionDate(transactionDate);

        validateTransfer(request, accountWrapper);

        TransactionType transactionType = request.getTransactionRole().getTransactionType();
        List<Charge> charges = depositService.getCharges(accountWrapper.account.getIdentifier(), transactionType);

        // TODO add action and set the status to failed in separated transaction
        InteropActionEntity action = addAction(transaction, request, transactionDate);
        MoneyData fee = request.getFspFee();
        action.setFee(fee == null ? null : fee.getAmount());
        // TODO: extend Charge with a property that could be stored in charges

        bookTransfer(request, accountWrapper, action, charges, transactionDate);

        transactionRepository.save(transaction);

        return InteropTransferResponseData.build(request.getTransferCode(), action.getState(), action.getExpirationDate(),
                request.getExtensionList(), request.getTransferCode(), transactionDate);
    }

    Double getWithdrawableBalance(Account account, ProductDefinition productDefinition) {
        // on-hold amount, if any, is subtracted to payable account
        return MathUtil.subtractToZero(account.getBalance(), productDefinition.getMinimumBalance());
    }

    private void prepareTransfer(@NotNull InteropTransferCommand request, @NotNull AccountWrapper accountWrapper, @NotNull InteropActionEntity action,
                                 List<Charge> charges, LocalDateTime transactionDate) {
        BigDecimal amount = request.getAmount().getAmount();
        // TODO: validate amount with quote amount
        boolean isDebit = request.getTransactionRole().isWithdraw();
        if (!isDebit)
            return;

        String prepareAccountId = action.getTransaction().getPrepareAccountIdentifier();
        Account payableAccount = prepareAccountId == null ? null : validateAndGetAccount(request, prepareAccountId);
        if (payableAccount == null) {
            logger.warn("Can not prepare transfer: Payable account was not found for " + accountWrapper.account.getIdentifier());
            return;
        }

        final JournalEntry journalEntry = createJournalEntry(action.getIdentifier(), TransactionType.CURRENCY_WITHDRAWAL.getCode(),
                DateConverter.toIsoString(transactionDate), request.getNote(), getLoginUser());

        HashSet<Debtor> debtors = new HashSet<>(1);
        HashSet<Creditor> creditors = new HashSet<>(1);

        addCreditor(accountWrapper.account.getIdentifier(), amount.doubleValue(), creditors);
        addDebtor(payableAccount.getIdentifier(), amount.doubleValue(), debtors);

        prepareCharges(request, accountWrapper, action, charges, payableAccount, debtors, creditors);

        if (debtors.isEmpty()) // must be same size as creditors
            return;

        journalEntry.setDebtors(debtors);
        journalEntry.setCreditors(creditors);
        accountingService.createJournalEntry(journalEntry);
    }

    private void prepareCharges(@NotNull InteropTransferCommand request, @NotNull AccountWrapper accountWrapper, @NotNull InteropActionEntity action,
                                @NotNull List<Charge> charges, Account payableAccount, HashSet<Debtor> debtors, HashSet<Creditor> creditors) {
        MoneyData fspFee = request.getFspFee(); // TODO compare with calculated and with quote

        BigDecimal amount = request.getAmount().getAmount();
        Currency currency = accountWrapper.productDefinition.getCurrency();

        BigDecimal total = MathUtil.normalize(calcTotalCharges(charges, amount), currency);

        if (MathUtil.isEmpty(total)) {
            return;
        }

        if (creditors == null) {
            creditors = new HashSet<>(1);
        }
        if (debtors == null) {
            debtors = new HashSet<>(charges.size());
        }
        addCreditor(accountWrapper.account.getIdentifier(), total.doubleValue(), creditors);
        addDebtor(payableAccount.getIdentifier(), total.doubleValue(), debtors);
    }

    private void bookTransfer(@NotNull InteropTransferCommand request, @NotNull AccountWrapper accountWrapper, @NotNull InteropActionEntity action,
                              List<Charge> charges, LocalDateTime transactionDate) {
        boolean isDebit = request.getTransactionRole().isWithdraw();
        String accountId = accountWrapper.account.getIdentifier();
        String message = request.getNote();
        double doubleAmount = request.getAmount().getAmount().doubleValue();

        String loginUser = getLoginUser();
        String transactionTypeCode = (isDebit ? TransactionType.CURRENCY_WITHDRAWAL : TransactionType.CURRENCY_DEPOSIT).getCode();
        String transactionDateString = DateConverter.toIsoString(transactionDate);

        InteropTransactionEntity transaction = action.getTransaction();
        Account nostroAccount = validateAndGetAccount(request, transaction.getNostroAccountIdentifier());
        Account payableAccount = null;

        double preparedAmount = 0d;
        double accountNostroAmount = doubleAmount;

        if (isDebit) {
            InteropActionEntity prepareAction = findAction(transaction, InteropActionType.PREPARE);
            if (prepareAction != null) {
                JournalEntry prepareJournal = accountingService.findJournalEntry(prepareAction.getIdentifier());
                if (prepareJournal == null)
                    throw new UnsupportedOperationException("Can not find prepare result for " + action.getActionType() +
                            "/" + request.getIdentifier());

                payableAccount = validateAndGetAccount(request, transaction.getPrepareAccountIdentifier());
                preparedAmount = prepareJournal.getDebtors().stream().mapToDouble(d -> Double.valueOf(d.getAmount())).sum();
                if (preparedAmount < doubleAmount)
                    throw new UnsupportedOperationException("Prepared amount " + preparedAmount + " is less than transfer amount " +
                            doubleAmount + " for " + request.getIdentifier());

                // now fails if prepared is not enough
                accountNostroAmount = preparedAmount >= doubleAmount ? 0d : doubleAmount - preparedAmount;

                double fromPrepareToNostroAmount = doubleAmount - accountNostroAmount;
                preparedAmount -= fromPrepareToNostroAmount;

                if (fromPrepareToNostroAmount > 0) {
                    final JournalEntry fromPrepareToNostroEntry = createJournalEntry(action.getIdentifier(),
                            transactionTypeCode, transactionDateString, message + " #commit", loginUser);

                    HashSet<Debtor> debtors = new HashSet<>(1);
                    HashSet<Creditor> creditors = new HashSet<>(1);

                    addCreditor(payableAccount.getIdentifier(), fromPrepareToNostroAmount, creditors);
                    addDebtor(nostroAccount.getIdentifier(), fromPrepareToNostroAmount, debtors);

                    fromPrepareToNostroEntry.setDebtors(debtors);
                    fromPrepareToNostroEntry.setCreditors(creditors);
                    accountingService.createJournalEntry(fromPrepareToNostroEntry);
                }
            }
        }
        if (accountNostroAmount > 0) {
            // can not happen that prepared amount is less than requested transfer amount (identifier and message can be default)
            final JournalEntry journalEntry = createJournalEntry(action.getIdentifier(), transactionTypeCode,
                    transactionDateString, message/* + (payableAccount == null ? "" : " #difference")*/, loginUser);

            HashSet<Debtor> debtors = new HashSet<>(1);
            HashSet<Creditor> creditors = new HashSet<>(1);

            addCreditor(isDebit ? accountId : nostroAccount.getIdentifier(), accountNostroAmount, creditors);
            addDebtor(isDebit ? nostroAccount.getIdentifier() : accountId, accountNostroAmount, debtors);

            journalEntry.setDebtors(debtors);
            journalEntry.setCreditors(creditors);
            accountingService.createJournalEntry(journalEntry);
        }

        preparedAmount = bookCharges(request, accountWrapper, action, charges, payableAccount, preparedAmount, transactionDate);

        if (preparedAmount > 0) {
//            throw new UnsupportedOperationException("Prepared amount differs from transfer amount " + doubleAmount + " for " + request.getIdentifier());
            // transfer back remaining prepared amount TODO: JM maybe fail this case?

            final JournalEntry fromPrepareToAccountEntry = createJournalEntry(action.getIdentifier() + InteropRequestData.IDENTIFIER_SEPARATOR + "diff",
                    transactionTypeCode, transactionDateString, message + " #release difference", loginUser);

            HashSet<Debtor> debtors = new HashSet<>(1);
            HashSet<Creditor> creditors = new HashSet<>(1);

            addCreditor(payableAccount.getIdentifier(), preparedAmount, creditors);
            addDebtor(accountId, preparedAmount, debtors);

            fromPrepareToAccountEntry.setDebtors(debtors);
            fromPrepareToAccountEntry.setCreditors(creditors);
            accountingService.createJournalEntry(fromPrepareToAccountEntry);
        }
    }

    private double bookCharges(@NotNull InteropTransferCommand request, @NotNull AccountWrapper accountWrapper, @NotNull InteropActionEntity action,
                               @NotNull List<Charge> charges, Account payableAccount, double preparedAmount, LocalDateTime transactionDate) {
        boolean isDebit = request.getTransactionRole().isWithdraw();
        String accountId = accountWrapper.account.getIdentifier();
        String message = request.getNote();
        BigDecimal amount = request.getAmount().getAmount();
        Currency currency = accountWrapper.productDefinition.getCurrency();

        BigDecimal calcFee = MathUtil.normalize(calcTotalCharges(charges, amount), currency);
        BigDecimal requestFee = request.getFspFee().getAmount();
        if (!MathUtil.isEqualTo(calcFee, requestFee))
            throw new UnsupportedOperationException("Quote fee " + requestFee + " differs from transfer fee " + calcFee);

        if (MathUtil.isEmpty(calcFee)) {
            return preparedAmount;
        }

        String loginUser = getLoginUser();
        String transactionTypeCode = (isDebit ? TransactionType.CURRENCY_WITHDRAWAL : TransactionType.CURRENCY_DEPOSIT).getCode();
        String transactionDateString = DateConverter.toIsoString(transactionDate);

        ArrayList<Charge> unpaidCharges = new ArrayList<>(charges);
        if (preparedAmount > 0) {
            InteropActionEntity prepareAction = findAction(action.getTransaction(), InteropActionType.PREPARE);
            if (prepareAction != null) {
                final JournalEntry fromPrepareToRevenueEntry = createJournalEntry(action.getIdentifier() + InteropRequestData.IDENTIFIER_SEPARATOR + "fee",
                        transactionTypeCode, transactionDateString, message + " #commit fee", loginUser);

                double payedAmount = 0d;

                HashSet<Debtor> debtors = new HashSet<>(1);
                HashSet<Creditor> creditors = new HashSet<>(1);

                for (Charge charge : charges) {
                    BigDecimal value = calcChargeAmount(amount, charge, currency, true);
                    if (value == null)
                        continue;

                    double doubleValue = value.doubleValue();
                    if (doubleValue > preparedAmount) {
                        break;
                    }
                    unpaidCharges.remove(charge);
                    preparedAmount -= doubleValue;
                    payedAmount += doubleValue;

                    addDebtor(charge.getIncomeAccountIdentifier(), doubleValue, debtors);
                }
                if (!unpaidCharges.isEmpty())
                    throw new UnsupportedOperationException("Prepared amount " + preparedAmount + " is less than transfer fee amount for " +
                            request.getIdentifier());

                if (payedAmount > 0) {
                    addCreditor(payableAccount.getIdentifier(), payedAmount, creditors);

                    fromPrepareToRevenueEntry.setDebtors(debtors);
                    fromPrepareToRevenueEntry.setCreditors(creditors);
                    accountingService.createJournalEntry(fromPrepareToRevenueEntry);
                }
            }
        }
        if (!unpaidCharges.isEmpty()) {
            // can not happen that prepared amount is more or less than requested transfer amount (identifier and message can be default)
            final JournalEntry journalEntry = createJournalEntry(action.getIdentifier() + InteropRequestData.IDENTIFIER_SEPARATOR + "fee",
                    transactionTypeCode, transactionDateString, message + " #fee", loginUser);

            HashSet<Debtor> debtors = new HashSet<>(1);
            HashSet<Creditor> creditors = new HashSet<>(1);

            double payedAmount = 0d;
            for (Charge charge : charges) {
                BigDecimal value = calcChargeAmount(amount, charge, currency, true);
                if (value == null)
                    continue;

                double doubleValue = value.doubleValue();
                payedAmount += doubleValue;

                addDebtor(charge.getIncomeAccountIdentifier(), doubleValue, debtors);
            }

            if (payedAmount > 0) {
                addCreditor(accountId, payedAmount, creditors);

                journalEntry.setDebtors(debtors);
                journalEntry.setCreditors(creditors);
                accountingService.createJournalEntry(journalEntry);
            }
        }

        return preparedAmount;
    }

    // Util

    private JournalEntry createJournalEntry(String actionIdentifier, String transactionType, String transactionDate, String message, String loginUser) {
        final JournalEntry fromPrepareToNostroEntry = new JournalEntry();
        fromPrepareToNostroEntry.setTransactionIdentifier(actionIdentifier);
        fromPrepareToNostroEntry.setTransactionType(transactionType);
        fromPrepareToNostroEntry.setTransactionDate(transactionDate);
        fromPrepareToNostroEntry.setMessage(message);
        fromPrepareToNostroEntry.setClerk(loginUser);
        return fromPrepareToNostroEntry;
    }

    private void addCreditor(String accountNumber, double amount, HashSet<Creditor> creditors) {
        Creditor creditor = new Creditor();
        creditor.setAccountNumber(accountNumber);
        creditor.setAmount(Double.toString(amount));
        creditors.add(creditor);
    }

    private void addDebtor(String accountNumber, double amount, HashSet<Debtor> debtors) {
        Debtor debtor = new Debtor();
        debtor.setAccountNumber(accountNumber);
        debtor.setAmount(Double.toString(amount));
        debtors.add(debtor);
    }

    private BigDecimal calcChargeAmount(BigDecimal amount, Charge charge) {
        return calcChargeAmount(amount, charge, null, false);
    }

    private BigDecimal calcChargeAmount(@NotNull BigDecimal amount, @NotNull Charge charge, Currency currency, boolean norm) {
        Double value = charge.getAmount();
        if (value == null)
            return null;

        BigDecimal portion = BigDecimal.valueOf(100.00d);
        MathContext mc = MathUtil.CALCULATION_MATH_CONTEXT;
        BigDecimal feeAmount = BigDecimal.valueOf(MathUtil.nullToZero(charge.getAmount()));
        BigDecimal result = charge.getProportional()
                ? amount.multiply(feeAmount.divide(portion, mc), mc)
                : feeAmount;
        return norm ? MathUtil.normalize(result, currency) : result;
    }

    @NotNull
    private BigDecimal calcTotalCharges(@NotNull List<Charge> charges, BigDecimal amount) {
        return charges.stream().map(charge -> calcChargeAmount(amount, charge)).reduce(MathUtil::add).orElse(BigDecimal.ZERO);
    }

    private Account validateAndGetAccount(@NotNull String accountId) {
        //TODO: error handling
        Account account = accountingService.findAccount(accountId);
        validateAccount(account);

        return account;
    }

    private AccountWrapper validateAndGetAccount(@NotNull InteropRequestData request) {
        //TODO: error handling
        String accountId = request.getAccountId();
        Account account = accountingService.findAccount(accountId);
        validateAccount(request, account);

        ProductInstance product = depositService.findProductInstance(accountId);
        ProductDefinition productDefinition = depositService.findProductDefinition(product.getProductIdentifier());

        Currency currency = productDefinition.getCurrency();
        if (!currency.getCode().equals(request.getAmount().getCurrency()))
            throw new UnsupportedOperationException();

        request.normalizeAmounts(currency);

        Double withdrawableBalance = getWithdrawableBalance(account, productDefinition);
        if (request.getTransactionRole().isWithdraw() && withdrawableBalance < request.getAmount().getAmount().doubleValue())
            throw new UnsupportedOperationException();

        return new AccountWrapper(account, product, productDefinition, withdrawableBalance);
    }

    private Account validateAndGetPayableAccount(@NotNull InteropRequestData request, @NotNull AccountWrapper wrapper) {
        String referenceId = wrapper.account.getReferenceAccount();
        if (referenceId == null)
            return null;

        return validateAndGetAccount(request, referenceId);
    }

    @NotNull
    private Account validateAndGetNostroAccount(@NotNull InteropRequestData request) {
        //TODO: error handling
        List<Account> nostros = fetchAccounts(false, ACCOUNT_NAME_NOSTRO, AccountType.ASSET.name(), false, null, null, null, null);
        int size = nostros.size();
        if (size != 1)
            throw new UnsupportedOperationException("NOSTRO Account " + (size == 0 ? "not found" : "is ambigous"));

        Account nostro = nostros.get(0);
        validateAccount(request, nostro);
        return nostro;
    }

    @NotNull
    private Account validateAndGetAccount(@NotNull InteropRequestData request, @NotNull String accountId) {
        //TODO: error handling
        Account account = accountingService.findAccount(accountId);

        validateAccount(request, account);
        return account;
    }

    private void validateAccount(Account account) {
        if (account == null)
            throw new UnsupportedOperationException("Account not found");
        if (!account.getState().equals(Account.State.OPEN.name()))
            throw new UnsupportedOperationException("Account is in state " + account.getState());
    }

    private void validateAccount(@NotNull InteropRequestData request, Account account) {
        validateAccount(account);

        String accountId = account.getIdentifier();

        if (account.getHolders() != null) { // customer account
            ProductInstance product = depositService.findProductInstance(accountId);
            ProductDefinition productDefinition = depositService.findProductDefinition(product.getProductIdentifier());
            if (!Boolean.TRUE.equals(productDefinition.getActive()))
                throw new UnsupportedOperationException("NOSTRO Product Definition is inactive");

            Currency currency = productDefinition.getCurrency();
            if (!currency.getCode().equals(request.getAmount().getCurrency()))
                throw new UnsupportedOperationException();
        }
    }

    private BigDecimal validateTransfer(@NotNull InteropTransferRequestData request, @NotNull AccountWrapper accountWrapper) {
        BigDecimal amount = request.getAmount().getAmount();

        boolean isDebit = request.getTransactionRole().isWithdraw();
        Currency currency = accountWrapper.productDefinition.getCurrency();

        BigDecimal total = isDebit ? amount : MathUtil.negate(amount);
        MoneyData fspFee = request.getFspFee();
        if (fspFee != null) {
            if (!currency.getCode().equals(fspFee.getCurrency()))
                throw new UnsupportedOperationException();
            //TODO: compare with calculated quote fee
            total = MathUtil.add(total, fspFee.getAmount());
        }
        MoneyData fspCommission = request.getFspCommission();
        if (fspCommission != null) {
            if (!currency.getCode().equals(fspCommission.getCurrency()))
                throw new UnsupportedOperationException();
            //TODO: compare with calculated quote commission
            total = MathUtil.subtractToZero(total, fspCommission.getAmount());
        }
        if (isDebit && accountWrapper.withdrawableBalance < request.getAmount().getAmount().doubleValue())
            throw new UnsupportedOperationException();
        return total;
    }

    public List<Account> fetchAccounts(boolean includeClosed, String term, String type, boolean includeCustomerAccounts,
                                       Integer pageIndex, Integer size, String sortColumn, String sortDirection) {
        return accountingService.fetchAccounts(includeClosed, term, type, includeCustomerAccounts, pageIndex, size, sortColumn, sortDirection);
    }

    public InteropIdentifierEntity findIdentifier(@NotNull InteropIdentifierType idType, @NotNull String idValue, String subIdOrType) {
        return identifierRepository.findOne(Specifications.where(idTypeEqual(idType)).and(idValueEqual(idValue)).and(subIdOrTypeEqual(subIdOrType)));
    }

    public static Specification<InteropIdentifierEntity> idTypeEqual(@NotNull InteropIdentifierType idType) {
        return (Root<InteropIdentifierEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.and(cb.equal(root.get("type"), idType));
    }

    public static Specification<InteropIdentifierEntity> idValueEqual(@NotNull String idValue) {
        return (Root<InteropIdentifierEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.and(cb.equal(root.get("value"), idValue));
    }

    public static Specification<InteropIdentifierEntity> subIdOrTypeEqual(String subIdOrType) {
        return (Root<InteropIdentifierEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Path<Object> path = root.get("subValueOrType");
            return cb.and(subIdOrType == null ? cb.isNull(path) : cb.equal(path, subIdOrType));
        };
    }

    @NotNull
    private String calcActionIdentifier(@NotNull String actionCode, @NotNull InteropActionType actionType) {
        return actionType == InteropActionType.PREPARE || actionType == InteropActionType.COMMIT ? actionType + InteropRequestData.IDENTIFIER_SEPARATOR + actionCode : actionCode;
    }

    private InteropActionEntity validateAndGetAction(@NotNull String transactionCode, @NotNull String actionIdentifier, @NotNull InteropActionType actionType) {
        return validateAndGetAction(transactionCode, actionIdentifier, actionType, true);
    }

    private InteropActionEntity validateAndGetAction(@NotNull String transactionCode, @NotNull String actionIdentifier, @NotNull InteropActionType actionType, boolean one) {
        //TODO: error handling
        InteropActionEntity action = actionRepository.findByIdentifier(actionIdentifier);
        if (action == null) {
            if (one)
                throw new UnsupportedOperationException("Interperation action " + actionType + '/' + actionIdentifier + " was not found for this transaction " + transactionCode);
            return null;
        }
        if (!action.getTransaction().getIdentifier().equals(transactionCode))
            throw new UnsupportedOperationException("Interperation action " + actionType + '/' + actionIdentifier + " does not exist in this transaction " + transactionCode);
        if (action.getActionType() != actionType)
            throw new UnsupportedOperationException("Interperation action " + actionIdentifier + " is not this type " + actionType);

        return action;
    }

    @NotNull
    private InteropTransactionEntity validateAndGetTransaction(@NotNull InteropRequestData request, @NotNull AccountWrapper accountWrapper) {
        return validateAndGetTransaction(request, accountWrapper, true);
    }

    @NotNull
    private InteropTransactionEntity validateAndGetTransaction(@NotNull InteropRequestData request, @NotNull AccountWrapper accountWrapper, boolean create) {
        return validateAndGetTransaction(request, accountWrapper, getNow(), create);
    }

    @NotNull
    private InteropTransactionEntity validateAndGetTransaction(@NotNull InteropRequestData request, @NotNull AccountWrapper accountWrapper,
                                                               @NotNull LocalDateTime createdOn, boolean create) {
        //TODO: error handling
        String transactionCode = request.getTransactionCode();
        InteropTransactionEntity transaction = transactionRepository.findOneByIdentifier(request.getTransactionCode());
        InteropState state = InteropStateMachine.handleTransition(transaction == null ? null : transaction.getState(), request.getActionType());
        LocalDateTime now = getNow();
        if (transaction == null) {
            if (!create)
                throw new UnsupportedOperationException("Interperation transaction " + request.getTransactionCode() + " does not exist ");
            transaction = new InteropTransactionEntity(transactionCode, accountWrapper.account.getIdentifier(), getLoginUser(), now);
            transaction.setState(state);
            transaction.setAmount(request.getAmount().getAmount());
            transaction.setName(request.getNote());
            transaction.setTransactionType(request.getTransactionRole().getTransactionType());
            Account payableAccount = validateAndGetPayableAccount(request, accountWrapper);
            transaction.setPrepareAccountIdentifier(payableAccount == null ? null : payableAccount.getIdentifier());
            transaction.setNostroAccountIdentifier(validateAndGetNostroAccount(request).getIdentifier());
            transaction.setExpirationDate(request.getExpiration());
        }
        LocalDateTime expirationDate = transaction.getExpirationDate();
        if (expirationDate != null && expirationDate.isBefore(now))
            throw new UnsupportedOperationException("Interperation transaction expired on " + expirationDate);
        return transaction;
    }

    private Currency getCurrency(InteropActionEntity action) {
        ProductInstance product = depositService.findProductInstance(action.getTransaction().getCustomerAccountIdentifier());
        ProductDefinition productDefinition = depositService.findProductDefinition(product.getProductIdentifier());
        return productDefinition.getCurrency();
    }

    private InteropActionEntity addAction(@NotNull InteropTransactionEntity transaction, @NotNull InteropRequestData request) {
        return addAction(transaction, request, getNow());
    }

    private InteropActionEntity addAction(@NotNull InteropTransactionEntity transaction, @NotNull InteropRequestData request,
                                          @NotNull LocalDateTime createdOn) {
        InteropActionEntity lastAction = getLastAction(transaction);

        InteropActionType actionType = request.getActionType();
        String actionIdentifier = calcActionIdentifier(request.getIdentifier(), request.getActionType());
        InteropActionEntity action = new InteropActionEntity(actionIdentifier, transaction, request.getActionType(),
                (lastAction == null ? 0 : lastAction.getSeqNo() + 1), getLoginUser(),
                (lastAction == null ? transaction.getCreatedOn() : createdOn));
        action.setState(InteropActionState.ACCEPTED);
        action.setAmount(request.getAmount().getAmount());
        action.setExpirationDate(request.getExpiration());
        transaction.getActions().add(action);

        InteropState currentState = transaction.getState();
        if (transaction.getId() != null || InteropStateMachine.isValidAction(currentState, actionType)) // newly created was already set
            transaction.setState(InteropStateMachine.handleTransition(currentState, actionType));
        return action;
    }

    private InteropActionEntity getLastAction(InteropTransactionEntity transaction) {
        List<InteropActionEntity> actions = transaction.getActions();
        int size = actions.size();
        return size == 0 ? null : actions.get(size - 1);
    }

    private InteropActionEntity findAction(InteropTransactionEntity transaction, InteropActionType actionType) {
        List<InteropActionEntity> actions = transaction.getActions();
        for (InteropActionEntity action : actions) {
            if (action.getActionType() == actionType)
                return action;
        }
        return null;
    }

    private LocalDateTime getNow() {
        return LocalDateTime.now(Clock.systemUTC());
    }

    private String getLoginUser() {
        return UserContextHolder.checkedGetUser();
    }

    public static class AccountWrapper {
        @NotNull
        private final Account account;
        @NotNull
        private final ProductInstance product;
        @NotNull
        private final ProductDefinition productDefinition;
        @NotNull
        private final Double withdrawableBalance;

        public AccountWrapper(Account account, ProductInstance product, ProductDefinition productDefinition, Double withdrawableBalance) {
            this.account = account;
            this.product = product;
            this.productDefinition = productDefinition;
            this.withdrawableBalance = withdrawableBalance;
        }
    }
}
