package ar.edu.unq.desapp.grupoa.backenddesappapi.service.impl

import ar.edu.unq.desapp.grupoa.backenddesappapi.model.CryptoCurrencyEnum
import ar.edu.unq.desapp.grupoa.backenddesappapi.service.CryptoService
import ar.edu.unq.desapp.grupoa.backenddesappapi.service.integration.BinanceApi
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

@Service
class CryptoCurrencyServiceImpl : CryptoService {

    private val logger = LoggerFactory.getLogger(CryptoService::class.java)

    @Autowired
    private lateinit var binanceApi: BinanceApi

    val cache: Cache<String, Pair<LocalDateTime, List<Map<String, String>>>> = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build()


    override fun getCryptoQuote(cryptoActiveName: CryptoCurrencyEnum): Float? {
        return try {
            binanceApi.getCryptoCurrencyValue(cryptoActiveName.name)
        } catch (e: Exception) {
            null
        }
    }

    override fun showCryptoAssetQuotes(): Map<String, Float?> {
        return try {
            val allQuotes = binanceApi.showCryptoAssetQuotes()
            val cryptoAssets = CryptoCurrencyEnum.values().map { it.name }
            allQuotes.filterKeys { it in cryptoAssets }
        } catch (e: Exception) {
            emptyMap()
        }

    }

    override fun getCryptoCurrencyValueUSDTtoARS(): BigDecimal? {
        return try {
            val value = binanceApi.getCryptoCurrencyValue("USDTARS").toString()
            BigDecimal(value)
        } catch (e: Exception) {
            null
        }
    }

    override fun showCryptoAssetQuotesLast24Hours(cryptoCurrency: CryptoCurrencyEnum): List<String> {
        logger.info("Trying to get crypto asset quotes in the last 24 hours to ${cryptoCurrency.name}")
        val quotes = mutableListOf<String>()
        try {
            val data = binanceApi.getCryptoCurrencyValueHistory(cryptoCurrency.name, "1h", 24)
            quotes.add("Quotes from the last 24 hours for ${cryptoCurrency.name}:")
            for (quote in data) {
                val dateTime = LocalDateTime.parse(quote["timestamp"], DateTimeFormatter.ISO_DATE_TIME)
                val price = quote["price"]
                quotes.add("Quote Day and Time: $dateTime - Crypto Asset Quote: $price")
            }
        } catch (e: Exception) {
            quotes.add("Error getting quotes for ${cryptoCurrency.name}: ${e.message}")
            logger.error("Error getting quotes for${cryptoCurrency.name}: ${e.message}", e)
        }
        return quotes
    }
    override fun showCryptoAssetQuotesEvery10Minutes(): Map<String, List<String>> {
        val quotesByCrypto = mutableMapOf<String, List<String>>()

        try {
            val cryptoAssets = CryptoCurrencyEnum.values()
            for (crypto in cryptoAssets) {
                val quotes = mutableListOf<String>()

                val cacheKey = crypto.name
                val cachedData = cache.getIfPresent(cacheKey)
                val now = LocalDateTime.now()
                val combinedData: List<Map<String, String>>

                if (cachedData != null && cachedData.first.plusMinutes(10).isAfter(now)) {
                    combinedData = cachedData.second
                } else {
                    val dataFirstInterval = binanceApi.getCryptoCurrencyValueHistory(crypto.name, "5m", 12)
                    val dataSecondInterval = binanceApi.getCryptoCurrencyValueHistory(crypto.name, "5m", 12)
                    combinedData = combineDataEvery10Minutes(dataFirstInterval + dataSecondInterval)
                    cache.put(cacheKey, now to combinedData)
                }

                quotes.add("Quotes every 10 minutes for ${crypto.name}:")
                for (quote in combinedData) {
                    val dateTime = LocalDateTime.parse(quote["timestamp"], DateTimeFormatter.ISO_DATE_TIME)
                    val price = quote["price"]
                    quotes.add("Active Crypto: ${crypto.name}, Price: $price, Update time: $dateTime")
                }
                quotesByCrypto[crypto.name] = quotes
            }
        } catch (e: Exception) {
            quotesByCrypto["error"] = listOf("Error getting quotes: ${e.message}")
        }

        return quotesByCrypto
    }

    fun combineDataEvery10Minutes(data: List<Map<String, String>>): List<Map<String, String>> {
        val combinedData = mutableListOf<Map<String, String>>()

        for (i in data.indices step 2) {
            if (i + 1 < data.size) {
                val item1 = data[i]
                val item2 = data[i + 1]

                val timestamp1 = item1["timestamp"]
                val timestamp2 = item2["timestamp"]

                if (timestamp1 != null && timestamp2 != null) {
                    val price1 = item1["price"]?.toFloatOrNull() ?: continue
                    val price2 = item2["price"]?.toFloatOrNull() ?: continue
                    val averagePrice = (price1 + price2) / 2

                    val combinedItem = mapOf(
                        "timestamp" to timestamp1,
                        "price" to averagePrice.toString()
                    )

                    combinedData.add(combinedItem)
                }
            }
        }

        return combinedData
    }

}