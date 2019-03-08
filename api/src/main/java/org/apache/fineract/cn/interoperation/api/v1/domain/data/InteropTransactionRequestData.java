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

import org.apache.fineract.cn.interoperation.api.v1.domain.InteropActionType;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropTransactionRole;

import javax.validation.constraints.NotNull;
import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;

public class InteropTransactionRequestData extends InteropRequestData {

    protected InteropTransactionRequestData() {
        super();
    }

    public InteropTransactionRequestData(@NotNull String transactionCode, @NotNull String requestCode, @NotNull String accountId,
                                         @NotNull MoneyData amount, @NotNull InteropTransactionTypeData transactionType, String note,
                                         GeoCodeData geoCode, LocalDateTime expiration, List<ExtensionData> extensionList) {
        super(transactionCode, requestCode, accountId, amount, InteropTransactionRole.PAYER, transactionType, note, geoCode, expiration, extensionList);
    }

    public InteropTransactionRequestData(@NotNull String transactionCode, @NotNull String requestCode, @NotNull String accountId,
                                         @NotNull MoneyData amount, @NotNull InteropTransactionTypeData transactionType) {
        this(transactionCode, requestCode, accountId, amount, transactionType, null, null, null, null);
    }

    private InteropTransactionRequestData(InteropRequestData other) {
        this(other.getTransactionCode(), other.getRequestCode(), other.getAccountId(), other.getAmount(), other.getTransactionType(),
                other.getNote(), other.getGeoCode(), other.getExpiration(), other.getExtensionList());
    }

    @NotNull
    @Transient
    @Override
    public InteropActionType getActionType() {
        return InteropActionType.REQUEST;
    }

    @Transient
    @NotNull
    @Override
    public String getIdentifier() {
        return getRequestCode();
    }
}
