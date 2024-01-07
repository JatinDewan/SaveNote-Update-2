package note.notes.savenote.Database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SharedPref(private val context: Context) {
    companion object{
        private val Context.datastore: DataStore<Preferences> by preferencesDataStore("sort")
        val SORT_BY_VIEW = intPreferencesKey("sortByView")
        val SHOW_COMPLETED = booleanPreferencesKey("complete")
        val LAYOUT_VIEW = booleanPreferencesKey("layout")
    }

    val getCompleted: Flow<Boolean> = context.datastore.data
        .map {
                preferences -> preferences[SHOW_COMPLETED] ?: true
        }

    val getLayout: Flow<Boolean> = context.datastore.data
        .map {
                preferences -> preferences[LAYOUT_VIEW] ?: true
        }

    val getView: Flow<Int> = context.datastore.data
        .map {
                preferences -> preferences[SORT_BY_VIEW] ?: 0
        }



    suspend fun saveShowCompleted(sort:Boolean) {
        context.datastore.edit{
                preferences -> preferences[SHOW_COMPLETED] = sort
        }
    }

    suspend fun saveLayout(layout:Boolean) {
        context.datastore.edit{
                preferences -> preferences[LAYOUT_VIEW] = layout
        }
    }

    suspend fun saveSortView(sortView:Int) {
        context.datastore.edit{
                preferences -> preferences[SORT_BY_VIEW] = sortView
        }
    }
}