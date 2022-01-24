package agentPro_Prototype_Agents;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hsmf.datatypes.PropertyValue.BooleanPropertyValue;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLCodec.CodecException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;
import jade.domain.introspection.IntrospectionOntology;


public class CommunicationAnalyzerAgent_old extends _Agent_Template {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static String [] performatives = {"(CFP","(PROPOSE", "(ACCEPT-PROPOSAL", 
			"(INFORM", "(REFUSE"};
	static List<String> historicData = new ArrayList<String>();
	
	public void setup() {
		super.setup();
		
		   getContentManager().registerOntology(JADEManagementOntology.getInstance());
		    getContentManager().registerOntology(IntrospectionOntology.getInstance());
		    getContentManager().registerOntology(FIPAManagementOntology.getInstance());
		    SLCodec codec = new SLCodec();
		    getContentManager().registerLanguage((Codec)codec, "fipa-sl0");
		    getContentManager().registerLanguage((Codec)codec, "fipa-sl1");
		    getContentManager().registerLanguage((Codec)codec, "fipa-sl2");
		    getContentManager().registerLanguage((Codec)codec, "fipa-sl");
		
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File("D:/TeamDrive/Agent.Pro/_paper/2020 ICIT FYPAC/communication.xlsx"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 																										// file
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int sheetIndex = 0;
		XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
		
		ArrayList<Integer> list = new ArrayList<>();

		
			try {
				readExcel(this, sheet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	
		public static void readExcel(CommunicationAnalyzerAgent_old myAgent, XSSFSheet sheet
				) throws FileNotFoundException, IOException {

			

			int iterator = 1; // Iterator to increase the current row by 1 in the while loop
			int maxFilledCellNumber = 1; // To save current cell number and get the maximum filled one, start value is 1
										
			Row currentRow = sheet.getRow(iterator); // Initially get the first row
			ArrayList<String> string_msg_list = new ArrayList<String>();
			ArrayList <ACLMessage>msg_list = new ArrayList<ACLMessage>();

				while (currentRow != null) {
					currentRow = sheet.getRow(iterator);
					if (currentRow != null) {
						for (int j = 0; j <= currentRow.getLastCellNum(); j++) {
							if (currentRow.getCell(j) != null) {
								currentRow.getCell(j);
								String cell_value = currentRow
										.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL).getStringCellValue();
								//cell_value.replace("(", "");
								boolean found = false;
								for(String s : performatives) {
									if(cell_value.equals(s)) {
										string_msg_list.add(cell_value);
										found = true;
										break;
									}
								}
								if(!found && !cell_value.contains(":content")) {								
										//string_msg_list.get(string_msg_list.size()-1).concat(cell_value);
										String new_string = string_msg_list.get(string_msg_list.size()-1)+cell_value;
										string_msg_list.set(string_msg_list.size()-1, new_string);		
								}
								maxFilledCellNumber++;
							}
						}
					}
					iterator++; // Go to next row
				}
				for(String s : string_msg_list) {
					 StringACLCodec stringACLCodec = new StringACLCodec();
			          String charset = null;		        
			           charset = "US-ASCII"; 
			          ACLMessage tmp = null;
					try {
						tmp = stringACLCodec.decode(s.getBytes(charset), charset);
					} catch (CodecException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					msg_list.add(tmp);
				}
				ArrayList<ArrayList<Integer>> matrix = new ArrayList<ArrayList<Integer>>();
				//ArrayList<Integer>first_coloumn = new ArrayList<Integer>();
				ArrayList<String>senders_and_receivers=new ArrayList<String>();
				//ArrayList<String>receivers = new ArrayList<String>();
				//first_coloumn.add(0);
				//matrix.add(first_coloumn);
				HashMap<String, Integer> map = new HashMap<String, Integer>();	//Key = sender value = receiver
				for (ACLMessage msg : msg_list) {
					@SuppressWarnings("unchecked")
					Iterator<AID> it = msg.getAllIntendedReceiver();
					    while(it.hasNext()) {
					    	AID receiver_aid = it.next();	
					    	String receiver = receiver_aid.getLocalName();
					    	String sender = msg.getSender().getLocalName();	
					    	String key = sender+"."+receiver;
					    	AID df = myAgent.getDefaultDF();
					    	
					    	if(!senders_and_receivers.contains(sender) && !sender.equals(df.getLocalName())) {
					    		senders_and_receivers.add(sender);
					    	}
							if(!senders_and_receivers.contains(receiver)&& !receiver.equals(df.getLocalName()) && !receiver.equals("InterfaceAgent") ) {
								senders_and_receivers.add(receiver);
							}
							if(!map.containsKey(key)) {
								map.put(key, 1);
							}
					    }			
				}
				for(int i = 0; i<senders_and_receivers.size(); i++) {
					ArrayList<Integer>next_coloumn = new ArrayList<Integer>();
					matrix.add(next_coloumn);				
				}
				for(int i = 0; i<senders_and_receivers.size(); i++) {
					for(int j = 0; j<senders_and_receivers.size(); j++) {
						String key = senders_and_receivers.get(i)+"."+senders_and_receivers.get(j);
						if(map.containsKey(key)) {
							matrix.get(i).add(j, 1);
						}else {
							matrix.get(i).add(j, 0);
						}
					}
				}
				System.out.println(matrix.toString());
		}
	
}
