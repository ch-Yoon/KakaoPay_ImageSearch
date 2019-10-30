package com.ch.yoon.kakao.pay.imagesearch.data.repository;

import com.ch.yoon.kakao.pay.imagesearch.RxSchedulerRule;
import com.ch.yoon.kakao.pay.imagesearch.data.repository.image.ImageLocalDataSource;
import com.ch.yoon.kakao.pay.imagesearch.data.repository.image.ImageRemoteDataSource;
import com.ch.yoon.kakao.pay.imagesearch.data.repository.image.ImageRepository;
import com.ch.yoon.kakao.pay.imagesearch.data.repository.image.ImageRepositoryImpl;
import com.ch.yoon.kakao.pay.imagesearch.data.source.local.room.entity.SearchLogModel;
import com.ch.yoon.kakao.pay.imagesearch.data.source.remote.kakao.request.ImageSearchRequest;
import com.ch.yoon.kakao.pay.imagesearch.data.source.remote.kakao.request.ImageSortType;
import com.ch.yoon.kakao.pay.imagesearch.data.source.remote.kakao.response.KakaoImageSearchResponse;
import com.ch.yoon.kakao.pay.imagesearch.data.source.remote.kakao.response.KakaoImageSearchMetaInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ImageRepositoryImplTest {

    @Rule
    public RxSchedulerRule rxSchedulerRule = new RxSchedulerRule();

    @Mock
    private ImageRemoteDataSource mockImageRemoteDataSource;
    @Mock
    private ImageLocalDataSource mockImageLocalDataSource;

    private ImageRepository imageRepository;
    private CompositeDisposable compositeDisposable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        initImageRepository();
        initCompositeDisposable();
    }

    @After
    public void clear() {
        compositeDisposable.clear();
    }

    private void initImageRepository() {
        imageRepository = new ImageRepositoryImpl(mockImageRemoteDataSource, mockImageLocalDataSource);
    }

    private void initCompositeDisposable() {
        compositeDisposable = new CompositeDisposable();
    }

    @Test
    public void 리모트_데이터소스에_이미지_요청을_전달하는지_테스트() {
        // given
        when(mockImageRemoteDataSource.requestImageList(any(ImageSearchRequest.class)))
            .thenReturn(Single.just(emptyImageSearchResponse()));

        // when
        imageRepository.requestImageList(emptyImageSearchRequest());

        // then
        verify(mockImageRemoteDataSource, times(1)).requestImageList(any(ImageSearchRequest.class));
    }

    @Test
    public void 로컬_데이터소스에_키워드_업데이트_요청을_전달하는지_테스트() {
        // given
        when(mockImageLocalDataSource.insertOrUpdateSearchLog("테스트"))
            .thenReturn(Single.just(emptySearchLog()));

        // when
        imageRepository.insertOrUpdateSearchLog("테스트");

        // then
        verify(mockImageLocalDataSource, times(1)).insertOrUpdateSearchLog("테스트");
    }

    @Test
    public void 로컬_데이터소스에_검색_목록_요청을_전달하는지_테스트() {
        // given
        when(mockImageLocalDataSource.selectAllSearchLog())
            .thenReturn(Single.just(emptySearchLogList()));

        // when
        imageRepository.requestSearchLogList();

        // then
        verify(mockImageLocalDataSource, times(1)).selectAllSearchLog();
    }

    @Test
    public void 로컬_데이터소스에_키워드_데이터_삭제_요청을_전달하는지_테스트() {
        // given
        when(mockImageLocalDataSource.deleteSearchLog("테스트"))
            .thenReturn(Completable.complete());

        // when
        imageRepository.deleteSearchLog("테스트");

        // then
        verify(mockImageLocalDataSource, times(1)).deleteSearchLog("테스트");
    }

    private KakaoImageSearchResponse emptyImageSearchResponse() {
        KakaoImageSearchMetaInfo emptyKakaoImageSearchMetaInfo = new KakaoImageSearchMetaInfo(false);
        return new KakaoImageSearchResponse(emptyKakaoImageSearchMetaInfo, new ArrayList<>());
    }

    private ImageSearchRequest emptyImageSearchRequest() {
        return new ImageSearchRequest("", ImageSortType.ACCURACY, 0, 0, true);
    }

    private List<SearchLogModel> emptySearchLogList() {
        return new ArrayList<>();
    }

    private SearchLogModel emptySearchLog() {
        return new SearchLogModel("", 0);
    }

}
