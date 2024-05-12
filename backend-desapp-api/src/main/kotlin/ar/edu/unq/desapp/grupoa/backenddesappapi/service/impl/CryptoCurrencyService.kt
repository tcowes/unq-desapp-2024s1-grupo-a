package ar.edu.unq.desapp.grupoa.backenddesappapi.service.impl
import ar.edu.unq.desapp.grupoa.backenddesappapi.model.CryptoCurrencyEnum
import ar.edu.unq.desapp.grupoa.backenddesappapi.persistence.UserRepository
import ar.edu.unq.desapp.grupoa.backenddesappapi.service.CryptoService
import ar.edu.unq.desapp.grupoa.backenddesappapi.service.integration.BinanceApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class CryptoCurrencyService : CryptoService {

    @Autowired
    private lateinit var binanceApi: BinanceApi
    override fun showCryptoAssetQuotes(): Map<String, Float?> {
        val cryptoassets = CryptoCurrencyEnum.entries
        val quotes = emptyMap<String, Float?>().toMutableMap()
        for (cryptoasset in cryptoassets) {
            try {
                quotes[cryptoasset.name] = binanceApi.getCryptoCurrencyValue(cryptoasset.name)
            } catch (e: Exception) {
                quotes[cryptoasset.name] = null
            }
        }
        return quotes
    }

    override fun showCryptoAssetQuotesLast24Hours(cryptoCurrency: CryptoCurrencyEnum): List<String> {
        val quotes = mutableListOf<String>()
        try {
            val data = binanceApi.getCryptoCurrencyValueHistory(cryptoCurrency.name, 24)
            quotes.add("Quotes from the last 24 hours for ${cryptoCurrency.name}:")
            for (quote in data) {
                val dateTime = LocalDateTime.parse(quote["timestamp"], DateTimeFormatter.ISO_DATE_TIME)
                val price = quote["price"]
                quotes.add("Quote Day and Time: $dateTime - Crypto Asset Quote: $price")
            }
        } catch (e: Exception) {
            quotes.add("Error getting quotes for ${cryptoCurrency.name}: ${e.message}")
        }
        return quotes
    }
}