package com.user.ncard.vo

import java.io.Serializable

/**
 * Created by Pham on 4/1/18.
 */
data class GiftItem(val id: Int, val title: String, val description: String, val price: Price?,
                    val regularPrice: Price?, val salePrice: Price?, val sku: String?, val virtual: Boolean?,
                    val onSale: Boolean?, val featured: Boolean?, val categories: List<Category>?, val imageUrls: List<String>?,
                    val inStock: Boolean?, val stockQuantity: Int?, val vendorName: String, var count: Int?) : Serializable

data class Price(val currency: String, val amount: Double?) : Serializable
data class Category(val id: Int, val name: String, val slug: String) : Serializable

data class DisplayCategory(val category: Category, val itemList: ArrayList<GiftItem>)