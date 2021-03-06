/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.po.share.dashlet;

import static org.alfresco.webdrone.RenderElement.getVisibleRenderElement;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.webdrone.RenderTime;
import org.alfresco.webdrone.WebDrone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * SiteContentBreakdownDashlet page object for site content breakdown report dashlet
 * 
 * @author jcule
 */
public class SiteContentBreakdownDashlet extends AbstractDashlet implements Dashlet
{

    private static Log logger = LogFactory.getLog(SiteContentBreakdownDashlet.class);

    private static final String SITE_CONTENT_REPORT_DASHLET = "div[id*='SiteContentReport']";
    private static final String PIE_CHART_SLICES = "path[transform]";
    private static final String TOOLTIP_DATA = "div[id^='tipsyPvBehavior']";
    private static final String ORIGINAL_TITLE_ATTRIBUTE = "original-title";
    
    /**
     * Constructor
     * 
     * @param drone
     */
    protected SiteContentBreakdownDashlet(WebDrone drone)
    {
        super(drone, By.cssSelector(SITE_CONTENT_REPORT_DASHLET));
    }

    @SuppressWarnings("unchecked")
    public SiteContentBreakdownDashlet render()
    {
        return render(new RenderTime(maxPageLoadingTime));
    }

    @SuppressWarnings("unchecked")
    @Override
    public SiteContentBreakdownDashlet render(final long time)
    {
        return render(new RenderTime(time));
    }

    @SuppressWarnings("unchecked")
    public SiteContentBreakdownDashlet render(RenderTime timer)
    {
        elementRender(timer, getVisibleRenderElement(By.cssSelector(SITE_CONTENT_REPORT_DASHLET)));
        return this;
    }
    
    
    /**
     * Gets the list of content data
     * 
     * @return
     */
    /**
    public List<String> getSiteContentReportData()
    {
        List<String> siteContentData = new ArrayList<String>();
        try
        {

            List<WebElement> siteContentDataElements = drone.findAll(By.cssSelector(SITE_CONTENT_DATA));
            if (siteContentDataElements.size() > 0)
            {
                for (WebElement contentDataElement : siteContentDataElements)
                {
                    siteContentData.add(contentDataElement.getText());
                }
            }

        }
        catch (NoSuchElementException nse)
        {
            logger.error("No site content data " + nse);
        }
        return siteContentData;
    }
    **/
    
    /**
     * Gets the list of strings: mime type - count
     * @return
     */
    /**
    public List<String> getTypesAndCounts()
    {
        List<String> siteContentData = getSiteContentReportData();
        StringBuilder builder = new StringBuilder(" ");
        List<String> typesAndCounts = new ArrayList<String>();
        for (String data : siteContentData)
        {
            builder.append(data).append(" ");
        }
        String [] items = builder.toString().split("items");
        for (String item : items)
        {
            if (item.indexOf("-") != -1)
            {
                typesAndCounts.add(item.trim().substring(item.indexOf(")")+1));
            }   
        }
        return typesAndCounts;
    }
    
    **/
   /** 
    public List<String> getTooltipFile()
    {
        List<WebElement> pieChartSlices = getPieChartSlices();
        List<String> toolTipCounts = new ArrayList<String>();
        for (WebElement pieChartSlice : pieChartSlices)
        {
            drone.mouseOver(pieChartSlice);
            pieChartSlice.click();
            WebElement tooltipElement = drone.findAndWait(By.cssSelector(TOOLTIP_DATA));
            String items = getTooltipElement(tooltipElement.getAttribute(ORIGINAL_TITLE_ATTRIBUTE), "/div/text()[preceding-sibling::br]");
            String [] counts = items.split(" ");
            String fileCount = counts[0];
            toolTipTypes.add(getTooltipElement(tooltipElement.getAttribute(ORIGINAL_TITLE_ATTRIBUTE), "/div/strong"));
            //System.out.println("TTTTT 0 **** " + tooltipElement.getAttribute(ORIGINAL_TITLE_ATTRIBUTE));
            //System.out.println("TTTTT 1 **** " + getTooltipElement(tooltipElement.getAttribute(ORIGINAL_TITLE_ATTRIBUTE), "/div/strong"));
            System.out.println("TTTTT 2 **** " + getTooltipElement(tooltipElement.getAttribute(ORIGINAL_TITLE_ATTRIBUTE), "/div/text()[preceding-sibling::br]"));
            //System.out.println("TTTTT 3 **** " + getTooltipElement(tooltipElement.getAttribute(ORIGINAL_TITLE_ATTRIBUTE), "/div"));
        }   
        return toolTipTypes;
    }
**/
    /**
     * Gets the list of files data appearing in tooltips (file type-count) 
     * @return
     */
    public List<String> getTooltipFileData()
    {
        List<WebElement> pieChartSlices = getPieChartSlices();
        List<String> toolTipData = new ArrayList<String>();
        for (WebElement pieChartSlice : pieChartSlices)
        {
            drone.mouseOver(pieChartSlice);
            WebElement tooltipElement = drone.findAndWait(By.cssSelector(TOOLTIP_DATA));
            String fileType = getElement(tooltipElement.getAttribute(ORIGINAL_TITLE_ATTRIBUTE), "/div/strong");
            String items = getElement(tooltipElement.getAttribute(ORIGINAL_TITLE_ATTRIBUTE), "/div/text()[preceding-sibling::br]");
            String [] counts = items.split(" ");
            String fileCount = counts[0];
            StringBuilder builder = new StringBuilder();
            builder.append(fileType).append("-").append(fileCount);
            toolTipData.add(builder.toString());
        }   
        return toolTipData;
    }
    
    
    /**
     * Gets the list of file types data appearing in tooltips  
     * @return
     */
    public List<String> getTooltipFileTypes()
    {
        List<WebElement> pieChartSlices = getPieChartSlices();
        List<String> toolTipFileTypes = new ArrayList<String>();
        for (WebElement pieChartSlice : pieChartSlices)
        {
            drone.mouseOver(pieChartSlice);
            WebElement tooltipElement = drone.findAndWait(By.cssSelector(TOOLTIP_DATA));
            String fileType = getElement(tooltipElement.getAttribute(ORIGINAL_TITLE_ATTRIBUTE), "/div/strong");
            toolTipFileTypes.add(fileType);
        }   
        return toolTipFileTypes;
    }
    
    
    
    
    
    
    
    
    /**
     * Gets the list of pie chart slices elements
     * 
     * @return
     */
    private List<WebElement> getPieChartSlices()
    {
        List<WebElement> pieChartSlices = new ArrayList<WebElement>();
        try
        {
            pieChartSlices = drone.findAll(By.cssSelector(PIE_CHART_SLICES));

        }
        catch (NoSuchElementException nse)
        {
            logger.error("No Site Content Report pie chart slices " + nse);
        }
        return pieChartSlices;
    }
    
    /**
     * Parses the xml string of the original-title element 
     * 
     * @param xml
     * @param element
     * @return
     */
    /**
    private String getElement(String xml, String element)
    {
        String tooltipElement = " ";

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        InputSource source = new InputSource(new StringReader(xml));
        try
        {
            tooltipElement = (String) xpath.evaluate(element, source, XPathConstants.STRING);
        } catch (XPathExpressionException ee)
        {
            logger.error("Cannot parse xml string " + ee);
        }
        return tooltipElement;
    }
    **/
}
