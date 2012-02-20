/**
 * 
 */
package com.example.helloandroid.prefs;

import com.example.helloandroid.adapters.BaseItem;

/**
 * @author james
 *
 */
abstract class NextBusValue extends BaseItem {
	private String shortLabel;
	private String longLabel;
	
	public void init(String shortLabel, String longLabel, String tag) {
		this.shortLabel = shortLabel;
		this.longLabel = longLabel;
		if (shortLabel == null || shortLabel.equals("")) {
			this.shortLabel = longLabel;
		} else if (longLabel == null || longLabel.equals("")) {
			this.longLabel = shortLabel;
		}
		setTag(tag);
	}
	
	public String getShortLabel() {
		return shortLabel;
	}
	public String getLongLabel() {
		return longLabel;
	}
	@Override
	public String toString() {
		return getTag();
	}

	@Override
	public int hashCode() {
		String tag = getTag();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		String tag = getTag();
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NextBusValue other = (NextBusValue) obj;
		if (tag == null) {
			if (other.getTag() != null)
				return false;
		} else if (!tag.equals(other.getTag()))
			return false;
		return true;
	}
	
	@Override
	public String getItemLabel() {
		if (getShortLabel() != null && !getShortLabel().equals("")) {
			return getShortLabel();
		} else {
			return getLongLabel();
		}
	}
}
