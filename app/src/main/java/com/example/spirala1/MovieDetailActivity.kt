package com.example.spirala1

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MovieDetailActivity : AppCompatActivity() {
    private lateinit var movie: Movie
    private lateinit var addFavorite : Button
    private lateinit var title : TextView
    private lateinit var overview : TextView
    private lateinit var releaseDate : TextView
    private lateinit var genre : TextView
    private lateinit var website : TextView
    private lateinit var poster : ImageView
    private lateinit var shareButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)
        title = findViewById(R.id.movie_title)
        addFavorite = findViewById(R.id.addFavourite)
        overview = findViewById(R.id.movie_overview)
        releaseDate = findViewById(R.id.movie_release_date)
        genre = findViewById(R.id.movie_genre)
        poster = findViewById(R.id.movie_poster)
        website = findViewById(R.id.movie_website)
        shareButton = findViewById(R.id.shareButton)
        val extras = intent.extras
        if (extras != null) {
            movie = getMovieByTitle(extras.getString("movie_title",""))
            populateDetails()
        } else {
            finish()
        }
        website.setOnClickListener{
            showWebsite()
        }
        title.setOnClickListener {
            searchWebsite()
        }
        shareButton.setOnClickListener {
            shareMovieDetails()
        }
        addFavorite.setOnClickListener{
            writeDB(this,movie)
        }
    }

    fun writeDB(context: Context, movie:Movie){
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch{
            val result = SearchFragment.MovieRepository.writeFavorite(context,movie)
            when (result) {
                is String -> onSuccess1(result)
                else-> onError()
            }
        }
    }

    fun onSuccess1(message:String){
        val toast = Toast.makeText(applicationContext, "Spaseno", Toast.LENGTH_SHORT)
        toast.show()
        addFavorite.visibility= View.GONE
    }
    fun onError() {
        val toast = Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun populateDetails() {
        title.text=movie.title
        releaseDate.text=movie.releaseDate
        website.text=movie.homepage
        overview.text=movie.overview
        val context: Context = poster.context
    }
    private fun getMovieByTitle(name:String):Movie{
        val movies: ArrayList<Movie> = arrayListOf()
        movies.addAll(getRecentMovies())
        movies.addAll(getFavoriteMovies())
        val movie= movies.find { movie -> name == movie.title }
        return movie?:Movie(0,"Test","Test","Test","Test","Test","Test")
    }

    private fun showWebsite(){
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(movie.homepage))
        try {
            startActivity(webIntent)
        } catch (e: ActivityNotFoundException) {
        }
    }
    private fun searchWebsite(){
        val movieTitle = movie.title.replace(" ", "%20")
        val searchQuery = "$movieTitle+trailer"
        val googleUri = Uri.parse("https://www.google.com/search?q=$searchQuery")
        val webIntent = Intent(Intent.ACTION_VIEW, googleUri)
        try {
            startActivity(webIntent)
        } catch (e: ActivityNotFoundException) {
        }
    }
    private fun shareMovieDetails() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Movie Details")
        shareIntent.putExtra(Intent.EXTRA_TEXT, "${movie.title}: ${movie.overview}")
        val chooser = Intent.createChooser(shareIntent, "Share Movie Details")
        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        } else {
            Toast.makeText(this, "No app can handle this action", Toast.LENGTH_SHORT).show()
        }
    }
}