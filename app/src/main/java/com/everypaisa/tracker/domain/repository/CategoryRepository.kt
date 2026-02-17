package com.everypaisa.tracker.domain.repository

import com.everypaisa.tracker.data.entity.CategoryEntity
import com.everypaisa.tracker.data.entity.MerchantMappingEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<CategoryEntity>>
    fun getExpenseCategories(): Flow<List<CategoryEntity>>
    fun getIncomeCategories(): Flow<List<CategoryEntity>>
    fun getCategoryByName(name: String): Flow<CategoryEntity?>
    suspend fun insertCategory(category: CategoryEntity)
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(category: CategoryEntity)
}

interface MerchantMappingRepository {
    fun getAllMappings(): Flow<List<MerchantMappingEntity>>
    suspend fun getCategoryForMerchant(merchantName: String): String?
    suspend fun insertMapping(mapping: MerchantMappingEntity)
    suspend fun deleteMapping(mapping: MerchantMappingEntity)
}
