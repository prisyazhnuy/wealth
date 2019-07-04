package com.prisyazhnuy.wealth.provider

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.prisyazhnuy.wealth.R
import com.prisyazhnuy.wealth.WealthApp
import com.prisyazhnuy.wealth.models.Client
import io.reactivex.Single


interface AssetProvider {
    fun loadClients(): Single<List<Client>>
}

class AssetProviderImpl : AssetProvider {

    private val mapper: ObjectMapper = ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        // send Date in "yyyy-MM-dd'T'HH:mm:ss.SSSZ" format
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .registerModule(JodaModule())
    private val objectReader = mapper.reader().forType(object : TypeReference<List<Client>>() {})

    override fun loadClients(): Single<List<Client>> {
        return Single.fromCallable { loadJSONFromAsset() }
            .map { objectReader.readValue<List<Client>>(it) }
    }

    private fun loadJSONFromAsset(): String {
        var json = ""
        WealthApp.instance.resources.openRawResource(R.raw.ngpo).use { stream ->
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            json = String(buffer, Charsets.UTF_8)
        }
        return json
    }

}