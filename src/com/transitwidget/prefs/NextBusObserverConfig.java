/**
 * 
 */
package com.transitwidget.prefs;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.transitwidget.api.ServiceProvider;
import com.transitwidget.feed.model.Agency;
import com.transitwidget.feed.model.Direction;
import com.transitwidget.feed.model.Route;
import com.transitwidget.provider.contract.WidgetConfiguration;

/**
 * @author james
 * 
 */
public class NextBusObserverConfig {
	private final Context ctx;
	private final int widgetId;

	private NextBusAgencyField af = new NextBusAgencyField();
	private NextBusRouteField rf = new NextBusRouteField();
	private NextBusDirectionField df = new NextBusDirectionField();
	private NextBusStopField sf = new NextBusStopField();

	/** Seconds in the day */
	private int startObserving;
	/** Seconds in the day */
	private int stopObserving;

	public NextBusObserverConfig(Context ctx, int widgetId) {
		this.ctx = ctx;
		this.widgetId = widgetId;

		af.setNext(rf);
		rf.setNext(df);
		df.setNext(sf);

		// Load from database
        String selection = WidgetConfiguration.WIDGET_ID + " = ?";
        String[] selectionArgs = { String.valueOf(widgetId) };
        Cursor cursor = ctx.getContentResolver().query(WidgetConfiguration.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor.moveToFirst()) {
        	String agencyTag = cursor.getString(cursor.getColumnIndex(WidgetConfiguration.AGENCY));
        	String routeTag = cursor.getString(cursor.getColumnIndex(WidgetConfiguration.ROUTE));
        	String directionTag = cursor.getString(cursor.getColumnIndex(WidgetConfiguration.DIRECTION));
        	String stopTag = cursor.getString(cursor.getColumnIndex(WidgetConfiguration.STOP));

        	setAgency(new NextBusAgency().init(ServiceProvider.getAgency(ctx, agencyTag)));
        	setRoute(new NextBusRoute().init(ServiceProvider.getRoute(ctx, agencyTag, routeTag)));
    		setDirection(new NextBusDirection().init(ServiceProvider.getDirection(ctx, agencyTag, directionTag)));
    		setStop(new NextBusStop().init(ServiceProvider.getStop(ctx, agencyTag, stopTag)));

    		setStartObserving(cursor.getInt(cursor.getColumnIndex(WidgetConfiguration.START_TIME)));
        	setStopObserving(cursor.getInt(cursor.getColumnIndex(WidgetConfiguration.END_TIME)));
        } else {
        	this.startObserving = -1;
        	this.stopObserving = -1;
        }
        cursor.close();

		if (this.startObserving == this.stopObserving && this.startObserving != -1) {
			this.startObserving = this.stopObserving = -1;
		}
	}

	public void save() {
		// Persist to database
		ContentValues values = new ContentValues();
		values.put(WidgetConfiguration.WIDGET_ID, widgetId);
		values.put(WidgetConfiguration.AGENCY, getAgencyField().getTag());
		values.put(WidgetConfiguration.ROUTE, getRouteField().getTag());
		values.put(WidgetConfiguration.DIRECTION, getDirectionField().getTag());
		values.put(WidgetConfiguration.STOP, getStopField().getTag());
		values.put(WidgetConfiguration.START_TIME, getStartObserving());
		values.put(WidgetConfiguration.END_TIME, getStopObserving());

		// Check for existing preferences
		String selection = WidgetConfiguration.WIDGET_ID + " = ?";
		String[] selectionArgs =  { String.valueOf(widgetId) };
		Cursor cursor = ctx.getContentResolver().query(WidgetConfiguration.CONTENT_URI, null, selection, selectionArgs, null);
		if (cursor.moveToFirst()) {
			ctx.getContentResolver().update(WidgetConfiguration.CONTENT_URI, values, selection, selectionArgs);
		} else {
			ctx.getContentResolver().insert(WidgetConfiguration.CONTENT_URI, values);
		}
		cursor.close();
	}

	private static boolean safeEquals(Object a, Object b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}

	public List<NextBusAgency> getAgencies() {
		return getAgencyField().loadValidValues();
	}

	public NextBusAgency getAgency() {
		return getAgencyField().get();
	}

	public void setAgency(NextBusAgency newAgency) {
		getAgencyField().set(newAgency);
	}

	public List<NextBusRoute> getRoutes() {
		return getRouteField().loadValidValues();
	}

	public NextBusRoute getRoute() {
		return getRouteField().get();
	}

	public void setRoute(NextBusRoute route) {
		getRouteField().set(route);
	}

	public List<NextBusDirection> getDirections() {
		return getDirectionField().loadValidValues();
	}

	public NextBusDirection getDirection() {
		return getDirectionField().get();
	}

	public void setDirection(NextBusDirection direction) {
		getDirectionField().set(direction);
	}

	public List<NextBusStop> getStops() {
		return getStopField().loadValidValues();
	}

	public NextBusStop getStop() {
		return getStopField().get();
	}

	public void setStop(NextBusStop stop) {
		getStopField().set(stop);
	}

	public int getStartObserving() {
		return startObserving;
	}

	public void setStartObserving(int startObserving) {
		this.startObserving = startObserving;
	}

	public int getStopObserving() {
		return stopObserving;
	}

	public void setStopObserving(int stopObserving) {
		this.stopObserving = stopObserving;
	}

	protected abstract class AbstractNextBusField<T extends NextBusValue>
	implements NextBusField<T> {
		protected T value = null;
		protected NextBusField<?> next;
		protected List<T> validValues = null;

		public void setNext(NextBusField<?> next) {
			this.next = next;
		}
		
		public boolean isSet() {
			return value != null;
		}

		public T get() {
			return value;
		}

		public String getTag() {
			if (value != null) {
				return value.getTag();
			} else {
				return null;
			}
		}
		
		public boolean set(T value) {
			if (safeEquals(this.value, value)) {
				return false;
			}
			this.value = value;
			if (next != null)
				next.clear();
			return true;
		}
		
		public void clear() {
			value = null;
			validValues = null;
			if (next != null)
				next.clear();
		}
		
		public boolean hasValidValuesLoaded() {
			return validValues != null;
		}

		public final List<T> loadValidValues() {
			if (!hasValidValuesLoaded()) {
				validValues = doLoadValidValues();
			}
			return validValues;
		}

		protected abstract List<T> doLoadValidValues();
		
		public List<T> getValidValues() {
			return validValues;
		}
		
		public int indexOf(T value) {
			if (hasValidValuesLoaded()) {
				for (int i = 0; i < validValues.size(); i++) {
					if (validValues.get(i).equals(value))
						return i;
				}
			}
			return -1;
		}
	}

	protected class NextBusAgencyField extends AbstractNextBusField<NextBusAgency> {
		@Override
		protected List<NextBusAgency> doLoadValidValues() {
			ArrayList<NextBusAgency> results = new ArrayList<NextBusAgency>();
			for (Agency model : ServiceProvider.getAgencies(ctx)) {
				results.add(new NextBusAgency().init(model));
			}
			return results;
		}
	}

	public NextBusField<NextBusAgency> getAgencyField() {
		return af;
	}
	
	
	protected class NextBusRouteField extends AbstractNextBusField<NextBusRoute> {
		@Override
		protected List<NextBusRoute> doLoadValidValues() {
			if (af.getTag() == null)
				return null;
			ArrayList<NextBusRoute> results = new ArrayList<NextBusRoute>();
			for (Route model : ServiceProvider.getRoutes(ctx, af.getTag())) {
				results.add(new NextBusRoute().init(model));
			}			
			return results;
		}
	}

	public NextBusField<NextBusRoute> getRouteField() {
		return rf;
	}

	protected class NextBusDirectionField extends AbstractNextBusField<NextBusDirection> {
		@Override
		protected List<NextBusDirection> doLoadValidValues() {
			if (rf.getTag() == null)
				return null;
			ArrayList<NextBusDirection> results = new ArrayList<NextBusDirection>();
			for (Direction model : ServiceProvider.getRouteConfig(ctx, af.getTag(), rf.getTag())) {
				results.add(new NextBusDirection().init(model));
			}
			return results;
		}
	}

	public NextBusField<NextBusDirection> getDirectionField() {
		return df;
	}

	protected class NextBusStopField extends AbstractNextBusField<NextBusStop> {
		@Override
		protected List<NextBusStop> doLoadValidValues() {
			if (df.getTag() == null)
				return null;
			List<NextBusDirection> dirs = df.getValidValues();
			if (dirs == null)
				return null;
			// Stops are attached to directions in the route config, not fetched
			// from the feed separately. Find the selected direction (this.direction
			// might be from prefs, and thus not have the list of stops).
			for (NextBusDirection d : dirs) {
				if (d.equals(df.get())) {
					return d.getStops();
				}
			}
			return null;
		}
	}

	public NextBusField<NextBusStop> getStopField() {
		return sf;
	}
}
