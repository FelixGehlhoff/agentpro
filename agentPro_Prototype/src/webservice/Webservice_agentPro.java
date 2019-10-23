package webservice;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.json.JSONArray;
import org.json.JSONObject;


public class Webservice_agentPro {
	
	private final static String USER_AGENT = "Mozilla/5.0";
	private static String soap_getById = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\r\n" + 
			"   <soapenv:Header/>\r\n" + 
			"   <soapenv:Body>\r\n" + 
			"      <tem:GetManufacturingOdersById>\r\n" + 
			"         <tem:serviceId>9A79D9D5-9270-42CA-BFCB-7D34D88937D2</tem:serviceId>\r\n" + 
			"         <tem:manufacturingOrders>&lt;?xml version=\"1.0\" encoding=\"utf-16\"?&gt;&#xD;\r\n" + 
			"         	&lt;ManufacturingOrderList xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"&gt;&#xD;\r\n" + 
			"  &lt;ManufacturingOrders&gt;&#xD;\r\n" + 
			"    &lt;ManufacturingOrder&gt;&#xD;\r\n" + 
			"      &lt;ID&gt;9&lt;/ID&gt;&#xD;\r\n" + 
			"        &lt;/ManufacturingOrder&gt;&#xD;\r\n" + 
			"         &lt;/ManufacturingOrders&gt;&#xD;\r\n" + 
			"         &lt;/ManufacturingOrderList&gt;\r\n" + 
			"		 </tem:manufacturingOrders>\r\n" + 
			"      </tem:GetManufacturingOdersById>\r\n" + 
			"   </soapenv:Body>\r\n" + 
			"</soapenv:Envelope>";
	
	private static String soap_getAll = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\r\n" + 
     		"   <soapenv:Header/>\r\n" + 
     		"   <soapenv:Body>\r\n" + 
     		"      <tem:GetAllManufacturingOrders>\r\n" + 
     		"         <tem:serviceId>9A79D9D5-9270-42CA-BFCB-7D34D88937D2</tem:serviceId>\r\n" + 
     		"      </tem:GetAllManufacturingOrders>\r\n" + 
     		"   </soapenv:Body>\r\n" + 
     		"</soapenv:Envelope>\r\n" + 
     		"";
	private static String soap_updateOrder = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\r\n" + 
			"   <soapenv:Header/>\r\n" + 
			"   <soapenv:Body>\r\n" + 
			"      <tem:UpdateManufacturingOrder>\r\n" + 
			"         <tem:serviceId>9A79D9D5-9270-42CA-BFCB-7D34D88937D2</tem:serviceId>\r\n" + 
			"         <tem:updatedManufacturingOrder>&lt;ManufacturingOrderList xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"&gt;&#xD;\r\n" + 
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
			"&lt;/ManufacturingOrderList&gt;\r\n" + 
			"		 </tem:updatedManufacturingOrder>\r\n" + 
			"      </tem:UpdateManufacturingOrder>\r\n" + 
			"   </soapenv:Body>\r\n" + 
			"</soapenv:Envelope>";
	
	private static String soapAction_getAll = "http://tempuri.org/IWebService/GetAllManufacturingOrders";
	private static String soapAction_getById = "http://tempuri.org/IWebService/GetManufacturingOdersById";
	private static String soapAction_updateOrder = "http://tempuri.org/IWebService/UpdateManufacturingOrder";
	private static String password_getAll = "1xIZPL6f0ejppjp0OPBR";

	public static void main(String[] args) throws Exception {
		
		HttpClient client = HttpClient.newBuilder()	
				.version(HttpClient.Version.HTTP_1_1)
				
				  .build();
		  // HttpRequest request = HttpRequest.newBuilder()
		    //     .uri(URI.create("http://foo.com/"))
		      //   .build();

		HttpRequest request = HttpRequest.newBuilder()
			
			     //.uri(URI.create("http://openjdk.java.net/"))
			     .uri(URI.create("https://move.a-t-solution.de/Move_AgentPro/Webservice/WebService.svc"))
			     .header("Authorization", basicAuth("Move_AgentPro", password_getAll))
			     //.header("serviceId", "9A79D9D5-9270-42CA-BFCB-7D34D88937D2")
			     .header("Content-Type", "text/xml")
			     .header("SOAPAction", soapAction_getAll)
			     .timeout(Duration.ofMinutes(1))

			     .POST(BodyPublishers.ofString(soap_getAll))
			     //.GET()
			     .build();
		
		//HttpResponse<String> response = client.send(request, BodyHandlers.ofString()); //send uses blocking mode --> synchronous

		//System.out.println("Response status code: " + response.statusCode());
		//System.out.println("Response headers: " + response.headers());
		//System.out.println("Response body: " + response.body());
		
		
		
		//client.sendAsync(request, BodyHandlers.ofString())
		
		
		//client.sendAsync(request, BodyHandlers.ofString())

		
      //.thenApply(HttpResponse::body)
      //.thenAccept(System.out::println)
      //.join(); 


		String a = client.sendAsync(request, BodyHandlers.ofString())
		.thenApply(HttpResponse::body).get();
		//System.out.println(a+" QQQQQQQQQQQQQQQQQQQQQQQQQQ");
	
		
        System.out.println("Access file using absolute path: ");
        String absolutePath = "C://Users/Gehlhoff/eclipse-workspace/testing/orders.xml";
        File file = new File(absolutePath);
        printPaths(file);
        System.out.println(file.getName());
		
		client.sendAsync(request, BodyHandlers.ofFile(Paths.get("C://Users/Gehlhoff/eclipse-workspace/testing/orders.xml")))
        .thenApply(HttpResponse::body)
        .get(); 
		//client.sendAsync(request, BodyHandlers.ofFile(Paths.get("webservice_Get.json")))

		
		
		tryJaxb(a);
		
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
		
		/*
		
		Map<String, String> parameters = new HashMap<>();		
		parameters.put("Username", "Move_AgentPro");
		parameters.put("Password", "1xIZPL6f0ejppjp0OPBR");
		ParameterStringBuilder.getParamsString(parameters);
		
		String url_endpoint = "https://move.a-t-solution.de/Move_AgentPro/Webservice/WebService.svc?";
		
		//URL url = new URL(url_endpoint+ParameterStringBuilder.getParamsString(parameters));
		URL url = new URL("https://move.a-t-solution.de/Move_AgentPro/Webservice/WebService.svc");
		HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/xml");
		con.setRequestProperty("User-Agent", USER_AGENT);
		//con.setRequestProperty("serviceId", "9A79D9D5-9270-42CA-BFCB-7D34D88937D2");
		con.setRequestProperty("serviceId", "9A79D9D5-9270-42CA-BFCB-7D34D88937D2");

		//con.setConnectTimeout(8000);
		//con.setReadTimeout(8000);
			
		/*
		con.setDoOutput(true);
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		
		out.writeBytes(ParameterStringBuilder.getParamsString(parameters));	//param1=value&param2=value
		out.flush();
		out.close();
		System.out.println(ParameterStringBuilder.getParamsString(parameters));
		
		
		//get response
		int status = con.getResponseCode();
		System.out.println("Sent GET to "+url_endpoint+ParameterStringBuilder.getParamsString(parameters)+" status "+status);
		
		
		BufferedReader in = new BufferedReader(
				  new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
				    content.append(inputLine);
				}
				in.close();
				
				System.out.println(content.toString());
				JSONArray array = new JSONArray(content.toString());
				for(int i = 0; i<array.length();i++	) {
					JSONObject obj = (JSONObject)array.get(i);
					System.out.println("text = "+obj.getString("text"));
				}
				//JSONObject myResponse = new JSONObject(content.toString());
				
				
*/

	
			//sendPost();

	}

	private static void tryJaxb(String a) {
		
		String b = a.replace("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><GetAllManufacturingOrdersResponse xmlns=\"http://tempuri.org/\"><GetAllManufacturingOrdersResult>", "");
		b = b.replace("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><GetManufacturingOdersByIdResponse xmlns=\"http://tempuri.org/\"><GetManufacturingOdersByIdResult>", "");
		String b1 = b.replace("&lt;?xml version=\"1.0\" encoding=\"utf-16\"?&gt;&#xD;", "");
		String c = b1.replace("&lt;", "<");
		String d = c.replace("&gt;", ">");
		String f = d.replace("&#xD;", "");
		String g = f.replace("</GetAllManufacturingOrdersResult></GetAllManufacturingOrdersResponse></s:Body></s:Envelope>", "");
		g = g.replace("</GetManufacturingOdersByIdResult></GetManufacturingOdersByIdResponse></s:Body></s:Envelope>", "");
		//System.out.println(f.subSequence(0, 350));
		System.out.println("start printout \n"+g);
		//System.out.println(a.contains("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><GetAllManufacturingOrdersResponse xmlns=\"http://tempuri.org/\"><GetAllManufacturingOrdersResult>"));
		JAXBContext jaxbContext;
		try
		{
		    jaxbContext = JAXBContext.newInstance(ManufacturingOrderList.class);             
		 
		    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		    Marshaller marshaller = jaxbContext.createMarshaller();
		 
		    ManufacturingOrderList mo = (ManufacturingOrderList) jaxbUnmarshaller.unmarshal(new StringReader(g));
		    marshaller.marshal(mo, new File("not_edited.xml"));
		     
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
		
		
		
	}

	private static String basicAuth(String username, String password) {
	    return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
	}

	private static void sendPost() throws Exception {

		String url = "https://selfsolve.apple.com/wcResults.do";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
		
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		//print result
		System.out.println(response.toString());

	}
	public static void sendGet() throws IOException {
		String url2 = "http://www.google.com/search?q=mkyong";
		
		URL obj = new URL(url2);
		HttpURLConnection con2 = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con2.setRequestMethod("GET");

		//add request header
		con2.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con2.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url2);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con2.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());
	}
	
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
    }
}
