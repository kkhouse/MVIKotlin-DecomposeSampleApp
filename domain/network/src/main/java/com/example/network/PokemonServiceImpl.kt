package com.example.network

import com.apollographql.apollo3.ApolloClient
import com.badoo.reaktive.coroutinesinterop.singleFromCoroutine
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.observeOn
import com.badoo.reaktive.single.subscribeOn
import com.example.model.AppError
import com.example.model.ProcessResult
import javax.inject.Inject

class PokemonServiceImpl @Inject constructor(
    private val client : ApolloClient
): PokemonService {

    override fun getAllPokemon(): Single<ProcessResult<List<GetPokemonQuery.GetAllPokemon>>> {
        return singleFromCoroutine {
            try {
                client.query(GetPokemonQuery()).execute().data?.let { data ->
                    ProcessResult.Success(data.getAllPokemon)
                } ?: ProcessResult.Failure(AppError.UnknownError(null))
            } catch (e: Exception) {
                ProcessResult.Failure(AppError.UnknownError(e))
            }
        }.subscribeOn(ioScheduler).observeOn(mainScheduler)
    }
}