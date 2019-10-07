package support_classes;

/* -----------------------
 * XYTaskDatasetDemo2.java
 * -----------------------
 * (C) Copyright 2008, 2009, by Object Refinery Limited.
 *
 */

import java.awt.Color;
//import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
//import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
//import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
//import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.gantt.XYTaskDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
//import org.jfree.data.time.Day;
//import org.jfree.data.time.Hour;
import org.jfree.data.time.SimpleTimePeriod;
//import org.jfree.data.time.TimeSeries;
//import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
//import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
//import org.jfree.ui.RefineryUtilities;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Timeslot;
import agentPro.onto.WorkPlan;
//import jade.util.leap.*;
//import jade.util.leap.List;

/**
 * A demonstration of the {@link XYTaskDataset} class.
 */
public class XYTaskDataset_Total extends ApplicationFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Constructs the demo application.
     *
     * @param title  the frame title.
     */
	
	static WorkPlan workplan = new WorkPlan();
	
	static String DateFormat = "yyyy-MM-dd HH.mm.ss";
	static SimpleDateFormat SimpleDateFormat = new SimpleDateFormat(DateFormat);
	//static XYTaskDatasetDemo2 instance;
	static Storage_element [] storage_array;
	
    public XYTaskDataset_Total(String title, WorkPlan workplan) {
        super(title);
        //instance = new XYTaskDatasetDemo2(title, workplan, agent_name);
        XYTaskDataset_Total.workplan = workplan;
        
        JPanel chartPanel = createDemoPanel();
        chartPanel.setPreferredSize(new java.awt.Dimension(700, 500));
        setContentPane(chartPanel);
        
       
        //static public final String saveChartToJPG(final JFreeChart chart, String fileName, final int width, final int height) throws IOException {
    }

    /**
     * Creates a subplot.
     *
     * @param dataset  the dataset.
     *
     * @return A subplot.
     */
    
    /*
    private static XYPlot createSubplot1(XYDataset dataset) {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setUseFillPaint(true);
        renderer.setBaseFillPaint(Color.white);
        renderer.setBaseShape(new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0));
        renderer.setAutoPopulateSeriesShape(false);
        NumberAxis yAxis = new NumberAxis("Y");
        yAxis.setLowerMargin(0.1);
        yAxis.setUpperMargin(0.1);
        XYPlot plot = new XYPlot(dataset, new DateAxis("Time"), yAxis,
                renderer);
        return plot;
    }*/

    /**
     * Creates a subplot.
     *
     * @param dataset  the dataset.
     *
     * @return A subplot.
     */
    private static XYPlot createSubplot2(IntervalXYDataset dataset) {
    	  	
        DateAxis xAxis = new DateAxis("Date/Time");
    	
        int counter = 0;
        for(Storage_element element : storage_array) {
	    	
	    	if(element != null) {
	    		counter++;
	    	}
	    	
        }
        String [] names = new String [counter];
       
        for(int i = 0;i<counter;i++) {
        	names[i]=storage_array[i].getID();
        }
        
        SymbolAxis yAxis = new SymbolAxis("resources", names);
        
        //SymbolAxis yAxis = new SymbolAxis("Resources", new String[] {"Team A",
        //        "Team B", "Team C", "Team D", "Team E"});
        yAxis.setGridBandsVisible(false);
        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setUseYInterval(true);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        return plot;
    }

    /**
     * Creates a demo chart.
     *
     * @return A demo chart.
     */
    private static JFreeChart createChart() {
        CombinedDomainXYPlot plot = new CombinedDomainXYPlot(
                new DateAxis("Date/Time"));
        plot.setDomainPannable(true);
        //plot.add(createSubplot1(createDataset1()));
        plot.add(createSubplot2(createDataset2()));
        JFreeChart chart = new JFreeChart("Total_Schedule", plot);
        chart.setBackgroundPaint(Color.white);
        ChartUtilities.applyCurrentTheme(chart);
        
        try {
			saveChartToJPG(chart,"F:/TeamDrive/Agent.Pro/_AgentPro/Prototyp/GANTT_Charts/eclipse/"+SimpleDateFormat.format(System.currentTimeMillis())+"_gantt_RESOURCES.", 700,470);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return chart;
    }

    /**
     * Creates a panel for the demo.
     *
     * @return A panel.
     */
    public static JPanel createDemoPanel() {
        return new ChartPanel(createChart());
        
    }


    /**
     * Creates a dataset for the demo.  Normally a dataset wouldn't be hard
     * coded like this - it would be read from a file or a database or some
     * other source.
     *
     * @return A dataset.
     */
    private static IntervalXYDataset createDataset2() {
        XYTaskDataset dataset = new XYTaskDataset(createTasks());
        dataset.setTransposed(true);
        dataset.setSeriesWidth(0.6);
        return dataset;
    }

    /**
     * Creates a task series collection.
     *
     * @return A task series collection.
     */
    private static TaskSeriesCollection createTasks() {
        TaskSeriesCollection dataset = new TaskSeriesCollection();
        
        int size_allWS = workplan.getConsistsOfAllocatedWorkingSteps().size();
        
        Storage_element []	array_of_storage_elements = new Storage_element[ size_allWS ] ;			//captures elements of storage (that contain id (string) and timeslots as a list)
        
        //ArrayList <Timeslot> array_list = new ArrayList <>();
        
        
        @SuppressWarnings("unchecked")
		Iterator<AllocatedWorkingStep> it = workplan.getConsistsOfAllocatedWorkingSteps().iterator();
	    while(it.hasNext()) {
	    	
	    	AllocatedWorkingStep allWorkingStep = it.next();
	    	String resource_name = allWorkingStep.getHasResource().getName();
	    	
	    	boolean id_found = false;
	    	for(int i = 0;i<size_allWS;i++) {	
	    		
	    		if(array_of_storage_elements[i] != null && array_of_storage_elements[i].getID().equals(resource_name)) {		
	    			//array [i] = new storage(workpiece_ID);
	    			array_of_storage_elements[i].addHasSlots(allWorkingStep.getHasTimeslot());    			//falls es für diese resource schon timeslots gibt --> hinzufügen
	    			id_found = true;
	    			//System.out.println("DEBUG_array["+i+"] slot "+allWorkingStep.getHasTimeslot().getStartDate()+ "added for "+resource_name);
	    		}
	    	}    	
	    	if(!id_found) {	//wenn id nicht gefunden wurde --> neues storage element auf erstem freien platz
		    	for(int i = 0;i<size_allWS;i++) {	
		    		
		    		if(array_of_storage_elements[i] == null) {		
		    			array_of_storage_elements [i] = new Storage_element(resource_name);
		    			array_of_storage_elements[i].addHasSlots(allWorkingStep.getHasTimeslot());    			//falls es für dieses workpiece schon timeslots gibt --> hinzufügen
		    			//System.out.println("DEBUG_NEW element in array["+i+"] slot "+allWorkingStep.getHasTimeslot().getStartDate()+ "added for "+workpiece_ID);
		    			break;
		    		}
		    	} 
	    	}
	    	storage_array = array_of_storage_elements;
    
	    }
	    
	    /*
	     * jetzt hat man ein array aus storage elementen, die jeweils den resource namen und alle Timeslots dazu haben
	     * jetzt soll für jedes dieser elemente eine task series erzeugt werden und die Timeslots hinzugefügt werden
	     */
	    
	   //int counter = 1;
	    for(Storage_element element : array_of_storage_elements) {
	    	
	    	if(element != null) {
		    	TaskSeries taskseries = new TaskSeries(element.getID());
		    	//System.out.println("DEBUG XY TaskData element.getID() "+element.getID());
		    	 @SuppressWarnings("unchecked")
		 		Iterator<Timeslot> iterator = element.getAllHasSlots();
		 	    while(iterator.hasNext()) {
		 	    	Timeslot slot = iterator.next();
		 	    	//System.out.println("DEBUG XY TaskData slot.getStartDate() "+SimpleDateFormat.format(Long.parseLong(slot.getStartDate()))+" slot.getEndDate()  "+SimpleDateFormat.format(Long.parseLong(slot.getEndDate())));
		 	    	taskseries.add(new Task("test",  new SimpleTimePeriod(date2(slot.getStartDate()),
		          		  date2(slot.getEndDate()))));
		 	    	//s.add(new Task("transport",   new Hour(11, new Day())));
		 	    }
		 	    dataset.add(taskseries);
	    	}
    	
	    }

	    /*
        TaskSeries s1 = new TaskSeries("Team A");
        s1.add(new Task("T1a", new Hour(11, new Day())));
        s1.add(new Task("T1b", new Hour(14, new Day())));
        s1.add(new Task("T1c", new Hour(16, new Day())));
        /*
        TaskSeries s2 = new TaskSeries("Team B");
        s2.add(new Task("T2a", new Hour(13, new Day())));
        s2.add(new Task("T2b", new Hour(19, new Day())));
        s2.add(new Task("T2c", new Hour(21, new Day())));
        TaskSeries s3 = new TaskSeries("Team C");
        s3.add(new Task("T3a", new Hour(13, new Day())));
        s3.add(new Task("T3b", new Hour(19, new Day())));
        s3.add(new Task("T3c", new Hour(21, new Day())));
        TaskSeries s4 = new TaskSeries("Team D");
        s4.add(new Task("T4a", new Day()));
        TaskSeries s5 = new TaskSeries("Team E");
        s5.add(new Task("T5a", new Day()));
        dataset.add(s1);
        dataset.add(s2);
        dataset.add(s3);
        dataset.add(s4);
        dataset.add(s5);
        */
	    
        return dataset;
    }
    
private static Date date2(String date) {
    	
    	
	 final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Germany/Hamburg"));
        calendar.setTimeInMillis(Long.parseLong(date));
        final Date result = calendar.getTime();
        //System.out.println("_______________________"+date+"_____________________DEBUG_____2_______"+result);
        //System.out.println("____________________________________________DEBUG_____2_______"+SimpleDateFormat.format(result));
        return result;

    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    /*
    public static void main(String[] args) {
        XYTaskDatasetDemo2 demo = new XYTaskDatasetDemo2(
                "JFreeChart : XYTaskDatasetDemo2.java");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }*/
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

}