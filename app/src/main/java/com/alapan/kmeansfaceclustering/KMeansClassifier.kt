package com.alapan.kmeansfaceclustering

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import kotlin.math.sqrt

class KMeansClassifier(context: Context) {

    private val centroides: List<FloatArray>
    private val clusterYo: Int

    init {
        val jsonStr = context.assets.open("centroides_coseno.json")
            .bufferedReader(Charset.forName("UTF-8")).use { it.readText() }

        val obj = JSONObject(jsonStr)
        clusterYo = obj.getInt("cluster_yo")

        val raw = obj.getJSONArray("centroides")
        centroides = List(raw.length()) { i ->
            val arr = raw.getJSONArray(i)
            FloatArray(arr.length()) { j -> arr.getDouble(j).toFloat() }
        }
    }

    private fun distanciaCoseno(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        return 1 - (dot / (sqrt(normA) * sqrt(normB)))
    }

    fun clasificar(vector: FloatArray): Boolean {
        val distancias = centroides.map { distanciaCoseno(it, vector) }
        val cluster = if (distancias[0] < distancias[1]) 0 else 1
        return cluster == clusterYo
    }
}
