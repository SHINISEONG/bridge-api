package contorller

import domain.Product
import dto.req.ProductReqDto
import dto.res.ApiCommonResDto
import dto.res.toResDto
import io.github.shiniseong.bridgeApi.annotation.method.Delete
import io.github.shiniseong.bridgeApi.annotation.method.Get
import io.github.shiniseong.bridgeApi.annotation.method.Patch
import io.github.shiniseong.bridgeApi.annotation.method.Post
import io.github.shiniseong.bridgeApi.annotation.param.JsonBody
import io.github.shiniseong.bridgeApi.annotation.param.PathVariable

class ProductController {
    @Get("/:id")
    fun getProductById(@PathVariable("id") id: Long) = ApiCommonResDto(
        status = 0,
        message = "success",
        data = Product(
            id = 1,
            name = "Apple",
            price = 100,
            stock = 10,
        ),
    )

    @Post("")
    fun createProduct() = ApiCommonResDto(
        status = 0,
        message = "success",
        data = Product(
            id = 1,
            name = "Apple",
            price = 100,
            stock = 10,
        ),
    )

    @Post("/all")
    fun createProducts(@JsonBody products: List<ProductReqDto>) = ApiCommonResDto(
        status = 0,
        message = "success",
        data = products.mapIndexed { index, it -> it.toDomain(index + 1L) }.map { it.toResDto() }
    )

    @Patch("/:id/stock/:stock")
    fun updateProductStock(@PathVariable("id") id: Long, @PathVariable("stock") stock: Int) = ApiCommonResDto(
        status = 0,
        message = "success",
        data = Product(
            id = id,
            name = "Apple",
            price = 100,
            stock = stock,
        ),
    )

    @Delete("/:id")
    fun deleteProduct(@PathVariable("id") id: Long) = ApiCommonResDto(
        status = 0,
        message = "delete success",
        data = Unit,
    )
}