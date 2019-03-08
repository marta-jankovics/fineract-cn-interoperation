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

-- sample data initialization

INSERT INTO horus_offices (identifier, a_name, description, created_by, created_on)
VALUES ('Headquarter', 'Headquarter', 'Demo Headquarter', 'operator', CURDATE());

SET @office_id = -1;
SELECT id INTO @office_id FROM horus_offices WHERE identifier = 'Headquarter';

INSERT INTO horus_addresses (office_id, street, city, country_code, country)
VALUES (@office_id, 'Headquarter street', 'Headquarter city', 'IC', 'Inclusia');

INSERT INTO maat_addresses (street, city, country_code, country)
VALUES ('Customer street', 'Customer city', 'IC', 'Inclusia');


SET @ledger_1300_id = -1;
SELECT id INTO @ledger_1300_id FROM thoth_ledgers WHERE identifier = '1300';
SET @ledger_3700_id = -1;
SELECT id INTO @ledger_3700_id FROM thoth_ledgers WHERE identifier = '3700';
SET @ledger_7300_id = -1;
SELECT id INTO @ledger_7300_id FROM thoth_ledgers WHERE identifier = '7300';
SET @ledger_7900_id = -1;
SELECT id INTO @ledger_7900_id FROM thoth_ledgers WHERE identifier = '7900';
SET @ledger_8100_id = -1;
SELECT id INTO @ledger_8100_id FROM thoth_ledgers WHERE identifier = '8100';
SET @ledger_9100_id = -1;
SELECT id INTO @ledger_9100_id FROM thoth_ledgers WHERE identifier = '9100';

INSERT INTO thoth_accounts (identifier, a_name, a_type, balance, ledger_id, a_state, created_by, created_on)
VALUES ('97b1470ffd664cb799c3a9', 'Interoperation Payable Liability', 'LIABILITY', 1000000, @ledger_8100_id, 'OPEN', 'operator', CURDATE());
INSERT INTO thoth_accounts (identifier, a_name, a_type, balance, ledger_id, a_state, created_by, created_on)
VALUES ('353388f8c343445eac1bd6', 'Interoperation NOSTRO', 'ASSET', 1000000, @ledger_7900_id, 'OPEN', 'operator', CURDATE());
INSERT INTO thoth_accounts (identifier, a_name, a_type, balance, ledger_id, a_state, created_by, created_on)
VALUES ('0faaf81e05674f19859f18', 'Interoperation Product Cash', 'ASSET', 0, @ledger_7300_id, 'OPEN', 'operator', CURDATE());
INSERT INTO thoth_accounts (identifier, a_name, a_type, balance, ledger_id, a_state, created_by, created_on)
VALUES ('72df4f6613a911e9ab14d6', 'Interoperation Product Expenses', 'EXPENSE', 0, @ledger_3700_id, 'OPEN', 'operator', CURDATE());
INSERT INTO thoth_accounts (identifier, a_name, a_type, balance, ledger_id, a_state, created_by, created_on)
VALUES ('65f7de0e13a811e9ab14d6', 'Interoperation Product Accrue Liability', 'LIABILITY', 0, @ledger_8100_id, 'OPEN', 'operator', CURDATE());
INSERT INTO thoth_accounts (identifier, a_name, a_type, balance, ledger_id, a_state, created_by, created_on)
VALUES ('020323b613aa11e9ab14d6', 'Interoperation Product Equity', 'EQUITY', 0, @ledger_9100_id, 'OPEN', 'operator', CURDATE());
INSERT INTO thoth_accounts (identifier, a_name, a_type, balance, ledger_id, a_state, created_by, created_on)
VALUES ('87d607ba13aa11e9ab14d6', 'Interoperation Product Fees Revenue', 'REVENUE', 1000000, @ledger_1300_id, 'OPEN', 'operator', CURDATE());