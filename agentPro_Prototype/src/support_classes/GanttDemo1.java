package support_classes;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * ---------------
 * GanttDemo1.java
 * ---------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: GanttDemo1.java,v 1.12 2004/04/26 19:11:54 taqua Exp $
 *
 * Changes
 * -------
 * 06-Jun-2002 : Version 1 (DG);
 * 10-Oct-2002 : Modified to use DemoDatasetFactory (DG);
 * 10-Jan-2003 : Renamed GanttDemo --> GanttDemo1 (DG);
 * 16-Oct-2003 : Shifted dataset from DemoDatasetFactory to this class (DG);
 *
 */



import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.WorkPlan;

/**
 * A simple demonstration application showing how to create a Gantt chart.
 * <P>
 * This demo is intended to show the conceptual approach rather than being a polished
 * implementation.
 *
 *
 */
public class GanttDemo1 extends ApplicationFrame {

    /**
     * Creates a new demo.
     *
     * @param title  the frame title.
     * 
     */
	
	static WorkPlan workplan = new WorkPlan();
	
	static String DateFormat = "yyyy-MM-dd_HH.mm.ss";
	static SimpleDateFormat SimpleDateFormat = new SimpleDateFormat(DateFormat);
	static String title;
 	
    public GanttDemo1(final String title, WorkPlan workplan) {

        super(title);
        this.workplan = workplan;
        this.title = title;
        
        final IntervalCategoryDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset);

        // add the chart to a panel...
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(700, 470)); //500, 270
        setContentPane(chartPanel);
        
                
        try {
			saveChartToJPG(chart,"C:/Users/Mitarbeiter/Dropbox (HSU_Agent.Pro)/_AgentPro/Prototyp/GANTT_Charts/eclipse/"+SimpleDateFormat.format(System.currentTimeMillis())+"_gantt", 700,470);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       

    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    
    /**
     * Creates a sample dataset for a Gantt chart.
     *
     * @return The dataset.
     */
    public static IntervalCategoryDataset createDataset() {
    	
    	final TaskSeries s1 = new TaskSeries("Scheduled");
    	/*
    	AllocatedWorkingStep [] array = new AllocatedWorkingStep[workplan.getConsistsOfAllocatedWorkingSteps().size()];
    	//System.out.println("SIZE "+workplan.getConsistsOfAllocatedWorkingSteps().size());
    	
    	 @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = workplan.getConsistsOfAllocatedWorkingSteps().iterator();
    	 int counter = 1;
		    while(it.hasNext()) {
		    	AllocatedWorkingStep allWorkingStep = it.next();
		    	//System.out.println(myAgent.SimpleDateFormat.format(new Date())+" "+myAgent.getLocalName()+logLinePrefix+allWorkingStep.getHasOperation().getName()+" "+allWorkingStep.getHasResource().getName()+" "+myAgent.SimpleDateFormat.format(Long.parseLong(allWorkingStep.getHasTimeslot().getStartDate()))+" "+myAgent.SimpleDateFormat.format(Long.parseLong(allWorkingStep.getHasTimeslot().getEndDate())));
		    	if (counter%2 == 0) { // gerade
		    		array[counter-2] = allWorkingStep;
		    		counter++;
		    	}else {
		    		if(counter==workplan.getConsistsOfAllocatedWorkingSteps().size()) {
		    			array[counter-1] = allWorkingStep;
		    		}else {
		    			array[counter] = allWorkingStep;
			    		counter++;
		    		}
		    		
		    	}
		    	
		    	/*
		    	  s1.add(new Task(allWorkingStep.getHasResource().getName()+"."+allWorkingStep.getHasOperation().getName(),
		    			  new SimpleTimePeriod(date2(allWorkingStep.getHasTimeslot().getStartDate()),
				        		  date2(allWorkingStep.getHasTimeslot().getEndDate()))));	*/
		    	  //System.out.println("____________________________________________DEBUG____________ new task added"+allWorkingStep.getHasOperation().getName());		    	  
		    //}
		    /*
		    for(int i = 0;i<workplan.getConsistsOfAllocatedWorkingSteps().size();i++) {
		    	  s1.add(new Task(array[i].getHasResource().getName()+"."+array[i].getHasOperation().getName(),
		    			  new SimpleTimePeriod(date2(array[i].getHasTimeslot().getStartDate()),
				        		  date2(array[i].getHasTimeslot().getEndDate()))));	
		    }*/
		    
		    @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = workplan.getConsistsOfAllocatedWorkingSteps().iterator();
    	 
		    while(it.hasNext()) {
		    	AllocatedWorkingStep allWorkingStep = it.next();
		    	s1.add(new Task(allWorkingStep.getHasResource().getName()+"."+allWorkingStep.getHasOperation().getName(),
		    			  new SimpleTimePeriod(date2(allWorkingStep.getHasTimeslot().getStartDate()),
				        		  date2(allWorkingStep.getHasTimeslot().getEndDate()))));	
		    }

        /*
        s1.add(new Task("Write Proposal",
               new SimpleTimePeriod(date(1, Calendar.APRIL, 2001),
                                    date(5, Calendar.APRIL, 2001))));
        s1.add(new Task("Obtain Approval",
               new SimpleTimePeriod(date(9, Calendar.APRIL, 2001),
                                    date(9, Calendar.APRIL, 2001))));
        s1.add(new Task("Requirements Analysis",
               new SimpleTimePeriod(date(10, Calendar.APRIL, 2001),
                                    date(5, Calendar.MAY, 2001))));
        s1.add(new Task("Design Phase",
               new SimpleTimePeriod(date(6, Calendar.MAY, 2001),
                                    date(30, Calendar.MAY, 2001))));
        /*
        s1.add(new Task("Design Signoff",
               new SimpleTimePeriod(date(2, Calendar.JUNE, 2001),
                                    date(2, Calendar.JUNE, 2001))));
        s1.add(new Task("Alpha Implementation",
               new SimpleTimePeriod(date(3, Calendar.JUNE, 2001),
                                    date(31, Calendar.JULY, 2001))));
        s1.add(new Task("Design Review",
               new SimpleTimePeriod(date(1, Calendar.AUGUST, 2001),
                                    date(8, Calendar.AUGUST, 2001))));
        s1.add(new Task("Revised Design Signoff",
               new SimpleTimePeriod(date(10, Calendar.AUGUST, 2001),
                                    date(10, Calendar.AUGUST, 2001))));
        s1.add(new Task("Beta Implementation",
               new SimpleTimePeriod(date(12, Calendar.AUGUST, 2001),
                                    date(12, Calendar.SEPTEMBER, 2001))));
        s1.add(new Task("Testing",
               new SimpleTimePeriod(date(13, Calendar.SEPTEMBER, 2001),
                                    date(31, Calendar.OCTOBER, 2001))));
        s1.add(new Task("Final Implementation",
               new SimpleTimePeriod(date(1, Calendar.NOVEMBER, 2001),
                                    date(15, Calendar.NOVEMBER, 2001))));
        s1.add(new Task("Signoff",
               new SimpleTimePeriod(date(28, Calendar.NOVEMBER, 2001),
                                    date(30, Calendar.NOVEMBER, 2001))));

        final TaskSeries s2 = new TaskSeries("Actual");
        s2.add(new Task("Write Proposal",
               new SimpleTimePeriod(date(1, Calendar.APRIL, 2001),
                                    date(5, Calendar.APRIL, 2001))));
        s2.add(new Task("Obtain Approval",
               new SimpleTimePeriod(date(9, Calendar.APRIL, 2001),
                                    date(9, Calendar.APRIL, 2001))));
        s2.add(new Task("Requirements Analysis",
               new SimpleTimePeriod(date(10, Calendar.APRIL, 2001),
                                    date(15, Calendar.MAY, 2001))));
        s2.add(new Task("Design Phase",
               new SimpleTimePeriod(date(15, Calendar.MAY, 2001),
                                    date(17, Calendar.JUNE, 2001))));
        s2.add(new Task("Design Signoff",
               new SimpleTimePeriod(date(30, Calendar.JUNE, 2001),
                                    date(30, Calendar.JUNE, 2001))));
        s2.add(new Task("Alpha Implementation",
               new SimpleTimePeriod(date(1, Calendar.JULY, 2001),
                                    date(12, Calendar.SEPTEMBER, 2001))));
        s2.add(new Task("Design Review",
               new SimpleTimePeriod(date(12, Calendar.SEPTEMBER, 2001),
                                    date(22, Calendar.SEPTEMBER, 2001))));
        s2.add(new Task("Revised Design Signoff",
               new SimpleTimePeriod(date(25, Calendar.SEPTEMBER, 2001),
                                    date(27, Calendar.SEPTEMBER, 2001))));
        s2.add(new Task("Beta Implementation",
               new SimpleTimePeriod(date(27, Calendar.SEPTEMBER, 2001),
                                    date(30, Calendar.OCTOBER, 2001))));
        s2.add(new Task("Testing",
               new SimpleTimePeriod(date(31, Calendar.OCTOBER, 2001),
                                    date(17, Calendar.NOVEMBER, 2001))));
        s2.add(new Task("Final Implementation",
               new SimpleTimePeriod(date(18, Calendar.NOVEMBER, 2001),
                                    date(5, Calendar.DECEMBER, 2001))));
        s2.add(new Task("Signoff",
               new SimpleTimePeriod(date(10, Calendar.DECEMBER, 2001),
                                    date(11, Calendar.DECEMBER, 2001))));
		*/
        final TaskSeriesCollection collection = new TaskSeriesCollection();
        collection.add(s1);
        //collection.add(s2);

        return collection;
    }

    /**
     * Utility method for creating <code>Date</code> objects.
     *
     * @param day  the date.
     * @param month  the month.
     * @param year  the year.
     *
     * @return a date.
     */
    private static Date date(final int day, final int month, final int year) {

        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        final Date result = calendar.getTime();
        //System.out.println("____________________________________________DEBUG____1________"+result);
        //System.out.println("____________________________________________DEBUG____1________"+SimpleDateFormat.format(result));
        return result;

    }
    
    private static Date date2(String date) {
    	
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Germany/Hamburg"));
        calendar.setTimeInMillis(Long.parseLong(date));
        final Date result = calendar.getTime();
        //System.out.println("____________________________________________DEBUG_____2_______"+result);
       // System.out.println("____________________________________________DEBUG_____2_______"+SimpleDateFormat.format(result));
        return result;

    }
        
    /**
     * Creates a chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return The chart.
     */
    private JFreeChart createChart(final IntervalCategoryDataset dataset) {
        final JFreeChart chart = ChartFactory.createGanttChart(
            title,  // chart title
            "Operation",              // domain axis label
            "Date",              // range axis label
            dataset,             // data
            true,                // include legend
            true,                // tooltips
            false                // urls
        );    
//        chart.getCategoryPlot().getDomainAxis().setMaxCategoryLabelWidthRatio(10.0f);
        return chart;    
    }
    /**
     * Save chart to file in JPG format.
     *
     * @param chart JFreeChart.
     * @param fileName Name of JPG file.
     * @param width Width of JPG image.
     * @param height Height of JPG image.
     * @return Final file name used.
     * @throws IOException on error.
     */
    static public final String saveChartToJPG(final JFreeChart chart, String fileName, final int width, final int height) throws IOException {
        String result = null;
       
        if (chart != null) {
            if (fileName == null) {
                final String chartTitle = chart.getTitle().getText();
                if (chartTitle != null) {
                    fileName = chartTitle;
                } else {
                    fileName = "chart";
                }
            }
            result = fileName+".jpg";
            ChartUtilities.saveChartAsJPEG(new File(result), chart, width, height);
        }//else: input unavailable
       
        return result;
    }//saveChartToJPG()
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    /*
    public static void main(final String[] args) {

    	
        final GanttDemo1 demo = new GanttDemo1("Gantt Chart Demo 1");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
		
    }*/

}

