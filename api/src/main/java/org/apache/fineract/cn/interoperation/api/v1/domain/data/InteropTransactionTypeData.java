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

import org.apache.fineract.cn.interoperation.api.v1.domain.InteropInitiatorType;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropTransactionRole;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropTransactionScenario;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class InteropTransactionTypeData {

    @NotNull
    private InteropTransactionScenario scenario;

    @Pattern(regexp = "^[A-Z_]{1,32}$")
    private String subScenario;

    @NotNull
    private InteropTransactionRole initiator;

    @NotNull
    private InteropInitiatorType initiatorType;

    protected InteropTransactionTypeData() {
    }

    public InteropTransactionTypeData(InteropTransactionScenario scenario, String subScenario, InteropTransactionRole initiator, InteropInitiatorType initiatorType) {
        this.scenario = scenario;
        this.subScenario = subScenario;
        this.initiator = initiator;
        this.initiatorType = initiatorType;
    }

    public InteropTransactionScenario getScenario() {
        return scenario;
    }

    public String getSubScenario() {
        return subScenario;
    }

    public InteropTransactionRole getInitiator() {
        return initiator;
    }

    public InteropInitiatorType getInitiatorType() {
        return initiatorType;
    }

    protected void setScenario(InteropTransactionScenario scenario) {
        this.scenario = scenario;
    }

    protected void setSubScenario(String subScenario) {
        this.subScenario = subScenario;
    }

    protected void setInitiator(InteropTransactionRole initiator) {
        this.initiator = initiator;
    }

    protected void setInitiatorType(InteropInitiatorType initiatorType) {
        this.initiatorType = initiatorType;
    }
}
