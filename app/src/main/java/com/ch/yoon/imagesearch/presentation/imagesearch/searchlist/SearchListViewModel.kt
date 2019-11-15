package com.ch.yoon.imagesearch.presentation.imagesearch.searchlist

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ch.yoon.imagesearch.R
import com.ch.yoon.imagesearch.data.repository.error.RepositoryException
import com.ch.yoon.imagesearch.data.remote.kakao.request.ImageSearchRequest
import com.ch.yoon.imagesearch.data.remote.kakao.request.ImageSortType
import com.ch.yoon.imagesearch.data.repository.image.ImageRepository
import com.ch.yoon.imagesearch.data.repository.image.model.ImageDocument
import com.ch.yoon.imagesearch.data.repository.image.model.ImageSearchMeta
import com.ch.yoon.imagesearch.presentation.base.BaseViewModel
import com.ch.yoon.imagesearch.presentation.common.livedata.NonNullMutableLiveData
import com.ch.yoon.imagesearch.presentation.common.livedata.SingleLiveEvent
import com.ch.yoon.imagesearch.presentation.common.pageload.PageLoadHelper
import com.ch.yoon.imagesearch.extension.*
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Creator : ch-yoon
 * Date : 2019-10-28
 **/
class SearchListViewModel(
    application: Application,
    private val imageRepository: ImageRepository,
    private val pageLoadHelper: PageLoadHelper<String>
) : BaseViewModel(application) {

    init {
        observePageLoadInspector()
    }

    enum class ImageSearchState {
        SUCCESS, FAIL, NONE
    }

    private val _imageSortType = NonNullMutableLiveData(ImageSortType.ACCURACY)
    val imageSortType: LiveData<ImageSortType> = _imageSortType

    private val _countOfItemInLine = NonNullMutableLiveData(2)
    val countOfItemInLine: LiveData<Int> = _countOfItemInLine

    private val _imageDocumentList = MutableLiveData<MutableList<ImageDocument>>()
    val imageDocumentList: LiveData<List<ImageDocument>> = Transformations.map(_imageDocumentList) { it?.toList() }

    private val _imageSearchState = MutableLiveData<ImageSearchState>(ImageSearchState.NONE)
    val imageSearchState: LiveData<ImageSearchState> = _imageSearchState

    private val _moveToDetailScreenEvent = SingleLiveEvent<ImageDocument>()
    val moveToDetailScreenEvent: LiveData<ImageDocument> = _moveToDetailScreenEvent

    private val isRemainingMoreData
        get() = searchMeta?.isEnd?.not() ?: true

    private var searchMeta: ImageSearchMeta? = null

    fun changeCountOfItemInLine(countOfItemInLine: Int) {
        _countOfItemInLine.value = countOfItemInLine
    }

    fun onClickImage(imageDocument: ImageDocument) {
        _moveToDetailScreenEvent.value = imageDocument
    }

    fun observeChangingFavoriteImage() {
        imageRepository.observeChangingFavoriteImage()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ changedImageDocument ->
                _imageDocumentList.replace(changedImageDocument) { it.id == changedImageDocument.id }
            }, {
                Log.d(TAG, it.message)
            })
            .register()
    }

    fun changeImageSortType(imageSortType: ImageSortType) {
        _imageDocumentList.clear()
        _imageSortType.value = imageSortType
        pageLoadHelper.getCurrentKey()?.let { currentKey ->
            pageLoadHelper.requestFirstLoad(currentKey)
        }
    }

    fun loadImageList(keyword: String) {
        _imageDocumentList.clear()
        pageLoadHelper.requestFirstLoad(keyword)
    }

    fun retryLoadMoreImageList() {
        pageLoadHelper.requestRetryAsPreviousValue()
    }

    fun loadMoreImageListIfPossible(position: Int) {
        if (isRemainingMoreData) {
            pageLoadHelper.requestPreloadIfPossible(position, _imageDocumentList.size(), _countOfItemInLine.value)
        }
    }

    private fun observePageLoadInspector() {
        pageLoadHelper.onPageLoadApproveCallback = { key, pageNumber, dataSize, isFirstPage ->
            val request = ImageSearchRequest(key, _imageSortType.value, pageNumber, dataSize, isFirstPage)
            requestImageListToRepository(request)
        }
    }

    private fun requestImageListToRepository(request: ImageSearchRequest) {
        _imageSearchState.value = ImageSearchState.NONE

        imageRepository.requestImageList(request)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { _imageSearchState.value = ImageSearchState.SUCCESS }
            .doOnError { _imageSearchState.value = ImageSearchState.FAIL }
            .subscribe({ imageSearchResponse ->
                if (request.isFirstRequest) {
                    _imageDocumentList.clear()
                }
                with(imageSearchResponse) {
                    updateImageDocumentList(imageDocumentList)
                    updateSearchMeta(imageSearchMeta)
                }
            }, { throwable ->
                handlingImageSearchError(throwable)
            })
            .register()
    }

    private fun updateImageDocumentList(receivedImageDocumentList: List<ImageDocument>) {
        _imageDocumentList.addAll(receivedImageDocumentList)
        if(_imageDocumentList.isEmpty()) {
            updateShowMessage(R.string.success_image_search_no_result)
        }
    }

    private fun updateSearchMeta(searchMeta: ImageSearchMeta) {
        this.searchMeta = searchMeta
        if(_imageDocumentList.isNotEmpty() && searchMeta.isEnd) {
            updateShowMessage(R.string.success_image_search_last_data)
        }
    }

    private fun handlingImageSearchError(throwable: Throwable) {
        when(throwable) {
            is RepositoryException.NetworkNotConnectingException -> {
                updateShowMessage(R.string.network_not_connecting_error)
            }
            else -> {
                updateShowMessage(R.string.unknown_error)
                Log.d(TAG, throwable.message ?: "unknown error")
            }
        }
    }
}