package com.warehouse.shipping.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warehouse.shipping.data.repository.WarehouseRepository
import com.warehouse.shipping.data.local.entity.ProductInventoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class InventoryEditViewModel(private val repository: WarehouseRepository) : ViewModel() {
    private val _product = MutableStateFlow<ProductInventoryEntity?>(null)
    val product: StateFlow<ProductInventoryEntity?> = _product

    fun loadProduct(id: String?) {
        if (id == null || id == "new") return
        viewModelScope.launch {
            _product.value = repository.getInventoryById(id)
        }
    }

    fun saveProduct(
        id: String?,
        name: String,
        code: String?,
        barcode: String?,
        l: Double, w: Double, h: Double, weight: Double,
        customSpecs: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val finalId = if (id == null || id == "new") UUID.randomUUID().toString() else id
            val newProduct = ProductInventoryEntity(
                id = finalId,
                product_code = if (code.isNullOrBlank()) null else code,
                name = name,
                barcode = if (barcode.isNullOrBlank()) null else barcode,
                length_cm = l,
                width_cm = w,
                height_cm = h,
                weight_g = weight,
                custom_specs = customSpecs,
                created_at = _product.value?.created_at ?: "",
                updated_at = "", // Repository will handle this
                deleted_at = null,
                machine_id = ""
            )
            repository.insertInventory(newProduct)
            onSuccess()
        }
    }
}
