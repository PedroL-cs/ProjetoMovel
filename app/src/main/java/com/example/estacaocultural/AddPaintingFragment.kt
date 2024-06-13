package com.example.estacaocultural

import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.estacaocultural.databinding.FragmentAddPaintingBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream

class AddPaintingFragment : Fragment() {

    private lateinit var binding: FragmentAddPaintingBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPaintingBinding.inflate(inflater)
        val db = Firebase.firestore

        val nomeobra = binding.addTitle
        val nomeautor = binding.addAuthor
        val ano = binding.addYearPlace
        val descricao = binding.addDescription

        registerResult()
        binding.setImage.setOnClickListener {
            pickImage()
        }

        binding.saveButton.setOnClickListener {
            AlertDialog.Builder(requireContext()).setTitle("Tem certeza que deseja salvar estas informações?")
                .setPositiveButton("Sim"){_,_ ->
                    if (imageBitmap != null) {
                        val base64Image = bitmapToBase64(imageBitmap!!)

                        val data = hashMapOf(
                            "id" to "",
                            "nomeObra" to nomeobra.text.toString(),
                            "autor" to nomeautor.text.toString(),
                            "ano" to ano.text.toString(),
                            "descricao" to descricao.text.toString(),
                            "imagem" to base64Image
                        )

                        db.collection("Obras").add(data)
                            .addOnSuccessListener { documentReference ->
                                val id = documentReference.id
                                documentReference.update("id", id)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Document ID updated: $id")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Error updating document ID", e)
                                    }

                                // Sucesso! O documento foi adicionado ao Firestore.
                                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                                requireActivity().onBackPressed()
                            }
                            .addOnFailureListener { e ->
                                // Ocorreu um erro ao tentar adicionar o documento ao Firestore.
                                Toast.makeText(requireContext(), "Erro ao salvar imagem. Tente salvar em uma resolução menor", Toast.LENGTH_SHORT).show()
                                Log.w(TAG, "Error adding document", e)
                            }


                    } else {
                        Toast.makeText(requireContext(), "Por favor, selecione uma imagem", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Não", null)
                .show()
        }

        binding.cancelButton2.setOnClickListener {
            requireActivity().onBackPressed()
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
                    val bitmap = decodeSampledBitmapFromUri(requireContext().contentResolver, it, binding.setImage.width, binding.setImage.height)
                    imageBitmap = bitmap
                    bitmap?.let { resizedBitmap ->
                        binding.setImage.setImageBitmap(resizedBitmap)
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