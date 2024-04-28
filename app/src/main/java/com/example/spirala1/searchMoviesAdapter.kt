package com.example.spirala1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class searchMoviesAdapter(
    private var movies: List<Movie>,
    private val onItemClicked: (movie:Movie) -> Unit,
) : RecyclerView.Adapter<searchMoviesAdapter.searchMovieViewHolder>() {
    private val posterPath: String = "https://image.tmdb.org/t/p/w342"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): searchMovieViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.layout, parent, false)
        return searchMovieViewHolder(view)
    }
    override fun getItemCount(): Int = movies.size
    override fun onBindViewHolder(holder: searchMovieViewHolder, position: Int) {
        holder.movieTitle.text = movies[position].title;
        val genreMatch: String? = movies[position].genre
        val context: Context = holder.movieImage.getContext()
        var id: Int = 0;
        if (genreMatch!==null)
            id = context.getResources()
                .getIdentifier(genreMatch, "drawable", context.getPackageName())
        if (id===0) id=context.getResources()
            .getIdentifier("picture1", "drawable", context.getPackageName())
        Glide.with(context)
            .load(posterPath + movies[position].posterPath)
            .centerCrop()
            .placeholder(R.drawable.picture1)
            .error(id)
            .fallback(id)
            .into(holder.movieImage);
        holder.movieImage.setImageResource(id)
        holder.itemView.setOnClickListener{ onItemClicked(movies[position]) }
    }
    fun updateMovies(movies: List<Movie>) {
        this.movies = movies
        notifyDataSetChanged()
    }
    inner class searchMovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val movieImage: ImageView = itemView.findViewById(R.id.movieImage)
        val movieTitle: TextView = itemView.findViewById(R.id.movieTitle)
    }
}