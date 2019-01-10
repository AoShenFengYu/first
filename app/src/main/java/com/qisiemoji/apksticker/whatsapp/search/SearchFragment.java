package com.qisiemoji.apksticker.whatsapp.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.core.models.enums.MediaType;
import com.giphy.sdk.core.network.api.CompletionHandler;
import com.giphy.sdk.core.network.api.GPHApi;
import com.giphy.sdk.core.network.api.GPHApiClient;
import com.giphy.sdk.core.network.response.ListMediaResponse;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.recyclerview.SpacesItemDecoration;
import com.qisiemoji.apksticker.recyclerview.ptr.listener.IRefreshListener;
import com.qisiemoji.apksticker.recyclerview.refresh.CustomRefreshFrameLayout;
import com.qisiemoji.apksticker.whatsapp.Sticker;
import com.qisiemoji.apksticker.whatsapp.StickerPack;
import com.qisiemoji.apksticker.whatsapp.gifpick.GifPickActivity;
import com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.qisiemoji.apksticker.whatsapp.StickerPackListActivity.hotWords;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.EXTRA_SELECTED_LIST;

public class SearchFragment extends Fragment implements IRefreshListener, View.OnClickListener, GifSearchClickListener {
    private static final int PERMISSION_STORAGE_REQUEST_CODE = 100;
    public static final int SCAN_GIF_FILES_SUCCESS = 1000;
    public static final int SCAN_GIF_FILES_FAIL = 2000;

    private Handler handler;

    private LineBreakLayout defaultTrendTagsLayout;

    private RecyclerView recyclerView;

    private GifSearchAdapter adapter;

    private View rootView;

    protected CustomRefreshFrameLayout waveChannel;

    private ImageView backImg;

    private EditText editText;

    private ImageView sendImg;

    private int offset;

    private String currentTag;

    private LinkedList<Media> dataSet = new LinkedList<Media>();

    private ProgressBar progressBar;

    private RelativeLayout topLayout;

    private ArrayList<Integer> randomColors = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new MyHandler(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);
        topLayout = (RelativeLayout) rootView.findViewById(R.id.search_top_layout);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.search_recycleview);
        waveChannel = (CustomRefreshFrameLayout) rootView.findViewById(R.id.search_wave_channel);
        backImg = (ImageView) rootView.findViewById(R.id.search_back);
        sendImg = (ImageView) rootView.findViewById(R.id.search_send);
        editText = (EditText) rootView.findViewById(R.id.search_edittext);
        progressBar = (ProgressBar) rootView.findViewById(R.id.search_progress_bar);
        initNoNet();

        backImg.setOnClickListener(this);
        sendImg.setOnClickListener(this);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    clickSend();
                    return true;
                }
                return false;
            }
        });

        final StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager
                .VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(4));
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.addItemDecoration(new MyItemDecoration(getActivity()));
        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mLayoutManager.invalidateSpanAssignments(); //防止第一行到顶部有空白区域
            }
        });

        adapter = new GifSearchAdapter(getActivity(), this);
        adapter.setDataSet(dataSet);
        RefreshRecyclerViewAdapter refreshAdapter = new RefreshRecyclerViewAdapter<GifSearchAdapter>(adapter);
        refreshAdapter.footerVisible(true);
        recyclerView.setAdapter(refreshAdapter);

        waveChannel.disableWhenHorizontalMove(true);
        waveChannel.setTimeName(this.getClass().getSimpleName());
        waveChannel.setLoadingTextResId(R.string.str_footer_loading);
        waveChannel.setListener(this);
        waveChannel.setRefreshEnable(false);
        waveChannel.setLoadMoreEnable(true);
        if (getArguments() != null) {
            String tag = getArguments().getString("tag", "");
            if (!TextUtils.isEmpty(tag)) {
                editText.setText(tag);
//                editText.setSelection(editText.getText().length());
                fetch(tag);
                //避免软键盘弹起
                topLayout.setFocusable(true);
                topLayout.setFocusableInTouchMode(true);
//                getActivity().getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        } else {
            showSoftInput();
        }
//        waveChannel.setLoadMoreEnable(false,"ssss");


        bottomRecyclerView = rootView.findViewById(R.id.gifsearch_bottom_recyclerview);
        mSelectStickerAdapter = new SelectStickerAdapter(getActivity(), mAllStickerPaths, mSelectStickerAdapterCallback);
        GridLayoutManager ms = new GridLayoutManager(getActivity(),4);
        bottomRecyclerView.setLayoutManager(ms);
        bottomRecyclerView.setAdapter(mSelectStickerAdapter);

        createStickerPack = rootView.findViewById(R.id.create_sticker_pack);
        createStickerPack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO create sticker pack or publish
            }
        });

        defaultTrendTagsLayout = rootView.findViewById(R.id.lineBreakLayout);
        setupDefaultTrendTags(new LineBreakLayout.LineBreakLayoutListener() {
            @Override
            public void onSelect(String s) {
                editText.setText(s);
                clickSend();
                defaultTrendTagsLayout.setVisibility(View.GONE);
            }
        });

        initStickerPack();
        showContents();
        updateCreateStickerPakcRecyclerViewSize();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (waveChannel != null) {
            waveChannel.setListener(null);
            waveChannel.removeAllViews();
            waveChannel.clearAnimation();
            waveChannel = null;
        }
    }

    /**
     * 显示软键盘
     */
    private void showSoftInput() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, 0);
        } catch (Exception e) {

        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftInput() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        } catch (Exception e) {

        }
    }

    /**
     * 请求搜索内容
     * @param tag
     */
    private void fetch(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        dismissError();
        //首次搜索
        if (!tag.equals(currentTag)) {
            progressBar.setVisibility(View.VISIBLE);
            reset();
            currentTag = tag;
        }
        GPHApi client = new GPHApiClient("3otOKnzEUBswRmEYr6");
        client.search(tag, MediaType.gif, null, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse result, Throwable e) {
                if (result == null) {
                    noFound();
                    return;
                }

                if (result.getData() != null) {
                    for (Media gif : result.getData()) {
                        Log.v("giphy", gif.getId());
                    }

                    updateUI(result.getData(), currentTag);
                } else {
                    Log.e("giphy error", "No results found");
                    noFound();
                }

            }
        });
    }

    private void dismissError() {
        rootView.findViewById(R.id.no_net_layout).setVisibility(View.GONE);
    }

    protected void error() {
        progressBar.setVisibility(View.GONE);
        rootView.findViewById(R.id.no_net_layout).setVisibility(View.VISIBLE);
        ((TextView) rootView.findViewById(R.id.no_net_text)).setText(R.string.no_net);
        ((ImageView) rootView.findViewById(R.id.no_net_img)).setImageResource(R.drawable.gifkb_ic_connection);
        rootView.findViewById(R.id.no_net_refresh).setVisibility(View.VISIBLE);
    }

    protected void noFound() {
        progressBar.setVisibility(View.GONE);
        rootView.findViewById(R.id.no_net_layout).setVisibility(View.VISIBLE);
        ((TextView) rootView.findViewById(R.id.no_net_text)).setText(R.string.no_content);
        ((ImageView) rootView.findViewById(R.id.no_net_img)).setImageResource(R.drawable.gifkb_ic_no_found);
        rootView.findViewById(R.id.no_net_refresh).setVisibility(View.GONE);
    }

    private void initNoNet() {
        rootView.findViewById(R.id.no_net_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(currentTag)) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                dismissError();
                String tag = currentTag;
                reset();
                fetch(tag);
            }
        });
    }

    private void reset() {
        dataSet.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        offset = 0;
        currentTag = "";
    }

    private synchronized void updateUI(List<Media> searchInfos, String tag) {
        if (getActivity() == null || !isAdded()) {
            return;
        }
        //非本次搜索，抛弃
        if (!currentTag.equals(tag)) {
            return;
        }
        if (searchInfos == null || searchInfos.size() == 0) {
            if (getContext() != null) {
                error();
            }
            return;
        }
        if (adapter == null) {
            return;
        }
        offset++;
        if (searchInfos.size() <= 0) {
            fetch(currentTag);
            return;
        }
        waveChannel.refreshComplete(true);
        dataSet.addAll(searchInfos);
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh(boolean isPassive) {
    }

    @Override
    public void onLoadMore(int newState) {
        fetch(currentTag);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_back:
                clickBack();
                break;
            case R.id.search_send:
//                clickSend();
                editText.setText("");
                editText.requestFocus();
                showSoftInput();
                break;
            default:
                break;
        }
    }

    private void clickBack() {
        if (getActivity() == null || !isAdded()) {
            return;
        }
        getActivity().finish();
    }

    private void clickSend() {
        if (getActivity() == null || !isAdded()) {
            return;
        }
        String tag = editText.getText().toString();
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        hideSoftInput();
        reset();
        fetch(tag);
    }

    @Override
    public void clickItem(Media media) {
        if (media == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), GifPickActivity.class);
        intent.putExtra("media", media);
        getActivity().startActivityForResult(intent, 1);
    }

    private static class MyHandler extends Handler {
        WeakReference<SearchFragment> thisLayout;

        MyHandler(SearchFragment layout) {
            thisLayout = new WeakReference<SearchFragment>(layout);
        }

        @Override
        public void handleMessage(Message msg) {
            final SearchFragment theLayout = thisLayout.get();
            if (theLayout == null) {
                return;
            }

            switch (msg.what) {
                case SCAN_GIF_FILES_SUCCESS:
                    break;
                case SCAN_GIF_FILES_FAIL:
                    break;
                default:
                    break;
            }
        }
    }

    public static final int ADD_PACK = 200;
    private ArrayList<String> mAllStickerPaths = new ArrayList<>();
    /***
     * 底部已添加sticker的view
     */
    private ImageView createStickerPack;
    private RecyclerView bottomRecyclerView;
    private SelectStickerAdapter mSelectStickerAdapter;
    private WAStickerManager.PublishStickerPackTask mPublishStickerPackTask;
    private StickerPack mStickerPack;
    private String mPackName;
    private String mAuthor;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<String> list = data.getStringArrayListExtra(EXTRA_SELECTED_LIST);
        if (list != null && list.size() > 0) {
            handleReceivedImagePath(list.get(0));
        }
    }

    /**
     * 处理新图片
     * @param imagePath
     */
    public void handleReceivedImagePath(String imagePath) {
        if (imagePath == null) {
            return;
        }

        // RecyclerView
        mAllStickerPaths.add(0, imagePath);
        mSelectStickerAdapter.setStickerPaths(mAllStickerPaths);
        mSelectStickerAdapter.notifyDataSetChanged();

        // StickerPack
        String tmp = mStickerPack.trayImageFile;
        mStickerPack.trayImageFile = imagePath;
        if (tmp != null) {
            Sticker sticker = new Sticker(null, null);
            sticker.imageFileUrl = tmp;
            mStickerPack.stickers.add(0, sticker);
        }

        // Update local
        WAStickerManager.getInstance().setLastOperatedStickerPackStateByPriority(WAStickerManager.LastOperatedStickerPackState.Update);
        WAStickerManager.getInstance().update(getContext(), mStickerPack, WAStickerManager.FileStickerPackType.SearchLocal);

        updateCreateStickerPakcRecyclerViewSize();
    }

    private boolean isValidContent() {
        if (mStickerPack == null || mStickerPack.trayImageFile == null || mStickerPack.stickers == null) {
            return false;
        }

        // Whatsapp nees 1 icon 3 stickers
        int stickerCount = 0;
        for (Sticker sticker : mStickerPack.stickers) {
            if (sticker.imageFileUrl != null) {
                stickerCount = stickerCount + 1;
            }
            if (stickerCount >= 3) {
                return true;
            }
        }
        return false;
    }
    
    private interface SelectStickerAdapterCallback {
        void onClickDelete(String stickerPath, int index);
    }

    private SelectStickerAdapterCallback mSelectStickerAdapterCallback = new SelectStickerAdapterCallback() {
        @Override
        public void onClickDelete(String stickerPath, int index) {
            mAllStickerPaths.remove(index);
            mSelectStickerAdapter.notifyDataSetChanged();

            updateCreateStickerPakcRecyclerViewSize();

            if (index == 0) {
                if (mStickerPack.stickers.size() > 0) {
                    mStickerPack.trayImageFile = mStickerPack.stickers.get(0).imageFileUrl;
                    mStickerPack.stickers.remove(0);
                } else {
                    mStickerPack.trayImageFile = null;
                }
            } else {
                mStickerPack.stickers.remove(index-1);
            }

            WAStickerManager.getInstance().setLastOperatedStickerPackStateByPriority(WAStickerManager.LastOperatedStickerPackState.Update);
            WAStickerManager.getInstance().update(getContext(), mStickerPack, WAStickerManager.FileStickerPackType.SearchLocal);
        }
    };

    static class SelectStickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        static int TYPE_MAKER_ENTRY = 0x1000;
        static int TYPE_STICKER = 0x1001;

        ArrayList<String> stickerPaths;
        SelectStickerAdapterCallback selectStickerAdapterCallback;
        Drawable defaultBackground;

        SelectStickerAdapter(Context context, ArrayList<String> items, SelectStickerAdapterCallback cb) {
            stickerPaths = items;
            selectStickerAdapterCallback = cb;

            Drawable drawable = context.getResources().getDrawable(R.drawable.keyboard_sticker_default);
            drawable.mutate();
            drawable.setColorFilter(context.getResources().getColor(R.color.text_color_secondary), PorterDuff.Mode.SRC_ATOP);
            defaultBackground = drawable;
        }


        public void setStickerPaths(ArrayList<String> items) {
            stickerPaths = items;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_MAKER_ENTRY) {
                return new MakerEntryHolder(inflater.inflate(MakerEntryHolder.LAYOUT, parent, false), selectStickerAdapterCallback);
            } else {
                return new StickerHolder(inflater.inflate(StickerHolder.LAYOUT, parent, false), selectStickerAdapterCallback);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_MAKER_ENTRY) {
                ((MakerEntryHolder) holder).bind();
            }else {
                ((StickerHolder) holder).bind(stickerPaths.get(position), position, defaultBackground);
            }
        }

        @Override
        public int getItemCount() {
            return stickerPaths.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return TYPE_MAKER_ENTRY;
            }
            return TYPE_STICKER;
        }
    }

    static class MakerEntryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        static final int LAYOUT = R.layout.item_view_maker_entry;

        View itemView;
        SelectStickerAdapterCallback callback;

        MakerEntryHolder(View itemView, SelectStickerAdapterCallback cb) {
            super(itemView);
            this.itemView = itemView;
            this.callback = cb;
        }

        void bind() {
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (callback == null) {
                return;
            }

            // TODO maker entry
        }
    }

    static class StickerHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        static final int LAYOUT = R.layout.item_view_create_sticker_pack;

        String stickerItem;
        SelectStickerAdapterCallback callback;
        AppCompatImageView imageView;
        AppCompatImageView delete;
        int index;

        StickerHolder(View itemView, SelectStickerAdapterCallback cb) {
            super(itemView);
            callback = cb;
            imageView = itemView.findViewById(R.id.image);
            delete = itemView.findViewById(R.id.delete);
        }

        void bind(String stickerPath, int position, Drawable defaultBackground) {
            stickerItem = stickerPath;
            index = position;
            if (stickerPath != null) {
                delete.setOnClickListener(this);
                imageView.setImageDrawable(null);
                imageView.setOnClickListener(this);
                Glide.with(imageView.getContext())
                        .load(stickerPath)
                        .placeholder(defaultBackground)
                        .dontTransform()
                        .dontAnimate()
                        .into(imageView);
            }
        }

        @Override
        public void onClick(View v) {
            if (callback == null) {
                return;
            }

            if (v.getId() == R.id.delete) {
                callback.onClickDelete(stickerItem, index);
            }
        }
    }

    private void initStickerPack() {
        List<StickerPack> stickerPacks = WAStickerManager.getInstance().queryAll(getContext(), WAStickerManager.FileStickerPackType.SearchLocal);
        if (stickerPacks != null && stickerPacks.size() > 0) {
            mStickerPack = stickerPacks.get(0);
            if (mStickerPack.trayImageFile != null) {
                mAllStickerPaths.add(mStickerPack.trayImageFile);
            }

            for (Sticker sticker : mStickerPack.stickers) {
                mAllStickerPaths.add(sticker.imageFileUrl);
            }
        } else {
            List<Sticker> stickers = new ArrayList<>();
            mStickerPack = new StickerPack("search_local", mPackName, mAuthor, null, "", "", "", "");
            mStickerPack.setStickers(stickers);
            WAStickerManager.getInstance().save(getActivity(), mStickerPack, WAStickerManager.FileStickerPackType.SearchLocal);
        }
    }

    private void showContents() {
        // maker entry
        mAllStickerPaths.add("maker_entry");
        mSelectStickerAdapter.setStickerPaths(mAllStickerPaths);
        mSelectStickerAdapter.notifyDataSetChanged();

        // defaultTrendTagsLayout
        defaultTrendTagsLayout.setVisibility(View.VISIBLE);
    }

    private void updateCreateStickerPakcRecyclerViewSize() {
        if (mAllStickerPaths.size() > 4) {
            bottomRecyclerView.getLayoutParams().height = getResources().getDimensionPixelOffset(R.dimen.create_pack_rv_height_2);
        } else {
            bottomRecyclerView.getLayoutParams().height = getResources().getDimensionPixelOffset(R.dimen.create_pack_rv_height_1);
        }
    }

    private void setupDefaultTrendTags(LineBreakLayout.LineBreakLayoutListener listener) {
        randomColors.add(Color.RED);
        randomColors.add(Color.BLUE);
        randomColors.add(Color.BLACK);
        randomColors.add(Color.CYAN);
        randomColors.add(Color.GREEN);
        randomColors.add(Color.GRAY);
        randomColors.add(Color.YELLOW);

        List<LineBreakLayout.LineBreakLayoutItem> lable = new ArrayList<>();
        // fixme use hard code now, should get the list from server
        for (String word : hotWords) {
            lable.add(new LineBreakLayout.LineBreakLayoutItem(word, getRandomColor()));
        }
        lable.add(new LineBreakLayout.LineBreakLayoutItem("love", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("happy birthday", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem( "hello", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem( "yes", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("no", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("kiss", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("excited", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("sad", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("what", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("crying", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("happy", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("trump", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("hahaha", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("no", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("kiss", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("excited", getRandomColor()));
        lable.add(new LineBreakLayout.LineBreakLayoutItem("sad", getRandomColor()));

        defaultTrendTagsLayout.setItems(lable, true);
        defaultTrendTagsLayout.setLineBreakLayoutListener(listener);
        defaultTrendTagsLayout.setVisibility(View.GONE);
    }

    private int getRandomColor() {
        Random r = new Random();
        return randomColors.get(r.nextInt(randomColors.size()));

    }
}
