package simpleui.examples.modifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simpleui.modifiers.v3.M_Button;
import simpleui.modifiers.v3.M_Container;
import simpleui.modifiers.v3.M_KeyValue;
import simpleui.util.Log;
import android.content.Context;
import android.widget.Button;

public class M_KeyValueModifierTests extends M_Container {
	private static final String LOG_TAG = M_KeyValueModifierTests.class
			.getSimpleName();
	String key = "health";
	final Map<String, Float> values = new HashMap<String, Float>();

	public void setKey(final String key) {
		Log.d(LOG_TAG, "Set key=" + key);
		this.key = key;
	}

	private Float setMapping(final String arg0, final float arg1) {
		Log.d(LOG_TAG, "Set mapping (" + arg0 + "," + arg1 + ")");
		return values.put(arg0, arg1);
	}

	public M_KeyValueModifierTests() {

		values.put("A", 10f);
		values.put("B", 20f);

		final M_KeyValue testee = new M_KeyValue(key) {

			@Override
			protected List<String> getListOfKeys() {
				return new ArrayList<String>(values.keySet());
			}

			@Override
			protected float getValueForKey(final String arg0) {
				final Float valueForKey = values.get(arg0);
				if (valueForKey == null) {
					return 0;
				}
				return valueForKey;
			}

			@Override
			protected void setValueAndKey(final String arg0,
					final float arg1) {
				setMapping(arg0, arg1);
				Log.d(LOG_TAG, "starValues = " + values);
			}

			@Override
			protected float getDefaultValue(String key) {
				return 0;
			}

			@Override
			protected String getValueIdentifier() {
				return "Start value";
			}

		};
		add(testee);

		add(new M_Button("Save") {

			@Override
			public void onClick(Context arg0, Button arg1) {
				testee.save();
				testee.rebuildUi();
			}
		});

	}

}
