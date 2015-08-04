package com.tpw.homeshell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.IccCardConstants;
import android.telephony.SubscriptionManager;
//add by zoujianyu,Topwise.2015-5-26.set default data id if no set.bug 527.
public class SimStateReceiver extends BroadcastReceiver {
	private static final String TAG = "SimStateReceiver";
	private String[] mSimState = new String[TelephonyManager.getDefault().getSimCount()];

	public void onReceive(Context context, Intent intent) {
		int slotId = -1;
		int defaultSlotId = -1;

		if (intent.getAction().equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED) 
			&& SubscriptionManager.getDefaultDataSubId() == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {

			slotId = intent.getIntExtra(PhoneConstants.SLOT_KEY, -1);
			mSimState[slotId] = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);

			if (mSimState.length == 2) {
				if (IccCardConstants.INTENT_VALUE_ICC_READY.equals(mSimState[0])) {
					defaultSlotId = 0;
				} else if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(mSimState[0])
						&& IccCardConstants.INTENT_VALUE_ICC_READY.equals(mSimState[1])) {
					defaultSlotId = 1;
				}

				if (defaultSlotId >= 0) {
					int subId = getSubId(defaultSlotId);

					Log.e(TAG, "[BroadcastReceiver] receving ACTION_SIM_STATE_CHANGED"
						+ "  defaultSlotId:" + defaultSlotId + " subId:" + subId 
						+ "  defaultDataSubId:" + SubscriptionManager.getDefaultDataSubId());
					((SubscriptionManager)context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE))
						.setDefaultDataSubId(subId);
				}
			}

		}
	}

	private int getSubId(int slotId) {
		final int[] subIds = SubscriptionManager.getSubId(slotId);
		if (subIds != null && subIds.length > 0) {
			return subIds[0];
		}

		return SubscriptionManager.INVALID_SUBSCRIPTION_ID;
	}
}

