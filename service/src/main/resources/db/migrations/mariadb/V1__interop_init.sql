--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

CREATE TABLE hathor_identifiers (
  id                          BIGINT       NOT NULL AUTO_INCREMENT,
  customer_account_identifier VARCHAR(32)  NOT NULL,
  type                        VARCHAR(32)  NOT NULL,
  a_value                     VARCHAR(128) NOT NULL,
  sub_value_or_type           VARCHAR(128) NULL,
  created_by                  VARCHAR(32)  NOT NULL,
  created_on                  TIMESTAMP(3) NOT NULL,
  last_modified_by            VARCHAR(32)  NULL,
  last_modified_on            TIMESTAMP(3) NULL,
  CONSTRAINT pk_hathor_identifiers PRIMARY KEY (id),
  CONSTRAINT uk_hathor_identifiers_value UNIQUE (type, a_value, sub_value_or_type)
);


CREATE TABLE hathor_transactions (
  id                          BIGINT         NOT NULL AUTO_INCREMENT,
  identifier                  VARCHAR(36)    NOT NULL,
  a_name                      VARCHAR(256)   NULL,
  description                 VARCHAR(1024)  NULL,
  transaction_type            VARCHAR(32)    NOT NULL,
  amount                      NUMERIC(22, 4) NOT NULL,
  state                       VARCHAR(32)    NOT NULL,
  customer_account_identifier VARCHAR(32)    NOT NULL,
  payable_account_identifier  VARCHAR(32)    NULL,
  nostro_account_identifier   VARCHAR(32)    NOT NULL,
  transaction_date            TIMESTAMP(3)   NULL,
  expiration_date             TIMESTAMP(3)   NULL,
  created_by                  VARCHAR(32)    NOT NULL,
  created_on                  TIMESTAMP(3)   NOT NULL,
  last_modified_by            VARCHAR(32)    NULL,
  last_modified_on            TIMESTAMP(3)   NULL,
  CONSTRAINT pk_hathor_transactions PRIMARY KEY (id),
  CONSTRAINT uk_hathor_transactions_id UNIQUE (identifier)
);


CREATE TABLE hathor_actions (
  id              BIGINT         NOT NULL AUTO_INCREMENT,
  identifier      VARCHAR(64)    NOT NULL,
  transaction_id  BIGINT         NOT NULL,
  action_type     VARCHAR(32)    NOT NULL,
  seq_no          INT            NOT NULL,
  state           VARCHAR(32)    NOT NULL,
  amount          NUMERIC(22, 4) NOT NULL,
  fee             NUMERIC(22, 4) NULL,
  commission      NUMERIC(22, 4) NULL,
  error_code      CHAR(4)        NULL,
  error_msg       VARCHAR(128)   NULL,
  expiration_date TIMESTAMP(3)   NULL,
  created_by      VARCHAR(32)    NOT NULL,
  created_on      TIMESTAMP(3)   NOT NULL,
  CONSTRAINT pk_hathor_identifiers PRIMARY KEY (id),
  CONSTRAINT uk_hathor_actions_id UNIQUE (identifier),
  CONSTRAINT uk_hathor_actions_type UNIQUE (transaction_id, action_type),
  CONSTRAINT uk_hathor_actions_seq UNIQUE (transaction_id, seq_no),
  CONSTRAINT fk_hathor_actions_trans FOREIGN KEY (transaction_id) REFERENCES hathor_transactions (id)
);

-- data initialization

INSERT INTO thoth_tx_types (identifier, a_name, description)
VALUES ('FCWD', 'Interoperation withdrawal', 'Demo FCWD Interoperation withdrawal');
INSERT INTO thoth_tx_types (identifier, a_name, description)
VALUES ('FCDP', 'Interoperation deposit', 'Demo FCDP Interoperation deposit');


INSERT INTO shed_actions (identifier, a_name, description, transaction_type)
VALUES ('InteropWithdraw', 'Interoperation withdrawal', 'Demo FCWD Interoperation withdrawal', 'FCWD');
INSERT INTO shed_actions (identifier, a_name, description, transaction_type)
VALUES ('InteropDeposit', 'Interoperation deposit', 'Demo FCDP Interoperation deposit', 'FCDP');

