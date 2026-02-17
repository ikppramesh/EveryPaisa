package com.everypaisa.tracker.data.repository

import com.everypaisa.tracker.data.dao.CategoryDao
import com.everypaisa.tracker.data.dao.MerchantMappingDao
import com.everypaisa.tracker.data.entity.CategoryEntity
import com.everypaisa.tracker.data.entity.MerchantMappingEntity
import com.everypaisa.tracker.domain.repository.CategoryRepository
import com.everypaisa.tracker.domain.repository.MerchantMappingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    override fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    override fun getExpenseCategories(): Flow<List<CategoryEntity>> = categoryDao.getExpenseCategories()
    override fun getIncomeCategories(): Flow<List<CategoryEntity>> = categoryDao.getIncomeCategories()
    override fun getCategoryByName(name: String): Flow<CategoryEntity?> = categoryDao.getCategoryByName(name)
    override suspend fun insertCategory(category: CategoryEntity) = categoryDao.insert(category)
    override suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)
    override suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)
}

class MerchantMappingRepositoryImpl @Inject constructor(
    private val merchantMappingDao: MerchantMappingDao
) : MerchantMappingRepository {
    override fun getAllMappings(): Flow<List<MerchantMappingEntity>> = merchantMappingDao.getAllMappings()
    override suspend fun getCategoryForMerchant(merchantName: String): String? = merchantMappingDao.getCategoryForMerchant(merchantName)
    override suspend fun insertMapping(mapping: MerchantMappingEntity) = merchantMappingDao.insert(mapping)
    override suspend fun deleteMapping(mapping: MerchantMappingEntity) = merchantMappingDao.delete(mapping)
}
