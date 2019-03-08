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

import org.apache.fineract.cn.interoperation.api.v1.domain.InteropActionState;

import javax.validation.constraints.NotNull;
import java.beans.Transient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class InteropResponseData {

    @NotNull
    private final String transactionCode;

    @NotNull
    private final InteropActionState state;

    private final String expiration;

    private final List<ExtensionData> extensionList;


    protected InteropResponseData(@NotNull String transactionCode, @NotNull InteropActionState state, LocalDateTime expiration, List<ExtensionData> extensionList) {
        this.transactionCode = transactionCode;
        this.state = state;
        this.expiration = format(expiration);
        this.extensionList = extensionList;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public InteropActionState getState() {
        return state;
    }

    public String getExpiration() {
        return expiration;
    }

    @Transient
    public LocalDateTime getExpirationDate() {
        return parse(expiration);
    }

    public List<ExtensionData> getExtensionList() {
        return extensionList;
    }

    protected static LocalDateTime parse(String date) {
        return date == null ? null : LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
    }

    protected static String format(LocalDateTime date) {
        return date == null ? null : date.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
