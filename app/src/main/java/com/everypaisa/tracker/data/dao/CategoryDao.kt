package com.everypaisa.tracker.data.dao

import androidx.room.*
import com.everypaisa.tracker.data.entity.CategoryEntity
import com.everypaisa.tracker.data.entity.MerchantMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories ORDER BY display_order, name")
    fun getAllCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE is_income = 0 ORDER BY display_order")
    fun getExpenseCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE is_income = 1 ORDER BY display_order")
    fun getIncomeCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE name = :name")
    fun getCategoryByName(name: String): Flow<CategoryEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)
    
    @Update
    suspend fun update(category: CategoryEntity)
    
    @Delete
    suspend fun delete(category: CategoryEntity)
    
    @Query("DELETE FROM categories WHERE is_system = 0")
    suspend fun deleteCustomCategories()
}

@Dao
interface MerchantMappingDao {
    
    @Query("SELECT * FROM merchant_mappings")
    fun getAllMappings(): Flow<List<MerchantMappingEntity>>
    
    @Query("SELECT category FROM merchant_mappings WHERE merchant_name = :merchantName")
    suspend fun getCategoryForMerchant(merchantName: String): String?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: MerchantMappingEntity)
    
    @Delete
    suspend fun delete(mapping: MerchantMappingEntity)
    
    @Query("DELETE FROM merchant_mappings")
    suspend fun deleteAll()
}
