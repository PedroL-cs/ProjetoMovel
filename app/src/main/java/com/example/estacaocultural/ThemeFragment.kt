package com.example.estacaocultural

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.estacaocultural.databinding.FragmentThemeBinding

class ThemeFragment : Fragment() {
    private lateinit var binding: FragmentThemeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThemeBinding.inflate(inflater, container, false)


        return binding.root
    }

}