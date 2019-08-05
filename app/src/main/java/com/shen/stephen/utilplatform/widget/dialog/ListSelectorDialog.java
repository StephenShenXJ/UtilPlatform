package com.shen.stephen.utilplatform.widget.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.util.StrUtil;

import java.io.Serializable;
import java.util.ArrayList;

public class ListSelectorDialog extends BaseDialog {
    private static String ARGS_KEY_ITEMS = "com.shen.stephen.utilplatform.widget.dialog.ARGS_KEY_ITEMS";

    private DialogPresentInterface.DialogItemSelectListener mItemSelectListener;
    private ArrayList<OptionItemData> mItems;
    private int defaultPos;

    public static ListSelectorDialog newInstance(String title,
                                                 ArrayList<OptionItemData> items, int defaultPosition,
                                                 DialogPresentInterface.DialogItemSelectListener itemSelectListener) {
        ListSelectorDialog dialog = new ListSelectorDialog();
        dialog.mItemSelectListener = itemSelectListener;
        Bundle args = new Bundle();
        args.putString(ARGS_KEY_TITLE, title);
        args.putInt(ARGS_KEY_DEFAULT_POSITION, defaultPosition);
        args.putSerializable(ARGS_KEY_ITEMS, items);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressWarnings("unchecked")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARGS_KEY_TITLE);
        int defaultPosition = getArguments().getInt(ARGS_KEY_DEFAULT_POSITION);
        mItems = (ArrayList<OptionItemData>) getArguments().getSerializable(
                ARGS_KEY_ITEMS);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String leftButton = getString(R.string.btn_cancel_text);
        if (!StrUtil.isEmpty(leftButton))
            builder.setNegativeButton(leftButton, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        // set title
        builder.setTitle(title).setSingleChoiceItems(genStringItems(mItems), defaultPosition,
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mItemSelectListener != null) {
                            OptionItemData item = null;
                            if (which >= 0 || which < mItems.size()) {
                                item = mItems.get(which);
                            }

                            mItemSelectListener.onSelectItem(dialog, item,
                                    which);
                        }
                    }
                }).setCancelable(true);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private String[] genStringItems(ArrayList<OptionItemData> options) {
        if (options == null || options.isEmpty()) {
            return new String[0];
        }
        String[] stringItems = new String[options.size()];

        for (int i = 0; i < options.size(); i++) {
            stringItems[i] = options.get(i).getDescription();
        }

        return stringItems;
    }

    public static class OptionItemData implements Serializable, Parcelable {
        private int optionId = 0;
        private String optionIdString = null;

        private String description;

        private static final long serialVersionUID = 5166514285335951683L;

        public static OptionItemData newInstance(int id, String description) {
            OptionItemData item = new OptionItemData();
            item.setDescription(description);
            item.setOptionId(id);
            return item;
        }

        public static OptionItemData newInstanceSring(String id, String description) {
            OptionItemData item = new OptionItemData();
            item.setDescription(description);
            item.setOptionIdString(id);
            return item;
        }

        public void setOptionIdString(String id) {
            this.optionIdString = id;
        }

        public void setOptionId(int id) {
            this.optionId = id;
        }

        public String getOptionidString() {
            return this.optionIdString;
        }

        public int getOptionid() {
            return this.optionId;
        }

        public String getDescription() {
            return description;
        }


        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(description);
            dest.writeInt(optionId);
        }

        public static final Creator<OptionItemData> CREATOR = new Creator<OptionItemData>() {
            public OptionItemData createFromParcel(Parcel source) {
                OptionItemData optionItem = new OptionItemData();
                optionItem.description = source.readString();
                optionItem.optionId = source.readInt();
                return optionItem;
            }

            public OptionItemData[] newArray(int size) {
                return new OptionItemData[size];
            }
        };
    }
}