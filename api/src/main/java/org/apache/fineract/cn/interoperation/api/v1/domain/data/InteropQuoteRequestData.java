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
package org.apache.fineract.cn.interoperation.api.v1.domain.data;

import org.apache.fineract.cn.deposit.api.v1.definition.domain.Currency;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropActionType;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropAmountType;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropTransactionRole;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;


public class InteropQuoteRequestData extends InteropRequestData {

    @NotNull
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
    private String quoteCode;

    @NotNull
    private InteropAmountType amountType;

    @Valid
    private MoneyData fees; // only for disclosed Payer fees on the Payee side

    private InteropQuoteRequestData() {
        super();
    }

    public InteropQuoteRequestData(@NotNull String transactionCode, String requestCode, @NotNull String accountId, @NotNull MoneyData amount,
                                   @NotNull InteropTransactionRole transactionRole, @NotNull InteropTransactionTypeData transactionType,
                                   String note, GeoCodeData geoCode, LocalDateTime expiration, List<ExtensionData> extensionList,
                                   @NotNull String quoteCode, @NotNull InteropAmountType amountType, MoneyData fees) {
        super(transactionCode, requestCode, accountId, amount, transactionRole, transactionType, note, geoCode, expiration, extensionList);
        this.quoteCode = quoteCode;
        this.amountType = amountType;
        this.fees = fees;
    }

    public InteropQuoteRequestData(@NotNull String transactionCode, @NotNull String accountId, @NotNull InteropAmountType amountType,
                                   @NotNull MoneyData amount, @NotNull InteropTransactionRole transactionRole, @NotNull InteropTransactionTypeData transactionType,
                                   @NotNull String quoteCode) {
        this(transactionCode, null, accountId, amount, transactionRole, transactionType, null, null, null, null, quoteCode,
                amountType, null);
    }

    private InteropQuoteRequestData(@NotNull InteropRequestData other, @NotNull String quoteCode, @NotNull InteropAmountType amountType,
                                    MoneyData fees) {
        this(other.getTransactionCode(), other.getRequestCode(), other.getAccountId(), other.getAmount(), other.getTransactionRole(),
                other.getTransactionType(), other.getNote(), other.getGeoCode(), other.getExpiration(), other.getExtensionList(),
                quoteCode, amountType, fees);
    }

    public String getQuoteCode() {
        return quoteCode;
    }

    public InteropAmountType getAmountType() {
        return amountType;
    }

    public MoneyData getFees() {
        return fees;
    }

    public void normalizeAmounts(@NotNull Currency currency) {
        super.normalizeAmounts(currency);
        if (fees != null)
            fees.normalizeAmount(currency);
    }

    protected void setQuoteCode(String quoteCode) {
        this.quoteCode = quoteCode;
    }

    protected void setAmountType(InteropAmountType amountType) {
        this.amountType = amountType;
    }

    protected void setFees(MoneyData fees) {
        this.fees = fees;
    }

    @NotNull
    @Transient
    @Override
    public InteropActionType getActionType() {
        return InteropActionType.QUOTE;
    }

    @Transient
    @NotNull
    @Override
    public String getIdentifier() {
        return quoteCode;
    }
}
