package com.example.ung_dung_chay_bo.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.ung_dung_chay_bo.core.utils.Constants
import com.example.ung_dung_chay_bo.data.local.TrackingDatabase
import com.example.ung_dung_chay_bo.data.local.dao.ActivityRecordDao
import com.example.ung_dung_chay_bo.data.local.dao.TrackPointDao
import com.example.ung_dung_chay_bo.data.repository.ActivityRepositoryImpl
import com.example.ung_dung_chay_bo.data.repository.ChatRepositoryImpl
import com.example.ung_dung_chay_bo.data.repository.CommunityRepositoryImpl
import com.example.ung_dung_chay_bo.data.repository.PostRepositoryImpl
import com.example.ung_dung_chay_bo.data.repository.UserRepositoryImpl
import com.example.ung_dung_chay_bo.domain.repository.ActivityRepository
import com.example.ung_dung_chay_bo.domain.repository.ChatRepository
import com.example.ung_dung_chay_bo.domain.repository.CommunityRepository
import com.example.ung_dung_chay_bo.domain.repository.PostRepository
import com.example.ung_dung_chay_bo.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.USER_PREFERENCES_NAME)

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindCommunityRepository(impl: CommunityRepositoryImpl): CommunityRepository

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.dataStore
        }

        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        @Provides
        @Singleton
        fun provideTrackingDatabase(@ApplicationContext context: Context): TrackingDatabase {
            return Room.databaseBuilder(
                context,
                TrackingDatabase::class.java,
                "tracking_database"
            ).fallbackToDestructiveMigration().build()
        }

        @Provides
        @Singleton
        fun provideTrackPointDao(db: TrackingDatabase): TrackPointDao = db.trackPointDao()

        @Provides
        @Singleton
        fun provideActivityRecordDao(db: TrackingDatabase): ActivityRecordDao = db.activityRecordDao()
    }
}
