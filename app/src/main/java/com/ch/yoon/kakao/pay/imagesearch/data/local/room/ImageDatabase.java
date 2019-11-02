package com.ch.yoon.kakao.pay.imagesearch.data.local.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ch.yoon.kakao.pay.imagesearch.data.local.room.dao.SearchLogDao;
import com.ch.yoon.kakao.pay.imagesearch.data.local.room.entity.SearchLog;

/**
 * Creator : ch-yoon
 * Date : 2019-08-05.
 */
@Database(entities = {SearchLog.class}, version = 1, exportSchema = false)
public abstract class ImageDatabase extends RoomDatabase {

    private static final String databaseName = "app_database";

    private static ImageDatabase INSTANCE;

    public static synchronized ImageDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                context.getApplicationContext(),
                ImageDatabase.class,
                databaseName
            ).build();
        }

        return INSTANCE;
    }

    public abstract SearchLogDao searchLogDao();

}