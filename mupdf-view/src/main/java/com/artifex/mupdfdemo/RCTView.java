package com.artifex.mupdfdemo;

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;

public class RCTView extends MuPDFReaderView implements FilePicker.FilePickerSupport{
    private Context mContext;

    private final int    OUTLINE_REQUEST=0;
    private final int    PRINT_REQUEST=1;
    private final int    FILEPICK_REQUEST=2;

    private MuPDFCore muPDFCore;

    private FilePicker mFilePicker;

    private String filePath = Environment.getExternalStorageDirectory() + "/Download/pdf_t2.pdf"; // 文件路径

    public RCTView(Context context, AttributeSet attrs) {
        super(context, attrs);
        System.out.println("LUOKUN");
        mContext = context;
        muPDFCore = openFile(filePath);

        if (muPDFCore == null) {
            System.out.println("打开失败");
            return;
        }

        this.setAdapter(new MuPDFPageAdapter(context, this,muPDFCore));
    }

    /**
     * 打开文件
     * @param path 文件路径
     * @return
     */
    private MuPDFCore openFile(String path) {

        try {
            muPDFCore = new MuPDFCore(mContext, path);
        } catch (Exception e) {
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        }
        return muPDFCore;
    }

    @Override
    public void performPickFor(FilePicker picker) {
        mFilePicker = picker;
    }
}
