
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.estacaocultural.Painting
import com.example.estacaocultural.R
import com.example.estacaocultural.databinding.FragmentPaintingInfoBinding
import com.example.estacaocultural.edit_painting_info
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.util.Locale

class Painting_info : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var binding:FragmentPaintingInfoBinding
    private lateinit var auth: FirebaseAuth
    private var painting: Painting? = null
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        arguments?.let {
            painting = it.getParcelable(ARG_PAINTING)
        }
        tts = TextToSpeech(requireContext(), this)
    }

    private fun speakDescription(description: String) {
        if (tts.isSpeaking) {
            tts.stop()
        } else {
            tts.speak(description, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(requireContext(), "Idioma não suportado", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Falha na inicialização do TextToSpeech", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = requireActivity() as AppCompatActivity

        binding = FragmentPaintingInfoBinding.inflate(inflater)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            binding.editButton.visibility = View.GONE
            updateQrButtonConstraints()
        } else {
            binding.editButton.visibility = View.VISIBLE
            binding.editButton.setOnClickListener{
                val editPaintingInfoFragment = edit_painting_info().apply {
                    arguments = Bundle().apply {
                        putParcelable("painting", painting)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container, editPaintingInfoFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.QRbutton.setOnClickListener{
            painting?.let {  painting ->
                val qrCodeBitmap = generateQRCode(painting.id)
                qrCodeBitmap?.let { bitmap ->
                    showQRCodePopup(bitmap)
                } ?: run {
                    Toast.makeText(requireContext(), "Erro ao gerar código QR", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.ttsButton.setOnClickListener {
            painting?.let {
                speakDescription(it.descricao)
            }
        }
        return binding.root
    }

    private fun updateQrButtonConstraints() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.root)
        constraintSet.connect(
            R.id.QRbutton,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            32
        )
        constraintSet.applyTo(binding.root)
    }

    // Dentro da sua Fragment ou Activity
    fun showQRCodePopup(qrCodeBitmap: Bitmap) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_qr_code, null)
        val imageViewQRCode = dialogView.findViewById<ImageView>(R.id.imageViewQRCode)
        imageViewQRCode.setImageBitmap(qrCodeBitmap)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Configuração para fechar o dialog ao clicar fora dele
        dialog.setCanceledOnTouchOutside(true)

        dialog.show()
    }


    private fun generateQRCode(content: String): Bitmap? {
        val writer = QRCodeWriter()
        return try {
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: WriterException) {
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val painting = arguments?.getParcelable<Painting>("painting")
        painting?.let {
            binding.paintingTitle.text = it.nomeObra
            binding.paintingAuthor.text = it.autor
            binding.paintingYear.text = it.ano
            binding.paintingDescription.text = it.descricao

            // Decodificar a string Base64 em bytes
            val decodedBytes = Base64.decode(painting.imagem, Base64.DEFAULT)

            Glide.with(this)
                .asBitmap()
                .load(decodedBytes)
                .into(binding.painting)
        }
    }


    companion object {
        private const val ARG_PAINTING = "painting"

        @JvmStatic
        fun newInstance(painting: Painting) =
            Painting_info().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PAINTING, painting)
                }
            }
    }
}