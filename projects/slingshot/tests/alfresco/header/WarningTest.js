/**
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Dave Draper
 */
define(["intern!object",
        "intern/chai!expect",
        "intern/chai!assert",
        "require",
        "alfresco/TestCommon"], 
        function (registerSuite, expect, assert, require, TestCommon) {

   registerSuite({
      name: 'Warning Test',
      'alfresco/header/Warning': function () {

         var browser = this.remote;
         var testname = "WarningTest";
         return TestCommon.bootstrapTest(this.remote, "./tests/alfresco/header/page_models/Warning_TestPage.json", testname)

         .end()

         .elementByCssSelector("#WARNINGS1 > div.warnings > div.info > span:last-child")
            .text()
            .then(function (result1) {
               TestCommon.log(testname,43, "Test 1a: Check warning");
               expect(result1).to.equal("WARNING", "Test 1a - Warning not displayed");
            })
            .end()

         .elementByCssSelector("#WARNINGS2 > div.warnings > div.info > span:last-child")
            .text()
            .then(function (result1) {
               TestCommon.log(testname,51, "Test 1b: Check error");
               expect(result1).to.equal("ERROR", "Test 1b - Error not displayed");
            })
            .end()
         
         .elementByCssSelector("#LICENSEWARNING_READONLY > div.warnings > div.info > span:last-child")
            .text()
            .then(function (result1) {
               TestCommon.log(testname,59, "Test 1c: Check readonly message");
               expect(result1).to.equal("Alfresco is running in READ ONLY mode. Please consult your System Administrator to resolve this.", "Test 1c - Readonly error not displayed");
            })
            .end()

         .elementsByCssSelector("#LICENSEWARNING_DISPLAY_TO_ADMIN > div.warnings > div.info")
            .then(function (adminWarnings) {
               TestCommon.log(testname,46,"Test 2a - Test that admins see low severity warnings");
               expect(adminWarnings).to.have.length(3, "Test 2a - Admins should see low severity warnings");
            })
            .end()

         .elementsByCssSelector("#LICENSEWARNING_HIDE_FROM_USER > div.warnings > div.info")
            .then(function (nonAdminWarnings) {
               TestCommon.log(testname,46,"Test 2b - Test that non-admin users don't see low severity warnings");
               expect(nonAdminWarnings).to.have.length(0, "Test 2b - Low severity warnings should be hidden from non-admins");
            })
            .end()

         .elementsByCssSelector("#LICENSEWARNING_DISPLAY_TO_USER > div.warnings > div.info")
            .then(function (nonAdminWarnings) {
               TestCommon.log(testname,46,"Test 2c - Test that non-admin users see high severity warnings");
               expect(nonAdminWarnings).to.have.length(3, "Test 2c - High severity warnings should be displayed to non-admins");
            })
            .end()

         // Post the coverage results...
         .then(function() {
            TestCommon.postCoverageResults(browser);
         });
      }
   });
});