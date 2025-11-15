package com.notistorex.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create notification_groups table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `notification_groups` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `name` TEXT NOT NULL,
                    `description` TEXT,
                    `iconName` TEXT NOT NULL DEFAULT 'face',
                    `colorHex` TEXT NOT NULL DEFAULT '#2196F3',
                    `groupType` TEXT NOT NULL DEFAULT 'custom',
                    `isMuted` INTEGER NOT NULL DEFAULT 0,
                    `isActive` INTEGER NOT NULL DEFAULT 1,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `userNotes` TEXT,
                    `priority` INTEGER NOT NULL DEFAULT 0,
                    `totalNotifications` INTEGER NOT NULL DEFAULT 0,
                    `unreadNotifications` INTEGER NOT NULL DEFAULT 0,
                    `todayNotifications` INTEGER NOT NULL DEFAULT 0,
                    `appCount` INTEGER NOT NULL DEFAULT 0,
                    `isSynced` INTEGER NOT NULL DEFAULT 0,
                    `syncTime` INTEGER,
                    `backupId` TEXT
                )
            """)
            
            // Create app_group_memberships table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `app_group_memberships` (
                    `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    `packageName` TEXT NOT NULL,
                    `groupId` TEXT NOT NULL,
                    `addedAt` INTEGER NOT NULL,
                    `isActive` INTEGER NOT NULL DEFAULT 1,
                    `addedBy` TEXT NOT NULL DEFAULT 'user',
                    `userNotes` TEXT,
                    `priority` INTEGER NOT NULL DEFAULT 0,
                    `isMuted` INTEGER NOT NULL DEFAULT 0,
                    `notificationCount` INTEGER NOT NULL DEFAULT 0,
                    `lastNotificationAt` INTEGER,
                    `isSynced` INTEGER NOT NULL DEFAULT 0,
                    `syncTime` INTEGER,
                    `backupId` TEXT,
                    FOREIGN KEY(`packageName`) REFERENCES `all_apps`(`packageName`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`groupId`) REFERENCES `notification_groups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """)
            
            // Create indices for better performance
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_app_group_memberships_packageName` ON `app_group_memberships` (`packageName`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_app_group_memberships_groupId` ON `app_group_memberships` (`groupId`)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_app_group_memberships_packageName_groupId` ON `app_group_memberships` (`packageName`, `groupId`)")
            
            // Insert default groups
            insertDefaultGroups(database)
        }
        
        private fun insertDefaultGroups(database: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            
            // Insert default system groups
            database.execSQL("""
                INSERT OR IGNORE INTO notification_groups 
                (id, name, description, iconName, colorHex, groupType, isMuted, isActive, createdAt, updatedAt, priority)
                VALUES 
                ('unread', 'Unread Notifications', 'Notifications you haven''t read yet', 'email', '#FF6B6B', 'unread', 0, 1, $now, $now, 1),
                ('read', 'Read Notifications', 'Notifications you''ve already read', 'done', '#4CAF50', 'read', 0, 1, $now, $now, 0),
                ('muted', 'Muted Notifications', 'Notifications from muted apps', 'face', '#9E9E9E', 'muted', 1, 1, $now, $now, -1)
            """)
        }
    }
}
