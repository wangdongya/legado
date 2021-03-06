package io.legado.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.legado.app.data.entities.BookChapter

@Dao
interface BookChapterDao {

    @Query("select * from chapters where bookUrl = :bookUrl")
    fun observeByBook(bookUrl: String): LiveData<List<BookChapter>>

    @Query("SELECT * FROM chapters where bookUrl = :bookUrl and title like '%'||:key||'%'")
    fun liveDataSearch(bookUrl: String, key: String): LiveData<List<BookChapter>>

    @Query("select * from chapters where bookUrl = :bookUrl")
    fun getChapterList(bookUrl: String): List<BookChapter>

    @Query("select * from chapters where bookUrl = :bookUrl and `index` = :index")
    fun getChapter(bookUrl: String, index: Int): BookChapter?

    @Query("select count(url) from chapters where bookUrl = :bookUrl")
    fun getChapterCount(bookUrl: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg bookChapter: BookChapter)

    @Query("delete from chapters where bookUrl = :bookUrl")
    fun delByBook(bookUrl: String)

}