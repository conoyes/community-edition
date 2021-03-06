/**
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
        "alfresco/TestCommon",
        "intern/dojo/node!wd/lib/special-keys"], 
        function (registerSuite, expect, assert, require, TestCommon, specialKeys) {

   registerSuite({
      name: 'Footer Test',
      'FooterTest': function () {
         var browser = this.remote;
         var testname = "FooterTest";
         return TestCommon.bootstrapTest(this.remote, "./tests/alfresco/footer/page_models/Footer_TestPage.json", testname)

         .end()

         // This isn't the most comprehenive set of tests...
         // 1) It's not obvious how to test that the footer is stuck to the bottom without visually checking
         // 2) Some of the config could be further tested
         // It's good enough as a starting point though.
         .elementByCss(".alfresco-footer-AlfShareFooter span.copyright span:last-child")
            .text()
            .then(function (text) {
               TestCommon.log(testname,43,"Check copyright text");
               expect(text).to.equal("SOME COPYRIGHT LABEL", "The copyright has not been set correctly");
            })
            .end()

         .elementByCss(".alfresco-footer-AlfShareFooter .licenseHolder")
            .text()
            .then(function (text) {
               TestCommon.log(testname,51,"Check license text");
               expect(text).to.equal("Licensed To: SOME LICENSE LABEL", "The license label was not set correctly");
            })
            .end()

         // Post the coverage results...
         .then(function() {
            TestCommon.postCoverageResults(browser);
         })
         .end();
      }
   });
});