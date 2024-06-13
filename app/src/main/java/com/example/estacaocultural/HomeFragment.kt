package com.example.estacaocultural

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.estacaocultural.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PaintingAdapter
    private var paintings: List<Painting> = listOf() // Lista original de pinturas


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        val user = FirebaseAuth.getInstance().currentUser

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (user == null) {
            binding.addButton.visibility = View.GONE
        } else {
            binding.addButton.visibility = View.VISIBLE

            binding.addButton.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container, AddPaintingFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        fetchPaintings()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPaintings(newText.orEmpty())
                return true
            }
        })

        return binding.root
    }

    private fun fetchPaintings() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Obras").get()
            .addOnSuccessListener { result ->
                paintings = result.map { document ->
                    document.toObject(Painting::class.java)
                }
                adapter = PaintingAdapter(paintings) { painting ->
                    // Handle click event here, e.g., navigate to Painting_info fragment
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.fragment_container, Painting_info.newInstance(painting))
                        .addToBackStack(null)
                        .commit()
                }
                binding.recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun filterPaintings(query: String) {
        val filteredList = paintings.filter {
            it.nomeObra.contains(query, ignoreCase = true) || it.autor.contains(query, ignoreCase = true)
        }
        adapter.updateList(filteredList)
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}