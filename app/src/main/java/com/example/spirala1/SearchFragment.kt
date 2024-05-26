package com.example.spirala1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class SearchFragment : Fragment() {
    private lateinit var searchResults: RecyclerView
    private lateinit var searchText: EditText
    private lateinit var searchMoviesAdapter: searchMoviesAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_search, container, false)
        searchResults = view.findViewById(R.id.search_results)
        val searchButton = view.findViewById<ImageButton>(R.id.searchButton)
        searchButton.setOnClickListener {
            onClick()
        }
        searchResults.layoutManager = GridLayoutManager(activity, 2)
        searchText = view.findViewById(R.id.searchText)
        searchMoviesAdapter = searchMoviesAdapter(arrayListOf()) { movie -> showMovieDetails(movie) }
        searchResults.adapter=searchMoviesAdapter
        arguments?.getString("search")?.let {
            searchText.setText(it)
        }
        return view;
    }

    private fun showMovieDetails(movie: Movie) {
        val intent = Intent(activity, MovieDetailActivity::class.java).apply {
            putExtra("movie_title", movie.title)
        }
        startActivity(intent)
    }

    sealed class Result<out R> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }

    object MovieRepository {

        private const val tmdb_api_key : String = BuildConfig.TMDB_API_KEY

        suspend fun searchRequest(
            query: String
        ): Result<List<Movie>>{
            return withContext(Dispatchers.IO) {
                try {
                    val movies = arrayListOf<Movie>()
                    val url1 =
                        "https://api.themoviedb.org/3/search/movie?api_key=$tmdb_api_key&query=$query"
                    val url = URL(url1)
                    (url.openConnection() as? HttpURLConnection)?.run {
                        val result = this.inputStream.bufferedReader().use { it.readText() }
                        val jo = JSONObject(result)
                        val results = jo.getJSONArray("results")
                        for (i in 0 until results.length()) {
                            val movie = results.getJSONObject(i)
                            val title = movie.getString("original_title")
                            val id = movie.getInt("id")
                            val posterPath = movie.getString("poster_path")
                            val overview = movie.getString("overview")
                            val releaseDate = movie.getString("release_date")
                            movies.add(Movie(id.toLong(), title, overview, releaseDate, null, posterPath,null))
                            if (i == 5) break
                        }
                    }
                    return@withContext Result.Success(movies);
                }
                catch (e: MalformedURLException) {
                    return@withContext Result.Error(Exception("Cannot open HttpURLConnection"))
                } catch (e: IOException) {
                    return@withContext Result.Error(Exception("Cannot read stream"))
                } catch (e: JSONException) {
                    return@withContext Result.Error(Exception("Cannot parse JSON"))
                }

            }
        }

        suspend fun getUpcomingMovies(
        ) : GetMoviesResponse?{
            return withContext(Dispatchers.IO) {
                var response = Api.ApiAdapter.retrofit.getUpcomingMovies()
                val responseBody = response.body()
                return@withContext responseBody
            }
        }

        suspend fun getFavoriteMovies(context: Context) : List<Movie> {
            return withContext(Dispatchers.IO) {
                var db = AppDatabase.getInstance(context)
                var movies = db!!.movieDao().getAll()
                return@withContext movies
            }
        }
        suspend fun writeFavorite(context: Context,movie:Movie) : String?{
            return withContext(Dispatchers.IO) {
                try{
                    var db = AppDatabase.getInstance(context)
                    db!!.movieDao().insertAll(movie)
                    return@withContext "success"
                }
                catch(error:Exception){
                    return@withContext null
                }
            }
        }

    }

    val scope = CoroutineScope(Job() + Dispatchers.Main)

    //On Click handler
    private fun onClick() {
        val toast = Toast.makeText(context, "Search start", Toast.LENGTH_SHORT)
        toast.show()
        search(searchText.text.toString())
    }
    fun search(query: String){
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        // Kreira se Coroutine na UI
        scope.launch{
            // Vrti se poziv servisa i suspendira se rutina dok se `withContext` ne zavrsi
            val result = MovieRepository.searchRequest(query)
            // Prikaze se rezultat korisniku na glavnoj niti
            when (result) {
                is Result.Success<List<Movie>> -> searchDone(result.data)
                else-> onError()
            }
        }
    }

    fun searchDone(movies:List<Movie>){
        val toast = Toast.makeText(context, "Search done", Toast.LENGTH_SHORT)
        toast.show()
        searchMoviesAdapter.updateMovies(movies)
    }
    fun onError() {
        val toast = Toast.makeText(context, "Search error", Toast.LENGTH_SHORT)
        toast.show()
    }

}