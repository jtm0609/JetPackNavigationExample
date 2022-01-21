package com.example.pagingexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration

class MainActivity : AppCompatActivity() {

    companion object{
        private const val LAST_SEARCH_QUERY: String="last_search_query"
        private const val DEFAULT_QUERY="Android"
    }
    private lateinit var viewModel:SearchRepositoriesViewModel
    private val adapter=ReposAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel= ViewModelProviders.of(this)
            .get(SearchRepositoriesViewModel::class.java)

        //add dividers between RecyclerView's row items
        val decoraton=DividerItemDecoration(this,DividerItemDecoration.VERTICAL)




    }
}