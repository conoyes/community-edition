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
      name: 'AlfSearchList Test',
      'AlfSearchListTest': function () {
         var browser = this.remote;
         var testname = "AlfSearchListTest";
         return TestCommon.bootstrapTest(this.remote, "./tests/alfresco/documentlibrary/page_models/SearchList_TestPage.json", testname)

         .end()
         
         // Include a facet with no data...
         .elementByCss("#INCLUDE_FACET_0")
            .moveTo()
            .click()
            .end()
         // Include an initial facet (for code coverage)...
         .elementByCss("#INCLUDE_FACET_1")
            .moveTo()
            .click()
            .end()

         // The initial AlfSearchList doesn't perform an initial search when "useHash" is set to true (which
         // for this test it is).

         // Check that no request to search exists...
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,46,"Check that no search request is used when 'useHash' is enabled");
               assert(elements.length == 0, "Test #1 - Search term set unexpectedly");
            })
            .end()

         // Click the button to set a search term (but don't actually provide one)
         .elementByCss("#SET_SEARCH_TERM_1")
            .moveTo()
            .click()
            .end()

         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,59,"Check that no search request is used when an empty search term is provided");
               assert(elements.length == 0, "Test #2a - Search term set unexpectedly");
            })
            .end()

         // Click the button to set the search term...
         .elementByCss("#SET_SEARCH_TERM_2")
            .moveTo()
            .click()
            .end()

         // Check that updating the hash results in a search request being made...
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "term", "testTerm1"))
            .then(function(elements) {
               TestCommon.log(testname,73,"Check setting the searchTerm provided requests the appropriate search");
               assert(elements.length == 1, "Test #2b - Search term didn't request search");
            })
            .end()
         // Make sure our facet was included...
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "facetFields", "qname1"))
            .then(function(elements) {
               TestCommon.log(testname,80,"Check that facet fields have been requested");
               assert(elements.length == 1, "Test #2b.1 - facet fields not set appropriately from request");
            })
            .end()

         // Click the button to set the SAME search term (a new request shouldn't be issued)...
         .elementByCss("#SET_SEARCH_TERM_2")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,85,"Check setting the same searchTerm doesn't request another search");
               assert(elements.length == 1, "Test #2c - Duplicate search term made the same search (before response provided)");
            })
            .end()

         // Click the button to set a DIFFERENT search term (a new request SHOULD be issued)...
         .elementByCss("#SET_SEARCH_TERM_3")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,97,"Check setting the same searchTerm doesn't request another search");
               assert(elements.length == 2, "Test #2d - New search term made issues a new search request");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "term", "testTerm2"))
            .then(function(elements) {
               TestCommon.log(testname,103,"Check setting the same searchTerm doesn't request another search");
               assert(elements.length == 1, "Test #2e - Duplicate search term made the same search (before response provided)");
            })
            .end()

         // Send some response data, and then issue the search again, there should now be 3 search 
         // requests and the last one should be for the last search term...
         .elementByCss("#PUBLISH_SEARCH_RESULTS")
            .moveTo()
            .click()
            .end()
         .elementByCss("#SET_SEARCH_TERM_3")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,120,"Check setting the same searchTerm doesn't request another search");
               assert(elements.length == 3, "Test #2f - New search term made issues a new search request");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "term", "testTerm2"))
            .then(function(elements) {
               TestCommon.log(testname,126,"Check setting the same searchTerm doesn't request another search");
               assert(elements.length == 1, "Test #2g - Duplicate search term made the same search (before response provided)");
            })
            .end()

         // Click the button to set a variety of data (including facet filters)....
         // Set the same term again to check that the filters are removed...
         .elementByCss("#SET_MULTIPLE_SEARCH_DATA")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "filters", "filter1,filter2,filter3"))
            .then(function(elements) {
               TestCommon.log(testname,139,"Check setting multiple hash attribute requests the appropriate search (facet fields)");
               assert(elements.length == 1, "Test #2h - facet fields not set appropriately from hash change");
            })
            .end()
         // Ensure the next search term will be set by publishing some data (to prevent concurrent request blocking)...
         .elementByCss("#PUBLISH_SEARCH_RESULTS")
            .moveTo()
            .click()
            .end()
         .elementByCss("#SET_SEARCH_TERM_3")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "term", "testTerm2"))
            .then(function(elements) {
               TestCommon.log(testname,154,"Check setting the same searchTerm doesn't request another search");
               assert(elements.length == 1, "Test #2i - Duplicate search term clears previously set filters");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "filters", ""))
            .then(function(elements) {
               TestCommon.log(testname,160,"Check setting multiple hash attribute requests the appropriate search (facet fields)");
               assert(elements.length == 1, "Test #2j - Facet fields cleared");
            })
            .end()

         // Test scope settings...
         // Set empty scope...
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,169,"Check number of searches made before updating scope");
               assert(elements.length == 5, "Test #3a - Unexpected number of search requests made before testing scope settings " + elements.length);
            })
            .end()
         .elementByCss("#SET_SCOPE_0")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,179,"Check that setting null scope doesn't issue search request");
               assert(elements.length == 5, "Test #3b - Setting a null scope issued a search request");
            })
            .end()
         .elementByCss("#SET_SCOPE_1")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,189,"Check that re-setting the current scope doesn't issue a search request");
               assert(elements.length == 5, "Test #3c - Setting the current scope issued a search request.");
            })
            .end()

         .elementByCss("#SET_SCOPE_2")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,200,"Check that setting a new scope issues a new search request");
               assert(elements.length == 6, "Test #3d - Setting a new scope didn't issue a search request");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "repo", "false"))
            .then(function(elements) {
               TestCommon.log(testname,206,"Check that setting ALL_SITES scope makes REPO false");
               assert(elements.length == 1, "Test #3e - Repo param not set to false when ALL_SITES scope set");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "site", ""))
            .then(function(elements) {
               TestCommon.log(testname,212,"Check that setting ALL_SITES scope makes site empty");
               assert(elements.length == 1, "Test #3f - Site data passed when ALL_SITES scope set");
            })
            .end()

         .elementByCss("#SET_SCOPE_3")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,223,"Check that setting the site scope issues a search request");
               assert(elements.length == 7, "Test #3g - Setting a site scope didn't issue a search request");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "repo", "false"))
            .then(function(elements) {
               TestCommon.log(testname,229,"Check that setting site scope makes REPO false");
               assert(elements.length == 1, "Test #3h - Repo param not set to false when ALL_SITES scope set");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "site", "site1"))
            .then(function(elements) {
               TestCommon.log(testname,235,"Check that setting site scope sets site param");
               assert(elements.length == 1, "Test #3i - Site data not when site scope set");
            })
            .end()

         .elementByCss("#SET_SCOPE_1")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,246,"Check that setting the REPO scope issues a search request");
               assert(elements.length == 8, "Test #3j - Setting the REPO scope didn't issue a search request");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "repo", "true"))
            .then(function(elements) {
               TestCommon.log(testname,252,"Check that setting REPO scope makes repo param true");
               assert(elements.length == 1, "Test #3k - Repo param not set to true when REPO scope set");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "site", ""))
            .then(function(elements) {
               TestCommon.log(testname,258,"Check that setting REPO scope clears site param");
               assert(elements.length == 1, "Test #3l - Site data not cleared when REPO scope set");
            })
            .end()


         // Check that updating the hash results in a search request being made...
         .elementByCss("#SET_MULTIPLE_SEARCH_DATA")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "term", "testTerm2"))
            .then(function(elements) {
               TestCommon.log(testname,74,"Check setting multiple hash attribute requests the appropriate search (search term)");
               assert(elements.length == 1, "Test #3a - search term not set appropriately from hash change");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "facetFields", "qname1"))
            .then(function(elements) {
               TestCommon.log(testname,80,"Check setting multiple hash attribute requests the appropriate search (facet fields)");
               assert(elements.length == 1, "Test #3b - facet fields not set appropriately from hash change");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "filters", "filter1,filter2,filter3"))
            .then(function(elements) {
               TestCommon.log(testname,86,"Check setting multiple hash attribute requests the appropriate search (facet filters)");
               assert(elements.length == 1, "Test #3c - facet filters not set appropriately from hash change");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "sortAscending", "false"))
            .then(function(elements) {
               TestCommon.log(testname,92,"Check setting multiple hash attribute requests the appropriate search (sort order)");
               assert(elements.length == 1, "Test #3d - sort order not set appropriately from hash change");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "sortField", "cm:title"))
            .then(function(elements) {
               TestCommon.log(testname,98,"Check setting multiple hash attribute requests the appropriate search (sort property)");
               assert(elements.length == 1, "Test #3e - sort property not set appropriately from hash change");
            })
            .end()

         // Click the button to remove filter2 from the filters list...
         .elementByCss("#REMOVE_FACET_FILTER")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "filters", "filter1,filter3"))
            .then(function(elements) {
               TestCommon.log(testname,110,"Checking removal of facet filter");
               assert(elements.length == 1, "Test #4 - facet filter 'filter2' was not removed");
            })
            .end()

         // Test facet includes...
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,246,"Check the number of previous searches before including facets");
               assert(elements.length == 10, "Test #5a - Unexpected number of previous searches " + elements.length);
            })
            .end()

         // Click the button to include an additional facet in search requests...
         .elementByCss("#INCLUDE_FACET_2")
            .moveTo()
            .click()
            .end()

         .elementByCss("#APPLY_FACET_FILTER_0")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,246,"Check that setting bad facet filters doesn't trigger a search");
               assert(elements.length == 10, "Test #5b - bad facet filter triggered search");
            })
            .end()

         // Click the button to add filter4 to the filters list...
         .elementByCss("#APPLY_FACET_FILTER")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,246,"Check that applying a new filter triggers a search request");
               assert(elements.length == 11, "Test #5c - applying a filter didn't trigger a search");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "filters", "filter1,filter3,filter4"))
            .then(function(elements) {
               TestCommon.log(testname,128,"Checking application of facet filter");
               assert(elements.length == 1, "Test #5d - facet filter 'filter4' was not applied");
            })
            .end()

         // Check that the additional qname was included...
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "facetFields", "qname1,qname2"))
            .then(function(elements) {
               TestCommon.log(testname,136,"Checking inclusion of facet");
               assert(elements.length == 1, "Test #6 - additional facet qnames were not included");
            })
            .end()

         // Publish data to prevent block on concurrent requests...
         .elementByCss("#PUBLISH_SEARCH_RESULTS")
            .moveTo()
            .click()
            .end()
         .elementByCss("#SET_MULTIPLE_SEARCH_DATA_2")
            .moveTo()
            .click()
            .end()
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_REQUEST", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,246,"Check that setting new hash data triggers a new search");
               assert(elements.length == 12, "Test #7a - setting different multiple data didn't trigger a new search");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "term", "testTerm3"))
            .then(function(elements) {
               TestCommon.log(testname,74,"New search term not applied");
               assert(elements.length == 1, "Test #7b - search term not set appropriately from hash change");
            })
            .end()
         .elementsByCss(TestCommon.pubSubDataCssSelector("last", "filters", "filter1,filter2,filter3"))
            .then(function(elements) {
               TestCommon.log(testname,86,"Check that setting a new search term clears the previous filters");
               assert(elements.length == 1, "Test #7c - previous filters not cleared on new search term");
            })
            .end()

         // Click the button to generate a fake search request response...
         .elementByCss("#PUBLISH_SEARCH_RESULTS")
            .moveTo()
            .click()
            .end()

         // Check that facet data is published... NOTE: TOPIC THROWS AN ERROR IN TESTING - MUST BE THE @
         // .elementsByCss(TestCommon.topicSelector("ALF_FACET_RESULTS_@qname1", "publish", "any"))
         //    .then(function(elements) {
         //       TestCommon.log(testname,149,"Check that facet search results were published");
         //       assert(elements.length == 1, "Test #7 - Search result facet data not published");
         //    })
         //    .end()

         // Commented out whilst updating the unit test...
         // Check that search result count is published...
         .elementsByCss(TestCommon.topicSelector("ALF_SEARCH_RESULTS_COUNT", "publish", "any"))
            .then(function(elements) {
               TestCommon.log(testname,158,"Check that search resultset size is published");
               assert(elements.length > 0, "Test #8 - Search resultset size not published");
            })
            .end()

         // // Test that Number of search results is as expected.
            .elementsByCss(".alfresco-search-AlfSearchResult")
            .then(function(elements) {
               TestCommon.log(testname,166,"Check that 3 results are displayed");
               assert(elements.length === 3, "Test #9 - Number of results expected is 3, actual results displayed is: " + elements.length);
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