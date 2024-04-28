package uk.ac.tees.mad.d3656925.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uk.ac.tees.mad.d3656925.data.CommuterCarpoolingRepository
import uk.ac.tees.mad.d3656925.data.CommuterCarpoolingRepositoryImpl
import uk.ac.tees.mad.d3656925.data.DriverRepository
import uk.ac.tees.mad.d3656925.data.DriverRepositoryImplementation
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providesFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun providesFirebaseFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun providesRepo(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore
    ): CommuterCarpoolingRepository =
        CommuterCarpoolingRepositoryImpl(firebaseAuth, firebaseFirestore)

    @Provides
    @Singleton
    fun providesDriverRepo(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        @ApplicationContext applicationContext: Context
    ): DriverRepository =
        DriverRepositoryImplementation(firebaseAuth, firebaseFirestore, applicationContext)

}