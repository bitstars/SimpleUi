package simpleui.modifiers.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import simpleui.SimpleUI;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.Toast;

public abstract class M_KeyValue extends M_Container {

	private static final String LOG_TAG = M_KeyValue.class
			.getSimpleName();
	private String key;
	private float value;

	public M_KeyValue(@NonNull String defaultKey) {

		if (defaultKey == null) {
			throw new RuntimeException("getKey() return value may not be null.");
		} else if (getListOfKeys().isEmpty()
				|| !getListOfKeys().contains(defaultKey)) {
			// Add def. key to list
			setValueAndKey(defaultKey, getDefaultValue(defaultKey));
		}
		this.key = defaultKey;
		this.value = getValueForKey(key);

		final M_Container startValueModifier = new M_Container(

				new M_FloatModifier() {

					@Override
					public String getVarName() {
						return "Start Value for " + key;
					}

					@Override
					public boolean saveFloat(final float value) {
						M_KeyValue.this.value = value;
						return true;
					}

					@Override
					public float loadFloat() {
						return getValueForKey(key);
					}
				});

		final M_Container keySelector = new M_Container(new M_Spinner() {

			@Override
			public boolean save(final SpinnerItem arg0) {
				return true;
			}

			@Override
			public int loadSelectedItemId() {
				final int i = getListOfKeys().indexOf(key);
				if (i == -1) {
					return 0;
				} else {
					return i;
				}
			}

			@Override
			public List<SpinnerItem> loadListToDisplay() {
				final ArrayList<SpinnerItem> list = new ArrayList<SpinnerItem>();
				for (int i = 0; i < getListOfKeys().size(); i++) {
					final String key = getListOfKeys().get(i);
					list.add(new SpinnerItem(i, key));
				}
				return list;
			}

			@Override
			public String getVarName() {
				return "Value Identifier";
			}

			@Override
			protected void onUserSelectedNewItem(Context context,
					long selectedItemId, SpinnerItem selectedItem) {
				super.onUserSelectedNewItem(context, selectedItemId,
						selectedItem); // Just log
				key = getListOfKeys().get((int) selectedItemId);
				startValueModifier.rebuildUi();
			}

		});

		final M_Button addButton = new M_Button("Add") {
			@Override
			public void onClick(final Context arg0, final Button arg1) {
				final M_TextModifier newIdentifierModifier = new M_TextModifier() {
					{
						setInfoText("Enter identifier");
					}

					@Override
					public boolean save(final String key) {
						if (key == null || key.equals("")) {
							Toast.makeText(getContext(), "invalid key",
									Toast.LENGTH_SHORT).show();
							return false;
						} else if (getListOfKeys().contains(key)) {
							Toast.makeText(getContext(),
									key + " already exists.",
									Toast.LENGTH_SHORT).show();
						} else {
							setValueAndKey(key, getDefaultValue(key));
						}
						M_KeyValue.this.key = key;
						keySelector.rebuildUi();
						startValueModifier.rebuildUi();
						return true;
					}

					@Override
					public String load() {
						return "";
					}

					@Override
					public String getVarName() {
						return "New identifier";
					}
				};
				final M_Container m = new M_Container(newIdentifierModifier);
				SimpleUI.showCancelOkDialog(arg0, "Cancel", "Ok", m);
			}
		};

		final M_HalfHalf halfHalf = new M_HalfHalf(keySelector, addButton);
		halfHalf.setWeightOfRight(0.8f);
		add(halfHalf);

		add(startValueModifier);
	}

	/**
	 * @param key
	 * @return The default value (used when a key is added).
	 */
	protected abstract float getDefaultValue(String key);

	@Override
	public boolean save() {
		super.save();
		setValueAndKey(key, value);
		return true;
	}

	/**
	 * @return The list of keys to be modified. If you want to edit a Map, use
	 *         {@link Map#keySet()}. Is not modified.
	 */
	protected abstract List<String> getListOfKeys();

	protected abstract void setValueAndKey(String key, float value);

	protected abstract float getValueForKey(String key);

	/**
	 * @return The name of your value (e.g. "Start value", "Default Value")
	 */
	protected abstract String getValueIdentifier();
}