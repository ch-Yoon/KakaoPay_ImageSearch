package com.ch.yoon.kakao.pay.imagesearch.ui.imagesearch.imagelist;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ch.yoon.kakao.pay.imagesearch.R;
import com.ch.yoon.kakao.pay.imagesearch.extentions.SingleLiveEvent;
import com.ch.yoon.kakao.pay.imagesearch.repository.ImageRepository;
import com.ch.yoon.kakao.pay.imagesearch.repository.model.imagesearch.request.ImageSearchRequest;
import com.ch.yoon.kakao.pay.imagesearch.repository.model.imagesearch.request.ImageSortType;
import com.ch.yoon.kakao.pay.imagesearch.repository.model.imagesearch.response.ImageSearchResult;
import com.ch.yoon.kakao.pay.imagesearch.repository.model.imagesearch.response.SimpleImageInfo;
import com.ch.yoon.kakao.pay.imagesearch.repository.model.imagesearch.response.ResultMeta;
import com.ch.yoon.kakao.pay.imagesearch.repository.model.imagesearch.response.error.ImageSearchError;
import com.ch.yoon.kakao.pay.imagesearch.repository.model.imagesearch.response.error.ImageSearchException;
import com.ch.yoon.kakao.pay.imagesearch.ui.base.BaseViewModel;
import com.ch.yoon.kakao.pay.imagesearch.ui.imagesearch.imagelist.helper.ImageSearchInspector;
import com.ch.yoon.kakao.pay.imagesearch.utils.CollectionUtil;

import java.util.List;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Creator : ch-yoon
 * Date : 2019-08-01.
 */
public class ImageListViewModel extends BaseViewModel {

    private static final String TAG = ImageListViewModel.class.getName();
    private static final ImageSortType DEFAULT_IMAGE_SORT_TYPE = ImageSortType.ACCURACY;
    private static final int DEFAULT_COUNT_OF_ITEM_IN_LINE = 3;

    @NonNull
    private final ImageRepository imageRepository;
    @NonNull
    private final ImageSearchInspector imageSearchInspector;

    @NonNull
    private final MutableLiveData<Integer> countOfItemInLineLiveData = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<List<SimpleImageInfo>> imageInfoListLiveData = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<ImageSearchState> imageSearchStateLiveData = new MutableLiveData<>();

    @NonNull
    private final SingleLiveEvent<String> showMessageLiveEvent = new SingleLiveEvent<>();

    @Nullable
    private ResultMeta resultMeta;

    ImageListViewModel(@NonNull Application application,
                       @NonNull ImageRepository imageRepository,
                       @NonNull ImageSearchInspector imageSearchInspector) {
        super(application);
        this.imageRepository = imageRepository;
        this.imageSearchInspector = imageSearchInspector;

        init();
    }

    private void init() {
        countOfItemInLineLiveData.setValue(DEFAULT_COUNT_OF_ITEM_IN_LINE);
        imageSearchStateLiveData.setValue(ImageSearchState.NONE);
        observeImageSearchApprove();
    }

    @NonNull
    public LiveData<Integer> observeCountOfItemInLine() {
        return countOfItemInLineLiveData;
    }

    @NonNull
    public LiveData<List<SimpleImageInfo>> observeImageInfoList() {
        return imageInfoListLiveData;
    }

    @NonNull
    public LiveData<String> observeShowMessage() {
        return showMessageLiveEvent;
    }

    @NonNull
    public LiveData<ImageSearchState> observeImageSearchState() {
        return imageSearchStateLiveData;
    }

    public void changeCountOfItemInLine(int countOfItemInLine) {
        countOfItemInLineLiveData.setValue(countOfItemInLine);
    }

    public void loadImageList(@NonNull String keyword) {
        imageInfoListLiveData.setValue(null);
        imageSearchInspector.submitFirstImageSearchRequest(keyword, DEFAULT_IMAGE_SORT_TYPE);
    }

    public void retryLoadMoreImageList() {
        imageSearchInspector.submitRetryRequest(DEFAULT_IMAGE_SORT_TYPE);
    }

    public void loadMoreImageListIfPossible(int displayPosition) {
        if(remainingMoreData()) {
            final List<SimpleImageInfo> imageDocumentList = imageInfoListLiveData.getValue();
            if (imageDocumentList != null) {
                imageSearchInspector.submitPreloadRequest(
                    displayPosition,
                    imageDocumentList.size(),
                    DEFAULT_IMAGE_SORT_TYPE,
                    getCountOfItemInLine()
                );
            }
        }
    }

    private int getCountOfItemInLine() {
        Integer countOfItemInLine = countOfItemInLineLiveData.getValue();
        if(countOfItemInLine == null) {
            countOfItemInLine = DEFAULT_COUNT_OF_ITEM_IN_LINE;
        }

        return countOfItemInLine;
    }

    private void observeImageSearchApprove() {
        imageSearchInspector.observeImageSearchApprove(this::requestImageSearchToRepository);
    }

    private void requestImageSearchToRepository(ImageSearchRequest imageSearchRequest) {
        changeImageSearchState(ImageSearchState.NONE);

        registerDisposable(
            imageRepository.requestImageList(imageSearchRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handlingReceivedResponse, this::handlingError)
        );
    }

    private void handlingReceivedResponse(ImageSearchResult imageSearchResult) {
        changeImageSearchState(ImageSearchState.SUCCESS);

        List<SimpleImageInfo> oldList = imageInfoListLiveData.getValue();
        List<SimpleImageInfo> newList = imageSearchResult.getSimpleImageInfoList();
        if(CollectionUtil.isEmpty(oldList)) {
            oldList = newList;
        } else {
            oldList.addAll(newList);
        }

        resultMeta = imageSearchResult.getResultMeta();
        if(CollectionUtil.isEmpty(oldList)) {
            updateMessage(R.string.success_image_search_no_result);
        } else if(resultMeta.isLastData()){
            updateMessage(R.string.success_image_search_last_data);
        }

        imageInfoListLiveData.setValue(oldList);
    }

    private void handlingError(Throwable throwable) {
        changeImageSearchState(ImageSearchState.FAIL);

        if(throwable instanceof ImageSearchException) {
            final ImageSearchError error = ((ImageSearchException)throwable).getImageSearchError();
            updateMessage(error.getErrorMessageResourceId());
        }

        Log.d(TAG, throwable.getMessage());
    }

    private void updateMessage(@StringRes int stringResourceId) {
        final String message = getString(stringResourceId);
        showMessageLiveEvent.setValue(message);
    }

    private boolean remainingMoreData() {
        return resultMeta == null || ! resultMeta.isLastData();
    }

    private void changeImageSearchState(ImageSearchState imageSearchState) {
        ImageSearchState previousImageSearchState = imageSearchStateLiveData.getValue();
        if(previousImageSearchState != imageSearchState) {
            imageSearchStateLiveData.setValue(imageSearchState);
        }
    }

}
