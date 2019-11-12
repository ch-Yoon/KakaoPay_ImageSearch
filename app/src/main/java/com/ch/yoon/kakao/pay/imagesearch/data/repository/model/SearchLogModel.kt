package com.ch.yoon.kakao.pay.imagesearch.data.repository.model

/**
 * Creator : ch-yoon
 * Date : 2019-10-30
 **/
data class SearchLogModel(
    val keyword: String,
    val time: Long
) : Comparable<SearchLogModel> {

    override fun compareTo(other: SearchLogModel): Int {
        return when {
            time < other.time -> 1
            time > other.time -> -1
            else -> 0
        }
    }
}