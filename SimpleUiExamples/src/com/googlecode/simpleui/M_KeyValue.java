package com.googlecode.simpleui;

import java.util.ArrayList;
import java.util.List;

import simpleui.SimpleUI;
import simpleui.modifiers.v3.M_Button;
import simpleui.modifiers.v3.M_Container;
import simpleui.modifiers.v3.M_FloatModifier;
import simpleui.modifiers.v3.M_HalfHalf;
import simpleui.modifiers.v3.M_Spinner;
import simpleui.modifiers.v3.M_TextModifier;
import util.Log;
import android.content.Context;
import android.widget.Button;

public abstract class M_KeyValue extends M_Container {

	private static final String LOG_TAG = M_KeyValue.class
			.getSimpleName();

	public M_KeyValue() {
		final M_Container startValueModifier = new M_Container(
				new M_FloatModifier() {

					@Override
					public String getVarName() {
						return "Start Value for " + getKey();
					}

					@Override
					public boolean saveFloat(final float value) {
						setValueForKey(getKey(), value);
						return true;
					}

					@Override
					public float loadFloat() {
						final float value = getValueForKey(getKey());
						return value;
					}
				});

		final M_Container keySelector = new M_Container(new M_Spinner() {
			@Override
			public boolean save(final SpinnerItem arg0) {
				final String key = getKeys().get(arg0.getId());
				setKey(key);
				return true;
			}

			@Override
			public int loadSelectedItemId() {
				final int i = getKeys()
						.indexOf(getKey());
				if (i == -1) {
					return 0;
				} else {
					return i;
				}
			}

			@Override
			public List<SpinnerItem> loadListToDisplay() {
				final ArrayList<SpinnerItem> list = new ArrayList<SpinnerItem>();
				for (int i = 0; i < getKeys().size(); i++) {
					final String key = getKeys().get(i);
					list.add(new SpinnerItem(i, key));
				}
				return list;
			}

			@Override
			public String getVarName() {
				return "Value Identifier";
			}

			@Override
			protected void onSelect(final int posInList) {
				super.onSelect(posInList);
				startValueModifier.rebuildUi();
			}

		});

		final M_Button addButton = new M_Button("Add") {
			@Override
			public void onClick(final Context arg0, final Button arg1) {
				final M_TextModifier newIdentifierModifier = new M_TextModifier() {
					@Override
					public boolean save(final String key) {
						if (getKeys()
								.contains(key)) {
							onAddKeyAlreadyExists();
							return false;
						} else {
							setKey(key);
							keySelector.rebuildUi();
							startValueModifier.rebuildUi();
							return true;
						}
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
				newIdentifierModifier.setInfoText("Enter identifier");
				final M_Container m = new M_Container(newIdentifierModifier);
				SimpleUI.showCancelOkDialog(arg0, "Cancel", "Ok", m);
			}
		};

		add(new M_HalfHalf(keySelector, addButton));
		add(startValueModifier);
	}

	protected abstract void setValueForKey(String key, float value);

	protected abstract float getValueForKey(String key);

	protected abstract void setKey(String key);

	protected abstract String getKey();

	protected abstract List<String> getKeys();

	protected void onAddKeyAlreadyExists() {
		Log.d(LOG_TAG, "Key already exists.");
	}
}