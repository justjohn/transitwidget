/**
 * 
 */
package com.example.helloandroid.prefs;

/**
 * @author james
 *
 */
public class NextBusValue {
	private final String SEP = "\001";
	private String shortLabel;
	private String longLabel;
	private String tag;

	public void init(String shortLabel, String longLabel, String tag) {
		this.shortLabel = shortLabel;
		this.longLabel = longLabel;
		if (shortLabel == null || shortLabel.isEmpty()) {
			this.shortLabel = longLabel;
		} else if (longLabel == null || longLabel.isEmpty()) {
			this.longLabel = shortLabel;
		}
		this.tag = tag;
	}
	public void initFromPrefs(String prefsString) {
		String[] parts = prefsString.split(SEP);
		this.shortLabel = parts[0];
		this.longLabel = parts[1];
		this.tag = parts[2];
	}
	public String toPrefsString() {
		return shortLabel + SEP + longLabel + SEP + tag;
	}
	public String getShortLabel() {
		return shortLabel;
	}
	public String getLongLabel() {
		return longLabel;
	}
	public String getTag() {
		return tag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NextBusValue other = (NextBusValue) obj;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		return true;
	}
}
