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
 * The purpose of this test is to ensure that keyboard accessibility is possible between the header and the 
 * main table. It should be possible to use the tab/shift-tab keys to navigate along the headers (and the enter/space key
 * to make requests for sorting) and then the cursor keys to navigate around the table itself.
 * 
 * @author Dave Draper
 */
define(["intern!object",
        "intern/chai!expect",
        "intern/chai!assert",
        "require",
        "alfresco/TestCommon"], 
        function (registerSuite, expect, assert, require, TestCommon) {

   registerSuite({
      name: 'PropertyLink Test',
      'alfresco/renderers/PropertyLink': function () {

         var browser = this.remote;
         var testname = "PropertyLinkTest";
         return TestCommon.bootstrapTest(this.remote, "./tests/alfresco/renderers/page_models/PropertyLink_TestPage.json", testname)

         .end()

         .elementByCss("#LIST_WITH_HEADER_ITEMS tr:first-child td span.inner")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "name", "Site1"))
            .then(function(elements) {
               TestCommon.log(testname,49,"Check that currentItem is published");
               assert(elements.length == 1, "Test #1a - 'name' not included in currentItem data");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "urlname", "site1"))
            .then(function(elements) {
               TestCommon.log(testname,55,"Check that currentItem is published");
               assert(elements.length == 1, "Test #1b - 'urlname' not included in currentItem data");
            })
            .end()

         .elementsByCss(TestCommon.topicSelector("publishTopic", "publish", "last"))
            .then(function(elements) {
               assert(elements.length == 1, "Test #1c - topic not published correctly");
            })
            .end()

         // Post the coverage results...
         .then(function() {
            TestCommon.postCoverageResults(browser);
         });
      }
   });
});