import com.amazonaws.regions.Regions
import com.amazonaws.services.kms.AWSKMS
import com.amazonaws.services.kms.AWSKMSClientBuilder
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.kms.model.GenerateDataKeyRequest
import com.amazonaws.services.kms.model.GenerateDataKeyResult
import java.nio.ByteBuffer
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class EnvelopeEncryptionService constructor(private val keyId: String) {

    constructor() : this("USE_YOURS")

    private val defaultClient: AWSKMS = AWSKMSClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2).build()
    private val keySpec: String = "AES"

    fun generateDataKey(): GenerateDataKeyResult {
        val dataKeyRequest = GenerateDataKeyRequest()


        dataKeyRequest.keyId = keyId
        dataKeyRequest.keySpec = "AES_256"

        return defaultClient.generateDataKey(dataKeyRequest)
    }

    fun encryptMessage(message: String, dataKey: GenerateDataKeyResult): EnvelopeEncryptedMessage {
        val key = SecretKeySpec(dataKey.plaintext.array(), keySpec)
        val cipher = Cipher.getInstance(keySpec)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val enc = cipher.doFinal(message.toByteArray())
        val cipherText = Base64.getEncoder().encodeToString(enc)
        val envelope = EnvelopeEncryptedMessage()
        envelope.encryptedKey = dataKey.ciphertextBlob.array()
        envelope.cipherText = cipherText

        return envelope
    }

    fun encrypt(message: String): EnvelopeEncryptedMessage {
        try {
            val keyResult: GenerateDataKeyResult = generateDataKey()
            return encryptMessage(message, keyResult)
        } catch (e: Exception) {
            throw RuntimeException("unable to encrypt", e)
        }
    }

    fun decrypt(secretKeySpec: SecretKeySpec, cipherText: String): String {
        val decodeBase64src: ByteArray = Base64.getDecoder().decode(cipherText)
        val cipher: Cipher = Cipher.getInstance(keySpec)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        return String(cipher.doFinal(decodeBase64src))
    }

    fun decryptKey(envelope: EnvelopeEncryptedMessage): SecretKeySpec {
        val encryptedKey: ByteBuffer = ByteBuffer.wrap(envelope.encryptedKey)
        val decryptRequest: DecryptRequest = DecryptRequest().withCiphertextBlob(encryptedKey)
        val plainTextKey: ByteBuffer = defaultClient.decrypt(decryptRequest).plaintext
        return SecretKeySpec(plainTextKey.array(), keySpec)
    }

    fun decrypt(envelope: EnvelopeEncryptedMessage): String {
        try {
            val key: SecretKeySpec = decryptKey(envelope)
            return decrypt(key, envelope.cipherText)
        } catch (e: Exception) {
            throw RuntimeException("unable to decrypt", e)
        }
    }
}
