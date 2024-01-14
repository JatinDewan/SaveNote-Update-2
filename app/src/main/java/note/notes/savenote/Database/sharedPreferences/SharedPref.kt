package note.notes.savenote.Database.sharedPreferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ISharedPreferences{
    val getCompletedChecklistEntries:Flow<Boolean>

    val getLayoutInformation: Flow<Boolean>

    val getSortByView: Flow<Int>
    suspend fun setCompletedChecklistLayout(showCompleted: Boolean)
    suspend fun setLayoutInformation(setLayout: Boolean)
    suspend fun setSortByView(view: Int)
}
class SharedPref(private val context: Context): ISharedPreferences {
    companion object{
        private val Context.datastore: DataStore<Preferences> by preferencesDataStore("sort")
        val SORT_BY_VIEW = intPreferencesKey("sortByView")
        val SHOW_COMPLETED = booleanPreferencesKey("complete")
        val LAYOUT_VIEW = booleanPreferencesKey("layout")
    }

    override val getCompletedChecklistEntries: Flow<Boolean>
        get() = context.datastore.data.map {
            preferences -> preferences[SHOW_COMPLETED] ?: true
        }

    override val getLayoutInformation: Flow<Boolean>
        get() = context.datastore.data.map {
            preferences -> preferences[LAYOUT_VIEW] ?: true
        }

    override val getSortByView: Flow<Int>
        get() = context.datastore.data.map {
            preferences -> preferences[SORT_BY_VIEW] ?: 0
        }

    override suspend fun setCompletedChecklistLayout(showCompleted: Boolean) {
        context.datastore.edit{
            preferences -> preferences[SHOW_COMPLETED] = showCompleted
        }
    }

    override suspend fun setLayoutInformation(setLayout: Boolean) {
        context.datastore.edit{
            preferences -> preferences[LAYOUT_VIEW] = setLayout
        }
    }

    override suspend fun setSortByView(view: Int) {
        context.datastore.edit{
            preferences -> preferences[SORT_BY_VIEW] = view
        }
    }
}