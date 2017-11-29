package com.hemaapp.hm_FrameWork.util;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;


/**
 * 获取硬件标识码
 */
public class DeviceUuidFactory {
	private static final String PREFS_DEVICE_ID = "device_id";
	private static UUID uuid;

	public static String get(Context context) {
		if (uuid == null) {
			String id = SharedPreferencesUtil.get(context, PREFS_DEVICE_ID);
			if (id != null) {
				// Use the ids previously computed and stored in the
				// prefs file
				uuid = UUID.fromString(id);
			} else {
				final String androidId = Secure.getString(
						context.getContentResolver(), Secure.ANDROID_ID);
				// Use the Android ID unless it's broken, in which case
				// fallback on deviceId,
				// unless it's not available, then fallback on a random
				// number which we store
				// to a prefs file
				try {
					if (!"9774d56d682e549c".equals(androidId)) {
						uuid = UUID.nameUUIDFromBytes(androidId
								.getBytes("utf8"));
					} else {
						final String deviceId = ((TelephonyManager) context
								.getSystemService(Context.TELEPHONY_SERVICE))
								.getDeviceId();
						uuid = deviceId != null ? UUID
								.nameUUIDFromBytes(deviceId.getBytes("utf8"))
								: UUID.randomUUID();
					}
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				// Write the value out to the prefs file
				SharedPreferencesUtil.save(context, PREFS_DEVICE_ID,
						uuid.toString());
			}
		}
		return uuid.toString();
	}

}
