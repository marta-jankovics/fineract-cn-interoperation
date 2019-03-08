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

USE tn02;

INSERT INTO maat_customers (identifier, a_type, given_name, surname, date_of_birth, is_member, current_state, address_id, created_by, created_on)
  VALUES ('InteropMerchant', 'PERSON', 'Interoperation', 'Merchant', '2000-01-01', 1, 'ACTIVE',
        (SELECT id FROM maat_addresses WHERE street = 'Customer street'),
        'operator', CURDATE());


SET @ledger_7900_id = -1;
SELECT id INTO @ledger_7900_id FROM thoth_ledgers WHERE identifier = '7900';

SET @account_payable_id = -1;
SELECT id INTO @account_payable_id FROM thoth_accounts WHERE identifier = '97b1470ffd664cb799c3a9';

SET @account_customer_id = '83c4ed1c074b484e85cc79';

INSERT INTO thoth_accounts (identifier, a_name, a_type, balance, reference_account_id, ledger_id, a_state, created_by, created_on)
VALUES (@account_customer_id, 'Interoperation Merchant Account', 'ASSET', 1000000, @account_payable_id, @ledger_7900_id, 'OPEN', 'operator', CURDATE());


INSERT INTO hathor_identifiers (customer_account_identifier, type, a_value, sub_value_or_type, created_by, created_on)
VALUES (@account_customer_id, 'IBAN', 'IC11in01tn0283c4ed1c074b484e85cc79', null, 'operator', CURDATE());
INSERT INTO hathor_identifiers (customer_account_identifier, type, a_value, sub_value_or_type, created_by, created_on)
VALUES (@account_customer_id, 'MSISDN', '27710102999', null, 'operator', CURDATE());


SET @action_withdraw_id = -1;
SELECT id INTO @action_withdraw_id FROM shed_actions WHERE identifier = 'InteropWithdraw';

INSERT INTO shed_product_definitions (identifier, a_name, description, a_type, minimum_balance, equity_ledger_identifier,
                                      cash_account_identifier, expense_account_identifier, accrue_account_identifier, is_flexible,
                                      is_active, created_by, created_on)
  VALUES ('InteropMerchantProduct', 'Interoperation Merchant Product', 'Demo Interoperation Product', 'SAVINGS', 100, '9100', '0faaf81e05674f19859f18',
          '72df4f6613a911e9ab14d6', '65f7de0e13a811e9ab14d6', 0, 1,  'operator', CURDATE());

SET @prod_def_id = -1;
SELECT id INTO @prod_def_id FROM shed_product_definitions WHERE identifier = 'InteropMerchantProduct';

INSERT INTO shed_currencies (product_definition_id, a_code, a_name, sign, scale)
  VALUES (@prod_def_id, 'TZS', 'Inclusia Rupee', 'TSh', 2);

INSERT INTO shed_terms (product_definition_id, period, time_unit, interest_payable)
  VALUES (@prod_def_id, 1, 'YEAR', 'ANNUALLY');

INSERT INTO shed_charges (product_definition_id, action_id, a_name, description, income_account_identifier, proportional, amount)
VALUES (@prod_def_id, @action_withdraw_id, 'Interoperation Withdraw Fee', 'Interoperation Revenue Fees', '87d607ba13aa11e9ab14d6', 1, 1.00);

INSERT INTO shed_product_instances (product_definition_id, customer_identifier, account_identifier, a_state, opened_on, created_by, created_on)
VALUES (@prod_def_id, 'InteropMerchant', @account_customer_id, 'ACTIVE', CURDATE(), 'operator', CURDATE());