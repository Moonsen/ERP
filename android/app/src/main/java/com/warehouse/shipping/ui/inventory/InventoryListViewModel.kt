package com.warehouse.shipping.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warehouse.shipping.data.local.dao.ProductInventoryDao
import com.warehouse.shipping.data.local.entity.ProductInventoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

class InventoryListViewModel(private val dao: ProductInventoryDao) : ViewModel() {
    val products: Flow<List<ProductInventoryEntity>> = dao.getAll()

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            val now = Date().toString() // Use proper ISO formatter in real app
            dao.softDelete(id, now, now)
        }
    }
}
