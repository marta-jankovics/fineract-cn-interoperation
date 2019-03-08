/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.interoperation.service.internal.repository;

import org.apache.fineract.cn.interoperation.api.v1.domain.InteropActionState;
import org.apache.fineract.cn.interoperation.api.v1.domain.InteropActionType;
import org.apache.fineract.cn.mariadb.util.LocalDateTimeConverter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hathor_actions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hathor_actions_id", columnNames = {"identifier"}),
        @UniqueConstraint(name = "uk_hathor_actions_type", columnNames = {"transaction_id", "action_type"}),
        @UniqueConstraint(name = "uk_hathor_actions_seq", columnNames = {"transaction_id", "seq_no"})
})
public class InteropActionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "identifier", nullable = false, length = 64)
    private String identifier;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", referencedColumnName="id", nullable = false)
    private InteropTransactionEntity transaction;

    @Column(name = "action_type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private InteropActionType actionType;

    @Column(name = "seq_no", nullable = false)
    private int seqNo;

    @Column(name = "state", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private InteropActionState state;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "fee")
    private BigDecimal fee;

    @Column(name = "commission")
    private BigDecimal commission;
//
//    @Column(name = "charges", nullable = false, length = 1024)
//    private String charges;
//
//    @Column(name = "ledgers", nullable = false, length = 1024)
//    private String ledgers;

    @Column(name = "error_code", length = 4)
    private String errorCode;

    @Column(name = "error_msg", length = 128)
    private String errorMsg;

    @Column(name = "expiration_date")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime expirationDate;

    @Column(name = "created_by", nullable = false, length = 32)
    private String createdBy;

    @Column(name = "created_on", nullable = false)
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime createdOn;


    protected InteropActionEntity() {
    }

    public InteropActionEntity(@NotNull String identifier, @NotNull InteropTransactionEntity transaction, @NotNull InteropActionType actionType,
                               int seqNo, @NotNull String createdBy, @NotNull LocalDateTime createdOn) {
        this.identifier = identifier;
        this.transaction = transaction;
        this.actionType = actionType;
        this.seqNo = seqNo;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
    }

    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    private void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public InteropTransactionEntity getTransaction() {
        return transaction;
    }

    private void setTransaction(InteropTransactionEntity transaction) {
        this.transaction = transaction;
    }

    public InteropActionType getActionType() {
        return actionType;
    }

    private void setActionType(InteropActionType actionType) {
        this.actionType = actionType;
    }

    public int getSeqNo() {
        return seqNo;
    }

    private void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public InteropActionState getState() {
        return state;
    }

    public void setState(InteropActionState state) {
        this.state = state;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }
//
//    public String getCharges() {
//        return charges;
//    }
//
//    public void setCharges(String charges) {
//        this.charges = charges;
//    }
//
//    public String getLedgers() {
//        return ledgers;
//    }
//
//    public void setLedgers(String ledgers) {
//        this.ledgers = ledgers;
//    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    private void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    private void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InteropActionEntity that = (InteropActionEntity) o;

        return identifier.equals(that.identifier);

    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
