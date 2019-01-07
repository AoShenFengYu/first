package com.qisiemoji.apksticker.whatsapp.edit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.views.imgedit.DrawType;
import com.qisiemoji.apksticker.views.imgedit.DrawView;
import com.qisiemoji.apksticker.whatsapp.ToolBarActivity;
import com.qisiemoji.apksticker.whatsapp.edit.widget.RoundCornerColorImageView;
import com.qisiemoji.apksticker.whatsapp.manager.EditImageManager;
import com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager;

import java.util.ArrayList;

import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.EXTRA_SELECTED_LIST;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.RESULT_CODE_FINISH_EDIT_IMAGE;

public class EditImageNewActivity extends ToolBarActivity {

    private AppCompatTextView mPreButton;
    private AppCompatTextView mNextButton;
    private EditText editText;
    private LinearLayout editView;

//    private EditImageLayout mEditImageLayout;
    private DrawView drawView;
    private ViewPager mToolContentViewPager;

    private interface ToolSettingsListener {
        void onDrawSizeUpdated(int size);
        void onDrawColorUpdated(int color);
        void onTextSizeUpdated(int size);
        void onTextColorUpdated(int color);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupEditImageView();
        setupTopControlViews();
        setupToolContentViews();
    }

    private void setupEditImageView() {
        ArrayList<String> list = getIntent().getStringArrayListExtra(EXTRA_SELECTED_LIST);
        String imagePath = list.get(0);
        editText = (EditText) findViewById(R.id.editText);
        editView = (LinearLayout) findViewById(R.id.edit_view);
        drawView = findViewById(R.id.draw_view);
        drawView.setEditText(editText);
        drawView.setEditView(editView);
        Bitmap picture = BitmapFactory.decodeFile(imagePath);
        drawView.setBasePicture(picture);
        drawView.setSuperMode(true);
        findViewById(R.id.confirm_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                drawView.setText(text);
            }
        });

//        mEditImageLayout = findViewById(R.id.edit_image_view);
//        mEditImageLayout.setImagePath(imagePath);
//        mEditImageLayout.setEditImageHelperListener(new EditImageHelper.EditImageHelperListener() {
//            @Override
//            public void onPreNextStateUpdated(boolean canPre, boolean canNext) {
//                updatePreNextButtonState(canPre, canNext);
//            }
//        });
    }

    private void setupTopControlViews() {
        AppCompatTextView done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap  = drawView.saveBitmap();

//                mEditImageLayout.finishOperation();
//                mEditImageLayout.setDrawingCacheEnabled(true);
//                Bitmap bitmap = Bitmap.createBitmap(mEditImageLayout.getDrawingCache());
//                mEditImageLayout.setDrawingCacheEnabled(false);

                WAStickerManager.SaveTempImageFileTask task = new WAStickerManager.SaveTempImageFileTask(EditImageNewActivity.this, bitmap, new WAStickerManager.SaveTempImageFileCallback() {
                    @Override
                    public void onFinishSaved(String path) {
                        Intent intent = new Intent();
                        ArrayList<String> urls = new ArrayList<>();
                        urls.add(path);
                        intent.putStringArrayListExtra(EXTRA_SELECTED_LIST, urls);
                        setResult(RESULT_CODE_FINISH_EDIT_IMAGE, intent);
                        finish();
                    }
                });
                task.execute();
            }
        });

        AppCompatTextView cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mPreButton = findViewById(R.id.pre);
        mPreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mEditImageLayout.preOperation();
                drawView.lastStep();
            }
        });

        mNextButton = findViewById(R.id.next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mEditImageLayout.nextOperation();
                drawView.nextStep();
            }
        });
        updatePreNextButtonState(false, false);
    }

    private void setupToolContentViews() {
        mToolContentViewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tabs);

        TabAdapter adapter = new TabAdapter(this, new ToolSettingsListener() {
            @Override
            public void onDrawSizeUpdated(int size) {
                EditImageManager.getInstance().setDrawSettingSize(size);
            }

            @Override
            public void onDrawColorUpdated(int color) {
                EditImageManager.getInstance().setDrawSettingColor(color);
            }

            @Override
            public void onTextSizeUpdated(int size) {
//                EditImageManager.getInstance().setTextSettingSize(size);
            }

            @Override
            public void onTextColorUpdated(int color) {
//                EditImageManager.getInstance().setTextSettingColor(color);
            }
        });
        mToolContentViewPager.setAdapter(adapter);
        mToolContentViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mToolContentViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
//                    mEditImageLayout.finishOperation();
//                    mEditImageLayout.setCurrentToolType(EditImageHelper.ToolType.Draw);
                    drawView.setDrawType(DrawType.DRAW);
                    drawView.setPaintColor(Color.RED);
                    drawView.confirmText();
                } else {
//                    mEditImageLayout.finishOperation();
//                    mEditImageLayout.setCurrentToolType(EditImageHelper.ToolType.Text);
                    drawView.setTextColor(Color.RED);
                    drawView.setDrawType(DrawType.TEXT);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        TabLayout.Tab tabDraw = tabLayout.newTab();
        tabDraw.setCustomView(createTabView(tabLayout, getResources().getDrawable(R.drawable.ic_launcher)));
        tabLayout.addTab(tabDraw);
        TabLayout.Tab tabText = tabLayout.newTab();
        tabText.setCustomView(createTabView(tabLayout, getResources().getDrawable(R.drawable.sticker_3rdparty_add)));
        tabLayout.addTab(tabText);
        setTablayoutIndicatorPosition(tabLayout);
    }

    private void setTablayoutIndicatorPosition(TabLayout tabLayout) {
        //First rotate the tab layout
        tabLayout.setRotationX(180);
        //Find all childs for tablayout
        for (int i = 0; i <tabLayout.getChildCount() ; i++) {
            LinearLayout linearList = ((LinearLayout)tabLayout.getChildAt(i));
            for(int position = 0;position<linearList.getChildCount();position++) {
                //One by one again rotate layout for text and icons
                LinearLayout item=((LinearLayout) linearList.getChildAt(position));
                item.setRotationX(180);
            }
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_edit_image_new;
    }

    private void updatePreNextButtonState(boolean canPre, boolean canNext) {
        mPreButton.setEnabled(canPre);
        mPreButton.setClickable(canPre);
        mPreButton.setTextColor(canPre ? Color.BLACK : Color.GRAY);

        mNextButton.setEnabled(canNext);
        mNextButton.setClickable(canNext);
        mNextButton.setTextColor(canNext ? Color.BLACK : Color.GRAY);
    }

    private View createTabView(ViewGroup viewGroup, Drawable drawable) {
        AppCompatImageView imageView = new AppCompatImageView(viewGroup.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setImageDrawable(drawable);
        return imageView;
    }

    private static class TabAdapter extends PagerAdapter implements View.OnClickListener {
        private static final String TAG_DRAW_SIZE = "draw_size";
        private static final String TAG_DRAW_COLOR = "draw_color";
        private static final String TAG_TEXT_SIZE = "text_size";
        private static final String TAG_TEXT_COLOR = "text_size";

        private LayoutInflater layoutInflater;
        private ToolSettingsListener listener;
//        private DrawContentOnClickListener drawContentOnClickListener;
//        private TextContentOnClickListener textContentOnClickListener;

        private ArrayList<RoundCornerColorImageView> drawSizeViews = new ArrayList<>();
        private ArrayList<RoundCornerColorImageView> drawColorViews = new ArrayList<>();
        private ArrayList<RoundCornerColorImageView> textSizeViews = new ArrayList<>();
        private ArrayList<RoundCornerColorImageView> textColorViews = new ArrayList<>();

        TabAdapter(Context context, ToolSettingsListener listener) {
            this.layoutInflater = LayoutInflater.from(context);
            this.listener = listener;
//            this.drawContentOnClickListener = new DrawContentOnClickListener();
//            this.textContentOnClickListener = new TextContentOnClickListener();
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
                view = createDrawContent();
            } else {
                view = createTextContent();
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

//            RoundCornerColorImageView size1 = view.findViewById(R.id.draw_size_1);
//            size1.setTag(TAG_DRAW_SIZE);
//            size1.setMeaningSize(9);
//            size1.setOnClickListener(this);
//            drawSizeViews.add(size1);
//
//            RoundCornerColorImageView size2 = view.findViewById(R.id.draw_size_2);
//            size2.setTag(TAG_DRAW_SIZE);
//            size2.setMeaningSize(18);
//            size2.setOnClickListener(this);
//            drawSizeViews.add(size2);
//
//            RoundCornerColorImageView size3 = view.findViewById(R.id.draw_size_3);
//            size3.setTag(TAG_DRAW_SIZE);
//            size3.setMeaningSize(36);
//            size3.setOnClickListener(this);
//            drawSizeViews.add(size3);
//
//            RoundCornerColorImageView color1 = view.findViewById(R.id.draw_color_1);
//            color1.setTag(TAG_DRAW_COLOR);
//            color1.setColor(Color.BLUE);
//            color1.setOnClickListener(this);
//            drawColorViews.add(color1);
//
//            RoundCornerColorImageView color2 = view.findViewById(R.id.draw_color_2);
//            color2.setTag(TAG_DRAW_COLOR);
//            color2.setColor(Color.WHITE);
//            color2.setOnClickListener(this);
//            drawColorViews.add(color2);
//
//            RoundCornerColorImageView color3 = view.findViewById(R.id.draw_color_3);
//            color3.setTag(TAG_DRAW_COLOR);
//            color3.setColor(Color.YELLOW);
//            color3.setOnClickListener(this);
//            drawColorViews.add(color3);
//
//            RoundCornerColorImageView color4 = view.findViewById(R.id.draw_color_4);
//            color4.setTag(TAG_DRAW_COLOR);
//            color4.setColor(Color.GRAY);
//            color4.setOnClickListener(this);
//            drawColorViews.add(color4);
//
//            RoundCornerColorImageView color5 = view.findViewById(R.id.draw_color_5);
//            color5.setTag(TAG_DRAW_COLOR);
//            color5.setColor(Color.RED);
//            color5.setOnClickListener(this);
//            drawColorViews.add(color5);
//
//            RoundCornerColorImageView color6 = view.findViewById(R.id.draw_color_6);
//            color6.setTag(TAG_DRAW_COLOR);
//            color6.setColor(Color.CYAN);
//            color6.setOnClickListener(this);
//            drawColorViews.add(color6);
//
//            RoundCornerColorImageView color7 = view.findViewById(R.id.draw_color_7);
//            color7.setTag(TAG_DRAW_COLOR);
//            color7.setColor(Color.YELLOW);
//            color7.setOnClickListener(this);
//            drawColorViews.add(color7);
//
//            RoundCornerColorImageView color8 = view.findViewById(R.id.draw_color_8);
//            color8.setTag(TAG_DRAW_COLOR);
//            color8.setColor(Color.GREEN);
//            color8.setOnClickListener(this);
//            drawColorViews.add(color8);
            return view;
        }

        private View createTextContent() {
            View view = layoutInflater.inflate(R.layout.edit_image_text_content, null);
//            RoundCornerColorImageView size1 = view.findViewById(R.id.text_size_1);
//            size1.setTag(TAG_TEXT_SIZE);
//            size1.setMeaningSize(9);
//            size1.setOnClickListener(this);
//            textSizeViews.add(size1);
//
//            RoundCornerColorImageView size2 = view.findViewById(R.id.text_size_2);
//            size2.setTag(TAG_TEXT_SIZE);
//            size2.setMeaningSize(18);
//            size2.setOnClickListener(this);
//            textSizeViews.add(size2);
//
//            RoundCornerColorImageView size3 = view.findViewById(R.id.text_size_3);
//            size3.setTag(TAG_TEXT_SIZE);
//            size3.setMeaningSize(36);
//            size3.setOnClickListener(this);
//            textSizeViews.add(size3);
            return view;
        }

        @Override
        public void onClick(View v) {
            if (TAG_DRAW_SIZE.equals(v.getTag())) {
                int selectSize = 0;
                for (RoundCornerColorImageView sizeView : drawSizeViews) {
                    if (sizeView.equals(v)) {
                        sizeView.enableBorder();
                        selectSize = sizeView.getMeaningSize();
                    } else {
                        sizeView.disableBorder();
                    }
                }
                listener.onDrawSizeUpdated(selectSize);
            } else if (TAG_DRAW_COLOR.equals(v.getTag())) {
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
            } else if (TAG_TEXT_SIZE.equals(v.getTag())) {
                int selectSize = 0;
                for (RoundCornerColorImageView sizeView : textSizeViews) {
                    if (sizeView.equals(v)) {
                        sizeView.enableBorder();
                        selectSize = sizeView.getMeaningSize();
                    } else {
                        sizeView.disableBorder();
                    }
                }
                listener.onTextSizeUpdated(selectSize);
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
    }
}
