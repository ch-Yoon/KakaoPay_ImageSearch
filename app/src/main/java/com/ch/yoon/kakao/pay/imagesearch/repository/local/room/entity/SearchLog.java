package com.ch.yoon.kakao.pay.imagesearch.repository.local.room.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * Creator : ch-yoon
 * Date : 2019-08-06.
 */
@Entity(tableName = "searchLogs")
public class SearchLog implements Comparable<SearchLog> {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "keyword")
    private String keyword;

    @ColumnInfo(name = "time")
    private long time;

    public SearchLog(@NonNull String keyword, long time) {
        this.keyword = keyword;
        this.time = time;
    }

    @NonNull
    public String getKeyword() {
        return keyword;
    }

    public long getTime() {
        return time;
    }

    @NonNull
    @Override
    public String toString() {
        return "SearchLog{" +
            "keyword='" + keyword + '\'' +
            ", time=" + time +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchLog searchLog = (SearchLog) o;
        return time == searchLog.time &&
            keyword.equals(searchLog.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyword, time);
    }

    @Override
    public int compareTo(SearchLog o) {
        if(time < o.getTime()) {
            return 1;
        } else if(time > o.getTime()) {
            return -1;
        } else {
            return 0;
        }
    }

}