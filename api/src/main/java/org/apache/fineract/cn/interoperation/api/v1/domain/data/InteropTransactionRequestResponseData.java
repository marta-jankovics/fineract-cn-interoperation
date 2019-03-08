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
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

public class InteropTransactionRequestResponseData extends InteropResponseData {

    @NotNull
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
    private final String requestCode;

    private InteropTransactionRequestResponseData(@NotNull String transactionCode, @NotNull InteropActionState state, LocalDateTime expiration, List<ExtensionData> extensionList,
                                                  @NotNull String requestCode) {
        super(transactionCode, state, expiration, extensionList);
        this.requestCode = requestCode;
    }

    public static InteropTransactionRequestResponseData build(@NotNull String transactionCode, @NotNull InteropActionState state,
                                                              LocalDateTime expiration, List<ExtensionData> extensionList,
                                                              @NotNull String requestCode) {
        return new InteropTransactionRequestResponseData(transactionCode, state, expiration, extensionList, requestCode);
    }

    public static InteropTransactionRequestResponseData build(@NotNull String transactionCode, @NotNull InteropActionState state,
                                                              LocalDateTime expiration, @NotNull String requestCode) {
        return build(transactionCode, state, expiration, null, requestCode);
    }

    public String getRequestCode() {
        return requestCode;
    }
}
