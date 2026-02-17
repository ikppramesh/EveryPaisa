package com.everypaisa.tracker.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.everypaisa.tracker.data.entity.CategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseSeedCallback : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Seed default categories
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultCategories(db)
        }
    }
    
    private fun seedDefaultCategories(db: SupportSQLiteDatabase) {
        val categories = listOf(
            // Expense categories
            CategoryEntity("Food & Dining", "#FC8019", true, false, 1),
            CategoryEntity("Groceries", "#5AC85A", true, false, 2),
            CategoryEntity("Shopping", "#E91E63", true, false, 3),
            CategoryEntity("Transportation", "#29B6F6", true, false, 4),
            CategoryEntity("Bills & Utilities", "#FFA726", true, false, 5),
            CategoryEntity("Entertainment", "#AB47BC", true, false, 6),
            CategoryEntity("Healthcare", "#EF5350", true, false, 7),
            CategoryEntity("Education", "#42A5F5", true, false, 8),
            CategoryEntity("Personal Care", "#EC407A", true, false, 9),
            CategoryEntity("Travel", "#26C6DA", true, false, 10),
            CategoryEntity("Investments", "#66BB6A", true, false, 11),
            CategoryEntity("Subscriptions", "#7E57C2", true, false, 12),
            CategoryEntity("Transfers", "#78909C", true, false, 13),
            CategoryEntity("Others", "#BDBDBD", true, false, 14),
            // Income categories
            CategoryEntity("Salary", "#4CAF50", true, true, 101),
            CategoryEntity("Refunds", "#8BC34A", true, true, 102),
            CategoryEntity("Cashback", "#CDDC39", true, true, 103),
            CategoryEntity("Interest", "#009688", true, true, 104),
            CategoryEntity("Dividends", "#00BCD4", true, true, 105),
            CategoryEntity("Income", "#4CAF50", true, true, 106)
        )
        
        categories.forEach { category ->
            db.execSQL(
                "INSERT OR REPLACE INTO categories (name, color, is_system, is_income, display_order) VALUES (?, ?, ?, ?, ?)",
                arrayOf(category.name, category.color, if (category.isSystem) 1 else 0, if (category.isIncome) 1 else 0, category.displayOrder)
            )
        }
    }
}
