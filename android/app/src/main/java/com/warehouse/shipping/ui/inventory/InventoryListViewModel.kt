package com.warehouse.shipping.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warehouse.shipping.data.repository.WarehouseRepository
import com.warehouse.shipping.data.local.entity.ProductInventoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InventoryListViewModel(private val repository: WarehouseRepository) : ViewModel() {
    val products: Flow<List<ProductInventoryEntity>> = repository.getAllInventory()

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            repository.softDeleteInventory(id)
        }
    }
}
