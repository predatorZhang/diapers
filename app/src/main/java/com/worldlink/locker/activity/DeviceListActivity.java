package com.worldlink.locker.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


import com.dexafree.materialList.cards.SimpleCard;
import com.dexafree.materialList.cards.SmallImageCard;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;
import com.worldlink.locker.R;
import com.worldlink.locker.common.BleDeviceInfo;
import com.worldlink.locker.common.BleDeviceManager;
import com.worldlink.locker.services.BluetoothLeService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(R.layout.activity_devicelist)
public class DeviceListActivity extends BaseActivity {

	private static final String TAG = "DeviceListActivity";

	public static final String EXTRA_DEVICE = "EXTRA_DEVICE";

	@ViewById
	public MaterialListView lv_devices;

	//蓝牙相关操作
	private BluetoothLeService mBluetoothLeService = null;
	private boolean mScanning = false;
	private BluetoothAdapter mBtAdapter = null;
	private boolean mInitialised = false;
	private IntentFilter mFilter;
	private boolean mBleSupported = true;
	private static BluetoothManager mBluetoothManager;
	private BleDeviceManager bleDeviceManager;
	private BluetoothDevice mBluetoothDevice = null;


	// Requests to other activities
	private static final int REQ_ENABLE_BT = 0;
	private static final int REQ_DEVICE_ACT = 1;

	//消息推送receiver
	//TODO LIST：在设置推送是否使用的使用，接收广播
	public static final String BroadcastPushStyle = "BroadcastPushStyle";
	BroadcastReceiver mUpdatePushReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateNotifyService();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dl_menu, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Destroy");
		super.onDestroy();
		if (mBluetoothLeService != null) {
			scanLeDevice(false);
			mBluetoothLeService.close();
			unregisterReceiver(mReceiver);
			unbindService(mServiceConnection);
			mBluetoothLeService = null;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case R.id.action_add:
				/*Intent i = new Intent(this, ScanActivity.class);
				startActivity(i);*/
				if (mScanning) {
					stopScan();
				} else {
					startScan();
				}
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateNotifyService() {
		/*boolean needPush = AccountInfo.getNeedPush(this);

		if (needPush) {
			String globalKey = SensorHubApplication.sUserObject.global_key;
			XGPushManager.registerPush(this, globalKey);
		} else {
			XGPushManager.registerPush(this, "*");
		}*/
	}

	// 信鸽文档推荐调用，防止在小米手机上收不到推送
	private void pushInXiaomi() {
		Context context = getApplicationContext();
		Intent service = new Intent(context, XGPushService.class);
		context.startService(service);
	}

	@AfterViews
	public void init() {

		//初始化推送消息
		IntentFilter intentFilter = new IntentFilter(BroadcastPushStyle);
		registerReceiver(mUpdatePushReceiver, intentFilter);

//        XGPushConfig.enableDebug(this, true);
		// qq push8
		updateNotifyService();
		pushInXiaomi();


		bleDeviceManager = new BleDeviceManager(this);
		//判断蓝牙设备是否可用
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
			mBleSupported = false;
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to BluetoothAdapter through BluetoothManager.
		mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBtAdapter = mBluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBtAdapter == null) {
			Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_LONG).show();
			mBleSupported = false;
		}


		mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);

		//TODO LIST：打开蓝牙设置

		if (!mInitialised) {
			// Broadcast receiver
			registerReceiver(mReceiver, mFilter);

			if (mBtAdapter.isEnabled()) {
				// Start straight away
				startBluetoothLeService();
			} else {
				// Request BT adapter to be turned on
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQ_ENABLE_BT);
			}
			mInitialised = true;
		} else {
			updateMaterialView();
		}


		lv_devices.addOnItemTouchListener(new com.dexafree.materialList.controller.RecyclerItemClickListener.OnItemClickListener() {

			@Override
			public void onItemLongClick(CardItemView view, int position) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onItemClick(CardItemView view, int position) {
				// TODO Auto-generated method stub
				/*Intent i = new Intent(DeviceListActivity.this, MainActivity_.class);
				startActivity(i);*/
				BleDeviceInfo bleDeviceInfo = (BleDeviceInfo) view.getTag();
				if (mScanning)
					stopScan();
				mBluetoothDevice = bleDeviceInfo.getBluetoothDevice();
				if (bleDeviceManager.getmConnIndex() == BleDeviceManager.NO_DEVICE) {
					/*mScanView.setStatus("Connecting");*/
					bleDeviceManager.setmConnIndex(position);
					onConnect();
				} else {
/*
					mScanView.setStatus("Disconnecting");
*/
					if (bleDeviceManager.getmConnIndex()  != BleDeviceManager.NO_DEVICE) {
						mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
					}
				}


			}
		});

	}

	void onConnect() {
		if (bleDeviceManager.getNumOfDevice() > 0) {
			int connState = mBluetoothManager.getConnectionState(mBluetoothDevice, BluetoothGatt.GATT);
			switch (connState) {
				case BluetoothGatt.STATE_CONNECTED:
					mBluetoothLeService.disconnect(null);
					break;
				case BluetoothGatt.STATE_DISCONNECTED:
					boolean ok = mBluetoothLeService.connect(mBluetoothDevice.getAddress());
					if (!ok) {
/*
						setError("Connect failed");
*/
					}
					break;
				default:
/*
					setError("Device busy (connecting/disconnecting)");
*/
					break;
			}
		}
	}


	private void updateMaterialView() {

		lv_devices.clear();
		lv_devices.removeAllViews();
		List<BleDeviceInfo> bles = bleDeviceManager.getDevices();
		for (BleDeviceInfo ble : bles) {

			SimpleCard card1 = new SmallImageCard(this);
			card1.setTitle(ble.getBluetoothDevice().getAddress());
			card1.setDescription("信号强度Rssi：" + ble.getRssi());
			card1.setDrawable(R.drawable.icon_device);
			card1.setTag(ble);
			lv_devices.add(card1);

		}


	}


	// Activity result handling
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

			case REQ_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {

//					Toast.makeText(this, R.string.bt_on, Toast.LENGTH_SHORT).show();
				} else {
					// User did not enable Bluetooth or an error occurred
////					Toast.makeText(this, R.string.bt_not_on, Toast.LENGTH_SHORT).show();
//					finish();
				}
				break;
			default:
				Log.e(TAG, "Unknown request code");
				break;
		}
	}


	/**
	 * 开启蓝牙服务
	 */
	private void startBluetoothLeService() {

		boolean f;
		Intent bindIntent = new Intent(this, BluetoothLeService.class);
		startService(bindIntent);

		f = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
		if (f)
			Log.d(TAG, "BluetoothLeService - success");
		else {
			//CustomToast.middleBottom(this, "Bind to BluetoothLeService failed");
			finish();
		}
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize BluetoothLeService");
				finish();
				return;
			}
			final int n = mBluetoothLeService.numConnectedDevices();
			if (n > 0) {
				runOnUiThread(new Runnable() {
					public void run() {

						//TODO LIST:提示存在多个连接
/*
						mThis.setError("Multiple connections!");
*/
					}
				});
			} else {
				startScan();
				Log.i(TAG, "BluetoothLeService connected");
			}
		}

		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
			Log.i(TAG, "BluetoothLeService disconnected");
		}
	};

	/*
	开始扫描
	 */
	private void startScan() {
		// Start device discovery
		if (mBleSupported) {

			bleDeviceManager.clear();
			//TODO LIST:更新数据源
			updateMaterialView();
			scanLeDevice(true);
			//TODO LIST：更新界面为正在扫描的状态
//			mScanView.updateGui(mScanning);
			if (!mScanning) {
//				setError("Device discovery start failed");
//				setBusy(false);
			}
		} else {
//			setError("BLE not supported on this device");
		}

	}

	private void stopScan() {
		mScanning = false;
		scanLeDevice(false);
	}


	private boolean scanLeDevice(boolean enable) {
		if (enable) {
			mScanning = mBtAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBtAdapter.stopLeScan(mLeScanCallback);
		}
		return mScanning;
	}

	// Device scan callback.
	// NB! Nexus 4 and Nexus 7 (2012) only provide one scan result per scan
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				public void run() {
					// Filter devices
					bleDeviceManager.updateOrAdd(device, rssi);
					//TODO LIST:更新界面UI
					updateMaterialView();

				}

			});
		}
	};


	/**
	 * 处理三类action
	 * ACTION_STATE_CHANGED：蓝牙设备状态变化（开启，关闭）
	 * ACTION_GATT_CONNECTED：连接上设备
	 * ACTION_GATT_DISCONNECTED：设备断开
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			final String action = intent.getAction();

			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
			{
				// Bluetooth adapter state change
				switch (mBtAdapter.getState()) {
					case BluetoothAdapter.STATE_ON:
						bleDeviceManager.setmConnIndex(BleDeviceManager.NO_DEVICE);
						startBluetoothLeService();
						break;
					case BluetoothAdapter.STATE_OFF:
						Toast.makeText(context, "蓝牙已关闭", Toast.LENGTH_LONG).show();
/*
						getActivity().finish();
*/
						break;
					default:
						Log.w(TAG, "Action STATE CHANGED not processed ");
						break;
				}
				//TODO LIST:更新UI
				updateMaterialView();
			}
			else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
			{
				int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
				if (status == BluetoothGatt.GATT_SUCCESS)
				{

					Intent intentMain = new Intent(DeviceListActivity.this,MainActivity_.class);
					startActivity(intentMain);

				}
				else
				{
//					setError("Connect failed. Status: " + status);
				}
			}
			else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
			{
				// GATT disconnect
				int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);

				//TODO LIST：删除那个activity
				//stopDeviceActivity();

				if (status == BluetoothGatt.GATT_SUCCESS)
				{
//					setBusy(false);
//					mScanView.setStatus(mBluetoothDevice.getName() + " disconnected", STATUS_DURATION);
				} else
				{
//					setError("Disconnect failed. Status: " + status);
				}
				bleDeviceManager.setmConnIndex(BleDeviceManager.NO_DEVICE);
				mBluetoothLeService.close();
			} else {
				Log.w(TAG, "Unknown action: " + action);
			}
		}
	};

}