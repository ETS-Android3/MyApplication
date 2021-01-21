# MuPDF简化版

入口：MuPDFActivity.java

打开文件：new MuPDFCore(this, path)

显示文件：MuPDFReaderView.setAdapter(new MuPDFPageAdapter(this, muPDFCore));

MuPDFReaderView extends ReaderView

MuPDFPageAdapter add MuPDFPageView


MuPDFActivity2
MuPDFCore
MuPDFReaderView
ReaderView
MuPDFView
PageView