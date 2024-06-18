package serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import enums.UserType

class UserTypeSerializer {
    class Serializer : StdSerializer<UserType>(UserType::class.java) {
        override fun serialize(p0: UserType?, p1: JsonGenerator?, p2: SerializerProvider?) {
            p1?.writeNumber(p0?.value ?: -99)
        }
    }

    class Deserializer : StdDeserializer<UserType>(UserType::class.java) {
        override fun deserialize(p0: JsonParser?, p1: DeserializationContext?): UserType {
            return UserType.from(p0?.intValue ?: -99)
        }
    }
}
