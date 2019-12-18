package webservice;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import agentPro.onto.AllocatedWorkingStep;
import agentPro.onto.Resource;
import agentPro.onto.WorkPlan;


public class Webservice_agentPro {
	
	//private static ManufacturingOrderList mol;
	
	//private final static String USER_AGENT = "Mozilla/5.0";
	private static String soap_getById1 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\r\n" + 
			"   <soapenv:Header/>\r\n" + 
			"   <soapenv:Body>\r\n" + 
			"      <tem:GetManufacturingOdersById>\r\n" + 
			"         <tem:serviceId>9A79D9D5-9270-42CA-BFCB-7D34D88937D2</tem:serviceId>\r\n" + 
			"         <tem:manufacturingOrders>&lt;?xml version=\"1.0\" encoding=\"utf-16\"?&gt;&#xD;\r\n" + 
			"         	&lt;ManufacturingOrderList xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"&gt;&#xD;\r\n" + 
			"  &lt;ManufacturingOrders&gt;&#xD;\r\n" + 
			"    &lt;ManufacturingOrder&gt;&#xD;\r\n" + 
			"      &lt;ID&gt;";

	private static String soap_getById2 = "&lt;/ID&gt;&#xD;\r\n" + 
			"        &lt;/ManufacturingOrder&gt;&#xD;\r\n" + 
			"         &lt;/ManufacturingOrders&gt;&#xD;\r\n" + 
			"         &lt;/ManufacturingOrderList&gt;\r\n" + 
			"		 </tem:manufacturingOrders>\r\n" + 
			"      </tem:GetManufacturingOdersById>\r\n" + 
			"   </soapenv:Body>\r\n" + 
			"</soapenv:Envelope>";
	
	public static String soap_getAll = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\r\n" + 
     		"   <soapenv:Header/>\r\n" + 
     		"   <soapenv:Body>\r\n" + 
     		"      <tem:GetAllManufacturingOrders>\r\n" + 
     		"         <tem:serviceId>9A79D9D5-9270-42CA-BFCB-7D34D88937D2</tem:serviceId>\r\n" + 
     		"      </tem:GetAllManufacturingOrders>\r\n" + 
     		"   </soapenv:Body>\r\n" + 
     		"</soapenv:Envelope>\r\n" + 
     		"";
	private static String soap_updateOrder1 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\r\n" + 
			"   <soapenv:Header/>\r\n" + 
			"   <soapenv:Body>\r\n" + 
			"      <tem:UpdateManufacturingOrder>\r\n" + 
			"         <tem:serviceId>9A79D9D5-9270-42CA-BFCB-7D34D88937D2</tem:serviceId>\r\n" + 
			"         <tem:updatedManufacturingOrder>"; 
	/*
	private static String soap_updateOrder_notused =	
			" &lt;ManufacturingOrderList xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"&gt;&#xD;\r\n"+
			"  &lt;ManufacturingOrders&gt;&#xD;\r\n" + 
			"    &lt;ManufacturingOrder&gt;&#xD;\r\n" + 
			"      &lt;ID&gt;9&lt;/ID&gt;&#xD;\r\n" + 
			"      &lt;Name&gt;FA_D534-78834-200-16 Rev. A (#5)&lt;/Name&gt;&#xD;\r\n" + 
			"      &lt;DeliveryDate&gt;2026-05-10T00:00:00&lt;/DeliveryDate&gt;&#xD;\r\n" + 
			"      &lt;Article&gt;&#xD;\r\n" + 
			"        &lt;ID&gt;1&lt;/ID&gt;&#xD;\r\n" + 
			"        &lt;Name&gt;D534-78834-200-16&lt;/Name&gt;&#xD;\r\n" + 
			"      &lt;/Article&gt;&#xD;\r\n" + 
			"      &lt;ActualProcess&gt;&#xD;\r\n" + 
			"        &lt;ID&gt;9&lt;/ID&gt;&#xD;\r\n" + 
			"        &lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"        &lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"        &lt;PlannedStartEarliest xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"        &lt;PlannedEndLatest&gt;2019-05-09T00:00:00&lt;/PlannedEndLatest&gt;&#xD;\r\n" + 
			"        &lt;ProcessSchedule&gt;&#xD;\r\n" + 
			"          &lt;ID&gt;1&lt;/ID&gt;&#xD;\r\n" + 
			"          &lt;Name&gt;Fertigungsprozess für D534-78834-200-16&lt;/Name&gt;&#xD;\r\n" + 
			"          &lt;ProcessSteps&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;1&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;2&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;35&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;12&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;2&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;12&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;0&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;1&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;3&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;7&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;15&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;2.5&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;4&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;8&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;10&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;2.5&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;5&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;9&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;10&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;2.5&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;6&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;10&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;10&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;2.5&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;7&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;1&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;10&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;1&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;8&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;3&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;20&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;9&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;9&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;6&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;0&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;1&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;10&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;5&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;0&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;1&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;11&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;4&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;5&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;4&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ProcessStep&gt;&#xD;\r\n" + 
			"              &lt;ID&gt;12&lt;/ID&gt;&#xD;\r\n" + 
			"              &lt;Order&gt;11&lt;/Order&gt;&#xD;\r\n" + 
			"              &lt;SetUpTimeMinutes&gt;10&lt;/SetUpTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;JobTimeMinutes&gt;1&lt;/JobTimeMinutes&gt;&#xD;\r\n" + 
			"              &lt;BufferTimeMinutes&gt;0&lt;/BufferTimeMinutes&gt;&#xD;\r\n" + 
			"            &lt;/ProcessStep&gt;&#xD;\r\n" + 
			"          &lt;/ProcessSteps&gt;&#xD;\r\n" + 
			"        &lt;/ProcessSchedule&gt;&#xD;\r\n" + 
			"        &lt;ActualProcessSteps&gt;&#xD;\r\n" + 
			"          &lt;ActualProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ID&gt;19&lt;/ID&gt;&#xD;\r\n" + 
			"            &lt;Order&gt;1&lt;/Order&gt;&#xD;\r\n" + 
			"            &lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;PlannedStart&gt;2019-08-14T16:25:00&lt;/PlannedStart&gt;&#xD;\r\n" + 
			"            &lt;PlannedStartDispatch&gt;2019-08-14T16:25:00&lt;/PlannedStartDispatch&gt;&#xD;\r\n" + 
			"            &lt;PlannedEndDispatch&gt;2019-08-16T16:45:00&lt;/PlannedEndDispatch&gt;&#xD;\r\n" + 
			"            &lt;Ressource /&gt;&#xD;\r\n" + 
			"          &lt;/ActualProcessStep&gt;&#xD;\r\n" + 
			"          &lt;ActualProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ID&gt;20&lt;/ID&gt;&#xD;\r\n" + 
			"            &lt;Order&gt;2&lt;/Order&gt;&#xD;\r\n" + 
			"            &lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;PlannedStart&gt;2019-05-08T16:45:00&lt;/PlannedStart&gt;&#xD;\r\n" + 
			"            &lt;PlannedStartDispatch&gt;2019-05-08T16:45:00&lt;/PlannedStartDispatch&gt;&#xD;\r\n" + 
			"            &lt;PlannedEndDispatch&gt;2019-05-08T19:20:00&lt;/PlannedEndDispatch&gt;&#xD;\r\n" + 
			"            &lt;Ressource /&gt;&#xD;\r\n" + 
			"          &lt;/ActualProcessStep&gt;&#xD;\r\n" + 
			"          &lt;ActualProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ID&gt;21&lt;/ID&gt;&#xD;\r\n" + 
			"            &lt;Order&gt;3&lt;/Order&gt;&#xD;\r\n" + 
			"            &lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;PlannedStart&gt;2019-05-08T19:20:00&lt;/PlannedStart&gt;&#xD;\r\n" + 
			"            &lt;PlannedStartDispatch&gt;2019-05-08T19:20:00&lt;/PlannedStartDispatch&gt;&#xD;\r\n" + 
			"            &lt;PlannedEndDispatch&gt;2019-05-08T21:10:00&lt;/PlannedEndDispatch&gt;&#xD;\r\n" + 
			"            &lt;Ressource /&gt;&#xD;\r\n" + 
			"          &lt;/ActualProcessStep&gt;&#xD;\r\n" + 
			"          &lt;ActualProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ID&gt;22&lt;/ID&gt;&#xD;\r\n" + 
			"            &lt;Order&gt;4&lt;/Order&gt;&#xD;\r\n" + 
			"            &lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;PlannedStart&gt;2019-05-08T21:10:00&lt;/PlannedStart&gt;&#xD;\r\n" + 
			"            &lt;PlannedStartDispatch&gt;2019-05-08T21:10:00&lt;/PlannedStartDispatch&gt;&#xD;\r\n" + 
			"            &lt;PlannedEndDispatch&gt;2019-05-08T21:55:00&lt;/PlannedEndDispatch&gt;&#xD;\r\n" + 
			"            &lt;Ressource /&gt;&#xD;\r\n" + 
			"          &lt;/ActualProcessStep&gt;&#xD;\r\n" + 
			"          &lt;ActualProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ID&gt;23&lt;/ID&gt;&#xD;\r\n" + 
			"            &lt;Order&gt;5&lt;/Order&gt;&#xD;\r\n" + 
			"            &lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;PlannedStart&gt;2019-05-08T21:55:00&lt;/PlannedStart&gt;&#xD;\r\n" + 
			"            &lt;PlannedStartDispatch&gt;2019-05-08T21:55:00&lt;/PlannedStartDispatch&gt;&#xD;\r\n" + 
			"            &lt;PlannedEndDispatch&gt;2019-05-08T22:05:00&lt;/PlannedEndDispatch&gt;&#xD;\r\n" + 
			"            &lt;Ressource /&gt;&#xD;\r\n" + 
			"          &lt;/ActualProcessStep&gt;&#xD;\r\n" + 
			"          &lt;ActualProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ID&gt;24&lt;/ID&gt;&#xD;\r\n" + 
			"            &lt;Order&gt;6&lt;/Order&gt;&#xD;\r\n" + 
			"            &lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;PlannedStart&gt;2019-05-08T22:05:00&lt;/PlannedStart&gt;&#xD;\r\n" + 
			"            &lt;PlannedStartDispatch&gt;2019-05-08T22:05:00&lt;/PlannedStartDispatch&gt;&#xD;\r\n" + 
			"            &lt;PlannedEndDispatch&gt;2019-05-08T22:15:00&lt;/PlannedEndDispatch&gt;&#xD;\r\n" + 
			"            &lt;Ressource /&gt;&#xD;\r\n" + 
			"          &lt;/ActualProcessStep&gt;&#xD;\r\n" + 
			"          &lt;ActualProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ID&gt;25&lt;/ID&gt;&#xD;\r\n" + 
			"            &lt;Order&gt;7&lt;/Order&gt;&#xD;\r\n" + 
			"            &lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;PlannedStart&gt;2019-05-08T22:15:00&lt;/PlannedStart&gt;&#xD;\r\n" + 
			"            &lt;PlannedStartDispatch&gt;2019-05-08T22:15:00&lt;/PlannedStartDispatch&gt;&#xD;\r\n" + 
			"            &lt;PlannedEndDispatch&gt;2019-05-08T22:55:00&lt;/PlannedEndDispatch&gt;&#xD;\r\n" + 
			"            &lt;Ressource /&gt;&#xD;\r\n" + 
			"          &lt;/ActualProcessStep&gt;&#xD;\r\n" + 
			"          &lt;ActualProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ID&gt;26&lt;/ID&gt;&#xD;\r\n" + 
			"            &lt;Order&gt;8&lt;/Order&gt;&#xD;\r\n" + 
			"            &lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;PlannedStart&gt;2019-05-08T22:55:00&lt;/PlannedStart&gt;&#xD;\r\n" + 
			"            &lt;PlannedStartDispatch&gt;2019-05-08T22:55:00&lt;/PlannedStartDispatch&gt;&#xD;\r\n" + 
			"            &lt;PlannedEndDispatch&gt;2019-05-08T23:30:00&lt;/PlannedEndDispatch&gt;&#xD;\r\n" + 
			"            &lt;Ressource /&gt;&#xD;\r\n" + 
			"          &lt;/ActualProcessStep&gt;&#xD;\r\n" + 
			"          &lt;ActualProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ID&gt;27&lt;/ID&gt;&#xD;\r\n" + 
			"            &lt;Order&gt;9&lt;/Order&gt;&#xD;\r\n" + 
			"            &lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;PlannedStart&gt;2019-05-08T23:30:00&lt;/PlannedStart&gt;&#xD;\r\n" + 
			"            &lt;PlannedStartDispatch&gt;2019-05-08T23:30:00&lt;/PlannedStartDispatch&gt;&#xD;\r\n" + 
			"            &lt;PlannedEndDispatch&gt;2019-05-08T23:50:00&lt;/PlannedEndDispatch&gt;&#xD;\r\n" + 
			"            &lt;Ressource /&gt;&#xD;\r\n" + 
			"          &lt;/ActualProcessStep&gt;&#xD;\r\n" + 
			"          &lt;ActualProcessStep&gt;&#xD;\r\n" + 
			"            &lt;ID&gt;28&lt;/ID&gt;&#xD;\r\n" + 
			"            &lt;Order&gt;10&lt;/Order&gt;&#xD;\r\n" + 
			"            &lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;\r\n" + 
			"            &lt;PlannedStart&gt;2019-05-08T23:50:00&lt;/PlannedStart&gt;&#xD;\r\n" + 
			"            &lt;PlannedStartDispatch&gt;2019-05-08T23:50:00&lt;/PlannedStartDispatch&gt;&#xD;\r\n" + 
			"            &lt;PlannedEndDispatch&gt;2019-05-09T00:00:00&lt;/PlannedEndDispatch&gt;&#xD;\r\n" + 
			"            &lt;Ressource /&gt;&#xD;\r\n" + 
			"          &lt;/ActualProcessStep&gt;&#xD;\r\n" + 
			"        &lt;/ActualProcessSteps&gt;&#xD;\r\n" + 
			"      &lt;/ActualProcess&gt;&#xD;\r\n" + 
			"    &lt;/ManufacturingOrder&gt;&#xD;\r\n" + 
			"  &lt;/ManufacturingOrders&gt;&#xD;\r\n" + 
			"&lt;/ManufacturingOrderList&gt;\r\n";
	*/
			private static String soap_updateOrder2 =
			"		 </tem:updatedManufacturingOrder>\r\n" + 
			"      </tem:UpdateManufacturingOrder>\r\n" + 
			"   </soapenv:Body>\r\n" + 
			"</soapenv:Envelope>";
	
	public static String soapAction_getAll = "http://tempuri.org/IWebService/GetAllManufacturingOrders";
	public static String soapAction_getById = "http://tempuri.org/IWebService/GetManufacturingOdersById";
	public static String soapAction_updateOrder = "http://tempuri.org/IWebService/UpdateManufacturingOrder";
	public static String soapAction_getOperationPlan = "http://tempuri.org/IWebService/GetOperationPlan";
	private static String password_getAll = "1xIZPL6f0ejppjp0OPBR";
	private static String address_Webservice = "https://move.a-t-solution.de/Move_AgentPro/Webservice/WebService.svc";
	private static String username_Webservice = "Move_AgentPro";

	private static String soap_OperationPlan1 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\r\n" + 
			"   <soapenv:Header/>\r\n" + 
			"   <soapenv:Body>\r\n" + 
			"      <tem:GetOperationPlan>\r\n" + 
			"         <tem:serviceId>9A79D9D5-9270-42CA-BFCB-7D34D88937D2</tem:serviceId>\r\n" + 
			"         <tem:ressourceId>";

	private static String soap_getOperationPlan2 = "</tem:ressourceId>\r\n" + 
			"      </tem:GetOperationPlan>\r\n" + 
			"   </soapenv:Body>\r\n" + 
			"</soapenv:Envelope>";
	
	public static void main(String[] args) throws Exception {
		
		
		
		
		HttpClient client = HttpClient.newBuilder()	
				.version(HttpClient.Version.HTTP_1_1)
				
				  .build();
		//HttpRequest request = buildRequest(soapAction_getAll, soap_getAll);	
		//HttpRequest request = buildRequest(soapAction_getById, buildSOAPBodyGetByID(9));
		HttpRequest request = buildRequest(soapAction_getOperationPlan, buildSOAPBodyGetOperationPlan(3));
		
		
		  // HttpRequest request = HttpRequest.newBuilder()
		    //     .uri(URI.create("http://foo.com/"))
		      //   .build();
/*
		HttpRequest request = HttpRequest.newBuilder()
			
			     //.uri(URI.create("http://openjdk.java.net/"))
			     .uri(URI.create(address_Webservice))
			     .header("Authorization", basicAuth(username_Webservice, password_getAll))
			     //.header("serviceId", "9A79D9D5-9270-42CA-BFCB-7D34D88937D2")
			     .header("Content-Type", "text/xml")
			     .header("SOAPAction", soapAction_getAll)
			     .timeout(Duration.ofMinutes(1))

			     .POST(BodyPublishers.ofString(soap_getAll))
			     //.GET()
			     .build();
		*/
		//HttpResponse<String> response = client.send(request, BodyHandlers.ofString()); //send uses blocking mode --> synchronous

		//System.out.println("Response status code: " + response.statusCode());
		//System.out.println("Response headers: " + response.headers());
		//System.out.println("Response body: " + response.body());
		
		
		
		//client.sendAsync(request, BodyHandlers.ofString())

		
      //.thenApply(HttpResponse::body)
      //.thenAccept(System.out::println)
      //.join(); 


		String a = client.sendAsync(request, BodyHandlers.ofString())
		.thenApply(HttpResponse::body).get();
		System.out.println(a+" QQQQQQQQQQQQQQQQQQQQQQQQQQ");
	
		OperationPlan opPlan = unmarshallStringToOperationPlan(a);
		System.out.println(opPlan.Ressource_Name+" "+opPlan.Ressource_ID);
		for(TimeSlot ts : opPlan.TimeSlots) {
			System.out.println(ts.Start+":"+ts.End);
		}
		
		/*
        System.out.println("Access file using absolute path: ");
        String absolutePath = "C://Users/Gehlhoff/eclipse-workspace/testing/orders.xml";
        File file = new File(absolutePath);
        printPaths(file);
        System.out.println(file.getName());
		
		client.sendAsync(request, BodyHandlers.ofFile(Paths.get("C://Users/Gehlhoff/eclipse-workspace/testing/orders.xml")))
        .thenApply(HttpResponse::body)
        .get(); 
        
		//client.sendAsync(request, BodyHandlers.ofFile(Paths.get("webservice_Get.json")))

		
		
		mol = unmarshallStringToManufacturingOrderList(a);
		mol.manufacturingorders.get(0).DeliveryDate = "2028-05-10T00:00:00";
		
		HttpRequest request2 = buildRequest(soapAction_updateOrder, buildSOAPBodyUpdateManufacturingOrder(mol));
		String b = client.sendAsync(request2, BodyHandlers.ofString())
				.thenApply(HttpResponse::body).get();
		System.out.println(b+" QQQQQQQQQQQQQQQQQQQQQQQQQQ");
		*/
/*
		HttpRequest request2 = HttpRequest.newBuilder()
			
			     //.uri(URI.create("http://openjdk.java.net/"))
			     .uri(URI.create("https://move.a-t-solution.de/Move_AgentPro/Webservice/WebService.svc"))
			     .header("Authorization", basicAuth("Move_AgentPro", password_getAll))
			     //.header("serviceId", "9A79D9D5-9270-42CA-BFCB-7D34D88937D2")
			     .header("Content-Type", "text/xml")
			     .header("SOAPAction", soapAction_updateOrder)
			     .timeout(Duration.ofMinutes(1))

			     .POST(BodyPublishers.ofString(soap_updateOrder))
			     //.GET()
			     .build();
		
		String b = client.sendAsync(request2, BodyHandlers.ofString())
				.thenApply(HttpResponse::body).get();
		System.out.println(b);
		*/
		//tryJaxb(b);
		

	}
	public static OperationPlan unmarshallStringToOperationPlan(String a) {
		OperationPlan opPlan = new OperationPlan();
		String b = a.replace("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><GetOperationPlanResponse xmlns=\"http://tempuri.org/\"><GetOperationPlanResult>&lt;?xml version=\"1.0\" encoding=\"utf-16\"?&gt;&#xD;", "");
		b = b.replace("&lt;", "<");
		b = b.replace("&gt;", ">");
		b = b.replace("&#xD;", "");
		b = b.replace("</GetOperationPlanResult></GetOperationPlanResponse></s:Body></s:Envelope>", "");
		//System.out.println("start printout \n"+b);
		JAXBContext jaxbContext;
		try
		{
		    jaxbContext = JAXBContext.newInstance(OperationPlan.class);             
		 
		    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		    //Marshaller marshaller = jaxbContext.createMarshaller();
		 
		    opPlan = (OperationPlan) jaxbUnmarshaller.unmarshal(new StringReader(b));

		    //marshaller.marshal(opPlan, new File("edited.xml"));

		}
		catch (JAXBException e)
		{
		    e.printStackTrace();
		}	
		return opPlan;
		
	}
	public static String buildSOAPBodyGetByID(int id) {
		String body = soap_getById1+id+soap_getById2;
		return body;
	}
	
	public static ManufacturingOrderList addToManufacturingOrderList(WorkPlan wp, ManufacturingOrder mo) {
		ManufacturingOrderList mol = new ManufacturingOrderList();

		   @SuppressWarnings("unchecked")
			Iterator<AllocatedWorkingStep> it = wp.getConsistsOfAllocatedWorkingSteps().iterator();
		   int i = 1;
		    while(it.hasNext()) {
		    	AllocatedWorkingStep allWS = it.next();
		    	if(!allWS.getHasResource().getName().contains("Puffer")) {
			    	
			    	for(ActualProcessStep aps : mo.ActualProcess.ActualProcessSteps) {
			    		if(aps.Order == i) {
			    			if(aps.Ressource == null) {
			    				aps.Ressource = new ArrayList<webservice.Resource>();
			    			}
			    			aps.Ressource.add(Webservice_agentPro.convertResource(allWS.getHasResource()));
			    			//aps.Ressource.set(0, Webservice_agentPro.convertResource(allWS.getHasResource()));
			    			aps.PlannedStart = (Date) new Date(Long.parseLong(allWS.getHasTimeslot().getStartDate()));
			    			aps.PlannedEnd = (Date) new Date(Long.parseLong(allWS.getHasTimeslot().getEndDate()));
			    		}
			    	}
			    	i++;
		    	}	    
		    }
		    mol.manufacturingorders.add(mo);
		return mol;
	}

	private static webservice.Resource convertResource(Resource hasResource) {
		webservice.Resource res = new webservice.Resource();
		res.Name = hasResource.getName();
		res.ID = hasResource.getID_Number();
		res.Status = 1;
		res.QuantityOfAllocations = 1;
		
		return res;
	}
	public static String buildSOAPBodyUpdateManufacturingOrder(ManufacturingOrderList mo) {		
		String mo_string = marshallToString(mo);
		String mo_1 = mo_string.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
		String mo_2 = mo_1.replace("<ManufacturingOrderList>", "&lt;ManufacturingOrderList xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"&gt;&#xD;");
		String mo_3 = mo_2.replace("<", "&lt;");
		String mo_4 = mo_3.replace(">", "&gt;&#xD;");
		String mo_5 = mo_4.replace("&lt;ActualStart&gt;&#xD;&lt;/ActualStart&gt;&#xD;", "&lt;ActualStart xsi:nil=\"true\" /&gt;&#xD;");
		String mo_6 = mo_5.replace("&lt;ActualEnd&gt;&#xD;&lt;/ActualEnd&gt;&#xD;", "&lt;ActualEnd xsi:nil=\"true\" /&gt;&#xD;");
		String mo_7 = mo_6.replace("&lt;PlannedStartEarliest&gt;&#xD;&lt;/PlannedStartEarliest&gt;&#xD;", "&lt;PlannedStartEarliest xsi:nil=\"true\" /&gt;&#xD;");
		//String mo_8	= mo_7.replace("&lt;ID&gt;&#xD;9&lt;/ID&gt;&#xD;", "&lt;ID&gt;9&lt;/ID&gt;&#xD;\r\n");
		String body = soap_updateOrder1+mo_7+soap_updateOrder2;
		//String body = soap_updateOrder1+soap_updateOrder_notused+soap_updateOrder2;
		System.out.println("PRINT OUT BODY /n"+body);
		//String body1 = body.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
		//String body2 = body1.replace("<ManufacturingOrderList>", "&lt;ManufacturingOrderList xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"&gt;&#xD;");
		//System.out.println("QQQQQQQQQQQQQQQQQQ NEW BODY "+body);
		return body;
	} 
	
	public static HttpRequest buildRequest(String soapAction, String soap_body) {

		HttpRequest request = HttpRequest.newBuilder()
				
			     //.uri(URI.create("http://openjdk.java.net/"))
			     .uri(URI.create(address_Webservice))
			     .header("Authorization", basicAuth(username_Webservice, password_getAll))
			     //.header("serviceId", "9A79D9D5-9270-42CA-BFCB-7D34D88937D2")
			     .header("Content-Type", "text/xml")
			     .header("SOAPAction", soapAction)
			     .timeout(Duration.ofMinutes(1))

			     .POST(BodyPublishers.ofString(soap_body))
			     //.GET()
			     .build();
		return request;
	}
	
	public static String buildSOAPBodyGetOperationPlan(int id) {
		String body = soap_OperationPlan1+id+soap_getOperationPlan2;
		return body;
		
	}

	public static ManufacturingOrderList unmarshallStringToManufacturingOrderList(String a) {
		ManufacturingOrderList mo = new ManufacturingOrderList();
		String b = a.replace("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><GetAllManufacturingOrdersResponse xmlns=\"http://tempuri.org/\"><GetAllManufacturingOrdersResult>", "");
		b = b.replace("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><GetManufacturingOdersByIdResponse xmlns=\"http://tempuri.org/\"><GetManufacturingOdersByIdResult>", "");
		String b1 = b.replace("&lt;?xml version=\"1.0\" encoding=\"utf-16\"?&gt;&#xD;", "");
		String c = b1.replace("&lt;", "<");
		String d = c.replace("&gt;", ">");
		String f = d.replace("&#xD;", "");
		String g = f.replace("</GetAllManufacturingOrdersResult></GetAllManufacturingOrdersResponse></s:Body></s:Envelope>", "");
		g = g.replace("</GetManufacturingOdersByIdResult></GetManufacturingOdersByIdResponse></s:Body></s:Envelope>", "");
		//System.out.println("start printout \n"+g);
		JAXBContext jaxbContext;
		try
		{
		    jaxbContext = JAXBContext.newInstance(ManufacturingOrderList.class);             
		 
		    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		    Marshaller marshaller = jaxbContext.createMarshaller();
		 
		    mo = (ManufacturingOrderList) jaxbUnmarshaller.unmarshal(new StringReader(g));
    
		    /*
		    for(ManufacturingOrder order : mo.manufacturingorders) {
		    	System.out.println("Article: "+order.Article.Name	+ " ID: "+order.ID);
		    	if(order.ID == 9) {
		    		for(ProcessStep ps : order.ActualProcess.ProcessSchedule.ProcessSteps) {
		    			System.out.println(ps.Order+" "+ps.Name + " "+ps.JobTimeMinutes);
		    			
		    		}
		    	
		    		for(ActualProcessStep aps : order.ActualProcess.ActualProcessSteps) {
		    			System.out.println(aps.Order+"  "+" "+aps.PlannedStart);
		    			
		    			Resource r = new Resource("Unima");
		    			ArrayList<Resource>list = new ArrayList<Resource>();
		    			list.add(r);
		    		aps.Ressource = list;
		    		System.out.println(aps.Ressource.get(0).Name);	    		
		    		}
		    		
		    	}
		    }
		    */
		    marshaller.marshal(mo, new File("edited.xml"));
		    
		    /*
		    System.out.println(mo.getManufacturingorders().get(0).getID());
		    System.out.println(mo.getManufacturingorders().get(0).getActualProcess().getPlannedEndLatest());
		    System.out.println(((ActualProcessStep)mo.getManufacturingorders().get(0).getActualProcess().getActualProcessSteps().get(0)).getID());
		    System.out.println(((ActualProcessStep)mo.getManufacturingorders().get(0).getActualProcess().getActualProcessSteps().get(0)).getPlannedStart());
			*/
		}
		catch (JAXBException e)
		{
		    e.printStackTrace();
		}	
		return mo;
	}
	
	public static String marshallToString (ManufacturingOrderList mo) {
		JAXBContext jaxbContext;
		try
		{
		    jaxbContext = JAXBContext.newInstance(ManufacturingOrderList.class);             		 
		    Marshaller marshaller = jaxbContext.createMarshaller();
		    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		    StringWriter sw = new StringWriter();
		    marshaller.marshal(mo, new File("not_edited.xml"));	  
		    marshaller.marshal(mo, sw);	 
		    String result = sw.toString();
		    return result;
		    
		    /*
		    for(ManufacturingOrder order : mo.manufacturingorders) {
		    	System.out.println("Article: "+order.Article.Name	+ " ID: "+order.ID);
		    	if(order.ID == 9) {
		    		for(ProcessStep ps : order.ActualProcess.ProcessSchedule.ProcessSteps) {
		    			System.out.println(ps.Order+" "+ps.Name + " "+ps.JobTimeMinutes);
		    			
		    		}
		    	
		    		for(ActualProcessStep aps : order.ActualProcess.ActualProcessSteps) {
		    			System.out.println(aps.Order+"  "+" "+aps.PlannedStart);
		    			
		    			Resource r = new Resource("Unima");
		    			ArrayList<Resource>list = new ArrayList<Resource>();
		    			list.add(r);
		    		aps.Ressource = list;
		    		System.out.println(aps.Ressource.get(0).Name);	    		
		    		}
		    		
		    	}
		    }
		    */
		    //marshaller.marshal(mo, new File("edited.xml"));
		    
		    /*
		    System.out.println(mo.getManufacturingorders().get(0).getID());
		    System.out.println(mo.getManufacturingorders().get(0).getActualProcess().getPlannedEndLatest());
		    System.out.println(((ActualProcessStep)mo.getManufacturingorders().get(0).getActualProcess().getActualProcessSteps().get(0)).getID());
		    System.out.println(((ActualProcessStep)mo.getManufacturingorders().get(0).getActualProcess().getActualProcessSteps().get(0)).getPlannedStart());
			*/
		}
		catch (JAXBException e)
		{
		    e.printStackTrace();
		}	
		return null;
	}

	private static String basicAuth(String username, String password) {
	    return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
	}



	/*
	private static void printPaths(File file)
    {
        try
        {
            System.out.println("File Path = " + file.getPath());
            System.out.println("Absolute Path = " + file.getAbsolutePath());
            System.out.println("Canonical Path = " + file.getCanonicalPath());
            System.out.println("\n");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }*/
}
