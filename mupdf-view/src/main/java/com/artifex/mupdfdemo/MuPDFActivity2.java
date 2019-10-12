package com.artifex.mupdfdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;

public class MuPDFActivity2 extends Activity{

//    private final int    OUTLINE_REQUEST=0;
//    private final int    PRINT_REQUEST=1;
//    private final int    FILEPICK_REQUEST=2;
//
//    private MuPDFCore    muPDFCore;
//    private MuPDFReaderView muPDFReaderView;
//
//    private FilePicker mFilePicker;
//
//    private String filePath = Environment.getExternalStorageDirectory() + "/Download/pdf_t2.pdf"; // 文件路径


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

//        muPDFCore = openFile(filePath);
//
//        if (muPDFCore == null) {
//            System.out.println("打开失败");
//            return;
//        }

        setContentView(R.layout.youkan_view);
//        muPDFReaderView = (MuPDFReaderView)findViewById(R.id.mu_pdf_mupdfreaderview);
//        muPDFReaderView.setAdapter(new MuPDFPageAdapter(this, this,muPDFCore));
    }

//    /**
//     * 打开文件
//     * @param path 文件路径
//     * @return
//     */
//    private MuPDFCore openFile(String path) {
//
//        try {
//            muPDFCore = new MuPDFCore(this, path);
//            OutlineActivityData.set(null);
//        } catch (Exception e) {
//            return null;
//        } catch (OutOfMemoryError e) {
//            return null;
//        }
//        return muPDFCore;
//    }
//
//    @Override
//    public void performPickFor(FilePicker picker) {
//        mFilePicker = picker;
//        Intent intent = new Intent(this, ChoosePDFActivity.class);
//        intent.setAction(ChoosePDFActivity.PICK_KEY_FILE);
//        startActivityForResult(intent, FILEPICK_REQUEST);
//    }
}
