package com.ch.yoon.kakao.pay.imagesearch.data.repository.image.model

/**
 * Creator : ch-yoon
 * Date : 2019-10-30
 **/
data class ImageSearchResponse(
    val imageSearchMeta: ImageSearchMeta,
    val imageDocumentList: List<ImageDocument>
)