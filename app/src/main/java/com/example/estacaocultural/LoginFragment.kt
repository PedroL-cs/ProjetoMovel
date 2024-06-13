package com.example.estacaocultural

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.estacaocultural.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)

        binding.confirmButton.setOnClickListener {
            var chave : String = binding.textoChave.text.toString()
            var senha : String = binding.textoSenha.text.toString()

            auth.signInWithEmailAndPassword(chave, senha).addOnCompleteListener(requireActivity()) {
                task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(),"Login realizado com sucesso" , Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                } else {
                    Toast.makeText(requireContext(), "Chave ou senha inválidos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }
}