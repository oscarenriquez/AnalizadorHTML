/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gt.edu.umg.automatas;

/**
 *
 * @author Desarrollo
 */
public class DocumentObjectModel {
    private DocumentObjectModel parent;
    private String value;   
    private DocumentObjectModel child;
    
    public DocumentObjectModel(String value) {
        this.value = value;
    }

    public DocumentObjectModel getParent() {
        return parent;
    }

    public void setParent(DocumentObjectModel parent) {
        this.parent = parent;
    }        

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DocumentObjectModel getChild() {
        return child;
    }

    public void setChild(DocumentObjectModel child) {
        this.child = child;
    }        
    
}
