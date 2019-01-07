package com.qisiemoji.apksticker.whatsapp.edit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.qisi.event.Tracker;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.tracker.TrackerCompat;
import com.qisiemoji.apksticker.whatsapp.ToolBarActivity;
import com.qisiemoji.apksticker.whatsapp.edit.widget.CustomViewPager;
import com.qisiemoji.apksticker.whatsapp.edit.widget.RoundCornerColorImageView;
import com.qisiemoji.apksticker.whatsapp.manager.EditImageManager;
import com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager;

import java.util.ArrayList;

import static com.qisiemoji.apksticker.whatsapp.edit.EditImageActivity.ColorAdapter.ERASER_COLOR;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.EXTRA_SELECTED_LIST;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.RESULT_CODE_FINISH_EDIT_IMAGE;

public class EditImageActivity extends ToolBarActivity {

    private AppCompatImageView mPreButton;
    private AppCompatImageView mNextButton;

    private FrameLayout mDrawTab;
    private FrameLayout mTextTab;

    private Bitmap mEditImageBaseBitmap;
    private ImageView mEditImageBase;
    private EditImageLayout mEditImageLayout;
    private CustomViewPager mToolContentViewPager;
    private TabLayout mTabLayout;
    private TabAdapter mTabAdapter;

    private ProgressBar mProgressBar;

    private ImageView mTextAdd;

    private ArrayList<Integer> mColors = new ArrayList<>();

    private interface ToolSettingsListener {
        void onDrawSizeUpdated(float size);
        void onDrawColorUpdated(int color);
        void onTextColorUpdated(int color);
        void onTextAddClick();
        void onTextBorderEnableClick(boolean enable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setToolbarTitle("");
        initColors();
        setupEditImageView();
        setupTopControlViews();
        setupToolContentViews();

        /*
        * The UI event queue will process events in order.
        * After setContentView() is invoked,
        * the event queue will contain a message asking for a relayout,
        * so anything you post to the queue will happen after the layout pass
        * */
        mEditImageLayout.post(new Runnable() {
            @Override
            public void run() {
                mEditImageLayout.createTextToolText(false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            hideKeyboard();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupEditImageView() {
        mEditImageLayout = findViewById(R.id.edit_image_view);
        // avoid draw text error : Font size to large to fit in cache.
        mEditImageLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        Bitmap srcBitmap;
        ArrayList<String> list = getIntent().getStringArrayListExtra(EXTRA_SELECTED_LIST);
        if (list != null && list.get(0) != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            srcBitmap = BitmapFactory.decodeFile(list.get(0), options);
        } else if (EditImageManager.getInstance().getCroppedImage() != null) {
            srcBitmap = EditImageManager.getInstance().getCroppedImage();
        } else {
            return;
        }
        mEditImageLayout.setEditImageHelperListener(new EditImageHelper.EditImageHelperListener() {
            @Override
            public void onPreNextStateUpdated(boolean canPre, boolean canNext) {
                updatePreNextButtonState(canPre, canNext);
            }

            @Override
            public void onClickExistTextToolOperation() {
                TabLayout.Tab tab = mTabLayout.getTabAt(0);
                if (tab != null) {
                    tab.select();
                }
            }

            @Override
            public void onClickTextToolOperation(int color, boolean borderEnable) {
                mTabAdapter.setSelectTextColor(color);
                mTabAdapter.setBorderEnable(borderEnable);
            }

            @Override
            public void onNewCurrentToolNull() {
                hideKeyboard();
            }

            @Override
            public void onTextToolOperationBorderUpdated(boolean enable) {
                mTabAdapter.setBorderEnable(enable);
            }
        });

        if (srcBitmap != null) {
            mEditImageBaseBitmap = getScaledBitmap(srcBitmap);
            // TODO use edit_image_view_bg directlt if the 1:1 problem fixed
            mEditImageBase = findViewById(R.id.edit_image_view_base);
            mEditImageBase.setImageBitmap(mEditImageBaseBitmap);
        }
    }

    private void setupTopControlViews() {
        AppCompatTextView done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                reportEditDone(mEditImageLayout.getDrawCount(), mEditImageLayout.getTextCount());
                Bitmap operationBitmap = mEditImageLayout.getOperationBitmap();
                Canvas destCanvas = new Canvas(mEditImageBaseBitmap);
                destCanvas.drawBitmap(operationBitmap, 0, 0, new Paint());
                mProgressBar.setVisibility(View.VISIBLE);
                WAStickerManager.SaveTempImageFileTask task = new WAStickerManager.SaveTempImageFileTask(EditImageActivity.this, mEditImageBaseBitmap, new WAStickerManager.SaveTempImageFileCallback() {
                    @Override
                    public void onFinishSaved(String path) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent();
                        ArrayList<String> urls = new ArrayList<>();
                        urls.add(path);
                        intent.putStringArrayListExtra(EXTRA_SELECTED_LIST, urls);
                        setResult(RESULT_CODE_FINISH_EDIT_IMAGE, intent);
                        finish();
                        EditImageManager.getInstance().setCroppedImage(null);
                    }
                });
                task.execute();
            }
        });

        mPreButton = findViewById(R.id.pre);
        mPreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditImageLayout.preOperation();
                hideKeyboard();
            }
        });

        mNextButton = findViewById(R.id.next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditImageLayout.nextOperation();
                hideKeyboard();
            }
        });
        updatePreNextButtonState(false, false);

        mProgressBar = findViewById(R.id.loading_progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void setupToolContentViews() {
        mTextAdd = findViewById(R.id.text_add);
        mTextAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditImageLayout.createTextToolText(true);
            }
        });

        mToolContentViewPager = findViewById(R.id.viewpager);
        mToolContentViewPager.disableScroll(true);
        mTabLayout = findViewById(R.id.tabs);

        mTabAdapter = new TabAdapter(this, new ToolSettingsListener() {

            @Override
            public void onDrawSizeUpdated(float drawSize) {
                EditImageManager.getInstance().setDrawSettingSize(drawSize);
            }

            @Override
            public void onDrawColorUpdated(int color) {
                if (color == ERASER_COLOR) {
                    EditImageManager.getInstance().setDrawSettingIsEraser(true);
                } else {
                    EditImageManager.getInstance().setDrawSettingIsEraser(false);
                    EditImageManager.getInstance().setDrawSettingColor(color);
                }
            }

            @Override
            public void onTextColorUpdated(int color) {
                EditImageManager.getInstance().setTextToolColor(color);
                mEditImageLayout.setTextToolColor(color);
            }

            @Override
            public void onTextAddClick() {
                mEditImageLayout.createTextToolText(true);
            }

            @Override
            public void onTextBorderEnableClick(boolean enable) {
                mEditImageLayout.enableTextToolTextBorder(enable);
            }
        }, mColors);
        mToolContentViewPager.setAdapter(mTabAdapter);
        mToolContentViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mToolContentViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    mEditImageLayout.finishOperation();
                    mEditImageLayout.setCurrentToolType(EditImageHelper.ToolType.Text);
                    updateTabState(EditImageHelper.ToolType.Text);
                    mTextAdd.setVisibility(View.VISIBLE);
                } else {
                    mEditImageLayout.finishOperation();
                    mEditImageLayout.setCurrentToolType(EditImageHelper.ToolType.Draw);
                    updateTabState(EditImageHelper.ToolType.Draw);
                    mTextAdd.setVisibility(View.GONE);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        LayoutInflater inflater = LayoutInflater.from(this);
        mTextTab = (FrameLayout) inflater.inflate(R.layout.custom_tab_view, null);
        AppCompatTextView textTabName = mTextTab.findViewById(R.id.tab_name);
        textTabName.setText(getResources().getString(R.string.tab_text));
        TabLayout.Tab tabText = mTabLayout.newTab();
        tabText.setCustomView(mTextTab);
        mTabLayout.addTab(tabText);

        mDrawTab = (FrameLayout) inflater.inflate(R.layout.custom_tab_view, null);
        AppCompatTextView drawTabName = mDrawTab.findViewById(R.id.tab_name);
        drawTabName.setText(getResources().getString(R.string.tab_draw));
        TabLayout.Tab tabDraw = mTabLayout.newTab();
        tabDraw.setCustomView(mDrawTab);
        mTabLayout.addTab(tabDraw);

        updateTabState(EditImageHelper.ToolType.Text);
    }

    private void initColors() {
        Resources res = getResources();
        mColors.add(res.getColor(R.color.edit_color_1));
        mColors.add(res.getColor(R.color.edit_color_2));
        mColors.add(res.getColor(R.color.edit_color_3));
        mColors.add(res.getColor(R.color.edit_color_4));
        mColors.add(res.getColor(R.color.edit_color_5));
        mColors.add(res.getColor(R.color.edit_color_6));
        mColors.add(res.getColor(R.color.edit_color_7));
        mColors.add(res.getColor(R.color.edit_color_8));
    }

    private void updateTabState(EditImageHelper.ToolType toolType) {
        if (mDrawTab == null || mTextTab == null) {
            return;
        }
        AppCompatImageView drawTabIcon = mDrawTab.findViewById(R.id.tab_icon);
        AppCompatTextView drawTabName = mDrawTab.findViewById(R.id.tab_name);
        AppCompatImageView textTabIcon = mTextTab.findViewById(R.id.tab_icon);
        AppCompatTextView textTabName = mTextTab.findViewById(R.id.tab_name);

        Resources res = getResources();
        drawTabIcon.setImageResource(toolType.equals(EditImageHelper.ToolType.Draw) ? R.drawable.sc_ic_draw_p : R.drawable.sc_ic_draw_n);
        drawTabName.setTextColor(toolType.equals(EditImageHelper.ToolType.Draw) ? res.getColor(R.color.tab_select_text_color) : res.getColor(R.color.tab_unselect_text_color));
        textTabIcon.setImageResource(toolType.equals(EditImageHelper.ToolType.Text) ? R.drawable.sc_ic_text_p : R.drawable.sc_ic_text_n);
        textTabName.setTextColor(toolType.equals(EditImageHelper.ToolType.Text) ? res.getColor(R.color.tab_select_text_color) : res.getColor(R.color.tab_unselect_text_color));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_edit_image;
    }

    private void updatePreNextButtonState(boolean canPre, boolean canNext) {
        mPreButton.setEnabled(canPre);
        mPreButton.setClickable(canPre);
        mPreButton.setImageResource(canPre ? R.drawable.sc_ic_undo_a : R.drawable.sc_ic_undo_d);

        mNextButton.setEnabled(canNext);
        mNextButton.setClickable(canNext);
        mNextButton.setImageResource(canNext ? R.drawable.sc_ic_redo_a : R.drawable.sc_ic_redo_d);
    }

    private static class TabAdapter extends PagerAdapter implements View.OnClickListener {
        private static final String TAG_DRAW_COLOR = "draw_color";
        private static final String TAG_TEXT_COLOR = "text_color";

        private static final int DRAW_STEPS = 100;

        private Context context;
        private LayoutInflater layoutInflater;
        private ToolSettingsListener listener;
        private ArrayList<Integer> colors;

        private ArrayList<RoundCornerColorImageView> drawColorViews = new ArrayList<>();
        private ArrayList<RoundCornerColorImageView> textColorViews = new ArrayList<>();

        private ColorAdapter textColorAdapter;

        private float drawSizeMin;
        private float drawSizeMax;
        private float drawSizeDefault;
        private float drawSizeStep;

        private boolean mBorderEnable;
        private ImageView mSwitchBorder;

        TabAdapter(Context context, ToolSettingsListener listener, ArrayList<Integer> colors) {
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
            this.listener = listener;
            this.colors = colors;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view;
            if (position == 0) {
                view = createTextContent();
            } else {
                view = createDrawContent();
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        private View createDrawContent() {
            View view = layoutInflater.inflate(R.layout.edit_image_draw_content, null);

            drawSizeMin = context.getResources().getDimensionPixelSize(R.dimen.draw_size_min);
            drawSizeMax = context.getResources().getDimensionPixelSize(R.dimen.draw_size_max);
            drawSizeDefault = context.getResources().getDimensionPixelSize(R.dimen.draw_size_default);
            drawSizeStep = (drawSizeMax - drawSizeMin) / DRAW_STEPS;
            SeekBar sizeSeekBar = view.findViewById(R.id.size_seek_bar);
            sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (listener == null) {
                        return;
                    }
                    float drawSize = drawSizeMin + (progress * drawSizeStep);
                    listener.onDrawSizeUpdated(drawSize);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            float initValue = (drawSizeDefault - drawSizeMin) * DRAW_STEPS / (drawSizeMax - drawSizeMin);
            sizeSeekBar.setProgress((int) initValue);

            RecyclerView recyclerView = view.findViewById(R.id.rv_color);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            final ColorAdapter colorAdapter = new ColorAdapter(colors, true);
            colorAdapter.setListener(new ColorItemListener() {
                @Override
                public void onSelectColor(int color, int position) {
                    colorAdapter.setSelectIndex(position);
                    listener.onDrawColorUpdated(color);
                }
            });
            colorAdapter.setSelectIndex(0);
            recyclerView.setAdapter(colorAdapter);

            // init text color
            colorAdapter.setSelectIndex(1);
            listener.onDrawColorUpdated(colors.get(0));
            return view;
        }

        private View createTextContent() {
            View view = layoutInflater.inflate(R.layout.edit_image_text_content, null);

            mSwitchBorder = view.findViewById(R.id.border_switch);
            mSwitchBorder.setImageResource(mBorderEnable ? R.drawable.switch_on : R.drawable.switch_off);
            mSwitchBorder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onTextBorderEnableClick(!mBorderEnable);
                }
            });

            RecyclerView recyclerView = view.findViewById(R.id.rv_color);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            textColorAdapter = new ColorAdapter(colors, false);
            textColorAdapter.setListener(new ColorItemListener() {
                @Override
                public void onSelectColor(int color, int position) {
                    textColorAdapter.setSelectIndex(position);
                    listener.onTextColorUpdated(color);
                }
            });
            textColorAdapter.setSelectIndex(0);
            recyclerView.setAdapter(textColorAdapter);

            // init text size/ color
            listener.onTextColorUpdated(colors.get(0));
            return view;
        }

        @Override
        public void onClick(View v) {
            if (TAG_DRAW_COLOR.equals(v.getTag())) {
                int selectColor = Color.TRANSPARENT;
                for (RoundCornerColorImageView colorView : drawColorViews) {
                    if (colorView.equals(v)) {
                        colorView.enableBorder();
                        selectColor = colorView.getColor();
                    } else {
                        colorView.disableBorder();
                    }
                }
                listener.onDrawColorUpdated(selectColor);
            } else if (TAG_TEXT_COLOR.equals(v.getTag())) {
                int selectColor = Color.TRANSPARENT;
                for (RoundCornerColorImageView colorView : textColorViews) {
                    if (colorView.equals(v)) {
                        colorView.enableBorder();
                        selectColor = colorView.getColor();
                    } else {
                        colorView.disableBorder();
                    }
                }
                listener.onTextColorUpdated(selectColor);
            }
        }

        public void setSelectTextColor(int color) {
            for (int i = 0 ; i < colors.size() ; i++) {
                if (colors.get(i) == color) {
                    textColorAdapter.setSelectIndex(i);
                    break;
                }
            }
        }

        public void setBorderEnable(boolean enable) {
            mBorderEnable = enable;
            mSwitchBorder.setImageResource(mBorderEnable ? R.drawable.switch_on : R.drawable.switch_off);
        }
    }

    private interface ColorItemListener {
        void onSelectColor(int color, int position);
    }

    static class ColorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        static final int ERASER_COLOR = Color.TRANSPARENT;

        private ArrayList<Integer> colorList;
        private int selectIndex = 0;
        private ColorItemListener listener;
        private boolean hasEraser;

        public ColorAdapter(ArrayList<Integer> colors, boolean hasEraser) {
            this.colorList = colors;
            this.hasEraser = hasEraser;
        }

        public void setListener(ColorItemListener l) {
            listener = l;
        }

        public void setSelectIndex(int index) {
            selectIndex = index;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ColorHolder(inflater.inflate(ColorHolder.LAYOUT, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            boolean isEraser = hasEraser && (position == 0);
            boolean selected = (position == selectIndex);
            int color;
            boolean enableColorImageBorder;
            if (hasEraser) {
                color = (position == 0) ? ERASER_COLOR : colorList.get(position -1);
                enableColorImageBorder = (position == 2);
            } else {
                color = colorList.get(position);
                enableColorImageBorder = (position == 1);
            }
            ((ColorHolder) holder).bind(isEraser, selected, color, position, enableColorImageBorder, listener);
        }

        @Override
        public int getItemCount() {
            return hasEraser ? colorList.size() + 1 : colorList.size();
        }
    }

    static class ColorHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        static final int LAYOUT = R.layout.item_view_color_item;

        ColorItemListener listener;
        View itemView;
        RoundCornerColorImageView colorImage;
        ImageView eraserImage;
        View select;
        int color;
        int position;
        boolean isEraser;

        ColorHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.colorImage = itemView.findViewById(R.id.color_image);
            this.eraserImage = itemView.findViewById(R.id.eraser_image);
            this.select = itemView.findViewById(R.id.select);
        }

        void bind(boolean isEraser, boolean selected, int color, int position, boolean enableColorImageBorder, ColorItemListener listener) {
            this.isEraser = isEraser;
            this.color = color;
            this.position = position;
            this.itemView.setOnClickListener(this);
            if (isEraser) {
                this.colorImage.setVisibility(View.INVISIBLE);
                this.eraserImage.setVisibility(View.VISIBLE);
            } else {
                this.colorImage.setVisibility(View.VISIBLE);
                this.eraserImage.setVisibility(View.INVISIBLE);
                this.colorImage.setColor(color);
                if (enableColorImageBorder) {
                    this.colorImage.enableBorder();
                } else {
                    this.colorImage.disableBorder();
                }
            }
            this.select.setVisibility(selected ? View.VISIBLE : View.GONE);
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onSelectColor(color, position);
            }
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private Bitmap getScaledBitmap(Bitmap srcBitmap) {
        // compute bitmap Rect
        int srcBitmapWidth = srcBitmap.getWidth(); // a
        int srcBitmapHeight = srcBitmap.getHeight(); // b
        int imageSize = getResources().getDimensionPixelSize(R.dimen.edit_image_size); // i
        int imagePadding = 0;//getResources().getDimensionPixelOffset(R.dimen.edit_image_padding);
        int destLeft = 0;
        int destTop = 0;
        int destWidth = imageSize;
        int destHeight = imageSize;
        if (srcBitmapWidth > srcBitmapHeight && srcBitmapWidth >= imageSize) { // a>b>i || a>i>b
            destWidth = imageSize;
            destHeight = (int) ((float)(srcBitmapHeight*destWidth) / (float)srcBitmapWidth);
            destTop = (imageSize - destHeight)/2;
        } else if (srcBitmapHeight > srcBitmapWidth && srcBitmapHeight > imageSize) { // i<a<b || a<i<b
            destHeight = imageSize;
            destWidth = (int) ((float)(srcBitmapWidth*destHeight) / (float)srcBitmapHeight);
            destLeft = (imageSize - destWidth)/2;
        } else if (srcBitmapHeight < srcBitmapWidth && srcBitmapWidth < imageSize) { // i>a>b
            destWidth = imageSize - imagePadding * 2;
            destHeight = (int) ((float)(srcBitmapHeight*destWidth) / (float)srcBitmapWidth);
            destLeft = imagePadding;
            destTop = (imageSize - destHeight)/2;
        } else if (srcBitmapWidth < srcBitmapHeight && srcBitmapHeight < imageSize) { // a<b<i
            destHeight = imageSize - imagePadding * 2;
            destWidth = (int) ((float)(srcBitmapWidth*destHeight) / (float)srcBitmapHeight);
            destTop = imagePadding;
            destLeft = (imageSize - destWidth)/2;
        }

        Rect srcRect = new Rect(0, 0, srcBitmapWidth, srcBitmapHeight);
        Rect destRect = new Rect(destLeft, destTop, destLeft + destWidth, destTop + destHeight);
        Bitmap scaledBitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        Canvas destCanvas = new Canvas(scaledBitmap);
        destCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        destCanvas.drawBitmap(srcBitmap, srcRect, destRect, new Paint());
        return scaledBitmap;
    }

    private void reportEditDone(int drawCount, int textCount) {
        Tracker.Extra extra = TrackerCompat.getExtra(this);
        extra.put("draw_count", String.valueOf(drawCount));
        extra.put("text_count", String.valueOf(textCount));
        TrackerCompat.getTracker().logEventRealTime("edit_image","done","click",extra);
    }
}
