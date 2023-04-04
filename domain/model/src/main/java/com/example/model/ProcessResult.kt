package com.example.model

sealed class AppError {
    data class UnknownError(val error: Exception?) : AppError()
    object OtherError: AppError()
}

sealed class ProcessResult<out T> {
    data class Success<T>(val data: T) : ProcessResult<T>()
    data class Failure<T>(val error: AppError): ProcessResult<T>()
    object Loading: ProcessResult<Nothing>()

    fun <U> map(f: (T) -> U): ProcessResult<U> {
        return when(this) {
            is Success -> try {
                Success(f(this.data))
            } catch (e: Exception) {
                Failure(AppError.UnknownError(e))
            }
            is Failure -> Failure(this.error)
            Loading -> Loading
        }
    }

    fun filter(predicate : (T) -> Boolean): ProcessResult<T> {
        return when(this) {
            is Success -> try {
                if (predicate(this.data)) {
                    Success(this.data)
                } else {
                    Failure(AppError.OtherError)
                }
            } catch (e: Exception) {
                Failure(AppError.UnknownError(e))
            }
            is Failure -> Failure(this.error)
            Loading -> Loading
        }
    }

    suspend fun <U> mapAsync(f: suspend (T) -> U): ProcessResult<U> {
        return when(this) {
            is Success -> try {
                Success(f(this.data))
            } catch (e: Exception) {
                Failure(AppError.UnknownError(e))
            }
            is Failure -> Failure(this.error)
            Loading -> Loading
        }
    }

    suspend fun filterAsync(predicate : suspend (T) -> Boolean): ProcessResult<T> {
        return when(this) {
            is Success -> try {
                if (predicate(this.data)) {
                    Success(this.data)
                } else {
                    Failure(AppError.OtherError)
                }
            } catch (e: Exception) {
                Failure(AppError.UnknownError(e))
            }
            is Failure -> Failure(this.error)
            Loading -> Loading
        }
    }

    fun <U> flatMap(f: (T) -> ProcessResult<U>): ProcessResult<U> {
        return when(this) {
            is Success -> try {
                f(this.data)
            } catch (e: Exception) {
                Failure(AppError.UnknownError(e))
            }
            is Failure -> Failure(this.error)
            Loading -> Loading
        }
    }

    fun mapFailure(f: (AppError) -> AppError): ProcessResult<T> {
        return when(this) {
            is Success -> Success(this.data)
            is Failure -> Failure(f(this.error))
            Loading -> Loading
        }
    }

    suspend fun mapFailureAsync(f: suspend (AppError) -> AppError): ProcessResult<T> {
        return when(this) {
            is Success -> Success(this.data)
            is Failure -> Failure(f(this.error))
            Loading -> Loading
        }
    }

    fun <U> mapEach(
        onSuccess: (T) -> U,
        onFailure: (AppError) -> AppError
    ): ProcessResult<U> {
        return when(this) {
            is Success -> Success(onSuccess(this.data))
            is Failure -> Failure(onFailure(this.error))
            Loading -> Loading
        }
    }

    suspend fun <U> mapEachAsync(
        onSuccess: suspend (T) -> U,
        onFailure: suspend (AppError) -> AppError,
    ): ProcessResult<U> {
        return when(this) {
            is Success -> Success(onSuccess(this.data))
            is Failure -> Failure(onFailure(this.error))
            Loading -> Loading
        }
    }

    fun forEach(
        onSuccess: (T) -> Unit,
        onFailure: (AppError) -> Unit
    ) {
        when(this) {
            is Success -> onSuccess(this.data)
            is Failure -> onFailure(this.error)
            Loading -> Loading
        }
    }

    suspend fun forEachAsync(
        onSuccess: suspend (T) -> Unit,
        onFailure: suspend (AppError) -> Unit,
        onLoading: suspend () -> Unit
    ) {
        when(this) {
            is Success -> onSuccess(this.data)
            is Failure -> onFailure(this.error)
            Loading -> onLoading()
        }
    }

    fun tap(
        f: (T) -> Unit
    ): ProcessResult<T> {
        return when(this) {
            is Success -> {
                f(this.data)
                this
            }
            else -> this
        }
    }

    fun get(): T? {
        return when(this) {
            is Success -> this.data
            else -> null
        }
    }
}
