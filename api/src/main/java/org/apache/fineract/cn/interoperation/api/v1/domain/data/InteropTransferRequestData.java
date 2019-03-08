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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;

public class InteropTransferRequestData extends InteropRequestData {

    @NotNull
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
    private String transferCode;

    @Valid
    private MoneyData fspFee;

    @Valid
    private MoneyData fspCommission;

    protected InteropTransferRequestData() {
        super();
    }

    public InteropTransferRequestData(@NotNull String transactionCode, @NotNull String accountId, @NotNull MoneyData amount,
                                      @NotNull InteropTransactionRole transactionRole, String note, LocalDateTime expiration,
                                      List<ExtensionData> extensionList, @NotNull String transferCode, MoneyData fspFee, MoneyData fspCommission) {
        super(transactionCode, null, accountId, amount, transactionRole, null, note, null, expiration, extensionList);
        this.transferCode = transferCode;
        this.fspFee = fspFee;
        this.fspCommission = fspCommission;
    }

    public InteropTransferRequestData(@NotNull String transactionCode, @NotNull String accountId, @NotNull MoneyData amount,
                                      @NotNull InteropTransactionRole transactionRole, @NotNull String transferCode) {
        this(transactionCode, accountId, amount, transactionRole, null, null, null, transferCode, null, null);
    }

    private InteropTransferRequestData(InteropRequestData other, @NotNull String transferCode, MoneyData fspFee, MoneyData fspCommission) {
        this(other.getTransactionCode(), other.getAccountId(), other.getAmount(), other.getTransactionRole(),
                other.getNote(), other.getExpiration(), other.getExtensionList(), transferCode, fspFee, fspCommission);
    }

    public String getTransferCode() {
        return transferCode;
    }

    public MoneyData getFspFee() {
        return fspFee;
    }

    public MoneyData getFspCommission() {
        return fspCommission;
    }

    public void normalizeAmounts(@NotNull Currency currency) {
        super.normalizeAmounts(currency);
        if (fspFee != null)
            fspFee.normalizeAmount(currency);
    }

    @Transient
    @Override
    public InteropActionType getActionType() {
        return null;
    }

    @Transient
    @NotNull
    @Override
    public String getIdentifier() {
        return transferCode;
    }
}
