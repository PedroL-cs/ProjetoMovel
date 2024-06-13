package com.example.estacaocultural

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.estacaocultural.databinding.FragmentEditPaintingInfoBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream

class edit_painting_info : Fragment() {
    private lateinit var binding: FragmentEditPaintingInfoBinding
    private lateinit var painting: Painting
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getParcelable<Painting>("painting")?.let { receivedPainting ->
            painting = receivedPainting
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditPaintingInfoBinding.inflate(inflater)
        val db = Firebase.firestore
        // Decodificar a string Base64 em bytes
        val decodedBytes = Base64.decode(painting.imagem, Base64.DEFAULT)

        registerResult()
        binding.imageView.setOnClickListener {
            pickImage()
        }

        Glide.with(this)
            .asBitmap()
            .load(decodedBytes)
            .into(binding.imageView)

        binding.apply {
            addTitle.setText(painting.nomeObra)
            addAuthor.setText(painting.autor)
            addYearPlace.setText(painting.ano)
            addDescription.setText(painting.descricao)

            saveButton.setOnClickListener {
                AlertDialog.Builder(requireContext()).setTitle("Tem certeza que deseja salvar estas informações?")
                    .setPositiveButton("Sim") { _, _ ->
                        if (imageBitmap != null) {
                            val base64Image = bitmapToBase64(imageBitmap!!)
                            val document = db.collection("Obras").document(painting.id)
                            document.update(
                                mapOf(
                                    "nomeObra" to addTitle.text.toString(),
                                    "autor" to addAuthor.text.toString(),
                                    "ano" to addYearPlace.text.toString(),
                                    "descricao" to addDescription.text.toString(),
                                    "imagem" to base64Image
                                )
                            ).addOnSuccessListener {
                                parentFragmentManager.beginTransaction()
                                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                    .replace(R.id.fragment_container, HomeFragment())
                                    .commit()
                                Toast.makeText(
                                    requireContext(),
                                    "Informações da obra atualizadas com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.addOnFailureListener { exception ->
                                Toast.makeText(
                                    requireContext(),
                                    "Erro ao atualizar informações da obra: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val document = db.collection("Obras").document(painting.id)
                            document.update(
                                mapOf(
                                    "nomeObra" to addTitle.text.toString(),
                                    "autor" to addAuthor.text.toString(),
                                    "ano" to addYearPlace.text.toString(),
                                    "descricao" to addDescription.text.toString(),
                                )
                            ).addOnSuccessListener {
                                parentFragmentManager.beginTransaction()
                                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                    .replace(R.id.fragment_container, HomeFragment())
                                    .commit()
                                Toast.makeText(
                                    requireContext(),
                                    "Informações da obra atualizadas com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.addOnFailureListener { exception ->
                                Toast.makeText(
                                    requireContext(),
                                    "Erro ao atualizar informações da obra: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }

            deleteButton.setOnClickListener {
                AlertDialog.Builder(requireContext()).setTitle("Tem certeza que deseja deletar esta obra?")
                    .setPositiveButton("Sim") { _, _ ->
                        db.collection("Obras").document(painting.id)
                            .delete()
                            .addOnSuccessListener {
                                parentFragmentManager.beginTransaction()
                                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                    .replace(R.id.fragment_container, HomeFragment())
                                    .commit()
                                Toast.makeText(
                                    requireContext(),
                                    "Obra deletada com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    requireContext(),
                                    "Erro ao deletar obra: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }

            cancelButton.setOnClickListener {
                AlertDialog.Builder(requireContext()).setTitle("Tem certeza que deseja descartar alterações?")
                    .setPositiveButton("Sim") { _, _ ->
                        parentFragmentManager.beginTransaction()
                            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                    }
                    .setNegativeButton("Não", null)
            }
        }

        return binding.root
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    // Método principal que vai habilitar a seleção de imagem da galeria
    private fun registerResult() {
        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            try {
                val data: Intent? = result.data
                val imageURI: Uri? = data?.data
                imageURI?.let {
                    val bitmap = decodeSampledBitmapFromUri(requireContext().contentResolver, it, binding.imageView.width, binding.imageView.height)
                    imageBitmap = bitmap
                    bitmap?.let { resizedBitmap ->
                        binding.imageView.setImageBitmap(resizedBitmap)
                    } ?: run {
                        Toast.makeText(requireContext(), "Error decoding image", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error selecting image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Converte imagem em string de base 64
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    // Métodos para decodificar seleção de imagem (não entendo nada disso)
    private fun decodeSampledBitmapFromUri(contentResolver: ContentResolver, imageUri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        contentResolver.openInputStream(imageUri).use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        return contentResolver.openInputStream(imageUri).use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}