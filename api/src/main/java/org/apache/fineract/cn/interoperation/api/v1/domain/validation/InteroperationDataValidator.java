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
package org.apache.fineract.cn.interoperation.api.v1.domain.validation;

import org.apache.fineract.cn.interoperation.api.v1.domain.data.InteropIdentifierCommand;
import org.apache.fineract.cn.interoperation.api.v1.domain.data.InteropIdentifierDeleteCommand;
import org.apache.fineract.cn.interoperation.api.v1.domain.data.InteropQuoteRequestData;
import org.apache.fineract.cn.interoperation.api.v1.domain.data.InteropTransactionRequestData;
import org.apache.fineract.cn.interoperation.api.v1.domain.data.InteropTransferCommand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InteroperationDataValidator {

    public InteroperationDataValidator() {
    }

    public InteropIdentifierCommand registerAccountIdentifier(InteropIdentifierCommand requestData) {
        return requestData;
    }

    public InteropIdentifierDeleteCommand deleteAccountIdentifier(InteropIdentifierDeleteCommand requestData) {
        return requestData;
    }

    public InteropTransactionRequestData validateCreateRequest(InteropTransactionRequestData requestData) {
        return requestData;
    }

    public InteropQuoteRequestData validateCreateQuote(InteropQuoteRequestData requestData) {
        return requestData;
    }

    public InteropTransferCommand validatePrepareTransfer(InteropTransferCommand requestData) {
        return validateCommitTransfer(requestData);
    }

    public InteropTransferCommand validateCommitTransfer(InteropTransferCommand requestData) {
        return requestData;
    }

    private void throwExceptionIfValidationWarningsExist(List<String> errors) {
        if (errors != null && !errors.isEmpty()) {
            throw new UnsupportedOperationException(String.join(", ", errors)); // TODO
        }
    }
}
