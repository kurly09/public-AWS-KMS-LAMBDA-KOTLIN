import com.amazonaws.services.lambda.runtime.Context

class LambdaHandler constructor(
    private val envelopeEncryptionService: EnvelopeEncryptionService
) {

    constructor() : this(EnvelopeEncryptionService()) // 람다에는 빈 생성자가 필수

    fun encrypt(input: PlainText, context: Context?): EnvelopeEncryptedMessage {
        return envelopeEncryptionService.encrypt(input.message)
    }

    fun decrypt(input: EnvelopeEncryptedMessage, context: Context?): PlainText {
        val result = PlainText()
        result.message = envelopeEncryptionService.decrypt(input)
        return result
    }

}