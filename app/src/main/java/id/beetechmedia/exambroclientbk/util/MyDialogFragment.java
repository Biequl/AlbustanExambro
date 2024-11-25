package id.beetechmedia.exambroclientbk.util;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * Created by Sabiqul on 23/11/2024.
 * BeeMedia
 * sabiqul.ulum@gmail.com
 */
public class MyDialogFragment extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Versi Aplikasi");
        builder.setMessage("2024 :\nSabiqul\n\nCopyright © 2020\nTim Teknis HD Pati 2020");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        return builder.create();
    }
}
