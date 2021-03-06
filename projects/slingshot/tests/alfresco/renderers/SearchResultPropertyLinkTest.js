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
      name: 'SearchResultPropertyLink Test',
      'SearchResultPropertyLinkTest': function () {
         var browser = this.remote;
         var testname = "SearchResultPropertyLinkTest";
         return TestCommon.bootstrapTest(this.remote, "./tests/alfresco/renderers/page_models/SearchResultPropertyLink_TestPage.json", testname)

         .end()

         // 1. Check that there are 4 anchors
         .elementsByCss("a.alfresco-navigation-_HtmlAnchorMixin")
            .then(function(elements) {
               TestCommon.log(testname,43,"Check that all 4 links have anchors");
               assert(elements.length == 4, "Test #1 - 'path' not initialised correctly");
            })
            .end()

         // 2. Use the keyboard to click each in turn
         .keys(specialKeys["Tab"])
         .keys(specialKeys["Return"])
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "type", "SHARE_PAGE_RELATIVE"))
            .then(function(elements) {
               TestCommon.log(testname,53,"Check that 'SHARE_PAGE_RELATIVE' is set as the type");
               assert(elements.length == 1, "Test #2a - 'SHARE_PAGE_RELATIVE' was not set as the navigation type");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "target", "CURRENT"))
            .then(function(elements) {
               TestCommon.log(testname,59,"Check that 'CURRENT' is set as the target");
               assert(elements.length == 1, "Test #2b - 'SHARE_PAGE_RELATIVE' was not set as the navigation target");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "url", "site/site1/document-details?nodeRef=workspace://SpacesStore/some-fake-uuid"))
            .then(function(elements) {
               TestCommon.log(testname,65,"Check that url is correct for site document");
               assert(elements.length == 1, "Test #2c - site document search result URL was incorrect");
            })
            .end()

         .keys(specialKeys["Tab"])
         .keys(specialKeys["Return"])
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "url", "site/site1/documentlibrary?path=/folder1/folder2/folder3"))
            .then(function(elements) {
               TestCommon.log(testname,74,"Check that url is correct for site folder");
               assert(elements.length == 1, "Test #2d - site folder search result URL was incorrect");
            })
            .end()

         .keys(specialKeys["Tab"])
         .keys(specialKeys["Return"])
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "url", "document-details?nodeRef=workspace://SpacesStore/some-fake-uuid"))
            .then(function(elements) {
               TestCommon.log(testname,83,"Check that url is correct for repo document");
               assert(elements.length == 1, "Test #2e - repo document search result URL was incorrect");
            })
            .end()

         .keys(specialKeys["Tab"])
         .keys(specialKeys["Return"])
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "url", "repository?path=/folder1/folder2/folder3"))
            .then(function(elements) {
               TestCommon.log(testname,92,"Check that url is correct for repo document");
               assert(elements.length == 1, "Test #2f - repo document search result URL was incorrect");
            })
            .end()

         // 3. Use the mouse to click each in turn
         .elementByCss("#SITE_DOC_LINK span.inner a")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "url", "site/site1/document-details?nodeRef=workspace://SpacesStore/some-fake-uuid"))
            .then(function(elements) {
               TestCommon.log(testname,104,"Check that url is correct for site document");
               assert(elements.length == 1, "Test #3a - site document search result URL was incorrect");
            })
            .end()
         .elementByCss("#SITE_FOLDER_LINK span.inner a")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "url", "site/site1/documentlibrary?path=/folder1/folder2/folder3"))
            .then(function(elements) {
               TestCommon.log(testname,74,"Check that url is correct for site folder");
               assert(elements.length == 1, "Test #3b - site folder search result URL was incorrect");
            })
            .end()

         .elementByCss("#REPO_DOC_LINK span.inner a")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "url", "document-details?nodeRef=workspace://SpacesStore/some-fake-uuid"))
            .then(function(elements) {
               TestCommon.log(testname,83,"Check that url is correct for repo document");
               assert(elements.length == 1, "Test #3c - repo document search result URL was incorrect");
            })
            .end()

         .elementByCss("#REPO_FOLDER_LINK span.inner a")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "url", "repository?path=/folder1/folder2/folder3"))
            .then(function(elements) {
               TestCommon.log(testname,92,"Check that url is correct for repo document");
               assert(elements.length == 1, "Test #3d - repo document search result URL was incorrect");
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