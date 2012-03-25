package com.transitwidget.utils;

import java.util.NoSuchElementException;

import android.widget.Adapter;

public class AdapterUtils {
	public static final int getAdapterPositionById(final Adapter adapter, final long id) throws NoSuchElementException {
	    final int count = adapter.getCount();

	    for (int pos = 0; pos < count; pos++) {
	        if (id == adapter.getItemId(pos)) {
	            return pos;
	        }    
	    }

	    throw new NoSuchElementException();
	}
}
