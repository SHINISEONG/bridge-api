package serializer

import com.fasterxml.jackson.databind.module.SimpleModule
import enums.UserType

class JacksonCustomSerializeModule : SimpleModule() {
    init {
        addSerializer(UserType::class.java, UserTypeSerializer.Serializer())
        addDeserializer(UserType::class.java, UserTypeSerializer.Deserializer())
    }
}