package ast;

abstract public class Type {

    public Type( String name ) {
        this.name = name;
    }

    public static Type booleanType = new TypeBoolean();
    public static Type intType = new TypeInt();
    public static Type stringType = new TypeString();
    public static Type nilType = new TypeNull();
    public static Type undefType = new TypeUndefined();
    
  
    public String getName() {
        return name;
    }


    private String name;
}
