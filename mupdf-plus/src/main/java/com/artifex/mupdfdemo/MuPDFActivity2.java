package com.artifex.mupdfdemo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;

import com.artifex.mupdfdemo.ReaderView.ViewMapper;
import com.artifex.utils.DigitalizedEventCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewAnimator;

class ThreadPerTaskExecutor implements Executor {
    public void execute(Runnable r) {
        new Thread(r).start();
    }
}

public class MuPDFActivity2 extends Activity implements FilePicker.FilePickerSupport
{
    /* The core rendering instance */
    enum TouchMode {DOWN, UP};
    enum TopBarMode {Main, Search, Annot, Delete, More, Accept};
    enum AcceptMode {Highlight, Underline, StrikeOut, Ink, CopyText};

    private final int    OUTLINE_REQUEST=0;
    private final int    PRINT_REQUEST=1;
    private final int    FILEPICK_REQUEST=2;
    private MuPDFCore    core;
    private String       mFileName;
    private MuPDFReaderView mDocView;
    private View         mButtonsView;
    private boolean      mButtonsVisible;
    private EditText     mPasswordView;
    private EditText     mFreeTextView;
    private RelativeLayout mBookNotePop;
    private RelativeLayout bookselecttextpop;
    private RelativeLayout bookselecttextup;
    private RelativeLayout bookselecttextdown;
    private TextView     mFilenameView;
    private SeekBar      mPageSlider;
    private int          mPageSliderRes;
    private TextView     mPageNumberView;
    private TextView     mInfoView;
    private ImageButton  mSearchButton;
    private ImageButton  mReflowButton;
    private ImageButton  mOutlineButton;
    private ImageButton	mMoreButton;
    private ImageButton mAnnotButton;
    private ViewAnimator mTopBarSwitcher;
    private ViewAnimator mButtonsSwitcher;
    private ImageButton  mLinkButton;
    private AcceptMode   mAcceptMode;
    private TouchMode    mTouchMode = TouchMode.DOWN;
    private ViewAnimator  mAnnotationWrapper;
    private ViewAnimator  mAnnotationConfirm;
    private RelativeLayout  mSearchWrapper;
    private ImageButton  mSearchBack;
    private ImageButton  mSearchFwd;
    private EditText     mSearchText;
    private SearchTask   mSearchTask;
    private AlertDialog.Builder mAlertBuilder;
    private boolean    mLinkHighlight = false;
    private final Handler mHandler = new Handler();
    private boolean mAlertsActive= false;
    private boolean mReflow = false;
    private AsyncTask<Void,Void,MuPDFAlert> mAlertTask;
    private AlertDialog mAlertDialog;
    private FilePicker mFilePicker;
    private Vibrator vibrator;
    private HashMap mTempMap;   //编辑文本的缓存字典

    public void createAlertWaiter() {
        mAlertsActive = true;
        // All mupdf library calls are performed on asynchronous tasks to avoid stalling
        // the UI. Some calls can lead to javascript-invoked requests to display an
        // alert dialog and collect a reply from the user. The task has to be blocked
        // until the user's reply is received. This method creates an asynchronous task,
        // the purpose of which is to wait of these requests and produce the dialog
        // in response, while leaving the core blocked. When the dialog receives the
        // user's response, it is sent to the core via replyToAlert, unblocking it.
        // Another alert-waiting task is then created to pick up the next alert.
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        mAlertTask = new AsyncTask<Void,Void,MuPDFAlert>() {

            @Override
            protected MuPDFAlert doInBackground(Void... arg0) {
                if (!mAlertsActive)
                    return null;

                return core.waitForAlert();
            }

            @Override
            protected void onPostExecute(final MuPDFAlert result) {
                // core.waitForAlert may return null when shutting down
                if (result == null)
                    return;
                final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
                for(int i = 0; i < 3; i++)
                    pressed[i] = MuPDFAlert.ButtonPressed.None;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            int index = 0;
                            switch (which) {
                                case AlertDialog.BUTTON1: index=0; break;
                                case AlertDialog.BUTTON2: index=1; break;
                                case AlertDialog.BUTTON3: index=2; break;
                            }
                            result.buttonPressed = pressed[index];
                            // Send the user's response to the core, so that it can
                            // continue processing.
                            core.replyToAlert(result);
                            // Create another alert-waiter to pick up the next alert.
                            createAlertWaiter();
                        }
                    }
                };
                mAlertDialog = mAlertBuilder.create();
                mAlertDialog.setTitle(result.title);
                mAlertDialog.setMessage(result.message);
                switch (result.iconType)
                {
                    case Error:
                        break;
                    case Warning:
                        break;
                    case Question:
                        break;
                    case Status:
                        break;
                }
                switch (result.buttonGroupType)
                {
                    case OkCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.cancel), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
                    case Ok:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.okay), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Ok;
                        break;
                    case YesNoCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON3, getString(R.string.cancel), listener);
                        pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
                    case YesNo:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.yes), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Yes;
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.no), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.No;
                        break;
                }
                mAlertDialog.setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            result.buttonPressed = MuPDFAlert.ButtonPressed.None;
                            core.replyToAlert(result);
                            createAlertWaiter();
                        }
                    }
                });

                mAlertDialog.show();
            }
        };

        mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
    }

    public void destroyAlertWaiter() {
        mAlertsActive = false;
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
    }

    private MuPDFCore openFile(String path)
    {
        int lastSlashPos = path.lastIndexOf('/');
        mFileName = new String(lastSlashPos == -1
                ? path
                : path.substring(lastSlashPos+1));
        System.out.println("Trying to open "+path);
        try
        {
            core = new MuPDFCore(this, path);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        }
        catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
        return core;
    }

    private MuPDFCore openBuffer(byte buffer[], String magic)
    {
        System.out.println("Trying to open byte buffer");
        try
        {
            core = new MuPDFCore(this, buffer, magic);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        }
        catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
        return core;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mAlertBuilder = new AlertDialog.Builder(this);

        if (core == null) {
            core = (MuPDFCore)getLastNonConfigurationInstance();

            if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
                mFileName = savedInstanceState.getString("FileName");
            }
        }
        if (core == null) {
            Intent intent = getIntent();
            byte buffer[] = null;
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                System.out.println("URI to open is: " + uri);
                if (uri.toString().startsWith("content://")) {
                    String reason = null;
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);
                        int len = is.available();
                        buffer = new byte[len];
                        is.read(buffer, 0, len);
                        is.close();
                    }
                    catch (OutOfMemoryError e) {
                        System.out.println("Out of memory during buffer reading");
                        reason = e.toString();
                    }
                    catch (Exception e) {
                        System.out.println("Exception reading from stream: " + e);

                        // Handle view requests from the Transformer Prime's file manager
                        // Hopefully other file managers will use this same scheme, if not
                        // using explicit paths.
                        // I'm hoping that this case below is no longer needed...but it's
                        // hard to test as the file manager seems to have changed in 4.x.
                        try {
                            Cursor cursor = getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
                            if (cursor.moveToFirst()) {
                                String str = cursor.getString(0);
                                if (str == null) {
                                    reason = "Couldn't parse data in intent";
                                }
                                else {
                                    uri = Uri.parse(str);
                                }
                            }
                        }
                        catch (Exception e2) {
                            System.out.println("Exception in Transformer Prime file manager code: " + e2);
                            reason = e2.toString();
                        }
                    }
                    if (reason != null) {
                        buffer = null;
                        Resources res = getResources();
                        AlertDialog alert = mAlertBuilder.create();
                        setTitle(String.format(res.getString(R.string.cannot_open_document_Reason), reason));
                        alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                        alert.show();
                        return;
                    }
                }
                if (buffer != null) {
                    core = openBuffer(buffer, intent.getType());
                } else {
                    core = openFile(Uri.decode(uri.getEncodedPath()));
                }
                SearchTaskResult.set(null);
            }
            if (core != null && core.needsPassword()) {
                requestPassword(savedInstanceState);
                return;
            }
            if (core != null && core.countPages() == 0)
            {
                core = null;
            }
        }
        if (core == null)
        {
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle(R.string.cannot_open_document);
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            alert.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            alert.show();
            return;
        }

        createUI(savedInstanceState);
    }

    public void requestPassword(final Bundle savedInstanceState) {
        mPasswordView = new EditText(this);
        mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        mPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        AlertDialog alert = mAlertBuilder.create();
        alert.setTitle(R.string.enter_password);
        alert.setView(mPasswordView);
        alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (core.authenticatePassword(mPasswordView.getText().toString())) {
                            createUI(savedInstanceState);
                        } else {
                            requestPassword(savedInstanceState);
                        }
                    }
                });
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alert.show();
    }

    public void createUI(Bundle savedInstanceState) {
        if (core == null)
            return;

        // Now create the UI.
        // First create the document view
        mDocView = new MuPDFReaderView(this) {
            /**
             * 监听来自手势/search()的切换页面动作
             * **/
            @Override
            protected void onMoveToChild(int i) {
                if (core == null)
                    return;
                mPageNumberView.setText(String.format("%d / %d", i + 1,
                        core.countPages()));
                mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
                mPageSlider.setProgress(i * mPageSliderRes);

                super.onMoveToChild(i);
            }

            /**
             * 点击空白处-展开与隐藏菜单
             * **/
            @Override
            protected void onTapMainDocArea() {

                if(mBookNotePop.getVisibility() == VISIBLE){
                    hideBookNotePop();
                }else if(mSearchWrapper.getVisibility() == VISIBLE) {
                    searchModeOff();
                }else if(mAnnotationWrapper.getVisibility() == VISIBLE) {
                    slideDownToHide(mAnnotationWrapper);
                }else if (!mButtonsVisible) {
                    showButtons();
                }else if(mButtonsVisible){
                    hideButtons();
                }
//                OnCancelAcceptButtonClick();
            }

            /**
             * 滑动页面时-隐藏菜单
             * **/
            @Override
            protected void onDocMotion() {
                searchModeOff();
                hideBookNotePop();
                hideButtons();
            }

            /**
             * 监听菜单内按钮点击事件
             * **/
            @Override
            protected void onHit(Hit item) {

            }

            /**
             * 监听添加文本批注事件
             * **/
            float _left;
            float _top;
            float free_text_font_size = 50;
            @Override
            protected void onFreetextAdd(float x, float y) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                //再次点击画布时取消编辑并保存已键入的值
                if(mFreeTextView.getVisibility() == View.VISIBLE){
                    mDocView.setMode(Mode.Viewing);
                    mFreeTextView.setVisibility(View.INVISIBLE);
                    mBookNotePop.setVisibility(View.INVISIBLE);

                    String _text = mFreeTextView.getText().toString().replace("\n", "");
                    MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();

                    if(!_text.equals("")){
                        if(mTempMap != null) {
                            mTempMap.put("text",_text);
                            mTempMap.put("width",(float)mFreeTextView.getMeasuredWidth());
                            mTempMap.put("height",(float)mFreeTextView.getMeasuredHeight());
                            pageView.addFreetextAnnotation(mTempMap);
                        }else {
                            pageView.addFreetextAnnotation(_left,_top,(float)mFreeTextView.getMeasuredWidth(),(float)mFreeTextView.getMeasuredHeight(),mFreeTextView.getText().toString());
                        }
                    }

                    mFreeTextView.setText("");
                    mTempMap = null;

                    if (imm != null)
                        imm.hideSoftInputFromWindow(mFreeTextView.getWindowToken(), 0);
                }else {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFreeTextView.getLayoutParams();
                    lp.setMargins((int)x, (int)y,0,0);
                    mFreeTextView.setLayoutParams(lp);
                    mFreeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,free_text_font_size);
                    mFreeTextView.setVisibility(View.VISIBLE);
                    _left = x;
                    _top = y;

                    if (imm != null)
                        imm.showSoftInput(mFreeTextView, 0);
                }
            }


        };
        /**
         * !important
         *
         * setAdapter适配器控件 定义一系列空的swiper-pager,然后填充数据进入
         * **/
        mDocView.setAdapter(new MuPDFPageAdapter(this, this, core));

        /**
         * 震动实例
         * **/
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        /**
         * EventCallback
         * **/
        mDocView.setEventCallback(new DigitalizedEventCallback(){
            @Override
            public void longPressOnPdfPosition(int page, float viewX, float viewY, float pdfX, float pdfY){
                vibrator.vibrate(100);
                mDocView.setMode(MuPDFReaderView.Mode.Selecting);
                MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
                if (pageView != null)
                    pageView.selectText(viewX, viewY, viewX, viewY);
                mTouchMode = TouchMode.DOWN;
            }

            @Override
            public void doubleTapOnPdfPosition(int page, float viewX, float viewY, float pdfX, float pdfY){

            }

            @Override
            public void singleTapOnPdfPosition(int page, float viewX, float viewY, float pdfX, float pdfY){
            }

            @Override
            public void pageChanged(int page){

            }

            @Override
            public void touchDown(RectF rect, float scale){
                MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
                if(rect != null) {
                    float docRelX = rect.left * scale + pageView.getLeft()-bookselecttextup.getWidth()/2;
                    float docRelY = rect.top * scale + pageView.getTop();
                    float docRelRight = rect.right * scale + pageView.getLeft()-bookselecttextdown.getWidth()/2;
                    float docRelBottom = rect.bottom * scale + pageView.getTop();
                    if(mDocView.mMode == MuPDFReaderView.Mode.Selecting){
                        bookselecttextup.setVisibility(View.VISIBLE);
                        bookselecttextup.setX(docRelX);
                        bookselecttextup.setY(docRelY-bookselecttextup.getMeasuredHeight());

                        bookselecttextdown.setVisibility(View.VISIBLE);
                        if(rect.bottom>0){
                            bookselecttextdown.setX(docRelRight);
                            bookselecttextdown.setY(docRelBottom);
                        }

                        if(docRelX<0){
                            bookselecttextpop.setX(0);
                        }else if(mDocView.getWidth() - docRelX <  bookselecttextpop.getMeasuredWidth()){
                            bookselecttextpop.setX(mDocView.getWidth()-bookselecttextpop.getMeasuredWidth());
                        }else {
                            bookselecttextpop.setX(docRelX);
                        }

                        if(docRelY<(bookselecttextpop.getMeasuredHeight()+bookselecttextdown.getMeasuredHeight())){
                            bookselecttextpop.setY((rect.bottom-rect.top) * scale+docRelY+bookselecttextdown.getMeasuredHeight());
                        }else {
                            bookselecttextpop.setY(docRelY-bookselecttextpop.getMeasuredHeight()-bookselecttextdown.getMeasuredHeight());
                        }
                    }else if(mDocView.mMode == MuPDFReaderView.Mode.Viewing){
                        showBookNotePop();

                        if(docRelX<0){
                            mBookNotePop.setX(0);
                        }else if(mDocView.getWidth() - docRelX <  mBookNotePop.getMeasuredWidth()){
                            mBookNotePop.setX(mDocView.getWidth()-mBookNotePop.getMeasuredWidth());
                        }else {
                            mBookNotePop.setX(docRelX);
                        }

                        if(docRelY<mBookNotePop.getMeasuredHeight()){
                            mBookNotePop.setY((rect.bottom-rect.top) * scale+docRelY);
                        }else {
                            mBookNotePop.setY(docRelY-mBookNotePop.getMeasuredHeight());
                        }
                    }
                }else {
                    mTouchMode = TouchMode.DOWN;
                    selectModeOff();
                }
            }

            @Override
            public void touchUp(){
                mTouchMode = TouchMode.UP;
                if(mDocView.mMode == MuPDFReaderView.Mode.Selecting) {
                    bookselecttextpop.setVisibility(View.VISIBLE);
                    bookselecttextdown.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                                bookselecttextpop.setVisibility(View.INVISIBLE);
                            }else if (motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE) {
                                //设置下标的位置
                                final float x = bookselecttextdown.getX();
                                final float y = bookselecttextdown.getY();
                                MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
                                if (pageView != null)
                                    pageView.selectText(bookselecttextup.getX(), bookselecttextup.getY()+bookselecttextup.getHeight(),x+motionEvent.getX()-bookselecttextdown.getWidth(), y+motionEvent.getY()-bookselecttextdown.getHeight());
                            }else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                                bookselecttextpop.setVisibility(View.VISIBLE);
                            }

                            return true;
                        }
                    });

                    bookselecttextup.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                                bookselecttextpop.setVisibility(View.INVISIBLE);
                            }else if (motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE) {
                                //设置上标的位置
                                final float x = bookselecttextup.getX();
                                final float y = bookselecttextup.getY();
                                MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
                                if (pageView != null)
                                    pageView.selectText(x+motionEvent.getX()-bookselecttextup.getWidth(), y+motionEvent.getY()+bookselecttextup.getHeight(),bookselecttextdown.getX(), bookselecttextdown.getY());
                            }else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                                bookselecttextpop.setVisibility(View.VISIBLE);
                            }

                            return true;
                        }
                    });
                }
            }

            @Override
            public void error(String message){

            }
            }
        );

        /**
         * 搜索任务
         * **/
        mSearchTask = new SearchTask(this, core) {
            @Override
            protected void onTextFound(SearchTaskResult result) {
                SearchTaskResult.set(result);
                // Ask the ReaderView to move to the resulting page
                mDocView.setDisplayedViewIndex(result.pageNumber);
                // Make the ReaderView act on the change to SearchTaskResult
                // via overridden onChildSetup method.
                mDocView.resetupChildren();
            }
        };

        // Make the buttons overlay, and store all its
        // controls in variables
        makeButtonsView();

        // Set up the page slider
        int smax = Math.max(core.countPages()-1,1);
        mPageSliderRes = ((10 + smax - 1)/smax) * 2;

        // Set the file-name text
        mFilenameView.setText(mFileName);

        // Activate the seekbar
        mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDocView.setDisplayedViewIndex((seekBar.getProgress()+mPageSliderRes/2)/mPageSliderRes);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                updatePageNumView((progress+mPageSliderRes/2)/mPageSliderRes);
            }
        });

        // Activate the search-preparing button
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchModeOn();
            }
        });

        // Search invoking buttons are disabled while there is no text specified
        mSearchBack.setEnabled(false);
        mSearchFwd.setEnabled(false);
        mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
        mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128));

        // React to interaction with the text widget
        mSearchText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                boolean haveText = s.toString().length() > 0;
                setButtonEnabled(mSearchBack, haveText);
                setButtonEnabled(mSearchFwd, haveText);

                // Remove any previous search results
                if (SearchTaskResult.get() != null && !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
                    SearchTaskResult.set(null);
                    mDocView.resetupChildren();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}
        });

        //React to Done button on keyboard
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    search(1);
                return false;
            }
        });

        mSearchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
                    search(1);
                return false;
            }
        });

        // Activate search invoking buttons
        mSearchBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search(-1);
            }
        });
        mSearchFwd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search(1);
            }
        });

//        mLinkButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                setLinkHighlight(!mLinkHighlight);
//            }
//        });

        mOutlineButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (core.hasOutline()) {
                    OutlineItem outline[] = core.getOutline();
                    if (outline != null) {
                        OutlineActivityData.get().items = outline;
                        Intent intent = new Intent(MuPDFActivity2.this, OutlineActivity.class);
                        startActivityForResult(intent, OUTLINE_REQUEST);
                    }
                }else {
                    showInfo("该PDF未提供目录！");
                }

            }
        });

        // Reenstate last state if it was recorded
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        mDocView.setDisplayedViewIndex(prefs.getInt("page"+mFileName, 0));

        if (savedInstanceState != null && !savedInstanceState.getBoolean("ButtonsHidden", true))
            showButtons();

        if(savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false))
            searchModeOn();

        if(savedInstanceState != null && savedInstanceState.getBoolean("ReflowMode", false))
            reflowModeSet(true);

        // Stick the document view and the buttons overlay into a parent view
        RelativeLayout layout = new RelativeLayout(this);
        layout.addView(mDocView);
        layout.addView(mButtonsView);
        setContentView(layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OUTLINE_REQUEST:
                if (resultCode >= 0)
                    mDocView.setDisplayedViewIndex(resultCode);
                break;
            case PRINT_REQUEST:
                if (resultCode == RESULT_CANCELED)
                    showInfo(getString(R.string.print_failed));
                break;
            case FILEPICK_REQUEST:
                if (mFilePicker != null && resultCode == RESULT_OK)
                    mFilePicker.onPick(data.getData());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Object onRetainNonConfigurationInstance()
    {
        MuPDFCore mycore = core;
        core = null;
        return mycore;
    }

    private void reflowModeSet(boolean reflow)
    {
        mReflow = reflow;
        mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core) : new MuPDFPageAdapter(this, this, core));
//        mReflowButton.setColorFilter(mReflow ? Color.argb(0xFF, 172, 114, 37) : Color.argb(0xFF, 255, 255, 255));
//        setButtonEnabled(mAnnotButton, !reflow);
        setButtonEnabled(mSearchButton, !reflow);
        if (reflow) setLinkHighlight(false);
//        setButtonEnabled(mLinkButton, !reflow);
        mDocView.refresh(mReflow);
    }

    private void toggleReflow() {
        reflowModeSet(!mReflow);
        showInfo(mReflow ? getString(R.string.entering_reflow_mode) : getString(R.string.leaving_reflow_mode));
    }

    /**
     * Activity意外收回时处理
     * 特别是屏幕旋转时会触发
     * **/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mFileName != null && mDocView != null) {
            outState.putString("FileName", mFileName);

            // Store current page in the prefs against the file name,
            // so that we can pick it up each time the file is loaded
            // Other info is needed only for screen-orientation change,
            // so it can go in the bundle
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt("page"+mFileName, mDocView.getDisplayedViewIndex());
            edit.commit();
        }

        if (!mButtonsVisible)
            outState.putBoolean("ButtonsHidden", true);

        if(mSearchWrapper.getVisibility() == View.VISIBLE)
            outState.putBoolean("SearchMode", true);

        if (mReflow)
            outState.putBoolean("ReflowMode", true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSearchTask != null)
            mSearchTask.stop();

        if (mFileName != null && mDocView != null) {
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt("page"+mFileName, mDocView.getDisplayedViewIndex());
            edit.commit();
        }
    }

    public void onDestroy()
    {
        if (mDocView != null) {
            mDocView.applyToChildren(new ViewMapper() {
                void applyToView(View view) {
                    ((MuPDFView)view).releaseBitmaps();
                }
            });
        }
        if (core != null)
            core.onDestroy();
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        core = null;
        super.onDestroy();
    }

    private void setButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setColorFilter(enabled ? Color.argb(255, 255, 255, 255):Color.argb(255, 128, 128, 128));
    }

    private void setLinkHighlight(boolean highlight) {
        mLinkHighlight = highlight;
        // LINK_COLOR tint
//        mLinkButton.setColorFilter(highlight ? Color.argb(0xFF, 172, 114, 37) : Color.argb(0xFF, 255, 255, 255));
        // Inform pages of the change.
        mDocView.setLinksEnabled(highlight);
    }

    private void showButtons() {
        if (core == null)
            return;
        if (!mButtonsVisible) {
            mButtonsVisible = true;
            // Update page number text and slider
            int index = mDocView.getDisplayedViewIndex();
            updatePageNumView(index);
            mPageSlider.setMax((core.countPages()-1)*mPageSliderRes);
            mPageSlider.setProgress(index*mPageSliderRes);

            mPageNumberView.setVisibility(View.VISIBLE);
            slideDownToVisible(mTopBarSwitcher);
            slideUpToVisible(mButtonsSwitcher);
        }
    }

    private void hideButtons() {
        if (mButtonsVisible) {
            mButtonsVisible = false;
            hideKeyboard();

            mPageNumberView.setVisibility(View.INVISIBLE);
            slideUpToHide(mTopBarSwitcher);
            slideDownToHide(mButtonsSwitcher);
        }
    }

    private void showBookNotePop(){
        final MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null) {
            if(pageView.getFreetextIndex()!=-1){
//                mButtonsView.findViewById(R.id.rly_popbooknotecolor).setVisibility(View.VISIBLE);
//                mButtonsView.findViewById(R.id.rly_popbooknotestyle).setVisibility(View.VISIBLE);
                mButtonsView.findViewById(R.id.rly_popbooknoteedit).setVisibility(View.VISIBLE);
                mButtonsView.findViewById(R.id.rly_popbooknoteedit).setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                onFreetextEdit(pageView.getFreetextIndex());
                            }
                        }
                );
            }else if(pageView.getSelectedAnnotationIndex()!=-1){

            }
        }
        mBookNotePop.setVisibility(View.VISIBLE);
    }

    private void hideBookNotePop(){
        if(mBookNotePop.getVisibility() == View.VISIBLE){
            mBookNotePop.setVisibility(View.INVISIBLE);
            mButtonsView.findViewById(R.id.rly_popbooknotecolor).setVisibility(View.GONE);
            mButtonsView.findViewById(R.id.rly_popbooknotestyle).setVisibility(View.GONE);
            mButtonsView.findViewById(R.id.rly_popbooknoteedit).setVisibility(View.GONE);
        }
    }

    private void searchModeOn() {
        hideButtons();
        mSearchWrapper.setVisibility(View.VISIBLE);
        //Focus on EditTextWidget
        mSearchText.requestFocus();
        showKeyboard();
    }

    private void searchModeOff() {
        hideKeyboard();
        SearchTaskResult.set(null);
        mSearchWrapper.setVisibility(View.GONE);
        // Make the ReaderView act on the change to mSearchTaskResult
        // via overridden onChildSetup method.
        mDocView.resetupChildren();
    }

    private void selectModeOff() {
        if (bookselecttextpop.getVisibility() == View.VISIBLE) {
            mDocView.mMode = MuPDFReaderView.Mode.Viewing;
            bookselecttextpop.setVisibility(View.INVISIBLE);
            bookselecttextdown.setVisibility(View.INVISIBLE);
            bookselecttextup.setVisibility(View.INVISIBLE);
        }
    }

    private void updatePageNumView(int index) {
        if (core == null)
            return;
        mPageNumberView.setText(String.format("%d / %d", index+1, core.countPages()));
    }

    //云打印pdf
    private void printDoc() {
        if (!core.fileFormat().startsWith("PDF")) {
            showInfo(getString(R.string.format_currently_not_supported));
            return;
        }

        Intent myIntent = getIntent();
        Uri docUri = myIntent != null ? myIntent.getData() : null;

        if (docUri == null) {
            showInfo(getString(R.string.print_failed));
        }

        if (docUri.getScheme() == null)
            docUri = Uri.parse("file://"+docUri.toString());

        Intent printIntent = new Intent(this, PrintDialogActivity.class);
        printIntent.setDataAndType(docUri, "aplication/pdf");
        printIntent.putExtra("title", mFileName);
        startActivityForResult(printIntent, PRINT_REQUEST);
    }

    private void showInfo(String message) {
        mInfoView.setText(message);

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            SafeAnimatorInflater safe = new SafeAnimatorInflater((Activity)this, R.animator.info, (View)mInfoView);
        } else {
            mInfoView.setVisibility(View.VISIBLE);
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    mInfoView.setVisibility(View.INVISIBLE);
                }
            }, 500);
        }
    }

    private void makeButtonsView() {
        mButtonsView = getLayoutInflater().inflate(R.layout.youkan_design,null);//done
        mFilenameView = (TextView)mButtonsView.findViewById(R.id.docNameText);//done
        mFreeTextView = (EditText)mButtonsView.findViewById(R.id.freeText);//done
        mBookNotePop = (RelativeLayout)mButtonsView.findViewById(R.id.booknotepop);//done

        bookselecttextpop = (RelativeLayout)mButtonsView.findViewById(R.id.bookselecttextpop);//done
        bookselecttextup = (RelativeLayout)mButtonsView.findViewById(R.id.bookselecttextup);//done
        bookselecttextdown = (RelativeLayout)mButtonsView.findViewById(R.id.bookselecttextdown);//done

        mPageSlider = (SeekBar)mButtonsView.findViewById(R.id.pageSlider);//done
        mPageNumberView = (TextView)mButtonsView.findViewById(R.id.pageNumber);//done
        mInfoView = (TextView)mButtonsView.findViewById(R.id.info);
        mSearchButton = (ImageButton)mButtonsView.findViewById(R.id.searchButton);//extend
//        mReflowButton = (ImageButton)mButtonsView.findViewById(R.id.reflowButton);    //功能暂时取消
        mOutlineButton = (ImageButton)mButtonsView.findViewById(R.id.outlineButton);//done
//        mAnnotButton = (ImageButton)mButtonsView.findViewById(R.id.editAnnotButton);  //复制文本功能暂时取消
        mTopBarSwitcher = (ViewAnimator)mButtonsView.findViewById(R.id.topBarswitcher);//done
        mButtonsSwitcher = (ViewAnimator)mButtonsView.findViewById(R.id.buttonsSwitcher);//done

        mAnnotationWrapper = (ViewAnimator)mButtonsView.findViewById(R.id.annotationWrapper);
        mAnnotationConfirm = (ViewAnimator)mButtonsView.findViewById(R.id.annotationConfirm);
        mSearchWrapper = (RelativeLayout)mButtonsView.findViewById(R.id.topBar1Search);
        mSearchBack = (ImageButton)mButtonsView.findViewById(R.id.searchBack);//extend
        mSearchFwd = (ImageButton)mButtonsView.findViewById(R.id.searchForward);//extend
        mSearchText = (EditText)mButtonsView.findViewById(R.id.searchText);//extend

//        mLinkButton = (ImageButton)mButtonsView.findViewById(R.id.linkButton);  //功能暂时取消
        mTopBarSwitcher.setVisibility(View.INVISIBLE);
        mButtonsSwitcher.setVisibility(View.INVISIBLE);
        mPageNumberView.setVisibility(View.INVISIBLE);
        mInfoView.setVisibility(View.INVISIBLE);
        mFreeTextView.setVisibility(View.INVISIBLE);
        mFreeTextView.measure(0,0);
        mBookNotePop.setVisibility(View.INVISIBLE);
        bookselecttextpop.setVisibility(View.INVISIBLE);
        bookselecttextup.setVisibility(View.INVISIBLE);
        bookselecttextdown.setVisibility(View.INVISIBLE);
    }


    public void OnPrintButtonClick(View v) {
        printDoc();
    }

    public void OnCancelSearchButtonClick(View v) {
        searchModeOff();
    }

    //删除当前所选的批注/文本/下划线/高亮/删除线
    public void OnDeleteButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null)
            pageView.deleteSelectedAnnotation();
        hideBookNotePop();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.showSoftInput(mSearchText, 0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
    }

    private void search(int direction) {
        hideKeyboard();
        int displayPage = mDocView.getDisplayedViewIndex();
        SearchTaskResult r = SearchTaskResult.get();
        int searchPage = r != null ? r.pageNumber : -1;
        mSearchTask.go(mSearchText.getText().toString(), direction, displayPage, searchPage);
    }

    @Override
    protected void onStart() {
        if (core != null)
        {
            core.startAlerts();
            createAlertWaiter();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        if (core != null)
        {
            destroyAlertWaiter();
            core.stopAlerts();
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (core != null && core.hasChanges()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialog.BUTTON_POSITIVE)
                        core.save();

                    finish();
                }
            };
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle("MuPDF");
            alert.setMessage(getString(R.string.document_has_changes_save_them_));
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), listener);
            alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), listener);
            alert.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void performPickFor(FilePicker picker) {
        mFilePicker = picker;
        Intent intent = new Intent(this, ChoosePDFActivity.class);
        intent.setAction(ChoosePDFActivity.PICK_KEY_FILE);
        startActivityForResult(intent, FILEPICK_REQUEST);
    }




    /************ Animate Tools ************/
    //向上滑动以显示
    public void slideUpToVisible(final ViewAnimator v){
        Animation anim = new TranslateAnimation(0,0, v.getHeight(),0);
        anim.setDuration(200);
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {}
        });
        v.startAnimation(anim);
    }
    //向上滑动以隐藏
    public void slideUpToHide(final ViewAnimator v){
        Animation anim = new TranslateAnimation(0,0,0, -v.getHeight());
        anim.setDuration(200);
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.INVISIBLE);
            }
        });
        v.startAnimation(anim);
    }
    //向下滑动以显示
    public void slideDownToVisible(final ViewAnimator v){
        Animation anim = new TranslateAnimation(0,0, -v.getHeight(),0);
        anim.setDuration(200);
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {}
        });
        v.startAnimation(anim);
    }
    //向下滑动以隐藏
    public void slideDownToHide(final ViewAnimator v){
        Animation anim = new TranslateAnimation(0,0,0, v.getHeight());
        anim.setDuration(200);
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.INVISIBLE);
            }
        });
        v.startAnimation(anim);
    }

    /************ DOM Click Event ************/
    //上一页
    public void onSmartMoveBackwards(View v){
        mDocView.smartMoveBackwards();
    }
    //下一页
    public void onSmartMoveForwards(View v){
        mDocView.smartMoveForwards();
    }

    //显示多功能批注菜单
    public void onShowAnnotationWrapper(View v){
        hideButtons();
        slideUpToVisible(mAnnotationWrapper);
    }
    //墨迹
    public void onAnnotationDrawer(View v){
        mDocView.setMode(MuPDFReaderView.Mode.Drawing);
        showInfo(getString(R.string.draw_annotation));
        slideDownToHide(mAnnotationWrapper);
        slideUpToVisible(mAnnotationConfirm);
    }
    //文本
    public void onAnnotationFreetext(View v){
        slideDownToHide(mAnnotationWrapper);
        showInfo("添加文本批注");
        mDocView.setMode(MuPDFReaderView.Mode.Freetexting);
    }

    //保存当前墨迹
    public void onAnnotationSave(View v){
        slideDownToHide(mAnnotationConfirm);

        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null) {
            if (!pageView.saveDraw())
                showInfo(getString(R.string.nothing_to_save));
        }
        mDocView.setMode(MuPDFReaderView.Mode.Viewing);
    }
    //取消当前墨迹
    public void onAnnotationCancel(View v){
        slideDownToHide(mAnnotationConfirm);
        OnCancelAcceptButtonClick();
    }

    //取消一切
    public void OnCancelAcceptButtonClick(){
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null) {
            pageView.deselectText();
            pageView.cancelDraw();
        }
        selectModeOff();
        mDocView.setMode(MuPDFReaderView.Mode.Viewing);
    }

    //保存为下划线
    public void onUnderlineSave(View v){
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null)
            pageView.markupSelection(Annotation.Type.UNDERLINE);
        selectModeOff();
    }

    //保存为高亮
    public void onHighlightSave(View v){
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null)
            pageView.markupSelection(Annotation.Type.HIGHLIGHT);
        selectModeOff();
    }

    //选择复制按钮
    public void onCopyTextSave(View v){
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null)
            pageView.copySelection();
        showInfo("文字复制成功");
        selectModeOff();
    }

    //文本编辑
    protected void onFreetextEdit(int index) {
        mDocView.setMode(MuPDFReaderView.Mode.Freetexting);
        mTempMap = MuPDFFreeTextData.mFreetext.get(index);
        MuPDFFreeTextData.mFreetext.remove(index);
        float x = (float)mTempMap.get("x");
        float y = (float)mTempMap.get("y");

        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFreeTextView.getLayoutParams();
        lp.setMargins((int)(x*pageView.getScale())+pageView.getLeft(), (int)(y*pageView.getScale())+pageView.getTop(),0,0);
        mFreeTextView.setLayoutParams(lp);
        mFreeTextView.setText((String)mTempMap.get("text"));
        mFreeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,(float)mTempMap.get("size")*pageView.getScale());
        mFreeTextView.setVisibility(View.VISIBLE);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.showSoftInput(mFreeTextView, 0);

        selectModeOff();
    }

    //上下bar添加点击事件可防止canvas onPassClick事件冒泡
    public void onBubbling(View v){

    }
}
