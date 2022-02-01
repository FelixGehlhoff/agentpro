package support_classes;

import agentPro_Prototype_Agents._Agent_Template;

public interface DatabaseValues {
	//Table names etc.
		  String dbaddress_sim = "jdbc:mysql://localhost:3306/MySQL?"+"user=root&password=SQL_0518&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false";	//Address od database   serverTimezone=UTC
		  String columnNameChangeOfState = "changeOfState";
		  String columnNameChangedState = "ChangedState";
		  String columnNameOfOperation = "Operation"; //Operation
		  String columnNameOfResource = "Resource";
		  String columnNameOfResource_ID = "Resource_ID";
		 String columnNameOfSetupTime = "SetupTime";
		  String columnNameOfPlanStart = "PlanStart";
		  String columnNameOfPlanEnd = "PlanEnde";
		 String columnNameOfIstStart = "IstStart";
		 String columnNameOfIstEnd = "IstEnde";
		 String columnNameOfDueDate = "dueDate";
		 String columnNameOfReleaseDate = "releaseDate";
		 String columnNameOfPriority = "priority";
		 String columnNameOfNumber = "number";
		 String columnNameOfProduct = "product";
		 String columnNameOfTargetWarehouse = "targetWarehouse";
		  String columnNameAuftrags_ID = "Auftrags_ID"; //Auftrags_ID
		  String columnNameOperation_Type = "Operation_Type";
		  String columnNameOfStarted = "Started";
		  String columnNameOfFinished = "Finished";
		  String prefix_schema = "agentpro";
		//  String prefix_schema = "flexsimdata";
		//  String nameOfMES_Data_Resource = prefix_schema+".total_operations_my";
		  String nameOfMES_Data_Resource = prefix_schema+".transporte";
		 String nameOfMES_Data = prefix_schema+".productionplan_new";
		  String nameOfOrderbook = prefix_schema+".orderbook";
		//private String columnNameFinished = "Finished";
		//private String columnNameOfIstStart = "IstStart";
		//private String columnNameOfIstEnde = "IstEnde";
		 String columnNameStartSimulation = "PlanStart_Simulation";
		 String columnNameEndSimulation = "PlanEnd_Simulation";
		 String columnNameResourceName_simulation = "Bezeichnung";
		 String columnNameErrorType = "Error_Type";
		 String columnNameError_Occur_Time = "Error_Occur_Time";
		 String columnNameID = "ID";
		 String columnNameRunning = "Status";//"On(1)/Off(0)";
		 String columnNameResourceType = "resource_type";
		 String columnNameResourceDetailedType = "resource_detailed_type";
		 String columNameStartIst = "StartIst";		//number is missing, e.g. StartIst1
		 String columNameGestartet = "Gestartet?";
		 String columNameEndeIst = "EndeIst";
		 String columNameBeendet = "Beendet?";
		 String columnNameStartSoll = "StartSoll";
		 String columnNameEndeSoll = "EndeSoll";
		 String tableNameBetriebskalender = prefix_schema+".betriebskalender";
		 String tableNameResource = prefix_schema+".resources";
		// String tableNameResource = prefix_schema+".resources_project";
		 String tableNameProductionPlan = prefix_schema+".productionplan";
		 String tableNameResourceSetupMatrix = prefix_schema+".resources_setupmatrix";
		 String columnNameColumnInProductionPlan = "columninproductionplan";
		  String columnNameLocationX = "locationX";
		  String columnNameLocationY = "locationY";
		 String columNameColumnNameInProductionPlan = "columnnameinproductionplan";
		 String columnNameorderid = "orderid";
		 
		 String columnNameOfStep = "Step";
			String columnNameOfFirstOperation = "FirstOperation";
			String columnNameOfLastOperation = "LastOperation";
			String nameOfProduction_Plan_Def_Table = prefix_schema+".duplicate_production_plan_def";
			String columnNameOfProductName = "ProductName";
			String columnNameOfFollowUpConstraint = "hasFollowUpOperationConstraint";
			String columnNameOfWithStep = "withStep";
		
		 String columnNameOfChangeover = "prod_changeover";
		 
		  String nameOfResource_Definitions_Table = "Resource_Definitions";
			 String columnNameOfResource_Name = "Resource_Name";
			 String columnNameOfResource_Type = "Resource_Type";
			 String columnNameOfResource_Detailed_Type = "Resource_Detailed_Type";
			 String columnNameOfCapability = "Capability";
			 String columnNameOfLocationX = "LocationX";
			 String columnNameOfLocationY = "LocationY";
			 String columnNameOfAvg_Transportation_Speed = "Avg_Transportation_Speed";
			 String columnNameOfAvg_PickUp_Time = "Avg_PickUp_Time";
			
			 String nameOfCapability_Operations_Mapping_Table = _Agent_Template.prefix_schema+".capability_operations_mapping";
			 String columnNameOfID = "ID";
			 String columnNameOfCapability_Name = "Capability_Name";
			 String columnNameOfEnables_Operation = "Enables_Operation";
			 String columnNameOfOperation_Number = "Operation_Number";
			 String columnNameOfTimeConsumption = "TimeConsumption";
			 
			String columnNameOfRequiresCapability = "Requires_Capability";
}
