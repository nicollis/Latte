package co.odua.latte;

import co.odua.latte.Latte.*;

public class Math{
	private final static int MATH = Latte.MATH;
	public final static Keyword keywords[] = {
			new Keyword("add", MATH),
			new Keyword("subtract", MATH),
			new Keyword("multiply", MATH),
			new Keyword("divide", MATH)
	};
	
	//basic add a number to a variable and update it
	public static void addToVariable(String vname, double increment){
		double variable1 = ((Number) Latte.vars.getVariableValue(vname)).doubleValue();
		Latte.vars.ChangeValue(vname, add(variable1, increment));
	}
	
	//Filters token statement and does math to 2 variables together
	public static void doMath(String token, String fVarName, String sVarName)
	{
		double variable1 = ((Number) Latte.vars.getVariableValue(fVarName)).doubleValue();
		double variable2 = ((Number)Latte.vars.getVariableValue(sVarName)).doubleValue();
		double results = privateDoMath(token, variable1, variable2);
		//sets new value
		Latte.vars.ChangeValue(sVarName, results);
	}
	
	private static double privateDoMath(String token, double variable1, double variable2){
		double results = 0.0;
		
		//finds what the user wanted to do
		if(token.equalsIgnoreCase("ADD")){
			results = add(variable1, variable2);
		}
		else if(token.equalsIgnoreCase("SUBTRACT")){
			results = sub(variable1, variable2);
		}
		else if(token.equalsIgnoreCase("MULTIPLY")){
			results = mul(variable1, variable2);
		}
		else if(token.equalsIgnoreCase("DIVIDE")){
			results = div(variable1, variable2);
		}
		
		return results;
	}
	
	//basic math
	private static double add(double var1, double var2){
		return var2 += var1;
	}
	private static double sub(double var1, double var2){
		return var2 -= var1;
	}
	private static double mul(double var1, double var2){
		return var2 *= var1;
	}
	private static double div(double var1, double var2){
		return var2 /= var1;
	}
}
