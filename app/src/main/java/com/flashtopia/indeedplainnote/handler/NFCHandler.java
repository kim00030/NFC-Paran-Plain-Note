package com.flashtopia.indeedplainnote.handler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flashtopia.indeedplainnote.R;
import com.flashtopia.indeedplainnote.components.MyCustomToast;
import com.flashtopia.indeedplainnote.data.NFCData;

import java.io.File;
import java.io.FileOutputStream;

/*
* This is singleton class to manage NFC. It implements CreateBeamUrisCallback
* because I use Android Beam to send data as file
*
* */
public class NFCHandler  implements NfcAdapter.CreateBeamUrisCallback{

    private static NFCHandler instance;
    //File name that will be created in external storage and send it to target device.
    private static final String FILE_NAME = "myeditorialData.txt";
    private File fileToTransfer = null;
    //reference of Alert dialog showing steps of NFC before sending file out
    private  AlertDialog nfcAlert;

    public static NFCHandler getInstance(){

        if (instance == null){
            instance = new NFCHandler();
        }
        return instance;
    }

    /*
    * Method to open  Alert dialog showing steps of NFC before sending file out
    * */
    private void openNFCGuideDialog(Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        nfcAlert = builder.create();
        //Create my custom layout and add it to this AlertDialog
        LayoutInflater inflater = LayoutInflater.from(activity);
        final View view = inflater.inflate(R.layout.nfc_dialog, null);
        ImageView image = (ImageView) view.findViewById(R.id.iv_nfc_talk);
        image.setImageResource(R.drawable.nfc_talk);

        TextView tv = (TextView) view.findViewById(R.id.tv_nfc_talk);
        tv.setText(R.string.note_is_ready_to_be_send);

        nfcAlert.setView(view);

        nfcAlert.show();

    }
    //***************************test for dialog for NFC
    /*public void sendFileToConnectedDevice2(NFCData nfcData) {
        Activity activity = nfcData.getActivity();
        openNFCGuideDialog(activity);

    }*/
    /*
    * Method send note as file to target device
    * */
    public void sendFileToConnectedDevice(NFCData nfcData) {

        //get reference of Activity object
        Activity activity = nfcData.getActivity();
        //get content of note
        String contentToBeSend = nfcData.getContentToBeSend();
        // Helper to get the default NFC Adapter.
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        //if note is empty
        if (contentToBeSend.length()<=0){
            MyCustomToast.getInstance().show(activity, activity.getString(R.string.enter_your_note_here),Toast.LENGTH_SHORT);
            return;
        }
        //if nfcAdapter is null. means the device has no NFC feature
        if (nfcAdapter == null){
            MyCustomToast.getInstance().show(activity, activity.getString(R.string.your_device_is_not_available_for_nfc), Toast.LENGTH_SHORT);
            return;
        }
        //Device Version check. device has to be at least Jelly Bean (API 4.2)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
            MyCustomToast.getInstance().show(activity, activity.getString(R.string.your_device_is_not_supported_for_nfc), Toast.LENGTH_SHORT);
            return;
        }

        if (!nfcAdapter.isEnabled()) {
            // NFC is disabled, show the settings UI to enable NFC
            MyCustomToast.getInstance().show(activity, activity.getString(R.string.please_enable_nfc), Toast.LENGTH_SHORT);
            //go to NFC setting
            activity.startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        else {// Check whether Android Beam feature is enabled on device
            if (!nfcAdapter.isNdefPushEnabled()) {

                // Android Beam is disabled, show the settings UI
                // to enable Android Beam
                MyCustomToast.getInstance().show(activity, activity.getString(R.string.please_enable_android_beam), Toast.LENGTH_SHORT);
                activity.startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));

            } else {
                //check External storage readable availability
                if (!isExternalStorageReadable()) {
                    MyCustomToast.getInstance().show(activity, activity.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT);
                    return;
                }

                //Create file that has content of note
                try {

                    File exdir = activity.getExternalFilesDir(null);
                    fileToTransfer = new File(exdir, FILE_NAME);
//                    fileToTransfer.setReadable(true, false);
                    FileOutputStream fos = new FileOutputStream(fileToTransfer);
                    fos.write(contentToBeSend.getBytes());
                    fos.close();


                } catch (Exception e) {
                    e.printStackTrace();
                    MyCustomToast.getInstance().show(activity, e.getMessage(), Toast.LENGTH_LONG);
                    return;
                }

                if (fileToTransfer != null){
                    nfcAdapter.setBeamPushUrisCallback(this, activity);
                    openNFCGuideDialog(activity);
                }


            }
        }
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {

        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /*
    *
    * Callback from calling nfcAdapter.setBeamPushUrisCallback(this, activity);
    * when user tap the devices each other, it calls
    * */
    @Override
    public Uri[] createBeamUris(NfcEvent event) {
        //Alert Dialog showing NFC reminder dismisses
        if (nfcAlert != null){
            nfcAlert.dismiss();
            nfcAlert = null;
        }
       return new Uri[]{Uri.fromFile(fileToTransfer)};
    }

}
