package co.odua.latte;

import java.util.ArrayList;

public class Variables {
	private int variableCounter;
	private ArrayList<var> varList;
	
	//data types
	public static enum DATATYPE{INT, LONG, FLOAT, DOUBLE, BOOL, DYN, STRING};
	//data types in string array
	private static String datatypes[] = 
		{"INT", "LONG", "FLOAT", "DOUBLE", "BOOL", "DYN", "STRING"};
	//Test to see if the string is a data type
	public static boolean isDataType(String token)
	{
		for(String str : datatypes){
			if(str.equalsIgnoreCase(token))
				return true;
		}
		return false;
	}
	
	public static class var {
		private String name;
		private Object value;
		private int id;
		private boolean isStaticType = false;
		private DATATYPE datatype;
		
		public String getName(){return this.name;}
		public Object getValue(){return this.value;}
		public void setValue(Object value){this.value = value;}
		public void setName(String name){this.name = name;}
	}
	
	public static class createVar {
		public String name;
		public Object value;
		public createVar(String VarName, Object Value) {
			name = VarName; value = Value;
		}
	}
	
	//returns variable instead of adding it to the list
	public static var newVariable(String nameValue, Object varValue){
		var tempVar = new var();
		tempVar.name = nameValue;
		if(varValue != null)
			tempVar.value = manageVariableFootprint(varValue);
		else
			tempVar.value = null;
		return tempVar;
	}
	//returns variable instead of adding it to the list
	public static var newVariable(String nameValue, Object varValue, DATATYPE dt){
		var tempVar = new var();
		tempVar.name = nameValue;
		if(varValue != null){
			tempVar.setValue(varValue);
			tempVar = setDataType(tempVar, dt);
		}else
			tempVar.value = null;
		return tempVar;
	}
	
	public Variables(){
		variableCounter = 0;
		varList = new ArrayList<Variables.var>();
	}
	
	public var getVariable(int id){
		var v = new var();
		for(var x : this.varList){
			if(x.id == id){
				return x;
			}
		}
		return v;
	}
	
	public Object getVariableValue(String VariableName){
		return varList.get(this.getVariableId(VariableName)).value;
	}
	
	public int getVariableCounter() {
		return variableCounter;
	}
	
	private void increaseCounter(){
		variableCounter++;
	}
	//Add dynamic variable to var list
	public void addVarable(String nameValue, Object varValue){
		var tempVar = new var();
		tempVar.name = nameValue;
		if(varValue != null)
			tempVar.value = manageVariableFootprint(varValue);
		else
			tempVar.value = null;
		tempVar.id = this.getVariableCounter();
		tempVar.isStaticType = false;
		this.varList.add(tempVar);
		this.increaseCounter();
	}
	//returns variable instead of adding it to the list
	public var createVariable(String nameValue, Object varValue){
		var tempVar = new var();
		tempVar.name = nameValue;
		if(varValue != null)
			tempVar.value = manageVariableFootprint(varValue);
		else
			tempVar.value = null;
		tempVar.id = this.getVariableCounter();
		tempVar.isStaticType = false;
		return tempVar;
	}
	//Add variable with datatype to var list
	public void addVarable(String nameValue, Object varValue, DATATYPE dt){
		var tempVar = new var();
		tempVar.id = this.getVariableCounter();
		tempVar.name = nameValue;
		//type case the Number object to data type selected
		if(varValue != null){
			switch(dt){
			case INT:
				tempVar.value = ((Number) varValue).intValue();
				tempVar.isStaticType = true;
				break;
			case LONG:
				tempVar.value = ((Number) varValue).longValue();
				tempVar.isStaticType = true;
				break;
			case FLOAT:
				tempVar.value = ((Number) varValue).floatValue();
				tempVar.isStaticType = true;
				break;
			case DOUBLE:
				tempVar.value = ((Number) varValue).doubleValue();
				tempVar.isStaticType = true;
				break;
			case BOOL:
				if(((Number) varValue).byteValue() != 0)
					tempVar.value = (Boolean) true;
				else
					tempVar.value = (Boolean) false;
				tempVar.isStaticType = true;
				break;
			case DYN:
				tempVar.value = manageVariableFootprint(varValue);
				tempVar.isStaticType = false;
				break;
			case STRING:
				tempVar.value = ((String) varValue).toString();
				tempVar.isStaticType = true;
				break;
			}
		}else{//value is null
			switch(dt){
			case INT:
				tempVar.value = (int) 0;
				tempVar.isStaticType = true;
				break;
			case LONG:
				tempVar.value = (long) 0;
				tempVar.isStaticType = true;
				break;
			case FLOAT:
				tempVar.value = (float) 0.0f;
				tempVar.isStaticType = true;
				break;
			case DOUBLE:
				tempVar.value = (double) 0.0;
				tempVar.isStaticType = true;
				break;
			case BOOL:
				tempVar.value = (boolean) false;
				tempVar.isStaticType = true;
				break;
			case DYN:
				tempVar.value = null;
				tempVar.isStaticType = false;
				break;
			case STRING:
				tempVar.value = (String) "";
				tempVar.isStaticType = true;
				break;
			}
		}
		//save the data type
		tempVar.datatype = dt;
		
		this.varList.add(tempVar);
		this.increaseCounter();
	}
	//Return variable with datatype 
		public var createVariable(String nameValue, Object varValue, DATATYPE dt){
			var tempVar = new var();
			tempVar.id = this.getVariableCounter();
			tempVar.name = nameValue;
			//type case the Number object to data type selected
			if(varValue != null){
				switch(dt){
				case INT:
					tempVar.value = ((Number) varValue).intValue();
					tempVar.isStaticType = true;
					break;
				case LONG:
					tempVar.value = ((Number) varValue).longValue();
					tempVar.isStaticType = true;
					break;
				case FLOAT:
					tempVar.value = ((Number) varValue).floatValue();
					tempVar.isStaticType = true;
					break;
				case DOUBLE:
					tempVar.value = ((Number) varValue).doubleValue();
					tempVar.isStaticType = true;
					break;
				case BOOL:
					if(((Number) varValue).byteValue() != 0)
						tempVar.value = (Boolean) true;
					else
						tempVar.value = (Boolean) false;
					tempVar.isStaticType = true;
					break;
				case DYN:
					tempVar.value = manageVariableFootprint(varValue);
					tempVar.isStaticType = false;
					break;
				case STRING:
					tempVar.value = ((String) varValue).toString();
					tempVar.isStaticType = true;
					break;
				}
			}else{//value is null
				switch(dt){
				case INT:
					tempVar.value = (int) 0;
					tempVar.isStaticType = true;
					break;
				case LONG:
					tempVar.value = (long) 0;
					tempVar.isStaticType = true;
					break;
				case FLOAT:
					tempVar.value = (float) 0.0f;
					tempVar.isStaticType = true;
					break;
				case DOUBLE:
					tempVar.value = (double) 0.0;
					tempVar.isStaticType = true;
					break;
				case BOOL:
					tempVar.value = (boolean) false;
					tempVar.isStaticType = true;
					break;
				case DYN:
					tempVar.value = null;
					tempVar.isStaticType = false;
					break;
				case STRING:
					tempVar.value = (String) "";
					tempVar.isStaticType = true;
					break;
				}
			}
			//save the data type
			tempVar.datatype = dt;
			return tempVar;
		}
	
	public void removeVarable(String nameValue){
		try{
			var tempVar = varList.get(this.getVariableId(nameValue));
			this.varList.remove(varList.indexOf(tempVar));
		}catch(Exception e){
			System.out.println("Variable "+nameValue+" not found");
		}
	}
	
	public int getVariableId(String VariableName){
		int results = 998877;
		for (var v : this.varList) {
			if(v.name.equals(VariableName)){
				results = v.id;
			}
		}
		return results;
	}
	
	//Change value and datatype of variable
	public void ChangeValue(String VariableName, Object value, DATATYPE dt){
		var tempV = getVariable(this.getVariableId(VariableName));
		if(dt == DATATYPE.DYN){
			tempV.value = manageVariableFootprint(value);
			tempV.isStaticType = false;
			tempV.datatype = DATATYPE.DYN;
		}else{
			switch(dt){
			case INT:
				tempV.value = ((Number) value).intValue();
				break;
			case LONG:
				tempV.value = ((Number) value).longValue();
				break;
			case FLOAT:
				tempV.value = ((Number) value).floatValue();
				break;
			case DOUBLE:
				tempV.value = ((Number) value).doubleValue();
				break;
			case BOOL:
				if(((Number) value).byteValue() != 0)
					tempV.value = (Boolean) true;
				else
					tempV.value = (Boolean) false;
				break;
			case STRING:
				tempV.value = ((String) value).toString();
				break;
			}
			tempV.isStaticType = true;
			tempV.datatype = dt;
		}
		this.varList.set(tempV.id, tempV);
	}
	
	//Change Value for variable
	public void ChangeValue(String VariableName, Object value){
		var tempV = getVariable(this.getVariableId(VariableName));
		if(!tempV.isStaticType)
			tempV.value = manageVariableFootprint(value);
		else{
			switch(tempV.datatype){
			case INT:
				tempV.value = ((Number) value).intValue();
				break;
			case LONG:
				tempV.value = ((Number) value).longValue();
				break;
			case FLOAT:
				tempV.value = ((Number) value).floatValue();
				break;
			case DOUBLE:
				tempV.value = ((Number) value).doubleValue();
				break;
			case BOOL:
				if(((Number) value).byteValue() != 0)
					tempV.value = (Boolean) true;
				else
					tempV.value = (Boolean) false;
				break;
			case STRING:
				tempV.value = ((String) value).toString();
				break;
			}
		}
		this.varList.set(tempV.id, tempV);
	}
	
	//Dynamically assign variables to their appropriate type
	//and insure that the smallest variable is used in the process.
	private static Object manageVariableFootprint(Object variable){
		boolean isNumber = true;
		try{
			@SuppressWarnings("unused")
			Number x = (Number) variable;
		}catch(ClassCastException e){
			isNumber = false;
		}
		//Check if variable contains letters
		if(!isNumber){
			 //If so the set type to String
			variable = (String) variable;
		}else{//Checks for a floating point
			double temp;
			if(((temp = ((Number) variable).doubleValue()) % 1) != 0){
				//if one is found finds the smallest type that will fit without rounding
				if(temp <= Float.MAX_VALUE && temp >= Float.MIN_VALUE)
					variable = (float) temp;
				else if(temp <= Double.MAX_VALUE && temp >= Double.MIN_VALUE)
					variable = (double) temp;
			}else{
				//if none is found finds the smallest type that will fit.
				if(temp <= Byte.MAX_VALUE && temp >= Byte.MIN_VALUE)
					variable = (byte) temp;
				else if(temp <= Short.MAX_VALUE && temp >= Short.MIN_VALUE)
					variable = (short) temp;
				else if(temp <= Integer.MAX_VALUE && temp >= Integer.MIN_VALUE)
					variable = (int) temp;
				else if(temp <= Long.MAX_VALUE && temp >= Long.MIN_VALUE)
					variable = (long) temp;
			}
		}
		return variable;
	}
	//public api to manageVariableFootprint
	public static Object memoryFriendly(Object obj){
		return manageVariableFootprint(obj);
	}
	
	//translates string into enum data type value
	public static DATATYPE stringToDatatype(String str){
		//TODO always returning dyn
		byte counter = 0;//counts the passes to know what string datatype it is on
		for(String s : datatypes){
			if(s.equalsIgnoreCase(str)){
				switch(counter){//int/long/float/double/bool/dyn in order
				case 0:
					return DATATYPE.INT;
				case 1:
					return DATATYPE.LONG;
				case 2:
					return DATATYPE.FLOAT;
				case 3:
					return DATATYPE.DOUBLE;
				case 4:
					return DATATYPE.BOOL;
				case 5:
					return DATATYPE.DYN;
				case 7:
					return DATATYPE.STRING;
				}
			}
			counter++;
				
		}
		return null;
	}
	
	//check if variable exist
	public boolean doesExist(String str)
	{
		for(var v : this.varList){
			if(v.name.equals(str))
				return true;
		}
		return false;
	}
	
	//return var as a static datatype instance
	public static var setDataType(var v, DATATYPE dt){
		Object value = v.value;
		if(dt == DATATYPE.DYN){
			v.value = manageVariableFootprint(value);
			v.isStaticType = false;
			v.datatype = DATATYPE.DYN;
		}else{
			switch(dt){
			case INT:
				v.value = ((Number) value).intValue();
				break;
			case LONG:
				v.value = ((Number) value).longValue();
				break;
			case FLOAT:
				v.value = ((Number) value).floatValue();
				break;
			case DOUBLE:
				v.value = ((Number) value).doubleValue();
				break;
			case BOOL:
				if(((Number) value).byteValue() != 0)
					v.value = (Boolean) true;
				else
					v.value = (Boolean) false;
				break;
			case STRING:
				v.value = ((String) value).toString();
				break;
			}
			v.isStaticType = true;
			v.datatype = dt;
		}
		return v;
	}
	
	
}
