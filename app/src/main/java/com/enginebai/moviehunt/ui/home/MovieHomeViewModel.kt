package com.enginebai.moviehunt.ui.home

import com.enginebai.base.utils.Listing
import com.enginebai.base.utils.NetworkState
import com.enginebai.base.view.BaseViewModel
import com.enginebai.moviehunt.data.local.MovieModel
import com.enginebai.moviehunt.data.repo.MovieRepo
import com.enginebai.moviehunt.ui.list.MovieCategory
import io.reactivex.Observable
import org.koin.core.inject

class MovieHomeViewModel : BaseViewModel() {

    private val movieRepo: MovieRepo by inject()
    private val listingMap = mutableMapOf<MovieCategory, Listing<MovieModel>>()

    fun fetchList(category: MovieCategory): Listing<MovieModel> {
        val listing = movieRepo.fetchMovieList(category)
        listingMap[category] = listing
        return listing
    }

    fun getList(category: MovieCategory): Listing<MovieModel> {
        val listing = movieRepo.getMovieList(category)
        listingMap[category] = listing
        return listing
    }

    fun refresh() {
        listingMap.values.forEach {
            it.refresh.invoke()
        }
    }

    /**
     * Merge all category refresh state into one state
     */
    fun refreshState(): Observable<NetworkState> {
        val stateList = mutableListOf<Observable<NetworkState>>()
        listingMap.values.forEach {
            it.refreshState?.apply {
                stateList.add(this)
            }
        }
        return Observable.combineLatest(stateList) { states ->
            var mergedState = NetworkState.IDLE
            states.forEach {
                val s = it as NetworkState
                if (s == NetworkState.LOADING) {
                    mergedState = NetworkState.ERROR
                }
            }
            mergedState

        }
    }
}