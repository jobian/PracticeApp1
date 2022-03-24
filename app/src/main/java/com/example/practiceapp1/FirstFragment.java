package com.example.practiceapp1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
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
    File csv_file = null;
    EditText duration, sampling_interval;
    Button start_button;

    private static final String csvFileName = "sCell_" + System.currentTimeMillis() + ".csv";
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
                Toast myToast = Toast.makeText(getActivity(), "Click Start first", Toast.LENGTH_SHORT);
                myToast.show();
            }
        });

        view.findViewById(R.id.button_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast myToast = Toast.makeText(getActivity(), "Click Start first", Toast.LENGTH_SHORT);
                myToast.show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void collectMeasurements(View view, int duration_secs, int sample_interval_secs) {
        List<CellInfo> cellInfoList = null;
        Context context = view.getContext().getApplicationContext();
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(context.openFileOutput(csvFileName, 0));
            osw.write("timestamp,rsrp,rsrq,rssnr,cqi,mcc,mnc,tac,pci,enbID,band" + "\n");
        } catch (IOException fnfe) {
            fnfe.printStackTrace();
            return;
        }

        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            Log.i(TAG, "ERROR: TelephonyManager is NULL.");
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            List<CellSignalStrength> sigs = telephonyManager.getSignalStrength().getCellSignalStrengths();
            //cellInfoList = telephonyManager.getAllCellInfo();
            List<CellSignalStrength> sig_list = null;
            CellSignalStrengthLte siglte = null;
            CellIdentityLte ltecell = null;
            int elapsed_time = 0;
            int mcc = context.getResources().getConfiguration().mcc;
            int mnc = context.getResources().getConfiguration().mnc;
            System.out.println("MNO: " + telephonyManager.getNetworkOperatorName() + ", mcc=" + mcc + ", mnc=" + mnc);

            try {
                if (true) {
                    if (sigs.get(0).getClass().toString().equals("class android.telephony.CellSignalStrengthLte")) {
                        //ltecell = (CellIdentityLte)((CellInfoLte) cellInfoList.get(0)).getCellIdentity();
                        siglte = (CellSignalStrengthLte) sigs.get(0);
                        while (elapsed_time <= duration_secs) {
                            // format: timestamp,rsrp,rsrq,rssnr,cqi,mcc,mnc,tac,pci,enbID,band
                            System.out.println("CSV write: " + System.currentTimeMillis() + "," +
                                    siglte.getRsrp() + "," + siglte.getRsrq() + "," +
                                    siglte.getRssnr() + "," + siglte.getCqi() + "," +
                                    mcc + "," + mnc + "," + "tac" + "," + "pci" + "  samp_int=" + sample_interval_secs);
                            //ltecell.getTac() + "," + ltecell.getPci());

                            osw.write(System.currentTimeMillis() + "," + siglte.getRsrp() + "," +
                                    siglte.getRsrq() + "," + siglte.getRssnr() + "," + siglte.getCqi() +
                                    "," + mcc + "," + mnc + "\n");

                            Thread.sleep(sample_interval_secs * 1000);
                            elapsed_time += sample_interval_secs;
                        }
                    }
                }
                osw.close();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        /*
        cellInfoList = telephonyManager.getAllCellInfo();
        if (cellInfoList == null) {
            Log.i(TAG, "CellInfoList is NULL...");
        } else if (cellInfoList.size() == 0) {
            Log.i(TAG, "CellInfoList is EMPTY...");
        }

        if (cellInfoList == null) {
            tv.setText("getAllCellInfo()返回null");
        } else if (cellInfoList.size() == 0) {
            tv.setText("基站列表为空");
        } else {
            int cellNumber = cellInfoList.size();
            BaseStation main_BS = bindData(cellInfoList.get(0));
            tv.setText("获取到" + cellNumber + "个基站, \n主基站信息：\n" + main_BS.toString());
            for (CellInfo cellInfo : cellInfoList) {
                BaseStation bs = bindData(cellInfo);
                Log.i(TAG, bs.toString());
            }
        }

        CellInfoLte cellInfo = (CellInfoLte) cellInfoList.get(0);
        CellIdentityLte cellID = cellInfo.getCellIdentity();
        int dl_bw = cellID.getBandwidth();
        int earfcn = cellID.getEarfcn();
        int[] bands = cellID.getBands();
        String mcc_mnc = cellID.getMccString() + "-" + cellID.getMncString();
        String mno = cellID.getMobileNetworkOperator();
        int pci = cellID.getPci();
        int tac = cellID.getTac();
        System.out.println("BW: " + dl_bw);
        System.out.println("EARFCN: " + earfcn);
        System.out.println("MNO: " + mno);
        System.out.println("PCI: " + pci);
        System.out.println("TAC: " + tac);
        Toast myToast = Toast.makeText(getActivity(), tmp_string + ", " + mcc_mnc, Toast.LENGTH_SHORT);
        myToast.show();

        Set<String> plmns = cellID.getAdditionalPlmns();
        System.out.println("Num of PLMNs: " + plmns.size());
        */
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