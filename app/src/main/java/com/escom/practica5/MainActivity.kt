package com.escom.practica5

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.escom.practica5.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var analytics: FirebaseAnalytics
    private val db = Firebase.firestore
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        analytics = FirebaseAnalytics.getInstance(this)
        setupRecyclerView()
        setupClickListeners()

        logAnalyticsEvent("practica5_inicio")
        Toast.makeText(this, "Práctica 5 - Firebase Firestore", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            logAnalyticsEvent("producto_seleccionado", "producto" to product.nombre)
            Toast.makeText(this, "Seleccionado: ${product.nombre}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProducts.adapter = productAdapter
    }

    private fun setupClickListeners() {
        binding.btnCargarProductos.setOnClickListener {
            cargarProductosDesdeFirestore()
        }

        binding.btnBuscar.setOnClickListener {
            val query = binding.etBuscar.text.toString()
            if (query.isNotEmpty()) {
                buscarProductos(query)
            } else {
                cargarProductosDesdeFirestore()
            }
        }

        binding.btnLimpiar.setOnClickListener {
            productAdapter.updateList(emptyList())
            binding.etBuscar.setText("")
            binding.tvEstado.text = "Lista limpiada - Presiona Cargar Productos"
            logAnalyticsEvent("lista_limpiada")
        }
    }

    private fun cargarProductosDesdeFirestore() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEstado.text = "Cargando productos..."

        db.collection("productos")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val productList = mutableListOf<Producto>()

                for (document in querySnapshot) {
                    val producto = Producto(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "Sin nombre",
                        precio = document.getDouble("precio") ?: 0.0,
                        descripcion = document.getString("descripcion") ?: "Sin descripción",
                        categoria = document.getString("categoria") ?: "General",
                        enStock = document.getBoolean("enStock") ?: false
                    )
                    productList.add(producto)
                }

                productAdapter.updateList(productList)
                binding.progressBar.visibility = View.GONE
                binding.tvEstado.text = "✅ ${productList.size} productos cargados"
                logAnalyticsEvent("carga_exitosa", "cantidad" to productList.size.toString())
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                binding.tvEstado.text = "❌ Error: ${exception.message}"
                logAnalyticsEvent("error_carga", "error" to exception.message.toString())
                Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun buscarProductos(query: String) {
        binding.progressBar.visibility = View.VISIBLE

        db.collection("productos")
            .whereGreaterThanOrEqualTo("nombre", query)
            .whereLessThanOrEqualTo("nombre", query + "\uf8ff")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val productList = mutableListOf<Producto>()

                for (document in querySnapshot) {
                    val producto = Producto(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        precio = document.getDouble("precio") ?: 0.0,
                        descripcion = document.getString("descripcion") ?: "",
                        categoria = document.getString("categoria") ?: "",
                        enStock = document.getBoolean("enStock") ?: false
                    )
                    productList.add(producto)
                }

                productAdapter.updateList(productList)
                binding.progressBar.visibility = View.GONE
                binding.tvEstado.text = "🔍 ${productList.size} resultados para '$query'"
                logAnalyticsEvent("busqueda_realizada", "termino" to query, "resultados" to productList.size.toString())
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                binding.tvEstado.text = "❌ Error en búsqueda"
                logAnalyticsEvent("error_busqueda", "error" to exception.message.toString())
            }
    }

    private fun logAnalyticsEvent(eventName: String, vararg parameters: Pair<String, String>) {
        val bundle = Bundle().apply {
            putString("pantalla", "MainActivity")
            putString("fecha", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            putString("hora", SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()))
            for ((key, value) in parameters) {
                putString(key, value)
            }
        }
        analytics.logEvent(eventName, bundle)
    }
}