package com.jokku.jokeapp

import com.jokku.jokeapp.data.*
import com.jokku.jokeapp.data.entity.Joke
import com.jokku.jokeapp.data.entity.JokeServerModel
import com.jokku.jokeapp.data.source.*
import com.jokku.jokeapp.model.*
import com.jokku.jokeapp.util.ResourceManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class BaseModelTest {

    @Test
    fun test_change_joke_status(): Unit = runBlocking {
        val testCacheDataSource = TestCacheDataSource()
        val testCloudDataSource = TestCloudDataSource()
        val cachedJoke = BaseCachedJoke()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.google.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val cacheResultHandler = CacheResultHandler(
            cachedJoke,
            testCacheDataSource,
            NoCachedJokes(TestResourceManager())
        )
        val cloudResultHandler = CloudResultHandler(
            cachedJoke,
            BaseCloudDataSource(retrofit.create(JokeService::class.java)),
            NoConnection(TestResourceManager()),
            ServiceUnavailable(TestResourceManager())
        )
        val model = BaseModel(testCacheDataSource, cacheResultHandler, cloudResultHandler, cachedJoke)
        model.chooseDataSource(false)
        testCloudDataSource.getJokeWithResult(true)
        val joke = model.getJoke()
        assertEquals(joke is BaseJokeUiModel, true)
        model.changeJokeStatus()
        assertEquals(testCacheDataSource.checkContainsId(0), true)
    }

    private inner class TestCacheDataSource : CacheDataSource {
        private val map = HashMap<Int, Joke>()
        private var success = true
        private var nextJokeIdToGet = -1

        fun getNextJokeWithResult(success: Boolean, id: Int) {
            this.success = success
            nextJokeIdToGet = id
        }

        fun checkContainsId(id: Int) = map.isNotEmpty()

        override suspend fun addOrRemove(id: Int, joke: Joke): JokeUiModel {
            return if (map.containsKey(id)) {
                val uiModel = map[id]!!.toBaseUiJoke()
                map.remove(id)
                uiModel
            } else {
                map[id] = joke
                joke.toFavoriteUiJoke()
            }
        }

        override suspend fun getJoke(): Result<Joke, Unit> {
            return if (success)
                Result.Success(map[nextJokeIdToGet]!!)
            else
                Result.Error(Unit)
        }
    }

    private inner class TestCloudDataSource : CloudDataSource {
        private var success = true
        private var count = 0

        fun getJokeWithResult(success: Boolean) {
            this.success = success
        }

        override suspend fun getJoke(): Result<JokeServerModel, ErrorType> {
            return if (success) {
                Result.Success(
                    JokeServerModel(
                        count++, "TestPunchline$count", "TestSetup$count", "TestType"
                    )
                )
            } else {
                Result.Error(ErrorType.NO_CONNECTION)
            }
        }
    }

    private inner class TestResourceManager : ResourceManager {
        val message: String = ""

        override fun getString(stringResId: Int) = message
    }
}