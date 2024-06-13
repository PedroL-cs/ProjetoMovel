package com.example.estacaocultural

import Painting_info
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var barLauncher: ActivityResultLauncher<ScanOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }

        barLauncher = registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                val obraId = result.contents
                fetchPaintingDetails(obraId)
            }
        }
    }

    private fun fetchPaintingDetails(obraId: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("Obras").document(obraId)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val painting = document.toObject(Painting::class.java)
                    painting?.let {
                        navigateToPaintingDetails(it)
                    } ?: run {
                        Log.d(TAG, "Documento encontrado, mas não foi possível converter para Painting")
                    }
                } else {
                    Log.d(TAG, "Documento não encontrado")
                    // Aqui você pode tratar o caso em que o documento não existe
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Erro ao buscar documento: $exception")
                // Aqui você pode lidar com falhas na busca do documento
            }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            R.id.nav_settings -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment()).commit()
            R.id.nav_scanner -> {
                scanCode()
                // iniciar leitura de QR
                //.replace(R.id.fragment_container, QRScannerFragment()).commit()
            }

        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun scanCode() {
        val options = ScanOptions()
        options.setPrompt("Posicione o código QR corretamente para a leitura")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        options.setCaptureActivity(CaptureAct::class.java)
        barLauncher.launch(options)
    }

    private fun navigateToPaintingDetails(painting: Painting) {
        val fragment = Painting_info.newInstance(painting)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Opcional: permite voltar à tela anterior
            .commit()
    }

}