package edu.berkeley.ground.exceptions;

import edu.berkeley.ground.model.versions.Item;

/**
 * Thrown to indicate that an Item of a given type already exists in the system.
 */
public class GroundItemAlreadyExistsException extends GroundException {

  public GroundItemAlreadyExistsException(Class<? extends Item> itemType, String sourceKey) {
    super("Item of type ["+itemType.getName()+"] with sourceKey ["+sourceKey+"] already exists");
  }

}
