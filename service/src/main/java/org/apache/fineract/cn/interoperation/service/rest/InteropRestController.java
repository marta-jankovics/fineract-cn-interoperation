/**
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
package org.apache.fineract.cn.interoperation.service.rest;

import org.apache.fineract.cn.anubis.annotation.AcceptedTokenType;
import org.apache.fineract.cn.anubis.annotation.Permittable;
import org.apache.fineract.cn.command.domain.CommandCallback;
import org.apache.fineract.cn.command.gateway.CommandGateway;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropIdentifierType;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropTransferActionType;
import org.apache.fineract.cn.interoperation.api.v1.domain.data.*;
import org.apache.fineract.cn.interoperation.service.internal.command.InitializeServiceCommand;
import org.apache.fineract.cn.interoperation.service.internal.service.InteropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import static org.apache.fineract.cn.interoperation.api.v1.PermittableGroupIds.INTEROPERATION_SINGLE;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/") //interoperation/v1
public class InteropRestController {

    private CommandGateway commandGateway;

    private InteropService interopService;

    @Autowired
    public InteropRestController(CommandGateway commandGateway,
                                 InteropService interopService) {
        this.commandGateway = commandGateway;
        this.interopService = interopService;
    }

    @Permittable(value = AcceptedTokenType.SYSTEM)
    @RequestMapping(
            value = "/initialize",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public
    @ResponseBody
    ResponseEntity<Void> initialize() {
        this.commandGateway.process(new InitializeServiceCommand());
        return ResponseEntity.accepted().build();
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "/health",
            method = RequestMethod.GET,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> health(@Context UriInfo uriInfo) {
        return ResponseEntity.ok().build();
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "/parties/{idType}/{idValue}",
            method = RequestMethod.GET,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<InteropIdentifierData> getAccountByIdentifier(@PathVariable("idType") InteropIdentifierType idType,
                                                                        @PathVariable("idValue") String idValue) {
        InteropIdentifierData account = interopService.getAccountByIdentifier(idType, idValue, null);

        return ResponseEntity.ok(account);
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "/parties/{idType}/{idValue}/{subIdOrType}",
            method = RequestMethod.GET,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<InteropIdentifierData> getAccountByIdentifier(@PathVariable("idType") InteropIdentifierType idType,
                                                                        @PathVariable("idValue") String idValue,
                                                                        @PathVariable(value = "subIdOrType") String subIdOrType) {
        InteropIdentifierData account = interopService.getAccountByIdentifier(idType, idValue, subIdOrType);

        return ResponseEntity.ok(account);
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "/parties/{idType}/{idValue}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<InteropIdentifierData> registerAccountIdentifier(@PathVariable("idType") InteropIdentifierType idType,
                                                                           @PathVariable("idValue") String idValue,
                                                                           @RequestBody @Valid InteropIdentifierData requestData)
            throws Throwable {
        CommandCallback<InteropIdentifierData> result = commandGateway.process(new InteropIdentifierCommand(requestData,
                        idType, idValue, null), InteropIdentifierData.class);

        return ResponseEntity.ok(result.get());
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "/parties/{idType}/{idValue}/{subIdOrType}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<InteropIdentifierData> registerAccountIdentifier(@PathVariable("idType") InteropIdentifierType idType,
                                                                           @PathVariable("idValue") String idValue,
                                                                           @PathVariable(value = "subIdOrType", required = false) String subIdOrType,
                                                                           @RequestBody @Valid InteropIdentifierData requestData)
            throws Throwable {
        CommandCallback<InteropIdentifierData> result = commandGateway.process(new InteropIdentifierCommand(requestData,
                idType, idValue, subIdOrType), InteropIdentifierData.class);

        return ResponseEntity.ok(result.get());
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "/parties/{idType}/{idValue}",
            method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<InteropIdentifierData> deleteAccountIdentifier(@PathVariable("idType") InteropIdentifierType idType,
                                                                           @PathVariable("idValue") String idValue)
            throws Throwable {
        CommandCallback<InteropIdentifierData> result = commandGateway.process(new InteropIdentifierDeleteCommand(idType, idValue, null), InteropIdentifierData.class);

        return ResponseEntity.ok(result.get());
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "/parties/{idType}/{idValue}/{subIdOrType}",
            method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<InteropIdentifierData> deleteAccountIdentifier(@PathVariable("idType") InteropIdentifierType idType,
                                                                         @PathVariable("idValue") String idValue,
                                                                         @PathVariable(value = "subIdOrType", required = false) String subIdOrType)
            throws Throwable {
        CommandCallback<InteropIdentifierData> result = commandGateway.process(new InteropIdentifierDeleteCommand(idType, idValue, subIdOrType), InteropIdentifierData.class);

        return ResponseEntity.ok(result.get());
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "transactions/{transactionCode}/requests/{requestCode}",
            method = RequestMethod.GET,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<InteropTransactionRequestResponseData> getTransactionRequest(@PathVariable("transactionCode") String transactionCode,
                                                                                       @PathVariable("requestCode") String requestCode) {
        InteropTransactionRequestResponseData result = interopService.getTransactionRequest(transactionCode, requestCode);

        return ResponseEntity.ok(result);
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "/requests",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<InteropTransactionRequestResponseData> createTransactionRequest(@RequestBody @Valid InteropTransactionRequestData requestData)
            throws Throwable {
        CommandCallback<InteropTransactionRequestResponseData> result = commandGateway.process(requestData, InteropTransactionRequestResponseData.class);

        return ResponseEntity.ok(result.get());
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "transactions/{transactionCode}/quotes/{quoteCode}",
            method = RequestMethod.GET,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<InteropQuoteResponseData> getQuote(@PathVariable("transactionCode") String transactionCode,
                                                             @PathVariable("quoteCode") String quoteCode) {
        InteropQuoteResponseData result = interopService.getQuote(transactionCode, quoteCode);

        return ResponseEntity.ok(result);
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "/quotes",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<InteropQuoteResponseData> createQuote(@RequestBody @Valid InteropQuoteRequestData requestData) throws Throwable {
        CommandCallback<InteropQuoteResponseData> result = commandGateway.process(requestData, InteropQuoteResponseData.class);

        return ResponseEntity.ok(result.get());
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "transactions/{transactionCode}/transfers/{transferCode}",
            method = RequestMethod.GET,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<InteropTransferResponseData> getTransfer(@PathVariable("transactionCode") String transactionCode,
                                                                   @PathVariable("transferCode") String transferCode) {
        InteropTransferResponseData result = interopService.getTransfer(transactionCode, transferCode);

        return ResponseEntity.ok(result);
    }

    @Permittable(value = AcceptedTokenType.TENANT, groupId = INTEROPERATION_SINGLE)
    @RequestMapping(
            value = "/transfers",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<InteropTransferResponseData> performTransfer(@RequestParam("action") String action,
                                                                       @RequestBody @Valid InteropTransferRequestData requestData)
            throws Throwable {
        CommandCallback<InteropTransferResponseData> result = commandGateway.process(new InteropTransferCommand(requestData, InteropTransferActionType.valueOf(action)),
                InteropTransferResponseData.class);

        return ResponseEntity.ok(result.get());
    }
}