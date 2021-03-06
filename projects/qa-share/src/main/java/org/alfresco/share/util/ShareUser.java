package org.alfresco.share.util;


import org.alfresco.po.share.*;
import org.alfresco.po.share.NewGroupPage.ActionButton;
import org.alfresco.po.share.dashlet.*;
import org.alfresco.po.share.dashlet.MyActivitiesDashlet.LinkType;
import org.alfresco.po.share.dashlet.MySitesDashlet.FavouriteType;
import org.alfresco.po.share.exception.ShareException;
import org.alfresco.po.share.site.CustomizeSitePage;
import org.alfresco.po.share.site.SiteDashboardPage;
import org.alfresco.po.share.site.SitePage;
import org.alfresco.po.share.site.UpdateFilePage;
import org.alfresco.po.share.site.document.*;
import org.alfresco.po.share.site.document.ConfirmDeletePage.Action;
import org.alfresco.po.share.user.Language;
import org.alfresco.po.share.user.LanguageSettingsPage;
import org.alfresco.po.share.user.MyProfilePage;
import org.alfresco.webdrone.HtmlPage;
import org.alfresco.webdrone.RenderTime;
import org.alfresco.webdrone.WebDrone;
import org.alfresco.webdrone.exception.PageException;
import org.alfresco.webdrone.exception.PageRenderTimeException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.Keys;
import org.testng.SkipException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ShareUser extends AbstractUtils
{

    private static Log logger = LogFactory.getLog(ShareUser.class);

    public enum SiteOperation
    {
        Delete, Open;
    }
    
    public enum TypeOfPage
    {
        CopyTo,MoveTo;
    }
    
    public ShareUser()
    {
        if (logger.isTraceEnabled())
        {
            logger.debug(this.getClass().getSimpleName() + " instantiated");
        }
    }

    /**
     * User Log-in followed by deletion of session cookies Assumes User is *NOT* logged in.
     *
     * @param driver   WebDrone Instance
     * @param userInfo String username, password
     * @return boolean true: if log in succeeds
     */
    public static synchronized SharePage login(WebDrone driver, String... userInfo)
    {
        LoginPage loginPage;
        SharePage sharePage = null;

        try
        {
            if (userInfo.length < 2)
            {
                userInfo = getAuthDetails(userInfo[0]);
            }
            
            checkIfDriverNull(driver);
            driver.maximize();

            driver.navigateTo(dronePropertiesMap.get(driver).getShareUrl());

            sharePage = getSharePage(driver);
            // Logout if already logged in
            try
            {
                loginPage = sharePage.render();
            }
            catch (ClassCastException e)
            {
                loginPage = logout(driver).render();
            }

            logger.info("Start: Login: " + userInfo[0] + " Password: " + userInfo[1]);

            loginPage.loginAs(userInfo[0], userInfo[1]);
            sharePage = driver.getCurrentPage().render();

            if (!sharePage.isLoggedIn())
            {
                throw new ShareException("Method isLoggedIn return false");
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Failed: Login: " + userInfo[0] + " Password: " + userInfo[1] + " Error: " + e;
            logger.info(errorMessage);
            throw new SkipException(errorMessage);
        }

        return sharePage;
    }

    /**
     * User Log out using logout URL Assumes User is logged in.
     *
     * @param driver WebDrone Instance
     */
    public static synchronized HtmlPage logout(WebDrone driver)
    {
        HtmlPage currentPage = null;
        checkIfDriverNull(driver);
        try
        {
            if (driver.getCurrentUrl().contains(dronePropertiesMap.get(driver).getShareUrl()))
            {
                ShareUtil.logout(driver);
                logger.info("Logout");
                currentPage = driver.getCurrentPage().render(maxWaitTime);
            }
        }
        catch (Exception e)
        {
            // Already logged out.
            logger.info("already logged out" + e.getMessage());
        }
        return currentPage;
    }

    /**
     * User Log out followed by deletion of session cookies Assumes User is
     * logged in.
     *
     * @param driver WebDrone Instance
     * @return LoginPage
     */
    public static LoginPage logoutCleanCookies(WebDrone driver)
    {
        logout(driver);
        // driver.manage().deleteAllCookies();
        logger.info("Session Cleanup done");
        return driver.getCurrentPage().render();
    }

    /**
     * Navigate to User DashBoard and waits for the page render to complete.
     * Assumes User is logged in
     *
     * @param driver WebDrone Instance
     * @return DashBoardPage
     */
    public static DashBoardPage openUserDashboard(WebDrone driver)
    {
        // Assumes User is logged in
        SharePage page = getSharePage(driver);
        if (page.getPageTitle().contains(MY_DASHBOARD))
        {
            logger.info("User Dashboard already Open");
            return (DashBoardPage) page;
        }

        return refreshUserDashboard(driver);
    }

    /**
     * Navigate to User DashBoard page and waits for the page render to
     * complete. Assumes User is logged in
     *
     * @param driver WebDrone Instance
     * @return DashBoardPage
     */
    public static DashBoardPage refreshUserDashboard(WebDrone driver)
    {
        // Assumes User is logged in
        SharePage page = getSharePage(driver);

        page.getNav().selectMyDashBoard().render();
        logger.info("Opened User Dashboard");
        return new DashBoardPage(driver).render();
    }

    /**
     * From the User DashBoard, navigate to the Site DashBoard and waits for the
     * page render to complete. Assumes User is logged in.
     *
     * @param driver   WebDrone Instance
     * @param siteName String Name of the site to be opened
     * @return SiteDashboardPage
     * @throws PageException
     */
    public static SiteDashboardPage openSiteDashboard(WebDrone driver, String siteName) throws PageException
    {
        // Assumes User is logged in
        HtmlPage page = getSharePage(driver).render();
        
        // Check if site dashboard is already open. Return
        if (page instanceof SiteDashboardPage)
        {
            if (((SiteDashboardPage) page).isSite(siteName))
            {
                logger.info("Site dashboad page already open for site - " + siteName);                
                return page.render();
            }
        }
        
        // Check if site is already open. Open SiteDashBoard      
        if (page instanceof SitePage)
        {
            if (((SitePage) page).isSite(siteName))
            {
                logger.info("Selecting Site DashBoard for Site - " + siteName);                
                return refreshSiteDashboard(driver);
            }
        }
        
        
        // Open User DashBoard: Using MySitesDashlet
        DashBoardPage dashBoard = openUserDashboard(driver);
        MySitesDashlet dashlet = dashBoard.getDashlet(DASHLET_SITES).render(refreshDuration);

        SiteDashboardPage siteDashPage = dashlet.selectSite(siteName).click().render(maxWaitTime);
        
        logger.info("Opened Site Dashboard using User DashBoard - MySites Dashlet: " + siteName);

        // Open User DashBoard: Using SiteURL
        // SiteDashboardPage siteDashPage = SiteUtil.openSiteURL(driver, getSiteShortname(siteName));

        // Open User DashBoard: Using SiteFinder
        // SiteDashboardPage siteDashPage = SiteUtil.openSiteFromSearch(driver, siteName);

        // logger.info("Opened Site Dashboard using SiteURL: " + siteName);

        return siteDashPage;
    }

    /**
     * Open document Library: Top Level Assumes User is logged in and a Specific
     * Site is open.
     *
     * @param driver WebDrone Instance
     * @return DocumentLibraryPage
     */
    public static DocumentLibraryPage openDocumentLibrary(WebDrone driver)
    {
        // Assumes User is logged in
        /*
         * SharePage page = getSharePage(driver); if (page instanceof
         * DocumentLibraryPage) { return (DocumentLibraryPage) page; }
         */

        // Open DocumentLibrary Page from Site Page
        SitePage site = (SitePage) getSharePage(driver);

        DocumentLibraryPage docPage = site.getSiteNav().selectSiteDocumentLibrary().render();
        logger.info("Opened Document Library");
        return docPage;
    }

    /**
     * Refresh document Library: Refreshes the drone, if already on any Page of
     * the DocumentLibrary Instance Assumes User is logged in and a Specific
     * Site is open.
     *
     * @param driver WebDrone Instance
     * @return DocumentLibraryPage
     * @throws PageException if current page is not an instance of DocumentLibraryPage
     */
    public static DocumentLibraryPage refreshDocumentLibrary(WebDrone driver)
    {
        // Assumes User is logged in
        driver.refresh();
        SharePage page = getSharePage(driver);

        if (page instanceof DocumentLibraryPage)
        {
            logger.info("Reloaded Document Library Page");
            return (DocumentLibraryPage) page;
        }
        else
        {
            throw new PageException("Failed to refresh Document Library Page");
        }
    }

    /**
     * Open Site and then Open Document Library Assumes User is logged in and a
     * Specific Site Dashboard is open.
     *
     * @param driver   WebDrone Instance
     * @param siteName String Name of the Site
     * @return DocumentLibraryPage
     */
    public static DocumentLibraryPage openSitesDocumentLibrary(WebDrone driver, String siteName)
    {
        // Assumes User is logged in

        // Checking for site doc lib to be open.
        HtmlPage page = getSharePage(driver).render();
        if (page instanceof DocumentLibraryPage)
        {
            if (((DocumentLibraryPage) page).isSite(siteName) && ((DocumentLibraryPage) page).isDocumentLibrary())
            {
                logger.info("Site doc lib page open ");
                return ((DocumentLibraryPage) page);
            }
        }

        // Open Site
        openSiteDashboard(driver, siteName);

        // Open DocumentLibrary Page from SiteDashBoard
        DocumentLibraryPage docPage = openDocumentLibrary(driver);

        // Return DocLib Page
        return docPage;
    }

    /**
     * Create Site using Share UI. Assumes User is logged in.
     *
     * @param driver         WebDrone Instance
     * @param siteName
     * @param siteVisibility String Public, Moderated, Private
     */
    public static SiteDashboardPage createSite(WebDrone driver, String siteName, String siteVisibility) throws Exception
    {
        SiteDashboardPage site = null;
        try
        {
            boolean siteCreated = SiteUtil.createSite(driver, siteName, siteVisibility);
            if (siteCreated)
            {
                logger.info("Site Created:" + siteName);
                site = driver.getCurrentPage().render();
            }
            else
            {
                logger.info("Site has not been created");
            }
        }
        catch (Exception ex)
        {
            throw new SkipException("Skip test. Error in Create Site: " + ex);
        }
        return site;
    }

    /**
     * Create Site using Share UI. Assumes User is logged in.
     *
     * @param driver         WebDrone Instance
     * @param siteName
     * @param siteVisibility String Public, Moderated, Private
     */
    public static SiteDashboardPage createSite(WebDrone driver, String siteName, String siteVisibility, boolean returnIfAlreadyCreated) throws Exception
    {
        SiteDashboardPage site = null;
        try
        {
            boolean siteCreated = SiteUtil.createSite(driver, siteName, siteVisibility);
            if (siteCreated)
            {
                logger.info("Site Created:" + siteName);
                site = driver.getCurrentPage().render();
            }
            else
            {
                logger.info("Site has not been created");

            }
        }
        catch (Exception ex)
        {
            logger.info("Throw exception", ex);
            logger.info("Skip test. Error in Create Site: " + ex);
        }
        return site;
    }

    /**
     * Creates a new folder at the Path specified, Starting from the Document
     * Library Page. Assumes User is logged in and a specific Site is open.
     *
     * @param driver           WebDrone Instance
     * @param folderName       String Name of the folder to be created
     * @param folderDesc       String Description of the folder to be created
     * @param parentFolderPath String Path for the folder to be created, under
     *                         DocumentLibrary : such as constDoclib + file.seperator +
     *                         parentFolderName1 + file.seperator + parentFolderName2
     * @throws Exception
     */
    public static DocumentLibraryPage createFolderInFolder(WebDrone driver, String folderName, String folderDesc, String parentFolderPath) throws Exception
    {
        try
        {
            // Data setup Options: Use UI, Use API, Copy, Data preloaded?

            // Using Share UI
            // Navigate to the parent Folder where the file needs to be uploaded
            ShareUserSitePage.navigateToFolder(driver, parentFolderPath);

            // Create Folder
            return ShareUserSitePage.createFolder(driver, folderName, folderDesc);
        }
        catch (Exception ex)
        {
            throw new SkipException("Skip test. Error in Create Folder: " + ex.getMessage());
        }
    }

    /*
     * Navigates to the Path specified, starting from the Document Library Page.
     * Assumes User is logged in and a specific Site is open.
     * @param fileName String
     * @param parentFolderPath String folder path relative to documentLibrary:
     * e.g. CONST_DOCLIB + file.seperator + parentFolderName1
     * @throws SkipException if error in this API
     */
    public static void uploadFileInFolder1(WebDrone driver, String fileName, String parentFolderPath) throws Exception
    {
        String fileLocation = DATA_FOLDER + fileName;

        File file = newFile(fileLocation, "New File being created via newFile:" + fileName);

        checkIfDriverNull(driver);

        try
        {
            // Navigate to the parent Folder where the file needs to be uploaded
            ShareUserSitePage.navigateToFolder(driver, parentFolderPath);

            // Upload File
            ShareUserSitePage.uploadFile(driver, file);
        }
        catch (Exception e)
        {
            throw new SkipException("Skip test. Error in UploadFile: " + e);
        }

    }

    /**
     * Navigates to the Path specified, Starting from the Document Library Page.
     * Assumes User is logged in and a specific Site is open.
     *
     * @param fileInfo : such as constDoclib + file.seperator + parentFolderName1
     * @throws SkipException if error in this API
     */
    public static DocumentLibraryPage uploadFileInFolder(WebDrone driver, String[] fileInfo) throws Exception
    {
        String fileName = "";
        String parentFolderPath = "";
        String fileContents = "";

        Integer argCount = fileInfo.length;

        DocumentLibraryPage docLibraryPage = null;

        checkIfDriverNull(driver);

        if (argCount < 1)
        {
            throw new IllegalArgumentException("Specify at least Filename");
        }
        else
        {
            fileName = fileInfo[0];
        }

        if (argCount > 1)
        {
            parentFolderPath = fileInfo[1];
        }
        else
        {
            parentFolderPath = DOCLIB;
        }

        if (argCount > 2)
        {
            fileContents = fileInfo[2];
        }
        else
        {
            fileContents = "New File being created via newFile:" + fileName;
        }

        String fileLocation = DATA_FOLDER + fileName;

        File file = newFile(fileLocation, fileContents);

        // Data setup Options: Use UI, Use API, Copy, Data preloaded?
        try
        {
            // Using Share UI
            {
                // Navigate to the parent Folder where the file needs to be uploaded
                ShareUserSitePage.navigateToFolder(driver, parentFolderPath);

                // Upload File
                docLibraryPage = ShareUserSitePage.uploadFile(driver, file);

            }

            logger.info("File Uploaded: " + fileName);
        }
        catch (Exception e)
        {
            throw new SkipException("Skip test. Error in UploadFile: " + e);
        }

        return docLibraryPage;

    }

    /**
     * Helper to search for an Activity Entry on the User Dashboard Page, with
     * configurable retry search option.
     *
     * @param driver       WebDrone instance
     * @param dashlet      String Name of the Dashlet such as
     *                     activities,content,myDocuments etc
     * @param entry        String Entry to look for within the Dashlet
     * @param entryPresent String Parameter to indicate should the entry be visible
     *                     within the dashlet
     * @return true if result is as expected
     * @throws InterruptedException
     */
    public static Boolean searchMyDashBoardWithRetry(WebDrone driver, String dashlet, String entry, Boolean entryPresent)
    {
        Boolean found = false;
        Boolean resultAsExpected = false;

        // Integer searchTimeOut = refreshDuration*retrySearchCount;

        if (dashlet == null || dashlet.isEmpty())
        {
            dashlet = DASHLET_ACTIVITIES;
        }

        // Assumes User is logged in and User DashBoard is open

        // Open User DashBoard
        // openUserDashboard(driver);

        // Code to repeat search until the element is found or Timeout is hit
        for (int searchCount = 1; searchCount <= retrySearchCount; searchCount++)
        {
            if (searchCount > 1)
            {
                webDriverWait(driver, refreshDuration);
                refreshSharePage(driver).render();
            }

            if (dashlet.equals(DASHLET_ACTIVITIES))
            {
                List<ActivityShareLink> entries = getActivityDashletEntries(driver, dashlet);

                if (entries != null)
                {
                    found = findInActivitiesList(entries, entry);
                }
            }
            else
            {
                List<ShareLink> entries = getDashletEntries(driver, dashlet);

                if (entries != null)
                {
                    found = findInList(entries, entry);
                }
            }
            // Loop again if result is not as expected: To cater for solr lag:
            // eventual consistency
            resultAsExpected = (entryPresent.equals(found));
            if (resultAsExpected)
            {
                break;
            }
        }

        return resultAsExpected;
    }

    /**
     * Helper to search for an Element in the list of <ShareLinks>.
     *
     * @param driver  WebDrone Instance
     * @param dashlet String Name of the dashlet
     * @return List<ShareLink>: List of Share Links available in the dashlet
     */
    public static List<ActivityShareLink> getActivityDashletEntries(WebDrone driver, String dashlet)
    {
        List<ActivityShareLink> entries = null;

        DashBoardPage userDashBoard = getCurrentPage(driver).render();
        if (dashlet == null)
        {
            dashlet = DASHLET_ACTIVITIES;
        }

        if (dashlet.equals(DASHLET_ACTIVITIES))
        {
            MyActivitiesDashlet myActivities = userDashBoard.getDashlet(dashlet).render();
            entries = myActivities.getActivities();
        }
        else
        {
            throw new SkipException("Incorrect Dashlet");
        }

        return entries;
    }

    /**
     * Helper to search for an Element in the list of <ShareLinks>.
     *
     * @param driver  WebDrone Instance
     * @param dashlet String Name of the dashlet
     * @return List<ShareLink>: List of Share Links available in the dashlet
     */
    protected static List<ShareLink> getDashletEntries(WebDrone driver, String dashlet)
    {
        List<ShareLink> entries = null;

        DashBoardPage userDashBoard = (DashBoardPage) getSharePage(driver);
        if (dashlet == null)
        {
            dashlet = DASHLET_DOCUMENTS;
        }

        if (dashlet.equals(DASHLET_DOCUMENTS))
        {
            MyDocumentsDashlet myDocuments = userDashBoard.getDashlet(dashlet).render();
            entries = myDocuments.getDocuments();
        }
        else if (dashlet.equals(DASHLET_SITES))
        {
            MySitesDashlet mySites = userDashBoard.getDashlet(dashlet).render();
            entries = mySites.getSites();
        }
        else if (dashlet.equals(DASHLET_TASKS))
        {
            MyTasksDashlet myTasks = userDashBoard.getDashlet(dashlet).render();
            entries = myTasks.getTasks();
        }

        return entries;
    }

    /**
     * Helper to search for an Element in the list of <List<ActivityShareLink>>.
     *
     * @param entryList <List<ActivityShareLink>>
     * @param entry     String entry to be found in the ShareLinks' list
     * @return Boolean true if entry is found, false if not
     */
    protected static Boolean findInActivitiesList(List<ActivityShareLink> entryList, String entry)
    {
        for (ActivityShareLink link : entryList)
        {
            if (entry.equalsIgnoreCase(link.getDescription()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper to search for an Element in the list of <ShareLinks>.
     *
     * @param entryList <ShareLinks>
     * @param entry     String entry to be found in the ShareLinks' list
     * @return Boolean true if entry is found, false if not
     */
    protected static Boolean findInList(List<ShareLink> entryList, String entry)
    {
        for (ShareLink link : entryList)
        {
            if (entry.equalsIgnoreCase(link.getDescription()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Function mime the action of opening the document detail Page.
     *
     * @param driver      WebDrone Instance
     * @param contentName String Criteria for site search
     * @return {@link DocumentDetailsPage}
     */
    public static DocumentDetailsPage openDocumentDetailPage(WebDrone driver, String contentName)
    {
        // Check if doc details is already open
        HtmlPage page = getSharePage(driver);
        if (page instanceof DocumentDetailsPage)
        {
            if (((DocumentDetailsPage) page).getDocumentTitle().equalsIgnoreCase(contentName))
            {
                logger.info("Document Details page is already open ");
                return ((DocumentDetailsPage) page);
            }
        }

        DocumentLibraryPage docLibraryPage = getSharePage(driver).render();
        DocumentDetailsPage docDetailsPage = docLibraryPage.selectFile(contentName);
        return docDetailsPage.render();
    }

    /**
     * Method to open the Document details page of given content in given Site
     * @param driver
     * @param siteName
     * @param contentName
     * @return {@link DetailsPage}
     */
    // TODO: Remove: Redundant util. Also if necessary: this should be part of ShareUserSitePage, rather than ShareUser.
    public static DetailsPage openDetailPage(WebDrone driver, String siteName, String contentName)
    {
        openSitesDocumentLibrary(driver, siteName);
        return ShareUserSitePage.openDetailsPage(driver, contentName);
    }

    /**
     * This method will be used for cloud application only and it is to select
     * the invited user network.
     *
     * @param driver
     * @param invitedUser
     */
    public static void selectHomeNetwork(WebDrone driver, String invitedUser)
    {
        if (isAlfrescoVersionCloud(driver))
        {
            String strInvitedUser = null;

            if (invitedUser != null)
            {
                strInvitedUser = getUserDomain(invitedUser);
                selectTenant(driver, strInvitedUser);
            }
        }
    }

    /**
     * This method will be used for multi-tenant application and it is to select
     * the specified network.
     *
     * @param driver
     * @param tenantID
     */
    public static void selectTenant(WebDrone driver, String tenantID)
    {
        DashBoardPage dashBoardPage = openUserDashboard(driver);

        if (tenantID != null)
        {
            dashBoardPage = dashBoardPage.getNav().selectNetworkDropdown();
            dashBoardPage.getNav().selectNetwork(tenantID);
        }
    }

    /**
     * Function to create user on Enterprise using UI
     *
     * @param driver           WebDrone Instance
     * @param invitingUsername String username of inviting user
     * @param userName         String username
     * @param fName            String firstname
     * @param lName            String lastname
     * @param password         String password
     */
    public static Boolean createEnterpriseUser(WebDrone driver, String invitingUsername, String userName, String fName, String lName, String password)
    {
        return createEnterpriseUserWithGroup(driver, invitingUsername, userName, fName, lName, password, null);
    }

    /**
     * Helper to search for an Activity Entry on the Site Dashboard Page, with
     * configurable retry search option.
     *
     * @param driver       WebDrone instance
     * @param dashlet      String Name of the Dashlet such as
     *                     activities,content,myDocuments etc
     * @param entry        String Entry to look for within the Dashlet
     * @param entryPresent String Parameter to indicate should the entry be visible
     *                     within the dashlet
     * @param siteName     String Parameter to indicate the site name to open the site
     *                     dashboard.
     * @return Boolean
     */
    public static Boolean searchSiteDashBoardWithRetry(WebDrone driver, String dashlet, String entry, Boolean entryPresent, String siteName)
    {
        return searchSiteDashBoardWithRetry(driver, dashlet, entry, entryPresent, siteName, ActivityType.USER);
    }

    /**
     * Helper to search for an Activity Entry on the Site Dashboard Page, with
     * configurable retry search option.
     *
     * @param driver       WebDrone instance
     * @param dashlet      String Name of the Dashlet such as
     *                     activities,content,myDocuments etc
     * @param entry        String Entry to look for within the Dashlet
     * @param entryPresent String Parameter to indicate should the entry be visible
     *                     within the dashlet
     * @param siteName     String Parameter to indicate the site name to open the site
     *                     dashboard.
     * @param activityType Enum paramerter to indicate the activity type.
     * @return Boolean
     */
    public static Boolean searchSiteDashBoardWithRetry(WebDrone driver, String dashlet, String entry, Boolean entryPresent, String siteName, ActivityType activityType)
    {
        Boolean found = false;
        Boolean resultAsExpected = false;

        List<ShareLink> shareLinkEntries = null;

        // Assumes User is logged in

        // Waiting for Site Dashlets
        if (dashlet == null || dashlet.isEmpty())
        {
            dashlet = SITE_CONTENT_DASHLET;
        }

        // Open Site DashBoard
        openSiteDashboard(driver, siteName);

        // Code to repeat search until the element is found or Timeout is hit
        for (int searchCount = 1; searchCount <= retrySearchCount; searchCount++)
        {
            if (searchCount > 1)
            {
                // This below code is needed to wait for the solr indexing.
                webDriverWait(driver, refreshDuration);

                refreshSiteDashboard(driver);
            }

            if (dashlet.equals(SITE_ACTIVITIES) && ActivityType.DESCRIPTION.equals(activityType))
            {
                found = getSiteActivityDashletDescriptions(driver).contains(entry);
            }
            else
            {
                shareLinkEntries = getSiteDashletEntries(driver, dashlet, activityType);

                if (shareLinkEntries != null)
                {
                    found = findInList(shareLinkEntries, entry);
                }
            }

            // Loop again if result is not as expected: To cater for solr lag: eventual consistency
            resultAsExpected = (entryPresent.equals(found));
            if (resultAsExpected)
            {
                break;
            }
        }

        return resultAsExpected;
    }

    /**
     * Navigate to Site DashBoard page and waits for the page render to complete. 
     * Assumes User is logged in and is on any SitePage
     *
     * @param driver WebDrone Instance
     * @return {@link SiteDashboardPage}
     */
    public static SiteDashboardPage refreshSiteDashboard(WebDrone driver)
    {
        // Open DocumentLibrary Page from Site Page
        SitePage site = getSharePage(driver).render();

        logger.info("Opening Site Dashboard");
        return site.getSiteNav().selectSiteDashBoard().render();
    }

    /**
     * Helper to search for an Element in the list of <ShareLinks>.
     *
     * @param driver  WebDrone Instance
     * @param dashlet String Name of the dashlet
     * @return List<ShareLink>: List of Share Links available in the dashlet
     */
    protected static List<ShareLink> getSiteDashletEntries(WebDrone driver, String dashlet, ActivityType activityType)
    {
        List<ShareLink> entries = null;

        SiteDashboardPage siteDashBoard = (SiteDashboardPage) getSharePage(driver);
        if (dashlet == null)
        {
            dashlet = SITE_CONTENT_DASHLET;
        }

        if (dashlet.equals(SITE_CONTENT_DASHLET))
        {
            SiteContentDashlet siteContentDashlet = siteDashBoard.getDashlet(dashlet).render();
            entries = siteContentDashlet.getSiteContents();
        }
        else if (dashlet.equals(SITE_ACTIVITIES))
        {
            SiteActivitiesDashlet siteActivitiesDashlet = null;
            if (ActivityType.USER.equals(activityType))
            {
                siteActivitiesDashlet = siteDashBoard.getDashlet(dashlet).render();
                entries = siteActivitiesDashlet.getSiteActivities(LinkType.User);
            }
            else if (ActivityType.DOCUMENT.equals(activityType))
            {
                siteActivitiesDashlet = siteDashBoard.getDashlet(dashlet).render();
                entries = siteActivitiesDashlet.getSiteActivities(LinkType.Document);
            }
        }

        return entries;
    }

    /**
     * Helper to search for an Element in the list of <String>.
     *
     * @param driver WebDrone Instance
     * @return List<String>: List of String available in the dashlet
     */
    protected static List<String> getSiteActivityDashletDescriptions(WebDrone driver)
    {
        SiteDashboardPage siteDashBoard = (SiteDashboardPage) getSharePage(driver);
        SiteActivitiesDashlet siteActivitiesDashlet = siteDashBoard.getDashlet(SITE_ACTIVITIES).render();
        return siteActivitiesDashlet.getSiteActivityDescriptions();
    }

    /**
     * This method is used to select the task from the user dash board by given
     * sitename.
     *
     * @param driver
     * @param taskName
     * @return {@link MyTasksDashlet}
     */
    public static MyTasksDashlet selectTaskByTaskName(WebDrone driver, String taskName)
    {
        MyTasksDashlet sites = ShareUser.openUserDashboard(driver).getDashlet(USER_TASKS).render();

        return sites.clickOnTask(taskName);
    }

    /**
     * This method is used to create content with name, title and description.
     * User should be logged in and present on site page.
     *
     * @param drone
     * @param contentDetails
     * @param contentType
     * @return {@link DocumentLibraryPage}
     * @throws Exception
     */
    public static DocumentLibraryPage createContent(WebDrone drone, ContentDetails contentDetails, ContentType contentType) throws Exception
    {
        // Open Document Library
        DocumentLibraryPage documentLibPage = ShareUser.openDocumentLibrary(drone);
        DocumentDetailsPage detailsPage = null;

        try
        {
            if (isAlfrescoVersionCloud(drone))
            {
                documentLibPage = createContentWithSpecificProps(drone, contentDetails);
            }
            else
            {
                CreatePlainTextContentPage contentPage = documentLibPage.getNavigation().selectCreateContent(contentType).render();
                detailsPage = contentPage.create(contentDetails).render();
                documentLibPage = (DocumentLibraryPage) detailsPage.getSiteNav().selectSiteDocumentLibrary();
                documentLibPage.render();
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            throw new SkipException("Error in creating content." + e);
        }

        return documentLibPage;
    }

    public static DocumentLibraryPage createContentInCurrentFolder(WebDrone drone, ContentDetails contentDetails, ContentType contentType, DocumentLibraryPage documentLibPage) throws Exception
    {
        DocumentDetailsPage detailsPage;

        try
        {
            if (isAlfrescoVersionCloud(drone))
            {
                documentLibPage = createContentWithSpecificProps(drone, contentDetails);
            }
            else
            {
                CreatePlainTextContentPage contentPage = documentLibPage.getNavigation().selectCreateContent(contentType).render();
                detailsPage = contentPage.create(contentDetails).render();
                documentLibPage = (DocumentLibraryPage) detailsPage.getSiteNav().selectSiteDocumentLibrary();
                documentLibPage.render();
            }
        }
        catch (Exception e)
        {
            throw new SkipException("Error in creating content." + e);
        }

        return documentLibPage;
    }

    /**
     * This method used to traverse through the folders and files inside the
     * parent folder. And returns the list of file or folder names as child
     * elements of parent folder.
     *
     * @param driver
     * @param fileOrFolderPath : This is the parent folder path in document library (Don't
     *                         include doclib in path)
     * @return List<String> : List of files or folder names present inside the
     * given parent folder.
     */
    public static List<String> getContentsOfDownloadedArchieve(WebDrone driver, String fileOrFolderPath)
    {
        List<String> fileOrFolderNames = new ArrayList<String>();
        try
        {
            if (fileOrFolderPath == null || fileOrFolderPath.isEmpty())
            {
                throw new UnsupportedOperationException("Incorrect FolderPath");
            }

            File fileOrFolder = new File(fileOrFolderPath);

            if (fileOrFolder.exists())
            {
                for (File file : fileOrFolder.listFiles())
                {
                    fileOrFolderNames.add(file.getName());

                    if (file.isDirectory())
                    {
                        fileOrFolderNames.addAll(getContentsOfDownloadedArchieve(driver, file.getAbsolutePath()));
                    }
                }
            }
            else
            {
                throw new UnsupportedOperationException("Not able to find the folderpath." + fileOrFolder.getName());
            }
        }
        catch (Exception e)
        {
            throw new SkipException("Error in accessing folder or file: " + e);
        }
        return fileOrFolderNames;
    }

    /**
     * This method does the extracting of archieve file in download folder and
     * returns true if it is successful otherwise false.
     *
     * @param driver
     * @param folderName , This is string value contains the absolute path for archieve
     *                   folder/file.
     * @return boolean
     */
    public static boolean extractDownloadedArchieve(WebDrone driver, String folderName)
    {
        UnzipDownload unzipDownload = new UnzipDownload();
        return unzipDownload.unzip(downloadDirectory + folderName, downloadDirectory);
    }

    /**
     * Method to search for a site, select the site from search results and
     * opens Document Library Page
     *
     * @param drone
     * @param siteName
     * @return {@link DocumentLibraryPage}
     */

    public static DocumentLibraryPage openSiteDocumentLibraryFromSearch(WebDrone drone, String siteName)
    {
        // Checking for site doc lib to be open.
        HtmlPage page = drone.getCurrentPage();
        if (page instanceof DocumentLibraryPage)
        {
            if (((DocumentLibraryPage) page).isSite(siteName) && ((DocumentLibraryPage) page).isDocumentLibrary())
            {
                logger.info("Site doc lib page open ");
                return ((DocumentLibraryPage) page);
            }
        }

        SiteUtil.openSiteFromSearch(drone, siteName);
        return openDocumentLibrary(drone);
    }

    /**
     * Customize site by adding different modules.
     * Assumes user is logged in and a Site dashboard is open
     *
     * @param drone
     * @param siteName
     * @return CustomizeSitePage
     * @author nshah
     */
    public static CustomizeSitePage customizeSite(WebDrone drone, String siteName)
    {
        SiteDashboardPage siteDashBoard = getSharePage(drone).render();
        
        return siteDashBoard.getSiteNav().selectCustomizeSite().render();
    }

    /**
     * This method is to create the content and updates with the specific
     * content details.
     *
     * @param drone
     * @param contentDetails
     * @return {@link DocumentLibraryPage}
     * @throws Exception
     */
    public static DocumentLibraryPage createContentWithSpecificProps(WebDrone drone, ContentDetails contentDetails) throws Exception
    {
        DocumentLibraryPage documentLibPage = null;
        EditDocumentPropertiesPage editDocPropertiesPage = null;
        String fileName = contentDetails.getName();
        String title = contentDetails.getTitle();
        String desc = contentDetails.getDescription();
        String content = contentDetails.getContent();
        String[] fileInfo = new String[3];

        fileInfo[0] = fileName;
        fileInfo[1] = DOCLIB;

        if (content != null && content.trim().length() > 0)
        {
            fileInfo[2] = content;
        }
        else
        {
            fileInfo[2] = "This is sample content.";
        }

        documentLibPage = uploadFileInFolder(drone, fileInfo).render(maxWaitTime);

        if ((title != null && title.length() > 0) || (desc != null && desc.length() > 0))
        {
            editDocPropertiesPage = documentLibPage.getFileDirectoryInfo(fileName).selectEditProperties().render();

            if (title != null && title.length() > 0)
            {
                editDocPropertiesPage.setDocumentTitle(title);
            }

            if (desc != null && desc.length() > 0)
            {
                editDocPropertiesPage.setDescription(desc);
            }

            documentLibPage = editDocPropertiesPage.selectSave().render();
        }
        return documentLibPage;
    }

    /**
     * This method is used to get sync status (with retry) for a content from
     * document library page and returns true if the content synced otherwise
     * false. Since cloud sync is not instantaneous, the method keeps retrying
     * until maxWaitTime_CloudSync is reached This method could be invoked after
     * syncToCloud is initiated from document library page.
     *
     * @param driver
     * @param fileName
     * @return boolean
     */
    // TODO: Ranjith: Use this method from AbstractCloudSyncTest, remove this one as redundant.
    public static boolean checkIfContentIsSynced(WebDrone driver, String fileName)
    {
        DocumentLibraryPage docLibPage = (DocumentLibraryPage) getSharePage(driver);
        docLibPage.render();

        String status = "";
        SyncInfoPage syncInfoPage;

        try
        {
            RenderTime t = new RenderTime(maxWaitTimeCloudSync);
            while (true)
            {
                t.start();
                try
                {
                    logger.info("NodeRef for File being checked: " + ShareUserSitePage.getFileDirectoryInfo(driver, fileName).getNodeRef());
                    syncInfoPage = docLibPage.getFileDirectoryInfo(fileName).clickOnViewCloudSyncInfo().render();
                    status = syncInfoPage.getCloudSyncStatus();
                    syncInfoPage.clickOnCloseButton();

                    // if
                    // (status.contains(driver.getLanguageValue("sync.status.pending.text")))
                    if (status.contains("Pending"))

                    {

                        webDriverWait(driver, 1000);
                        // Expected to work on repoPage too
                        docLibPage = refreshSharePage(driver).render();

                    }
                    else
                    {
                        // return
                        // status.contains(driver.getLanguageValue("sync.status.synced.text"));
                        return status.contains("Synced");
                    }
                }
                finally
                {
                    t.end();
                }
            }
        }
        catch (PageException e)
        {
        }
        catch (PageRenderTimeException exception)
        {
        }

        return false;
    }

    /**
     * This method is used to add the aspects on Document/Folder Details Page.
     * User should be on Folder/Document Details page.
     *
     * @param drone
     * @param aspects
     * @return DetailsPage
     */
    public static DetailsPage addAspects(WebDrone drone, List<DocumentAspect> aspects)
    {
        if (isAlfrescoVersionCloud(drone))
        {
            throw new UnsupportedOperationException("Add Aspects is not supported for Cloud");
        }
        DetailsPage detailsPage = (DetailsPage) ShareUser.getSharePage(drone);
        List<DocumentAspect> aspectsList = new ArrayList<DocumentAspect>();
        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();
        aspectsList.addAll(aspects);
        aspectsPage.add(aspectsList);
        detailsPage = aspectsPage.clickApplyChanges().render();
        return detailsPage;
    }

    /**
     * This method is used to add the aspects on DocumentLibrary Page.
     * User should be on Folder/Document Details page.
     *
     * @param drone
     * @param aspects
     * @param contentName
     * @return
     */
    public static DocumentLibraryPage addAspects(WebDrone drone, List<DocumentAspect> aspects, String contentName)
    {
        if (isAlfrescoVersionCloud(drone))
        {
            throw new UnsupportedOperationException("Add Aspects is not supported for Cloud");
        }

        DocumentLibraryPage documentLibraryPage = (DocumentLibraryPage) ShareUser.getSharePage(drone);
        documentLibraryPage.selectFile(contentName).render();
        DetailsPage detailsPage = addAspects(drone, aspects);
        return detailsPage.getSiteNav().selectSiteDocumentLibrary().render();
    }

    /**
     * This method is used to remove the aspects on Document/Folder Details Page.
     * User should be on Folder/Document Details page.
     * 
     * @param drone
     * @param aspects
     * @return DetailsPage
     */
    public static DetailsPage removeAspects(WebDrone drone, List<DocumentAspect> aspects)
    {
        if (isAlfrescoVersionCloud(drone))
        {
            throw new UnsupportedOperationException("Add Aspects is not supported for Cloud");
        }
        DetailsPage detailsPage = (DetailsPage) ShareUser.getSharePage(drone);
        List<DocumentAspect> aspectsList = new ArrayList<DocumentAspect>();
        SelectAspectsPage aspectsPage = detailsPage.selectManageAspects().render();
        aspectsList.addAll(aspects);
        aspectsPage.remove(aspectsList);
        detailsPage = aspectsPage.clickApplyChanges().render();
        return detailsPage;
    }

    /**
     * Create user with Group Name.
     *
     * @param driver
     * @param invitingUsername
     * @param userName
     * @param fname
     * @param lname
     * @param password
     * @param groupName
     * @return
     */
    public static Boolean createEnterpriseUserWithGroup(WebDrone driver, String invitingUsername, String userName, String fname, String lname, String password, String groupName)
    {
        try
        {
            String[] authDetails = getAuthDetails(invitingUsername);
            ShareUser.login(driver, authDetails[0], authDetails[1]);

            DashBoardPage dash = driver.getCurrentPage().render();
            
            UserSearchPage page = dash.getNav().getUsersPage().render();
            NewUserPage newPage = page.selectNewUser().render();
            
            String email = userName.contains("@") ? userName.replaceAll(" ", "") : userName.replaceAll(" ", "") + "@" + DOMAIN_FREE;

            if (groupName == null)
            {
                page = newPage.createEnterpriseUser(userName, fname, lname, email, password).render();
            }
            else
            {
                page = newPage.createEnterpriseUserWithGroup(userName, fname, lname, email, password, groupName).render();
            }

            logger.info("Created User: " + userName);
            ShareUser.logout(driver);
            
            return (page != null);
        }
        catch (Exception e)
        {
            logger.error("Error Creating User: " + userName + " Exception: " + e.getMessage());
            return false;
        }
    }

    /**
     * This method deletes the google account cookies.
     *
     * @param drone
     * @param url
     */
    public static void deleteSiteCookies(WebDrone drone, String url)
    {
        // Storing the current url
        String currentUrl = drone.getCurrentUrl();

        // Navigating to google account and Deleting cookies
        drone.navigateTo(url);
        drone.deleteCookies();

        // Navigating back to current url
        drone.navigateTo(currentUrl);
    }

    /**
     * This method uploads the new version for the document with the given file
     * from data folder. User should be on Document details page.
     *
     * @param fileName
     * @param drone
     * @return DocumentDetailsPage
     * @throws IOException
     */
    public static DocumentDetailsPage uploadNewVersionOfDocument(WebDrone drone, String fileName, String comments) throws IOException
    {
        String fileContents = "New File being created via newFile:" + fileName;
        File newFileName = newFile(DATA_FOLDER + (fileName), fileContents);

        DocumentDetailsPage detailsPage = (DocumentDetailsPage) getSharePage(drone);
        UpdateFilePage updatePage = detailsPage.selectUploadNewVersion().render();
        updatePage.selectMajorVersionChange();
        updatePage.uploadFile(newFileName.getCanonicalPath());
        updatePage.setComment(comments);
        detailsPage = updatePage.submit().render();
        return detailsPage;
    }
    /**
     * This method uploads the new version for the document with the given file
     * from data folder. User should be on Document details page.
     *
     * @param drone Webdrone instance
     * @param fileName String of file to be versioned
     * @param comments
     * @param major Indicates the type of version changes: if true - major version; if false - minor
     * @return DocumentDetailsPage
     * @throws IOException
     */

    public static DocumentDetailsPage uploadNewVersionOfDocument(WebDrone drone, String fileName, String comments, boolean major) throws IOException
    {
        String fileContents = "New File being created via newFile:" + fileName;
        File newFileName = newFile(DATA_FOLDER + (fileName), fileContents);

        DocumentDetailsPage detailsPage = (DocumentDetailsPage) getSharePage(drone);
        UpdateFilePage updatePage = detailsPage.selectUploadNewVersion().render();
        if (major)
        {
            updatePage.selectMajorVersionChange();
        }
        else
        {
            updatePage.selectMinorVersionChange();
        }
        updatePage.uploadFile(newFileName.getCanonicalPath());
        updatePage.setComment(comments);
        detailsPage = updatePage.submit().render();
        return detailsPage;
    }
    /**
     * Checks the checkbox for a content if not selected on the document library
     * page.
     *
     * @param drone
     * @param contentName
     * @return DocumentLibraryPage
     * @IMP Note: Expects the user is logged in and document library page within
     * the selected site is open.
     */
    public static DocumentLibraryPage selectContentCheckBox(WebDrone drone, String contentName)
    {
        DocumentLibraryPage docLibPage = ((DocumentLibraryPage) getSharePage(drone)).render();
        if (!docLibPage.getFileDirectoryInfo(contentName).isCheckboxSelected())
        {
            docLibPage.getFileDirectoryInfo(contentName).selectCheckbox();
        }
        return docLibPage.render();
    }

    /**
     * UnChecks the checkbox for a content if already selected on the document
     * library page.
     *
     * @param drone
     * @param contentName
     * @return DocumentLibraryPage
     * @IMP Note: Expects the user is logged in and document library page within
     * the selected site is open
     */
    public static DocumentLibraryPage unSelectContentCheckBox(WebDrone drone, String contentName)
    {
        DocumentLibraryPage docLibPage = ((DocumentLibraryPage) getSharePage(drone)).render();
        if (docLibPage.getFileDirectoryInfo(contentName).isCheckboxSelected())
        {
            docLibPage.getFileDirectoryInfo(contentName).selectCheckbox();
        }
        return docLibPage.render();
    }

    /**
     * Mimics the process of deleting selected content on a document library
     * page.
     *
     * @param drone
     * @return
     * @IMP Note: Expects the user is logged in and document library page within
     * the selected site is open
     */
    public static DocumentLibraryPage deleteSelectedContent(WebDrone drone)
    {
        DocumentLibraryPage docLibPage = ((DocumentLibraryPage) getSharePage(drone)).render();
        DocumentLibraryNavigation docLibNavOption = docLibPage.getNavigation().render();
        ConfirmDeletePage deletePage = docLibNavOption.selectDelete().render();
        docLibPage = deletePage.selectAction(Action.Delete).render();
        return docLibPage.render();
    }

    /**
     * Create copy of all contents present in Document Library.
     *
     * @param drone
     */
    public static DocumentLibraryPage createCopyOfAllContent(WebDrone drone)
    {
        DocumentLibraryPage docLibPage = ((DocumentLibraryPage) getSharePage(drone)).render();
        // Select All
        docLibPage = docLibPage.getNavigation().selectAll().render();

        // Select Copy To
        CopyOrMoveContentPage copyContent = docLibPage.getNavigation().render().selectCopyTo().render();

        // Keep the selected Destination: Current Site > DocumentLibrary Folder
        docLibPage = copyContent.selectOkButton().render();

        return docLibPage;
    }

    /**
     * Delete all content from document library.
     *
     * @param drone
     * @return DocumentLibraryPage
     */
    public static DocumentLibraryPage deleteAllContentFromDocumentLibrary(WebDrone drone)
    {
        DocumentLibraryPage docLibPage = (DocumentLibraryPage) getSharePage(drone);
        do
        {
            docLibPage.getNavigation().selectAll().render();
            docLibPage = deleteSelectedContent(drone);
        }
        while (docLibPage.hasFiles());
        return docLibPage;
    }

    /**
     * Get the value of the guid noderef for the specified content from document
     * library.
     *
     * @param drone
     */
    public static String getGuid(WebDrone drone, String contentName)
    {
        DocumentLibraryPage docLibPage = (DocumentLibraryPage) getSharePage(drone);
        String nodeRef = docLibPage.getFileDirectoryInfo(contentName).getNodeRef();
        return StringUtils.substringAfterLast(nodeRef, "/");
    }

    /**
     * This method is used to convert the given date into string format based on
     * given locale format.
     *
     * @param date
     * @param format
     * @return String
     */
    public static String convertDateToString(Date date, String format)
    {
        if (date == null || format == null)
        {
            throw new IllegalArgumentException("Date and Format values should not be null.");
        }

        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            String strDate = dateFormat.format(date);
            logger.info("Date : " + date + " converted into string value :" + strDate);

            return strDate;
        }
        catch (Exception e)
        {
            throw new PageException("converting Date to String is having problem : " + e.getMessage());
        }
    }

    /**
     * This method is used to get if the task is present or not. If the task is
     * not displayed, retry for defined time(maxWaitTime_CloudSync)
     *
     * @param driver
     * @param taskName
     * @return boolean true if task is present
     */
    // TODO: Ranjith: Refactor > Move method to AbstractWorkflow
    public static boolean checkIfTaskIsPresent(WebDrone driver, String taskName)
    {
        return AbstractWorkflow.checkIfTaskIsPresent(driver, taskName, true);
    }

    /**
     * Delete doc lib contents.
     *
     * @param drone
     * @return
     */
    public static DocumentLibraryPage deleteDocLibContents(WebDrone drone)
    {
        ConfirmDeletePage deletePage = ((DocumentLibraryPage) getSharePage(drone)).getNavigation().render().selectDelete();
        return deletePage.selectAction(Action.Delete).render();
    }

    /**
     * Fucntion to creater new group.
     *
     * @param driver
     * @param groupName
     * @return
     */
    public static Boolean createEnterpriseGroup(WebDrone driver, String groupName)
    {
        // UserSearchPage userPage = null;
        boolean isGroupCreated = false;
        try
        {
            ShareUser.login(driver, ADMIN_USERNAME, ADMIN_PASSWORD);
            DashBoardPage dash = (DashBoardPage) driver.getCurrentPage().render();
            GroupsPage page = dash.getNav().getGroupsPage();
            NewGroupPage newGrp = page.clickBrowse().render().navigateToNewGroupPage().render();
            page = newGrp.createGroup(groupName, groupName, ActionButton.CREATE_GROUP).render();
            if (page.getGroupList().contains(groupName))
            {
                isGroupCreated = true;
            }
            logger.info("Created Group: " + groupName);
            ShareUser.logout(driver);
            return isGroupCreated;

        }
        catch (Exception e)
        {
            logger.error("Error Creating Group: " + groupName + " Exception: " + e.getMessage());
            return false;
        }
    }

    /**
     * Util method to change the language from My Profile page (Only applicable
     * to Cloud2 env)
     *
     * @param drone
     * @param language
     */
    public static LanguageSettingsPage changeLanguage(WebDrone drone, Language language)
    {
        if (!isAlfrescoVersionCloud(drone))
        {
            throw new UnsupportedOperationException("Can only change language in Cloud Environment");
        }

        SharePage sharePage = drone.getCurrentPage().render();
        MyProfilePage myProfilePage = sharePage.getNav().selectMyProfile().render();
        LanguageSettingsPage languageSettingsPage = myProfilePage.getProfileNav().selectLanguage().render();
        languageSettingsPage = languageSettingsPage.changeLanguage(language).render();
        return languageSettingsPage;
    }

    /**
     * @param drone
     * @param contentName
     * @return
     */
    public static ManagePermissionsPage returnManagePermissionPage(WebDrone drone, String contentName)
    {
        SharePage sharePage = getSharePage(drone);
        if (sharePage instanceof ManagePermissionsPage)
        {
            return (ManagePermissionsPage) sharePage;
        }
        else if (sharePage instanceof DocumentDetailsPage)
        {
            return ((DocumentDetailsPage) sharePage).selectManagePermissions().render();
        }
        else if (sharePage instanceof FolderDetailsPage)
        {
            return ((FolderDetailsPage) sharePage).selectManagePermissions().render();
        }
        else if (sharePage instanceof RepositoryPage)
        {
            return ((RepositoryPage) sharePage).getFileDirectoryInfo(contentName).selectManagePermission().render();
        }
        else if (sharePage instanceof DocumentLibraryPage)
        {
            return ((DocumentLibraryPage) sharePage).getFileDirectoryInfo(contentName).selectManagePermission().render();
        }
        else
        {
            throw new UnsupportedOperationException("Invalid page to request manage permission!");
        }
    }

    public static DocumentDetailsPage editTextDocument(WebDrone drone, String name, String description, String content)
    {
        DocumentDetailsPage detailsPage = drone.getCurrentPage().render();

        EditTextDocumentPage inlineEditPage = detailsPage.selectInlineEdit().render();
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(name);
        contentDetails.setDescription(description);
        contentDetails.setContent(content);
        detailsPage = inlineEditPage.save(contentDetails).render();
        return detailsPage;
    }

    public static DocumentLibraryPage editProperties(WebDrone drone, String contentName, String description)
    {
        DocumentLibraryPage documentLibraryPage = drone.getCurrentPage().render();

        EditDocumentPropertiesPage editProp = documentLibraryPage.getFileDirectoryInfo(contentName).selectEditProperties().render();
        editProp.setDescription(description);
        return editProp.selectSave().render();
    }

    public static DocumentLibraryPage editProperties(WebDrone drone, String contentName, String newContentName, String title, String description)
    {
        DocumentLibraryPage documentLibraryPage = drone.getCurrentPage().render();

        EditDocumentPropertiesPage editProp = documentLibraryPage.getFileDirectoryInfo(contentName).selectEditProperties().render();

        if (newContentName != null)
        {
            editProp.setName(newContentName);
        }
        if (title != null)
        {
            editProp.setDocumentTitle(title);
        }
        if (description != null)
        {
            editProp.setDescription(description);
        }

        return editProp.selectSave().render();
    }

    /**
     * Function mime the action of opening the folder detail Page.
     *
     * @param driver      WebDrone Instance
     * @param contentName String Criteria for site search
     * @return {@link FolderDetailsPage}
     */
    public static FolderDetailsPage openFolderDetailPage(WebDrone driver, String contentName)
    {
        DocumentLibraryPage docLibraryPage = (DocumentLibraryPage) getSharePage(driver);
        FolderDetailsPage folderDetailsPage = docLibraryPage.getFileDirectoryInfo(contentName).selectViewFolderDetails();
        return folderDetailsPage.render();
    }

    /**
     * This method used to open the document library page in gallery view.
     * User should be present on document library page.
     *
     * @param customDrone
     * @param siteName
     * @return DocumentLibraryPage
     */
    public static DocumentLibraryPage openDocumentLibraryInGalleryView(WebDrone customDrone, String siteName)
    {
        DocumentLibraryPage docLibPage = null;
        SharePage sharePage = ShareUser.getSharePage(customDrone);

        if (!(sharePage instanceof DocumentLibraryPage))
        {
            docLibPage = ShareUser.openSitesDocumentLibrary(customDrone, siteName);
        }
        else
        {
            docLibPage = (DocumentLibraryPage) sharePage;
        }
        DocumentLibraryNavigation docLibNavigation = docLibPage.getNavigation().render();
        return docLibNavigation.selectGalleryView().render();
    }

    /**
     * Method to get Name of the ShareLink
     *
     * @param shareLink
     * @return
     */
    public static String getShareLinkName(ShareLink shareLink)
    {
        if (shareLink == null)
        {
            throw new UnsupportedOperationException("ShareLink cannot be null");
        }
        return shareLink.getDescription();
    }

    /**
     * Method to add given comment to given file (Assume User is in Document Library Page)
     *
     * @param drone
     * @param fileName
     * @param comment
     * @return {@link DocumentLibraryPage}
     */
    public static DocumentLibraryPage addComment(WebDrone drone, String fileName, String comment)
    {
        DocumentLibraryPage documentLibraryPage = getSharePage(drone).render();
        DocumentDetailsPage detailsPage = documentLibraryPage.selectFile(fileName).render();
        detailsPage = detailsPage.addComment(comment).render();
        return detailsPage.getSiteNav().selectSiteDocumentLibrary().render();
    }

    /**
     * Method to set author to given file (Assume User is in Document Library Page)
     *
     * @param drone
     * @param fileName
     * @param author
     * @return {@link DocumentLibraryPage}
     */
    public static DocumentLibraryPage setAuthor(WebDrone drone, String fileName, String author)
    {
        DocumentLibraryPage documentLibraryPage = getSharePage(drone).render();
        DocumentDetailsPage detailsPage = documentLibraryPage.selectFile(fileName).render();
        EditDocumentPropertiesPage propertiesPage = detailsPage.selectEditProperties().render();
        propertiesPage.setAuthor(author);
        detailsPage = propertiesPage.selectSave().render();
        return detailsPage.getSiteNav().selectSiteDocumentLibrary().render();
    }

    /**
     * Method to delete chosen user if you login as admin.
     *
     * @param drone
     * @param testUser
     * @return
     */
    public static UserSearchPage deleteUser(WebDrone drone, String testUser)
    {
        DashBoardPage dashBoard = drone.getCurrentPage().render();
        UserSearchPage page = dashBoard.getNav().getUsersPage().render();
        page = page.searchFor(testUser).render();
        UserProfilePage userProfile = page.clickOnUser(testUser).render();
        return userProfile.deleteUser().render();
    }

    /**
     * @param drone
     * @return
     */
    public static DashBoardPage selectMyDashBoard(WebDrone drone)
    {
        return ((SharePage) drone.getCurrentPage().render()).getNav().selectMyDashBoard();
    }

    /**
     * @param drone
     * @param siteName
     * @param opertion
     * @return
     */
    public static HtmlPage navigateToSiteThroughDashlet(WebDrone drone, String siteName, SiteOperation opertion)
    {
        MySitesDashlet dashlet = selectMyDashBoard(drone).getDashlet("my-sites").render();

        if (SiteOperation.Delete.equals(opertion))
        {
            return dashlet.deleteSite(siteName);
            // deletePage = deletePage.selectAction(Action.Delete).render();
            // return deletePage.selectAction(Action.Delete);
        }
        else if (SiteOperation.Open.equals(opertion))
        {
            ShareLink link = dashlet.selectSite(siteName);
            return link.click();
        }
        else
        {
            throw new UnsupportedOperationException("Operation not exist.");
        }

    }

    /**
     * Util to return the specified dashlet on the Dashboard page
     * @param drone
     * @param dashLet dashLet Name
     * @return Dashlet
     */
    public static Dashlet getDashlet(WebDrone drone, String dashLet)
    {
        DashBoardPage dashboard = (DashBoardPage) getSharePage(drone);
        MySitesDashlet dashlet = dashboard.getDashlet(dashLet).render();
        dashboard = dashlet.selectMyFavourites(FavouriteType.Recent).render();
        return dashboard.getDashlet(dashLet).render();
    }

    /**
     * Navigate menu items using Keyboard.
     *
     * @param drone
     * @param keys
     * @return
     */
    public static HtmlPage navigateMenuUsingKeys(WebDrone drone, Keys... keys)
    {

        if (ArrayUtils.isEmpty(keys))
        {
            throw new UnsupportedOperationException("Please provide appropriate input keys");
        }
        SharePage dashBoardPage = getSharePage(drone);
        for (int i = 0; i < keys.length; i++)
        {
            dashBoardPage.inputFromKeyborad(keys[i]);
        }
        drone.waitForPageLoad(maxWaitTime);

        return drone.getCurrentPage();
    }

    /**
     * This method is used to check if the Content item is present or not. If the content
     * is not present, retry for defined time(maxWaitTime_CloudSync)
     *
     * @param drone
     * @param contentName
     * @param isContentExpected
     * @return boolean
     */
    public static boolean checkIfContentIsPresent(WebDrone drone, String contentName, boolean isContentExpected)
    {
        DocumentLibraryPage documentLibraryPage = getSharePage(drone).render();

        RenderTime t = new RenderTime(maxWaitTimeCloudSync);
        try
        {
            while (true)
            {
                t.start();
                try
                {
                    if (isContentExpected)
                    {
                        if (documentLibraryPage.isItemVisble(contentName))
                        {
                            return true;
                        }
                    }
                    else
                    {
                        if (!documentLibraryPage.isItemVisble(contentName))
                        {
                            return true;
                        }
                    }

                    webDriverWait(drone, 1000);
                    documentLibraryPage = refreshSharePage(drone).render();
                }
                finally
                {
                    t.end();
                }
            }
        }
        catch (PageRenderTimeException p)
        {
        }

        return false;
    }

    /**
     * Method to get selected Aspects for a given File in given Site
     * @param drone
     * @param siteName
     * @param contentName
     * @return {@link Set<org.alfresco.po.share.site.document.DocumentAspect>}
     */
    public static Set<DocumentAspect> getSelectedAspects(WebDrone drone, String siteName, String contentName)
    {
        ShareUser.openSitesDocumentLibrary(drone, siteName);

        DetailsPage detailsPage = ShareUserSitePage.openDetailsPage(drone, contentName);
        SelectAspectsPage selectAspectsPage = detailsPage.selectManageAspects().render();
        Set<DocumentAspect> selectedAspects = selectAspectsPage.getSelectedAspects();
        selectAspectsPage.clickCancel().render();
        return selectedAspects;
    }

    /**
     * Copy or Move to File or folder from document library.
     * @param drone
     * @param destination
     * @param siteName
     * @param fileName
     * @return
     */
    public static HtmlPage copyOrMoveArtifact(WebDrone drone, String destination, String siteName, String fileName, PerformOperation operation, TypeOfPage type)
    {
        DocumentLibraryPage docPage = (DocumentLibraryPage) getSharePage(drone);
        CopyOrMoveContentPage copyOrMoveToPage ;
        
        if(TypeOfPage.CopyTo.equals(type))
        {
            copyOrMoveToPage = docPage.getFileDirectoryInfo(fileName).selectCopyTo().render();
        }
        else
        {
            copyOrMoveToPage = docPage.getFileDirectoryInfo(fileName).selectMoveTo().render();
        }
        
        copyOrMoveToPage.selectDestination(destination);
        copyOrMoveToPage.selectSite(siteName).render();
        if (PerformOperation.OK.equals(operation))
        {
            copyOrMoveToPage.selectOkButton().render();
        }
        else if (PerformOperation.CANCEL.equals(operation))
        {
            copyOrMoveToPage.selectCancelButton().render();
        }
        return getSharePage(drone);
    }

    public static List<String> getSiteActivityDashletDescription(WebDrone driver, String dashlet)
    {
        List<String> entries = null;

        SiteDashboardPage siteDashBoard = (SiteDashboardPage) getSharePage(driver).render();
        if (dashlet == null)
        {
            dashlet = DASHLET_ACTIVITIES;
        }

        if (dashlet.equals(SITE_ACTIVITIES))
        {
            SiteActivitiesDashlet siteActivitiesDashlet = siteDashBoard.getDashlet(SITE_ACTIVITIES).render();
            entries = siteActivitiesDashlet.getSiteActivityDescriptions();

        }
        else
        {
            throw new SkipException("Incorrect Dashlet");
        }

        return entries;
    }

    /**
     * Helper to search for an Activity Descriptions on the Site Dashboard Page, with
     * configurable retry search option.
     *
     * @param driver WebDrone instance
     * @param dashlet String Name of the Dashlet such as
     *            activities,content,myDocuments etc
     * @param entry String Entry to look for within the Dashlet
     * @param entryPresent String Parameter to indicate should the entry be visible
     *            within the dashlet
     * @param siteName String Parameter to indicate the site name to open the site
     *            dashboard.
     * @return Boolean
     */
    public static Boolean searchSiteDashBoardDescriptionsWithRetry(WebDrone driver, String dashlet, String entry, Boolean entryPresent, String siteName)
    {
        Boolean found = false;
        Boolean resultAsExpected = false;

        List<ShareLink> shareLinkEntries = null;

        // Assumes User is logged in

        // Waiting for Site Dashlets
        if (dashlet == null || dashlet.isEmpty())
        {
            dashlet = SITE_CONTENT_DASHLET;
        }

        // Open Site DashBoard
        openSiteDashboard(driver, siteName);

        // Code to repeat search until the element is found or Timeout is hit
        for (int searchCount = 1; searchCount <= retrySearchCount; searchCount++)
        {
            if (searchCount > 1)
            {
                // This below code is needed to wait for the solr indexing.
                webDriverWait(driver, refreshDuration);

                refreshSiteDashboard(driver);
            }

            found = getSiteActivityDashletDescription(driver, SITE_ACTIVITIES).contains(entry);

            // Loop again if result is not as expected: To cater for solr lag: eventual consistency
            resultAsExpected = (entryPresent.equals(found));
            if (resultAsExpected)
            {
                break;
            }
        }

        return resultAsExpected;
    }
}
