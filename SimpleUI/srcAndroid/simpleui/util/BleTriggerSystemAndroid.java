package simpleui.util;

import simpleui.util.BleTriggerOnDistance.BleDeviceInRangeListener;
import android.bluetooth.BluetoothDevice;

/**
 * based on the id of a BLE device you can trigger commands. As soon as the user
 * gets close to a device the command is triggered once. when another device
 * gets in range the command for that other device is triggered
 */
public class BleTriggerSystemAndroid implements BleTriggerSystem {

	private static final String LOG_TAG = "BleTriggerSystemAndroid";
	private final BleTriggerOnDistance trigger;
	private String lastFoundBleDeviceId;
	private String lastFoundBleDeviceIdUuid;
	protected float lastFoundBleDistance = Float.MAX_VALUE;
	private Command commandToTriggerOnNextRefresh;
	private Command commandLastTriggered;

	public BleTriggerSystemAndroid(int updateSpeedInMs) {
		trigger = new BleTriggerOnDistance(updateSpeedInMs) {
			@Override
			protected void onScanReset() {
				if (commandToTriggerOnNextRefresh != null) {
					if (commandToTriggerOnNextRefresh != commandLastTriggered) {
						commandToTriggerOnNextRefresh.execute();
						commandLastTriggered = commandToTriggerOnNextRefresh;
					}
					commandToTriggerOnNextRefresh = null;
					lastFoundBleDistance = Float.MAX_VALUE;
				}
			}
		};
	}

	@Override
	public void addCommand(final String bleDeviceId, float maxRangeInPercent,
			final Command command) {
		Log.d(LOG_TAG, "added command");
		trigger.addBleDeviceFoundListener(bleDeviceId,
				new BleDeviceInRangeListener(maxRangeInPercent) {

					@Override
					public boolean onDeviceInRange(String deviceId,
							Integer deviceRssi, BluetoothDevice device,
							String uuid, long totalTimeRunningInMs,
							float currentRangeInPercent) {
						Log.d(LOG_TAG, "uuid found: " + uuid);
						if (currentRangeInPercent < lastFoundBleDistance) {
							if (bleDeviceId.contains(":")) {
								if (!deviceId.equals(lastFoundBleDeviceId)) {
									lastFoundBleDeviceId = deviceId;
								}
							} else {
								if (!deviceId.equals(lastFoundBleDeviceIdUuid)) {
									lastFoundBleDeviceIdUuid = deviceId;
								}
							}
							lastFoundBleDistance = currentRangeInPercent;
							commandToTriggerOnNextRefresh = command;

						}

						return true;
					}
				});
	}

	@Override
	public boolean isWatching() {
		return trigger.isWatching();
	}

	@Override
	public void startWatching() {
		trigger.startWatching();
	}

	@Override
	public void stopWatching() {
		trigger.stopWatching();
	}

	@Override
	public void resetCooldowns() {
		commandLastTriggered = null;

	}

}
