package ar.edu.unq.desapp.grupoa.backenddesappapi.service.impl

import ar.edu.unq.desapp.grupoa.backenddesappapi.model.Transaction
import ar.edu.unq.desapp.grupoa.backenddesappapi.persistence.TransactionRepository
import ar.edu.unq.desapp.grupoa.backenddesappapi.persistence.UserRepository
import ar.edu.unq.desapp.grupoa.backenddesappapi.service.TransactionService
import ar.edu.unq.desapp.grupoa.backenddesappapi.webservice.dtos.CryptoAssetDTO
import ar.edu.unq.desapp.grupoa.backenddesappapi.webservice.dtos.VolumeOperatedDTO
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
@Transactional
class TransactionServiceImpl : TransactionService {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    override fun getTransactionById(transactionId: Long): Transaction {
        return transactionRepository.findById(transactionId)
            .orElseThrow { EntityNotFoundException("Transaction not found with id: $transactionId") }
    }

    override fun cancelTransaction(userId: Long, transactionId: Long) {
        val user = userRepository.findById(userId).get()
        val transaction = getTransactionById(transactionId)
        transaction.cancelTransaction(user)
        userRepository.save(user)
        transactionRepository.save(transaction)
    }

    override fun getVolumeOperated(userId: Long, startDate: LocalDateTime, endDate: LocalDateTime): VolumeOperatedDTO {
        val transactions = transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate)

        val totalUSD = transactions.sumOf { BigDecimal(it.amount).multiply(getCurrentCryptoPriceInUSD(it.cryptocurrency.name)) }
        val totalARS = totalUSD.multiply(getCurrentUSDtoARSExchangeRate())

        val assets = transactions.groupBy { it.cryptocurrency.name }
            .map { (cryptocurrency, trans) ->
                val totalAmount = trans.sumOf { it.amount }
                val currentPrice = getCurrentCryptoPriceInUSD(cryptocurrency)
                val currentPriceARS = currentPrice.multiply(getCurrentUSDtoARSExchangeRate())
                CryptoAssetDTO(cryptocurrency, totalAmount, currentPrice, currentPriceARS)
            }

        return VolumeOperatedDTO(
            timestamp = LocalDateTime.now(),
            totalUSD = totalUSD,
            totalARS = totalARS,
            assets = assets
        )

    }

    private fun getCurrentUSDtoARSExchangeRate(): BigDecimal {
        return BigDecimal(220)
    }

    private fun getCurrentCryptoPriceInUSD(cryptoAsset: String): BigDecimal {
        return BigDecimal(50000)
    }

    override fun deleteAll() {
        transactionRepository.deleteAll()
    }

}