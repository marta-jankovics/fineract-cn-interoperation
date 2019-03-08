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
package org.apache.fineract.cn.interoperation.api.v1.domain;

import org.apache.fineract.cn.lang.ServiceException;

import javax.validation.constraints.NotNull;

public class InteropStateMachine {

    private InteropStateMachine() {

    }

    /**
     * Executes the given transition from one status to another when the given transition (actualEvent + action) is valid.
     *
     * @param currentState actual {@link InteropState}
     * @param action        the {@link InteropActionType}
     * @return the new {@link InteropState} for the tag if the given transition is valid
     *
     */
    public static InteropState handleTransition(InteropState currentState, @NotNull InteropActionType action) {
        InteropState transitionState = getTransitionState(currentState, action);
        if (transitionState == null) {
            throw new ServiceException("State transition is not valid: ({0}) and action: ({1})", currentState, action);
        }
        return transitionState;
    }

    public static boolean isValidAction(InteropState currentState, @NotNull InteropActionType action) {
        if (currentState == null || action == InteropActionType.ABORT)
            return true;
        InteropState transitionState = getTransitionState(currentState, action);
        return transitionState != null && transitionState.ordinal() > currentState.ordinal();
    }

    private static InteropState getTransitionState(InteropState currentState, @NotNull InteropActionType action) {
        return InteropState.forAction(action);
    }
}
