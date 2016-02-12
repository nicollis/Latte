package co.odua.latte;

import java.util.ArrayList;
import java.util.TreeMap;

import co.odua.latte.Variables.var;

public class Objects {
	//a map for Object Labels
	protected TreeMap<String, Integer> objLabelTabel;
	//array list to hold Objects
	private ArrayList<object> objectList;
	
	//Constructor for Objects
	public Objects() {//Initialize objects
		this.objLabelTabel = new TreeMap<String, Integer>();
		this.objectList = new ArrayList<Objects.object>();
	}
	
	//base template for objects
	public static class object{
		String name;
		ArrayList<var> varList;
		public object() {
			this.varList = new ArrayList<Variables.var>();
		}
		public object(String name) {
			this.varList = new ArrayList<Variables.var>();
			this.name = name;
		}
	}
	//adds object to Object List
	public void createObject(String name)
	{
		object tempobj = new object();
		tempobj.name = name;
		this.objectList.add(tempobj);
	}
	//adds a new variable into an object from the list
	protected void addVariableToObject(String ObjectName, var Variable)
	{
		int objectId = this.objectList.indexOf(getObjectFromList(ObjectName));//gets index of object
		object tempObject = this.objectList.get(objectId);//copy's contents of object
		tempObject.varList.add(Variable);//adds variable to temporary object
		this.objectList.set(objectId, tempObject);//replace old object with new
	}
	//changes a variable in an object from the list
	protected void changeVariableInObject(String ObjectName, var Variable)
	{
		object tempObject = getObjectFromList(ObjectName);//finds and copies data from the object 
		int objectId = this.objectList.indexOf(tempObject);//gets index of object
		int varId = tempObject.varList.indexOf(getVariableFromObject(tempObject, Variable.getName()));//gets the id of variable
		tempObject.varList.set(varId, Variable);//replace the old variable with the new one
		this.objectList.set(objectId, tempObject);//replace old object with new
	}
	
	public boolean doesExist(String ObjectName){
		for(object o : this.objectList){
			if(o.name.equals(ObjectName))
				return true;
		}
		return false;
	}
	
	public boolean objVarDoesExist(String ObjectName, String VariableName){
		//find object and then see if variable exist in object
		object temp = getObjectFromList(ObjectName);
		if(!temp.varList.isEmpty())
			return doesVariableExistInObject(temp.varList, VariableName);
		return false;
	}
	
	//finds object by name and returns them for use
	private object getObjectFromList(String name){
		object tempobj = null;
		for(object o : this.objectList){
			if(name.equals(o.name)){
				tempobj = o;
			}
		}
		return tempobj;
	}
	
	private var getVariableFromObject(object obj, String name)
	{
		var tempVar = null;
		for(var v : obj.varList){
			if(v.getName().equals(name))
				tempVar = v;
		}
		return tempVar;
	}
	
	//Find variable name in a list of variables
	private boolean doesVariableExistInObject(ArrayList<var> vars, String variableName)
	{
		for(var v : vars){
			if(v.getName().equals(variableName))
					return true;
		}
		return false;
	}
	
	//returns value from an variable in an object
	public Object getVarValueFromObj(String objName, String varName)
	{
		
		for(var v : (getObjectFromList(objName)).varList){
			if(v.getName().equals(varName))
				return v.getValue();
		}
		return null;
	}
	
	
}