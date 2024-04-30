package com.example.flutter_bluetooth_pyj;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;

/** FlutterBluetoothPyjPlugin */
public class FlutterBluetoothPyjPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler, RequestPermissionsResultListener {
  private int id = 0;
  private static final String TAG = "FlutterBluetoothPyjPlugin";
  private Object initializationLock = new Object();
  private Context context;
  private ThreadPool threadPool;
  private String curMacAddress;

  private static final String NAMESPACE = "flutter_bluetooth_pyj";
  private MethodChannel channel;
  private EventChannel stateChannel;
  private BluetoothManager mBluetoothManager;
  private BluetoothAdapter mBluetoothAdapter;

  private FlutterPluginBinding pluginBinding;
  private ActivityPluginBinding activityBinding;
  private Application application;
  private Activity activity;

  private MethodCall pendingCall;
  private Result pendingResult;
  private static final int REQUEST_FINE_LOCATION_PERMISSIONS = 1452;

  private static String[] PERMISSIONS_LOCATION = {
          Manifest.permission.BLUETOOTH,
          Manifest.permission.BLUETOOTH_ADMIN,
          Manifest.permission.BLUETOOTH_CONNECT,
          Manifest.permission.BLUETOOTH_SCAN,
          Manifest.permission.ACCESS_FINE_LOCATION
  };

  public static void registerWith(Registrar registrar) {
    final com.example.flutter_bluetooth_pyj.FlutterBluetoothPyjPlugin instance = new com.example.flutter_bluetooth_pyj.FlutterBluetoothPyjPlugin();

    Activity activity = registrar.activity();
    Application application = null;
    if (registrar.context() != null) {
      application = (Application) (registrar.context().getApplicationContext());
    }
    instance.setup(registrar.messenger(), application, activity, registrar, null);
  }

  public FlutterBluetoothPyjPlugin(){
  }


  @Override
  public void onAttachedToEngine(FlutterPluginBinding binding) {
    pluginBinding = binding;
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding binding) {
    pluginBinding = null;
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    activityBinding = binding;
    setup(
            pluginBinding.getBinaryMessenger(),
            (Application) pluginBinding.getApplicationContext(),
            activityBinding.getActivity(),
            null,
            activityBinding);
  }

  @Override
  public void onDetachedFromActivity() {
    tearDown();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }

  private void setup(
          final BinaryMessenger messenger,
          final Application application,
          final Activity activity,
          final PluginRegistry.Registrar registrar,
          final ActivityPluginBinding activityBinding) {
    synchronized (initializationLock) {
      Log.i(TAG, "setup");
      this.activity = activity;
      this.application = application;
      this.context = application;
      channel = new MethodChannel(messenger, NAMESPACE + "/methods");
      channel.setMethodCallHandler(this);
      stateChannel = new EventChannel(messenger, NAMESPACE + "/state");
      stateChannel.setStreamHandler(stateHandler);
      mBluetoothManager = (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE);
      mBluetoothAdapter = mBluetoothManager.getAdapter();
      if (registrar != null) {
        // V1 embedding setup for activity listeners.
        registrar.addRequestPermissionsResultListener(this);
      } else {
        // V2 embedding setup for activity listeners.
        activityBinding.addRequestPermissionsResultListener(this);
      }
    }
  }

  private void tearDown() {
    Log.i(TAG, "teardown");
    context = null;
    activityBinding.removeRequestPermissionsResultListener(this);
    activityBinding = null;
    channel.setMethodCallHandler(null);
    channel = null;
    stateChannel.setStreamHandler(null);
    stateChannel = null;
    mBluetoothAdapter = null;
    mBluetoothManager = null;
    application = null;
  }


  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (mBluetoothAdapter == null && !"isAvailable".equals(call.method)) {
      result.error("bluetooth_unavailable", "Bluetooth is unavailable", null);
      return;
    }

    final Map<String, Object> args = call.arguments();

    switch (call.method){
      case "state":
        state(result);
        break;
      case "isAvailable":
        result.success(mBluetoothAdapter != null);
        break;
      case "isOn":
        result.success(mBluetoothAdapter.isEnabled());
        break;
      case "isConnected":
        result.success(threadPool != null);
        break;
      case "startScan":
      {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
          ActivityCompat.requestPermissions(activityBinding.getActivity(), PERMISSIONS_LOCATION, REQUEST_FINE_LOCATION_PERMISSIONS);
          pendingCall = call;
          pendingResult = result;
          break;
        }

        startScan(call, result);
        break;
      }
      case "stopScan":
        stopScan();
        result.success(null);
        break;
      case "connect":
        connect(result, args);
        break;
      case "disconnect":
        result.success(disconnect());
        break;
      case "destroy":
        result.success(destroy());
        break;
      case "writeData":
        writeData(result, args);
        break;
      default:
        result.notImplemented();
        break;
    }

  }

  private void getDevices(Result result){
    List<Map<String, Object>> devices = new ArrayList<>();
    for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
      Map<String, Object> ret = new HashMap<>();
      ret.put("address", device.getAddress());
      ret.put("name", device.getName());
      ret.put("type", device.getType());
      devices.add(ret);
    }

    result.success(devices);
  }

  /**
   * 获取状态
   */
  private void state(Result result){
    try {
      switch(mBluetoothAdapter.getState()) {
        case BluetoothAdapter.STATE_OFF:
          result.success(BluetoothAdapter.STATE_OFF);
          break;
        case BluetoothAdapter.STATE_ON:
          result.success(BluetoothAdapter.STATE_ON);
          break;
        case BluetoothAdapter.STATE_TURNING_OFF:
          result.success(BluetoothAdapter.STATE_TURNING_OFF);
          break;
        case BluetoothAdapter.STATE_TURNING_ON:
          result.success(BluetoothAdapter.STATE_TURNING_ON);
          break;
        default:
          result.success(0);
          break;
      }
    } catch (SecurityException e) {
      result.error("invalid_argument", "argument 'address' not found", null);
    }

  }


  private void startScan(MethodCall call, Result result) {
    Log.d(TAG,"start scan ");

    try {
      startScan();
      result.success(null);
    } catch (Exception e) {
      result.error("startScan", e.getMessage(), e);
    }
  }

  private void invokeMethodUIThread(final String name, final BluetoothDevice device)
  {
    final Map<String, Object> ret = new HashMap<>();
    ret.put("address", device.getAddress());
    ret.put("name", device.getName());
    ret.put("type", device.getType());

    activity.runOnUiThread(
            new Runnable() {
              @Override
              public void run() {
                channel.invokeMethod(name, ret);
              }
            });
  }

  private ScanCallback mScanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      BluetoothDevice device = result.getDevice();
      if(device != null && device.getName() != null){
        invokeMethodUIThread("ScanResult", device);
      }
    }
  };

  private void startScan() throws IllegalStateException {
    BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
    if(scanner == null) {
      throw new IllegalStateException("getBluetoothLeScanner() is null. Is the Adapter on?");
    }

    // 0:lowPower 1:balanced 2:lowLatency -1:opportunistic
    ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    scanner.startScan(null, settings, mScanCallback);
  }

  private void stopScan() {
    BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
    if(scanner != null) {
      scanner.stopScan(mScanCallback);
    }
  }

  /**
   * 连接
   */
  private void connect(Result result, Map<String, Object> args){
    if (args.containsKey("address")) {
      final String address = (String) args.get("address");
      this.curMacAddress = address;

      disconnect();

      new DeviceConnFactoryManager.Build()
              .setId(id)
              //设置连接方式
              .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
              //设置连接的蓝牙mac地址
              .setMacAddress(address)
              .build();

      //打开端口
      threadPool = ThreadPool.getInstantiation();
      threadPool.addSerialTask(new Runnable() {
        @Override
        public void run() {
          DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
        }
      });

      result.success(true);
    } else {
      result.error("******************* invalid_argument", "argument 'address' not found", null);
    }

  }

  /**
   * 关闭连接
   */
  private boolean disconnect(){
    if(DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id]!=null&&DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort!=null) {
      DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].reader.cancel();
      DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort.closePort();
      DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort=null;
    }
    return true;
  }

  private boolean destroy() {
    DeviceConnFactoryManager.closeAllPort();
    if (threadPool != null) {
      threadPool.stopThreadPool();
    }

    return true;
  }


  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    if (requestCode == REQUEST_FINE_LOCATION_PERMISSIONS) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        startScan(pendingCall, pendingResult);
      } else {
        pendingResult.error("no_permissions", "this plugin requires location permissions for scanning", null);
        pendingResult = null;
      }
      return true;
    }
    return false;

  }



  private final StreamHandler stateHandler = new StreamHandler() {
    private EventSink sink;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "stateStreamHandler, current action: " + action);

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
          threadPool = null;
          sink.success(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1));
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
          sink.success(1);
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
          threadPool = null;
          sink.success(0);
        }
      }
    };

    @Override
    public void onListen(Object o, EventSink eventSink) {
      sink = eventSink;
      IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
      filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
      filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
      filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
      context.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onCancel(Object o) {
      sink = null;
      context.unregisterReceiver(mReceiver);
    }
  };

  @SuppressWarnings("unchecked")
  private void writeData(Result result, Map<String, Object> args) {
    if (args.containsKey("bytes")) {
      final ArrayList<Integer> bytes = (ArrayList<Integer>)args.get("bytes");

      threadPool = ThreadPool.getInstantiation();
      threadPool.addSerialTask(new Runnable() {
        @Override
        public void run() {
          Vector<Byte> vectorData = new Vector<>();
          for(int i = 0; i < bytes.size(); ++i) {
            Integer val = bytes.get(i);
            vectorData.add(Byte.valueOf( Integer.toString(val > 127 ? val-256 : val ) ));
          }

          DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(vectorData);
        }
      });
    } else {
      result.error("bytes_empty", "Bytes param is empty", null);
    }
  }
}