package edu.berkeley.ground.exceptions;


import edu.berkeley.ground.model.versions.Item;

public class GroundElementNotFoundException extends GroundException {

  public GroundElementNotFoundException(Class<Item> itemType) {
    super("Item of type ["+itemType.getName()+"] not found");
  }

  public GroundElementNotFoundException(Class<Item> itemType, String id) {
    super("Item of type ["+itemType.getName()+"] with id ["+id+"] not found");
  }

  public GroundElementNotFoundException(Class<? extends Item> itemType, String field, Object value) {
    super("Item of type ["+itemType.getName()+"]"+
      " with field ["+field+"] having value ["+value.toString()+"] not found");
  }

}
