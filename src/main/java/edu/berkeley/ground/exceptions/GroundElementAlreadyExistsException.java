package edu.berkeley.ground.exceptions;


import edu.berkeley.ground.model.versions.Item;

public class GroundElementAlreadyExistsException extends GroundException {
  public GroundElementAlreadyExistsException(Class<? extends Item> itemType) {
    super("Item of type ["+itemType.getName()+"] already exists");
  }

  public GroundElementAlreadyExistsException(Class<? extends Item> itemType, String id) {
    super("Item of type ["+itemType.getName()+"] with id ["+id+"] already exists");
  }
}
