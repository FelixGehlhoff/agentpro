package agentPro_Prototype_Agents;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jade.core.AID;
import jade.lang.acl.ACLCodec.CodecException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;


public class CommunicationAnalyzerAgent extends _Agent_Template {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static String [] performatives = {"(CFP","(PROPOSE", "(ACCEPT-PROPOSAL", 
			"(INFORM", "(REFUSE"};
	static List<String> historicData = new ArrayList<String>();
	private static String filepath = "D:/TeamDrive/Agent.Pro/_paper/2020 ICIT FYPAC/communication.xlsx";
	private ArrayList<ArrayList<Integer>> matrix;
	private ArrayList<String> senders_and_receivers;
	
	public void setup() {
		super.setup();
		
		   //getContentManager().registerOntology(JADEManagementOntology.getInstance());
		    //getContentManager().registerOntology(IntrospectionOntology.getInstance());
		    //getContentManager().registerOntology(FIPAManagementOntology.getInstance());
		/*
		    SLCodec codec = new SLCodec();
		    getContentManager().registerLanguage((Codec)codec, "fipa-sl0");
		    getContentManager().registerLanguage((Codec)codec, "fipa-sl1");
		    getContentManager().registerLanguage((Codec)codec, "fipa-sl2");
		    getContentManager().registerLanguage((Codec)codec, "fipa-sl");
		*/
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File(filepath));
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
			
			try {
				writeExcel(this, 1, workbook, matrix, senders_and_receivers);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	
		public static void readExcel(CommunicationAnalyzerAgent myAgent, XSSFSheet sheet
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
								if(!found && !cell_value.contains(":content") && cell_value.startsWith(" :")) {								
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
				myAgent.matrix = matrix;
				myAgent.senders_and_receivers = senders_and_receivers;
				System.out.println(matrix.toString());
				System.out.println(senders_and_receivers.toString());
			
		}
		public static void writeExcel(CommunicationAnalyzerAgent myAgent, int sheetindex, XSSFWorkbook workbook,
				ArrayList<ArrayList<Integer>> matrix, 	ArrayList<String>senders_and_receivers
				) throws FileNotFoundException, IOException {
			// Werte in Excel eintragen
			
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			XSSFSheet sheet = workbook.getSheetAt(sheetindex);

			int iterator = 1; // Tterator to increase the current row by 1 in the while loop
			int maxFilledCellNumber = 1; // To save current cell number and get the maximum filled one

		//	int columnIndex = 3;
			int rowIndex = 0;
			int columnIndex = 0;
					  Row row = CellUtil.getRow(rowIndex, sheet);				   
					   
					  		for(int i = 0;i<senders_and_receivers.size();i++) {
					    		Cell cell = CellUtil.getCell(row, i+1);
					    		cell.setCellValue(senders_and_receivers.get(i));
					    		
					    	}
					    	//columnIndex = 0;
					    	for(int i = 0;i<senders_and_receivers.size();i++) {
					    		row = CellUtil.getRow(i+1, sheet);
					    		Cell cell = CellUtil.getCell(row, columnIndex);
					    		cell.setCellValue(senders_and_receivers.get(i));					    		
					    	}
					    	rowIndex = 1;
					    	columnIndex = 1;
					 
					    	
					    	for(ArrayList<Integer>line : matrix) {
					    		row = CellUtil.getRow(rowIndex, sheet);
					    		for(int i = 0;i<line.size();i++) {
					    			Cell cell = CellUtil.getCell(row, i+1);
						    		cell.setCellValue(line.get(i));
					    		}
					    		rowIndex++;
					    	}
					    
					    
				
			  
			
			
			/*
			Row row = sheet.getRow(iterator);
			int colomn_iterator = 0;

			Cell cell = row.getCell(colomn_iterator,
					Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

			while (disturbance != null) {
				disturbance = sheet.getRow(iterator);
				if (disturbance != null) {

					if (disturbance.getCell(disturbanceType) != null) {
						disturbanceDurationCell = disturbance.getCell(disturbanceType,
								Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
						// System.out.println(disturbanceDuration); // test
						maxFilledCellNumber++;
					}
				} else {
					sheet.createRow(maxFilledCellNumber).createCell(disturbanceType).setCellValue(disturbanceValue);
					Cell formulaCell = sheet.getRow(1).getCell(disturbanceType);
					evaluator.evaluateFormulaCell(formulaCell); // update formula
				}
				iterator++; // Go to next row
			}
*/
			FileOutputStream output = null;
			try {
				output = new FileOutputStream(new File(filepath));
				workbook.write(output); // write the new disturbance value into the excel file
				output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	
}
