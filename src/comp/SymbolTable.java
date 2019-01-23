/*  Caroline Aparecida de Paula Silva 
    Isabela Sayuri Matsumoto        */
    
package comp;

import java.util.*;

public class SymbolTable {

	public SymbolTable() {
        globalTable = new Hashtable();
        localTable  = new Hashtable();
        classTable = new Hashtable();
        superClassTable =  new Hashtable();
    }
    
    public Object putInGlobal( String key, Object value ) {
       return globalTable.put(key, value);
    }

    public Object putInLocal( String key, Object value ) {
       return localTable.put(key, value);
    }
    
    public Object putInClass( String key, Object value ) {
        return classTable.put(key, value);
     }

     public Object getInClass( Object key ) {
    	 
    	 Object result;
    	 
    	 if ((result = localTable.get(key)) != null) {
    		 return result;
    	 }

    	 else return classTable.get(key);
     }
     
	 public Object putInSuperClassTable( String key, Object value ) {
	     return superClassTable.put(key, value);
	  }
	 
    public Object getInLocal( Object key ) {
       return localTable.get(key);
    }
    
    public Object getInGlobal( Object key ) {
       return globalTable.get(key);
    }
    public Object getInSuperClassTable( Object key ) {
        return superClassTable.get(key);
     }
    public Object get( String key ) {
        // returns the object corresponding to the key. 
        Object result;
        if ( (result = localTable.get(key)) != null ) {
              // found local identifier
            return result;
        }
        else {
              // global identifier, if it is in globalTable
            return globalTable.get(key);
        }
    }

    public void removeLocalIdent() {
           // remove all local identifiers from the table
         localTable.clear();
    }
    
    public void removeSuperTableIdent() {
        // remove all local identifiers from the table
      superClassTable.clear();
 }
    
    public void removeClassIdent() {
        // remove all local identifiers from the table
     classTable.clear();
 }
      
        
    private Hashtable<String, Object> globalTable, localTable, classTable, superClassTable;

}
