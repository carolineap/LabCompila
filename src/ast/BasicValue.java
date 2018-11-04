package ast;

public class BasicValue extends Factor {

	public BasicValue(String stringValue) {
		StringValue = stringValue;
	}
	
	public BasicValue(boolean boolValue) {
		this.boolValue = boolValue;
	}
	
	public BasicValue(Integer intValue) {
		this.intValue = intValue;
	}
	
	private Integer intValue;
	private boolean boolValue;
	private String StringValue;
	
}
