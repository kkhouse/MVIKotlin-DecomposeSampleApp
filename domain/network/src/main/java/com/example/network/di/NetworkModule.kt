package com.example.network.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.example.network.PokemonService
import com.example.network.PokemonServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
class NetworkModule {

    companion object {
        const val ENDPOINT = "https://graphqlpokemon.favware.tech/v7"
    }

    @Provides
    fun provideOkHttpClient(
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    fun provideApolloClient(
        httpClient: OkHttpClient
    ) : ApolloClient = ApolloClient.Builder()
        .serverUrl(ENDPOINT)
        .okHttpClient(okHttpClient = httpClient)
        .build()

    @Provides
    fun provideNetworkService(apolloClient: ApolloClient) : PokemonService {
        return PokemonServiceImpl(client = apolloClient)
    }
}