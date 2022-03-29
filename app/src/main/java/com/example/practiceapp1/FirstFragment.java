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
import android.net.NetworkCapabilities;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.List;
import java.util.Set;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private TelephonyManager telephonyManager;
    private LocationManager locationManager;
    OutputStreamWriter osw = null;
    boolean _DEBUG_ = true;
    EditText duration, sampling_interval;
    Button start_button;

    private String csvFileName = null;
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
        Context context = view.getContext().getApplicationContext();
        File appFilesDirectory = context.getExternalFilesDir(null);
        System.out.println("External storage state: " + Environment.getExternalStorageState());
        csvFileName = "Cell_Data_" + System.currentTimeMillis() + ".csv";
        File csvFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), csvFileName);

        OutputStreamWriter osw = null;

        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            Log.i(TAG, "ERROR: TelephonyManager is NULL.");
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            List<CellSignalStrength> sigs = telephonyManager.getSignalStrength().getCellSignalStrengths();
            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            List<CellSignalStrength> sig_list = null;
            CellSignalStrengthLte siglte = null;
            CellIdentityLte ltecell = null;
            CellInfoLte infoLte = null;
            CellInfo info = null;
            long currTime = 0;
            long interval_start = 0;
            long endTime = 0;
            String measurement = null;
            boolean isServingCell;
            int elapsed_time = 0;

            if (_DEBUG_) { System.out.println("Number of cell sites: " + cellInfoList.size()); }

            try {
                if (_DEBUG_) { System.out.println("Writing out to file: " + csvFileName); }
                //osw = new OutputStreamWriter(context.openFileOutput(csvFileName, 0));
                osw = new OutputStreamWriter(new FileOutputStream(csvFile));
                osw.write("timestamp,rsrp,rsrq,rssnr,cqi,mcc,mnc,tac,pci,enbID,bw_khz,isServingCell" + "\n");

                currTime = interval_start = System.currentTimeMillis();
                endTime = currTime + (duration_secs * 1000);
                if (_DEBUG_) { System.out.println("Current: " + currTime + ", Next: " + interval_start + ", End: " + endTime); }

                while (currTime <= endTime) {
                    while (currTime < interval_start) {
                        Thread.sleep(100); // sleep for 100 ms
                        currTime = System.currentTimeMillis();
                    }

                    /****
                     * COLLECT PASSIVE MEASUREMENTS
                     */
                    for (int i=0; i<cellInfoList.size(); i++) {
                        if (!cellInfoList.get(i).getClass().toString().equals("class android.telephony.CellInfoLte")) {
                            return;
                        }

                        infoLte = (CellInfoLte) cellInfoList.get(i);
                        ltecell = infoLte.getCellIdentity();
                        siglte = infoLte.getCellSignalStrength();
                        //isServingCell = (infoLte.getCellConnectionStatus() == CellInfo.CONNECTION_PRIMARY_SERVING);
                        isServingCell = ltecell.getMobileNetworkOperator() != null;
                        if (_DEBUG_) { System.out.println("Evaluating cell #" + (i + 1) + ", MNO: " + ltecell.getMobileNetworkOperator()); }

                        measurement = currTime + "," + siglte.getRsrp() + "," + siglte.getRsrq() + "," +
                                siglte.getRssnr() + "," + siglte.getCqi() + "," + ltecell.getMccString() +
                                "," + ltecell.getMncString() + "," + ltecell.getTac() + "," +
                                ltecell.getPci() + "," + ltecell.getBandwidth() + "," + isServingCell;

                        // format: timestamp,rsrp,rsrq,rssnr,cqi,mcc,mnc,tac,pci,enbID,bw_khz,isServingCell
                        if (_DEBUG_) { System.out.println("CSV write: " + measurement); }

                        osw.write(measurement + "\n");
                    }
                    interval_start += (sample_interval_secs*1000);
                    currTime = System.currentTimeMillis();
                    if (_DEBUG_) { System.out.println("Current: " + currTime + ", Next: " + interval_start + ", End: " + endTime); }
                }
                osw.close();
                Log.i(TAG, "EXITING. CLOSING FILE.");
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

    public void sendEmail() {
        try {
            String email = "dldavis@vt.edu";
            String subject = "Data file: " + csvFileName;
            String message = "See attached CSV file.";
            Uri URI = null;
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
            if (URI != null) {
                emailIntent.putExtra(Intent.EXTRA_STREAM, URI);
            }
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
            this.startActivity(Intent.createChooser(emailIntent, "Sending email..."));
        } catch (Throwable t) {
        }
    }

}