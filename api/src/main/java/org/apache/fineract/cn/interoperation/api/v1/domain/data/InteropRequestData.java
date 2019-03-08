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
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropTransactionRole;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.beans.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public abstract class InteropRequestData {

    public static final String IDENTIFIER_SEPARATOR = "_";

    @NotNull
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
    private String transactionCode;

    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
    private String requestCode;

    @NotEmpty
    @Length(max = 32)
    private String accountId;

    @NotNull
    @Valid
    private MoneyData amount;

    @NotNull
    private InteropTransactionRole transactionRole;

    @Valid
    private InteropTransactionTypeData transactionType;

    @Length(max = 128)
    private String note;

    @Valid
    private GeoCodeData geoCode;

    private LocalDateTime expiration;

    @Size(max = 16)
    @Valid
    private List<ExtensionData> extensionList;

    protected InteropRequestData() {
    }

    protected InteropRequestData(@NotNull String transactionCode, String requestCode, @NotNull String accountId, @NotNull MoneyData amount,
                                 @NotNull InteropTransactionRole transactionRole, InteropTransactionTypeData transactionType, String note,
                                 GeoCodeData geoCode, LocalDateTime expiration, List<ExtensionData> extensionList) {
        this.transactionCode = transactionCode;
        this.requestCode = requestCode;
        this.accountId = accountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionRole = transactionRole;
        this.note = note;
        this.geoCode = geoCode;
        this.expiration = expiration;
        this.extensionList = extensionList;
    }

    protected InteropRequestData(@NotNull String transactionCode, @NotNull String accountId, @NotNull MoneyData amount, @NotNull InteropTransactionRole transactionRole) {
        this(transactionCode, null, accountId, amount, transactionRole, null, null, null, null, null);
    }

    @NotNull
    public String getTransactionCode() {
        return transactionCode;
    }

    public String getRequestCode() {
        return requestCode;
    }

    @NotNull
    public String getAccountId() {
        return accountId;
    }

    @NotNull
    public MoneyData getAmount() {
        return amount;
    }

    public InteropTransactionTypeData getTransactionType() {
        return transactionType;
    }

    @NotNull
    public InteropTransactionRole getTransactionRole() {
        return transactionRole;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public GeoCodeData getGeoCode() {
        return geoCode;
    }

    public void setGeoCode(GeoCodeData geoCode) {
        this.geoCode = geoCode;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public LocalDate getExpirationLocalDate() {
        return expiration == null ? null : expiration.toLocalDate();
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public List<ExtensionData> getExtensionList() {
        return extensionList;
    }

    public void setExtensionList(List<ExtensionData> extensionList) {
        this.extensionList = extensionList;
    }

    public void normalizeAmounts(@NotNull Currency currency) {
        amount.normalizeAmount(currency);
    }

    protected void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    protected void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    protected void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    protected void setAmount(MoneyData amount) {
        this.amount = amount;
    }

    protected void setTransactionRole(InteropTransactionRole transactionRole) {
        this.transactionRole = transactionRole;
    }

    protected void setTransactionType(InteropTransactionTypeData transactionType) {
        this.transactionType = transactionType;
    }

    @Transient
    public abstract InteropActionType getActionType();

    @Transient
    @NotNull
    public String getIdentifier() {
        return transactionCode;
    }
}
