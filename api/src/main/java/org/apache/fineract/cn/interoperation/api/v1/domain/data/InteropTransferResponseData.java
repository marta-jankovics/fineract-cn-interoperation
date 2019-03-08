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
import java.util.List;

public class InteropTransferResponseData extends InteropResponseData {

    @NotNull
    private final String transferCode;

    private String completedTimestamp;

    private InteropTransferResponseData(@NotNull String transactionCode, @NotNull InteropActionState state, LocalDateTime expiration,
                                        List<ExtensionData> extensionList, @NotNull String transferCode, LocalDateTime completedTimestamp) {
        super(transactionCode, state, expiration, extensionList);
        this.transferCode = transferCode;
        this.completedTimestamp = format(completedTimestamp);
    }

    public static InteropTransferResponseData build(@NotNull String transactionCode, @NotNull InteropActionState state,
                                                    LocalDateTime expiration, List<ExtensionData> extensionList,
                                                    @NotNull String transferCode, LocalDateTime completedTimestamp) {
        return new InteropTransferResponseData(transactionCode, state, expiration, extensionList, transferCode, completedTimestamp);
    }

    public static InteropTransferResponseData build(@NotNull String transactionCode, @NotNull InteropActionState state,
                                                    LocalDateTime expiration, @NotNull String transferCode, LocalDateTime completedTimestamp) {
        return build(transactionCode, state, expiration, transferCode, completedTimestamp);
    }

    public String getTransferCode() {
        return transferCode;
    }

    public String getCompletedTimestamp() {
        return completedTimestamp;
    }

    @Transient
    public LocalDateTime getCompletedTimestampDate() {
        return parse(completedTimestamp);
    }
}
