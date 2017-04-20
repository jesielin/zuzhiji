package com.zzj.zuzhiji.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.zzj.zuzhiji.R;

/**
 * Created by shawn on 2017-03-28.
 */

public class DialogUtils {

    public static ProgressDialog showProgressDialog(Context context, String message) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);

        progressDialog.show();

        return progressDialog;
    }

    public static void dismissDialog(Dialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public static Dialog showSingleChoiceDialog(Context context, String title, String[] contents, AdapterView.OnItemClickListener listener) {
        Dialog singleChoiceDialog = new Dialog(context);
        singleChoiceDialog.setContentView(R.layout.dialog_single_choice);
        ListView listView = (ListView) singleChoiceDialog.findViewById(R.id.list);

        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(context, R.layout.item_single_choice_dialog, R.id.text, contents);
        listView.setAdapter(stringArrayAdapter);
        listView.setOnItemClickListener(listener);
        singleChoiceDialog.setTitle(title);

        singleChoiceDialog.show();
        return singleChoiceDialog;
    }

    public static Dialog showEditDialog(final Context context, String title, final String preText, final OnEditTextConfirmListener listener) {
        final Dialog editDialog = new Dialog(context);


        editDialog.setContentView(R.layout.dialog_edit);
        editDialog.setTitle(title);

        final EditText etContent = (EditText) editDialog.findViewById(R.id.edit);
        etContent.setText(preText);

        Button btnCancel = (Button) editDialog.findViewById(R.id.cancel);
        Button btnConfirm = (Button) editDialog.findViewById(R.id.confirm);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog(editDialog);
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(etContent.getText().toString())) {
                    Toast.makeText(context, "不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (listener != null)
                    listener.onEditTextConfirm(etContent.getText().toString(), !etContent.getText().toString().equals(preText));
                dismissDialog(editDialog);

            }
        });
        editDialog.show();
        return editDialog;

    }

    public interface OnEditTextConfirmListener {
        void onEditTextConfirm(String text, boolean isChanged);
    }
}
