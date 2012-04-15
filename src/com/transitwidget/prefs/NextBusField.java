/**
 * 
 */
package com.transitwidget.prefs;

import java.util.List;

/**
 * Encapsulates information about one field required for getting
 * next bus predictions.
 * 
 * @author james
 *
 */
public interface NextBusField<T extends NextBusValue> {
	boolean isSet();
	T get();
	String getTag();
	/**
	 * Set the current value; not validated against the valid values.
	 * @return true if the value changed
	 */
	boolean set(T value);
	/** Clear the set value, and the loaded valid values. */
	void clear();

	/**
	 * Have the valid values been loaded?
	 * @return
	 */
	boolean hasValidValuesLoaded();

	/**
	 * May need to load data from network, so do not call on UI thread.
	 * @return the valid values; usually depends on other fields
	 * (e.g. routes depends upon agency).
	 */
	List<T> loadValidValues();

	/**
	 * 
	 * @return null if not loaded, else a list.
	 */
	List<T> getValidValues();

	/**
	 * If valid values are loaded, and the value is in the valid values,
	 * return its index, else return -1.
	 * @return
	 */
	int indexOf(T value);

}
