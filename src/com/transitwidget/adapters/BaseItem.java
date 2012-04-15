package com.transitwidget.adapters;

public abstract class BaseItem {
	private String tag;
	
	public BaseItem() {
		tag = null;
	}
	
	public BaseItem(String tag) {
		this.tag = tag;
	}
	
	public void setTag(String t) {
		tag = t;
	}
	
	public String getTag() {
		return tag;
	}
	
	public abstract String getItemLabel();
}
