package edu.berkeley.ground.exceptions;


import edu.berkeley.ground.model.versions.Item;

/**
 *  Thrown to indicate that an Item of a given type is expected to exist but it was not found in the system.
 */
public class GroundItemNotFoundException extends GroundException {

  public GroundItemNotFoundException(Class<Item> itemType, String sourceKey) {
    super("Item of type ["+itemType.getName()+"] with sourceKey ["+sourceKey+"] not found");
  }

  public GroundItemNotFoundException(Class<? extends Item> itemType, String field, Object value) {
    super("Item of type ["+itemType.getName()+"]"+
      " with field ["+field+"] having value ["+value.toString()+"] not found");
  }

}
