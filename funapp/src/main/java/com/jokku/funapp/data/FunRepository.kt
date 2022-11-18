package com.jokku.funapp.data

import com.jokku.funapp.data.cache.CacheDataSource
import com.jokku.funapp.data.cache.RepoCache
import com.jokku.funapp.data.cloud.CloudDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FunRepository<E> {
    suspend fun getFunItem(): RepoModel<E>
    suspend fun getFunItemList(): List<RepoModel<E>>
    suspend fun changeItemStatus(): RepoModel<E>
    suspend fun removeItem(id: E)
    fun chooseDataSource(cached: Boolean)
}

class BaseFunRepository<E>(
    private val cacheDataSource: CacheDataSource<E>,
    private val cloudDataSource: CloudDataSource<E>,
    private val repoCache: RepoCache<E>
) : FunRepository<E> {
    private var currentDataSource: DataFetcher<E> = cloudDataSource

    override fun chooseDataSource(cached: Boolean) {
        currentDataSource = if (cached) cacheDataSource else cloudDataSource
    }

    override suspend fun getFunItem(): RepoModel<E> = withContext(Dispatchers.IO) {
        try {
            val data = currentDataSource.getData()
            repoCache.save(data)
            data
        } catch (e: Exception) {
            repoCache.clear()
            throw e
        }
    }

    override suspend fun getFunItemList(): List<RepoModel<E>> = withContext(Dispatchers.IO) {
        cacheDataSource.getDataList()
    }

    override suspend fun changeItemStatus(): RepoModel<E> = withContext(Dispatchers.IO) {
        repoCache.changeItemStatus(cacheDataSource)
    }

    override suspend fun removeItem(id: E) {
        cacheDataSource.remove(id)
    }
}