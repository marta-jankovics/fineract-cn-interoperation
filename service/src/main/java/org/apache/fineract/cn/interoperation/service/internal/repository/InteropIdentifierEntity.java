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

import org.apache.fineract.cn.interoperation.api.v1.domain.InteropIdentifierType;
import org.apache.fineract.cn.mariadb.util.LocalDateTimeConverter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "hathor_identifiers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hathor_identifiers_value", columnNames = {"type", "a_value", "sub_value_or_type"})
})
public class InteropIdentifierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_account_identifier", nullable = false, length = 32)
    private String customerAccountIdentifier;

    @Column(name = "type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private InteropIdentifierType type;

    @Column(name = "a_value", nullable = false, length = 128)
    private String value;

    @Column(name = "sub_value_or_type", length = 128)
    private String subValueOrType;

    @Column(name = "created_by", nullable = false, length = 32)
    private String createdBy;

    @Column(name = "created_on", nullable = false)
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime createdOn;

    @Column(name = "last_modified_by", length = 32)
    private String lastModifiedBy;

    @Column(name = "last_modified_on")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime lastModifiedOn;


    protected InteropIdentifierEntity() {
    }

    public InteropIdentifierEntity(@NotNull String customerAccountIdentifier, @NotNull InteropIdentifierType type, @NotNull String value,
                                   String subValueOrType, @NotNull String createdBy, @NotNull LocalDateTime createdOn) {
        this.customerAccountIdentifier = customerAccountIdentifier;
        this.type = type;
        this.value = value;
        this.subValueOrType = subValueOrType;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
    }

    public InteropIdentifierEntity(@NotNull String customerAccountIdentifier, @NotNull InteropIdentifierType type, @NotNull String createdBy,
                                   @NotNull LocalDateTime createdOn) {
        this(customerAccountIdentifier, type, null, null, createdBy, createdOn);
    }

    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public String getCustomerAccountIdentifier() {
        return customerAccountIdentifier;
    }

    private void setCustomerAccountIdentifier(String customerAccountIdentifier) {
        this.customerAccountIdentifier = customerAccountIdentifier;
    }

    public InteropIdentifierType getType() {
        return type;
    }

    private void setType(InteropIdentifierType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSubValueOrType() {
        return subValueOrType;
    }

    public void setSubValueOrType(String subValueOrType) {
        this.subValueOrType = subValueOrType;
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

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(LocalDateTime lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InteropIdentifierEntity that = (InteropIdentifierEntity) o;

        if (!customerAccountIdentifier.equals(that.customerAccountIdentifier)) return false;
        if (type != that.type) return false;
        if (!value.equals(that.value)) return false;
        return subValueOrType != null ? subValueOrType.equals(that.subValueOrType) : that.subValueOrType == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + (subValueOrType != null ? subValueOrType.hashCode() : 0);
        return result;
    }
}
