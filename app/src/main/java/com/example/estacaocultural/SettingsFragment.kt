package com.example.estacaocultural

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.estacaocultural.databinding.FragmentSettingsBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingsFragment : Fragment() {

    private lateinit var binding:FragmentSettingsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = requireActivity() as AppCompatActivity
        val navigationView = activity.findViewById<NavigationView>(R.id.nav_view)

        binding = FragmentSettingsBinding.inflate(inflater)

        binding.loginButton.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.acessibilityButon.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, AcessibilityFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.themesButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, ThemeFragment())
                .addToBackStack(null)
                .commit()
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            binding.logOutButton.visibility = View.GONE
        } else {
            binding.logOutButton.visibility = View.VISIBLE
            binding.logOutButton.setOnClickListener {
                AlertDialog.Builder(requireContext()).setTitle("Tem certeza?")
                    .setPositiveButton("Sim"){_,_ ->
                        Firebase.auth.signOut()
                        binding.logOutButton.visibility = View.GONE
                    }
                        .setNegativeButton("Não", null)
                        .show()
            }
        }

        return binding.root
    }
}