package com.smartfair.smartfairdummy;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.ScanMode;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.exception.ScanError;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.ScanStatusListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleScanStatusListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.spec.EddystoneFrameType;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.log.LogLevel;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

public class BeaconListActivity extends AppCompatActivity {

    private ProximityManager proximityManager;
    private final String key = "cQYMOSFPqzYscSKSbFyztLAoJSHfjjeD";
    private static Map<String, BeaconData> beaconsMap;

    //??
    private final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KontaktSDK.initialize(key).setDebugLoggingEnabled(BuildConfig.DEBUG).setLogLevelEnabled(LogLevel.DEBUG, true);
        proximityManager = new ProximityManager(this);
        configureProximityManager();
        configureListeners();
        configureSpaces();
        configureFilters();
        activateBLE();
    }

    private void activateBLE() {
       /* int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        int i = 0;

    }

    private void configureProximityManager() {
        proximityManager.configuration()
                .scanMode(ScanMode.BALANCED)
                .scanPeriod(ScanPeriod.create(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(20)))
                .activityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                .eddystoneFrameTypes(EnumSet.of(EddystoneFrameType.UID, EddystoneFrameType.URL, EddystoneFrameType.TLM));
    }

    private void configureListeners() {
        proximityManager.setIBeaconListener(createIBeaconListener());
        proximityManager.setEddystoneListener(createEddystoneListener());
        proximityManager.setScanStatusListener(createScanStatusListener());
    }

    private void configureSpaces() {
        // Necesario si queres filtrar, sino te busca todos
        /*IBeaconRegion region = new BeaconRegion.Builder()
                .setIdentifier("All my iBeacons")
                .setProximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e"))
                .build();

        proximityManager.spaces().iBeaconRegion(region);*/

        /*
        Collection<IEddystoneNamespace> eddystoneNamespaces = new ArrayList<>();
        eddystoneNamespaces.add(EddystoneNamespace.create("namespace beacons","f7826da6bc5b71e0893e", false));
        proximityManager.spaces().eddystoneNamespaces(eddystoneNamespaces);*/

    }

    private void configureFilters() {
        //proximityManager.filters().iBeaconFilter(IBeaconFilters.newDeviceNameFilter("JonSnow"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        startScanning();
        beaconsMap = new HashMap<>();
    }

    @Override
    protected void onStop() {
        proximityManager.stopScanning();

        beaconsMap.clear();
        beaconsMap = null;

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        proximityManager.disconnect();
        proximityManager = null;

        super.onDestroy();
    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BeaconListActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
                super.onIBeaconDiscovered(ibeacon, region);
                Log.i("Sample", "IBeacon discovered! UniqueID: " + ibeacon.getUniqueId());

                BeaconData bd = (BeaconData) beaconsMap.get(ibeacon.getUniqueId());

                if (bd != null) {
                    bd.addMeasure(ibeacon.getDistance());
                } else {
                    bd = new BeaconData();
                    bd.setId(ibeacon.getUniqueId());
                    bd.addMeasure(Math.round(ibeacon.getDistance() * 100.0) / 100.0);
                    beaconsMap.put(bd.getId(), bd);
                }

                updateTable();
            }
        };
    }

    private EddystoneListener createEddystoneListener() {
        return new SimpleEddystoneListener() {
            @Override
            public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                super.onEddystoneDiscovered(eddystone, namespace);
                Log.i("Sample", "Eddystone discovered! UniqueID: " + eddystone.getUniqueId());

                BeaconData bd = (BeaconData) beaconsMap.get(eddystone.getUniqueId());

                if (bd != null) {
                    bd.addMeasure(eddystone.getDistance());
                } else {
                    bd = new BeaconData();
                    bd.setId(eddystone.getUniqueId());
                    bd.addMeasure(Math.round(eddystone.getDistance() * 100.0) / 100.0);
                    beaconsMap.put(bd.getId(), bd);
                }

                updateTable();
            }
        };
    }

    private ScanStatusListener createScanStatusListener() {
        return new SimpleScanStatusListener() {
            //@Override
            public void onScanStart() {
                showToast("Scanning started");
            }

            //@Override
            public void onScanStop() {
                showToast("Scanning stopped");
            }

            @Override
            public void onScanError(ScanError error) {
                showToast("Error scanning: " + error.getMessage());
            }

        };
    }

    public void scanBeacons(View view) {
        TableLayout beaconsTable = (TableLayout)findViewById(R.id.beaconList);
        beaconsTable.removeAllViews();

        Iterator it = beaconsMap.keySet().iterator();
        while (it.hasNext()) {
            String uniqueId = (String) it.next();

            TableRow tr = new TableRow(this);
            tr.setId(uniqueId.hashCode());
            tr.setLayoutParams(
                    new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                                                TableRow.LayoutParams.WRAP_CONTENT));

            TextView name = new TextView(this);
            name.setTextColor(Color.parseColor("#ffa500"));
            name.setText(uniqueId + " ");
            name.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            name.setBackgroundColor(Color.parseColor("#000000"));
            name.setPadding(10, 0, 0, 0);
            tr.addView(name, 0);

            TextView distance = new TextView(this);
            distance.setTextColor(Color.parseColor("#ffa500"));
            distance.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            distance.setText(beaconsMap.get(uniqueId).toString() + " (m)");
            distance.setBackgroundColor(Color.parseColor("#000000"));
            tr.addView(distance, 1);

            beaconsTable.addView(tr, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        }
    }

    private static void updateTable() {
        // calcular distancia y refrescar table
        calculatePosition();

    }

    private static void calculatePosition() {

        Set beacons = beaconsMap.keySet();

        if (beacons.size() == 2) {
            //
        } else if (beacons.size() > 2) {
            /*

            let M:
            Double = (beaconB.position.x - beaconC.position.x) * (-pow(beaconA.position.x, 2) + pow(beaconB.position.x, 2) - pow(beaconA.position.y, 2) + pow(beaconB.position.y, 2) + pow(beaconA.accuracy, 2) - pow(beaconB.accuracy, 2))
            let N:
            Double = (beaconA.position.x - beaconB.position.x) * (-pow(beaconB.position.x, 2) + pow(beaconC.position.x, 2) - pow(beaconB.position.y, 2) + pow(beaconC.position.y, 2) + pow(beaconB.accuracy, 2) - pow(beaconC.accuracy, 2))
            let y:
            Double = (M - N) / (((beaconA.position.y - beaconB.position.y) * (beaconB.position.x - beaconC.position.x) - (beaconB.position.y - beaconC.position.y) * (beaconA.position.x - beaconB.position.x)) * 2)
            let x1:
            Double = (-pow(beaconA.position.x, 2) + pow(beaconB.position.x, 2) - 2 * y * (beaconA.position.y - beaconB.position.y) - pow(beaconA.position.y, 2) + pow(beaconB.position.y, 2) + pow(beaconA.accuracy, 2) - pow(beaconB.accuracy, 2)) / (-2 * (beaconA.position.x - beaconB.position.x))
            let x2:
            Double = (-pow(beaconB.position.x, 2) + pow(beaconC.position.x, 2) - 2 * y * (beaconB.position.y - beaconC.position.y) - pow(beaconB.position.y, 2) + pow(beaconC.position.y, 2) + pow(beaconB.accuracy, 2) - pow(beaconC.accuracy, 2)) / (-2 * (beaconB.position.x - beaconC.position.x))
            let x:Double = (x1 + x2) / 2*/
        }
    }
}
