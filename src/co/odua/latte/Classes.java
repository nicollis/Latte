package co.odua.latte;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Pattern;

import co.odua.latte.Objects.object;
import co.odua.latte.Variables.DATATYPE;
import co.odua.latte.Variables.var;

public class Classes {
	//list to hold class locations
	TreeMap<String, Integer> classesLabelTable;
	//list to hold classes
	ArrayList<Class> classList;
	
	//Initializes class arrays on creation
	public Classes(){
		classesLabelTable = new TreeMap<String, Integer>();
		classList = new ArrayList<Classes.Class>();
	}
	
	//class object
	public static class Class{
		String name;//classes name
		ArrayList<var> vars;//variable list for class
		ArrayList<object> objs;//objects list for class
		TreeMap<String, Integer> functions;//Functions table list
		TreeMap<String, Integer> objLabelTable;//Objects Table List
		public Class(){//Initialization of class
			this.vars = new ArrayList<var>();
			this.objs = new ArrayList<object>();
			this.functions = new TreeMap<String, Integer>();
			this.objLabelTable = new TreeMap<String, Integer>();
		}
		public Class(String name){//Initialization of class
			this.name = name;
			this.vars = new ArrayList<var>();
			this.objs = new ArrayList<object>();
			this.functions = new TreeMap<String, Integer>();
			this.objLabelTable = new TreeMap<String, Integer>();
		}
	}
	
	//function to create classes
	public void createClass(String name){
		this.classList.add((new Class(name)));
	}
	
	//creates object inside of class
	public void createObjInClass(String className, String objName){
		object tobj = new object(objName);//creates a new object
		Class c = getClass(className);//get a copy of class
		c.objs.add(tobj);//adds new object to the class
		this.classList.set(this.classList.indexOf(c), c);//Modifies class with new one
	}
	
	//Get copy of class by its name
	public Class getClass(String name){
		for(Class c : classList){
			if(c.name.equals(name))
				return c;
		}
		return null;
	}
	//override a class with one of the same name
	public void setClass(Class c){
		this.classList.set(this.classList.indexOf(c), c);
	}
	
	//get copy of object in class
	public object getObject(String className, String objName){
		Class c = getClass(className);
		for(object o : c.objs){
			if(o.name.equals(objName))
				return o;
		}
		return null;
	}
	//override a classes object with one of the same name
	public void setObject(String className, object obj){
		Class c = getClass(className);
		c.objs.set(c.objs.indexOf(obj), obj);
		this.classList.set(this.classList.indexOf(c), c);
	}
	
	//check to see if class exist
	public boolean doesExist(String name){
		if(this.classList.isEmpty())
			return false;
		for(Class c : this.classList){
			if(c.name.equals(name))
				return true;
		}
		return false;
	}
	
	//check to see if object exist in class
	public boolean objDoesExist(String className, String objName){
		Class c = getClass(className);
		for(object o : c.objs){
			if(o.name.equals(objName))
				return true;
		}
		return false;
	}
	
	//check to see if var exist in class
	public boolean varDoesExist(String className, String varName){
		Class c = getClass(className);
		if(c == null)
			return false;
		for(var v : c.vars){
			if(v.getName().equals(varName))
				return true;
		}
		return false;
	}
	
	//check to see if var in object in class exist
	public boolean varDoesExistInObject(String className, String objName, String varName){
		object o = getObject(className, objName);
		for(var v : o.varList){
			v.getName().equals(varName);
			return true;
		}
		return false;
	}
	
	//Get copy of variable from class
	public var getVarFromClass(String className, String varName){
		Class c = getClass(className);
		for(var v : c.vars){
			if(v.getName().equals(varName))
				return v;
		}
		return null;
	}
	//Get copy of variable from object in class
		public var getVarFromObjectInClass(String className, String objName, String varName){
			object o = getObject(className, objName);
			for(var v : o.varList){
				if(v.getName().equals(varName))
					return v;
			}
			return null;
		}
	
	//Change variable value in class
	public void changeVarValue(String className, String varName, Object value){
		Class c = getClass(className);//get a copy of the class
		int clssID = this.classList.indexOf(c);
		var v = getVarFromClass(className, varName);//get a copy of the var in question
		v.setValue(value);//changes variables value
		int varID = c.vars.indexOf(v);//gets the id of the var needing to be changed
		c.vars.set(varID, v);//sets the var to its new value
		this.classList.set(clssID, c);//copys the changes over to the class list
	}
	//Change variable value in class with data type
	public void changeVarValue(String className, String varName, Object value, DATATYPE dt){
		Class c = getClass(className);//get a copy of the class
		int clssID = this.classList.indexOf(c);
		var v = getVarFromClass(className, varName);//get a copy of the var in question
		v.setValue(value);//changes variables value
		v = Variables.setDataType(v, dt);//set datatype for the variable
		int varID = c.vars.indexOf(v);//gets the id of the var needing to be changed
		c.vars.set(varID, v);//sets the var to its new value
		this.classList.set(clssID, c);//copys the changes over to the class list
	}
	//Add variable value in class
	public void addVarValue(String className, String varName, Object value){
		this.createClass(className);
		Class c = getClass(className);//get a copy of the class
		int clssID = this.classList.indexOf(c);
		var v = new var();//create a new variable
		v.setName(varName);//set its name
		v.setValue(value);//and its value
		c.vars.add(v);//add variable to variable list
		this.classList.set(clssID, c);//copy's the changes over to the class list
	}
	//Add variable value in class with data type
	public void addVarValue(String className, String varName, Object value, DATATYPE dt){
		this.createClass(className);
		Class c = getClass(className);
		int clssID = this.classList.indexOf(c);
		var v = new var();//create a new variable
		v.setName(varName);//set its name
		v.setValue(value);//and its value
		v = Variables.setDataType(v, dt);//set datatype for the variable
		c.vars.add(v);//add variable to variable list
		this.classList.set(clssID, c);//copy's the changes over to the class list
	}
	
	//change value in object in class
	public void changeVarValueInObject
		(String className, String objName, var variable){
		object o = getObject(className, objName);//gets a copy of the object
		int varID = o.varList.indexOf(variable);//gets the id of the var needing to be changed
		o.varList.set(varID, variable);//sets the var to its new value
		setObject(className, o);//sets the modified object back into the class
	}
	//add variable value to object in class
	public void addVarValueInObject(String className, String objName, var variable){
		object o = getObject(className, objName);//gets a copy of the object
		o.varList.add(variable);//add variable to variable list in object
		setObject(className, o);//sets the modified object back into the class
	}
	
	//test to see if variable name matches class.variable or class.object.variable format
	public boolean isClassOrObject(String name){
		if(Pattern.matches("[\\w]+\\.[\\w]+", name))
			return true;
		if(Pattern.matches("[\\w]+\\.[\\w]+\\.[\\w]+", name))
			return true;
		return false;
	}
	//splits string up by . found i.e. v.o = [v,o]
	public String[] splitString(String string){
		return string.split("\\.");
	}

}
