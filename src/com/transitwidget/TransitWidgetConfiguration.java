package com.transitwidget;

import java.util.ArrayList;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.transitwidget.prefs.NextBusAgency;
import com.transitwidget.prefs.NextBusDirection;
import com.transitwidget.prefs.NextBusField;
import com.transitwidget.prefs.NextBusObserverConfig;
import com.transitwidget.prefs.NextBusRoute;
import com.transitwidget.prefs.NextBusStop;
import com.transitwidget.prefs.NextBusValue;

public class TransitWidgetConfiguration extends PreferenceActivity {
	protected NextBusObserverConfig config;
	private int mAppWidgetId;
	private AgencyConfigurationField agencyField;
	private RouteConfigurationField routeField;
	private DirectionConfigurationField directionField;
	private StopConfigurationField stopField;
	
	protected TransitConfigurationField<?> lastChangedField = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        config = new NextBusObserverConfig(getApplicationContext(), mAppWidgetId);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.widget_preferences);
        agencyField = new AgencyConfigurationField("which_agency_preference");
        routeField = new RouteConfigurationField("which_route_preference");
        directionField = new DirectionConfigurationField("which_direction_preference");
        stopField = new StopConfigurationField("which_stop_preference");

        agencyField.loadChoices();
    }

    @Override
    public void onContentChanged () {
    	super.onContentChanged(); // Called from super.onCreate().
    	
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
    	super.onPostCreate(savedInstanceState);
    	
    }

    /**
     * Called when the screen is rotated, or when this Activity *might* need 
     * to be ejected from memory (e.g. when screen locked).  The instance may
     * not be deleted after this.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    }

    /**
     * Called if restoring the UI, such as when the phone is rotated.
     * Called after onCreate and after onPostCreate are called.
     */
    @Override
    protected void onRestoreInstanceState(Bundle state) {
    	super.onRestoreInstanceState(state);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    }
    @Override
    protected void onPostResume() {
    	// TODO Auto-generated method stub
    	super.onPostResume();
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    }
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	// TODO Auto-generated method stub
    	super.onWindowFocusChanged(hasFocus);
    	if (lastChangedField != null) {
    		lastChangedField.updateConfigFromValue();
    		lastChangedField = null;
    	}
    }
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    }
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    }
    
    private abstract class TransitConfigurationField<T extends NextBusValue> {
    	private final TransitConfigurationField<?> previous;
    	private TransitConfigurationField<?> next;
    	private final ListPreference preference;
		private final NextBusField<T> configField;
		private final CharSequence summary;

    	public TransitConfigurationField(
    			String preferenceName,
    			TransitConfigurationField<?> previous,
    			NextBusField<T> configField) {
    		if (configField == null)
    			throw new IllegalStateException();
			this.configField = configField;

			PreferenceManager preferenceManager = getPreferenceManager();
    		Preference p = preferenceManager.findPreference(preferenceName);
    		if (p == null || !(p instanceof ListPreference)) {
    			throw new IllegalStateException();
    		}
  			this.preference = (ListPreference)p;
  			this.summary = p.getSummary();

  			this.previous = previous;
  			if (previous != null) {
  				previous.setNext(this);
  			}

  			updateListFromConfig();
  			updateValueFromConfig();
  			
  			this.preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference p, Object newValue) {
					lastChangedField = TransitConfigurationField.this;
		  			String v = preference.getValue();
		  			int i = preference.findIndexOfValue(v);
					return true;
				}
			});
    	}

    	private void updateValueFromConfig() {
    		// Set the preference to a default value, unless the config is set.
    		preference.setValue("");
    		preference.setSummary(summary);
			if (configField.isSet()) {
  				T v = configField.get();
  				if (configField.hasValidValuesLoaded()) {
  	  				int ndx = configField.indexOf(v);
  	  				if (ndx >= 0) {
  	  					preference.setValueIndex(ndx);
  	  					List<T> vv = configField.getValidValues();
  	  					preference.setSummary(vv.get(ndx).getItemLabel());
  	  				}
  				}
  			}
			if (next != null) {
				next.loadChoices();
			}
		}

    	private void updateConfigFromValue() {
  			String v = preference.getValue();
    		preference.setSummary(summary);

  			T newT = null;
  			if (v != null && v.length() > 0) {
  				if (configField.hasValidValuesLoaded()) {
	  				List<T> vv = configField.getValidValues();
	  				for (T t : vv) {
	  					if (v.equals(t.getTag())) {
	  						newT = t;
	  						preference.setSummary(newT.getItemLabel());
	  						break;
	  					}
	  				}
  				} else {
  					newT = createNextBusField();
  					newT.setTag(v);
  				}
  			}
  			if (newT == null) {
  				configField.clear();
  			} else {
  				configField.set(newT);
  			}

  			if (next != null) {
				next.loadChoices();
			}
		}

		private void updateListFromConfig() {
			if (configField.hasValidValuesLoaded()) {
  				List<T> vv = configField.getValidValues();
  				List<String> names = new ArrayList<String>();
  				List<String> tags = new ArrayList<String>();
  				for (T t : vv) {
  					names.add(t.getItemLabel());
  					tags.add(t.getTag());
  				}
  				preference.setEntries(names.toArray(new String[0]));
  				preference.setEntryValues(tags.toArray(new String[0]));
  				preference.setEnabled(true);
  			} else {
  				preference.setEnabled(false);
  			}
			if (next != null) {
				next.loadChoices();
			}
		}

    	public void setNext(TransitConfigurationField<?> next) {
			this.next = next;
		}

    	/**
    	 * Must only be called from an {@link AsyncTask}.
    	 * @param config
    	 * @return
    	 */
    	List<T> getChoices() {
    		return configField.loadValidValues();
    	}
    	
    	public ListPreference getPreference() {
			return preference;
		}

    	public void loadChoices() {
    		new LoadChoices().execute();
    	}

        private class LoadChoices extends AsyncTask<Object, Object, Object> {
    		@Override
    		protected Object doInBackground(Object... params) {
    			getChoices();
    			return null;
    		}
    		@Override
    		protected void onPostExecute(Object result) {
    			updateListFromConfig();
    		}
        }

        public abstract T createNextBusField();
    }

    private class AgencyConfigurationField extends TransitConfigurationField<NextBusAgency> {
		public AgencyConfigurationField(String preferenceName) {
			super(preferenceName, null, config.getAgencyField());
			// TODO: want to be able to use preference store to initialize a new
			// widget based on the selections for the last widget configured.
			getPreference().setDefaultValue("mbta");
		}
		@Override
		public NextBusAgency createNextBusField() {
			return new NextBusAgency();
		}
    }

    private class RouteConfigurationField extends TransitConfigurationField<NextBusRoute> {
		public RouteConfigurationField(String preferenceName) {
			super(preferenceName, agencyField, config.getRouteField());
		}
		@Override
		public NextBusRoute createNextBusField() {
			return new NextBusRoute();
		}
    }

    private class DirectionConfigurationField extends TransitConfigurationField<NextBusDirection> {
		public DirectionConfigurationField(String preferenceName) {
			super(preferenceName, routeField, config.getDirectionField());
		}
		@Override
		public NextBusDirection createNextBusField() {
			return new NextBusDirection();
		}
    }

    private class StopConfigurationField extends TransitConfigurationField<NextBusStop> {
		public StopConfigurationField(String preferenceName) {
			super(preferenceName, directionField, config.getStopField());
		}
		@Override
		public NextBusStop createNextBusField() {
			return new NextBusStop();
		}
    }
}
