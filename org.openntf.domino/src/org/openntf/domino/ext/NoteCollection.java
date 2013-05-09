/**
 * 
 */
package org.openntf.domino.ext;

import java.util.Set;

import org.openntf.domino.NoteCollection.SelectOption;

/**
 * @author withersp
 * 
 */
public interface NoteCollection {

	/**
	 * Equals.
	 * 
	 * @param otherCollection
	 *            the other collection
	 * @return true, if successful
	 */
	public boolean equals(Object otherCollection);

	/**
	 * 
	 * @param options
	 *            a Set of SelectOption enum values corresponding to desired note types
	 */
	public void setSelectOptions(Set<SelectOption> options);
}