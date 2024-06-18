package dto.res

import domain.Product

data class ProductResDto(
    val id: Long,
    val name: String,
    val price: Int,
    val stock: Int,
)

fun Product.toResDto(): ProductResDto = ProductResDto(
    id = id,
    name = name,
    price = price,
    stock = stock,
)