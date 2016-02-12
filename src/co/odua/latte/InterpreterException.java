package co.odua.latte;

//Exception Class for errors in latte scripts
public class InterpreterException extends Exception {
	/**
	 * This class with throw errors given by the Latte Interpreter
	 */
	private static final long serialVersionUID = 1869963940858751538L;
	String errStr; // Describes the Error to user
	
	public InterpreterException(String str){
		errStr = str;
	}
	
	public String toString() {
		return errStr;
	}
}
