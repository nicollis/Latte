package co.odua.latte;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.TreeMap;

import co.odua.latte.Variables.DATATYPE;
import co.odua.latte.Variables.createVar;

public class Latte{
	private int PROG_SIZE; //Script size as read by getProgramSize()
	
	//These are the Token Types.
	private final int NONE = 0;
	private final int DELIMITER = 1;
	private final int VARIABLE = 2;
	private final int NUMBER = 3;
	private final int COMMAND = 4;
	private final int QUOTEDSTR = 5;
	private final int CONDITIONAL = 6;
	private final int DATATYPE = 7;
	private final int OBJECT = 8;
	
	//These are the types of Errors.
	private final int SYNTAX = 0;
	private final int UNBALPARENS = 1;
	private final int NOEXP = 2;
	private final int DIVBYZERO = 3;
	private final int EQUALEXPECTED = 4;
	private final int NOTVAR = 5;
	private final int DUPLABLE = 6;
	private final int UNDEFLABEL = 7;
	private final int THENEXPECTED = 8;
	private final int TILLEXPECTED = 9;
	private final int NEXTWITHOUTFOR = 10;
	private final int RETURNWITHOUTRUN = 11;
	private final int MISSINGQUOTE = 12;
	private final int FILENOTFOUND = 13;
	private final int FILEIOERROR = 14;
	private final int INPUTIOERROR = 15;
	private final int FILESIZEERROR = 16;
	private final int ENDIFEXPECTED = 17;
	private final int DEFINEWITHOUTRETURN = 18;
	private final int NOTNUMBER = 19;
	private final int TPCSTNONVAR = 20;
	private final int DUPOBJECT = 21;
	private final int OBJCLSSMIA = 22;
	private final int OBJWITHOUTENDOBJ = 23;
	
	//Internal Representation of Latte Scripts Keywords
	private final int UNKNCOM = 0;
	private final int PRINT = 1;
	private final int INPUT = 2;
	private final int IF = 3;
	private final int THEN = 4;
	private final int FOR = 5;
	private final int NEXT = 6;
	private final int TILL = 7;
	private final int GOTO = 8;
	private final int RUN = 9;
	private final int RETURN = 10;
	private final int UNTIL = 11;
	private final int DESTROY = 12;
	private final int EXIT = 13;
	private final int EOL = 14;
	private final int ENDIF = 15;
	private final int ELSE = 16;
	private final int DEFINE = 17;
	private final int AND = 18;
	private final int OR = 19;
	public static final int MATH = 20;//Public for external lib
	public static final int NEW = 21;
	private final int OBJ = 22;
	private final int ENDOBJ = 23;
	
	
	//This token indicates the end-of-program.
	private final String EOP = "\0";
	
	//Codes for double operators
	private final char LE = 1; // <=
	private final char GE = 2; // >=
	private final char NE = 3; // !=
	private final char PE = 4; // +=
	private final char ME = 5; // -=
	private final char TE = 6; // *=
	private final char DE = 7; // /=
	private final char PP = 8; // ++
	private final char MM = 9; // --
	
	//Array for values
	public static Variables vars;
	//Deceleration for objects
	public static Objects objs;
	//Deceleration for Classes
	public static Classes clss;
	
	//Class to link keywords to their tokens.
	static class Keyword {
		String keyword;
		int keywordToken;
		
		Keyword(String str, int t){
			keyword = str;
			keywordToken = t;
		}
	}
	
	//Table of keywords and their internal representation
	Keyword kwTable[] = {
		new Keyword("print", PRINT),
		new Keyword("input", INPUT),
		new Keyword("if", IF),
		new Keyword("then", THEN),
		new Keyword("goto", GOTO),
		new Keyword("for", FOR),
		new Keyword("next", NEXT),
		new Keyword("till", TILL),
		new Keyword("run", RUN),
		new Keyword("return", RETURN),
		new Keyword("until", UNTIL),
		new Keyword("destroy", DESTROY),
		new Keyword("exit", EXIT),
		new Keyword("eol", EOL),
		new Keyword("endif", ENDIF),
		new Keyword("else", ELSE),
		new Keyword("define", DEFINE),
		new Keyword("and", AND),
		new Keyword("or", OR),
		new Keyword("&&", AND),
		new Keyword("||", OR),
		new Keyword("new", NEW),
		new Keyword("object", OBJ),
		new Keyword("endobj", ENDOBJ)
	};
	
	private char[] prog; //program array
	private int progIdx; //current index into program
	private int lineCount;
	
	private String token; //holds current token
	private int tokType; //holds token type
	
	private int kwToken; //internal representation of a keyword
	private boolean untilLoopRunning; //internal check for until loop
	//boolean to keep track if we are adding data to variable or object
	private boolean isObject = false; 
	private String currentObj;
	private boolean isClass = false;
	private String currentClass;
	
	//Support for FOR loops.
	class ForInfo {
		String var; //counter variable
		int target; //target value
		int loc; // index in source code to loop to
		boolean isSmaller;//is the loop ++ or -- TODO implement in FOR loop
	}
	
	//Stack for FOR loops.
	private Stack<ForInfo> fStack;
	
	//Defines label table entries.
	class Label {
		String name; //label
		int loc; // index of label's location in source file
		public Label(String n, int i){
			name = n;
			loc = i;
		}
	}
	//a map for labels.
	private TreeMap<String,Integer> labelTable;
	
	//Stack for RUN's
	private Stack<Integer> rStack;
	
	//Relational operators
	char rops[] = {
			GE, NE, LE, '<', '>', '=', 0
	};
	
	//Conditional Expressions
	String cond[] ={
		"AND", "OR", "&&", "||", "IS", "NOT", "GREATER", "SMALLER"	
	};
	//String of relational operators.
	String relops = new String(rops);
	
	//Condensed Expressions 
	char cexpre[] = {
		PE, ME, TE, DE, PP, MM, 0	
	};
	
	//String for Condensed Expressions
	String cexpres = new String(cexpre);
	
	//Constructor for Latte.
	public Latte(String programName) throws InterpreterException{
		//Loads external keywords and join them into kwTable
		kwTable = joinKeywords(kwTable, Math.keywords);
		
		//load programs size
		getProgramSize(programName);
		char tempbuf[] = new char[PROG_SIZE];
		int size;
		
		//Loads program to execute
		size = loadProgram(tempbuf ,programName);
		
		if(size != -1){
			//create array to hold program
			prog = new char[size];
			
			//Copy the program into array
			System.arraycopy(tempbuf, 0, prog, 0, size);
		}
		//init vars so user can post to before run
		vars = new Variables();
		objs = new Objects();
		clss = new Classes();
	}
	
	//post data to script
	public void post(createVar ...params)
	{
		for(createVar v : params){
			vars.addVarable(v.name, v.value);
		}
	}
	
	//allow to get vars from lt script
	public Object get(String variableName)
	{
		return vars.getVariableValue(variableName);
	}
	
	//Gets Script file size
	private void getProgramSize(String fname) throws InterpreterException{
		PROG_SIZE = -1;
		try{
			//Gets file's length
			File file = new File(fname);
			PROG_SIZE = (int) file.length();
		}catch(Exception e){
			handleErr(FILESIZEERROR);
		}
	}
	
	//Load the program
	private int loadProgram(char[] p,String fname) throws InterpreterException{
		int size = 0;
		
		try{
			//Open file into reader
			FileReader fr = new FileReader(fname);
			//Buffer the file into a char array
			BufferedReader br = new BufferedReader(fr);
			
			size = br.read(p);
			//Close file from memory
			fr.close();
			
			//if file ends with an EOF mark, back up.
			if(p[size-1]==(char) 26) size--;
		}catch (FileNotFoundException e) {
			handleErr(FILENOTFOUND);
		}catch (IOException exc){
			handleErr(FILEIOERROR);
		}
		return size; //return size of program
	}
	
	//Execute the program
	public void run() {
		//Initialize for new program run.
		fStack = new Stack<ForInfo>();
		labelTable = new TreeMap<String,Integer>();
		rStack = new Stack<Integer>();
		progIdx = 0;
		lineCount = 0;
		untilLoopRunning = false;
		
		try{
			scanLables(); //find the labels in the program
		
			latteInterp(); // execute
		}catch(InterpreterException e){
			System.out.println("File IO Error");
		}
	}
	
	//Entry point for the Latte interpreter.
	private void latteInterp() throws InterpreterException
	{
		//This is the interpreters Main Loop
		do{
			getToken();
			//Check for assignment statement.
			if(tokType == VARIABLE || tokType == DATATYPE){
				putBack(); //return the var to the input stream
				assignment(); //handle assignment statment
			}
			else // is keyword
				switch(kwToken)	{
				case PRINT:
					print();
					break;
				case GOTO:
					execGoto();
					break;
				case IF:
					execIf();
					break;
				case FOR:
					execFor();
					break;
				case NEXT:
					next();
					break;
				case INPUT:
					input();
					break;
				case RUN:
					execRun();
					break;
				case RETURN:
					greturn();
					break;
				case EXIT:
					return;
				case DESTROY:
					destory();
					break;
				case ELSE:
					execElse();
					break;
				case DEFINE:
					define();
					break;
				case MATH:
					math();
					break;
				case OBJ:
					object();
					break;
				}
		}while (!token.equals(EOP));
	}
	
	//Find all labels
	private void scanLables() throws InterpreterException
	{
		Object results;
		
		//See if the first token in the field is a label
		getToken();
		if(token.equalsIgnoreCase("DEFINE")){
			getToken();
			labelTable.put(token, new Integer(progIdx));
		}
		
		if(token.equalsIgnoreCase("OBJECT")){
			getToken();
			objs.objLabelTabel.put(token, new Integer(progIdx));
		}
		if(token.equalsIgnoreCase("CLASS")){
			getToken();
			clss.classesLabelTable.put(token, new Integer(progIdx));
			clss.createClass(token);
			Classes.Class thisClass = clss.getClass(token);
			while(!token.equalsIgnoreCase("ENDCLASS")){
				getToken();
				if(token.equalsIgnoreCase("DEFINE")){ // adds functions to known labels
					getToken();
					results = thisClass.functions.put(token, new Integer(progIdx));
					
					if(results != null)
						handleErr(DUPLABLE);
				}
				if(token.equalsIgnoreCase("OBJECT")){ // adds objects to known objects
					getToken();
					results = thisClass.objLabelTable.put(token, new Integer(progIdx));
					
					if(results != null)
						handleErr(DUPOBJECT);
				}
			}
			//save instance of class to class array
			clss.setClass(thisClass);
		}
		
		findEOL();
		
		do{
			getToken();
			if(token.equalsIgnoreCase("DEFINE")){ // adds functions to known labels
				getToken();
				results = labelTable.put(token, new Integer(progIdx));
				
				if(results != null)
					handleErr(DUPLABLE);
			}
			if(token.equalsIgnoreCase("OBJECT")){ // adds objects to known objects
				getToken();
				results = objs.objLabelTabel.put(token, new Integer(progIdx));
				
				if(results != null)
					handleErr(DUPOBJECT);
			}
			if(token.equalsIgnoreCase("CLASS")){
				getToken();
				clss.classesLabelTable.put(token, new Integer(progIdx));
				clss.createClass(token);
				Classes.Class thisClass = clss.getClass(token);
				while(!token.equalsIgnoreCase("ENDCLASS")){
					getToken();
					if(token.equalsIgnoreCase("DEFINE")){ // adds functions to known labels
						getToken();
						results = thisClass.functions.put(token, new Integer(progIdx));
						
						if(results != null)
							handleErr(DUPLABLE);
					}
					if(token.equalsIgnoreCase("OBJECT")){ // adds objects to known objects
						getToken();
						results = thisClass.objLabelTable.put(token, new Integer(progIdx));
						
						if(results != null)
							handleErr(DUPOBJECT);
					}
				}
				//save instance of class to class array
				clss.setClass(thisClass);
			}
			//if not on a blank line, find next line.
			if(kwToken != EOL) findEOL();
		}while (!token.equals(EOP));
		progIdx = 0; //reset index to start program
	}
	
	//Find the start of the next line.
	private void findEOL(){
		while(progIdx < prog.length &&
				prog[progIdx] != '\n') ++progIdx;
		if(progIdx < prog.length) progIdx++;
	}
	
	//Assign a variable to a value
	private void assignment() throws InterpreterException
	{
		Object value = null;
		String vname = "";
		DATATYPE dt = null;
		int tempTokType = 0;
		boolean tempObject = false;
		boolean tempClass = false;
		
		//Get the variable name.
		getToken();
		//test to see if token is data type
		if(!Variables.isDataType(token))
			vname = token;
		else{//is datatype
			dt = Variables.stringToDatatype(token);
			tempTokType = tokType;
			getToken();
			vname = token;
		}
		
		if(!Character.isLetter(vname.charAt(0))){
			handleErr(NOTVAR);
			return;
		}
		
		//test to see if it is a objects variable
		if(clss.isClassOrObject(vname)){//test to see if it goes c.v or c.o.v
			String tempStr[] = clss.splitString(vname);//Splits string into array
			if(clss.doesExist(tempStr[0])){//if we are talking about a class
				isClass = true;
				tempClass = true;
				currentClass = tempStr[0];
				if(tempStr.length == 3){//if x.y.x then class has object
					isObject = true;
					tempObject = true;
					currentObj = tempStr[1];
					vname = tempStr[2];
				}else{
					vname = tempStr[1];
				}
			}else{//is object
				isObject = true;
				tempObject = true;
				
				currentObj = tempStr[0];
				vname = tempStr[1];
			}
		}
		
		//Get the equal sign. 
		getToken();
		boolean isCompactExpression = isCondExpes(token.charAt(0));
		if(!token.equals("\n")){//finds if variable dosn't have value
			if(!token.equals("=")){
				if(!isCompactExpression){
					handleErr(EQUALEXPECTED);
					return;
				}
			}
			//Get the value of assigned.
			if(!isCompactExpression)
				value = evaluate();
			else
				value = condensedExp(vname);
		}
		
		if(token.equalsIgnoreCase("NEW")){//handles the creation of new classes
			getToken();
			if(clss.doesExist(token)){//TODO working NEEDS LOTS OF DEBUGGING
				currentClass = vname;
				isClass = true;
				execNewClass();
				isClass = false;
				return;
			}else if(objs.doesExist(token) && !isClass){
				objs.createObject(vname);
				currentObj = vname;
				isObject = true;
				execNewObject();
				isObject = false;
				return;
			}else if(clss.objDoesExist(currentClass, token) && isClass){
				clss.createObjInClass(currentClass, vname);
				currentObj = vname;
				isObject = true;
				execNewObject();
				isObject = false;
				return;
			}else{
				handleErr(OBJCLSSMIA);
				return;
			}
		}
		
		if(!isClass){
			//if we are adding to variable and not object
			if(!isObject){
				//check if value exist
				if(vars.doesExist(vname)){
					if(tempTokType != DATATYPE)//var is not type cast
						vars.ChangeValue(vname, value);
					else
						vars.ChangeValue(vname, value, dt);
				}else{
					//Assign the value.
					if(dt == null)
						vars.addVarable(vname, value);
					else
						if(tempTokType != DATATYPE)
							vars.addVarable(vname, value, dt);
						else
							handleErr(TPCSTNONVAR);
				}
			}else{//is object
				//check if value exist in object
				if(objs.objVarDoesExist(currentObj, vname)){
					if(tempTokType != DATATYPE)//var is not type cast
						objs.changeVariableInObject(currentObj, vars.createVariable(vname, value));
					else
						objs.changeVariableInObject(currentObj, vars.createVariable(vname, value, dt));
				}else{
					//Assign the value.
					if(dt == null)
						objs.addVariableToObject(currentObj, vars.createVariable(vname, value));
					else
						if(tempTokType != DATATYPE)
							objs.addVariableToObject(currentObj, vars.createVariable(vname, value, dt));
						else
							handleErr(TPCSTNONVAR);
				}
			}
		}else{//is class
			//if we are adding to variable and not object
			if(!isObject){
				//check if value exist
				if(clss.varDoesExist(currentClass, vname)){
					if(tempTokType != DATATYPE)//var is not type cast
						clss.changeVarValue(currentClass, vname, value);
					else
						clss.changeVarValue(currentClass, vname, value, dt);
				}else{
					//Assign the value.
					if(dt == null)
						clss.addVarValue(currentClass, vname, value);
					else
						if(tempTokType != DATATYPE)
							clss.addVarValue(currentClass, vname, value, dt);
						else
							handleErr(TPCSTNONVAR);
				}
			}else{//is object
				//check if value exist in object
				if(clss.varDoesExistInObject(currentClass, currentObj, vname)){
					if(tempTokType != DATATYPE)//var is not type cast
						clss.changeVarValueInObject(currentClass, currentObj, 
								Variables.newVariable(vname, value));
					else
						clss.changeVarValueInObject(currentClass, currentObj, 
								Variables.newVariable(vname, value, dt));
				}else{
					//Assign the value.
					if(dt == null)
						clss.addVarValueInObject(currentClass, currentObj, 
								Variables.newVariable(vname, value));
					else
						if(tempTokType != DATATYPE)
							clss.addVarValueInObject(currentClass, currentObj, 
									Variables.newVariable(vname, value, dt));
						else
							handleErr(TPCSTNONVAR);
				}
			}
		}
		
		if(tempObject){
			isObject = false;
			tempObject = false;
		}
		if(tempClass){
			isClass = false;
			tempClass = false;
		}
	}
	
//****************************Keyword Functions***********************
	//Execute a simple version of the PRINT statement.
	private void print() throws InterpreterException
	{
		int len=0, spaces;
		String lastDelim = "";
		
		do{
			getToken(); //get next list item
			if(kwToken==EOL || token.equals(EOP)) break;
			
			if(tokType==QUOTEDSTR){//is string
				System.out.print(token);
				len += token.length();
				getToken();
			}
			else{//is expression
				putBack();
				Object result = evaluate();
				getToken();
				System.out.print(result);
				
				//Add length of output to running total.
				if(result != null)
					len += result.toString().length(); //save length
			}
			lastDelim = token;
			
			//If comma, move to next tab stop.
			if(lastDelim.equals(",")){
				//compute number of spaces to move to next tab
				spaces = 8 - (len % 8);
				len += spaces; //add in the tabbing position
				while(spaces != 0){
					System.out.print(" ");
					spaces--;
				}
			}
			else if(token.equals(";")){
				System.out.print(" ");
				len++;
			}
			else if(kwToken != EOL && !token.equals(EOP))
				handleErr(SYNTAX);
		}while(lastDelim.equals(";") || lastDelim.equals(","));
		
		if(kwToken==EOL || token.equals(EOP)){
			if(!lastDelim.equals(";") && !lastDelim.equals(","))
				System.out.println();
		}
		else handleErr(SYNTAX);
	}
	
	//Execute the GOTO statement
	private void execGoto() throws InterpreterException
	{
		Integer loc;
		
		getToken(); //get label to go to
		
		//Find the location of the label.
		loc = (Integer) labelTable.get(token);
		
		if(loc == null)
			handleErr(UNDEFLABEL);//label is not defined
		else //Start program running at that loc
			progIdx  = loc.intValue();
	}
	
	//Execute the IF statement
	private void execIf() throws InterpreterException
	{
		byte result = 0;
		
		//Runs though logic finding AND/OR/&&/|| 
		// and Returning when THEN is hit
		try{
			result = ((Number) evaluate()).byteValue();
		}catch(ClassCastException e){
			handleErr(SYNTAX);
		}
			
		/* if the result if true(non-zero)
		 * process target of IF. Otherwise move on
		 to next line in the program.*/
		if(result != 0){
			getToken();
			if(kwToken != THEN){
				handleErr(THENEXPECTED);
				return;
			}//else, target statment will be executed
		}
		else findElse(); //find the next line
		
		//Skip white space looking for new token
		getToken();
		if(kwToken == EOL){
			getToken();
		}
		putBack();
	}
	//Find ELSE statement for if
	private void findElse() throws InterpreterException{
		while(progIdx < prog.length && kwToken != ELSE){
				getToken();
				if(kwToken == ENDIF)
					break;
				if(progIdx >= prog.length){
					handleErr(ENDIFEXPECTED);
				}
			}
	}
	
	//if statement was true skip over else statement
	private void execElse() throws InterpreterException{
		findEndif();
	}
	
	//Find end of if statement
	private void findEndif() throws InterpreterException{
		while(progIdx < prog.length &&
				kwToken != ENDIF){
				getToken();
				if(progIdx >= prog.length){
					handleErr(ENDIFEXPECTED);
				}
		}
	}
	
	//Execute the FOR statement
	private void execFor() throws InterpreterException
	{
		ForInfo stckvar = new ForInfo();
		Number value;
		String vname;
		
		getToken();//get control variable
		vname = token;
		if(!Character.isLetter(vname.charAt(0))){
			handleErr(NOTVAR);
			return;
		}
		
		//Save index of control variable.
		stckvar.var = vname;
		
		getToken(); //read the equal sign
		if(token.charAt(0) != '='){
			putBack();
			value = (Number) vars.getVariableValue(vname);
		}else{
			value = (Number) evaluate();//get initial value
			vars.addVarable(vname, value);
		}
		
		getToken();//read and discard the TILL
		if(kwToken != TILL) handleErr(TILLEXPECTED);
		
		stckvar.target = ((Number) evaluate()).intValue(); // get target value
		
		/* If loop can execute at least once,
		 * push info on stack.*/
		if(value.intValue() >= ((Number) vars.getVariableValue(vname)).intValue()){
			stckvar.loc = progIdx;
			fStack.push(stckvar);
		}
		else //otherwise, skip loop code altogether
			while(kwToken != NEXT) getToken();
	}
	
	//Execute NEXT statement
	private void next() throws InterpreterException
	{
		ForInfo stckvar;
		
		try{
			//Retrieve info for this FOR loop.
			stckvar = (ForInfo) fStack.pop();
			Math.addToVariable(stckvar.var, 1); //increment control var
			
			//If done, return.
			if(((Number) vars.getVariableValue(stckvar.var)).intValue() 
					> stckvar.target) return;
			
			//Otherwise, restore the info.
			fStack.push(stckvar);
			progIdx = stckvar.loc; //loop
		}catch(EmptyStackException e){
			handleErr(NEXTWITHOUTFOR);
		}
	}
	
	//Execute a Simple form of input.
	private void input() throws InterpreterException
	{
		String var;
		double val = 0.0;
		String str;
		
		BufferedReader br = new
				BufferedReader(new InputStreamReader(System.in));
		
		getToken(); //see if prompt string is present
		if(tokType == QUOTEDSTR){
			//if so, print it and check for comma
			System.out.print(token);
			getToken();
			if(!token.equals(",")) handleErr(SYNTAX);
			getToken();
		}
		else System.out.print("? ");//otherwise, prompt with ?
		
		//get the input var
		var = token;
		
		try{
			str = br.readLine();
			val = Double.parseDouble(str); //read the variable
		}catch (IOException e){
			handleErr(INPUTIOERROR);
		}catch (NumberFormatException e){
			System.out.println("Invalid input.");
		}
		Math.addToVariable(var, val);//store it.
	}
	
	//Execute RUN Statement
	private void execRun() throws InterpreterException
	{
		Integer loc;
		
		getToken();
		
		//find the label to call.
		loc = (Integer) labelTable.get(token);
		
		if(loc == null)
			handleErr(UNDEFLABEL);
		else{
			//test to see if until is called
			getToken();
			
			if(token.toUpperCase().equals("UNTIL")){
				until(loc.intValue());
			}else{
				putBack();
				
				//save place to return to.
				rStack.push(new Integer(progIdx));
				
				//start program running at that loc.
				progIdx = loc.intValue();
			}
		}
	}
	//Execute RUN UNTIL condition is done
	private void until(int functionLocation) throws InterpreterException
	{
		ForInfo stckinfo = new ForInfo();
		String vname;
		int value;
		stckinfo.loc = functionLocation;
		
		//get variable to be tested
		getToken();
		vname = token;
		if(!Character.isLetter(vname.charAt(0))){
			handleErr(NOTVAR);
			return;
		}
		stckinfo.var = vname;
		value = ((Number) vars.getVariableValue(vname)).intValue();
		
		//get and pass equal sign
		getToken();
		if(token.charAt(0) != '=')
			handleErr(EQUALEXPECTED);
		
		//get the target value
		stckinfo.target = ((Number) evaluate()).intValue();
		
		//add stckinfo to fStack so it can be found on greturn
		fStack.push(stckinfo);
		
		//activate until loop
		untilLoopRunning = true;
		
		//Test to see if loop will get bigger or smaller
		if(value > stckinfo.target)
			stckinfo.isSmaller = false;
		else if(value <= stckinfo.target)
			stckinfo.isSmaller = true;
		
		//save place to come back too
		rStack.push(new Integer(progIdx));
		
		//jump to function location
		progIdx = stckinfo.loc;
		
	}
	
	//Return from RUN
	private void greturn() throws InterpreterException
	{
		if(untilLoopRunning){
			//get until conditions
			ForInfo stckinfo = fStack.pop();
			
			//get variables current value
			int value = ((Number) vars.getVariableValue(stckinfo.var)).intValue();

			//check if loop is done
			if(!stckinfo.isSmaller && value <= stckinfo.target)
				untilLoopRunning = false;
			else if(stckinfo.isSmaller && value >= stckinfo.target)
				untilLoopRunning = false;
			else{//needs to run loop again
				fStack.add(stckinfo);
				progIdx = stckinfo.loc;
			}
		}
		
		//If until loop is not running return out of loop
		if(!untilLoopRunning){
			try{
				//Restore to program index
				Integer t = (Integer) rStack.pop();
				progIdx = t.intValue();
			}catch(EmptyStackException e){
				handleErr(RETURNWITHOUTRUN);
			}
		}
	}
	
	//Execute Destory statement
	private void destory() throws InterpreterException
	{
		//get variable name to distroy
		getToken();
		if(tokType == VARIABLE)
			vars.removeVarable(token);
		else
			handleErr(NOTVAR);
	}
	
	//Skip over Define if found in script after scan labels has finished
	private void define() throws InterpreterException
	{
		while(progIdx < prog.length &&
				kwToken != RETURN){
				getToken();
				if(progIdx >= prog.length || kwToken == EXIT || kwToken == DEFINE){
					handleErr(DEFINEWITHOUTRETURN);
				}
		}
	}
	
	//Skip over Objects if found in script after scan labels has finished
	private void object() throws InterpreterException
	{
		while(progIdx < prog.length &&
				kwToken != ENDOBJ){
				getToken();
				if(progIdx >= prog.length || kwToken == EXIT || tokType == COMMAND){
					handleErr(OBJWITHOUTENDOBJ);
				}
		}
	}
	
	//if a keyword is MATH find conditions and run in external math.java
	private void math() throws InterpreterException
	{
		String op = token;//saves the math function
		getToken();
		String vname = token;
		
		getToken();
		if(!token.equalsIgnoreCase("TO") && !token.equalsIgnoreCase("FROM")){
			handleErr(SYNTAX);
		}
		
		getToken();
		String vname2 = token;
		
		Math.doMath(op, vname, vname2);
		
	}
	
	//Finds the value of the equation if it is using a condenced expression
	private Number condensedExp(String vname) throws InterpreterException
	{
		Number result = null;
		char op = token.charAt(0);
		try{
			//Get original value
			double temp = 0;
			if(!isObject)
				temp = ((Number) vars.getVariableValue(vname)).doubleValue();
			else
				if(!objs.objVarDoesExist(currentObj, vname))
					handleErr(NOTVAR);
				else
					temp = ((Number) objs.getVarValueFromObj(currentObj, vname)).doubleValue();
			
			if(op == PP || op == MM){//run is ++ or --
				switch(op){
				case PP:
					temp++;
					result = temp;
					break;
				case MM:
					temp--;
					result = temp;
					break;
				}
			}else{
				getToken();
				double partialResult = ((Number) evalExp3()).doubleValue();
				switch(op){
				case DE:
					temp /= partialResult;
					result = temp;
					break;
				case TE:
					temp *= partialResult;
					result = temp;
					break;
				case PE:
					temp += partialResult;
					result = temp;
					break;
				case ME:
					temp -= partialResult;
					result = temp;
					break;
				}
			}
		}catch(ClassCastException e){
			handleErr(NOTNUMBER);
		}
		return result;
	}
	
	//loop to create new object
	private void execNewObject() throws InterpreterException
	{
		getToken();//find Object's template name
		rStack.push(new Integer(progIdx));//save place to return too
		//check if use extends another class
		progIdx = objs.objLabelTabel.get(token);//gets index to object location
		getToken();
		if(token.equalsIgnoreCase("EXTENDS")){
			getToken();//gets extended class name
			rStack.push(new Integer(progIdx));//saves this classes location
			progIdx = objs.objLabelTabel.get(token);//gets index to object location
			createNewObjectLoop();
			progIdx = rStack.pop();
		}
		createNewObjectLoop();
		
		//take program index back to original location
		progIdx = rStack.pop();
	}
	//loops though assigning variables to the object
	private void createNewObjectLoop() throws InterpreterException{
		//loop until end of object
		findEOL();
		while(!token.equalsIgnoreCase("ENDOBJ") || progIdx<prog.length)
		{
			getToken();
			if(token.equalsIgnoreCase("ENDOBJ"))
					break;
			if(!token.equals("\n")){
				putBack();
				assignment();
			}
		}
	}
	
	//loop to create new class
	private void execNewClass() throws InterpreterException
	{
//		getToken();//find Class's template name
		rStack.push(new Integer(progIdx));//save place to return too
		//check if use extends another class
		progIdx = clss.classesLabelTable.get(token);//gets index to class location
		getToken();
		if(token.equalsIgnoreCase("EXTENDS")){
			getToken();//gets extended class name
			rStack.push(new Integer(progIdx));//saves this classes location
			progIdx = clss.classesLabelTable.get(token);//gets index to object location
			createNewClassLoop();
			progIdx = rStack.pop();
		}
		createNewClassLoop();
		
		//take program index back to original location
		progIdx = rStack.pop();
	}
	//loops though assigning variables to the class
		private void createNewClassLoop() throws InterpreterException{
			//loop until end of object
			findEOL();
			while(!token.equalsIgnoreCase("ENDCLASS") || progIdx<prog.length)
			{
				getToken();
				if(token.equalsIgnoreCase("ENDCLASS"))
						break;
				if(token.equalsIgnoreCase("DEFINE")){//skip over defines
					define();
					continue;
				}
				if(token.equalsIgnoreCase("OBJECT")){//skip over objects
					object();
					continue;
				}
				if(!token.equals("\n")){
					putBack();
					assignment();
				}
			}
		}
	
//******************** Expression Parser ***********************
	//Parser entery point
	private Object evaluate() throws InterpreterException
	{
		Object result = 0;
		
		getToken();
		if(token.equals(EOP))
			handleErr(NOEXP);//no expression present
		
		if(token.equalsIgnoreCase("NEW")){
			tokType = OBJECT;
			return result;
		}
		
		//Parse and evaluate the expression.
		result = evalExp0();
		
		putBack();
		
		
		return result;
	}
	
	//Process conditional statements
	private Object evalExp0() throws InterpreterException
	{
		Object result;
		
		result = evalExp1();
		//if at end of program, return.
		if(token.equals(EOP)) return result;
		
		String cond = token;
		//if token is a conditional then find if the condition is true
		if(tokType == CONDITIONAL){
			double l_temp = ((Number) result).doubleValue();
			getToken();
			if(tokType == CONDITIONAL){
				cond += " "+token;
				getToken();
			}
			double r_temp = ((Number) evalExp0()).doubleValue();
			if(cond.equalsIgnoreCase("AND") || cond.equalsIgnoreCase("&&")){
				if(l_temp == 1 && r_temp == 1) result = 1;
				else result = 0;
			}else if(cond.equalsIgnoreCase("OR") || cond.equalsIgnoreCase("||")){
				if(l_temp == 1 || r_temp == 1) result = 1;
				else result = 0;
			}else if(cond.equalsIgnoreCase("IS")){
				if(l_temp == r_temp) result = 1;
				else result = 0;
			}else if(cond.equalsIgnoreCase("IS NOT")){
				if(l_temp != r_temp) result = 1;
				else result = 0;
			}else if(cond.equalsIgnoreCase("IS GREATER")){
				if(l_temp > r_temp) result = 1;
				else result = 0;
			}else if(cond.equalsIgnoreCase("IS SMALLER")){
				if(l_temp < r_temp) result = 1;
				else result = 0;
			}
		}
		
		
		return result;
	}
	
	//Process relation operators.
	private Object evalExp1() throws InterpreterException
	{
		Object result;
		char op;
		
		result = evalExp2();
		//If at end of program, return.
		if(token.equals(EOP)) return result;
		
		op = token.charAt(0);
		
		if(isRelop(op)){
			double l_temp = Double.parseDouble(result.toString());
			getToken();
			double r_temp = Double.parseDouble(evalExp1().toString());
			switch(op){ // Perform the relation operation
			case '<':
				if(l_temp < r_temp) result = 1;
				else result = 0;
				break;
			case LE:
				if(l_temp <= r_temp) result = 1;
				else result = 0;
				break;
			case '>':
				if(l_temp > r_temp) result = 1;
				else result = 0;
				break;
			case GE:
				if(l_temp >= r_temp) result = 1;
				else result = 0;
				break;
			case '=':
				if(l_temp == r_temp) result = 1;
				else result = 0;
				break;
			case NE:
				if(l_temp != r_temp) result = 1;
				else result = 0;
				break;
			}
		}
		return result;
	}
	
	//Add or subtract two terms.
	private Object evalExp2() throws InterpreterException
	{
		char op;
		Object result;
		
		result = evalExp3();
		
		while((op = token.charAt(0)) == '+' || op == '-' 
				|| op == PE || op == ME || op == PP || op == MM){
			double temp = Double.parseDouble(result.toString());
			Number partialResult;
			
			if(op == PP || op == MM){//run is ++ or --
				switch(op){
				case PP:
					temp++;
					result = temp;
					progIdx += 2;
					break;
				case MM:
					temp--;
					result = temp;
					progIdx += 2;
					break;
				}
				return result;
			}else{
				getToken();
				partialResult = (Number) evalExp3();
				switch(op){
				case '-':
					temp -= partialResult.doubleValue();
					result = temp;
					break;
				case '+':
					temp += partialResult.doubleValue();
					result = temp;
					break;
				case PE:
					temp += partialResult.doubleValue();
					result = temp;
					break;
				case ME:
					temp += partialResult.doubleValue();
					result = temp;
					break;
				}
			}
		}
		return result;
	}
	
	//Multiply or divide two factors.
	private Object evalExp3() throws InterpreterException
	{
		char op;
		Object result;
		
		result = evalExp4();
		
		while((op = token.charAt(0)) == '*' ||
				op == '/' || op == '%'){
			double temp = Double.parseDouble(result.toString());
			getToken();
			Number partialResult = (Number) evalExp4();
			switch(op){
			case '*':
				temp *= partialResult.doubleValue();
				result = temp;
				break;
			case '/':
				if(partialResult.intValue() == 0)
					handleErr(DIVBYZERO);
				temp /= partialResult.doubleValue();
				result = temp;
				break;
			case '%':
				if(partialResult.intValue() == 0)
					handleErr(DIVBYZERO);
				temp %= partialResult.doubleValue();
				result = temp;
				break;
			}
		}
		return result;
	}
	
	//Process an exponent.
	private Object evalExp4() throws InterpreterException
	{
		Object result;
		
		result = evalExp5();
		
		if(token.equals("^")){
			getToken();
			Number partialResult = (Number) evalExp4();
			double ex = Double.parseDouble(result.toString());
			if(partialResult.intValue() == 0)
				result = 1;
			else{
				double temp = Double.parseDouble(result.toString());
				for(int t=partialResult.intValue()-1;t>0;t--)
					temp *= ex;
				result = temp;
			}
		}
		return result;
	}
	
	//Evaluate a unary + or -.
	private Object evalExp5() throws InterpreterException
	{
		Object result;
		String op = "";
		
		if((tokType == DELIMITER) && 
				token.equals("+") || token.equals("-")){
			op = token;
			getToken();
		}
		result = evalExp6();
		
		if(op.equals("-")) result = -Double.parseDouble(result.toString());
		
		return result;
	}
	
	//Process a parenthesized expression.
	private Object evalExp6() throws InterpreterException
	{
		Object result;
		
		if(token.equals("(")){
			getToken();
			result = evalExp0();
			if(!token.equals(")"))
					handleErr(UNBALPARENS);
			getToken();
		}
		else result = atom();
		
		return result;
	}
	
	//Get the value of a number or variable.
	private Object atom() throws InterpreterException
	{
		Object result = 0;
		
		switch(tokType){
		case NUMBER:
			try{
				result = Double.parseDouble(token);
			}catch(NumberFormatException e){
				handleErr(SYNTAX);
			}
			getToken();
			break;
		case QUOTEDSTR:
			try{
				result = token.toString();
				progIdx += token.toString().length();
			}catch(Exception e){
				handleErr(SYNTAX);
			}
			break;
		case VARIABLE:
			try{
				result = findVar(token);
				getToken();
			}catch(Exception e){
				handleErr(VARIABLE);
			}
			break;
		default:
			handleErr(SYNTAX);
			break;
		}
		return result;
	}
	
	//Return the value of a variable.
	private Object findVar(String vname) throws InterpreterException
	{
		if(!Character.isLetter(vname.charAt(0))){
			handleErr(SYNTAX);
			return 0.0;
		}
		if(clss.isClassOrObject(vname)){
			String str[] = clss.splitString(vname);
			if(clss.doesExist(str[0])){//if referring to a class
				if(str.length == 3)//is a variable in an object in a class
					return clss.getVarFromObjectInClass(str[0], str[1], str[2]);
				else
					return clss.getVarFromClass(str[0], str[1]);
			}else{//is referring to an object
				return objs.getVarValueFromObj(str[0], str[1]);
			}
		}else//is just a variable
			return vars.getVariableValue(vname);
	}
	
	//Return a token to the input stream.
	private void putBack()
	{
		if(token == EOP) return;
		for(int i=0;i<token.length();i++)progIdx--;
		if(tokType == DATATYPE) progIdx -= 2;
	}
	
	//Handle an error.
	private void handleErr(int error) throws InterpreterException{
		String[] err = {
				"Syntax Error",
				"Unbalanced Parentheses",
				"No Expression Present",
				"Division by Zero",
				"Equal sign expected",
				"Variable is not defined",
				"Duplicate label",
				"Undefined label",
				"THEN expected",
				"TILL expected",
				"NEXT without FOR",
				"RETURN without RUN",
				"Closing quotes needed",
				"File not found",
				"I/O error while loading file",
				"I/O error on INPUT statement",
				"Issue finding file size",
				"IF without ENDIF",
				"DEFINE without RETURN",
				"Variable is not Number",
				"Typecasting Variable that doesn't exist",
				"Dublicate object",
				"Class or Object is not found",
				"OBJECT without ENDOBJ"
		};
		
		throw new InterpreterException(err[error]+" Script Index:"+ ++progIdx+" Line:"+ ++lineCount);
	}
	
	//Obtain the next token.
	private void getToken() throws InterpreterException
	{
		
		tokType = NONE;
		token = "";
		kwToken = UNKNCOM;
		
		//check for end of program
		if(progIdx == prog.length){
			token = EOP;
			return;
		}
		
		//Skip over white space.
		while(progIdx < prog.length &&
				isSpaceOrTab(prog[progIdx])) progIdx++;
		
		//Trailing whitespace ends program.
		if(progIdx == prog.length){
			token = EOP;
			tokType = DELIMITER;
			return;
		}
		
		//Skip over code comments
		if(prog[progIdx] == '#'){
			findEOL();
			progIdx--;
		}
		
		switch(prog[progIdx]){//get line break for Unix and Windows formats
		case '\r':
			progIdx += 2;
			lineCount++;
			kwToken = EOL;
			token = "\n";
			return;
		case '\n':
			progIdx++;
			lineCount++;
			kwToken = EOL;
			token = "\n";
			return;
		}
		
		char ch = prog[progIdx];
		
		//Check for use of &&/||
		
		if(ch == '&' || ch == '|'){
			token = String.valueOf(ch);
			token += String.valueOf(prog[++progIdx]);
			kwToken = lookUp(token);
			if(isCond(token)){ 
				tokType = CONDITIONAL;
				progIdx++;
				return;
			}else{
				token = "";
			}
		}
		
		//Check for != not equal
		if(ch == '!'){
			if(progIdx+1 == prog.length) handleErr(SYNTAX);
			
			if(prog[progIdx+1] == '='){
				progIdx += 2;
				token = String.valueOf(NE);
				tokType = DELIMITER;
				return;
			}else{
				handleErr(SYNTAX);
				return;
			}
		}
		
		//Check for dataType
		if(ch == '['){
			progIdx++;
			while (prog[progIdx] != ']'){
				token += prog[progIdx];
				progIdx++;
			}
			progIdx++;
			tokType = DATATYPE;
			return;
		}
		
		// Check for relational operator.
		if(ch == '<' || ch == '>'){
			if(progIdx+1 == prog.length) handleErr(SYNTAX);
			
			switch(ch){
			case '<':
				if(prog[progIdx+1] == '>'){
					progIdx += 2;
					token = String.valueOf(NE);
				}
				else if(prog[progIdx+1] == '='){
					progIdx += 2;
					token = String.valueOf(LE);
				}
				else{
					progIdx++;
					token = "<";
				}
				break;
			case '>':
				if(prog[progIdx+1] == '='){
					progIdx += 2;
					token = String.valueOf(GE);
				}
				else{
					progIdx++;
					token = ">";
				}
				break;
			}
			tokType = DELIMITER;
			return;
		}
		if(ch == '+' || ch == '-' | ch == '*' || ch == '/'){
			if(progIdx+1 == prog.length) handleErr(SYNTAX);
			
			switch(ch){
			case '+':
				if(prog[progIdx+1] == '='){
					progIdx += 2;
					token = String.valueOf(PE);
				}
				else if(prog[progIdx+1] == '+'){
					progIdx += 2;
					token = String.valueOf(PP);
				}
				else{
					progIdx++;
					token = String.valueOf('+');
				}
				break;
			case '-':
				if(prog[progIdx+1] == '='){
					progIdx += 2;
					token = String.valueOf(ME);
				}
				else if(prog[progIdx+1] == '-'){
					progIdx += 2;
					token = String.valueOf(MM);
				}
				else{
					progIdx++;
					token = String.valueOf('-');
				}
				break;
			case '*':
				if(prog[progIdx+1] == '='){
					progIdx += 2;
					token = String.valueOf(TE);
				}
				else{
					progIdx++;
					token = "*";
				}
				break;
			case '/':
				if(prog[progIdx+1] == '='){
					progIdx += 2;
					token = String.valueOf(DE);
				}
				else{
					progIdx++;
					token = "/";
				}
				break;
			}
			tokType = DELIMITER;
			return;
		}
		
		if(isDelim(prog[progIdx])){
			// Is an operator.
			token += prog[progIdx];
			progIdx++;
			tokType = DELIMITER;
		}
		else if(Character.isLetter(prog[progIdx])){
			//Is a variable or keyword.
			while(!isDelim(prog[progIdx])){
				token += prog[progIdx];
				progIdx++;
				if(progIdx >= prog.length) break;
			}
			
			kwToken = lookUp(token);
			if(isCond(token)){ tokType = CONDITIONAL; return;}
			if(kwToken==UNKNCOM) tokType = VARIABLE;
			else tokType = COMMAND;
		}
		else if(Character.isDigit(prog[progIdx])){
			//Is a number.
			while(!isDelim(prog[progIdx])){
				token += prog[progIdx];
				progIdx++;
				if(progIdx >= prog.length) break;
			}
			tokType = NUMBER;
		}
		else if(prog[progIdx] == '"'){
			//is a quoted string.
			progIdx++;
			ch = prog[progIdx];
			while(ch != '"' && ch != '\n'){
				token += ch;
				progIdx++;
				ch = prog[progIdx];
			}
			if(ch == '\n' || ch == '\r') handleErr(MISSINGQUOTE);
			progIdx++;
			tokType = QUOTEDSTR;
		}
		else{ //unknown characters terminates program
			token = EOP;
			return;
		}
	}
	
	//Returns true if c is a conditional
	private boolean isCond(String c)
	{
		for (String str : cond) {
			if(str.equalsIgnoreCase(c)){
				return true;
			}
		}
		return false;
	}
	
	//Return true if c is a delimiter.
	private boolean isDelim(char c)
	{
		if((" \n\r,;<>+-/*%^=()#".indexOf(c) != -1))
			return true;
		return false;
	}
	
	//Return true if c is a space or a tab.
	boolean isSpaceOrTab(char c)
	{
		if(c == ' ' || c == '\t') return true;
		return false;
	}
	
	//Return true if c is a relational operator.
	boolean isRelop(char c){
		if(relops.indexOf(c) != -1) return true;
		return false;
	}
	
	//Return true if c is a Condensed Expression
	boolean isCondExpes(char c){
		if(cexpres.indexOf(c) != -1) return true;
		return false;
	}
	
	//Look up a token's internal representation in the
	// token table
	private int lookUp(String s)
	{
		int i;
		
		//Convert to lowercase; 
		s = s.toLowerCase();
		
		//See if token is in table.
		for(i=0;i<kwTable.length;i++)
			if(kwTable[i].keyword.equals(s))
				return kwTable[i].keywordToken;
		return UNKNCOM; //unknown keyword
	}
	
	//Join arrays into one
	private static Keyword[] joinKeywords(Keyword[] ...params)
	{
		int size = 0;
		for(Keyword[] array : params){
			size += array.length;
		}
		
		Keyword[] result = new Keyword[size];
		
		int j = 0;
		for(Keyword[] array : params){
			for(Keyword k : array){
				result[j++] = k;
			}
		}
		
		return result;
	}
	
}
