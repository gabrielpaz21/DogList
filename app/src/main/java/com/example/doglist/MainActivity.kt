package com.example.doglist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doglist.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MainActivity : AppCompatActivity(), OnQueryTextListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: DogAdapter
    private val dogImages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.svDogs.setOnQueryTextListener(this)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = DogAdapter(dogImages)
        binding.rvDogs.layoutManager = LinearLayoutManager(this)
        binding.rvDogs.adapter = adapter
    }

    private fun getRetrofit(): Retrofit = Retrofit
        .Builder()
        .baseUrl("https://dog.ceo/api/breed/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(getClient())
        .build()

    private fun getClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HeaderInterceptor())
        .build()

    private fun searchByName(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val call: Response<DogsResponse> =
                getRetrofit().create(ApiService::class.java).getDogsByBreeds("$query/images")
            val puppies: DogsResponse? = call.body()
            runOnUiThread {
                if (call.isSuccessful) {
                    val images = puppies?.images ?: emptyList()
                    dogImages.clear()
                    dogImages.addAll(images)
                    adapter.notifyDataSetChanged()
                } else {
                    showError()
                }
                hideKeyboard()
            }
        }
    }

    private fun hideKeyboard() {
        val imn = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imn.hideSoftInputFromWindow(binding.viewRoot.windowToken, 0)
    }

    private fun showError() {
        Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (!query.isNullOrEmpty()) {
            searchByName(query.trim().lowercase(Locale.getDefault()))
        }
        return true
    }

    override fun onQueryTextChange(p0: String?): Boolean = true
}