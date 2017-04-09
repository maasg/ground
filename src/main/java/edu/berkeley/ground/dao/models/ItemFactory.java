package edu.berkeley.ground.dao.models;


import edu.berkeley.ground.exceptions.GroundItemAlreadyExistsException;
import edu.berkeley.ground.exceptions.GroundElementNotFoundException;
import edu.berkeley.ground.exceptions.GroundException;
import edu.berkeley.ground.model.versions.Item;

public interface ItemFactory<T extends Item<?>> {

  /**
   * Retrieves an item from the database
   * @param sourceKey
   * @return
   * @throws GroundException
   */
  T retrieveFromDatabase(String sourceKey) throws GroundException;

  Class<T> getType(); //circumvent the freaking weak Java type system

  /**
   * retrieves whether the item of this type with the given sourceKey exists in the backend.
   * @param sourceKey the key for the item to check
   * @return true if the item exists, false otherwise.
   * @throws GroundException to propagate exceptions occuring while interacting with the backend.
   */
  default boolean itemExists(String sourceKey) throws GroundException {
    try {
      this.retrieveFromDatabase(sourceKey);
      return true;
    } catch (GroundElementNotFoundException elementNotFound) {
      return false;
    }
  }

  /**
   * Verifies that the provided sourceKey does not exists in the backend
   * @param sourceKey
   * @throws GroundItemAlreadyExistsException if the element exists
   * @throws GroundException to propagate any other exception
   */
  default void verifyItemNotExists(String sourceKey) throws GroundException {
    if (itemExists(sourceKey)) {
      throw new GroundItemAlreadyExistsException(getType(), sourceKey);
    }
  }

}
