package com.example.practiceapp1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.location.LocationListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.practiceapp1.databinding.FragmentFirstBinding;

import android.telephony.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.List;
import java.util.Set;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    boolean _DEBUG_ = true;
    EditText duration, sampling_interval;
    Button start_button;
    private static final String TAG = "FirstFragment";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        start_button = (Button) view.findViewById(R.id.button_1);
        duration = (EditText) view.findViewById(R.id.duration_val);
        sampling_interval = (EditText) view.findViewById(R.id.samp_int_val);

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collectMeasurements(view, Integer.parseInt(duration.getText().toString()),
                        Integer.parseInt(sampling_interval.getText().toString()));
                /*
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                 */
            }
        });
        /*
        binding.button_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
         */
        view.findViewById(R.id.button_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast myToast = Toast.makeText(getActivity(), "Click Start first", Toast.LENGTH_SHORT);
                //myToast.show();
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        view.findViewById(R.id.button_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast myToast = Toast.makeText(getActivity(), "Click Start first", Toast.LENGTH_SHORT);
                //myToast.show();
                Context context = view.getContext().getApplicationContext();
                String[] files = context.fileList();
                System.out.println("Num files: " + files.length);
                File csvFile;

                for (int i=0; i<files.length; i++) {
                    if (files[i].endsWith(".csv")) {
                        //csvFile = new File(files[i]);
                        if (context.deleteFile(files[i])) {
                            System.out.println("Deleting file #" + (i + 1) + ": " + files[i]);
                        } else {
                            System.out.println("Failed to delete file #" + (i + 1) + ": " + files[i]);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void collectMeasurements(View view, int duration_secs, int sample_interval_secs) {
        Context context = view.getContext().getApplicationContext(); // Initialize application context
        TelephonyManager telephonyManager;
        TelephonyManager.CellInfoCallback cellInfoCallback;
        File appFilesDirectory = context.getExternalFilesDir(null);

        // INITIALIZE ConnectivityManager
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connectivity.getAllNetworks();
        int activeNetworkIndex = 0, idx=0;
        //AvailableNetworkInfo networkInfo;
        NetworkCapabilities capabilities = null;
        long dl_bw_kbps = -1;

        System.out.println("\n***STATIC ConnectivityManager MEASUREMENTS***\nNumber of networks: " + networks.length);
        for (Network mNetwork: networks) {
            capabilities = connectivity.getNetworkCapabilities(mNetwork);
            System.out.println("Network.toString(): " + mNetwork.toString() + ", Network.getNetworkHandle():" +
                    mNetwork.getNetworkHandle() + ", isCellular(): " +
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) + ", isNotCongested(): " +
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED));

            if (connectivity.getActiveNetwork().toString().equals(mNetwork.getNetworkHandle())) {
                activeNetworkIndex = idx;
            }

            System.out.println("DL BW: " + capabilities.getLinkDownstreamBandwidthKbps() +
                    ", UL BW: " + capabilities.getLinkUpstreamBandwidthKbps() + ", Signal Strength: " +
                    capabilities.getSignalStrength() + ", active network: " + connectivity.getActiveNetwork().toString());

            idx++; // increment index
        }
        System.out.println("ActiveNetworkIndex = " + activeNetworkIndex);

        // INITIALIZE TelephonyManager
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            Log.i(TAG, "ERROR: TelephonyManager is NULL.");
            return;
        }
        else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            cellInfoCallback = new TelephonyManager.CellInfoCallback() {
                @Override
                public void onCellInfo(List<CellInfo> cellInfoList) {
                    // DO SOMETHING
                    System.out.print("********TEST FOR CALLBACK: " + cellInfoList.size());
                    for (CellInfo cellInfo: cellInfoList) {
                        System.out.print("********TEST FOR CALLBACK: CI = " + ((CellIdentityLte)cellInfo.getCellIdentity()).getCi());
                    }
                }
            };

            List<CellSignalStrength> sigs = telephonyManager.getSignalStrength().getCellSignalStrengths();
            CellSignalStrengthLte lteSigStrength = null;
            CellIdentityLte ltecellID = null;
            CellInfoLte lteInfo = null;
            CellInfo info = null;
            long currTime = 0;
            long interval_start = 0;
            long endTime = 0;
            String measurement = null;
            boolean isServingCell;
            int elapsed_time = 0;
            //NetworkCapabilities nc = null;

            if (_DEBUG_) {
                System.out.println("SystemFeatures[telephony] = " + context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY));
                System.out.println("SystemFeatures[conn svc] = " + context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CONNECTION_SERVICE));
                System.out.println("SubscriptionManager.defaultSubscriptionID = " + SubscriptionManager.getDefaultDataSubscriptionId());
                System.out.println("SubscriptionManager.activeSubscriptionID = " + SubscriptionManager.getActiveDataSubscriptionId());
                System.out.println("TelephonyManager.hasCarrierPrivileges(): " + telephonyManager.hasCarrierPrivileges());
                System.out.println("TelephonyManager: DataActivity: " + telephonyManager.getDataActivity() +
                        ", DataState: " + telephonyManager.getDataState());
                try {
                    System.out.println("TelephonyManager.getNetworkType: " + telephonyManager.getDataNetworkType());
                } catch (Exception e) { e.printStackTrace(); }

                System.out.println("Number of cell sites: " + telephonyManager.getAllCellInfo().size());
                System.out.println("Network operator: " + telephonyManager.getNetworkOperator() +
                        ", name = " + telephonyManager.getNetworkOperatorName());
            }

            try {
                long cTime = System.currentTimeMillis();
                String csvFileName = "Cell_Data_" + cTime + ".csv";
                File passiveDataFile = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), "p" + csvFileName);
                File activeDataFile = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), "a" + csvFileName);
                OutputStreamWriter pOSW = null;
                OutputStreamWriter aOSW = null;
                String command[] = {"ping", "-c4", "youtube.com"};
                ProcessBuilder pb = null;
                Process p = null;
                BufferedReader in = null;
                String inputLine = null;
                String pingResult = null;
                float minRTT=0, maxRTT=0, avgRTT=0, mdevRTT=0, percentPacketLoss=0;
                int index, nextSlash;

                if (_DEBUG_) { System.out.println("Writing measurements out to files: " + csvFileName); }

                pOSW = new OutputStreamWriter(new FileOutputStream(passiveDataFile));
                pOSW.write("timestamp,rsrp,rsrq,rssnr,cqi,mcc,mnc,tac,pci,enbID,bw_khz,isServingCell" + "\n");

                aOSW = new OutputStreamWriter(new FileOutputStream(activeDataFile));
                aOSW.write("timestamp,minRTT,avgRTT,maxRTT,mdevRTT,pctPktLoss" + "\n");

                currTime = interval_start = System.currentTimeMillis();
                endTime = currTime + (duration_secs * 1000);
                if (_DEBUG_) { System.out.println("Current: " + currTime + ", Next: " + interval_start + ", End: " + endTime); }

                while (currTime <= endTime) {
                    while (currTime < interval_start) {
                        Thread.sleep(100); // sleep for 100 ms
                        currTime = System.currentTimeMillis();
                    }

                    /****
                     * COLLECT ACTIVE MEASUREMENTS
                     */
                    if (_DEBUG_) { System.out.println("\n***ACTIVE MEASUREMENTS***\n"); }

                    pb = new ProcessBuilder(command);
                    pb.redirectErrorStream(true);
                    p = pb.start();
                    in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("......" + inputLine);
                        if (inputLine.indexOf("received, ") >= 0) {
                            index = inputLine.indexOf("received, ") + "received, ".length();
                            percentPacketLoss = Float.parseFloat(inputLine.substring(index, inputLine.indexOf("%")));
                        }
                        else if (inputLine.indexOf("rtt min/avg/max/mdev = ") >= 0) {
                            pingResult = inputLine.substring("rtt min/avg/max/mdev = ".length(), inputLine.indexOf(" ms"));

                            if (pingResult != null) {
                                index = 0;
                                nextSlash = pingResult.indexOf("/");
                                minRTT = Float.parseFloat(pingResult.substring(0, nextSlash));
                                System.out.println("ping result - " + pingResult + ", index = " + nextSlash + ", minRTT: " + minRTT);

                                index = nextSlash + 1;
                                nextSlash = pingResult.indexOf("/", index);
                                avgRTT = Float.parseFloat(pingResult.substring(index, nextSlash));
                                System.out.println("ping result - " + pingResult + ", index = " + index + ", avgRTT: " + avgRTT);

                                index = nextSlash + 1;
                                nextSlash = pingResult.indexOf("/", index);
                                maxRTT = Float.parseFloat(pingResult.substring(index, nextSlash));
                                System.out.println("ping result - " + pingResult + ", index = " + index + ", maxRTT: " + maxRTT);

                                index = nextSlash + 1;
                                mdevRTT = Float.parseFloat(pingResult.substring(index));
                                System.out.println("ping result - " + pingResult + ", index = " + index + ", mdevRTT: " + mdevRTT);
                            }
                        }
                    }
                    if (_DEBUG_) {
                        aOSW.write( currTime + "," + minRTT + "," + avgRTT + "," + maxRTT + "," + mdevRTT + "," + percentPacketLoss + "\n");
                        System.out.println("->RTT - min: " + minRTT + ", avg: " + avgRTT +
                                ", max: " + maxRTT + ", mdev: " + mdevRTT + ", packet loss: " +
                                percentPacketLoss + "%");
                    }

                    in.close();

                    /****
                     * COLLECT PASSIVE MEASUREMENTS
                     */
                    if (_DEBUG_) { System.out.println("\n***PASSIVE MEASUREMENTS***\n"); }

                    for (int i=0; i<telephonyManager.getAllCellInfo().size(); i++) {
                        if (telephonyManager.getAllCellInfo().get(i).getClass().toString().equals("class android.telephony.CellInfoLte")) {
                            lteInfo = (CellInfoLte) telephonyManager.getAllCellInfo().get(i);
                            ltecellID = lteInfo.getCellIdentity();
                            lteSigStrength = lteInfo.getCellSignalStrength();
                            isServingCell = (lteInfo.getCellConnectionStatus() == CellInfo.CONNECTION_PRIMARY_SERVING);

                            if (_DEBUG_) {
                                System.out.println("Evaluating cell #" + (i + 1) + ", MNO: " + ltecellID.getMobileNetworkOperator());

                                System.out.println("Cell ID: " + ltecellID.getMobileNetworkOperator() + ", CI: " + ltecellID.getCi() +
                                        ", Earfcn: " + ltecellID.getEarfcn() + ", Pci: " + ltecellID.getPci() + ", TAC: " + ltecellID.getTac());
                                System.out.println("Cell ID: mcc = " + ltecellID.getMccString() + ", mnc = " + ltecellID.getMncString() + ", BW = " +
                                        ltecellID.getBandwidth() + ", dl BW = { " +
                                        connectivity.getNetworkCapabilities(connectivity.getAllNetworks()[activeNetworkIndex]).getLinkDownstreamBandwidthKbps() + " }");
                                System.out.println("Cell info: connectionStatus = " + lteInfo.getCellConnectionStatus());
                                //System.out.println("Cell signal strength: Dbu: " + lteInfo.getCellSignalStrength().getDbm() + ", ASU = " + lteInfo.getCellSignalStrength().getAsuLevel());
                            }

                            // Compose the passive measurement report
                            // Format: timestamp,rsrp,rsrq,rssnr,cqi,mcc,mnc,tac,pci,earfcn,enbID,bw_khz,isServingCell
                            measurement = currTime + "," + lteSigStrength.getRsrp() + "," + lteSigStrength.getRsrq() + "," +
                                    lteSigStrength.getRssnr() + "," + lteSigStrength.getCqi() + "," + ltecellID.getMccString() +
                                    "," + ltecellID.getMncString() + "," + ltecellID.getTac() + "," + ltecellID.getPci() +
                                    "," + ltecellID.getEarfcn() + "," + ltecellID.getCi() + "," + ltecellID.getBandwidth() +
                                    "," + connectivity.getNetworkCapabilities(connectivity.getAllNetworks()[activeNetworkIndex]).getLinkDownstreamBandwidthKbps() +
                                    "," + isServingCell;

                            telephonyManager.requestCellInfoUpdate(context.getMainExecutor(), cellInfoCallback);
                            if (_DEBUG_) {
                                System.out.println("pCSV write: " + measurement);
                            }

                            pOSW.write(measurement + "\n");
                        }
                        else { System.out.println("--> NON-LTE CELL FOUND: " + telephonyManager.getAllCellInfo().get(i).getClass().toString()); }
                    }
                    interval_start += (sample_interval_secs*1000);
                    currTime = System.currentTimeMillis();
                    if (_DEBUG_) { System.out.println("Current: " + currTime + ", Next: " + interval_start + ", End: " + endTime); }
                }

                // CLOSE CSV FILES
                pOSW.close();
                aOSW.close();
                Log.i(TAG, "EXITING. CLOSING FILES.");

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        else {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
    }
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "Location changed...");
            Log.i(TAG, "Latitude :        " + location.getLatitude());
            Log.i(TAG, "Longitude :       " + location.getLongitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };
}